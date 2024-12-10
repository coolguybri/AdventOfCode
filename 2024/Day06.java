
import java.io.*;
import java.util.*;

public class Day06 {

	private static int DAY_NUM = 6;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;


	public enum Orientation {
		UP,
		RIGHT,
		DOWN,
		LEFT
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

	public static class PosVector {
		public final Pos pos;
		public final Orientation orient;
		public PosVector(Pos p, Orientation o) {
			pos = p;
			orient = o;
		}
		public String toString() { return String.format("[%d,%d,%s]", pos.x, pos.y, orient); }

		@Override
		public boolean equals(Object o) {
        		if (this == o) return true;
		        if (o == null || getClass() != o.getClass()) return false;
        		PosVector that = (PosVector) o;
        		return (this.pos.equals(that.pos)) && (this.orient == that.orient);
    		}

		@Override
    		public int hashCode() {
        		return Objects.hash(pos.x, pos.y, orient);
    		}
	}

	public static boolean inBounds(Pos currPos, int width, int height) {
		if (currPos.x < 0 || currPos.x >= width)
			return false;
		if (currPos.y < 0 || currPos.y >= height)
			return false;
		return true;
	}

	public static Orientation calculateNextOrientation(Orientation currOrientation) {
		switch (currOrientation) {
			case UP:
				return Orientation.RIGHT;
			case RIGHT:
				return Orientation.DOWN;
			case DOWN:
				return Orientation.LEFT;
			case LEFT:
				return Orientation.UP;
		}
		return null;
	}

	public static Pos calculateNextPos(Pos currPos, Orientation currOrientation) {
		switch (currOrientation) {
			case UP:
				return new Pos(currPos.x, currPos.y-1);
			case RIGHT:
				return new Pos(currPos.x+1, currPos.y);
			case DOWN:
				return new Pos(currPos.x, currPos.y+1);
			case LEFT:
				return new Pos(currPos.x-1, currPos.y);
		}
		return null;
	}

		
	public static int runSimulationPart1(PosVector startPos, FileParser.MatrixParser fp) throws Exception {
	
		Pos currPos = startPos.pos;
		Orientation currOrientation = startPos.orient;

		int moves = 0;
		HashSet<Pos> visited = new HashSet<Pos>();
		visited.add(currPos);

		for ( ; ; moves++) {
			Pos nextPos = calculateNextPos(currPos, currOrientation);
			if (!inBounds(nextPos, fp.getWidth(), fp.getHeight())) {
				System.out.format("Plot walked off the map %s\n", nextPos);
				break;
			}

			char nextCell = fp.getCell(nextPos.x, nextPos.y);
			switch (nextCell) {
				case '.':
				case '^':
					// Move into that next cell.
					System.out.format("[t:%d] Plot walking straight from %s to %s\n", moves, currPos, nextPos);
					currPos = nextPos;
					visited.add(currPos);
					break;

				case '#':
					Orientation nextOrientation = calculateNextOrientation(currOrientation);
					System.out.format("t:%d] Plot hit obstacle from %s to %s, turning to %s\n", moves, currPos, nextPos, nextOrientation);
					currOrientation = nextOrientation;
					break;

				default:
					throw new Exception("bad char in map");
					
			}
		}

		System.out.format("Plot finished in %d moves, visited=%d (%s)\n", moves, visited.size(), visited);
		return visited.size();
	}


	public static List<Pos> runSimulationPart2(PosVector startPos, FileParser.MatrixParser fp) throws Exception {
	
		int moves = 0;
		HashSet<Pos> visited = new HashSet<Pos>();
		visited.add(startPos.pos);
		
		{
			Pos currPos = startPos.pos;
			Orientation currOrientation = startPos.orient;

			for ( ; ; moves++) {
				Pos nextPos = calculateNextPos(currPos, currOrientation);
				if (!inBounds(nextPos, fp.getWidth(), fp.getHeight())) {
					System.out.format("Plot walked off the map %s\n", nextPos);
					break;
				}

				char nextCell = fp.getCell(nextPos.x, nextPos.y);
				switch (nextCell) {
					case '.':
					case '^':
						// Move into that next cell.
						System.out.format("[t:%d] Plot walking straight from %s to %s\n", moves, currPos, nextPos);
						currPos = nextPos;
						visited.add(currPos);
						break;

					case '#':
						Orientation nextOrientation = calculateNextOrientation(currOrientation);
						System.out.format("t:%d] Plot hit obstacle from %s to %s, turning to %s\n", moves, currPos, nextPos, nextOrientation);
						currOrientation = nextOrientation;
						break;
	
					default:
						throw new Exception("bad char in map");
				}
			}
		}

		System.out.format("Initial Plot finished in %d moves, visited=%d (%s)\n", moves, visited.size(), visited);
		List<Pos> permutationQueue = new ArrayList<Pos>();
		permutationQueue.addAll(visited);
		List<Pos> loopsDetected = new ArrayList<Pos>();;

		for (int p = 0 ; p < permutationQueue.size() ; p++) {
			Pos permutationPos = permutationQueue.get(p);
			Pos currPos = startPos.pos;
			Orientation currOrientation = startPos.orient;
			System.out.format("  Starting Perm %d with new block=%s\n", p, permutationPos);

			int permutationMoves = 0;
			HashSet<PosVector> loopVisited = new HashSet<PosVector>();
			loopVisited.add(new PosVector(currPos, currOrientation));
		
			for (boolean continuePerm = true ; continuePerm ; permutationMoves++) {
				Pos nextPos = calculateNextPos(currPos, currOrientation);
				if (!inBounds(nextPos, fp.getWidth(), fp.getHeight())) {
					System.out.format("Perm %d walked off the map %s\n", p, nextPos);
					continuePerm = false;
					break;
				}

				char nextCell;
				if (nextPos.equals(permutationPos)) {
					nextCell = '#';
					System.out.format("YO Perm %d hit new planted obstacle at %s, treating as obstruction\n", p, nextPos);
				} else {	
					nextCell = fp.getCell(nextPos.x, nextPos.y);
				}

				switch (nextCell) {
					case '.':
					case '^':
						// Move into that next cell.
						//System.out.format("[t:%d] Perm %d walking straight from %s to %s, visited=%s\n", 
						//	permutationMoves, p, currPos, nextPos, loopVisited);
						currPos = nextPos;
						if (!loopVisited.add(new PosVector(currPos, currOrientation))) {
							System.out.format("[t:%d] Perm %d detected loop at %s/%s\n", 
								permutationMoves, p, currPos, currOrientation);
							continuePerm = false;
							loopsDetected.add(permutationPos);
						}
	
						break;

					case '#':
						Orientation nextOrientation = calculateNextOrientation(currOrientation);
						//System.out.format("t:%d] Plot hit obstacle from %s to %s, turning to %s\n",
					//		 moves, currPos, nextPos, nextOrientation);
						currOrientation = nextOrientation;
						break;
	
					default:
						throw new Exception("bad char in map");
				}
			}
		}
		
		return loopsDetected;
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

		// Find the starting cell.
		Pos startPos = null;
		for (int h = 0 ; h < height ; h++) {
			for (int w = 0 ; w < width ; w++) {
				char c = fp.getCell(w, h);
				if (c == '^') {
					startPos = new Pos(w, h);
					break;
				}
			}
		}
		if (startPos == null) 
			throw new Exception("bad input");
		System.out.format("Plot start=%s\n", startPos);

		if (part1) {	
			int visited = runSimulationPart1(new PosVector(startPos, Orientation.UP), fp);
			System.out.format("Plot finished with visited=%d\n", visited);
		} else {
			List<Pos> possibleLoops = runSimulationPart2(new PosVector(startPos, Orientation.UP), fp);
			System.out.format("Plot finished with %d possibleLoops %s\n", possibleLoops.size(), possibleLoops);
		}
	}
}
