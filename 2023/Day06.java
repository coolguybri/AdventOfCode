
import java.io.*;
import java.util.*;

public class Day06 {

	private static boolean part2 = true;

	private static List<Long> parseList(String line) {
		List<Long> ret = new ArrayList<Long>();
		//System.out.format("\t\t\tparsing list %s\n", line);
		StringTokenizer st = new StringTokenizer(line, " ");
		while (st.hasMoreTokens()) {
			ret.add(Long.parseLong(st.nextToken().trim()));
		}	
		System.out.format("\t\t\timported list %s\n", ret);
		return ret;
	}

	protected static class RaceSet {
		public final List<Long> time_limits;
		public final List<Long> best_distance;

		private RaceSet(List<Long> tl, List<Long> bd) {
			time_limits = tl;
			best_distance = bd;
		}

		public long size() {
			return time_limits.size();
		}

		public long getTimeLimit(int raceno) {
			return time_limits.get(raceno);
		}

		public long getBestDistance(int raceno) {
			return best_distance.get(raceno);
		}

		// Time:      7  15   30
		// Distance:  9  40  200	
		public static RaceSet parse(BufferedReader br) throws IOException {
			String line;
			List<Long> tl;
			List<Long> bd;

			if (!part2) {
					line = br.readLine();
					tl = parseList(line.substring(line.indexOf(':') + 1));
					line = br.readLine();
					bd = parseList(line.substring(line.indexOf(':') + 1));
			} else {
					line = br.readLine();
		System.out.format("\t\t\tparsing line %s\n", line);
					line = line.replace(" ", "");
		System.out.format("\t\t\tparsing line %s\n", line);
					tl = parseList(line.substring(line.indexOf(':') + 1));
					line = br.readLine();
					line = line.replace(" ", "");
					bd = parseList(line.substring(line.indexOf(':') + 1));
			}

			if (tl.size() != bd.size())
				throw new IOException("bullshit: lists should be same size");
			return new RaceSet(tl, bd);
		}
	}

	protected static class RaceEngine {

		public static long runRaceSet(RaceSet rs) {
			long accumulator = 1;

			for (int raceno = 0 ; raceno < rs.size() ; raceno++) {
				long numWins = runRace(raceno, rs.getTimeLimit(raceno), rs.getBestDistance(raceno));
				if (numWins > 0) {
					accumulator *= numWins;
					System.out.format("RaceEngine: RACE (%d): wins=%d, accum=%d\n", raceno, numWins, accumulator);
				}
			}

			return accumulator;
		}
	
		protected static long runRace(int raceno, long time_limit, long best_distance) {
			long num_steps = time_limit - 1;
			long num_winners = 0;

			long debugiter = 0;
			for (long step = 1 ; step < num_steps ; step++) {
				long distance = ((time_limit - step) * step);
				if (distance > best_distance) {
					debugiter++;
					if ((debugiter % 100000) == 0)
							System.out.format("RaceEngine:runRace: (%d) (%d/%d) found BETTER distance:%d -> %d\n",
								 raceno, step, num_steps, best_distance, distance);
					num_winners++;
				}
			}

			return num_winners;
		}
	}


    public static void main(String[] args) throws Exception { 
    	System.out.format("Day Six, Yo!\n\n");
		long now = System.currentTimeMillis();

        //String filename = "input/day06.sample";
        String filename = "input/day06.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));

		RaceSet races = RaceSet.parse(br);
		long answer = RaceEngine.runRaceSet(races);

		long done = System.currentTimeMillis();
		System.out.format("answer: %d\n", answer);
		System.out.format("execution: %d\n", (done - now));
    }
}
