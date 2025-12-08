
import java.io.*;
import java.util.*;

public class Day06 {

    private static int DAY_NUM = 6;
    private static boolean SAMPLE_INPUT = false;

	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.MathProblemParser(DAY_NUM, SAMPLE_INPUT);
		
		boolean part1 = false;
		if (part1) {
	    	fp.parse();
	  	} else {
	    	fp.parsePart2();
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
}
