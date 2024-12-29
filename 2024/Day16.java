
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Day16 {

	private static int DAY_NUM = 16;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;
	private static int numThreads = 1;

	//private static int REPORT_INTERVAL = 1000000000;
	private static int REPORT_INTERVAL = 100;
	//private static int REPORT_INTERVAL = 1;

	private static ExecutorService executor;

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


        public static class ScanContext {
                public FileParser.MatrixParser  fp;
		public FileParser.Pos 		startPos;
		public FileParser.Pos 		endPos;
		public Orientation		currOrient;
		public AtomicLong		best_score;
		public AtomicLong		num_queued;
		public AtomicLong		num_processed;
//		public ConcurrentHashMap<FileParser.Pos, Long>	processedPos;
		public ConcurrentHashMap<String, Long>	processedPos;
		public ConcurrentHashMap<FileParser.Pos, Long>	bestPathSet;

                public ScanContext(FileParser.MatrixParser f) {
                        fp = f;
			best_score = new AtomicLong();
			num_queued = new AtomicLong();
			num_processed = new AtomicLong();
			//processedPos = new ConcurrentHashMap<FileParser.Pos, Long>();
			processedPos = new ConcurrentHashMap<String, Long>();
			bestPathSet = new ConcurrentHashMap<FileParser.Pos, Long>();
                }

		@Override
		public String toString() {
			return String.format("[sc matrix=%dx%d, start=%s, end=%s]", fp.getWidth(), fp.getHeight(), startPos, endPos);
		}
        }


 	public static boolean inBounds(FileParser.Pos currPos, int width, int height) {
                if (currPos.x < 0 || currPos.x >= width)
                        return false;
                if (currPos.y < 0 || currPos.y >= height)
                        return false;
                return true;
        }


	public static boolean probeNeighbor(ScanContext sc, FileParser.Pos nextPos) throws Exception {
		if (!inBounds(nextPos, sc.fp.getWidth(), sc.fp.getHeight())) {
//			System.out.format("[%d] probeNeighbor: hit OOB %s\n",
//				 Thread.currentThread().threadId(), nextPos);
			return false;
		}

		char c = sc.fp.getCell(nextPos);	
		if (c == '#') {
//			System.out.format("[%d] probeNeighbor: hit Wall %s\n",
//				 Thread.currentThread().threadId(), nextPos);
			return false;
		}

		if (c == 'E') {
//			System.out.format("[%d] probeNeighbor: found neighbor in horizon\n",
//				 Thread.currentThread().threadId());
			return true;
		}

		if (c == '.') {
//			System.out.format("[%d] probeNeighbor: found movable space\n",
//				 Thread.currentThread().threadId());
			return true;
		}

		throw new Exception(String.format("YO: bad char %c in map", c));
	}


	/*
	*/
	

	/*
	*/
	public static boolean exploreNeighbor(ScanContext sc, FileParser.Pos currPos, Path currPath, Orientation currOrient, long currCost,
				FileParser.Pos nextPos, Orientation nextOrient) throws Exception {
		// Only 90 degree turns are possible - 180 is illegal
		long nextCost = currCost + 1;
		if (currOrient != nextOrient) {
			nextCost += 1000;
		}

		// if we already hit this neighbor in this path, we have looped, and should end this path.
		if (currPath.path.contains(nextPos)) {
//			System.out.format("[%d] exploreNeighbor: skipping %s already-visited\n",
//				 Thread.currentThread().threadId(), nextPos);
			return false;
		}

		// make sure the neighbor is an actual open space.
		if (!probeNeighbor(sc, nextPos)) {
			return false;
		}

		while (true) {
			boolean skip = false;
			long costDiff = 0;
			Long pathCost = sc.processedPos.get(String.format("%s:%s", nextPos, nextOrient));
			if (pathCost != null) {
				costDiff = currCost - pathCost;
				if (costDiff > 1000) {
					skip = true;
				}
			} else {
				costDiff = -currCost;
			}

			if (skip) {
			//	System.out.format("[%d] exploreNeighbor: skipping %s:%s (costdiff=%d) due to better path there already (best=%d, curr=%d)\n",
			//	 Thread.currentThread().threadId(), nextPos, nextOrient, costDiff, pathCost, currCost);
				return false;
			} 

			if (pathCost == null) {
				//if (sc.processedPos.putIfAbsent(nextPos, currCost) == null) {
				if (sc.processedPos.putIfAbsent(String.format("%s:%s", nextPos, nextOrient), currCost) == null) {
					//System.out.format("[%d] exploreNeighbor: setting best path to %s=%d\n",
					// Thread.currentThread().threadId(), nextPos, currCost);
					break;
				}
			} else {
				if ((pathCost == currCost) || sc.processedPos.replace(String.format("%s:%s", nextPos, nextOrient), pathCost, currCost)) {
					//System.out.format("[%d] exploreNeighbor: replacing best path to %s=%d (prev=%d)\n",
				//		Thread.currentThread().threadId(), nextPos, currCost, pathCost);
					break;
				}
			}

			System.out.format("[%d] exploreNeighbor: trying again for sync for %s\n",
				 Thread.currentThread().threadId(), currPos);
		} 

		// QUEUE this next item for processing.
		Path nextPath = currPath.add(currPos);
		queueEvaluation(sc, nextPos, nextPath, nextOrient, nextCost);
		return true;
	}
	

	/*
	*/
	public static void evaluate(ScanContext sc, FileParser.Pos currPos, Path currPath, 
		Orientation currOrient, long currCost) throws Exception {

		// Evaluate current node.
		char c = sc.fp.getCell(currPos);
		if (c == 'E') {
			// We found the end!
			Path p = currPath.add(currPos);
			System.out.format("[%d] evaluate: found END score=%d, path=%s\n",
				 Thread.currentThread().threadId(), currCost, p);

			// Update the best score.
			while (true) {
				long best = sc.best_score.get();
				if ((best != 0) && (currCost > best)) {
					System.out.format("[%d] evaluate: SCORE %d is not the best (%d)\n",
						 Thread.currentThread().threadId(), currCost, best);
					break;
				}

				if ((best != currCost) && !sc.best_score.compareAndSet(best, currCost)) {
					System.out.format("[%d] evaluate: whoops, somebody updating at same time, lets try again\n",
						 Thread.currentThread().threadId());
					continue;
				}

				if (best != currCost) {
					sc.bestPathSet.clear();
					System.out.format("[%d] evaluate: SCORE %d is updated\n",
						 Thread.currentThread().threadId(), currCost);
				}

				for (FileParser.Pos bpos : p.path) {
					sc.bestPathSet.put(bpos, 0L);
				}

				System.out.format("[%d] evaluate: SCORE %d is NOW the best (prev=%d), pathset=%d %s\n",
					 Thread.currentThread().threadId(), currCost, best, sc.bestPathSet.size(), sc.bestPathSet);
				break;

			}

			return;
		}

		// We are not at the end, time to explore the relevent neighbors.
		if (currOrient != Orientation.Down) {
			FileParser.Pos up = currPos.up();
			exploreNeighbor(sc, currPos, currPath, currOrient, currCost, up, Orientation.Up);
		}
		if (currOrient != Orientation.Up) {
			FileParser.Pos down = currPos.down();
			exploreNeighbor(sc, currPos, currPath, currOrient, currCost, down, Orientation.Down);
		}
		if (currOrient != Orientation.Right) {
			FileParser.Pos left = currPos.left();
			exploreNeighbor(sc, currPos, currPath, currOrient, currCost, left, Orientation.Left);
		}
		if (currOrient != Orientation.Left) {
			FileParser.Pos right = currPos.right();
			exploreNeighbor(sc, currPos, currPath, currOrient, currCost, right, Orientation.Right);
		}
	}


	/*
	*/
	public static void queueEvaluation(ScanContext sc, FileParser.Pos currPos, Path currPath, Orientation currOrient, long currCost) throws Exception {
		long q = sc.num_queued.incrementAndGet();
		boolean reportNow = (q % REPORT_INTERVAL) == 0;
		//boolean reportNow = false;
		if (reportNow) {
			System.out.format("[%d] queue: pos=%s, path=%s, orient=%s, cost=%d, q=%d, p=%d\n",
				 Thread.currentThread().threadId(), currPos, currPath, currOrient, currCost,
					sc.num_queued.get(), sc.num_processed.get());
		}


		// DEBUG 
		//if (q >= 100) 	
		//	throw new Exception("bruh");	

		executor.submit(new Runnable() {
				    public void run() {
					long processed = sc.num_processed.incrementAndGet();
					if (reportNow) {
						System.out.format("[%d] dispatch: starting %s/%s (%d), q=%d, p=%d, bestscore=%d\n",
						 Thread.currentThread().threadId(),
						 currPos, currOrient, currCost,
						 sc.num_queued.get(), sc.num_processed.get(), sc.best_score.get());
					}

					try {
						evaluate(sc, currPos, currPath, currOrient, currCost);
					} catch (Exception e) {
						System.out.format("[%d] dispatch: exception in task: %s\n",
							 Thread.currentThread().threadId(), e);
						e.printStackTrace();
					}

					long remaining = sc.num_queued.get() - sc.num_processed.get();
					if (reportNow) {
				/*		System.out.format("[%d] dispatch: ending %s/%s, q=%d, p=%d, remaining=%d, score=%d\n",
							 Thread.currentThread().threadId(),
							currPos, currOrient,
							 sc.num_queued.get(), sc.num_processed.get(),
							 remaining, sc.best_score.get()); */
					}

					if (remaining == 0) {
						System.out.format("[%d] dispatch: bounded on 0, q=%d, p=%d, bestscore=%d, bestpathset=%d, %s\n",
							Thread.currentThread().threadId(),
							 sc.num_queued.get(), sc.num_processed.get(),
							 sc.best_score.get(), sc.bestPathSet.size(), sc.bestPathSet);
						//executor.shutdown();
					} 
				}});
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                FileParser.MatrixParser fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();
                
                int width = fp.getWidth();
                int height = fp.getHeight();
		ScanContext sc = new ScanContext(fp);
                for (int h = 0 ; h < height ; h++) {
                        for (int w = 0 ; w < width ; w++) {
                                FileParser.Pos pos = new FileParser.Pos(w, h);
                               	char c = fp.getCell(pos);
				if (c == 'S') {
					sc.startPos = pos;
				} else if (c == 'E') {
					sc.endPos = pos;
				}
                        }
                }
                System.out.format("Analysis start: sc=%s, part1=%b\n", sc, part1);

		executor = Executors.newFixedThreadPool(numThreads);
		queueEvaluation(sc, sc.startPos, new Path(), Orientation.Right, 0);

		// WAIT for all threads to end
		try {
			System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
				Thread.currentThread().threadId());
			//yyexecutor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		System.out.format("[%s] dispatch: All tasks have finished\n", Thread.currentThread().threadId()); 
	}
}
