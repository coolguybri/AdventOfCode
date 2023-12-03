
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day14 {


 
  

  static class Point {
  	int x;
  	int y;
  	
  	public Point(int x, int y) {
  		this.x = x;
  		this.y = y;
  	}
  	
  	public String toString() {
  		return String.format("(%d,%d)", x, y);
  	}
  }
  
  
  static List<Point> parsePath(String line) {
  	List<Point> path = new ArrayList<Point>();
  	
  	StringTokenizer st = new StringTokenizer(line, " -> ");
  	while (st.hasMoreTokens()) {
  		String szpoint = st.nextToken();
  		int commaPos = szpoint.indexOf(',');
  		int x = Integer.parseInt(szpoint.substring(0, commaPos));
  		int y = Integer.parseInt(szpoint.substring(commaPos + 1));
  		
  		Point p = new Point(x, y);
  		path.add(p);
  	}
  	
  	return path;
  }
  
  
  static class Map {
  	List<String> 	data;
  	Point 			emitter;
  	int				x_shift;
  	int				floor;
  	
  	 int min_y = Integer.MAX_VALUE;
 	 int min_x = Integer.MAX_VALUE;
 	 int max_x = Integer.MIN_VALUE;
 	 int max_y = Integer.MIN_VALUE;

  	
  	 protected Map(List<List<Point>> paths, Point em) {
		
		// Note the location fo the emitter.
		emitter = em;
		buildMinMax(emitter);
		
		// Iterate over every point, deriving the bounds of the map.
		for (Iterator<List<Point>> iterPath = paths.iterator() ; iterPath.hasNext() ; ) {
			List<Point> path = iterPath.next();
			for (Iterator<Point> iterPoint = path.iterator() ; iterPoint.hasNext() ; ) {
				Point p = iterPoint.next();
				buildMinMax(p);
			}
		}
		
		int depth = (max_y - min_y) + 1;
		int width = (max_x - min_x) + 1;
		x_shift = -min_x;
		floor = -1;
	
		// init to all air.
		data = new ArrayList<String>();
		for (int d = 0 ; d < depth ; d++) {
			data.add(".".repeat(width));
		}
	
		// Loop through the paths.
		for (int pathPos = 0 ; pathPos < paths.size() ; pathPos++) {
		
			List<Point> path = paths.get(pathPos);
		
			// Loop through all the next points, interpolating the middle points.
			int pointlen = path.size();
			for (int pointPos = 0; pointPos < (pointlen - 1) ; pointPos++) {
				Point startPoint = path.get(pointPos);
				Point endPoint = path.get(pointPos + 1);
			
				int deltax = endPoint.x - startPoint.x;
				int deltay = endPoint.y - startPoint.y;
			
				if (deltax > 0) {
					for (int x = startPoint.x ; x <= endPoint.x; x++)
						setPoint(x, startPoint.y, '#');
				} else if (deltax < 0) {
					for (int x = startPoint.x ; x >= endPoint.x; x--)
						setPoint(x, startPoint.y, '#');
				} else if (deltay > 0) {
					for (int y = startPoint.y ; y <= endPoint.y; y++)
						setPoint(startPoint.x, y, '#');
				} else if (deltay < 0) {
					for (int y = startPoint.y ; y >= endPoint.y; y--)
						setPoint(startPoint.x, y, '#');
				}
			}
		}
	  }
  
  	
  	  protected void setPoint(Point p, char c) {
   		setPoint(p.x, p.y, c);
   	  }
   
	  protected void setPoint(int x, int y, char c) {
	  	int newx = x + x_shift;
		String line = data.get(y);
		String newline = line.substring(0, newx) + c + line.substring(newx + 1);
		data.set(y, newline);
	  }
	  
	  public boolean isEmpty(int x, int y) {
	  	int newx = x + x_shift;
	  	if ((y < 0) || (y >= data.size()))
	  		return true; // broke out of bottom of map.
	  		
	  	String line = data.get(y);
	  	if ((newx < 0) || (newx >= line.length()))
	  		return true; // broke out of side of map
	  		
	  	Character c = line.charAt(newx);
	  	return (c == '.');
	  	
	  }
	  
	  public boolean isOutOfBounds(int x, int y) {
	  	int newx = x + x_shift;
	  	if ((y < 0) || (y >= data.size()))
	  		return true; // broke out of bottom of map.
	  		
	  	String line = data.get(y);
	  	if ((newx < 0) || (newx >= line.length()))
	  		return true; // broke out of side of map
	  		
	  	return false;
	  }
	  
	  public boolean isOutOfBoundsX(int x) {
	  	int newx = x + x_shift;
	  	String line = data.get(0);
	  	if ((newx < 0) || (newx >= line.length()))
	  		return true; // broke out of side of map
	  	return false;
	  }
  
  
	   protected void print() {
		for (int i = 0 ; i < data.size() ; i++) {
			System.out.println(data.get(i));
		}
	  }
	  
	   private void buildMinMax(Point p) {
			if (p.x < min_x)
				min_x = p.x;
			if (p.x > max_x)
				max_x = p.x;
			if (p.y < min_y)
				min_y = p.y;
			if (p.y > max_y)
				max_y = p.y;
  		}
  	
  	public void enableFloor(int buffer) {
  		floor = max_y + buffer;
  		
  		int width = (max_x - min_x) + 1;
  		for (int i  = 0 ; i < (buffer - 1) ; i++) {
  			data.add(".".repeat(width));
  		}
  		data.add("#".repeat(width));
  	}
  	
  	
  	public void extend(int desired_x) throws Exception {
  		int new_desired_x = desired_x + x_shift;
  		System.out.format("EXTEND %d (%d)\n", desired_x, new_desired_x);
  		
  		
  		if (new_desired_x < 0) {
  			int newcolumns = (new_desired_x * -1) + 10;
  			for (int l = 0 ; l < data.size() ; l++) {
  				String symbol = (l == (data.size() - 1)) ? "#" : ".";
  				String line = data.get(l);
  				data.set(l, symbol.repeat(newcolumns) + line);
  			}
  			min_x -= newcolumns;
  			x_shift += newcolumns;
  		} else if (new_desired_x > 0) {
  			int width = (max_x - min_x) + 1;
  			int newcolumns = ((new_desired_x+1) - width) + 10;
  			System.out.format("  EXTEND newcols=%d width=(%d)\n", newcolumns, width);
  			for (int l = 0 ; l < data.size() ; l++) {
  				String symbol = (l == (data.size() - 1)) ? "#" : ".";
  				String line = data.get(l);
  				data.set(l, line + symbol.repeat(newcolumns));
  			}
  			max_x += newcolumns;
  		} else {
  			throw new Exception("what the fuck man");
  		}
  		
  		System.out.format("Map Extend End: with minx=%d, miny=%d, maxx=%d, maxy=%d, xshift=%d\n", 
			min_x, min_y, max_x, max_y, x_shift);
		print();
  	}
  }
  
  
  static class SandSimulator {
  	public Map map;
  	
  	public SandSimulator(Map m) {
  		map = m;
  	}
  	
  	public int run() throws Exception {
  	
  		for (int sand_count = 0; true ; sand_count++) {
  			int sand_x = 500;
  			int sand_y = 0;
  			//System.out.format("starting sand drop at: %d, %d\n", sand_x, sand_y);
  			
  			// Detect blockage at the spawn point.
  			if (!map.isEmpty(sand_x, sand_y)) {
  				System.out.format("  BLOCKAGE DETECTED at: %d, %d\n", sand_x, sand_y);
				return sand_count;
  			}
  			
  			while (true) {
  				if (map.isEmpty(sand_x, sand_y + 1)) {
  					sand_y++;
  				} else if (map.isEmpty(sand_x - 1, sand_y + 1)) {
  					sand_x--;
  					sand_y++;
  				} else if (map.isEmpty(sand_x + 1, sand_y + 1)) {
  					sand_x++;
  					sand_y++;
  				} else {
  					//System.out.format("  rest at: %d, %d\n", sand_x, sand_y);
  					map.setPoint(sand_x, sand_y, 'o');
  					break;
  				}
  				
  				if (map.floor < 0) {
					if (map.isOutOfBounds(sand_x, sand_y)) {
						System.out.format("  LEAK DETECTED at: %d, %d\n", sand_x, sand_y);
						return sand_count;
					} 
				} else {
					if (map.isOutOfBoundsX(sand_x)) {
						map.extend(sand_x);
				}
  			}
  		}
  	}
  	} 
  	}
  
  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 14 Mutha Fuckah:\n\n");
	
	List<List<Point>> paths = new ArrayList<List<Point>>();
	
	// Open input file & import paths.
   	File file = new File("day14.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
	String line;
	while ((line = br.readLine()) != null) {		
		List<Point> currPath = parsePath(line);
		paths.add(currPath);
		System.out.format("Import: path=%s\n", currPath);
	}
	System.out.format("Import End: with paths=%d\n", paths.size());
	
	boolean part1 = false;
		
	Point sandEmitter = new Point(500, 0);
	Map map = new Map(paths, sandEmitter);
	System.out.format("Map Construct End: with minx=%d, miny=%d, maxx=%d, maxy=%d\n\n", 
		map.min_x, map.min_y, map.max_x, map.max_y);
	if (!part1) {
		map.enableFloor(2);
	}
	map.print();
	
	SandSimulator sim = new SandSimulator(map);
	int sandcount = sim.run();
	map.print();
	System.out.format("Sand drop End: with count=%d\n", sandcount);
   }
}
