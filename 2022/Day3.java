
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day3 {


 static char findDuplicatedType(int count, String line) throws Exception {
 
 	// Freak if we see malformed line.
 	int len = line.length();
	if ((len % 2) != 0) {
		throw new Exception(String.format("round %d crap: odd-len input: \"%s\"", count, line));
	}
	
	// Break into two compartments
	int breakpoint = len / 2;
	String compartmentOne = line.substring(0, breakpoint);
	String compartmentTwo = line.substring(breakpoint);
	
	
	Character oddball = null;
	for (int i = 0 ; i < breakpoint ; i++) {
		char searchChar = compartmentOne.charAt(i);
		int foundIt = compartmentTwo.indexOf(searchChar);
		
		if (foundIt >= 0) {
			if ((oddball != null) && (searchChar != oddball)) {
				throw new Exception(String.format("round %d crap: duplicated oddball: \"%s\", search=%c, oddball=%c", count, line, searchChar, oddball));
			}
			
			oddball = Character.valueOf(searchChar);
		}
	}
	
	if (oddball == null) {
		throw new Exception(String.format("round %d crap: no oddball detected: \"%s\"", count, line));
	}
	
	char ret = oddball;
	System.out.format("line split %s, %s, %s, %c\n", line, compartmentOne, compartmentTwo, ret);
	return ret;
 }
 
 
 static char findCommonType(int count, String line, String line2, String line3) throws Exception {
 
	Character common = null;
	for (int i = 0 ; i < line.length() ; i++) {
		char searchChar = line.charAt(i);
		boolean foundIt = (line2.indexOf(searchChar) >= 0) && (line3.indexOf(searchChar) >= 0);
		if (foundIt) {
			if ((common != null) && (searchChar != common)) {
				throw new Exception(String.format("round %d crap: multiple common: \"%s\", search=%c, common=%c", count, line, searchChar, common));
			}
			
			common = Character.valueOf(searchChar);
		}
	}
	
	if (common == null) {
		throw new Exception(String.format("round %d crap: no common detected: \"%s\"", count, line));
	}
	
	char ret = common;
	System.out.format("  parsed group %d: %s, %s, %s, %c\n", count, line, line2, line3, ret);
	return ret;
 }
 
 
 static int calculatePriority(char itemType) throws Exception {
    int priority = 0;
    
 	if ((itemType >= 'a') && (itemType <= 'z')) {
 	   int delta = itemType - 'a';
 		priority = 1 + delta;
 	} else if ((itemType >= 'A') && (itemType <= 'Z')) {
 		 int delta = itemType - 'A';
 		priority = 27 + delta;
 	} else {
 		throw new Exception(String.format("bad itemtype: \"%c\"", itemType));
 	}
 	
 	return priority;

 }

  public static void main(String[] args) throws Exception { 
  	System.out.format("Day Threeeeee Mutha Fuckah! Question One, Yo:\n\n");

	// Initial state.
	int elfgroup_count = 0;
	int rucksack_count = 0;
	int priority_total = 0;

	// Open input file.
   	File file = new File("day3.input");
    BufferedReader br = new BufferedReader(new FileReader(file));
    
    boolean part1 = false;
    
    if (part1) {
		// Loop through the input file until it hits EOF.
		String line;
		while ((line = br.readLine()) != null) {
			// Track how many rounds we are playing.
			rucksack_count++;

			// Skip blank lines.
			if (line.length() == 0)
				continue;

			// Process the current line, calc the priority of its badtype.
			char badItemType = findDuplicatedType(rucksack_count, line);
			int priority_current = calculatePriority(badItemType);
		
			// Increment the priority sum.
			priority_total += priority_current;
			System.out.format("rucksack %d end, type=%c, priority=%d, total=%d\n", 	
				rucksack_count, badItemType, priority_current, priority_total);
		}
	} else {
		// Loop through the input file until it hits EOF.
		String line;
		while ((line = br.readLine()) != null) {
		
			rucksack_count++;
			String line2 = br.readLine();
			rucksack_count++;
			String line3 = br.readLine();
			rucksack_count++;
			if (line2 == null || line3 == null)
				throw new Exception(String.format("bad row parsing"));
				
			elfgroup_count++;
			
			// Process the current line, calc the priority of its badtype.
			char commonType = findCommonType(elfgroup_count, line, line2, line3);
			int priority_current = calculatePriority(commonType);
		
			// Increment the priority sum.
			priority_total += priority_current;
			System.out.format("elfgroup %d, common=%c, priority=%d, total=%d\n", 	
				elfgroup_count, commonType, priority_current, priority_total);
		}
	}
		
	System.out.format("Analysis complete:\n part1=%b, rucksacks=%d, total=%d\n", part1, rucksack_count, priority_total);
  }
}
