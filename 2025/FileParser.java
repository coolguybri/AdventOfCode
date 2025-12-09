
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
	protected boolean				m_trimLine;

	public FileParser(int dayNum, boolean sample) throws IOException {
		m_filename = String.format("input/day%02d.%s", dayNum, sample ? "sample" : "input");
		System.out.format("Day %d, reading from file %s...\n", dayNum, m_filename);
		m_file = new File(m_filename);
		m_reader = new BufferedReader(new FileReader(m_file));

		m_line = null;
		m_lineNum = 0;
		m_currPos = 0;
		m_skipBlank = true;
		m_trimLine = true;
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
				
				if (m_trimLine) {
					m_line = m_line.trim();
				}

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
		
		public boolean isIn(long candidate) {
			return (candidate >= firstId) && (candidate <= lastId);
		}
		
		public Sequence merge(Sequence other) {
			if (this.firstId <= other.firstId) {
				if (this.lastId >= other.firstId) {
					if (this.lastId >= other.lastId)
						return new Sequence(this.firstId, this.lastId);
					else
						return new Sequence(this.firstId, other.lastId);
				}
			} else {
				if (other.lastId >= this.firstId) {
					if (other.lastId >= this.lastId)
						return new Sequence(other.firstId, other.lastId);
					else
						return new Sequence(other.firstId, this.lastId);
				}
			}
			// no merge possible.
			return null;
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
		3-5
		10-14
		16-20
		12-18
		
		1
		5
		8
		11
		17
		32
	*/
	public static class SequenceListPlusItemsParser extends FileParser {

		private ArrayList<Sequence>	ranges;
		private ArrayList<Long> items;
		
		public SequenceListPlusItemsParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_skipBlank = false;
			
			ranges = new ArrayList<Sequence>();
			items = new ArrayList<Long>();
		}

		public void parse() throws IOException {
			InternalReaderIterator iter;
			for (iter = getFileIterator(); iter.hasNext() ; ) {
				String line = iter.next();
				if (line.length() == 0)
					break;
				
				//System.out.format("  processing line: \"%s\"\n", line);
				String[] boundaries = line.split("-");	
				long firstId = Long.parseLong(boundaries[0]);
				long lastId = Long.parseLong(boundaries[1]);
				
				var seq = new Sequence(firstId, lastId);
				ranges.add(seq);
				System.out.format("  imported sequence: %s, range-count=%d\n", seq.toString(), ranges.size());
			}

			System.out.format("imported %d ranges\n", ranges.size());
			
			for ( ; iter.hasNext() ; ) {
				String line = iter.next();
				long nextId = Long.parseLong(line);
				
				items.add(nextId);
				System.out.format("  imported item: %d, item-count=%d\n", nextId, items.size());
			}
		}
		
		public ArrayList<Sequence> getRanges() { return ranges; }
		public ArrayList<Long> getItems() { return items; }
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
			System.out.format("finished reading %d lines\n", lineNum);
		}
	}
	

	/*
	*/
	public static class MatrixParser extends LineParser {
 
 		private int width;
 		private int height;
 		
		public MatrixParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
		}
		
		public void parse() throws IOException {
			super.parse();
			
			int lineLen = -1;
			for (var line : m_list) {
				if (lineLen < 0) {
					lineLen = line.length();
				}
				if (lineLen != line.length()) {
					throw new IOException("line lengths are not equal");
				}
			}
			
			width = m_list.get(0).length();
			height = m_list.size();
			System.out.format("matrix: %d x %d\n", getWidth(), getHeight());
		}
		
		public int getWidth() { return width; }
		public int getHeight() { return height; }
		
		public char getCell(Pos pos, char defaultValue) { 
			return getCell(pos.x, pos.y, defaultValue); 
		}
		
		public char getCell(int x, int y, char defaultValue) { 
			if ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight())) {
				//System.out.format("matrix: skipping out of bounds %d,%d\n", x, y);
				return defaultValue;
			}
			
			return m_list.get(y).charAt(x);
		}
		
		public void setCell(Pos pos, char replaceChar) { 
			setCell(pos.x, pos.y, replaceChar); 
		}
		
		public void setCell(int x, int y, char replaceChar) { 
			if ((x < 0) || (x >= getWidth()) || (y < 0) || (y >= getHeight())) {
				throw new IllegalArgumentException("out of bounds");
			}
			
			StringBuilder sb = new StringBuilder(m_list.get(y));
			sb.setCharAt(x, replaceChar);
			m_list.set(y, sb.toString());
		}
	}
	
	
	
	/*
	*/
	public static class MathProblemParser extends LineParser {

 		ArrayList<Character> operators;
 		
		public MathProblemParser(int dayNum, boolean sample) throws IOException {
			super(dayNum, sample);
			m_trimLine = false;
		}
		
		private void parseOperators() throws IOException {
			// suck up the operators and convert to a list of chars.
			int linecount = m_list.size();
			String operatorLine = m_list.remove(linecount - 1);
	
			String[] operatorsArray = operatorLine.trim().split("\\s+"); // regex for 1 to many whitespace chars
			operators = new ArrayList<>();
			for (String operator : operatorsArray) {
				 operators.add(operator.charAt(0));
			}
			System.out.format("mathproblems: operators: %s\n", operators);
		}
		
		public void parse() throws IOException {
			super.parse();
			parseOperators();
			
			// suck up all the operands and convert to lists of longs.
			var operandsListList = new ArrayList<ArrayList<Long>>();
			int linecount = m_list.size();
			for (int l = 0 ; l < linecount ; l++) {
				String operandsLine = m_list.get(l);
				var operandsList =  new ArrayList<Long>();

				String[] operandsArray = operandsLine.trim().split("\\s+");
				for (String operand : operandsArray) {
				 	operandsList.add(Long.parseLong(operand));
				}
				operandsListList.add(operandsList);
				System.out.format("mathproblems: operands (%02d): %s\n", l, operandsList);
			}
			
			// perform the math.
			long grandTotal = 0;
			for (var operator : operators) {
				switch (operator) {
				case '+':
					{
						System.out.format("* hit add operation\n");

						long sum = 0;
						for (var operandsList : operandsListList) {
							long operand = operandsList.remove(0);
							sum += operand;
							System.out.format(" operand=%d, sum=%d\n", operand, sum);
						}
						
						grandTotal += sum;
						System.out.format("* hit add sum=%d, grandTotal=%d\n", sum, grandTotal);
					}
					break;
					
				case '*':
					{
						System.out.format("* hit multiply operation\n");
					
						long product = 1;
						for (var operandsList : operandsListList) {
							long operand = operandsList.remove(0);
							product *= operand;
							System.out.format(" operand=%d, product=%d\n", operand, product);
						}
						
						grandTotal += product;
						System.out.format("* hit add product=%d, grandTotal=%d\n", product, grandTotal);
					}
					break;
				}
			}
		}
		
		private char getCharAt(int lineNo, int colNo) {
			if ((lineNo < 0) || (lineNo >= m_list.size()))
				return ' ';
			String line = m_list.get(lineNo);
			if ((colNo < 0) || (colNo >= line.length()))
				return ' ';
			return line.charAt(colNo);
		}
			
		protected Accumulator createAccumulator(char operator) throws IOException {
			switch (operator) {
			case '+':
				return new AddAccumulator();	
			case '*':
				return new MultiplyAccumulator();
			default:
				throw new IOException("illegal operator");
			}
		}
			
		protected interface Accumulator {
			long accumulate(long operand);
			long getResult();
		}
		
		protected class AddAccumulator implements Accumulator {
			private long sum = 0;
			
			public long accumulate(long operand) {
				sum += operand;
				return sum;
			}

			public long getResult() {
				return sum;
			}
		}
		
		protected  class MultiplyAccumulator implements Accumulator {
			private long product = 1;
			
			public long accumulate(long operand) {
				product *= operand;
				return product;
			}

			public long getResult() {
				return product;
			}
		}
			
		public void parsePart2() throws IOException {
			super.parse();
			parseOperators();
				
			// compute max line len.
			int maxLineLen = 0;
			int numLines = 0;
			for (var line : m_list) {
				numLines++;
				int lineLen = line.length();
				if (lineLen > maxLineLen) 
					maxLineLen = lineLen;
			}
			System.out.format(" parsing, numLines=%d, maxLineLen=%d\n", numLines, maxLineLen);

			long grandTotal = 0;
			
			// Get current operator.
			char operator = operators.remove(0);
			Accumulator accum = createAccumulator(operator);
			
			for (int parsePos = 0; parsePos < maxLineLen ; parsePos++) {
				boolean allClearColumn = true;
				StringBuffer sb = new StringBuffer();
				
				for (int q = 0 ; q < numLines ; q++) {
					char c = getCharAt(q, parsePos);
					sb.append(c);

					allClearColumn &= (c == ' ');
					System.out.format(" parsing, read %c from line %d, pos %d, buf=%s, allclear=%b\n", c, q, parsePos, sb.toString(), allClearColumn);
				}
				
				if (!allClearColumn) {
					long operand = Long.parseLong(sb.toString().trim());
					System.out.format("  parsed operand: %d\n", operand);
					
					long total = accum.accumulate(operand);
					System.out.format(" accumulating, %d tot=%d\n", operand, total);
				} else {
					grandTotal += accum.getResult();
					accum = null;
					
					if (operators.isEmpty()) {
						System.out.format(" end of accumulation\n");
						break;
					}

					operator = operators.remove(0);
					accum = createAccumulator(operator);
				}
			}
			
			if (accum != null) {
				grandTotal += accum.getResult();
				accum = null;
			}
			
			System.out.format("grand total is %d\n", grandTotal);
		}
	}
}


