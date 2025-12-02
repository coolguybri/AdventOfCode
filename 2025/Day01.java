
import java.io.*;
import java.util.*;

public class Day01 {

    private static int DAY_NUM = 1;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.RotationParser fp = new FileParser.RotationParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();

		List<FileParser.Rotation> rotations = fp.getList();

		boolean part1 = false;
	
		int currPosition = 50;
		int specialCounter = 0;
		int rotationCount = 0;
		int rotationTotal = rotations.size();
		
		System.out.format("\nStarting with initial pos=%d\n", currPosition);
				 
		for (var iter = rotations.iterator() ; iter.hasNext() ; rotationCount++) {
			var rotation = iter.next();
			int crossOrLandZero = 0;
			int oldPosition = currPosition;
			
			if (!rotation.directionIsLeft) {
				// Turning Right naturally finds when we cross zero.
				currPosition += rotation.clickCount;
				while (currPosition > 99) {
					currPosition -= 100;
					crossOrLandZero++;
				}
			} else {
				// Gotta put in a bunch of special logic to detect transitions to 0.
				boolean atZero = (currPosition == 0);
				currPosition -= rotation.clickCount;
				while (currPosition < 0) {
					currPosition += 100;  
					if (!atZero)
						crossOrLandZero++;
					atZero = false;
				}
				if (currPosition == 0) {
					crossOrLandZero++;
				}
			}
			
			if (part1) {
				if (currPosition == 0) {
					specialCounter++;
				}
			} else {
				specialCounter += crossOrLandZero;
			}
			
			System.out.format("rotation %d/%d %s pos=%d, crossZero=%d, counter=%d\n",
				 rotationCount, rotationTotal, rotation, currPosition, crossOrLandZero, specialCounter);
				 
		}
		
		System.out.format("Process complete:\n specialCounter=%d\n", specialCounter);
				
	}
}
