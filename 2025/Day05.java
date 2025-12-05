
import java.io.*;
import java.util.*;

public class Day05 {

    private static int DAY_NUM = 5;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.SequenceListPlusItemsParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    boolean part1 = false;
	    if (part1) {
			int numFresh = 0;
			
			var items = fp.getItems();
			var ranges = fp.getRanges();
			for (var item : items) {
				for (var range : ranges) {
					if (range.isIn(item)) {
						numFresh++;
						System.out.format(" found fresh: %d, found in range %s, totFresh=%d\n", item, range, numFresh);
						break;
					}
				}
			}
			
			System.out.format("part1 complete: totFresh=%d\n", numFresh);
			
		} else {
			// part 2
			int numFresh = 0;

			var currRangeList = fp.getRanges();
			int repCount = (currRangeList.size() * 2);
			System.out.format("starting with range %s, repcount=%d\n", currRangeList, repCount);
			
			// collapse all the ranges.: loop through each row, comparing it to every other row, and merging if possible.
			for (int rep = 0 ; rep < repCount ; rep++) {
				var currRange = currRangeList.get(0);
				var outputList = new ArrayList<FileParser.Sequence>();
				
				for (int r2 = 1 ; r2 < currRangeList.size() ; r2++) {
					var compRange = currRangeList.get(r2);
					var mergedRange = currRange.merge(compRange);
					if (mergedRange != null) {
						System.out.format("  compacted range %s and %s to %s\n", currRange, compRange, mergedRange);
						currRange = mergedRange;
					} else {
						outputList.add(compRange);
					}
				}
				
				outputList.add(currRange);
				currRangeList = outputList;
			}
			
			System.out.format("compacted range list=%s\n", currRangeList);
			
			long count = 0;
			for (var seq : currRangeList) {
				count += seq.getCount();
				System.out.format("  calc seq %s, freshCount=%d\n", seq, count	);
			}
			
			
			System.out.format("part2 complete: freshCount=%d\n", count);
		}


		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
}
