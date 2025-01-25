
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Day20 {

	private static int DAY_NUM = 20;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;
	private static int numThreads = 16;
	//rivate static int numThreads = 1;

	private static int REPORT_INTERVAL = 1000000000;
	//private static int REPORT_INTERVAL = 100000;
	//private static int REPORT_INTERVAL = 1;


	public static enum Orientation {
		Up,
		Down,
		Left,
		Right,
	}

	public static class Path {
		public List<FileParser.Pos> path;

		public Path() {
			path = new ArrayList<FileParser.Pos>();
		}

		public Path(List<FileParser.Pos> p) {
			path = p;
		}

		public Path add(FileParser.Pos pos) {
			var newPath = new ArrayList<FileParser.Pos>(path);
			newPath.add(pos);
			return new Path(newPath);
		}

		@Override
		public String toString() {
			return String.format("[path (%d)=%s]", path.size(), path);
		}
	}

	public static class Matrix {
		public final int width;
		public final int height;
		public char[][] cells;

		public Matrix(FileParser.MatrixParser fp) {
			width = fp.getWidth();
			height = fp.getHeight();
			cells = new char[width][height];

			for (int w = 0 ; w < width ; w++) {
				for (int h = 0 ; h < height ; h++) {
					cells[h][w] = fp.getCell(w, h);
				}
			}
		}

		public char getCell(int x, int y) { return cells[y][x]; }
		public char getCell(FileParser.Pos pos) { return getCell(pos.x, pos.y); }
		public void setCell(int x, int y, char c) { cells[y][x] = c; }
		public void setCell(FileParser.Pos pos, char c) { setCell(pos.x, pos.y, c); }
		public int getHeight() { return height; }
		public int getWidth() { return width; }

		public void draw() {
			for (int h = 0 ; h < height ; h++) {
				for (int w = 0 ; w < width ; w++) {
					System.out.format("%c", cells[h][w]);
				}
				System.out.format("\n");
			}
		}
	}


        public static class ScanContext {
                public Matrix			matrix;
		public FileParser.Pos 		startPos;
		public FileParser.Pos 		endPos;
		public Orientation		currOrient;
		public AtomicLong		best_score;
		public AtomicLong		num_queued;
		public AtomicLong		num_processed;
		public ConcurrentHashMap<String, Long>	processedPos;
		public List<FileParser.Pos>	bestPath;
		public Queue<EvalRec>		evalQueue;
		public FileParser.Pos		hackStartPos;
		public FileParser.Pos		hackEndPos;
		public Orientation		hackOrient;


                public ScanContext(Matrix m, FileParser.Pos hsp, FileParser.Pos hep, Orientation ho) {
                        matrix = m;
			best_score = new AtomicLong();
			num_queued = new AtomicLong();
			num_processed = new AtomicLong();
			processedPos = new ConcurrentHashMap<String, Long>();
			bestPath = Collections.synchronizedList(new ArrayList<FileParser.Pos>());
			evalQueue = new LinkedList<EvalRec>();
			hackStartPos = hsp;
			hackEndPos = hep;
			hackOrient = ho;
                }

		@Override
		public String toString() {
			return String.format("[sc matrix=%dx%d, start=%s, end=%s]",
				 matrix.getWidth(), matrix.getHeight(), startPos, endPos);
		}


		public boolean inBounds(FileParser.Pos currPos) {
			if (currPos.x < 0 || currPos.x >= matrix.getWidth())
				return false;
			if (currPos.y < 0 || currPos.y >= matrix.getHeight())
				return false;
			return true;
		}


		public boolean probeNeighbor(FileParser.Pos nextPos) throws Exception {
			if (!inBounds(nextPos)) {
				return false;
			}

			char c = matrix.getCell(nextPos);	
			if (c == '#') {
				return false;
			}

			if (c == 'E') {
				// System.out.format("[%d] probeNeighbor: found neighbor in horizon\n",
				// Thread.currentThread().threadId());
				return true;
			}

			if (c == '.') {
				return true;
			}

			throw new Exception(String.format("YO: bad char %c in map", c));
		}


		public boolean exploreNeighbor(FileParser.Pos currPos, Path currPath, Orientation currOrient, long currCost,
					FileParser.Pos nextPos, Orientation nextOrient) throws Exception {

			// if we already hit this neighbor in this path, we have looped, and should end this path.
			if (currPath.path.contains(nextPos)) {
				// System.out.format("[%d] exploreNeighbor: skipping %s already-visited\n",
				//  Thread.currentThread().threadId(), nextPos);
				return false;
			}

			// make sure the neighbor is an actual open space.
			if (!probeNeighbor(nextPos)) {
				return false;
			}

			while (true) {
				boolean skip = false;
				long costDiff = 0;
				Long pathCost = processedPos.get(String.format("%s:%s", nextPos, nextOrient));
				if (pathCost != null) {
					costDiff = currCost - pathCost;
					if (costDiff > 1000) {
						skip = true;
					}
				} else {
					costDiff = -currCost;
				}

				if (skip) {
					// System.out.format("[%d] exploreNeighbor: skipping %s:%s (costdiff=%d) due to better path there already (best=%d, curr=%d)\n",
					// Thread.currentThread().threadId(), nextPos, nextOrient, costDiff, pathCost, currCost);
					return false;
				} 

				if (pathCost == null) {
					if (processedPos.putIfAbsent(String.format("%s:%s", nextPos, nextOrient), currCost) == null) {
						// System.out.format("[%d] exploreNeighbor: setting best path to %s=%d\n",
						// Thread.currentThread().threadId(), nextPos, currCost);
						break;
					}
				} else {
					if ((pathCost == currCost) || processedPos.replace(String.format("%s:%s", nextPos, nextOrient), pathCost, currCost)) {
						// System.out.format("[%d] exploreNeighbor: replacing best path to %s=%d (prev=%d)\n",
						// Thread.currentThread().threadId(), nextPos, currCost, pathCost);
						break;
					}
				}

				System.out.format("[%d] exploreNeighbor: trying again for sync for %s\n",
					 Thread.currentThread().threadId(), currPos);
			} 

			// QUEUE this next item for processing.
			long nextCost = currCost + 1;
			Path nextPath = currPath.add(currPos);
			queueEvaluation(nextPos, nextPath, nextOrient, nextCost);
			return true;
		}
	

		public void evaluate(FileParser.Pos currPos, Path currPath, 
						Orientation currOrient, long currCost) throws Exception {

			// Is this the end?
			char c = matrix.getCell(currPos);
			if (c == 'E') {
				// We found the end!
				Path p = currPath.add(currPos);
				//System.out.format("[%d] evaluate: found END score=%d, path=%s\n",
			//		 Thread.currentThread().threadId(), currCost, p);
				System.out.format("[%d] evaluate: found END score=%d\n",
					 Thread.currentThread().threadId(), currCost);

				// Update the best score.
				while (true) {
					long best = best_score.get();
					if ((best != 0) && (currCost > best)) {
						System.out.format("[%d] evaluate: SCORE %d is not the best (%d)\n",
							 Thread.currentThread().threadId(), currCost, best);
						break;
					}

					if ((best != currCost) && !best_score.compareAndSet(best, currCost)) {
						System.out.format("[%d] evaluate: whoops, somebody updating at same time, lets try again\n",
							 Thread.currentThread().threadId());
						continue;
					}

					if (best != currCost) {
						bestPath.clear();
						System.out.format("[%d] evaluate: SCORE %d is updated\n",
							 Thread.currentThread().threadId(), currCost);
					}

					for (FileParser.Pos bpos : p.path) {
						bestPath.add(bpos);
					}

					System.out.format("[%d] evaluate: SCORE %d is NOW the best (prev=%d)\n",
						 Thread.currentThread().threadId(), currCost, best);
					break;
				}

				return;
			}

			// Is this the hack tunnel?
			if (currPos.equals(hackStartPos)) {
				long nextCost = currCost + 1;
				//Path nextPath = currPath.add(currPos);
				System.out.format("[%d] exploreNeighbor: TAKING HACK at %s to %s\n",
				  Thread.currentThread().threadId(), hackStartPos, hackEndPos);
				//queueEvaluation(hackEndPos, nextPath, hackOrient, nextCost);
				exploreNeighbor(currPos, currPath, currOrient, nextCost, hackEndPos, hackOrient);
			}

			// We are not at the end, time to explore the relevent neighbors.
			FileParser.Pos up = currPos.up();
			exploreNeighbor(currPos, currPath, currOrient, currCost, up, Orientation.Up);
			FileParser.Pos down = currPos.down();
			exploreNeighbor(currPos, currPath, currOrient, currCost, down, Orientation.Down);
			FileParser.Pos left = currPos.left();
			exploreNeighbor(currPos, currPath, currOrient, currCost, left, Orientation.Left);
			FileParser.Pos right = currPos.right();
			exploreNeighbor(currPos, currPath, currOrient, currCost, right, Orientation.Right);
		}


		public static class EvalRec {
			public final FileParser.Pos 	currPos;
			public final Path 		currPath;
			public final Orientation 	currOrient;
			public final long 		currCost;
			public final boolean		report;

			public EvalRec(FileParser.Pos cpos, Path cpath, Orientation co, long cc, boolean r) {
				currPos = cpos;
				currPath = cpath;
				currOrient = co;
				currCost = cc;
				report = r;
			}
		}

		public void queueEvaluation(FileParser.Pos currPos, Path currPath, Orientation currOrient, long currCost) throws Exception {
			long q = num_queued.incrementAndGet();
			boolean reportNow = (q % REPORT_INTERVAL) == 0;
			if (reportNow) {
				//System.out.format("[%d] queue: pos=%s, path=%s, orient=%s, cost=%d, q=%d, p=%d\n",
			//		 Thread.currentThread().threadId(), currPos, currPath, currOrient, currCost,
			//			num_queued.get(), num_processed.get());
			}

			evalQueue.add(new EvalRec(currPos, currPath, currOrient, currCost, reportNow));
		}

		public void waitForCompletion() {
			while (!evalQueue.isEmpty()) {
				EvalRec er = evalQueue.remove();

				long processed = num_processed.incrementAndGet();
				if (er.report) {
					System.out.format("[%d] dispatch: starting %s/%s (%d), q=%d, p=%d, bestscore=%d\n",
						 Thread.currentThread().threadId(),
						 er.currPos, er.currOrient, er.currCost,
						 num_queued.get(), num_processed.get(), best_score.get());
				}

				try {
					evaluate(er.currPos, er.currPath, er.currOrient, er.currCost);
				} catch (Exception e) {
					System.out.format("[%d] dispatch: exception in task: %s\n",
						 Thread.currentThread().threadId(), e);
					e.printStackTrace();
				}

				long remaining = num_queued.get() - num_processed.get();
				if (er.report) {
					/* System.out.format("[%d] dispatch: ending %s/%s, q=%d, p=%d, remaining=%d, score=%d\n",
					 Thread.currentThread().threadId(),
					er.currPos, er.currOrient,
					 num_queued.get(), num_processed.get(),
					 remaining, best_score.get()); */
				}
			}

			System.out.format("[%d] dispatch: bounced on 0, q=%d, p=%d, bestscore=%d\n",
				Thread.currentThread().threadId(),
				 num_queued.get(), num_processed.get(), best_score.get());
		}
	}



	public static ExecutorService 	executor;
	public static AtomicLong		num_queued_ex = new AtomicLong(0);
	public static AtomicLong		num_processed_ex = new AtomicLong(0);
	public static Map<Long, Long> 	best_map = new HashMap<Long, Long>();
	public static long 		best_score = 0;
	public static long 		over_thresh = 0;

	/*
	*/
	public static void process(FileParser.Pos wall1, FileParser.Pos query0, FileParser.Pos query2, Orientation queryOrient, 
					FileParser.MatrixParser fp, FileParser.Pos startPos, FileParser.Pos endPos, 
					long baseline_score) throws Exception { 
		Matrix mv = new Matrix(fp);
		//mv.setCell(wall1, '.');

		ScanContext scv = new ScanContext(mv, query0, query2, queryOrient); 
		scv.startPos = startPos;
		scv.endPos = endPos;
		System.out.format("[%s] process-variant start: sc=%s, wall1=%s query0=%s, query2=%s, ovr_thresh=%d\n", 
			Thread.currentThread().threadId(), scv, wall1, query0, query2, over_thresh);
		//mv.draw();

		// Grab the baseline.
		scv.queueEvaluation(scv.startPos, new Path(), Orientation.Right, 0);
		scv.waitForCompletion();
		long variant_score = scv.best_score.get();
		long savings = (variant_score == 0) ? 0 : (baseline_score - variant_score);
		System.out.format("[%s] process-variant: Cost=%d (%d), w1=%s, q0=%s, q2=%s\n",
			 Thread.currentThread().threadId(), variant_score, savings, wall1, query0, query2);
		if (variant_score < best_score) {
			System.out.format("[%s] process-variant: NEW HIGH=%d (%d from baseline)\n", 
				Thread.currentThread().threadId(), variant_score, savings);
			best_score = variant_score;
		}

		if (savings > 100) {
			over_thresh++;
			System.out.format("[%s] process-variant: OVER THRESH=%d (tot=%d)\n", 
				Thread.currentThread().threadId(), savings, over_thresh);
		}

		long currCount = best_map.getOrDefault(savings, 0L);
		currCount++;
		best_map.put(savings, currCount);
	}

		
	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                FileParser.MatrixParser fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();

		FileParser.Pos startPos = null;
		FileParser.Pos endPos = null;
		List<FileParser.Pos> walls = new ArrayList<FileParser.Pos>();
		for (int h = 0 ; h < fp.getHeight() ; h++) {
			for (int w = 0 ; w < fp.getWidth() ; w++) {
				FileParser.Pos pos = new FileParser.Pos(w, h);
				char c = fp.getCell(pos);
				if (c == 'S') {
					startPos = pos;
				} else if (c == 'E') {
					endPos = pos;
				} else if (c == '#') {
					walls.add(pos);
				}
			}
		}

		Matrix m = new Matrix(fp);
               	ScanContext sc = new ScanContext(m, null, null, Orientation.Right); 
		sc.startPos = startPos;
		sc.endPos = endPos;
                System.out.format("Analysis start: sc=%s, part1=%b, wals=%s\n", sc, part1, walls);
		m.draw();

		// Grab the baseline.
		sc.queueEvaluation(sc.startPos, new Path(), Orientation.Right, 0);
		sc.waitForCompletion();
		final long baseline_score = sc.best_score.get();
                System.out.format("Baseline Cost=%d\n", baseline_score);

		if (true) {
			Matrix testm = new Matrix(fp);
			ScanContext testsc = new ScanContext(testm, new FileParser.Pos(7,7), new FileParser.Pos(5,7), Orientation.Left);
			testsc.startPos = startPos;
			testsc.endPos = endPos;
			testsc.queueEvaluation(sc.startPos, new Path(), Orientation.Right, 0);
			testsc.waitForCompletion();
			final long test_score = testsc.best_score.get();
			System.out.format("Test Cost=%d\n", test_score);
		}

		// iterate through the wall permutations.
		executor = Executors.newFixedThreadPool(numThreads);
		best_score = baseline_score;
		for (int w = 0 ; w < walls.size() ; w++) {
			FileParser.Pos wall1 = walls.get(w);

			for (Orientation orient : Orientation.values()) { 
				FileParser.Pos query0 = null;
				FileParser.Pos query2 = null;
				switch (orient) {
				case Orientation.Up:
					query0 = wall1.down();
					query2 = wall1.up();
					break;
				case Orientation.Down:
					query0 = wall1.up();
					query2 = wall1.down();
					break;
				case Orientation.Left:
					query0 = wall1.right();
					query2 = wall1.left();
					break;
				case Orientation.Right:
					query0 = wall1.left();
					query2 = wall1.right();
					break;
				}

				if ((query0.x < 0) || (query0.x >= fp.getWidth()) || (query0.y < 0) || (query0.y >= fp.getHeight()) ||
					(query2.x < 0) || (query2.x >= fp.getWidth()) || (query2.y < 0) || (query2.y >= fp.getHeight())) {
					System.out.format("[%s] variant-queue: SKIPPING due to q1 or q2 OOB w=%d w1=%s, q0=%s, q2=%s, q=%d, p=%d\n",
						 Thread.currentThread().threadId(), w, wall1, query0, query2, num_queued_ex.get(), num_processed_ex.get());
					continue;
				}

				var endChars = Set.of('.', 'S', 'E');
				char cv0 = fp.getCell(query0);
				char cv2 = fp.getCell(query2);
				if (!(endChars.contains(cv0) && endChars.contains(cv2))) {
					System.out.format("[%s] variant-queue: SKIPPING due to w2 not clear w=%d w1=%s, q0=%s (%c), q2=%s (%c), q=%d, p=%d\n",
						 Thread.currentThread().threadId(), w, wall1, query0, cv0, query2, cv2, num_queued_ex.get(), num_processed_ex.get());
					continue;
				}

				long q = num_queued_ex.incrementAndGet();
				System.out.format("[%s] variant-queue: w=%d orient=%s, w1=%s, q0=%s, q2=%s, q=%d, p=%d\n",
					 Thread.currentThread().threadId(), w, orient, wall1, query0, query2, num_queued_ex.get(), num_processed_ex.get());

				final FileParser.Pos w1_pass = wall1;
				final FileParser.Pos q0_pass = query0;
				final FileParser.Pos q2_pass = query2;
				final FileParser.Pos start_pass = startPos;
				final FileParser.Pos end_pass = endPos;
				executor.submit(new Runnable() {
					    public void run() {
						long queued = num_queued_ex.get();
						long processed = num_processed_ex.get();
						long remaining = queued - processed;
						System.out.format("[%s] variant-dispatch: starting w1=%s, q0=%s, q2=%s, q=%d, p=%d, remaining=%d\n",
							 Thread.currentThread().threadId(), w1_pass, q0_pass, q2_pass,
							 queued, processed, remaining);

						try {
							process(w1_pass, q0_pass, q2_pass, orient, fp, start_pass, end_pass, baseline_score);
						} catch (Exception e) {
							e.printStackTrace();
						}

						queued = num_queued_ex.get();
						processed = num_processed_ex.incrementAndGet();
						remaining = queued - processed;
						System.out.format("[%s] variant-dispatch: ending w1=%s, q0=%s, q2=%s, q=%d, p=%d, remaining=%d\n",
							 Thread.currentThread().threadId(), w1_pass, q0_pass, q2_pass,
							 queued, processed, remaining);

						if (remaining == 0) {
							System.out.format("[%d] variant-dispatch: bounced on 0, q=%d, p=%d\n",
								Thread.currentThread().threadId(), queued, processed);
							System.out.format("[%d] variant-dispatch: num over thresh=%d\n", 
								Thread.currentThread().threadId(), over_thresh);
							System.out.format("[%d] variant-dispatch: Savings Map=%s\n", 
								Thread.currentThread().threadId(), best_map);
						//	executor.shutdown();
						}
					} 
				}); 
			}
		}
 
		// WAIT for all threads to end
		try {
			System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
				Thread.currentThread().threadId());
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
                System.out.format("num over thresh=%d\n", over_thresh);
                System.out.format("Savings Map=%s\n", best_map);
	}
}
