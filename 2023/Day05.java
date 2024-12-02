
import java.io.*;
import java.util.*;

public class Day05 {

	private static List<Long> parseList(String line) {
		List<Long> ret = new ArrayList<Long>();

		//System.out.format("\t\t\tparsing list %s\n", line);
		StringTokenizer st = new StringTokenizer(line, " ");
		while (st.hasMoreTokens()) {
			ret.add(Long.parseLong(st.nextToken()));
		}	
		return ret;
	}


	private static class MapRecord {

		public final long target_start;
		public final long source_start;
		public final long range_len;

		private MapRecord(long ts, long ss, long rl) {
			target_start = ts;
			source_start = ss;
			range_len = rl;
		}

		public String toString() {
			return String.format("[maprec: ts=%d, ss=%d, len=%d",
				 target_start, source_start, range_len);
		}

		// 52 50 48
		public static MapRecord parse(String line) {
			StringTokenizer st = new StringTokenizer(line.trim(), " ");
			long ts = Long.parseLong(st.nextToken());
			long ss = Long.parseLong(st.nextToken());
			long rl = Long.parseLong(st.nextToken());

			//System.out.format("\t\t\tparsing list %s\n", line);
			MapRecord mr = new MapRecord(ts, ss, rl);
			//System.out.format("\t\timported: %s\n", mr);
			return mr;
		}
	}


	// seed-to-soil map:
	// 50 98 2
	// 52 50 48
	private static class ResourceMapper {

		public final String name;
		public final List<MapRecord> recs;

		private ResourceMapper(String n, List<MapRecord> r) {
			name = n;
			recs = r;
		}

		public String toString() {
			return String.format("[mapper %s recs=%s", name, recs);
		}

		public long evaluate(long value) {
			//System.out.format("\t\tmapper %s evaluate: %d\n", name, value);
			for (MapRecord mp : recs) {
				if ((value >= mp.source_start) && (value < (mp.source_start + mp.range_len))) {
					long target = mp.target_start + (value - mp.source_start);
					//System.out.format("\t\t\tmapper %s match: %d -> %d\n", name, value, target);
					return target;
				}
			}

			// No mapping existing, so go with default.
			//System.out.format("\t\t\tmapper %s default: %d\n", name, value);
			return value;
		}

		public static ResourceMapper parse(BufferedReader br) throws Exception {
			// Read name.
			String name = br.readLine();
			if (name == null) {
				// hit eof
				return null;
			}

			String line;
			List<MapRecord> recs = new ArrayList<MapRecord>();
			while ((line = br.readLine()) != null) {
				// break out in the first blank line.
				line = line.trim();
				if (line.isEmpty())
					break;

				recs.add(MapRecord.parse(line));	
			}

			ResourceMapper rm = new ResourceMapper(name, recs);
			System.out.format("\t\timported: %s\n", rm);
			return rm;
		}
	}

	private static class MapPipeline {
		protected List<ResourceMapper> mapList;

		private MapPipeline(List<ResourceMapper> ml) {
			mapList = ml;
		}

		public String toString() {
			return String.format("[pipeline: %s", mapList);
		}

		public long evaluate(long value) {
			//System.out.format("\t\tpipeline: evaluate: %d\n", value);
			for (ResourceMapper rm : mapList) {
				value = rm.evaluate(value);
			}
			return value;
		}

		public static MapPipeline parse(BufferedReader br) throws Exception {
			List<ResourceMapper> ml = new ArrayList<ResourceMapper>();

			ResourceMapper rm;
			while ((rm = ResourceMapper.parse(br)) != null) {
				ml.add(rm);
			}

			MapPipeline mp = new MapPipeline(ml);
			System.out.format("\t\timported: %s\n", mp);
			return mp;
		}
	}


	// Initial state.
	//private static List<Game> games = new ArrayList<Game>();


    public static void main(String[] args) throws Exception { 
    	System.out.format("Day Five, Yo!\n\n");

        //String filename = "input/day05.sample";
        String filename = "input/day05.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
	
	// Read in seed list and blank line that follows.
	// seeds: 79 14 55 13
	List<Long> seeds = parseList(br.readLine().substring(6));
	br.readLine();

	// Setup pipeline.
	MapPipeline pipeline = MapPipeline.parse(br);

	// Lowest location.
	long lowestLocation = 0;

	long now = System.currentTimeMillis();

	boolean part2 = true;
	if (!part2) {
		for (Long seed : seeds) {
			long mappedValue = pipeline.evaluate(seed);
			System.out.format("evaluated: %d -> %d\n", seed, mappedValue);
			if ((lowestLocation == 0) || (mappedValue < lowestLocation)) {
				lowestLocation = mappedValue;
			}
		}	
	} else {
		for (int i = 0 ; i < seeds.size() ; i+=2) {
			long seedStart = seeds.get(i);
			long seedRange = seeds.get(i+1);
			long seedEnd = seedStart + seedRange;
			for (long j = seedStart ; j < seedEnd ; j++) {
				long mappedValue = pipeline.evaluate(j);
				if ((j % 1000000) == 0) {
					System.out.format("evaluated (%d/%d) (%d/%d): %d -> %d\n",
						 i, seeds.size(), (j - seedStart), seedRange, j, mappedValue);
				}
				if ((lowestLocation == 0) || (mappedValue < lowestLocation)) {
					lowestLocation = mappedValue;
				}
			}
		}
	}

	long done = System.currentTimeMillis();
	System.out.format("lowestLocation: %d\n", lowestLocation);
	System.out.format("execution: %d\n", (done - now));
    }
}
