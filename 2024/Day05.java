
import java.io.*;
import java.util.*;

public class Day05 {

	private static int DAY_NUM = 5;
	private static boolean SAMPLE_INPUT = true;
 	private static boolean part1 = false;
	private static boolean part2 = !part1;


	/*
	*/
	protected static boolean orderIsOrdered(List<Integer> update, FileParser.PageRuleParser fp) {

		boolean success = true;
		int updateLen = update.size();
		for (int i = 0 ; i < updateLen ; i++) {
			int currPage = update.get(i);

			for (Iterator<FileParser.PageRuleParser.PageRule> ruleIter = fp.getRules().iterator() ; ruleIter.hasNext() ; ) {
				FileParser.PageRuleParser.PageRule rule = ruleIter.next();
				// System.out.format(" eval-start: currPage=%d, rule=%s\n", currPage, rule);

				if (rule.before == currPage) {
					// System.out.format(" eval-mid: currPage=%d, rule MATCH_BEFORE\n", currPage);
					for (int j = i - 1 ; j >= 0 ; j--) {
						int compPage = update.get(j);
						if (compPage == rule.after) {
							//System.out.format(" eval-end: currPage=%d, rule BREAK: %d is before\n", currPage, compPage);
							return false;
						}
					}
				} else if (rule.after == currPage) {
					// System.out.format(" eval-mid: currPage=%d, rule MATCH_AFTER\n", currPage);
					for (int j = i + 1 ; j < updateLen ; j++) {
						int compPage = update.get(j);
						if (compPage == rule.before) {
							//System.out.format(" eval-end: currPage=%d, rule BREAK: %d is after\n", currPage, compPage);
							return false;
						}
					}
				} else {
					// System.out.format(" eval-end: currPage=%d, no rule match, skipping\n", currPage);
				}
			}
		}
	
		// System.out.format(" eval-end: rule OK\n");
		return true;
	}

	/*
	*/
	protected static List<Integer> fixOrder(List<Integer> u, FileParser.PageRuleParser fp) {

		List<Integer> currUpdate = new ArrayList<Integer>(u);
		for (int attempt = 0 ;  ; attempt++) {
			int updateLen = currUpdate.size();
			boolean foundFailure = false;
			System.out.format(" fix: starting attempt=%d, update=%s\n", attempt, currUpdate);

			for (int i = 0 ; !foundFailure && (i < updateLen) ; i++) {
				int currPage = currUpdate.get(i);

				for (Iterator<FileParser.PageRuleParser.PageRule> ruleIter = fp.getRules().iterator() ;
					 !foundFailure && ruleIter.hasNext() ; ) {
					FileParser.PageRuleParser.PageRule rule = ruleIter.next();
					System.out.format(" fix: applyRule attempt=%d, currPage=%d, rule=%s\n",
						 attempt, currPage, rule);

					if (rule.before == currPage) {
						System.out.format(" fix: currPage=%d, rule MATCH_BEFORE %d\n",
							rule.before, rule.after);
						for (int j = i - 1 ; j >= 0 ; j--) {
							int compPage = currUpdate.get(j);
							if (compPage == rule.after) {
								System.out.format(" fix: attempt=%d, currPage=%d, rule BREAK: %d is before\n",
									attempt, currPage, compPage);
								currUpdate.remove(j);
								if (i == currUpdate.size()) 
									currUpdate.add(compPage);
								else
									currUpdate.add(i+1, compPage);

								foundFailure = true;
								break;
							}
						}
					} else {
						System.out.format(" fix: attempt=%d currPage=%d, no rule match, skipping\n",
							 attempt, currPage);
					}
				}
			}
		}
	}


	public static long factorial(long number) {
		long result = 1;

		for (int factor = 2; factor <= number; factor++) {
		    result *= factor;
		}

		return result;
	}

	protected static final long LOG_INTERVAL_MILLIS = 5 * 1000;
	protected static long lastLog = 0;
	protected static void logStatus(String log) {
		long now =  System.currentTimeMillis();
		if (now - lastLog > LOG_INTERVAL_MILLIS) {
			System.out.print(log);
			lastLog = now;
		}
	}

	protected static long progress_total = 0;
	protected static long progress_current = 0;

	protected static List<Integer> findOrder(List<Integer> builder, List<Integer> source, FileParser.PageRuleParser fp, int recursionLevel) {
		double progress_pcent = ((double) progress_current / (double) progress_total) * 100.0;

		logStatus(
			String.format("findOrder: build=%s, source=%s, recursion=%d, progress=%d/%d (%02f)\n",
			 builder, source, recursionLevel, progress_current, progress_total, progress_pcent));

		// recursion termination
		if (source.isEmpty()) {

			progress_current++;

			if (orderIsOrdered(builder, fp)) {
				System.out.format("REORDER: MATCH at termination\n");
				return builder;
			}
			return null;
		}

		int sourceLen = source.size();
		for (int i = 0 ; i < sourceLen ; i++) {
			List<Integer> ourBuilder = new ArrayList<Integer>(builder);
			List<Integer> ourSource = new ArrayList<Integer>(source);
			Integer currItem = ourSource.remove(i);
			ourBuilder.add(currItem);
			List<Integer> gp = findOrder(ourBuilder, ourSource, fp, recursionLevel+1);
			if (gp != null) {
				System.out.format("REORDER: MATCH: gp=%s\n", gp);
				return gp;
			}
		}

		// If we got here, no match.
		return null;
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		long start = System.currentTimeMillis();
		FileParser.PageRuleParser fp = new FileParser.PageRuleParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		// loop throught he orders.
		int sumMatched = 0;
		int sumUnMatched = 0;
		int numMatched = 0;
		int numProcessed = 0;
		for (Iterator<List<Integer>> iter = fp.getUpdates().iterator() ; iter.hasNext() ; numProcessed++) {
			List<Integer> update = iter.next();
			boolean ordered = orderIsOrdered(update, fp);
			if (ordered) {
				numMatched++;
				int halfPoint = update.size() / 2;
				int halfPointPage = update.get(halfPoint);
				sumMatched += halfPointPage;
				System.out.format("UPDATE: YES: new=%d, sumMatched=%d, part1=%b\n", halfPointPage, sumMatched, part1);
			} else {
				long searchSpace = factorial(update.size());
				long searchStart = System.currentTimeMillis();
				System.out.format("UPDATE: NO: starting search for %s, total=%d\n", update, searchSpace);
				progress_total = searchSpace;
				progress_current = 0;

				List<Integer> newUpdate = fixOrder(update, fp);
				long searchEnd = System.currentTimeMillis();


				int halfPoint = newUpdate.size() / 2;
				int halfPointPage = newUpdate.get(halfPoint);
				sumUnMatched += halfPointPage;
				double elapsed = ((double) (searchEnd - searchStart) / 1000.0);
				System.out.format("UPDATE: NO: [%02f] new=%d, sumUnMatched=%d, part1=%b\n", elapsed, halfPointPage, sumUnMatched, part1);
			}
		}

		long end = System.currentTimeMillis();
		System.out.format("Analysis complete (:\n sumMatched=%d, sumUnmatched=%d, numProcessed=%d, part1=%b\n",
			 sumMatched, sumUnMatched, numProcessed, part1);
	}
}
