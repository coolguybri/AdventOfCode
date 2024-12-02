
import java.io.*;
import java.util.*;

public abstract class FileParser {
	protected final String 			m_filename;
	protected final File 			m_file;
	protected final BufferedReader 	m_reader;

	protected String 				m_line;
	protected int 					m_lineNum;
	protected int					m_currPos;

	public FileParser(int dayNum, boolean sample) throws IOException {
		m_filename = String.format("input/day%02d.%s", dayNum, sample ? "sample" : "input");
		System.out.format("Day %d, reading from file %s...\n\n", dayNum, m_filename);
		m_file = new File(m_filename);
		m_reader = new BufferedReader(new FileReader(m_file));

		m_line = null;
		m_lineNum = 0;
		m_currPos = 0;
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

				if (m_line == null) 
					break;

				m_lineNum++;
				m_currPos = 0;

				// Skip blank lines.
				m_line = m_line.trim();
				if (m_line.length() > 0)
					break;
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

		public int getNextInteger() {
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
				throw new IllegalArgumentException(
					String.format("next integer not found in string \"%s\" from pos %d", m_line, m_currPos));
			}

			return Integer.parseInt(m_line.substring(startIntPos, endIntPos));
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
}


