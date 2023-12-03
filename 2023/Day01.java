
import java.io.*;
import java.util.*;

public class Day01 {

    private static Map<String, Integer> INT_STRING_MAP = Map.of(
		"one", 1,
		"two", 2,
		"three", 3,
		"four", 4,
		"five", 5,
		"six", 6,
		"seven", 7,
		"eight", 8,
		"nine", 9);

    private static int getFirstIntegerEx(String s) {

		boolean found = false;
		int best_position = 0;
        int best_int = 0;

        // First look for any integers.
        int slen = s.length();
		for (int i = 0; i < slen ; i++) {
            char c = s.charAt(i);
			if (Character.isDigit(c)) {
                found = true;
				best_position = i;
				best_int = Integer.valueOf(c) - 48;
				break;
			}
		}
//		System.out.format("getFirstIntegerEx:postIntSearch line=%s, found=%b, pos=%d, int=%d\n", s, found, best_position, best_int);

        // Now look for the strings.
		for (Map.Entry<String,Integer> entry : INT_STRING_MAP.entrySet()) {
           int newPos = s.indexOf(entry.getKey());
		   if ((newPos >= 0) && (!found || (newPos < best_position))) {
              found = true;
              best_position = newPos;
			  best_int = entry.getValue();
		   }
		}

        if (found) {
          return best_int;
		}

        throw new IllegalArgumentException(String.format("no integer found in string \"%s\"", s));
   }

    private static int getLastIntegerEx(String s) {

		boolean found = false;
		int best_position = 0;
        int best_int = 0;

        // First look for any integers.
        int slen = s.length();
		for (int i = slen - 1; i >= 0 ; i--) {
            char c = s.charAt(i);
			if (Character.isDigit(c)) {
                found = true;
				best_position = i;
				best_int = Integer.valueOf(c) - 48;
				break;
			}
		}
//		System.out.format("getLastIntegerEx:postIntSearch line=%s, found=%b, pos=%d, int=%d\n", s, found, best_position, best_int);

        // Now look for the strings.
		for (Map.Entry<String,Integer> entry : INT_STRING_MAP.entrySet()) {
           int newPos = s.lastIndexOf(entry.getKey());
		   if ((newPos >= 0) && (!found || (newPos > best_position))) {
              found = true;
              best_position = newPos;
			  best_int = entry.getValue();
		   }
		}

        if (found) {
          return best_int;
		}

        throw new IllegalArgumentException(String.format("no integer found in string \"%s\"", s));
   }

    private static int getFirstInteger(String s) {
        int slen = s.length();
		for (int i = 0; i < slen ; i++) {
            char c = s.charAt(i);
			if (Character.isDigit(c)) {
				return Integer.valueOf(c) - 48;
			}
		}
        throw new IllegalArgumentException(String.format("no integer found in string \"%s\"", s));
   }

    private static int getLastInteger(String s) {
        int slen = s.length();
		for (int i = slen - 1 ; i >= 0 ; i--) {
            char c = s.charAt(i);
			if (Character.isDigit(c)) {
				return Integer.valueOf(c) - 48;
			}
		}
        throw new IllegalArgumentException(String.format("no integer found in string \"%s\"", s));
	}

    public static void main(String[] args) throws Exception { 
    	System.out.format("Day One, Questions One\n\n");

        // Initial state.
        int sum = 0;

        String filename = "day01.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        
        // Loop through the input file.
        String line;
        int lineNum = 0;
        for (; (line = br.readLine()) != null ; lineNum++) {
   			// Skip blank lines.
            line = line.trim();
			if (line.length() == 0	) {
				continue;
			} 

            int firstInt = getFirstIntegerEx(line);
            int secondInt = getLastIntegerEx(line);
			int result = 10 * firstInt + secondInt;
            sum += result;
			System.out.format("line %d ints=%d,%d, result=%d, sum=%d\t\t%s\n", lineNum, firstInt, secondInt, result, sum, line);
		}
		
		System.out.format("Analysis complete:\n lines=%d\n result=%d\n", lineNum, sum);
	}
}
