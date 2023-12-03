
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;

public class Day08 {

	private static List<String> trees = new ArrayList<String>();
	private static int max_x = 0;
	private static int max_y = 0;

  protected static int getTreeValue(int x, int y) {
  	String row = trees.get(y);
  	return Integer.parseInt(String.valueOf(row.charAt(x)))	;
  }
  
  
  protected static boolean searchUp(int x, int y, int curr_value) {
  	for (int search_y = y - 1 ; search_y >= 0 ; search_y--) {
  		int search_value = getTreeValue(x, search_y);
  		if (search_value >= curr_value) {
  			return false;
  		}
  	}
  	
  	return true;
  }
  
  protected static boolean searchDown(int x, int y, int curr_value) {
  	for (int search_y = y + 1 ; search_y < max_y ; search_y++) {
  		int search_value = getTreeValue(x, search_y);
  		if (search_value >= curr_value) {
  			return false;
  		}
  	}
  	
  	return true;
  }
  
  protected static boolean searchLeft(int x, int y, int curr_value) {
  	for (int search_x = x - 1 ; search_x >= 0 ; search_x--) {
  		int search_value = getTreeValue(search_x, y);
  		if (search_value >= curr_value) {
  			return false;
  		}
  	}
  	
  	return true;
  }
  
  protected static boolean searchRight(int x, int y, int curr_value) {
  	for (int search_x = x + 1 ; search_x < max_x ; search_x++) {
  		int search_value = getTreeValue(search_x, y);
  		if (search_value >= curr_value) {
  			return false;
  		}
  	}
  	
  	return true;
  }
  
  
  protected static int scoreUp(int x, int y, int curr_value) {
    int trees_visible = 0;
  	for (int search_y = y - 1 ; search_y >= 0 ; search_y--) {
  		trees_visible++;
  		int search_value = getTreeValue(x, search_y);
  		if (search_value >= curr_value) {
  			break;
  		}
  	}
  	
  	System.out.format("scoreUp (%d, %d), visible=%d\n", x, y, trees_visible);
  	return trees_visible;
  }
  
  protected static int scoreDown(int x, int y, int curr_value) {
    int trees_visible = 0;
  	for (int search_y = y + 1 ; search_y < max_y ; search_y++) {
  		trees_visible++;
  		
  		int search_value = getTreeValue(x, search_y);
  		if (search_value >= curr_value) {
  			break;
  		}
  	}
  	
  	System.out.format("  scoreDown (%d, %d), visible=%d\n", x, y, trees_visible);
  	return trees_visible;
  }
  
  protected static int scoreLeft(int x, int y, int curr_value) {
    int trees_visible = 0;
  	for (int search_x = x - 1 ; search_x >= 0 ; search_x--) {
  		trees_visible++;
  		
  		int search_value = getTreeValue(search_x, y);
  		if (search_value >= curr_value) {
  			break;
  		}
  	}
  	
  	System.out.format("  scoreLeft (%d, %d), visible=%d\n", x, y, trees_visible);
  	return trees_visible;
  }
  
  protected static int scoreRight(int x, int y, int curr_value) {
    int trees_visible = 0;
  	for (int search_x = x + 1 ; search_x < max_x ; search_x++) {
  		trees_visible++;
  		
  		int search_value = getTreeValue(search_x, y);
  		if (search_value >= curr_value) {
  			break;
  		}
  		
  	}
  	
  	System.out.format("  scoreRight (%d, %d), visible=%d\n", x, y, trees_visible);
  	return trees_visible;
  }
  
  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 8 Mutha Fuckah:\n\n");

	// Initial state.
	int visible_count = 0;
	
	// Open input file.
   	File file = new File("day8.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	String line;
   	int linelen = 0;
   	int linecount = 0;
   	while ((line = br.readLine()) != null) {
   		trees.add(line);
   		
   		if (linecount == 0) {
   			linelen = line.length();
   		} else if (linelen != line.length()) {
			throw new Exception("unequal line length!");
		}
		
   		linecount++;
   	}
   	max_x = linelen;
   	max_y = linecount;
   	
   	System.out.format("Parsed grid, length=%d, width=%d\n", linelen, linecount);
   	
   	for (int y = 0 ; y < linecount ; y++) {
   		for (int x = 0 ; x < linelen ; x++) {
   			int currValue = getTreeValue(x, y);
   			if (searchUp(x, y, currValue) || searchDown(x, y, currValue) || searchLeft(x, y, currValue) || searchRight(x, y, currValue)) {
   				visible_count++;
   			}
   		}
   	}
   	
   	System.out.format("Analyiss complete part 1, visible=%d\n", visible_count);
   	
   	int max_score = 0;
   	int best_x = 0;
   	int best_y = 0;
   	for (int y = 0 ; y < linecount ; y++) {
   		for (int x = 0 ; x < linelen ; x++) {
   			int currValue = getTreeValue(x, y);
   			int score =  scoreUp(x, y, currValue) * scoreDown(x, y, currValue) * scoreLeft(x, y, currValue) * scoreRight(x, y, currValue);
   			if (score > max_score) {
   				max_score = score;
   				best_x = x;
   				best_y = y;
   			}
   		}
   	}
   	
   	System.out.format("Analyiss complete part 2, max_score=%d, x=%d, y=%d\n", max_score, best_x, best_y);
  }
}
