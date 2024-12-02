
import java.io.*;
import java.util.*;

public class Day09 {

	private static boolean part2 = true;

	private static List<Long> parseList(String line) {
                List<Long> ret = new ArrayList<Long>();

                //System.out.format("\t\t\tparsing list %s\n", line);
                StringTokenizer st = new StringTokenizer(line, " ");
                while (st.hasMoreTokens()) {
                        ret.add(Long.parseLong(st.nextToken()));
                }
                return ret;
        }


	private static class History {
		private List<Long> values;

		private History(List<Long> v) {
			values = v;
		}

		public String toString() {
			return String.format("[h: %s]", values);
		}

		public static History parse(BufferedReader br) throws IOException {
			String line = br.readLine();
			if (line == null) 
				return null;

			List<Long> v = parseList(line);
			History h = new History(v);
			return h;
		}

		private List<List<Long>> buildTree() {

			List<List<Long>> tree = new ArrayList<List<Long>>();
			List<Long> curr = values;
			tree.add(curr);

			while (true) {
				List<Long> newList = new ArrayList<Long>();
				boolean allZero = true;

				int listsize = curr.size();
				for (int i = 0 ;  ( i <= listsize - 2) ; i++) {
					long newValue = curr.get(i+1) - curr.get(i);
					allZero &= (newValue == 0);
					newList.add(newValue);
				}

				tree.add(newList);
				curr = newList;

				if (allZero) {
					return tree;
				}
			}
		}

		public long extrapolate() {
			List<List<Long>> tree = buildTree();
			System.out.format("extrapolate: tree:%s\n", tree);

			List<Long> curr = tree.get(tree.size() - 1);

			if (!part2) {
					curr.add(0L);

					for (int depth = tree.size() - 1 ; depth > 0 ; depth--) {
						int lastpos = curr.size() - 1;
						curr = tree.get(depth - 1);
						List<Long> deeper = tree.get(depth);
						long e_value = curr.get(lastpos) + deeper.get(lastpos);
						System.out.format("\textrapolate: depth:%d e_value:%d\n", depth, e_value);
						curr.add(e_value);
					}

					List<Long> top = tree.get(0);
					return top.get(top.size() - 1);	
			} else {
					curr.add(0, 0L);

					for (int depth = tree.size() - 1 ; depth > 0 ; depth--) {
						int pos = 0;
						curr = tree.get(depth - 1);
						List<Long> deeper = tree.get(depth);
						long e_value = curr.get(pos) - deeper.get(pos);
						System.out.format("\textrapolate: depth:%d e_value:%d\n", depth, e_value);
						curr.add(0, e_value);
					}

					List<Long> top = tree.get(0);
					return top.get(0);	
			}
		}
	}

	private static class Game {
		private final List<History> histories;

		private Game(List<History> h) {
			histories = h;
		}

		public String toString() {
			return String.format("[game: h:%s]", histories);
		}

		public long play() {
			long answer = 0;
			for (History h : histories) {
				long e = h.extrapolate();
				answer += e;
				System.out.format("play: e:%d a:%d\n\n", e, answer);
			}
			return answer;
		}

		public static Game parse(BufferedReader br) throws IOException {

			List<History> histories = new ArrayList<History>();	
			History h;	
			while ((h = History.parse(br)) != null) {
				histories.add(h);
			}

			Game game = new Game(histories);
			System.out.format("imported game: %s\n", game);
			return game;
		}
	}

	public static void main(String[] args) throws Exception { 
		long now = System.currentTimeMillis();
		System.out.format("Day Nine, Yo!\n\n");

		//String filename = "input/day09.sample";
		String filename = "input/day09.input";       	
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(file));
		Game game = Game.parse(br);

		long answer = game.play();

		long done = System.currentTimeMillis();
		System.out.format("answer: %d\n", answer);
		System.out.format("execution: %d\n", (done - now));
	}
}
