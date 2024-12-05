
import java.io.*;
import java.util.*;

public class Day04 {

	private static int DAY_NUM = 4;
	private static boolean SAMPLE_INPUT = false;

	private static String SEARCH = "XMAS";
	private static char S2_MID = 'A';
	private static char S2_END1 = 'M';
	private static char S2_END2 = 'S';

	public static boolean inBounds(int x, int y, int width, int height) {
		if (x < 0 || x >= width)
			return false;
		if (y < 0 || y >= height)
			return false;
		return true;
	}

	public static int processPosition2(FileParser.MatrixParser fp, int col, int row) {
		char c = fp.getCell(col, row);
		if (S2_MID != c) 
			return 0;

		System.out.format("processPosition2: found mid at %d,%d\n", col, row);

		int numHits = 0;
		int width = fp.getWidth();
		int height = fp.getHeight();
		for (int deltaX = -1 ; deltaX <= 1 ; deltaX+=2) {
			for (int deltaY = -1 ; deltaY <= 1 ; deltaY+=2) {
				// look for out of bounds in column.
				int newCol = col + deltaX;
				int newRow = row + deltaY;
				if (!inBounds(newCol, newRow, width, height))
					continue;
				char c2 = fp.getCell(newCol, newRow);
				if (S2_END1 != c2)
					continue;

				System.out.format("processPosition2: found c1 at %d,%d, dir=%d,%d\n",
					 newCol, newRow, deltaX, deltaY);

				int counterCol = col + (deltaX * -1);
				int counterRow = row + (deltaY * -1);
				if (!inBounds(counterCol, counterRow, width, height))
					continue;
				char counterC = fp.getCell(counterCol, counterRow);

				System.out.format("processPosition2: looking for counter at %d,%d, dir=%d,%d, c=%c\n",
					 counterCol, counterRow, deltaX * -1, deltaY * -1, counterC);
				if (S2_END2 != counterC)
					continue;
				
				System.out.format("processPosition2: FOUND from %d,%d, to %d,%d\n",
					 newCol, newRow, counterCol, counterRow);
				numHits++;
			}
		}

		return (numHits == 2) ? 1 : 0;
	}

	public static int processPosition(FileParser.MatrixParser fp, int col, int row) {
		char c = fp.getCell(col, row);
		if (SEARCH.charAt(0) != c) 
			return 0;

		System.out.format("processPosition: found c0 at %d,%d\n", col, row);

		int numHits = 0;
		int width = fp.getWidth();
		int height = fp.getHeight();
		for (int deltaX = -1 ; deltaX <= 1 ; deltaX++) {
			for (int deltaY = -1 ; deltaY <= 1 ; deltaY++) {
				// dont process the center square.
				if (deltaX == 0 && deltaY == 0)
					continue;

				// look for out of bounds in column.
				int newCol = col + deltaX;
				if (newCol < 0 || newCol >= width)
					continue;

				// look for out of bounds in row.
				int newRow = row + deltaY;
				if (newRow < 0 || newRow >= height)
					continue;

				char c2 = fp.getCell(newCol, newRow);
				if (SEARCH.charAt(1) != c2)
					continue;

				System.out.format("processPosition: found c1 at %d,%d, dir=%d,%d\n",
					 newCol, newRow, deltaX, deltaY);

				for (int i = 2 ; i < SEARCH.length() ; i++) {
					int nextCol = col + (deltaX * i);
					int nextRow = row + (deltaY * i);

					// look for out of bounds in column.
					if (nextCol < 0 || nextCol >= width)
						break;

					// look for out of bounds in row.
					if (nextRow < 0 || nextRow >= height)
						break;

					char nextC = fp.getCell(nextCol, nextRow);
					if (SEARCH.charAt(i) != nextC)
						break;

					System.out.format("processPosition: found c%d at %d,%d\n",
						 i, nextCol, nextRow);
					if (i == SEARCH.length() - 1) {
						System.out.format("processPosition: MATCH from %d,%d to %d,%d\n",
							 col, row, nextCol, nextRow);
						numHits++;
					}
					
				}
			}
		}

		return numHits;
	}
		
	/*
	*/
	public static void main(String[] args) throws Exception { 
		boolean part1 = false;
		boolean part2 = !part1;

		// Import the data; will throw if no file or data makes no sense.
		FileParser.MatrixParser fp = new FileParser.MatrixParser(DAY_NUM, SAMPLE_INPUT);
		fp.parse();

		int width = fp.getWidth();
		int height = fp.getHeight();
		System.out.format("Analysis start: matrix width=%d, height=%d, part1=%b\n", width, height, part1);

		int sum = 0;
		for (int row = 0 ; row < height ; row++) {
			for (int col = 0 ; col < width ; col++) {

				int newHits;

				if (part1) {
					newHits = processPosition(fp, col, row);
				} else {
					newHits = processPosition2(fp, col, row);
				}
					
				sum += newHits;
				System.out.format(" adding %d more, sum=%d\n", newHits, sum);
			}
		}

		System.out.format("Analysis complete:\n sum=%d, part1=%b\n", sum, part1);
	}
}
