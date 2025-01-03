
import java.io.*;
import java.util.*;

public abstract class FileParser {
	protected final String 			m_filename;
	protected final File 			m_file;
	protected final BufferedReader 	m_reader;

	protected String 				m_line;
	protected int 					m_lineNum;
	protected int					m_currPos;
	protected boolean				m_skipBlank;

	public FileParser(int dayNum, boolean sample) throws IOException {
		m_filename = String.format("input/day%02d.%s", dayNum, sample ? "sample" : "input");
		System.out.format("Day %d, reading from file %s...\n\n", dayNum, m_filename);
		m_file = new File(m_filename);
		m_reader = new BufferedReader(new FileReader(m_file));

		m_line = null;
		m_lineNum = 0;
		m_currPos = 0;
		m_skipBlank = true;
	}

	public InternalReaderIterator getFileIterator() {
		return new InternalReaderIterator();
	}

	protected class InternalReaderIterator implements Iterator<String> {
		private boolean		m_nextCached;

		public InternalReaderIterator() {
			m_nextCached = false;
		}

		private void pullCache() {
			// Read in one good line; null means EOF.
			while (true) {
				try {
					m_line = m_reader.readLine();
				} catch (Exception e) {
					System.out.format("ERROR execption reading %s with %s\n\n",
						 m_filename, e.toString());
					m_line = null;
				}

				if (m_line == null) {
					System.out.format("breaking out with null line on %d\n", m_lineNum);
					break;
				}

				m_lineNum++;
				m_currPos = 0;
				m_line = m_line.trim();

				// Skip blank lines.
				if (!m_skipBlank || (m_line.length() > 0)) {
					//System.out.format("breaking out with line len=%d, skip=%b on %d\n", m_line.length(), m_skipBlank, m_lineNum);
					break;
				}
			}

			m_nextCached = true;
		}

		public boolean hasNext() {
			if (!m_nextCached)
				pullCache();

			return (m_line != null); 
		}

		public String next() {
			if (!m_nextCached)
				pullCache();

			m_nextCached = false;
			return m_line;
		}

		public Integer getNextInteger() {
			int slen = m_line.length();

			// Skip all whitespace before the intger. 
			for (; m_currPos < slen ; m_currPos++) {
				char c = m_line.charAt(m_currPos);
				if (Character.isDigit(c)) {
					break;
				}
			}

			int startIntPos = m_currPos;
			for (; m_currPos < slen ; m_currPos++) {
				char c = m_line.charAt(m_currPos);
				if (!Character.isDigit(c)) {
					break;
				}
			}
			int endIntPos = m_currPos;

			if (endIntPos <= startIntPos) {
				//throw new IllegalArgumentException(
				//	String.format("next integer not found in string \"%s\" from pos %d", m_line, m_currPos));
				return null;
			}

			return Integer.parseInt(m_line.substring(startIntPos, endIntPos));
		}

		public Long getNextLong() {
			int slen = m_line.length();

			// Skip all whitespace before the intger. 
			for (; m_currPos < slen ; m_currPos++) {
				char c = m_line.charAt(m_currPos);
				if (Character.isDigit(c)) {
					break;
				}
			}

			int startIntPos = m_currPos;
			for (; m_currPos < slen ; m_currPos++) {
				char c = m_line.charAt(m_currPos);
				if (!Character.isDigit(c)) {
					break;
				}
			}
			int endIntPos = m_currPos;

			if (endIntPos <= startIntPos) {
				//throw new IllegalArgumentException(
				//	String.format("next integer not found in string \"%s\" from pos %d", m_line, m_currPos));
				return null;
			}

			return Long.parseLong(m_line.substring(startIntPos, endIntPos));
		}
	}


	/*
	*/
	public static class DoubleIntegerListParser extends FileParser {
        	protected final List<Integer> m_list1;
        	protected final List<Integer> m_list2;
 
        	public DoubleIntegerListParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list1 = new ArrayList<Integer>();
			m_list2 = new ArrayList<Integer>();
		}

		public List<Integer> getList1() { return m_list1; }
		public List<Integer> getList2() { return m_list2; }

		public void parse() throws IOException {
			int lineNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				int firstInt = iter.getNextInteger();
				m_list1.add(firstInt);

				int secondInt = iter.getNextInteger();
				m_list2.add(secondInt);

				System.out.format("line %d ints=%d,%d\n",
					lineNum, firstInt, secondInt);
			}
		}
	}


	/*
	*/
	public static class ListLongListParser extends FileParser {
        	protected final List<List<Long>> m_list;

   		public ListLongListParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<List<Long>>();
		}

		public List<List<Long>> getList() { return m_list; }

		public void parse() throws IOException {
			int lineNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				List<Long> builder = new ArrayList<Long>();

				Long nextInt = null;
				while ((nextInt = iter.getNextLong()) != null) {
					builder.add(nextInt);
				}

				m_list.add(builder);
				System.out.format("line %d ints=%s\n", lineNum, builder);
			}
			System.out.format("finished parsing %d lines\n\n", lineNum);
		}
	}

	/*
	*/
	public static class ListIntegerListParser extends FileParser {
        	protected final List<List<Integer>> m_list;

   		public ListIntegerListParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<List<Integer>>();
		}

		public List<List<Integer>> getList() { return m_list; }

		public void parse() throws IOException {
			int lineNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				List<Integer> builder = new ArrayList<Integer>();

				Integer nextInt = null;
				while ((nextInt = iter.getNextInteger()) != null) {
					builder.add(nextInt);
				}

				m_list.add(builder);
				System.out.format("line %d ints=%s\n", lineNum, builder);
			}
			System.out.format("finished parsing %d lines\n\n", lineNum);
		}
	}


	/*
	*/
	 public static class LineParser extends FileParser {
		protected final List<String> m_list;
 
		public LineParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<String>();
		}

		public List<String> getList() { return m_list; }

		public void parse() throws IOException {
			int lineNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				m_list.add(line);
				System.out.format("line %02d: %s\n", lineNum, line);
			}
			System.out.format("finished reading %d lines\n\n", lineNum);
		}
	}

	/*
        */	
 	public static class Pos {
                public final int x;
                public final int y;
                public Pos(int x, int y) {
                        this.x = x;
                        this.y = y;
                }

		@Override
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

		public Pos add(Pos adder) {
			return new Pos(adder.x + this.x, adder.y + this.y);
		}
        }

	/*
	*/
	public static class ClawParser extends FileParser {

		private static int nextid = 0;
	
		public static class ClawMachine {
			public final int id;
			public final Pos a;
			public final Pos b;
			public final Pos prize;	

			public ClawMachine(Pos a, Pos b, Pos p) {
				this.id = nextid++;
				this.a = a;
				this.b = b;
				this.prize = p;
			}

			@Override
			public String toString() {
				return String.format("[claw [%03d] a=%s, b=%s, prize=%s]", id, a, b, prize);
			}
		}

		protected final List<ClawMachine> m_list;
 
		public ClawParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<ClawMachine>();
		}

		public List<ClawMachine> getList() { return m_list; }

		// Button A: X+94, Y+34
		// Button B: X+22, Y+67
		// Prize: X=8400, Y=5400
		private Pos parseLine(String line) {
			int fbpos = line.indexOf('X') + 2;
			int fepos = line.indexOf(',');
			int x = Integer.parseInt(line.substring(fbpos, fepos));
			int sbpos = line.indexOf('Y') + 2;
			int y = Integer.parseInt(line.substring(sbpos));
			return new Pos(x, y);
		}

		public void parse() throws IOException {
			int recNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; recNum++) {
				Pos a = parseLine(iter.next());	
				Pos b = parseLine(iter.next());	
				Pos prize = parseLine(iter.next());
				ClawMachine m = new ClawMachine(a, b, prize);

				System.out.format("machine %02d: %s\n", recNum, m);
				m_list.add(m);
			}
			System.out.format("finished reading %d records\n\n", recNum);
		}
	}

	/*
	*/
	public static class RobotParser extends FileParser {

		private static int nextid = 0;
	
		public static class Robot {
			public final int id;
			public final Pos initial;
			public final Pos velocity;
			public Pos currPos;

			public Robot(Pos i, Pos v) {
				this.id = nextid++;
				this.initial = i;
				this.velocity = v;
				this.currPos = this.initial;
			}

			@Override
			public String toString() {
				return String.format("[robot [%03d] init=%s, v=%s, curr=%s]", id, initial, velocity, currPos);
			}
		}

		protected final List<Robot> m_list;
 
		public RobotParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<Robot>();
		}

		public List<Robot> getList() { return m_list; }

		// p=14,11 v=-25,-54
		public void parse() throws IOException {
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; ) {
				String line = iter.next();
				int fbpos = line.indexOf('=') + 1;
				int fepos = line.indexOf(',', fbpos);
				int xi = Integer.parseInt(line.substring(fbpos, fepos));
				int sepos = line.indexOf(' ', fepos);
				int yi = Integer.parseInt(line.substring(fepos+1, sepos));
				Pos init = new Pos(xi, yi);

				int fbpos2 = line.indexOf('=', sepos) + 1;
				int fepos2 = line.indexOf(',', fbpos2);
				int xv = Integer.parseInt(line.substring(fbpos2, fepos2));
				int yv = Integer.parseInt(line.substring(fepos2+1));
				Pos velocity = new Pos(xv, yv);

				Robot r = new Robot(init, velocity);
				System.out.format("robot imported: %s\n", r);
				m_list.add(r);
			}
			System.out.format("finished reading %d records\n\n", m_list.size());
		}
	}


	/*
	*/
	public static class MatrixParser extends LineParser {
 
		public MatrixParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
		}

		public char getCell(int x, int y) { return m_list.get(y).charAt(x); }
		public char getCell(Pos pos) { return getCell(pos.x, pos.y); }
		public int getHeight() { return m_list.size(); }
		public int getWidth() { return m_list.get(0).length(); }	
	}

	/*
	*/
	public static class RobotPathParser extends FileParser {
		protected final List<char[]> 	m_map;
		protected final boolean 	m_expand;
		protected String 		m_instructions;
 
		public RobotPathParser(int dayNum, boolean sample, boolean expand) throws IOException {
			super(dayNum, sample);
			m_skipBlank = false;
			m_map = new ArrayList<char[]>();
			m_instructions = "";
			m_expand = expand;
		}

		public void parse() throws IOException {
			int lineNum = 0;
			InternalReaderIterator iter = getFileIterator();
			for ( ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				if (line.length() == 0) {
					break;
				}
				System.out.format("map %02d: %s\n", lineNum, line);

				if (!m_expand) {
					m_map.add(line.toCharArray());
				} else {
					var clist = new char[line.length() * 2];
					for (int p = 0 ; p < line.length() ; p++) {
						char pc = line.charAt(p);
						switch (pc) {
						case '.':
							clist[p*2] = '.';
							clist[p*2+1] = '.';
							break;
						case 'O':
							clist[p*2] = '[';
							clist[p*2+1] = ']';
							break;
						case '#':
							clist[p*2] = '#';
							clist[p*2+1] = '#';
							break;
						case '@':
							clist[p*2] = '@';
							clist[p*2+1] = '.';
							break;
						}
		
					}
					m_map.add(clist);
				}
			}
			System.out.format("finished reading %d map lines\n\n", lineNum);

			for ( ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				m_instructions += line;
				System.out.format("instr %02d: tot=%d, new=%d (%s)\n",
					 lineNum, m_instructions.length(), line.length(), line);
			}
			System.out.format("finished reading %d instructions\n\n", m_instructions.length());
		}

		public char getCell(Pos p) { return m_map.get(p.y)[p.x]; }
		public char getCell(int x, int y) { return m_map.get(y)[x]; }
		public void setCell(Pos p, char c) { m_map.get(p.y)[p.x] = c; }
		public void setCell(int x, int y, char c) { m_map.get(y)[x] = c; }
		public int getHeight() { return m_map.size(); }
		public int getWidth() { return m_map.get(0).length; }	
		public String getInstructions() { return m_instructions; }
		public List<char[]> getMap() { return m_map; }
	}


	/*
		Register A: 729
		Register B: 0
		Register C: 0

		Program: 0,1,5,4,3,0
	*/
	public static class InstructionParser extends FileParser {

		public int 			register_a;
		public int 			register_b;
		public int 			register_c;
		public int			inst_ptr;

		public ArrayList<Integer>	instructions;
		
		public InstructionParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			instructions = new ArrayList<Integer>();
		}

		public void parse() throws IOException {
			InternalReaderIterator iter = getFileIterator();
			register_a = Integer.parseInt(iter.next().substring(12));
			register_b = Integer.parseInt(iter.next().substring(12));
			register_c = Integer.parseInt(iter.next().substring(12));

			String instructionLine = iter.next().substring(9);
			StringTokenizer st = new StringTokenizer(instructionLine, ",");
			while (st.hasMoreTokens()) {
				instructions.add(Integer.parseInt(st.nextToken()));
			}

			System.out.format("machine: a=%d, b=%d, c=%d, instructions=%s)\n",
				register_a, register_b, register_c, instructions);
		}
	}

	/*
	*/
	public static class PageRuleParser extends FileParser {

		public static class PageRule {
			public final int before;
			public final int after;

			public PageRule (int b, int a) {
				before = b;
				after = a;
			}

			public String toString() {
				return String.format("[b:%d,a:%d]", before, after);
			}
		}
	
		protected final List<PageRule> m_rules;
		protected final List<List<Integer>> m_updates;
 
		public PageRuleParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_skipBlank = false;
			m_rules = new ArrayList<PageRule>();
			m_updates = new ArrayList<List<Integer>>();
			System.out.format("page rule parser init\n");
		}

		public List<PageRule> getRules() { return m_rules; }
		public List<List<Integer>> getUpdates() { return m_updates; }

		public void parse() throws IOException {

			System.out.format("rule line parse\n");

			// First section: rule list.
			int lineNum = 0;
			InternalReaderIterator iter = getFileIterator();
			for ( ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				System.out.format("rule line %02d: \"%s\" (%d)\n", lineNum, line, line.length());
				if (line.isEmpty()) {
					System.out.format("rule section complete\n");
					break;
				}

				int before = iter.getNextInteger();
				int after = iter.getNextInteger();
				PageRule pr = new PageRule(before, after);
				m_rules.add(pr);
				System.out.format("rule line %02d: %s\n", lineNum, pr);
			}

			// Next section: orders.
			for ( ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				System.out.format("order line %02d: \"%s\" (%d)\n", lineNum, line, line.length());
				List<Integer> builder = new ArrayList<Integer>();

				Integer nextInt = null;
				while ((nextInt = iter.getNextInteger()) != null) {
					builder.add(nextInt);
				}

				m_updates.add(builder);
				System.out.format("order line %02d: %s\n", lineNum, builder);
			}

			System.out.format("finished reading %d lines\n\n", lineNum);
		}
	}
}


