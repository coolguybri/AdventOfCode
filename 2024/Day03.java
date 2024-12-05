
import java.io.*;
import java.util.*;

public class Day03 {

	private static int DAY_NUM = 3;
	private static boolean SAMPLE_INPUT = false;

	private static abstract class Operation {
	}

	private static class DoOperation extends Operation {
	}

	private static class DontOperation extends Operation {
	}

	private static class MulOperation extends Operation {
		public final int m_operand1;
		public final int m_operand2;

		public MulOperation(int op1, int op2) {
			m_operand1 = op1;
			m_operand2 = op2;
		}

		int getResult() {
			return m_operand1 * m_operand2;
		}
	}

	public static int validateDo(String line, int currPos, List<Operation> validOperations) {
		System.out.format(" validateDo: parsing currPos=%d, line=\"%s\"\n", currPos, line.substring(currPos));

		int lineLen = line.length();
		int start1 = currPos;

		char c = line.charAt(currPos);
		if (c != ')') {
			System.out.format(" validateDo: failed due to end-paren at pos %d, got \"%c\"\n",
				 currPos, c);
			return -1;
		}
		currPos++;

		System.out.format(" validateDo: succeeded\n");
		validOperations.add(new DoOperation());
		return 1;
	}

	public static int validateDont(String line, int currPos, List<Operation> validOperations) {
		System.out.format(" validateDont: parsing currPos=%d, line=\"%s\"\n", currPos, line.substring(currPos));

		int lineLen = line.length();
		int start1 = currPos;

		char c = line.charAt(currPos);
		if (c != ')') {
			System.out.format(" validateDont: failed due to end-paren at pos %d, got \"%c\"\n",
				 currPos, c);
			return -1;
		}
		currPos++;

		System.out.format(" validateDont: succeeded\n");
		validOperations.add(new DontOperation());
		return 1;
	}


	public static int validateMul(String line, int currPos, List<Operation> validOperations) {
		System.out.format(" validateMul: parsing currPos=%d, line=\"%s\"\n", currPos, line.substring(currPos));

		int lineLen = line.length();
		char c;


		// num1
		int start1 = currPos;
		for (; currPos < lineLen ; currPos++) {
			c = line.charAt(currPos);
            if (!Character.isDigit(c)) {
                break;
            }
        }
		int end1 = currPos;
		if ((end1 - start1) <= 0) {
			System.out.format(" validateMul: failed due to len1=%d\n", (end1 - start1));
			return -1;
		}

		// comma
		c = line.charAt(currPos);
		if (c != ',') {
			System.out.format(" validateMul: failed due to no comma at pos %d, got \"%c\"\n",
				 currPos, c);
			return -1;
		}
		currPos++;

		// num2
		int start2 = currPos;
		for (; currPos < lineLen ; currPos++) {
			c = line.charAt(currPos);
            if (!Character.isDigit(c)) {
                break;
            }
        }
		int end2 = currPos;
		if ((end2 - start2) <= 0) {
			System.out.format(" validateMul: failed due to len2=%d\n", (end2 - start2));
			return -1;
		}

		// comma
		c = line.charAt(currPos);
		if (c != ')') {
			System.out.format(" validateMul: failed due to end-paren at pos %d, got \"%c\"\n",
				 currPos, c);
			return -1;
		}
		currPos++;

		// cool!	
		int int1 = Integer.parseInt(line.substring(start1, end1));
		int int2 = Integer.parseInt(line.substring(start2, end2));
		System.out.format(" validateMul: succeeded with %d, %d\n", int1, int2);
		validOperations.add(new MulOperation(int1, int2));
		return currPos - start1;
	}


	public static void processLine(String line, List<Operation> validOperations) {

		String MUL_SEARCH_PATTERN = "mul(";

		int currPos = 0;
		int lineLen = line.length();
		System.out.format("processLine: starting with \"%s\", len=%d\n", line, lineLen);

		while (currPos < lineLen) {
			// Find the next token-start; if we don't find it, we are done.
			int nextStart = line.indexOf(MUL_SEARCH_PATTERN, currPos);
			if (nextStart == -1) {
				System.out.format("processLine: currPos=%d, len=%d, no more token-starts found\n",
					 currPos, lineLen);
				return;
			}

			// Advance past the token-start.
			currPos = nextStart + MUL_SEARCH_PATTERN.length();
			System.out.format("processLine: nextStart=%d, currPos=%d, len=%d, found token-start\n",
				 nextStart, currPos, lineLen);
	
			int operationLen = validateMul(line, currPos, validOperations);
			if (operationLen < 0) {
				// This was not a valid token, only the prefix matched.
				// Continue looking for more operations from this same exact point.
				System.out.format("processLine: currPos=%d, len=%d, token didnt pan out, skipping\n",
					 currPos, lineLen);
				continue;
			}

			// We got a good one, advance past the entire mul spec.
			currPos += operationLen;
			System.out.format("processLine: currPos=%d, len=%d, token consumed (%d), continuing\n",
				 currPos, operationLen, lineLen);
		}
	}

	public static void processLine2(String line, List<Operation> validOperations) {

		String SEARCH_PATTERN_MUL = "mul(";
		String SEARCH_PATTERN_DO = "do(";
		String SEARCH_PATTERN_DONT = "don't(";

		int currPos = 0;
		int lineLen = line.length();
		System.out.format("processLine2: starting with \"%s\", len=%d\n", line, lineLen);

		while (currPos < lineLen) {
			int operationLen = 0;
			if (line.indexOf(SEARCH_PATTERN_MUL, currPos) == currPos) {
				currPos += SEARCH_PATTERN_MUL.length();
				System.out.format("processLine2: MUL, currPos=%d, len=%d, found token-start\n",
					 currPos, lineLen);
				operationLen = validateMul(line, currPos, validOperations);
			} else if (line.indexOf(SEARCH_PATTERN_DO, currPos) == currPos) {
				currPos += SEARCH_PATTERN_DO.length();
				System.out.format("processLine2: DO, currPos=%d, len=%d, found token-start\n",
					 currPos, lineLen);
				operationLen = validateDo(line, currPos, validOperations);
			} else if (line.indexOf(SEARCH_PATTERN_DONT, currPos) == currPos) {
				currPos += SEARCH_PATTERN_DONT.length();
				System.out.format("processLine2: DONT, currPos=%d, len=%d, found token-start\n",
					 currPos, lineLen);
				operationLen = validateDont(line, currPos, validOperations);
			} else {
				currPos++;
				continue;
			}

			if (operationLen < 0) {
				// This was not a valid token, only the prefix matched.
				// Continue looking for more operations from this same exact point.
				System.out.format("processLine2: currPos=%d, len=%d, token didnt pan out, skipping\n",
					 currPos, lineLen);
				continue;
			}

			// We got a good one, advance past the entire mul spec.
			if (operationLen > 0) {
				currPos += operationLen;
				System.out.format("processLine: currPos=%d, len=%d, token consumed (%d), continuing\n",
					 currPos, operationLen, lineLen);
			}
		}
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		FileParser.LineParser fp = new FileParser.LineParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		List<String> lines = fp.getList();
		int totLinesLen = lines.size();

		boolean part1 = false;
		boolean part2 = !part1;

		List<Operation> validOperations = new ArrayList<Operation>();
	
		int lineNo = 0;	
		for (Iterator<String> iter = lines.iterator() ; iter.hasNext() ; lineNo++) {
			String line = iter.next();

			if (part1) {
				processLine(line, validOperations);
			} else {
				processLine2(line, validOperations);
			}

			System.out.format("line %d processed, now %d validOperations\n",
				 lineNo, validOperations.size());
		}

		int sum = 0;
		boolean blocked = false;
		System.out.format("processing Operations: %s\n",
			 validOperations.toString());
		for (Iterator<Operation> iter = validOperations.iterator(); iter.hasNext() ; ) {
			Operation mo = iter.next();
			if (mo instanceof MulOperation) {
				if (!blocked) {	
					sum += ((MulOperation) mo).getResult();	
				}
				System.out.format("MUL processed, block=%b, sum=%d\n", blocked, sum);
			} else if (mo instanceof DoOperation) {
				blocked = false;
				System.out.format("DO processed, block=%b\n", blocked);
			} else if (mo instanceof DontOperation) {
				blocked = true;
				System.out.format("DONT processed, block=%b\n", blocked);
			}

		}

		System.out.format("Analysis complete:\n sum=%d, part1=%b\n", sum, part1);
	}
}
