
import java.io.*;
import java.util.*;

public class Day15 {

	private static int DAY_NUM = 15;
	private static boolean SAMPLE_INPUT = false;
	private static boolean part1 = false;
	private static boolean part2 = !part1;

	protected static FileParser.Pos ForceLeft = new FileParser.Pos(-1, 0);
	protected static FileParser.Pos ForceRight = new FileParser.Pos(1, 0);
	protected static FileParser.Pos ForceUp = new FileParser.Pos(0, -1);
	protected static FileParser.Pos ForceDown = new FileParser.Pos(0, 1);


	/*
	*/
	public static void printMap(List<char[]> map, FileParser.Pos robotPos) {
		for (int y = 0 ; y < map.size() ; y++) {
			char[] line = map.get(y);
			System.out.print("\t");
			for (int x = 0 ; x < line.length ; x++) {
				if ((x == robotPos.x) && (y == robotPos.y)) {
					System.out.print('@');
				} else {
					System.out.print(line[x]);
				}
			}
			System.out.print("\n");
		}
	}

	public static FileParser.Pos attemptMovePart2(FileParser.RobotPathParser fp,
		 FileParser.Pos robotPos, FileParser.Pos force) throws Exception {
		if (!attemptMovePart2(fp, Collections.singleton(robotPos), force)) {
			return robotPos;
		}

		// TODO: perform the actiual move.
		return robotPos.add(force);
	}

	public static boolean attemptMovePart2(FileParser.RobotPathParser fp,
		 Set<FileParser.Pos> forceWave, FileParser.Pos forceDir) throws Exception {

		Set<FileParser.Pos> affectedBoxes = new HashSet<FileParser.Pos>();
		for (int wave = 0 ; true ; wave++) {
			System.out.format("attemptMove2: wave=%d, forceWave=%s, forceDir=%s\n",
				wave, forceWave, forceDir);
			Set<FileParser.Pos> crestBoxes = new HashSet<FileParser.Pos>();

			for (FileParser.Pos forcePos : forceWave) {
				FileParser.Pos nextPos = forcePos.add(forceDir);
				char c = fp.getCell(nextPos);

				// We can move into this empty space with no problem.
				if (c == '.') {
					System.out.format("attemptMove2: empty space looks cool\n");
					continue;
				}

				// A wall will block all movement.
				if (c == '#') {
					System.out.format("attemptMove2: BLOCKED by wall at %s\n", nextPos);
					return false;
				}

				// A box will start our push algorithm.
				if (c == '[') {
					FileParser.Pos box = nextPos;
					crestBoxes.add(box);
					System.out.format("attemptMove2: BOX left (%s)\n", box);
					continue;
				} 
				if (c == ']') {
					FileParser.Pos box = nextPos.left();
					crestBoxes.add(box);
					System.out.format("attemptMove2: BOX right (%s)\n", box);
					continue;
				}

				// Shoudl never get here.
				throw new Exception(String.format("Yo! bad map during box move"));
			}

			if (crestBoxes.isEmpty()) {
				// If we got here, there is nothing blocking us.

				// Mske the move. Sort the boxes acording to the direction we ar emoving in.
				var sortedList = new ArrayList<FileParser.Pos>(affectedBoxes);
				System.out.format("attemptMove: no boxes against crest of shockwace, cleared to move with %d boxes\n",
					 sortedList.size());

				for (var abox : sortedList) {
					fp.setCell(abox, '.');
					System.out.format("attemptMove: erasing %s\n", abox);
					fp.setCell(abox.right(), '.');
					System.out.format("attemptMove: erasing %s\n", abox.right());
				}

				for (var abox : sortedList) {
					FileParser.Pos newPos = abox.add(forceDir);
					fp.setCell(newPos, '[');
					fp.setCell(newPos.right(), ']');
				}

				return true;
			}


			// If we got here, we need to perform another wave.
			affectedBoxes.addAll(crestBoxes);
			Set<FileParser.Pos> newForceWave = new HashSet<FileParser.Pos>();
			if (forceDir.x == 0) {
				for (FileParser.Pos box : crestBoxes) {
					newForceWave.add(box);
					newForceWave.add(box.right());
				}
			} else if (forceDir.x > 0) {
				FileParser.Pos max = null;	
				for (FileParser.Pos box : crestBoxes) {
					if ((max == null) || (box.x > max.x))
						max = box;
				}
				newForceWave.add(max.right());
			} else {
				FileParser.Pos max = null;	
				for (FileParser.Pos box : crestBoxes) {
					if ((max == null) || (box.x < max.x))
						max = box;
				}
				newForceWave.add(max);
			}
			forceWave = newForceWave;
		}
	}


	public static FileParser.Pos attemptMove(FileParser.RobotPathParser fp, FileParser.Pos robotPos, FileParser.Pos force) throws Exception {
		FileParser.Pos nextPos = robotPos.add(force);
		char c = fp.getCell(nextPos);

		// We can move into this empty space with  no problem.
		if (c == '.') {
			System.out.format("attemptMove: empty space looks cool\n");
			return nextPos;
		}

		// A wall will block all movement.
		if (c == '#') {
			System.out.format("attemptMove: BLOCKED by wall\n");
			return robotPos;
		}

		// A box will start our push algorithm.
		if ((c == 'O') || (c == '[') || (c == ']')) {
			System.out.format("attemptMove: BOX (%c)\n", c);
			var boxList = new ArrayList<FileParser.Pos>();
			boxList.add(nextPos);
			for (FileParser.Pos probePos = nextPos.add(force) ; true ; probePos = probePos.add(force) ) {
				char probeCell = fp.getCell(probePos);
				System.out.format("attemptMove: probePos=%s, c=%c\n", probePos, probeCell);

				if (probeCell == '.') {
					// Just nodify the first and last boxes, the rest just slide along.
					fp.setCell(probePos, 'O');
					fp.setCell(nextPos, '.');
					return nextPos;
				}

				if (probeCell == '#') {
					System.out.format("attemptMove: BOXes all the way to wall, cannot move\n");
					return robotPos;
				}

				if ((c == 'O') || (c == '[') || (c == ']')) {
					System.out.format("attemptMove: Another box at %s\n", probePos);
					boxList.add(probePos);
					continue;
				}
				
				throw new Exception(String.format("Yo! bad map during box move"));
			}
		}

		// We should never get here.
		throw new Exception(String.format("YO, bad map element: %c", c));
	}

	/*
	*/
	public static FileParser.Pos attemptMove(FileParser.RobotPathParser fp, FileParser.Pos robotPos, char instruction) throws Exception {
		switch (instruction) {
			case '<':
				System.out.format("attemptMove: left\n");
				return part2 ?  attemptMovePart2(fp, robotPos, ForceLeft) : attemptMove(fp, robotPos, ForceLeft);
			case '>':
				System.out.format("attemptMove: right\n");
				return part2 ?  attemptMovePart2(fp, robotPos, ForceRight) : attemptMove(fp, robotPos, ForceRight);
			case '^':
				System.out.format("attemptMove: up\n");
				return part2 ?  attemptMovePart2(fp, robotPos, ForceUp) : attemptMove(fp, robotPos, ForceUp);
			case 'v':
				System.out.format("attemptMove: down\n");
				return part2 ? attemptMovePart2(fp, robotPos, ForceDown) : attemptMove(fp, robotPos, ForceDown);
		}

		// We should never get here.
		throw new Exception(String.format("Yo!: bad instruction: %c", instruction));
	}


	/*
	*/
	public static void main(String[] args) throws Exception { 

		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.RobotPathParser(DAY_NUM, SAMPLE_INPUT, part2);
		fp.parse();
		int width = fp.getWidth();
		int height = fp.getHeight();
		System.out.format("Analysis start: map width=%d, height=%d, part1=%b\n", width, height, part1);

		// Init the map/robolocations.
		FileParser.Pos robotPos = null;
		for (int h = 0 ; h < height ; h++) {
			for (int w = 0 ; w < width ; w++) {
				char c = fp.getCell(w, h);
				if (c == '@') {
					robotPos = new FileParser.Pos(w, h);
					fp.setCell(w, h, '.');
					break;
				}
			}
		}
		System.out.format("Import complete. robot=%s\n", robotPos);
		printMap(fp.getMap(), robotPos);

		// Loop thru instructions.	
		String instructions = fp.getInstructions();
		for (int i = 0 ; i < instructions.length() ; i++) {
			char currInstruction = instructions.charAt(i);
			System.out.format(" processing %c, robot=%s\n", currInstruction, robotPos);
			robotPos = attemptMove(fp, robotPos, currInstruction);
			//printMap(fp.getMap(), robotPos);
		}
		System.out.format("Instructions complete, robot=%s\n", robotPos);
		printMap(fp.getMap(), robotPos);

		// Compute GPS coordnates for each Box.
		long gpsSum = 0;
		if (part1) {
			for (int h = 0 ; h < height ; h++) {
				for (int w = 0 ; w < width ; w++) {
					char c = fp.getCell(w, h);
					if (c == 'O') {
						long gps = (100 * h) + w;
						gpsSum += gps;
						System.out.format(" gps for %d,%d: %d (%d)\n", w, h, gps, gpsSum);
					}
				}
			}
		} else {
			for (int h = 0 ; h < height ; h++) {
				for (int w = 0 ; w < width ; w++) {
					char c = fp.getCell(w, h);
					if (c == '[') {
						long gps = (100 * h) + w;
						gpsSum += gps;
						System.out.format(" gps for %d,%d: %d (%d)\n", w, h, gps, gpsSum);
					}
				}
			}
		}
		System.out.format("Computation complete. sum=%d\n", gpsSum);
	}
}
