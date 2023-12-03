
import java.io.*;
import java.util.*;

public class Day03 {

  private static class Pos {
	public final int x;
	public final int y;

	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public String toString() {
		return String.format("[pos x=%d, y=%d]", x, y);
	}

	@Override
	public int hashCode()
	{
	    return x * y;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Pos))
			return false;
		Pos otherPos = (Pos) other;
		return (this.x == otherPos.x) && (this.y == otherPos.y);
	}
}


	// Initial state.
	private static List<String> schematic = new ArrayList<String>();
	private static int score = 0;
	private static Map<Pos, List<Integer>> gears = new HashMap<Pos, List<Integer>>();;


   private static boolean part2 = false;

   private static boolean isSymbol(char c) {
	if (part2) {
		return (c == '*');
	} else {
		return !Character.isDigit(c) && (c != '.');
	}
   }

   private static int ScorePotentialPartNumber(String line, int lineno, int firstpos, int lastpos) {
	String substring = line.substring(firstpos, lastpos);
	int partNumber = Integer.parseInt(substring);
	//System.out.format("\tscoring partNumber=%d, first=%d, last=%d\n", partNumber, firstpos, lastpos);
	// Now see if there are any symbols next to it.
	boolean found = false;

	// Look left.
	if (firstpos > 0) {
		//System.out.format("\t\tanalyzing left %d,%d\n", firstpos - 1, lineno);
		if (isSymbol(schematic.get(lineno).charAt(firstpos - 1))) {
			found = true;
			Pos pos = new Pos(firstpos -1, lineno);
			List<Integer> existing = gears.get(pos);
			if (existing == null) {
				existing = new ArrayList<Integer>();
				gears.put(pos, existing);
			}
			existing.add(partNumber);
		}
	}

	// Look right.
	if (lastpos < (line.length() - 1)) {
		//System.out.format("\t\tanalyzing right %d,%d\n", lastpos, lineno);
		if (isSymbol(schematic.get(lineno).charAt(lastpos))) {
			found = true;
			Pos pos = new Pos(lastpos, lineno);
			List<Integer> existing = gears.get(pos);
			if (existing == null) {
				existing = new ArrayList<Integer>();
				gears.put(pos, existing);
			}
			existing.add(partNumber);
		}
	}

	// Now look at the row above, added 1 to each side.
	if (lineno > 0) {
		int firstscan = firstpos - 1;
		if (firstscan < 0)
			firstscan = 0;
		int endscan = lastpos + 1;
		if (endscan > line.length())
			endscan = line.length();
		for (int j = firstscan ; j < endscan ; j++) {
			//System.out.format("\t\tanlyzing above %d,%d\n", j, lineno - 1);
			if (isSymbol(schematic.get(lineno - 1).charAt(j))) {
				found = true;
				Pos pos = new Pos(j, lineno - 1);
				List<Integer> existing = gears.get(pos);
				if (existing == null) {
					existing = new ArrayList<Integer>();
					gears.put(pos, existing);
				}
				existing.add(partNumber);
			}
		}
	}

	// Now look at the row below, added 1 to each side.
	if (lineno < (schematic.size() - 1)) {
		int firstscan = firstpos - 1;
		if (firstscan < 0)
			firstscan = 0;
		int endscan = lastpos + 1;
		if (endscan > line.length())
			endscan = line.length();
		for (int j = firstscan ; j < endscan ; j++) {
			//System.out.format("\t\tanlyzing below %d,%d\n", j, lineno + 1);
			if (isSymbol(schematic.get(lineno + 1).charAt(j))) {
				found = true;
				Pos pos = new Pos(j, lineno + 1);
				List<Integer> existing = gears.get(pos);
				if (existing == null) {
					existing = new ArrayList<Integer>();
					gears.put(pos, existing);
				}
				existing.add(partNumber);
			}
		}
	}

	if (!found) {
		System.out.format("\t\tfinished for partNumber=%d, no symbol found, dropping\n", partNumber);
		return 0;
	}

	System.out.format("\t\t%d, partNumber good\n", partNumber);
	return partNumber;
    }



    public static void main(String[] args) throws Exception { 
    	System.out.format("Day Three, Yo!\n\n");


        String filename = "day03.sample";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        for (String line ; (line = br.readLine()) != null ; ) {
		line = line.trim();
		//System.out.format("\timporting linelen %d\n", line.length());
		schematic.add(line);
	}
	System.out.format("\nImport complete for %s:, lines=%d\n\n", filename, schematic.size());

	int lineno = -1;
	for (String line : schematic) {
		lineno++;
		//System.out.format("Line Start %d: %s\n", lineno, line);

		boolean closed = true;
		int firstpos = -1;
		int lastpos = -1;
		for (int i = 0 ; i < line.length() ; i++) {
			if (Character.isDigit(line.charAt(i))) {
				if (closed) {
					firstpos = i;
					closed = false;
				}
			} else {
				if (!closed) {
					lastpos = i;
					score += ScorePotentialPartNumber(line, lineno, firstpos, lastpos);
					closed = true;
				}
			}
		}
		if (!closed) {
			lastpos = line.length();
			score += ScorePotentialPartNumber(line, lineno, firstpos, lastpos);
		}

	}
	System.out.format("\nAnalysis-part1 complete: score=%d\n\n", score);

	int sum_ratio = 0;
//	System.out.format("\nAnalysis-part2 gears: %s\n", gears.toString());
	Iterator<Map.Entry<Pos, List<Integer>>> mapiter = gears.entrySet().iterator();
	    while (mapiter.hasNext()) {
		Map.Entry<Pos, List<Integer>> entry = mapiter.next();
		Pos key = entry.getKey();
		List<Integer> value = entry.getValue();
		//System.out.println("Key=" + key + ", Value=" + value);

		if (value.size() == 2) {
			int node1 = value.get(0);
			int node2 = value.get(1);	
			int ratio = node1 * node2;
			sum_ratio += ratio;
			System.out.format(" YO: found gear-node at %s: %d, %d = %d, [%d]\n",
				key, node1, node2, ratio, sum_ratio);
		}
	    }
	System.out.format("\nAnalysis-part2 complete: %d\n", sum_ratio);
    }
}
