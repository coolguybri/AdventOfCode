
import java.io.*;
import java.util.*;

public class Day04 {

    private static int DAY_NUM = 4;
    private static boolean SAMPLE_INPUT = false;

	private static char CHAR_ROLL = '@';
	private static char CHAR_DEFAULT = '~';

	/*
	*/
	public static boolean isSpecial(FileParser.MatrixParser fp, FileParser.Pos p) {
		int neighborRoll = 0;
		
		var up = p.up();
		if (fp.getCell(up, CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		if (fp.getCell(up.left(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		if (fp.getCell(up.right(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
			
		var down = p.down();
		if (fp.getCell(down, CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		if (fp.getCell(down.left(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		if (fp.getCell(down.right(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
			
		if (fp.getCell(p.left(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		if (fp.getCell(p.right(), CHAR_DEFAULT) == CHAR_ROLL)
			neighborRoll++;
		
		return (neighborRoll < 4);
	}

	/*
	*/
    public static void main(String[] args) throws Exception { 

		long startTime = System.currentTimeMillis();
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	    boolean part1 = false;
	    if (part1) {
	    	int numRolls = 0;
	    	int specialRolls = 0;
	    
			int height = fp.getHeight();
			int width = fp.getWidth();
			for (int h = 0 ; h < height ; h++) {
				for (int w = 0 ; w < width ; w++) {
					var p = new FileParser.Pos(w, h);
					char c = fp.getCell(p, CHAR_DEFAULT);
					if (c != CHAR_ROLL) 
						continue;
					
					numRolls++;
					System.out.format("found roll: %s (%d)\n", p, numRolls);
					
					if (isSpecial(fp, p)) {
						
						specialRolls++;
						System.out.format("found special: %s (%d)\n", p, specialRolls);
					}
				}
			}
			
			System.out.format("Process complete part 1:\n numRolls=%d, specialRolls=%d\n", 
				numRolls, specialRolls);
			
		} else {
		    int numRemoved = 0;
			int loopIter;
			for (loopIter = 0 ; true ; loopIter++) {

				var	 specials = new HashSet<FileParser.Pos>();
				int numRolls = 0;
				System.out.format(" iter %d: starting next loop\n", loopIter);

				int height = fp.getHeight();
				int width = fp.getWidth();
				for (int h = 0 ; h < height ; h++) {
					for (int w = 0 ; w < width ; w++) {
						var p = new FileParser.Pos(w, h);
						char c = fp.getCell(p, CHAR_DEFAULT);
						if (c != CHAR_ROLL) 
							continue;
						
						numRolls++;
						System.out.format(" iter %d: found roll: %s (%d)\n", loopIter, p, numRolls);
						
						if (isSpecial(fp, p)) {
							specials.add(p);
							System.out.format(" iter %d: found special: %s (%d)\n", loopIter, p, specials.size());
						}
					}
				}
				
				System.out.format(" iter %d: numRolls=%d, specialRolls=%d\n", 
					loopIter, numRolls, specials.size());
				
				// Terminal condition: could not find any rolls to pickup.
				if (specials.isEmpty()) {
					System.out.format(" iter %d: breakout\n", loopIter);
					break;
				}
				
				System.out.format(" iter %d: updating map\n", loopIter);
				for (var p : specials) {
					fp.setCell(p, 'x');
					numRemoved++;
				}
			}
				
			System.out.format("Process complete part 2:\n totalIter=%d, numRemoved=%d\n", 
				loopIter, numRemoved);
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
}
