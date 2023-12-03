
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day10 {

	

  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 10 Mutha Fuckah:\n\n");
	
	// Open input file.
	int cycle = 0;
	int reg_x = 1;
   	File file = new File("day10.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	
   	boolean processing_command = false;
   	int end_cycle = 0;
   	String curr_verb = null;
   	String curr_param = null;
   	int signal_cycle = 20;
   	int signal_sum = 0;
   	
   	
   	// Init a blank screen.
   	List<String> crt = new ArrayList<String>();
   	for (int row = 0 ; row < 6 ; row++) {
   		crt.add(".".repeat(40));
   	}
   	
   	// Cycle loop.
   	while (true) {
   	
   		cycle++;
   		
   		// Beginning of cycle: If we are not already executing an instruction, read the next one in from the instruction pipelone.
   		if (!processing_command) {
   		
			// Read in the next instruction.
			String line;
			if ((line = br.readLine()) == null) {
				cycle--; // this last cycle did not actualy exist.
				System.out.format("Hit EOF\n");
				break;
			}
			
			processing_command = true;
			StringTokenizer tokenizer = new StringTokenizer(line, " ") ;
			curr_verb = tokenizer.nextToken();
			if (tokenizer.hasMoreTokens()) {
				curr_param = tokenizer.nextToken();
			}
			
			System.out.format(" * cycle=%d, start: verb=%s, param=%s\n", cycle, curr_verb, curr_param != null ? curr_param : "");
			if (curr_verb.equals("noop")) {
				end_cycle = cycle;
			} else if (curr_verb.equals("addx")) {
				end_cycle = cycle + 1;
			} else {
				throw new Exception(String.format("get the hell outa here with this bs instruction: %s", curr_verb));
			}
		} else {
			System.out.format("   cycle=%d, start: still processing \"%s\"\n", cycle, curr_verb);
		}
			
		// Middle of cycle.

		if (cycle == signal_cycle) {
			int signal_strength = cycle * reg_x;
			signal_sum += signal_strength;
			System.out.format("SIGNAL: cycle=%d, reg=%d, now=%d, sum=%d\n", cycle, reg_x, signal_strength, signal_sum);
			
			// Mark the next signal.
			signal_cycle += 40;
		}
		
		// Update CRT.
		int crt_x = ((cycle - 1) % 40);
		int crt_y = (cycle - 1) / 40;
		if ((crt_x >= (reg_x - 1)) && (crt_x <= (reg_x + 1))) {
			String line = crt.get(crt_y);
			String newline = line.substring(0, crt_x) + '#'  + line.substring(crt_x + 1);
			crt.set(crt_y, newline);
			System.out.format("VISIBLE: cycle=%d, crt_x=%d, crt_y=%d\n", cycle, crt_x, crt_y);
		}
		
		// End of cycle: potentially aply effetcvs of the instruction.
   		if (end_cycle < cycle) {
   			throw new Exception(String.format("illegal state machine with end_cycle too low"));
   		}
   		
   		if (end_cycle == cycle) {
   			System.out.format("   cycle=%d, end: verb=%s, param=%s\n", cycle, curr_verb, curr_param != null ? curr_param : "");
   			
   			// implemen the effects of the instruction.
   			if (curr_verb.equals("noop")) {
				// nothing
			} else if (curr_verb.equals("addx")) {
				int addend = Integer.parseInt(curr_param);
				reg_x += addend;
				System.out.format("   $ add complete: addend=%d, total=%d\n", addend, reg_x);
			} else {
				throw new Exception(String.format("get the hell outa here with this bs instruction: %s", curr_verb));
			}
   			
   			// reset state machine.
   			processing_command = false;
   			end_cycle = 0;
   			curr_verb = null;
   			curr_param = null;
   		} else {
   			System.out.format("   cycle=%d, end: still processing \"%s\"\n", cycle, curr_verb);
   		}
   	}
   	

   	System.out.format("finished cycles=%d\n", cycle);
   	
   	for (Iterator iter = crt.iterator() ; iter.hasNext() ; ) {
   		System.out.format(": %s\n", iter.next());
   	}
   	
   	
   	//System.out.format("Analyiss complete part 2, max_score=%d, x=%d, y=%d\n", max_score, best_x, best_y);
  }
}
