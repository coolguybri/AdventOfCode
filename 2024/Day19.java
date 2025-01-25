
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class Day19 {

	private static int DAY_NUM = 19;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;

	private static int numThreads = 1;
	//private static long REPORT_INTERVAL = 1000000;
	private static long REPORT_INTERVAL = 1;


        public static class ScanContext {
                public AtomicLong               num_queued;
                public AtomicLong               num_processed;
                public AtomicBoolean           	solved;
		public ExecutorService 		executor;

		public final String			design;
		public final HashSet<String> 		towels;
                public ConcurrentHashMap<String, Long>  solvedSet;
                public ConcurrentHashMap<List<String>, Long>  fullSolvedSet;

                public ScanContext(List<String> symbols, String d) {
                        num_queued = new AtomicLong();
                        num_processed = new AtomicLong();
			solved = new AtomicBoolean(false);
			executor = Executors.newFixedThreadPool(numThreads);
                        solvedSet = new ConcurrentHashMap<String, Long>();
                        fullSolvedSet = new ConcurrentHashMap<List<String>, Long>();
		
			// Index all the symbols for easy lookup.a
			design = d;
			towels = new HashSet<String>();
			for (String symbol : symbols) {
				towels.add(symbol);
			}
                }

                @Override
                public String toString() {
                        return String.format("[sc towels=%s]", towels);
                }


        	public void evaluate(String currDesign, ArrayList<String> currPath, String currSolved) throws Exception {
			Long currUsedCount;
			if ((currUsedCount = solvedSet.putIfAbsent(currSolved, 1L)) != null) {
				System.out.format("[%s] eval: short-circuiting currPath=%s because already in solved set (%d)\n",
					Thread.currentThread().threadId(), currSolved, currUsedCount);
				while (true) {
					long newUsedCount = currUsedCount + 1;
					if (solvedSet.replace(currSolved, currUsedCount, newUsedCount)) {
						System.out.format("[%s] eval: currPath=%s updated used count from %d to %d\n",
							Thread.currentThread().threadId(), currSolved, currUsedCount, newUsedCount);
						break;
					}

					System.out.format("[%s] eval: currPath=%s FAILED to update used count from %d to %d\n",
						Thread.currentThread().threadId(), currSolved, currUsedCount, newUsedCount);
					currUsedCount = solvedSet.get(currSolved);
				}
				return;
			} else {
				System.out.format("[%s] eval: first-time for currPath=%s, setting use-count to 1\n",
					Thread.currentThread().threadId(), currSolved);
			}

			int len = currDesign.length();
			for (int i = len ; i > 0 ; i--) {
				String searchterm = currDesign.substring(0, i);
				if (!towels.contains(searchterm)) {
					//System.out.format("[%s] eval: design=%s, towel=%s is not available; skipping\n",
					// Thread.currentThread().threadId(), currDesign, searchterm);
					continue;
				}

				ArrayList<String> newPath = new ArrayList<String>(currPath);
				newPath.add(searchterm);
				String newSolved = currSolved + searchterm;

				//System.out.format("[%s] eval: design=%s, towel=%s will work!\n",
				//	 Thread.currentThread().threadId(), currDesign, searchterm);

				int consumed = searchterm.length();
				int remaining = currDesign.length() - consumed;
				if (remaining == 0) {
					System.out.format("[%s] eval: HIT SOLUTION for %s: %s\n",
						 Thread.currentThread().threadId(), design, newPath);
					solved.set(true);

					Long currSolveCount;
					if ((currSolveCount = fullSolvedSet.putIfAbsent(newPath, 1L)) != null) {
						System.out.format("[%s] eval: solution %s already is in set (%d)\n",
							Thread.currentThread().threadId(), newPath, currSolveCount);
						while (true) {
							long newSolveCount = currSolveCount + 1;
							if (fullSolvedSet.replace(newPath, currSolveCount, newSolveCount)) {
								System.out.format("[%s] eval: design=%s updated solution from %d to %d\n",
									Thread.currentThread().threadId(), currDesign, currSolveCount, newSolveCount);
								break;
							}

							System.out.format("[%s] eval: design=%s FAILED to update used solution from %d to %d\n",
								Thread.currentThread().threadId(), currDesign, currSolveCount, newSolveCount);
							currUsedCount = fullSolvedSet.get(newPath);
						}
					}
				} else {
					String newDesign = currDesign.substring(consumed);
					queueEvaluation(newDesign, newPath, newSolved);
				}
			}
		}


		public void queueEvaluation(String currDesign, ArrayList<String> currPath, String currSolved) throws Exception {

			//	if (solved.get()) {
			//		System.out.format("[%s] dispatch-queue: skipping due to already solved\n",
			//			 Thread.currentThread().threadId());
			//		return;
			//	}

			long q = num_queued.incrementAndGet();
			boolean reportNow = (q % REPORT_INTERVAL) == 0;
			if (reportNow) {
				System.out.format("[%s] dispatch-queue: path=%s, rest=%s, q=%d, p=%d\n",
					 Thread.currentThread().threadId(), currPath, currDesign, num_queued.get(), num_processed.get());
			}

			executor.submit(new Runnable() {
					    public void run() {
						long processed = num_processed.incrementAndGet();
						if (reportNow) {
							System.out.format("[%d] dispatch-begin: path=%s, rest=%s, starting q=%d, p=%d\n",
							 Thread.currentThread().threadId(), currPath, currDesign,
							 num_queued.get(), num_processed.get());
						}

						try {
							evaluate(currDesign, currPath, currSolved);
						} catch (Exception e) {
							System.out.format("[%d] dispatch-error: exception in task: %s\n",
								 Thread.currentThread().threadId(), e);
							e.printStackTrace();
						}

						long remaining = num_queued.get() - num_processed.get();
						if (reportNow) {
						      //System.out.format("[%d] dispatch-end: path=%s, rest=%s, q=%d, p=%d, remaining=%d\n",
							//	 Thread.currentThread().threadId(), currPath, currDesign,
							//	num_queued.get(), num_processed.get(), remaining);
						}

						if (remaining == 0) {
							System.out.format("[%d] YOYOYO dispatch-final: bounced on 0, q=%d, p=%d\n",
								Thread.currentThread().threadId(), num_queued.get(), num_processed.get());
							executor.shutdown();
						}
					}});
		}
	}

	/*
	*/
	public static void main(String[] args) throws Exception { 

 		// Import the data; will throw if no file or data makes no sense.
                FileParser.SymbolParser fp = new FileParser.SymbolParser(DAY_NUM, SAMPLE_INPUT);
                fp.parse();

		long numSolved = 0;
		for (String design : fp.compositions) {
			System.out.format("[%d] process: START START: %s\n",
				Thread.currentThread().threadId(), design);

			ScanContext sc = new ScanContext(fp.symbols, design);
			sc.queueEvaluation(design, new ArrayList<String>(), "");

			// WAIT for all threads to end 
			try {
				System.out.format("[%d] process: waiting for unfinished tasks\n",
					Thread.currentThread().threadId());
				sc.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}

			if (sc.solved.get()) {

				Map<List<String>, Long> solutions = sc.fullSolvedSet;

				long totPossibilities = 0;
				for (List<String> solution : solutions.keySet()) {

					long multiplier = solutions.get(solution);
					String builder = "";
					for (String part : solution) {
						builder += part;
						Long currUsedCount = sc.solvedSet.get(builder);
						if (currUsedCount != null) {
							multiplier *= currUsedCount;
						}
						System.out.format("[%d] score: design=%s, segment=%s, count=%d, multiplier=%d\n",
							Thread.currentThread().threadId(), design, builder, currUsedCount, multiplier);
					}

					totPossibilities += multiplier;
					System.out.format("[%d] score: design=%s, solution=%s, count=%d, total=%d\n",
						Thread.currentThread().threadId(), design, solution, multiplier, totPossibilities);
				}

				numSolved += totPossibilities;
				System.out.format("[%d] process: COMPLETE design %s, solved=%d, tot=%d\n",
					Thread.currentThread().threadId(), design, totPossibilities, numSolved);
			}
		} 
		
		System.out.format("[%d] process: found %d solvable paths\n",
			Thread.currentThread().threadId(), numSolved);
	}
}
