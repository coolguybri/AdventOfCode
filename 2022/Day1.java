
import java.io.*;
import java.util.*;

public class Day1 {

    public static void main(String[] args) throws Exception { 
    	System.out.format("Day One, Questions One\n\n");

        // Initial state.
       	boolean inElf = false;
       	int elf_num = 0;
       	int curr_score = 0;
       	int max_score = 0;
       	
       	File file = new File("day1.input");
        BufferedReader br = new BufferedReader(new FileReader(file));
        PriorityQueue<Integer> pQueue = new PriorityQueue<Integer>((a,b) -> b-a);
        
        // Loop through the input file.
        String line;
        while ((line = br.readLine()) != null) {
			if (line.length() == 0	) {
				if (inElf) {
					// See if we have a new max score.	
					if (curr_score > max_score) {
						max_score = curr_score;
						System.out.format("  ** new max score: %d\n", max_score);
					}
					
					// Add it to the priority queue.
					pQueue.add(curr_score);
					
					// Reset the state.
					System.out.format("elf %d complete: curr=%d, max=%d\n", elf_num, curr_score, max_score);
					inElf = false;
				} 
			} else {
				if (!inElf) {
					// Increment the elf count.
					elf_num++;
					
					// Reset the state.
					System.out.format("elf %d start\n", elf_num);
					inElf = true;
					curr_score = 0;
				}
				
				// Increment the current elf's calorie score.
				int curr = Integer.valueOf(line);
				curr_score += curr;
				//System.out.format("  new score: %d, %d\n", curr, curr_score);
			}
		}
		
		System.out.format("Analysis complete:\n max=%d\n", max_score);
		int team_total = 0;
		for (int i = 0 ; i < 3 ; i++) {
			int next_top = pQueue.poll();
			
			System.out.format("   Top %d:\n max=%d\n", i, next_top);
			team_total += next_top;
		}
		System.out.format("team_total=%d\n", team_total);
	}
}
