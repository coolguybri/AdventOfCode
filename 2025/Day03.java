
import java.io.*;
import java.util.*;

public class Day03 {

    private static int DAY_NUM = 3;
    private static boolean SAMPLE_INPUT = false;

	/*
		This was my first hack at part 1, which solved it. Later this was replaced by the other method,
		by generalizing this logic for digit lengths other than 2.
	*/ 
	protected static long computeMaximumJoltageOld(List<Integer> bank) {
		int size = bank.size();
		
		// Find the largest digit as far to the left as possible, skippign the last column.
		int bestFirstPos = -1;
		int bestFirstDigit = -1;
		for (int i = 0 ; i < (size - 1) ; i++) {
			int num = bank.get(i);
			if (num > bestFirstDigit) {
				bestFirstPos = i;
				bestFirstDigit = num;
			}
		}
		
		// Now find the next best digit, by moving right from our first char, and choosing the highest digit we see.
		int bestSecondDigit = -1;
		for (int i = bestFirstPos + 1 ; i < size ; i++) {
			int num = bank.get(i);
			if (num > bestSecondDigit) {
				bestSecondDigit = num;
			}
		}
		
		return (bestFirstDigit * 10) + bestSecondDigit;
	}
	
	
	/*
		This can solve both part 1 and part 2.
	*/
	protected static long computeMaximumJoltage(List<Integer> bank, int digitLength) {
		int size = bank.size();
		
		long maxJoltage = 0;
		int currStartPos = 0;
		for (int d = 0 ; d < digitLength ; d++) {
		
			// Find the largest digit as far to the left as possible, skipping enough at the 
			// end to still be able to get enough digits for the rest.
			int bestNextPos = -1;
			int bestNextDigit = -1;	
			int digitsAfterThis = (digitLength - d) - 1;
			for (int i = currStartPos ; i < (size - digitsAfterThis) ; i++) {
				int num = bank.get(i);
				if (num > bestNextDigit) {
					bestNextPos = i;
					bestNextDigit = num;
				}
			}
			
			long placeValue = (long) Math.pow(10, digitsAfterThis);
			maxJoltage += placeValue * bestNextDigit;
			System.out.format("  compute digit: d=%d, currPos=%d, bestDig=%d, bestPos=%d, placeValue=%d, maxjolt=%d\n", 
				d, currStartPos, bestNextDigit, bestNextPos, placeValue, maxJoltage);

			// Mark where the next digit search will start.
			currStartPos = bestNextPos + 1;
		}
		
		return maxJoltage;
	}
	
	/*
	*/
    public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.BatteryCacheParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	   	long totalOutputJoltage = 0;
	    boolean part1 = false;
	    
	    int bankCount = fp.getBankCount();
	    for (int b = 0 ; b < bankCount ; b++) {
	    	List<Integer> bank = fp.getBank(b);
	    	System.out.format("  bank %02d start: %s\n", b, bank);
	    	
	    	int digitLength = part1 ? 2 : 12;
	    	long maxJoltage = computeMaximumJoltage(bank, digitLength);
	    	totalOutputJoltage += maxJoltage;
	    	System.out.format("  bank %02d complete: maxJolt=%d, totalOutputJoltage=%d\n", b, maxJoltage, totalOutputJoltage);
	    }

		System.out.format("Process complete:\n totalOutputJoltage=%d\n", totalOutputJoltage);	
	}
}
