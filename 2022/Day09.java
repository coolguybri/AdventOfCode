
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day09 {

	
	static class RopeEnd {
		int x;
		int y;
		
		RopeEnd() {
		 x = 0; 
		 y = 0;
		}
	}


  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 9 Mutha Fuckah:\n\n");

	// Initial state.
	int numknots = 9;
	int step_count = 0;
	RopeEnd head = new RopeEnd();
	RopeEnd knot[] = new RopeEnd[numknots];
	for (int i = 0 ; i < numknots ; i++) {
		knot[i] = new RopeEnd();
	}
	RopeEnd tail = knot[numknots - 1];
	HashSet<String> unique_positions = new HashSet<String>();
	unique_positions.add(String.format("%d,%d", tail.x,  tail.y));
	
	// Open input file.
   	File file = new File("day9.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	String line;
   	while ((line = br.readLine()) != null) {
   		// Parse the line.
   		StringTokenizer tokenizer = new StringTokenizer(line, " ") ;
		char direction = tokenizer.nextToken().charAt(0);
		int distance = Integer.parseInt(tokenizer.nextToken());
		step_count++;
		
		System.out.format(" Next step, direction=%c, distance=%d, start=%d,%d\n", direction, distance, head.x, head.y);
		
		for (int d = 0 ; d < distance ; d++) {
			// Move the head.
			switch (direction) {
				case 'U':
					head.y += 1;
					break;
				case 'D':
					head.y -= 1;
					break;
				case 'R':
					head.x += 1;
					break;
				case 'L':
					head.x -= 1;
					break;
			}
			
			// Move the knots in response.
			RopeEnd lastknot = head;
			for (int i = 0 ; i < numknots ; i++) {
				RopeEnd curr = knot[i];
				int x_delta = lastknot.x - curr.x;
				int y_delta = lastknot.y - curr.y;
				if ((Math.abs(x_delta) > 1) || (Math.abs(y_delta) > 1)) {
					if (y_delta > 0)
						curr.y += 1;
					else if (y_delta < 0)
						curr.y -= 1;
				
					if (x_delta > 0)
						curr.x += 1;
					else if (x_delta < 0)
						curr.x -= 1;
				}
				
				lastknot = curr;
			}
			
			// Mark the tail position.
			unique_positions.add(String.format("%d,%d", tail.x, tail.y));
		}
		
		System.out.format("   finished: head=%d,%d, tail=%d,%d\n", head.x, head.y, tail.x, tail.y);
   	}
   	
   	System.out.format("finished Steps=%d, unique=%d\n", step_count, unique_positions.size());
   	
   	
   	//System.out.format("Analyiss complete part 2, max_score=%d, x=%d, y=%d\n", max_score, best_x, best_y);
  }
}
