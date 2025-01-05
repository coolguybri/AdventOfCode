
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class Day18 {

	private static int DAY_NUM = 18;
	private static boolean SAMPLE_INPUT =false ;
	private static boolean part1 = false;
	//private static boolean part1 = true;
	private static boolean part2 = !part1;

	private static int numThreads = 1;
	private static long REPORT_INTERVAL = 100000;

	private static int map_width = SAMPLE_INPUT ? 7 : 71;
	private static int map_height = SAMPLE_INPUT ? 7 : 71;

	/*
	*/
	public static class Map {
		protected final int width;
		protected final int height;
		protected final List<char[]>    cells;

		public Map(int w, int h, List<FileParser.Pos> corrupted, int numItems) {
			width = w;
			height = h;
			cells = new ArrayList<char[]>();
			for (int y = 0 ; y < height ; y++) {
				var newseq = new char[width];
				for (int x = 0 ; x < width; x++) {
					newseq[x] = '.';
				}
				cells.add(newseq);
			}

			int count = 0;
			for (FileParser.Pos pos : corrupted) {
				if (count++ >= numItems)
					break;

				setCell(pos, '#');
			}
		}

		public char getCell(FileParser.Pos p) { return cells.get(p.y)[p.x]; }
		public char getCell(int x, int y) { return cells.get(y)[x]; }
		public void setCell(FileParser.Pos p, char c) { cells.get(p.y)[p.x] = c; }
		public void setCell(int x, int y, char c) { cells.get(y)[x] = c; }
		public int getHeight() { return cells.size(); }
		public int getWidth() { return cells.get(0).length; }

		public boolean inBounds(FileParser.Pos currPos) {
			if (currPos.x < 0 || currPos.x >= width)
				return false;
			if (currPos.y < 0 || currPos.y >= height)
				return false;
			return true;
		}
	}


	/*
	*/
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
                public Map			map;
                public FileParser.Pos           endPos;
	
		public ExecutorService 		executor;

                public AtomicLong               best_score;
                public AtomicLong               num_queued;
                public AtomicLong               num_processed;
                public ConcurrentHashMap<FileParser.Pos, Long>  processedPos;
                public ConcurrentHashMap<FileParser.Pos, Long>  bestPathSet;

                public ScanContext(Map m) {
                        map = m;
			endPos = new FileParser.Pos(m.getWidth() - 1, m.getHeight() - 1);

                        best_score = new AtomicLong();
                        num_queued = new AtomicLong();
                        num_processed = new AtomicLong();
                        processedPos = new ConcurrentHashMap<FileParser.Pos, Long>();
                        bestPathSet = new ConcurrentHashMap<FileParser.Pos, Long>();
		
			executor = Executors.newFixedThreadPool(numThreads);
                }

                @Override
                public String toString() {
                        return String.format("[sc matrix=%dx%d, end=%s]", map.getWidth(), map.getHeight(), endPos);
                }


        	public boolean probeNeighbor(FileParser.Pos nextPos) throws Exception {
			if (!map.inBounds(nextPos)) {
			      //System.out.format("[%d] probeNeighbor: hit OOB at %s\n",
		//		       Thread.currentThread().threadId(), nextPos);
				return false;
			}

			char c = map.getCell(nextPos);
			if (c == '#') {
			      //System.out.format("[%d] probeNeighbor: hit Wall at %s\n",
		//		       Thread.currentThread().threadId(), nextPos);
				return false;
			}

			if (nextPos.equals(endPos)) {
			      //System.out.format("[%d] probeNeighbor: found exit in horizon at %s\n",
			//	       Thread.currentThread().threadId(), nextPos);
				return true;
			}

			if (c == '.') {
			      //System.out.format("[%d] probeNeighbor: found movable space at %s\n",
			//	       Thread.currentThread().threadId(), nextPos);
				return true;
			}

			throw new Exception(String.format("YO: bad char %c in map", c));
		}

	
		public boolean exploreNeighbor(FileParser.Pos currPos, Path currPath, long currCost, FileParser.Pos nextPos) throws Exception {
			long nextCost = currCost + 1;

			// if we already hit this neighbor in this path, we have looped, and should end this path.
			if (currPath.path.contains(nextPos)) {
			      //System.out.format("[%d] exploreNeighbor: skipping %s already-visited\n",
				// Thread.currentThread().threadId(), nextPos);
				return false;
			}

			// make sure the neighbor is an actual open space.
			if (!probeNeighbor(nextPos)) {
				return false;
			}

			while (true) {
				boolean skip = false;
				long costDiff = 0;
				Long prevCost = processedPos.get(nextPos);
				if (prevCost != null) {
					costDiff = nextCost - prevCost;
					if (costDiff >= 0) {
						skip = true;
					}
				} else {
					costDiff = -nextCost;
				}

				if (skip) {
				      //System.out.format("[%d] exploreNeighbor: skipping %s(costdiff=%d) due to better path there already (best=%d, curr=%d)\n",
				//		Thread.currentThread().threadId(), nextPos, costDiff, prevCost, nextCost);
					return false;
				}

				if (prevCost == null) {
					if (processedPos.putIfAbsent(nextPos, nextCost) == null) {
					//	System.out.format("[%d] exploreNeighbor: setting best path to %s=%d\n",
					///		Thread.currentThread().threadId(), nextPos, nextCost);
						break;
					}
				} else {
					if ((prevCost == nextCost) || processedPos.replace(nextPos, prevCost, nextCost)) {
					//	System.out.format("[%d] exploreNeighbor: replacing best path to %s=%d (prev=%d)\n",
					 //             Thread.currentThread().threadId(), nextPos, nextCost, prevCost);
						break;
					}
                        	}
			}

		       	// QUEUE this next item for processing.
			Path nextPath = currPath.add(currPos);
			queueEvaluation(nextPos, nextPath, nextCost);
			return true;
		}


        	public void evaluate(FileParser.Pos currPos, Path currPath, long currCost) throws Exception {

			// Evaluate current node.
			char c = map.getCell(currPos);
			if (currPos.equals(endPos)) {
				// We found the end!
				Path p = currPath.add(currPos);
				System.out.format("[%d] evaluate: found END score=%d, path=%s\n",
					 Thread.currentThread().threadId(), currCost, p);

				// Update the best score.
				while (true) {
					long best = best_score.get();
					if ((best != 0) && (currCost >= best)) {
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
						bestPathSet.clear();
						System.out.format("[%d] evaluate: SCORE %d is updated\n",
							 Thread.currentThread().threadId(), currCost);
					}

					for (FileParser.Pos bpos : p.path) {
						bestPathSet.put(bpos, 0L);
					}

					System.out.format("[%d] evaluate: SCORE %d is NOW the best (prev=%d), pathset=%d %s\n",
						 Thread.currentThread().threadId(), currCost, best, bestPathSet.size(), bestPathSet);
					break;

				}

				return;
			}

			// We are not at the end, time to explore the relevent neighbors.
			FileParser.Pos up = currPos.up();
			exploreNeighbor(currPos, currPath, currCost, up);
			FileParser.Pos down = currPos.down();
			exploreNeighbor(currPos, currPath, currCost, down);
			FileParser.Pos left = currPos.left();
			exploreNeighbor(currPos, currPath, currCost, left);
			FileParser.Pos right = currPos.right();
			exploreNeighbor(currPos, currPath, currCost, right);
		}


		/*
		*/
		public void queueEvaluation(FileParser.Pos currPos, Path currPath, long currCost) throws Exception {
			long q = num_queued.incrementAndGet();
			boolean reportNow = (q % REPORT_INTERVAL) == 0;
			if (reportNow) {
				System.out.format("[%s] queue: pos=%s, path=%s, cost=%d, q=%d, p=%d\n",
					 Thread.currentThread().threadId(), currPos, currPath, currCost,
				num_queued.get(), num_processed.get());
			}

			executor.submit(new Runnable() {
					    public void run() {
						long processed = num_processed.incrementAndGet();
						if (reportNow) {
							System.out.format("[%d] dispatch: starting %s path=%s, (%d), q=%d, p=%d, bestscore=%d\n",
							 Thread.currentThread().threadId(),
							 currPos, currPath, currCost,
							 num_queued.get(), num_processed.get(), best_score.get());
						}

						try {
							evaluate(currPos, currPath, currCost);
						} catch (Exception e) {
							System.out.format("[%d] dispatch: exception in task: %s\n",
								 Thread.currentThread().threadId(), e);
							e.printStackTrace();
						}

						long remaining = num_queued.get() - num_processed.get();
						if (reportNow) {
						      System.out.format("[%d] dispatch: ending %s, path=%s, q=%d, p=%d, remaining=%d, score=%d\n",
								 Thread.currentThread().threadId(),
								currPos, currPath,
								 num_queued.get(), num_processed.get(),
								 remaining, best_score.get());
						}

						if (remaining == 0) {
							System.out.format("[%d] YOYOYO dispatch: bounced on 0, q=%d, p=%d, bestscore=%d, bestpathset=%d, %s\n",
								Thread.currentThread().threadId(),
								 num_queued.get(), num_processed.get(),
								 best_score.get(), bestPathSet.size(), bestPathSet);
							executor.shutdown();
						}
					}});
		}
	}

	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                FileParser.PosParser fp = new FileParser.PosParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();
		List<FileParser.Pos> corrupted = fp.getList();

		if (part1) {
			int maxCount = SAMPLE_INPUT ? 12 : 1024;
			Map m = new Map(map_width, map_height, corrupted, maxCount);
			ScanContext sc = new ScanContext(m);
			System.out.format("[%s] imported %d corrupted points, sc=%s\n",
				 Thread.currentThread().threadId(), corrupted.size(), sc);

			sc.queueEvaluation(new FileParser.Pos(0,0), new Path(), 0);

			/* WAIT for all threads to end */
			try {
				System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
					Thread.currentThread().threadId());
				sc.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			} 
		
			long best = sc.best_score.get();
			System.out.format("[%s] bestscore=%d\n", Thread.currentThread().threadId(), best);
			
		} else {
			for (int run = 0 ; true ; run++) {
				int normCount = SAMPLE_INPUT ? 12 : 1024;
				int maxCount = normCount + run;
				Map m = new Map(map_width, map_height, corrupted, maxCount);
				ScanContext sc = new ScanContext(m);
				System.out.format("[%s] imported %d corrupted points, sc=%s\n",
					 Thread.currentThread().threadId(), corrupted.size(), sc);

				sc.queueEvaluation(new FileParser.Pos(0,0), new Path(), 0);

				/* WAIT for all threads to end */
				try {
					System.out.format("[%d] dispatch: waiting for unfinished tasks\n",
						Thread.currentThread().threadId());
					sc.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				    e.printStackTrace();
				} 

				long best = sc.best_score.get();
				if (best == 0) {
					FileParser.Pos lastStraw = corrupted.get((normCount + run)-1);
					System.out.format("[%d] dispatch: loop %d (maxCount=%d) could not find solution, lastStraw=%s\n",
						Thread.currentThread().threadId(), run, maxCount, lastStraw);

					break;
				}
			}
		}

                //System.out.format("[%s] dispatch: All tasks have finished\n", Thread.currentThread().threadId());
	}
}
