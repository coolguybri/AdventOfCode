
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day15 {


  static class Point {
  	long x;
  	long y;
  	
  	public Point(long x, long y) {
  		this.x = x;
  		this.y = y;
  	}
  	
  	public String toString() {
  		return String.format("(%d,%d)", x, y);
  	}
  }
  
  static class Map {
  	long			x_shift;
  	List<Sensor> 	sensors;
  	
  	 long min_y = Long.MAX_VALUE;
 	 long min_x = Long.MAX_VALUE;
 	 long max_x = Long.MIN_VALUE;
 	 long max_y = Long.MIN_VALUE;

  	
  	 protected Map(List<Sensor> s) {
		
		// Iterate over every point, deriving the bounds of the map.
		sensors = s;
		for (Iterator<Sensor> iter = sensors.iterator() ; iter.hasNext() ; ) {
			Sensor sensor = iter.next();
			buildMinMax(sensor.sensorPos);
			buildMinMax(sensor.nearestBeaconPos);
		}
		
		long depth = (max_y - min_y) + 1;
		long width = (max_x - min_x) + 1;
		x_shift = -min_x;
	  }
  

	  
	  public boolean isEmpty(long x, long y) {
	 	for (Iterator<Sensor> iter = sensors.iterator() ; iter.hasNext() ; ) {
			Sensor sensor = iter.next();
			if ((sensor.sensorPos.x == x) && (sensor.sensorPos.y == y)) {
				System.out.format("Found SENSOR at %s\n", sensor.sensorPos);
				return false;
			}
			if ((sensor.nearestBeaconPos.x == x) && (sensor.nearestBeaconPos.y == y)) {
				System.out.format("Found BEACON at %s\n", sensor.nearestBeaconPos);
				return false;
			}
		}
		return true;
	  }
	  
	  
  
	   /* protected void print() {
		for (int i = 0 ; i < data.size() ; i++) {
			System.out.println(data.get(i));
		}
	  } */
	  
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
  	
  	
  	
  	boolean checkExcludedScan(long x, long y) {
  		
  		for (Iterator<Sensor> iter = sensors.iterator() ; iter.hasNext() ; ) {
			Sensor sensor = iter.next();
			
			if ((sensor.nearestBeaconPos.x == x) && (sensor.nearestBeaconPos.y == y)) {
				System.out.format("Found BEACON at %s\n", sensor.nearestBeaconPos);
				return true;
			}
				
			long distance = calculateDistance(sensor.sensorPos.x, sensor.sensorPos.y, x, y);
			if (distance <= sensor.nearestBeaconDistance)
				return true;
		}
		
		return false;
  	}
  	
  	
  	int checkExcluded(long y) {
  		int ret = 0;
  		
  		// Check out the current data.
  		for (long x = min_x ; x <= max_x ; x++) {
  			// Loop through all the sensors, and see if it is excluded.
  			if (checkExcludedScan(x, y)) {
  				System.out.format("checkExcluded x=%d, y=%d, got match to main sequence\n", x, y);
  				ret++;
  			}
  		}
  		
  		// Continue to the right, until we hit one that is not.
  		for (long x = max_x + 1 ; true ; x++) {
  			if (!checkExcludedScan(x, y))
  				break;
  				
  			System.out.format("checkExcluded x=%d, y=%d, got match to the right\n", x, y);
  			ret++;
  		}
  		
  		// Continue to the left, until we hit one that is not.
  		for (long x = min_x - 1 ; true ; x--) {
  			if (!checkExcludedScan(x, y))
  				break;
  				
  			System.out.format("checkExcluded x=%d, y=%d, got match to the left\n", x, y); 
  			ret++;
  		}
  		
  		return ret;
  	}
  }
  
  
  static long calculateDistance(Point a, Point b) {
  	return calculateDistance(a.x, a.y, b.x, b.y);
  }
  
  static long calculateDistance(long ax, long ay, long bx, long by) {
  	return Math.abs(ax - bx) + Math.abs(ay - by);
  }
  	
  	static class Sensor {
  		 Point sensorPos;
  		 Point nearestBeaconPos;
  		 long   nearestBeaconDistance;
  		
  		public Sensor(Point sp, Point bp) {
  			sensorPos = sp;
  			nearestBeaconPos = bp;
  			nearestBeaconDistance = calculateDistance(sp, bp);
  		}
  		
  		public String toString() {
  			return String.format("{sensor=%s,beacon=%s,dist=%d}", 
  				sensorPos, nearestBeaconPos, nearestBeaconDistance);
  		}
  	}
  	
  	
  static Sensor parseSensor(String line) {
  		// Sample line:
  		// Sensor at x=2, y=18: closest beacon is at x=-2, y=15
  		int xPosFirst = line.indexOf("x=") + 2;
  		int xPosEnd = line.indexOf(",", xPosFirst + 1);
  		int yPosFirst = line.indexOf("y=") + 2;
  		int yPosEnd = line.indexOf(":", yPosFirst + 1);
  		Point sensorPos = new Point(Integer.parseInt(line.substring(xPosFirst, xPosEnd)), Integer.parseInt(line.substring(yPosFirst, yPosEnd)));
  		System.out.format("parseSensor: sensorPos=%s\n", sensorPos);
  		
  		xPosFirst = line.indexOf("x=", yPosEnd + 1) + 2;
  		xPosEnd = line.indexOf(",", xPosFirst + 1);
  		yPosFirst = line.indexOf("y=", yPosEnd + 1) + 2;
  		Point beaconPos = new Point(Integer.parseInt(line.substring(xPosFirst, xPosEnd)), Integer.parseInt(line.substring(yPosFirst)));
		return new Sensor(sensorPos, beaconPos);
  }
  
  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 15 Mutha Fuckah:\n\n");
  	
  	long start = System.currentTimeMillis();
	
	List<Sensor> sensors = new ArrayList<Sensor>();
	
	// Open input file & import paths.
   	File file = new File("day15.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
	String line;
	while ((line = br.readLine()) != null) {		
		Sensor sensor = parseSensor(line);
		sensors.add(sensor);
		System.out.format("Import: sensor=%s\n", sensor);
	}
	System.out.format("Import End: with sensorsCount=%d\n", sensors.size());
	
	Map map = new Map(sensors);
	//map.print();
	System.out.format("Map Construct End: with minx=%d, miny=%d, maxx=%d, maxy=%d, shift=%d\n\n", 
		map.min_x, map.min_y, map.max_x, map.max_y, map.x_shift);
	
	boolean part1 = false;
	
	if (part1) {
		// Look at a sample row.
		int rowExcluded = 2000000;
		//int rowExcluded = 10;
		int excluded = map.checkExcluded(rowExcluded);
		System.out.format("Analysis End: with row=%d, excluded=%d\n", rowExcluded, excluded);
	} else {
		long limit = 4000000;
		//long limit = 100000;
		//long limit = 20;
		
		long scanStart = 0;
		long runs = limit * limit;
		
		System.out.format("Analysis Start: checks=%d\n", runs);
		
		long numchecks = 0;
		List<Point> goodPoints = new ArrayList<Point>();
		for (long y = scanStart ; y < limit ; y++) {
			for (long x = 0 ; x < limit ; x++) {
			
				numchecks++;
				
				if ((numchecks % 10000000) == 0) {
					long now = System.currentTimeMillis();
					System.out.format("  check [%d / %d], time %d: inspecting (%d,%d)\n", numchecks, runs, ((now - start) / 1000), x, y);
				}
				
				if (!map.checkExcludedScan(x, y)) {
					System.out.format("Analysis Find: non-excluded (%d,%d), freq=%d\n", x, y, ((x * 4000000) + y));
					goodPoints.add(new Point(x, y));
				}
			}
		}
		
		long now = System.currentTimeMillis();
		long millis = ((now - start));
		double kruns = runs / 100000.0;
		double tpc = ((double) millis) / kruns;
		System.out.format("Analysis End: time %d, tpc=%.01f, total non-excluded=%d\n", 
			(millis / 1000), tpc, goodPoints.size());
		for (Iterator<Point> iter = goodPoints.iterator() ; iter.hasNext() ; ) {
			Point p = iter.next();
			System.out.format("\t%s\n", p);
		}
	}
   }
}
