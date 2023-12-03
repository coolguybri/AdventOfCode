
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day06 {


  public static void main(String[] args) throws Exception { 
  	System.out.format("Day Six Mutha Fuckah:\n\n");

	// Initial state.
	boolean part1 = false;
	int char_count = 0;
	int marker_count = 4;
	if (!part1)
		marker_count = 14;
	
	// Open input file.
   	File file = new File("day6.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	String line = br.readLine();
   	
   	// The next line should be blank.
   	if (br.readLine() != null) {
   		throw new Exception(String.format("badfile with multiple line"));
   	}
   	
   	System.out.format("Parsing %d chars for input...\n", line.length());
   
    
	// Loop through the moves until it hits EOF.
	String matcher = "";
	for (int i = 0 ; i < line.length() ; i++) {
		char_count++;

		char c = line.charAt(i);
		int pos = matcher.indexOf(c);
		if (pos >= 0) {
		    
		    String newmatcher = "";
		    if (pos < matcher.length() - 1) {
		    	newmatcher = matcher.substring(pos + 1);
		    }
		    System.out.format("   repeat found: %c in \"%s\", new matcher=\"%s\"\n", c, matcher, newmatcher);
		    matcher = newmatcher;
		} 
		
		matcher += c;
		if (matcher.length() == marker_count) {
			System.out.format("   eom found: \"%s\", ending in pos=%d\n", matcher, char_count);
			break;
		}
	}
  }
}
