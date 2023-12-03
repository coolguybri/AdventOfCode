
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day12 {


	private static List<String> map = new ArrayList<String>();
  	static int map_width = 0;
	static int map_height = 0;
	

    static class PosState {
    	public final int x;
    	public final int y;
    	public final int state;
    	public boolean goodPath;
    	public boolean deadPath;
    	
    	public PosState(int p_x, int p_y, int s) {
    		x = p_x;
    		y = p_y;
    		state = s;
    		goodPath = false;
    		deadPath = false;
    	}
    	
    	public PosState(int p_x, int p_y, int s, boolean g, boolean d) {
    		x = p_x;
    		y = p_y;
    		state = s;
    		goodPath = g;
    		deadPath = d;
    	}
    	
    	public PosState updateState(int newstate) {
    		return new PosState(x, y, newstate, goodPath, deadPath);
    	}
    	
    	public String label() {
    		return String.format("%d:%d", x, y);
    	}
    	
    	
    	boolean climable(PosState next) {
    		// is it off the map?
    		if ((next.y >= map_height) || (next.y < 0) || (next.x >= map_width) || (next.x < 0)) {
    			//System.out.format("   [pos=(%d,%d) is out of bounds of the map]:\n", next.x, next.y);
    			return false;
    		}
    		
    		char currSymbol = getSymbol();
    		char nextSymbol = next.getSymbol();
    		if (nextSymbol == 'E')
    			nextSymbol = 'z';
    		else if (currSymbol == 'S')
    			currSymbol = 'a';	
    			
    		// is is too high?
    		int diff = nextSymbol - currSymbol;
    		if (diff > 1) {
    			//System.out.format("   [pos=(%d,%d) is too high (%d) (%c -> %c)]:\n", next.x, next.y, diff, currSymbol, nextSymbol);
    			return false;
    		}
    		
    		return true;
    	}
    	
    	
    	boolean isEnd() {
    		return getSymbol() == 'E';
    	}
    	
    	PosState up() {
    		return new PosState(x, y - 1, 0);
    	}
    	
    	PosState down() {
    		return new PosState(x, y + 1, 0);
    	}
    	
    	PosState right() {
    		return new PosState(x + 1, y, 0);
    	}
    	
    	PosState left() {
    		return new PosState(x - 1, y, 0);
    	}
    	
    	public char getSymbol() {
    		return map.get(y).charAt(x);
    	}
    	
    	public String toString() {
    		String pathState = "undetermined";
    		if (goodPath) {
    			pathState = "KNOWN-GOOD";
    		}
    		if (deadPath) {
    			pathState = "DEAD";
    		}
    		return String.format("[(%d,%d):%d:%s]", x, y, state, pathState);
    	}
    }
    
    
  public static PosState mapFindStart() throws Exception {
  	int row = 0;
  	for (Iterator<String> iter = map.iterator() ; iter.hasNext() ; row++) {
  		String mapRow = iter.next();
  		int startPos = mapRow.indexOf('S');
  		if (startPos >= 0) {
  			return new PosState(startPos, row, 0);
  		}
  	}
  	
  	throw new Exception("could not find Starting location!");
  }
  
  public static PosState mapFindEnd() throws Exception {
  	int row = 0;
  	for (Iterator<String> iter = map.iterator() ; iter.hasNext() ; row++) {
  		String mapRow = iter.next();
  		int startPos = mapRow.indexOf('E');
  		if (startPos >= 0) {
  			return new PosState(startPos, row, 0);
  		}
  	}
  	
  	throw new Exception("could not find Ending location!");
  }
    
	
	
  
  

  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 12 Mutha Fuckah:\n\n");
	
	boolean part1 = false;
	List<PosState> startingSpots = new ArrayList<PosState>();
	
	// Open input file.
   	File file = new File("day12.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	int linelen = -1;
   	String line;
   	while (((line = br.readLine()) != null)) {
   		map.add(line);
   		
   		if (linelen < 0) {
   			linelen = line.length();
   		} else if (linelen != line.length()) {
   			throw new Exception("illegal map");
   		}
   	}
   	PosState startPos = mapFindStart();
   	PosState endPos = mapFindEnd();
   	map_height = map.size();
   	map_width = linelen;
   	System.out.format("Map import complete: height=%d, width=%d, start=%d,%d, end=%d,%d\n",
   		 map.size(), linelen, startPos.x, startPos.y, endPos.x, endPos.y);
   		 
    if (part1) {
    	startingSpots.add(startPos);
    } else {
    	// scan for all a spots.
    	int row = 0;
    	for (Iterator<String> iter = map.iterator() ; iter.hasNext() ; row++) {
  			String mapRow = iter.next();
  			for (int col = 0 ; col < mapRow.length(); col++) {
  				char c = mapRow.charAt(col);
  				
  				if ((c == 'a') || (c == 'S')) {
  					System.out.format("map-scan MATCH: %d,%d=%c\n", col, row, c);
  					startingSpots.add(new PosState(col, row, 0));
  				}
  			}
  		}
    }
    
    System.out.format("About to process the %d/%d start point\n", startingSpots.size(), (map_height * map_width));
    /*for (Iterator<PosState> iter = startingSpots.iterator() ; iter.hasNext() ; ) {
   			System.out.format("\t%s\n", iter.next());
   	} */
   	
   	int shortest_path = Integer.MAX_VALUE;
   	int tot_runs = startingSpots.size();
   	int runs = 0;
   	for (Iterator<PosState> iter = startingSpots.iterator() ; iter.hasNext() ; runs++) {
   		PosState currStart = iter.next();
   		System.out.format("STARTING RUN %d/%d: %s (best=%d)\n", runs, tot_runs, currStart, shortest_path);
   		int len = processStart(currStart, runs, tot_runs);
   		System.out.format("ENDING RUN %d/%d: %s with %d (best=%d)\n", runs, tot_runs, currStart, len, shortest_path);
   		
   		if (len < shortest_path) {
   			shortest_path = len;
   			System.out.format("ENDING RUN New MaxLEN complete: shortest_path=%d\n",
   		 		shortest_path);
   		}
   	} 
   	
   	
   	System.out.format("END: with %d\n", shortest_path);
   }
   
	private static int processStart(PosState startPos, int runNum, int totRuns) throws Exception 	{
   	
		int min_path_len = Integer.MAX_VALUE;
	 	int solutions_found = 0;
		int dead_paths = 0;
		HashSet<String> prevNodes = new HashSet<String>();
	 	HashMap<String, Integer> bestPaths = new HashMap<String, Integer>();
   
   		// Init the path in our current starting position.
   		Stack<PosState> currPath = new Stack<PosState>();
   		currPath.add(0, startPos);
   	//	prevNodes.add(startPos.label());
   	
   	// Loop through the positions.
   
		int loopcount = 0;
		while (!currPath.empty()) {
			loopcount++;
			PosState currPos = currPath.pop();
		
			if ((loopcount % 100000) == 0) {
				System.out.format("start %d/%d: step %d start: %s, len=%d, solutions=%d, min_lengh=%d, dead_patchs=%d\n",
					 runNum, totRuns, loopcount, currPos, currPath.size(), solutions_found, min_path_len, dead_paths);
			}
		
			// See if we hit the end.
			if (currPos.isEnd()) {
				// Found the end!
				solutions_found++;
				int currentLen = currPath.size();
				//System.out.format("  found end, sol %d, len=%d, pos=%d,%d\n", solutions_found, currentLen, currPos.x, currPos.y);
			
				// Loop through the current path, marking them all as being members of a good path.
				for (int i = 0 ; i < currPath.size() ; i++) {
					PosState pos = currPath.get(i);
					pos.goodPath = true;
					//System.out.format("  marking %d,%d as good path\n", pos.x, pos.y);
				}
			
				if (currentLen < min_path_len) {
					min_path_len = currentLen;
					//System.out.format("  found new best, sol %d, len=%d\n", solutions_found, min_path_len);
				}
			
				continue;
			}
		
			Integer prevPathLen = bestPaths.get(currPos.label());
			if ((prevPathLen != null) && (prevPathLen <= currPath.size())) {
				//System.out.format("  found node again, but this path is not better, sol %d, len=%d, prevlen=%d, currlen=%d\n", 
				//	solutions_found, min_path_len, prevPathLen, currPath.size());
				continue;
			}
		
			prevNodes.add(currPos.label());
		
			// OK, time to make a move then. Look for the good next step.
			PosState nextPos = null;
			switch (currPos.state) {
			case 0:
				nextPos = currPos.up();
				if (!prevNodes.contains(nextPos.label()) && currPos.climable(nextPos)) {
					currPath.push(currPos.updateState(1));
					currPath.push(nextPos);
					//prevNodes.add(nextPos.label());
					continue;
				} 
				//System.out.format(" * cant go up\n");
				// Fall through!
				//currPos.state = 1;
			
			case 1:
				nextPos = currPos.right();
				if (!prevNodes.contains(nextPos.label()) && currPos.climable(nextPos)) {
					currPath.push(currPos.updateState(2));
					currPath.push(nextPos);
					//prevNodes.add(nextPos.label());
					continue;
				} 
				//System.out.format(" * cant go right\n");
				// Fall through!
				//currPos.state = 2;
			
			case 2:
				nextPos = currPos.down();
				if (!prevNodes.contains(nextPos.label()) && currPos.climable(nextPos)) {
					currPath.push(currPos.updateState(3));
					currPath.push(nextPos);
					//prevNodes.add(nextPos.label());
					continue;
				} 
				//System.out.format(" * cant go down\n");
				// Fall through!
				//currPos.state = 3;
			
			case 3:
				nextPos = currPos.left();
				if (!prevNodes.contains(nextPos.label()) && currPos.climable(nextPos)) {
					currPath.push(currPos.updateState(4));
					currPath.push(nextPos);
					//prevNodes.add(nextPos.label());
					continue;
				} 
				//System.out.format(" * cant go left\n");
				// Fall through!
				//currPos.state = 4;
			
			case 4:
				// We have tried all the paths from this node. Time to die, and pul my parent from the stack in the next iteratin of thr eloop.	
				//System.out.format(" * DEAD with pos=%s, len=%d\n", currPos, currPath.size());
				dead_paths++;
			
				//if (currPos.goodPath) {
					if (!prevNodes.remove(currPos.label())) {
						throw new Exception("what the fuck, prevNodes insanity");
					} 
				
					bestPaths.put(currPos.label(), currPath.size());
				//}
				break;
			
			default:
				throw new Exception("what the fuck, bad state");
			}
		}
   
		System.out.format("Path-finding complete. solutions=%d, min_lengh=%d, dead_patchs=%d\n", solutions_found, min_path_len, dead_paths);
		return min_path_len;
  	}
}
