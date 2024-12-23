
import java.io.*;
import java.util.*;

public class Day12 {

	private static int DAY_NUM = 12;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = true;
	private static boolean part2 = !part1;


	public static class Pos {
		public final int x;
		public final int y;
		public Pos(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public String toString() {
			return String.format("(%d,%d)", x, y); 
		}

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

   		public Pos left() {
       		return new Pos(x-1, y);
   		}
   		public Pos right() {
       		return new Pos(x+1, y);
   		}
   		public Pos up() {
       		return new Pos(x, y-1);
   		}
   		public Pos down() {
       		return new Pos(x, y+1);
   		}
	}


	public static boolean inBounds(Pos currPos, int width, int height) {
		if (currPos.x < 0 || currPos.x >= width)
			return false;
		if (currPos.y < 0 || currPos.y >= height)
			return false;
		return true;
	}

	public static class Region {
		public final char marker;
		public List<Pos> members; 
		public int		edges;

		public Region(char m) {
			marker = m;
			members = new ArrayList<Pos>();
			edges = 0;
		}

		@Override
		public String toString() {
			return String.format("reg %c: edge=%d, area=%d, %s", marker, edges, members.size(), members);
		}
		
		public int getPerimeter() { return edges; }
		public int getArea() { return members.size(); }
	}
	
	public static class ScanContext {
		public FileParser.MatrixParser 	fp;
		public HashSet<Pos> 		processedCells;
		public List<Region>			regions;

		public ScanContext(FileParser.MatrixParser f) {
			fp = f;
			processedCells = new HashSet<Pos>();
			regions = new ArrayList<Region>();
		}		
	}


	public static boolean scanCellForRegion(ScanContext sc, Region r, Pos pos, int recursionLevel) {

		// Make sure we are inbounds.
		if (!inBounds(pos, sc.fp.getWidth(), sc.fp.getHeight())) {
			System.out.format("scan (%c): %s skipping out-of-bounds\n", r.marker, pos);
			return false;
		}

		// Make sure we are still in-region.
		char c = sc.fp.getCell(pos.x, pos.y);
		if (c != r.marker) {
			System.out.format("scan (%c): %s skipping due to out-of-range (%c)\n", r.marker, pos, c);
			return false;
		}

		// We are now inregion; lets see if we already oprocessed it. 
		if (sc.processedCells.contains(pos)) {
			System.out.format("scan (%c): %s skipping already-processed\n", r.marker, pos);
			return true;
		}

		// Got one!
		int newedges = 0;
		sc.processedCells.add(pos);
		r.members.add(pos);
		System.out.format("scan (%c): %s added to region (size=%d), starting nested scan\n",
			r.marker, pos, r.members.size());

		if (!scanCellForRegion(sc, r, pos.left(), recursionLevel+1))
			newedges++;
		if (!scanCellForRegion(sc, r, pos.right(), recursionLevel+1))
			newedges++;
		if (!scanCellForRegion(sc, r, pos.up(), recursionLevel+1))
			newedges++;
		if (!scanCellForRegion(sc, r, pos.down(), recursionLevel+1))
			newedges++;

		r.edges += newedges;
		System.out.format("scan (%c): %s finished nested scan, %d newedges, total=%d\n",
			r.marker, pos, newedges, r.edges);
		return true;
	}


	public static void startAnalysis(ScanContext sc, Pos pos) {
		if (sc.processedCells.contains(pos)) {
			System.out.format("startAnalysis: %s skipping already-processed\n", pos);
			return;
		}

		// if we got here we are starting a new region.
		char c = sc.fp.getCell(pos.x, pos.y);
		Region region = new Region(c);
		sc.regions.add(region);
		scanCellForRegion(sc, region, pos, 0);
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

		ScanContext sc = new ScanContext(fp);
		for (int h = 0 ; h < height ; h++) {
			for (int w = 0 ; w < width ; w++) {
				Pos pos = new Pos(w, h);
				char c = fp.getCell(w, h);

				System.out.format("Analysis cell %c at %s\n", c, pos);
				startAnalysis(sc, pos);
			}
		}

		System.out.format("Analysis discovery complete. %s\n", sc.regions);

		long sum = 0;
		for (Iterator<Region> regIter = sc.regions.iterator() ;  regIter.hasNext() ; ) {
			Region r = regIter.next();
			long rcost = ((long) r.getArea()) * ((long) r.getPerimeter());
			System.out.format("\t region %c p=%d, a=%d, s=%d, sum=%d\n", r.marker, r.getPerimeter(), r.getArea(), rcost, sum);
			sum += rcost;
		}
		System.out.format("Computation complete: %d\n", sum);
	}
}
