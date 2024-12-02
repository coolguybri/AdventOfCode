
import java.io.*;
import java.util.*;

public class Day01 {

    private static int DAY_NUM = 1;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.DoubleIntegerListParser fp = new FileParser.DoubleIntegerListParser(DAY_NUM, SAMPLE_INPUT);
       	fp.parse();

		// Sort list 1.
		List<Integer> list1 = fp.getList1();
		System.out.format("imported list1: %s\n", list1);
		Collections.sort(list1);
		System.out.format("sorted list1: %s\n", list1);

		// Sort list 2.
		List<Integer> list2 = fp.getList2();
		System.out.format("imported list2: %s\n", list2);
		Collections.sort(list2);
		System.out.format("sorted list2: %s\n", list2);

		// Note the size of the list (both lists are the same).
		int totLineLen = list2.size();

		boolean part1 = false;
		if (part1) {
			int deltaSum = 0;
			for (int lineNum = 0 ; lineNum < totLineLen; lineNum++) {
				int item1 = list1.get(lineNum);
				int item2 = list2.get(lineNum);
				int delta = Math.abs(item1 - item2);

				deltaSum += delta;
				System.out.format("line %d/%d ints=%d,%d, delta=%d, deltaSum=%d\n",
					 lineNum, totLineLen, item1, item2, delta, deltaSum);
			}
			System.out.format("Part 1 Analysis complete:\n lines=%d\n result=%d\n",
				 totLineLen, deltaSum);
		} else {
			int sum = 0;
			for (int lineNum = 0 ; lineNum < totLineLen; lineNum++) {
				int item1 = list1.get(lineNum);

				// Find the number of instances in list two.
				int numOccurrences = 0;
				int line2Num = 0;
				for ( ; line2Num < totLineLen; line2Num++) {
					int item2 = list2.get(line2Num);
					if (item2 >= item1)
						break;
				}
				for ( ; line2Num < totLineLen ; line2Num++, numOccurrences++) {
					int item2 = list2.get(line2Num);
					if (item2 > item1)
						break;
				}
				
				int similarity = item1 * numOccurrences;

				sum += similarity;
				System.out.format("line %d/%d int=%d occurrence=%d, similarity=%d, sum=%d\n",
					 lineNum, totLineLen, item1, numOccurrences, similarity, sum);
			}
			System.out.format("Part 2 Analysis complete:\n lines=%d\n result=%d\n",
				 totLineLen, sum);
		}
	}
}
