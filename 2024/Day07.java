
import java.io.*;
import java.util.*;

public class Day07 {

	private static int DAY_NUM = 7;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;


	public static class DiscoveryContext {
		public final long expectedResult;
		public List<String> successes;
		public long successAccumulator;

		public DiscoveryContext(long er) {
			expectedResult = er;
			successes = new ArrayList<String>();
		}
	}

	public static void discover(List<Long> l, DiscoveryContext dc) {
		List<Long> currList = new ArrayList<>(l);
		long accumulator = currList.remove(0);
		discoverInternal(accumulator, String.format("%d", accumulator), currList, dc, 0);
	}

	public static void discoverInternal(long accumulator, String equation, List<Long> l, DiscoveryContext dc, int recursionLevel) {
		recursionLevel++;

		// recursion termination.
		if (l.size() == 0) {
			if (accumulator	== dc.expectedResult) {
				System.out.format("discovery:FOUND ONE:%s, = %d\n", equation, accumulator);
				dc.successes.add(equation);
			} else {
				//System.out.format("discovery(%d): search ends with a dud: accum=%d, expect=%d, %s\n",
			//		 recursionLevel, accumulator, dc.expectedResult, equation);
			}
			return;
		}
		
		List<Long> currList = new ArrayList<>(l);
		long nextInt = currList.remove(0);

		// Multiply
		discoverInternal(accumulator * nextInt, String.format("%s X %d", equation, nextInt), currList, dc, recursionLevel);

		// Add
		discoverInternal(accumulator + nextInt, String.format("%s + %d", equation, nextInt), currList, dc, recursionLevel);

		if (part2) {
			// OR
			long orResult = Long.parseLong(String.format("%d%d", accumulator, nextInt));
			discoverInternal(orResult, String.format("%s || %d", equation, nextInt), currList, dc, recursionLevel);
		}
	}

	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.ListLongListParser fp = new FileParser.ListLongListParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		List<List<Long>> list = fp.getList();
		System.out.format("Starting with:\n%s\n", list);

		long sum = 0;
		List<String> matches = new ArrayList<String>();
		for (Iterator<List<Long>> iter = list.iterator() ; iter.hasNext() ; ) {
			List<Long> record = iter.next();
			long expectedResult = record.remove(0);
			DiscoveryContext dc = new DiscoveryContext(expectedResult);
			discover(record, dc);


			if (dc.successes.size() > 0) {
				matches.addAll(dc.successes);
				sum += expectedResult;
				System.out.format("record produced %d matches, adding %d, now %d\n", dc.successes.size(), expectedResult, sum);
			}
		}

		System.out.format("Finsihed with %d matches, sum=%d:\n%s\n", matches.size(), sum, matches);
	}
}
