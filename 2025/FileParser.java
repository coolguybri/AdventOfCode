
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
		System.out.format("Day %d, reading from file %s...\n", dayNum, m_filename);
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
		5,4
		4,2
		4,5
		3,0
		2,1
	*/
	public static class PosParser extends FileParser {

		private ArrayList<Pos>	list;
		
		public PosParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			list = new ArrayList<Pos>();
		}

		public void parse() throws IOException {
			for (InternalReaderIterator iter = getFileIterator(); iter.hasNext() ; ) {
				String line = iter.next();
				StringTokenizer st = new StringTokenizer(line, ",");
				int x = Integer.parseInt(st.nextToken());
				int y = Integer.parseInt(st.nextToken());
				list.add(new Pos(x, y));
			}

			System.out.format("imported: %s)\n", list);
		}

		public List<Pos> getList() { return list; }
	}
	
	
	/*
    */	
 	public static class Rotation {
		public final boolean directionIsLeft;
		public final int clickCount;
		
		public Rotation(boolean directionIsLeft, int clickCount) {
			this.directionIsLeft = directionIsLeft;
			this.clickCount = clickCount;
		}

		@Override
		public String toString() {
			return String.format("(%c:%02d)", directionIsLeft ? 'L' : 'R', clickCount);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Rotation that = (Rotation) o;
			return (this.directionIsLeft == that.directionIsLeft) && (this.clickCount == that.clickCount);
		}

		@Override
		public int hashCode() {
			return Objects.hash(directionIsLeft, clickCount);
		}
    }
     
    /*
		L68
		L30
		R48
		L5
	*/
	public static class RotationParser extends FileParser {

		private ArrayList<Rotation>	list;
		
		public RotationParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			list = new ArrayList<Rotation>();
		}

		public void parse() throws IOException {
			for (InternalReaderIterator iter = getFileIterator(); iter.hasNext() ; ) {
				String line = iter.next();
				char dir = line.charAt(0);
				
				boolean bdir;
				switch (dir) {
				case 'L':
					bdir = true;
					break;
				case 'R':
					bdir = false;
					break;
				default:
					throw new IOException("bad direction");
				}
					
				int count = Integer.parseInt(line.substring(1));
				var rotation = new Rotation(bdir, count);
				list.add(rotation);
				System.out.format("  importing: %s\n", rotation);
			}

			System.out.format("imported: %s)\n", list);
		}

		public List<Rotation> getList() { return list; }
	}


	/*
    */	
 	public static class Sequence {
		public final long firstId;
		public final long lastId;
		
		public Sequence(long firstId, long lastId) {
			this.firstId = firstId;
			this.lastId = lastId;
		}
		
		public long getCount() {
			return (lastId - firstId) + 1;
		}
		
		public Iterator<Long> getIterator() {
			return new InternalIterator();
		}

		@Override
		public String toString() {
			return String.format("[%d-%d](%d)", firstId, lastId, getCount());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Sequence that = (Sequence) o;
			return (this.firstId == that.firstId) && (this.lastId == that.lastId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(firstId, lastId);
		}
		
		private class InternalIterator implements Iterator<Long> {
        	private long currentId; 

			protected InternalIterator() {
				currentId = firstId;
			}
			
			@Override
			public boolean hasNext() {
				return currentId <= lastId;
			}
	
			@Override
			public Long next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				return currentId++;
			}
		}
    }
     
    /*
		11-22,95-115,998-1012,1188511880-1188511890,222220-222224,
		1698522-1698528,446443-446449,38593856-38593862,565653-565659,
		824824821-824824827,2121212118-2121212124
	*/
	public static class SequenceListParser extends FileParser {

		private ArrayList<Sequence>	list;
		private int currSequenceId;
		private Iterator<Long> currIter;
		
		public SequenceListParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			list = new ArrayList<Sequence>();
			currSequenceId = -1;
			currIter = null;
		}

		public void parse() throws IOException {
			int totalIds = 0;
			for (InternalReaderIterator iter = getFileIterator(); iter.hasNext() ; ) {
				String line = iter.next();
				String[] sequenceSpecs = line.split(",");
				
				for (String sequenceSpec : sequenceSpecs) {
					String[] boundaries = sequenceSpec.split("-");
					long firstId = Long.parseLong(boundaries[0]);
					long lastId = Long.parseLong(boundaries[1]	);
					
					var seq = new Sequence(firstId, lastId);
					list.add(seq);
					totalIds += seq.getCount();
					System.out.format("  importing sequence: %s, totseq=%d, totalids=%d\n", seq.toString(), list.size(), totalIds);
				}
			}

			System.out.format("imported total count: %d\n", totalIds);
		}

		public boolean hasNextId() {
			// if we are still going throught he current sequence, continue to do so.
			if ((currIter != null) && currIter.hasNext())
				return true;
					
			// Figure out the next sequence we should read from, looking for the termintating end.
			currIter = null;
			currSequenceId++;
			if (currSequenceId >= list.size())
				return false;
				
			currIter = list.get(currSequenceId).getIterator();
			return true;
		}
		
		public long getNextId() { 
			return currIter.next(); 
		}
	}
	
	
	/*
	*/
	public static class BatteryCacheParser extends FileParser {
		protected final List<List<Integer>> m_list;
 
		public BatteryCacheParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_list = new ArrayList<List<Integer>>();
		}

		public int getBankCount() { return m_list.size(); }
		public List<Integer> getBank(int index) { return m_list.get(index); }

		public void parse() throws IOException {
			int lineNum = 0;
			for (InternalReaderIterator iter = getFileIterator() ; iter.hasNext() ; lineNum++) {
				String line = iter.next();
				var bank = new ArrayList<Integer>();
				
				for (char c : line.toCharArray()) {
					int joltage = Character.getNumericValue(c);	
					bank.add(joltage);
				}
				
				m_list.add(bank);
				System.out.format("bank %02d: %s\n", lineNum, bank);
			}
			System.out.format("finished reading %d banks\n\n", lineNum);
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
	public static class MatrixParser extends LineParser {
 
		public MatrixParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
		}

		public char getCell(int x, int y) { return m_list.get(y).charAt(x); }
		public char getCell(Pos pos) { return getCell(pos.x, pos.y); }
		public int getHeight() { return m_list.size(); }
		public int getWidth() { return m_list.get(0).length(); }	
	}
}


