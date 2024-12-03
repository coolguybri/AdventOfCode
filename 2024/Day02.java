
import java.io.*;
import java.util.*;

public class Day02 {

	private static int DAY_NUM = 2;
	private static boolean SAMPLE_INPUT = false;

	/*
	*/
	public static boolean isSuperSafe(List<Integer> report) {
		System.out.format(" super: starting with list=%s\n", report.toString());

		boolean safe = isSafe(report);
		if (safe) {
			System.out.format(" super: safe for original report %s\n", report.toString());
			return true;
		}

		for (int i = 0 ; i < report.size() ; i++) {
			List<Integer> newlist = new ArrayList<Integer>(report);
			newlist.remove(i);
			System.out.format(" super: popping %d and falling back to list=%s\n", i, newlist.toString());
			
			safe = isSafe(newlist);
			if (safe) {
				System.out.format(" super: safe for modified report pos=%d, list=%s\n", i, newlist.toString());
				return true;
			}
		}

		System.out.format(" super: all permutations were unsafe\n");
		return false;
	}

	
	/*
	*/
	public static boolean isSafe(List<Integer> report) {
		int reportLen = report.size();
		if (reportLen < 2) {
			// A report with one number will always be safe.
			return true;
		}

		boolean direction = true;
		if (report.get(0) < report.get(1)) {
			direction = true;
		} else if (report.get(0) > report.get(1)) {
			direction = false;
		} else {
			// two adjacent numbers cannot be equal to each other and still be safe.
			System.out.format("  not safe due to intro-adjacent\n");
			return false;
		}

		for (int i = 1 ; i < reportLen ; i++) {
			int currItem = report.get(i);
			int prevItem = report.get(i-1);
			int delta = currItem - prevItem;

			if ((Math.abs(delta) < 1) || (Math.abs(delta) > 3)) {
				System.out.format("  not safe due to delta=%d at pos %d\n", delta, i);
				return false;
			}

			if ((direction && (delta < 0)) || (!direction && (delta > 0))) {
				System.out.format("  not safe due wrong direction with delta=%d dir=%b at pos %d\n",
					 delta, direction, i);
				return false;
			}
		}

		return true;
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.ListIntegerListParser fp = new FileParser.ListIntegerListParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		List<List<Integer>> reports = fp.getList();
		int totReportsLen = reports.size();

		boolean part1 = false;
		boolean part2 = !part1;
		int safeCount = 0;
		int reportCount = 0;
		for (Iterator<List<Integer>> iter = reports.iterator() ; iter.hasNext() ; reportCount++) {
			List<Integer> report = iter.next();
			if (part1 && isSafe(report)) {
				safeCount++;
				System.out.format("part 1 report %d/%d is safe\n", reportCount, totReportsLen);
			} else if (part2 && isSuperSafe(report)) {
				safeCount++;
				System.out.format("part 2 report %d/%d is safe\n", reportCount, totReportsLen);
			} else {
				System.out.format("report %d/%d is NOTSAFE\n", reportCount, totReportsLen);
			}
		}

		System.out.format("Analysis complete:\n reports=%d\n total safe=%d, part=%b\n",
			 reportCount, safeCount, part1);
	}
}
