
import java.io.*;
import java.util.*;

public class Day02 {

    private static int DAY_NUM = 2;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.SequenceListParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    long sumBadIds = 0;
	    Set<Long> badIdSet = new HashSet<Long>();
	    
	    boolean part1 = false;

		while (fp.hasNextId()) {
			long currId = fp.getNextId();
			String curridstr = Long.toString(currId);
			//System.out.format("processing id: %d\n", currId);
			
			if (part1) {
				// we only need even-length strings.
				if ((curridstr.length() % 2) == 1)
					continue;
					
				var firstHalf = curridstr.substring(0, curridstr.length() / 2);
				var secondHalf = curridstr.substring(curridstr.length() / 2);
				if (firstHalf.equals(secondHalf)) {
					System.out.format(" ** found candidate id: %d\n", currId);
					sumBadIds += currId;
				}
			} else {
				
				int totalLen = curridstr.length();
				for (int segmentLen = totalLen - 1 ; segmentLen > 0 ; segmentLen--) {
				
					//System.out.format(" processing: id=%d, segmentlen=%d, totalLen=%d, candidate=%d\n", 
					//	currId, segmentLen, totalLen, (totalLen % segmentLen));
						
					if ((totalLen % segmentLen) != 0)
						continue;
						
					boolean stillLooksCool = true;
					int numSegments = totalLen / segmentLen;
					var firstPart = curridstr.substring(0, segmentLen);
					//System.out.format("  filter 1: id=%d, segmentlen=%d, totalLen=%d, numseg=%d, first=%s\n", 
					//	currId, segmentLen, totalLen, numSegments, firstPart);
						
					for (int partNo = 1 ; stillLooksCool && (partNo < numSegments) ; partNo++) {
						int nextPartPos = (partNo * segmentLen);
						var nextPart = curridstr.substring(nextPartPos, nextPartPos + segmentLen);
						if (!firstPart.equals(nextPart)) {
							stillLooksCool = false;
							//System.out.format("  filter 2 bad: id=%d, segmentlen=%d, totalLen=%d, numseg=%d, first=%s, next=%s\n", 
							//	currId, segmentLen, totalLen, numSegments, firstPart, nextPart);
							break;
						}
					}
					
					if (stillLooksCool && badIdSet.add(currId)) {
						System.out.format(" ** found candidate id: %d\n", currId);
						sumBadIds += currId;
					}
				}				
			}
		}
		
		System.out.format("Process complete:\n sumBadIds=%d\n", sumBadIds);		
	}
}
