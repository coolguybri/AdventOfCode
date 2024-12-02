
import java.io.*;
import java.util.*;

public class Day10 {

	private static final boolean part2 = true;

 private static class Pos {
        public final int x;
        public final int y;

        public Pos(int x, int y) {
                this.x = x;
                this.y = y;
        }

        public String toString() {
                return String.format("[%d,%d]", x, y);
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


	private enum Direction {
		North,
		South,
		East,
		West,
	}

	private static class Vector {
		public final Pos pos;
		public final Direction dir;
		
        public String toString() {
                return String.format("[v p=%s, d=%s]", pos, dir);
        }

		public Vector(Pos p, Direction d) {
			pos = p;
			dir = d;
		}
	}

	private static class Game {
		private final List<String> map;
		private List<String> scratchpad;
		private List<List<CountStats>> scratchstat;
		private final Pos startPos;

		private Game(List<String> m, Pos s) {
			map = m;
			startPos = s;
		}

		public String toString() {
			return String.format("[game: s:%s, m:%s]", startPos, map);
		}

		
		private boolean canHandleConnection(Pos p, Direction d) {
			char c = map.get(p.y).charAt(p.x);
			System.out.format("\teval point: %s, d:%s, c:%c\n", p, d, c);
			switch (c) {
			case '|':
				return (d == Direction.North) || (d == Direction.South);
			case '-':
				return (d == Direction.West) || (d == Direction.East);
			case 'L':
				return (d == Direction.North) || (d == Direction.East);
			case 'J':
				return (d == Direction.North) || (d == Direction.West);
			case '7':
				return (d == Direction.South) || (d == Direction.West);
			case 'F':
				return (d == Direction.South) || (d == Direction.East);
			case '.':
				return false;
			case 'S':
			default:
				throw new RuntimeException(String.format("Illegal map entry in pos %s: %c", p, c));
			}
		}

		private List<Vector> findSearchStartPoints(Pos p) {
			List<Vector> ret = new ArrayList<Vector>();
			Pos n = new Pos(p.x, p.y-1);
			Pos s = new Pos(p.x, p.y+1);
			Pos w = new Pos(p.x-1, p.y);
			Pos e = new Pos(p.x+1, p.y);
		
			if ((p.y > 0) && canHandleConnection(n, Direction.South)) {
				ret.add(new Vector(n, Direction.South));
			}
			if ((p.y < (map.size() - 1)) && canHandleConnection(s, Direction.North)) {
				ret.add(new Vector(s, Direction.North));
			}
			if ((p.x > 0) && canHandleConnection(w, Direction.East)) {
				ret.add(new Vector(w, Direction.East));
			}
			if ((p.x < (map.get(0).length() - 1)) && canHandleConnection(e, Direction.West)) {
				ret.add(new Vector(e, Direction.West));
			}

			return ret;
		}

		private List<Vector> followPathLoop(Vector v) {
			Vector currVector = v;
			List<Vector> path = new ArrayList<Vector>();
			while (true) {
					path.add(currVector);
					char c = map.get(currVector.pos.y).charAt(currVector.pos.x);
					System.out.format("\tpath vector: pathlen=%d, %s, c:%c\n", path.size(), currVector, c);

					switch (c) {
					case 'S':
						System.out.format("\tpath vector found END: %s, pathlen=%d\n", currVector, path.size());
						return path;

					case '|':
						if (currVector.dir == Direction.North) 
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y+1), Direction.North);
						else if (currVector.dir == Direction.South)
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y-1), Direction.South);
						else
							throw new RuntimeException(String.format("bad direction %s for |", currVector));
						break;

					case '-':
						if (currVector.dir == Direction.West) 
							currVector = new Vector(new Pos(currVector.pos.x+1, currVector.pos.y), Direction.West);
						else if (currVector.dir == Direction.East)
							currVector = new Vector(new Pos(currVector.pos.x-1, currVector.pos.y), Direction.East);
						else
							throw new RuntimeException(String.format("bad direction %s for -", currVector));
						break;

					case 'L':
						if (currVector.dir == Direction.North) 
							currVector = new Vector(new Pos(currVector.pos.x+1, currVector.pos.y), Direction.West);
						else if (currVector.dir == Direction.East)
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y-1), Direction.South);
						else
							throw new RuntimeException(String.format("bad direction %s for L", currVector));
						break;

					case 'J':
						if (currVector.dir == Direction.North) 
							currVector = new Vector(new Pos(currVector.pos.x-1, currVector.pos.y), Direction.East);
						else if (currVector.dir == Direction.West)
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y-1), Direction.South);
						else
							throw new RuntimeException(String.format("bad direction %s for J", currVector));
						break;

					case '7':
						if (currVector.dir == Direction.South) 
							currVector = new Vector(new Pos(currVector.pos.x-1, currVector.pos.y), Direction.East);
						else if (currVector.dir == Direction.West)
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y+1), Direction.North);
						else
							throw new RuntimeException(String.format("bad direction %s for 7", currVector));
						break;

					case 'F':
						if (currVector.dir == Direction.South) 
							currVector = new Vector(new Pos(currVector.pos.x+1, currVector.pos.y), Direction.West);
						else if (currVector.dir == Direction.East)
							currVector = new Vector(new Pos(currVector.pos.x, currVector.pos.y+1), Direction.North);
						else
							throw new RuntimeException(String.format("bad direction %s for -", currVector));
						break;

					case '.':
					default:
						throw new RuntimeException(String.format("Illegal map entry for %s: %c", currVector, c));
					}
			}	
		}

		private class CountStats {
			public boolean tl;
			public boolean tr;
			public boolean bl;
			public boolean br;
			public boolean visited;
			public boolean pipe;

			public CountStats() {
				tl = tr = bl = br = false;
				visited = false;
				pipe = false;
			}

			public CountStats(boolean xtl, boolean xtr, boolean xbl, boolean xbr) {
				tl = xtl;
				tr = xtr;
				bl = xbl;
				br = xbr;
				visited = false;
				pipe = false;
			}

			public boolean allOutside() {
				return tl && tr && bl && br;
			}
		}

		private void setCell(Pos p, char c) {
			String line = scratchpad.get(p.y);
			String builder = line.substring(0, p.x);
			builder += c;
			builder += line.substring(p.x+1);
			scratchpad.set(p.y, builder);	
		}

		private char getCell(Pos p) {
			return scratchpad.get(p.y).charAt(p.x);
		}

		private CountStats getStat(Pos p) {
			if ((p.y < 0) || (p.y >= scratchstat.size()) || (p.x < 0) || (p.x >= scratchstat.get(0).size()))
				return new CountStats(true, true, true, true);
			return scratchstat.get(p.y).get(p.x);
		}

		private void setStat(Pos p, CountStats cs) {
			List<CountStats> line;
			if (p.y < scratchstat.size()) {
				line = scratchstat.get(p.y);
				if (p.x < line.size()) {
					line.set(p.x, cs);
				}
			}
		}


		private int bleed(Vector v) {

			switch (v.dir) {
				case North:
					return bleedNorth(v.pos, new Pos(v.pos.x, v.pos.y+1));
				case South:
					return bleedSouth(v.pos, new Pos(v.pos.x, v.pos.y-1));
				case West:
					return bleedWest(v.pos, new Pos(v.pos.x+1, v.pos.y));
				case East:
					return bleedEast(v.pos, new Pos(v.pos.x-1, v.pos.y));
			}	

			throw new IllegalStateException("bad direction");
		}

		private int bleedNorth(Pos toPos, Pos fromPos) {
			CountStats from = getStat(fromPos);
			CountStats to = getStat(toPos);
			char toChar = getCell(toPos);
			//System.out.format("\tbleedNorth: from %s, to %s, t=\"%c\"\n", fromPos, toPos, toChar);

			if (toChar == 'J') {
				if (from.tr || from.tl)
					to.tr = to.br = to.bl = true;
			} else if (toChar == 'L') {
				if (from.tr || from.tl) 
					to.tl = to.br = to.bl = true;
			} else if (toChar == '-') {
				if (from.tr || from.tl) 
					to.br = to.bl = true;
			} else if (toChar == '|') {
				if (from.tr)
					to.br = to.tr = true;
				if (from.tl)
					to.bl = to.tl = true;
			} else if (toChar == 'F') {
				if (from.tl)
					to.bl = to.tl = to.tr = true;
				if (from.tr)
					to.br = true;
			} else if (toChar == '7') {
				if (from.tr)
					to.br = to.tr = to.tl = true;
				if (from.tl)
					to.bl = true;
			} else if ((toChar == '.') || (toChar == 'i')) {
				if (from.tr || from.tl)
					to.tl = to.tr = to.br = to.bl = true;
			}

			setStat(toPos, to);
			return 1;
		}

		private int bleedSouth(Pos toPos, Pos fromPos) {
			CountStats from = getStat(fromPos);
			CountStats to = getStat(toPos);
			char toChar = getCell(toPos);
			//System.out.format("\tbleedSouth: from %s, to %s, t=\"%c\"\n", fromPos, toPos, toChar);

			if (toChar == 'J') {
				if (from.br)
					to.tr = to.br = to.bl = true;
				if (from.bl)
					to.tl = true;
			} else if (toChar == 'L') {
				if (from.bl)
					to.tl = to.bl = to.br = true;
				if (from.br)
					to.tr = true;
			} else if (toChar == '-') {
				if (from.br || from.bl) 
					to.tr = to.tl = true;
			} else if (toChar == '|') {
				if (from.br)
					to.br = to.tr = true;
				if (from.bl)
					to.bl = to.tl = true;
			} else if (toChar == 'F') {
				if (from.br || from.bl) 
					to.tl = to.tr = to.bl = true;
			} else if (toChar == '7') {
				if (from.br || from.bl) 
					to.tl = to.tr = to.br = true;
			} else if ((toChar == '.') || (toChar == 'i')) {
				if (from.br || from.bl)
					to.tl = to.tr = to.br = to.bl = true;
			}

			setStat(toPos, to);
			return 1;
		}

		private int bleedWest(Pos toPos, Pos fromPos) {
			CountStats from = getStat(fromPos);
			CountStats to = getStat(toPos);
			char toChar = getCell(toPos);
			//System.out.format("\tbleedWest: from %s, to %s, t=\"%c\"\n", fromPos, toPos, toChar);

			if (toChar == 'J') {
				if (from.tl || from.bl)
					to.tr = to.br = to.bl = true;
			} else if (toChar == 'L') {
				if (from.bl)
					to.bl = to.br = to.tl = true;
				if (from.tl)
					to.tr = true;
			} else if (toChar == '-') {
				if (from.tl)
					to.tl = to.tr = true;
				if (from.bl)
					to.bl = to.br = true;
			} else if (toChar == '|') {
				if (from.tl | from.bl)
					to.tr = to.br = true;
			} else if (toChar == 'F') {
				if (from.tl)
					to.bl = to.tl = to.tr = true;
				if (from.bl)
					to.br = true;
			} else if (toChar == '7') {
				if (from.tl || from.bl)
					to.tr = to.br = to.tl = true;
			} else if ((toChar == '.') || (toChar == 'i')) {
				if (from.tl | from.bl)
					to.tl = to.bl = to.tr = to.br = true;
			}

			setStat(toPos, to);
			return 1;
		}

		private int bleedEast(Pos toPos, Pos fromPos) {
			CountStats from = getStat(fromPos);
			CountStats to = getStat(toPos);
			char toChar = getCell(toPos);
			//System.out.format("\tbleedEast: from %s, to %s, t=\"%c\"\n", fromPos, toPos, toChar);

			if (toChar == 'J') {
				if (from.tr)
					to.tl = true;
				if (from.br)
					to.bl = to.br = to.tr = true;
			} else if (toChar == 'L') {
				if (from.tr | from.br)
					to.tl = to.bl = to.br = true;
			} else if (toChar == '-') {
				if (from.tr)
					to.tl = to.tr = true;
				if (from.br)
					to.bl = to.br = true;
			} else if (toChar == '|') {
				if (from.tr | from.br)
					to.tl = to.bl = true;
			} else if (toChar == 'F') {
				if (from.tr | from.br)
					to.tl = to.bl = to.tr = true;
			} else if (toChar == '7') {
				if (from.tr)
					to.br = to.tr = to.tl = true;
				if (from.br)
					to.bl = true;
			} else if ((toChar == '.') || (toChar == 'i')) {
				if (from.tr | from.br)
					to.tl = to.bl = to.tr = to.br = true;
			}

			setStat(toPos, to);
			return 1;
		}

		private void printScratchStat(String label) {
			if (label.length() > 0 ) {
				System.out.format("%s:\n", label);
			}
			for (List<CountStats> line : scratchstat) {
				System.out.format("|");
				for (CountStats cs : line) {
					System.out.format("%c:%d:%d:%d:%d|", cs.visited ? '*' : ' ', cs.tl ? 1 : 0, cs.tr ? 1 : 0, cs.bl ? 1 : 0, cs.br ? 1 : 0);
				}
				System.out.format("\n");
			}	
		}

		private void printScratchEx() {
			for (List<CountStats> line : scratchstat) {
				for (CountStats cs : line) {
					char ctl = cs.tl ? ' ' : '.';
					char ctr = cs.tr ? ' ' : '.';
					if (cs.pipe) {
						ctl = (ctl == ' ') ? 'X' : 'O';
						ctr = (ctl == ' ') ? 'X' : 'O';
					}
					System.out.format("%c%c", ctl, ctr);
				}
				System.out.format("\n");
				for (CountStats cs : line) {
					//char cbl = cs.bl ? 'x' : 'o';
					//char cbr = cs.br ? 'x' : 'o';
					char cbl = cs.bl ? ' ' : '.';
					char cbr = cs.br ? ' ' : '.';
					if (cs.pipe) {
						//cbl = Character.toUpperCase(cbl);
						//cbr = Character.toUpperCase(cbr);
						cbl = (cbl == ' ') ? 'X' : 'O';
						cbr = (cbl == ' ') ? 'X' : 'O';
					}
					System.out.format("%c%c", cbl, cbr);
				}
				System.out.format("\n");
			}	
		}

		private void printScratchpad(String label) {
			System.out.format("%s:\n", label);
			for (String line : scratchpad) {
				System.out.format("%s\n", line);
			}	
		}


		public long getInternalCount(List<Vector> path) {
			long answer = 0;

			// init scratchstat table
			scratchstat = new ArrayList<List<CountStats>>();
			for (int i = 0 ; i < map.size() ; i++) {
				List<CountStats> cs = new ArrayList<CountStats>();
				scratchstat.add(cs);	
				for (int j = 0 ; j < map.get(0).length() ; j++) {
					cs.add(new CountStats());
				}
			}

			// init scratchpad table, temporarily set all pipes to 'X'.
			scratchpad = new ArrayList<String>(map);
			for (Vector v : path) {
				setCell(v.pos, 'X');
			}
			printScratchpad("path discovered");

			// Set every non-pipe to "i" state, restore path back to pipe symbols.
			for (int y = 0 ; y < scratchpad.size() ; y++) {
				String line = scratchpad.get(y);
				for (int x = 0 ; x < line.length() ; x++) {
					Pos p = new Pos(x, y);
					char c = getCell(p);
					CountStats cs = getStat(p);
					if (c != 'X') {
						setCell(p, 'i');
					} else {
						cs.pipe = true;
						setStat(p, cs);
					}
				}
			}
			for (Vector v : path) {
				char c = map.get(v.pos.y).charAt(v.pos.x);
				if (c == 'S')
					c = '|';
				setCell(v.pos, c);
			}
			printScratchpad("search state init");

			// create queue.
			Queue<Vector> q = new LinkedList<Vector>();

			// Queue up bottom row.
			int bottomLineNum = scratchpad.size() - 1;
			String bottomLine = scratchpad.get(bottomLineNum);
			for (int x = 0 ; x < bottomLine.length() ; x++) {
			//for (int x = 0 ; x < 1 ; x++) {
				q.add(new Vector(new Pos(x, bottomLineNum), Direction.North));	
				q.add(new Vector(new Pos(x, 0), Direction.South));	
			}
			for (int y = 0 ; y < scratchpad.size() ; y++) {
				q.add(new Vector(new Pos(0, y), Direction.East));	
				q.add(new Vector(new Pos(bottomLine.length() - 1, y), Direction.West));	
			}

			int 	maxiter = 0;
			int iter = 0;	
			Vector v;
			while (((v = q.poll()) != null) && ((maxiter <= 0) || (iter < maxiter))) {
				iter++;
		     	//printScratchpad(String.format("pre bleed loop, iter=%d, size=%d", iter, q.size()));
			    //printScratchStat("");
				bleed(v);

					CountStats from = getStat(v.pos);
					if (from.visited) {
						//System.out.format("\tbleed: no search for %s: visited:\n", v.pos);
						continue;
					}

				from.visited = true;
				setStat(v.pos, from);

				// look at neighbors.
				if ((v.dir != Direction.South) && (v.pos.y > 0)) {
					Vector newvec = new Vector(new Pos(v.pos.x, v.pos.y-1), Direction.North);
					q.add(newvec);
					//System.out.format("\tbleed-search: adding north %s, size=%d\n", newvec.pos, q.size());
				}
				if ((v.dir != Direction.North) && (v.pos.y < (map.size() - 1))) {
					Vector newvec = new Vector(new Pos(v.pos.x, v.pos.y+1), Direction.South);
					q.add(newvec);
					//System.out.format("\tbleed-search: adding south %s, size=%d`\n", newvec.pos, q.size());
				}
				if ((v.dir != Direction.East) && (v.pos.x > 0)) {
					Vector newvec = new Vector(new Pos(v.pos.x-1, v.pos.y), Direction.West);
					q.add(newvec);
					//System.out.format("\tbleed-search: adding west %s, size=%d\n", newvec.pos, q.size());
				}
				if ((v.dir != Direction.West) && (v.pos.x < (map.get(0).length() - 1))) {
					Vector newvec = new Vector(new Pos(v.pos.x+1, v.pos.y), Direction.East);
					q.add(newvec);
					//System.out.format("\tbleed-search: adding east %s, size=%d\n", newvec.pos, q.size());
				}

				if ((iter % 1000) == 0) {
					printScratchpad(String.format("post bleed loop, iter=%d, size=%d", iter, q.size()));
					printScratchStat("");
				}
			}

			long totalInside = 0;
			for (int y = 0 ; y < scratchpad.size() ; y++) {
				String line = scratchpad.get(y);
				for (int x = 0 ; x < line.length() ; x++) {
					Pos p = new Pos(x, y);
					char c = getCell(p);
					CountStats cs = getStat(p);
					if (cs.allOutside())
						setCell(p, 'e');
					else if (c == 'i')
						totalInside++;
				}
			}

			printScratchpad(String.format("complete, iter=%d, size=%d, inside=%d", iter, q.size(), totalInside));
			printScratchStat("");
			printScratchEx();
			return totalInside;	
		}

		public long play() {
			long answer = 0;

			List<Vector> searchStartPoints = findSearchStartPoints(startPos);
			System.out.format("start points: %s\n", searchStartPoints);

			List<Vector> bestpath = null;
			for (Vector v : searchStartPoints) {
				List<Vector> path = followPathLoop(v);
				if ((bestpath == null) || (path.size() < bestpath.size())) {
					bestpath = path;
				}
				System.out.format("found path: %d\n", path.size());
			}

			if (!part2) {
				answer = bestpath.size() / 2;
			} else {
				answer = getInternalCount(bestpath);
			}

			return answer;
		}

		public static Game parse(BufferedReader br) throws IOException {

			List<String> m = new ArrayList<String>();
			Pos startPos = null;

			String line;
			int lineNo = 0;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				m.add(line);

				int sPos = line.indexOf('S');
				if (sPos >= 0) {
					if (startPos != null)
						throw new IOException("Multple sdtart points");
					startPos = new Pos(sPos, lineNo);
				}

				lineNo++;
			}

			Game game = new Game(m, startPos);
			System.out.format("imported game: %s\n", game);
			for (String l : m) {
					System.out.format("%s\n", l);
			}
		
			
			return game;
		}
	}

	public static void main(String[] args) throws Exception { 
		long now = System.currentTimeMillis();
		System.out.format("Day Ten, Yo!\n\n");

		//String filename = "input/day10.sample7";
		String filename = "input/day10.input";       	
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(file));
		Game game = Game.parse(br);

		long answer = game.play();

		long done = System.currentTimeMillis();
		System.out.format("answer: %d\n", answer);
		System.out.format("execution: %d\n", (done - now));
	}
}
