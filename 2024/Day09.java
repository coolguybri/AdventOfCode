
import java.io.*;
import java.util.*;

public class Day09 {

	private static int DAY_NUM = 9;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;



	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.LineParser fp = new FileParser.LineParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();
		System.out.format("Analysis start: part1=%b\n", part1);

		String diskmap = fp.getList().get(0);
		System.out.format("Analysis: diskmap=%s\n", diskmap);

		int currId = 0;
		boolean nextIsBlock = true;
		List<Integer> blockmap = new ArrayList<Integer>();
		for (char currChar : diskmap.toCharArray()) {
			//System.out.format("block: processing %c (%s)\n", currChar, nextIsBlock ? "block" : "space");
			if (!Character.isDigit(currChar)) {
				throw new Exception("lame input");
			}

			int val = Character.getNumericValue(currChar);
			//System.out.format("block: converting %c to %d\n", currChar, val);
			int toWrite = nextIsBlock ? currId++ : -1;
			for (int i = 0 ; i < val ; i++) {
				blockmap.add(toWrite);
			}

			nextIsBlock = !nextIsBlock;
      		}
		System.out.format("Analysis: blockmap=(%d) %s\n", blockmap.size(), blockmap);

		if (!part1) {
			// Grab the last chunk and move it forward.
			for (int currPos = blockmap.size() - 1; currPos > 0 ; currPos--) {
				int currVal = blockmap.get(currPos);
				while (currVal == -1) {
					//System.out.format("Compact: skipping blank in slot %d\n", currPos);
					currVal = blockmap.get(--currPos);
				}
				int endPos;
				for (endPos = currPos - 1 ; endPos > 0 ; endPos--) {
					if (blockmap.get(endPos) != currVal)
						break;
				}
				int currLen = currPos - endPos;
				//System.out.format("Compact: attempting to move file-id %d from pos=%d(%d), len=%d\n",
				//	 currVal, currPos, endPos, currLen);
				currPos  = endPos + 1;

				boolean foundMove = false;
				int foundLen = 0;
				for (int startBlank = 0 ; startBlank < currPos ; startBlank++) {
					if (blockmap.get(startBlank) != -1) {
						continue;
					}

					int endBlank;
					for (endBlank = startBlank + 1 ; endBlank < currPos ; endBlank++) {
						if (blockmap.get(endBlank) != -1) 
							break;
					}
					int blankLen = endBlank - startBlank;
					if (blankLen < currLen) {
						//System.out.format("Compact: found blank but not big enough pos=%d, len=%d\n",
					//		 startBlank, blankLen);
						continue;
					}

					// Copy it over.
					for (int i = 0 ; i < currLen ; i++) {
						blockmap.set(startBlank + i, currVal);
						blockmap.set(currPos + i, -1);
					}
					foundMove = true;
					//System.out.format("Compact: moved %d from %d to %d, blockmap=(%d) %s\n",
					//	 currVal, currPos, startBlank, blockmap.size(), blockmap);
					break;
				}

				// if we got here, we could ot find a blnak spot - break out!
				if (!foundMove) {
					System.out.format("Compact: could not move %d, no slots big enough\n", currVal);
					continue;
				}
			}
		} else {
		}
		
		long checksum = 0;
		for (int block = 0 ; block < blockmap.size() ; block++) {
			int currVal = blockmap.get(block);
			if (currVal == -1)
				continue;

			checksum += (block * currVal);
		}
		System.out.format("Checksum: %d\n", checksum);
	}
}
