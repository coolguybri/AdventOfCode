
import java.io.*;
import java.util.*;

public class Day08 {

	private static int DAY_NUM = 8;
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


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.MatrixParser fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		int width = fp.getWidth();
		int height = fp.getHeight();
		System.out.format("Analysis start: matrix width=%d, height=%d, part1=%b\n", width, height, part1);

		HashMap<Character, List<Pos>> antennas = new HashMap<Character, List<Pos>>();
		for (int h = 0 ; h < height ; h++) {
			for (int w = 0 ; w < width ; w++) {
				char c = fp.getCell(w, h);
				if (Character.isLetterOrDigit(c)) {
					List<Pos> as = antennas.get(c);
					if (as == null) {
						as = new ArrayList<Pos>();
						antennas.put(c, as);
					}
					as.add(new Pos(w, h));
					System.out.format("Analysis %d,%d: frequency=%c, len=%d\n", w, h, c, as.size());
				} else if (c == '.') {
					//System.out.format("Analysis: %d,%d: blank\n", w, h);
				} else {
					System.out.format("Analysis %d,%d STRANGE char=%c\n", w, h, c);
				}
			}
		}
		System.out.format("Analysis antenna acquisition complete:\n%s\n", antennas);


		HashSet<Pos> inboundsAnodes = new HashSet<Pos>();
		for (Iterator<Character> antennaIter = antennas.keySet().iterator() ; antennaIter.hasNext(); ) {
			char frequency = antennaIter.next();
			List<Pos> positions = antennas.get(frequency);
			int posLen = positions.size();
			System.out.format("antinode generation: freq %c\n", frequency);

			for (int i = 0 ; i < (posLen-1) ; i++) {
				for (int j = i + 1; j < posLen ; j++) {
					Pos pos1 = positions.get(i);
					Pos pos2 = positions.get(j);
					int deltaX = pos2.x - pos1.x;		
					int deltaY = pos2.y - pos1.y;

					if (part1) {
						Pos anode1 = new Pos(pos1.x - deltaX, pos1.y - deltaY);
						Pos anode2 = new Pos(pos2.x + deltaX, pos2.y + deltaY);
						System.out.format("antinode computed between %s-%s: frequency=%c, edge=%d/%d, delta=%d,%d, nodes=%s,%s\n",
							 pos1, pos2, frequency, i, j, deltaX, deltaY, anode1, anode2);
	
						if (inBounds(anode1, width, height)) {
							if (!inboundsAnodes.add(anode1)) {
								System.out.format("  NOT ADDED antinode 1 %s, it is already in the list=%s\n", anode1, inboundsAnodes);
							} else {
								System.out.format("  added antinode 1 %s, list=%s\n", anode1, inboundsAnodes);
							}
						} else {
							System.out.format("  DROPPED antinode 1 %s for out-of-bounds\n", anode1);
						}
						if (inBounds(anode2, width, height)) {
							if (!inboundsAnodes.add(anode2)) {
								System.out.format("  NOT ADDED antinode 2 %s, it is already in the list=%s\n", anode2, inboundsAnodes);
							} else {
								System.out.format("  added antinode 2 %s, list=%s\n", anode2, inboundsAnodes);
							}
						} else {
							System.out.format("  DROPPED antinode 2 %s for out-of-bounds\n", anode2);
						}
					} else {
						Pos curr1 = pos1;
						for (int wave = 0 ; ; wave++) {
							System.out.format("anode1 wave %d %s-%s: freq=%c, edge=%d/%d, delta=%d,%d, node=%s\n",
							 wave, pos1, pos2, frequency, i, j, deltaX, deltaY, curr1);
	
							if (!inBounds(curr1, width, height)) {
								System.out.format("  DROPPED antinode 1 %s for out-of-bounds\n", curr1);
								break;
							}

							if (!inboundsAnodes.add(curr1)) {
								System.out.format("  NOTADDED %s, already in list=%s\n",
									 curr1, inboundsAnodes);
							} else {
								System.out.format("  added anode1 %s, list=%s\n", curr1, inboundsAnodes);
							}

							curr1 = new Pos(curr1.x - deltaX, curr1.y - deltaY);
						}

						Pos curr2 = pos2;
						for (int wave = 0 ; ; wave++) {
							System.out.format("anode2 wave %d %s-%s: freq=%c, edge=%d/%d, delta=%d,%d, node=%s\n",
							 wave, pos1, pos2, frequency, i, j, deltaX, deltaY, curr2);

							if (!inBounds(curr2, width, height)) {
								System.out.format("  DROPPED antinode 2 %s for out-of-bounds\n", curr2);
								break;
							}

							if (!inboundsAnodes.add(curr2)) {
								System.out.format("  NOTADDED %s, already in list=%s\n",
									 curr2, inboundsAnodes);
							} else {
								System.out.format("  added anode2 %s, list=%s\n", curr2, inboundsAnodes);
							}
	
							curr2 = new Pos(curr2.x + deltaX, curr2.y + deltaY);
						}
					}
				}

			}
		}

		List<Pos> sortedList = new ArrayList<Pos>();
		sortedList.addAll(inboundsAnodes);
		Collections.sort(sortedList, new PosSorter());

		System.out.format("Analysis antinode generation complete: part1=%b, %d\n%s\n", part1, inboundsAnodes.size(), inboundsAnodes);
		System.out.format("Analysis antinode generation complete sorted: %d\n%s\n", sortedList.size(), sortedList);
	}
}
