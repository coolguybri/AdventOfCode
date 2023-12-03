
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day05 {

private static final int NUM_STACKS = 9;

/*

  1             [Q]     [G]     [M]
  2             [B] [S] [V]     [P] [R]
  3     [T]     [C] [F] [L]     [V] [N]
  4 [Q] [P]     [H] [N] [S]     [W] [C]
  5 [F] [G] [B] [J] [B] [N]     [Z] [L]
  6 [L] [Q] [Q] [Z] [M] [Q] [F] [G] [D]
  7 [S] [Z] [M] [G] [H] [C] [C] [H] [Z]
  8 [R] [N] [S] [T] [P] [P] [W] [Q] [G]
  9  1   2   3   4   5   6   7   8   9
  */
  
  
	static List<Stack<Character>> initStacks(Stack<String> header_stack) {
	
		// TODO: first line tells us how many stacks there are.
		String stackCount = header_stack.pop();
		int num_stacks = NUM_STACKS;
	
	    // Allocate the current number of stacks.
		ArrayList<Stack<Character>> stacks = new ArrayList<Stack<Character>>();
		for (int i = 0 ; i < num_stacks ; i++) {
		 	stacks.add(new Stack<Character>());
		}
		 
		int init_count = 0;
		String allocLine;
		while (!header_stack.empty() && ((allocLine = header_stack.pop()) != null)) {
			init_count++;
			
			for (int i = 0 ; i < num_stacks ; i++) {
			    char c = allocLine.charAt((i * 4) + 1);
				if (c != ' ') {
					stacks.get(i).push(c);
					System.out.format("init %d: pushing %c to %d\n", 	
						init_count, c, i);	
				}
			}
		}
		 
		return stacks;
	}


  public static void main(String[] args) throws Exception { 
  	System.out.format("Day Fiiiive Mutha Fuckah:\n\n");

	// Initial state.
	int step_count = 0;
	int skipped_count = 0;
	boolean part1 = false;
	
	// Open input file.
   	File file = new File("day5.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	String line;
   
    // Skip the header, stashing it for processing later. 
    Stack<String> header_stack = new  Stack<String>();
    while (((line = br.readLine()) != null) && (line.length() > 0)) {
    	skipped_count++;
    	header_stack.push(line);
    }
    
    // init the stacks.
    List<Stack<Character>> stacks = initStacks(header_stack);

	// Loop through the moves until it hits EOF.
	while ((line = br.readLine()) != null) {
		// Track how many rounds we are playing.
		step_count++;

		// Parse out the move.
		StringTokenizer tokenizer = new StringTokenizer(line, " ") ;
		tokenizer.nextToken();
		int move_count = Integer.parseInt(tokenizer.nextToken());
		tokenizer.nextToken();
		int src_num = Integer.parseInt(tokenizer.nextToken());
		tokenizer.nextToken();
		int dst_num = Integer.parseInt(tokenizer.nextToken());
		
		if (part1) {
		
			// Implement the move with old crane.
			for (int m = 0 ; m < move_count ; m++) {
				char c = stacks.get(src_num - 1).pop();
				
				
				stacks.get(dst_num - 1).push(c);
				
				System.out.format("step %d:  move_count=%d/%d, c=%c, src=%d (%d), dst=%d (%d)\n", 	
						step_count, m, move_count, c, src_num,  stacks.get(src_num - 1).size(), dst_num, stacks.get(dst_num - 1).size());	
			}
		} else {
		
			Stack<Character> reverso = new Stack<Character>();
			for (int m = 0 ; m < move_count ; m++) {
				char c = stacks.get(src_num - 1).pop();
				reverso.push(c);	
			}
			
			int m = 0;
			while (!reverso.empty()) {
				m++;
				char c = reverso.pop();
				stacks.get(dst_num - 1).push(c);
				System.out.format("step %d:  move_count=%d/%d, c=%c, src=%d (%d), dst=%d (%d)\n", 	
							step_count, m, move_count, c, src_num,  stacks.get(src_num - 1).size(), dst_num, stacks.get(dst_num - 1).size());	
			}
		}
	}
	
	String finalString = "";
	for (int i = 0 ; i < NUM_STACKS; i++) {
		char c = stacks.get(i).pop();
		finalString += c;
	}

	System.out.format("Analysis complete:\npart1=%b\nskipped_count=%d\nstepcount=%d\nfinalString=%s\n", 
		part1, skipped_count, step_count, finalString);
		
		
  }
}
