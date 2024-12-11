
import java.io.*;
import java.util.*;

public class Day10 {

	private static int DAY_NUM = 10;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;


	public static class PosSorter implements Comparator<Pos> 
	{ 
    		public int compare(Pos a, Pos b) 
    		{ 
			if (a.y != b.y)
				return a.y - b.y;
			return a.x - b.x;
    		} 
	} 

	public static class Pos {
		public final int x;
		public final int y;
		public Pos(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public String toString() { return String.format("(%d,%d)", x, y); }

		@Override
		public boolean equals(Object o) {
        		if (this == o) return true;
		        if (o == null || getClass() != o.getClass()) return false;
        		Pos that = (Pos) o;
        		return (this.x == that.x) && (this.y == that.y);
    		}

		@Override
    		public int hashCode() {
        		return Objects.hash(x, y);
    		}
	}

	public static boolean inBounds(Pos currPos, int width, int height) {
		if (currPos.x < 0 || currPos.x >= width)
			return false;
		if (currPos.y < 0 || currPos.y >= height)
			return false;
		return true;
	}

	public static boolean matchNeighbor(ScanContext sc, Pos currPos, char matcher) {
		if (!inBounds(currPos, sc.fp.getWidth(), sc.fp.getHeight())) {
			return false;
		}

		char c = sc.fp.getCell(currPos.x, currPos.y);
		if (c != matcher) {
			return false;
		}

		System.out.format("matchNeighbor %c, pos=%s\n", matcher, currPos);
		return true;
	}

	public static List<Pos> getNeighbors(ScanContext sc, Pos currPos, char matcher) {
		List<Pos> ret = new ArrayList<Pos>();

		Pos up = new Pos(currPos.x, currPos.y-1);
		if (matchNeighbor(sc, up, matcher))
			ret.add(up);
			
		Pos down = new Pos(currPos.x, currPos.y+1);
		if (matchNeighbor(sc, down, matcher))
			ret.add(down);
			
		Pos left = new Pos(currPos.x-1, currPos.y);
		if (matchNeighbor(sc, left, matcher))
			ret.add(left);

		Pos right = new Pos(currPos.x+1, currPos.y);
		if (matchNeighbor(sc, right, matcher))
			ret.add(right);
	
		return ret;
        }

	
	public static class ScanContext {
		public final Pos 		trailhead;
		public FileParser.MatrixParser 	fp;
		public HashSet<Pos> 		peaks;
		public HashSet<List<Pos>>	paths;

		public ScanContext(Pos th, FileParser.MatrixParser f) {
			trailhead = th;
			fp = f;
			peaks = new HashSet<Pos>();
			paths = new HashSet<List<Pos>>();
		}		
	}

	public static char getNextElevation(char e) {
		return (char) (e + 1);
	}


	public static int scan(Pos trailhead, FileParser.MatrixParser fp) {
		ScanContext sc = new ScanContext(trailhead, fp);
		scanInternal(sc, trailhead, '1', 0, new ArrayList<Pos>());

		if (part1) {	
			int numPeaks = sc.peaks.size();
			System.out.format("scan: COMPLETE %d, peaks=%s\n", numPeaks, sc.peaks);
			return numPeaks;
		}

		int numPaths = sc.paths.size();
		System.out.format("scan: COMPLETE %d, paths=%s\n", numPaths, sc.paths);
		return numPaths;
	}

	public static void scanInternal(ScanContext sc, Pos currPos, char seekElevation, int recursionLevel, List<Pos> lastPath) {
		List<Pos> currPath = new ArrayList<Pos>(lastPath);
		currPath.add(currPos);
		System.out.format("scan: currPos=%s, seek=%c, rc=%d, path=%s\n", currPos, seekElevation, recursionLevel, currPath);

		// find matches in each neighbor, and add them to a list.
		List<Pos> matches = getNeighbors(sc, currPos, seekElevation);

		for (Iterator<Pos> iter = matches.iterator() ; iter.hasNext() ; ) {
			Pos nextPos = iter.next();
			if (seekElevation == '9') {
				sc.peaks.add(nextPos);
				System.out.format("scan: found peak at %s, list %d %s\n",
					 nextPos, sc.peaks.size(), sc.peaks);

				List<Pos> dupPath = new ArrayList<Pos>(currPath);
				dupPath.add(nextPos);
				sc.paths.add(dupPath);
				System.out.format("scan: found path %s, list %d %s\n",
					 nextPos, sc.paths.size(), sc.paths);
			} else {
				char nextElevation = getNextElevation(seekElevation);
				scanInternal(sc, nextPos, nextElevation, ++recursionLevel, currPath);
			}
		}
	}
	
	
	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.MatrixParser fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		int width = fp.getWidth();
		int height = fp.getHeight();
		System.out.format("Analysis start: matrix width=%d, height=%d, part1=%b\n", width, height, part1);

		List<Pos> trailheads = new ArrayList<Pos>();
		for (int h = 0 ; h < height ; h++) {
			for (int w = 0 ; w < width ; w++) {
				char c = fp.getCell(w, h);
				if (c == '0') {
					trailheads.add(new Pos(w, h));
					System.out.format("Analysis trailhead at %d,%d\n", w, h);
				}
			}
		}
		System.out.format("Analysis trailhead discovery complete: %d\n%s\n",
			 trailheads.size(), trailheads);

		long sum = 0;
		for (Iterator<Pos> iter = trailheads.iterator() ; iter.hasNext() ; ) {
			Pos trailhead = iter.next();
			int score = scan(trailhead, fp);
			sum += score;
			System.out.format("\n\n ***** latest scan complete: score=%d, sum=%d\n", score, sum);
		}
		System.out.format("Scan complete: sum=%d\n", sum);
	}
}
