
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day04 {

  public static void main(String[] args) throws Exception { 
  	System.out.format("Day Foooour Mutha Fuckah:\n\n");

	// Initial state.
	int elfpair_count = 0;
	int fully_enclosed_count = 0;
	int partial_overlap_count = 0;
	
	// Open input file.
   	File file = new File("day4.input");
    BufferedReader br = new BufferedReader(new FileReader(file));
    {
		// Loop through the input file until it hits EOF.
		String line;
		while ((line = br.readLine()) != null) {
			// Track how many rounds we are playing.
			elfpair_count++;

			// Skip blank lines.
			if (line.length() == 0)
				continue;
				
			// Parse it out.
			StringTokenizer tokenizer = new StringTokenizer(line, "-,") ;
			int low1 = Integer.parseInt(tokenizer.nextToken());
			int high1 = Integer.parseInt(tokenizer.nextToken());
			int low2 = Integer.parseInt(tokenizer.nextToken());
			int high2 = Integer.parseInt(tokenizer.nextToken());
			System.out.format("elfpair %d:  %d->%d, %d->%d\n", 	
					elfpair_count, low1, high1, low2, high2);
				
			// See if ranges fully overlap.
			if (((low1 <= low2) && (high1 >= high2)) ||
				((low2 <= low1) && (high2 >= high1))) {
				fully_enclosed_count++;
				System.out.format("** elfpair MATCH %d:  %d->%d, %d->%d\n", 	
					elfpair_count, low1, high1, low2, high2);
			} 
			
			// See if there is any overlap at all.
			if (((high1 >= low2) && (low1 <= high2)) ||
				((high2 >= low1) && (low2 <= high1))) {
				partial_overlap_count++;
				System.out.format("** elfpair PARTIAL MATCH %d:  %d->%d, %d->%d\n", 	
					elfpair_count, low1, high1, low2, high2);
			} 
		
		}
	} 
		
	System.out.format("Analysis complete:\npaircount=%d, fully_enclosed=%d, partial_overlap_count=%d\n", 
		elfpair_count, fully_enclosed_count, partial_overlap_count);
  }
}
