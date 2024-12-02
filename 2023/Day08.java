
import java.io.*;
import java.util.*;

public class Day08 {

	private static class Node {
		public final String name;
		public final String left;
		public final String right;

		private Node(String n, String l, String r) {
			name = n;
			left = l;
			right = r;
		}

		public String toString() {
			return String.format("[node: %s l:%s r:%s]", name, left, right);
		}

		// AAA = (BBB, BBB)
		public static Node parse(BufferedReader br) throws IOException {
			String line = br.readLine();
			if (line == null) {
				return null; // EOF
			}	

			// TODO: PARSE
			int splitpos = line.indexOf('=');
			String name = line.substring(0, splitpos).trim();
			String children = line.substring(splitpos + 3, line.length() - 1);
			splitpos = children.indexOf(',');
			String left = children.substring(0, splitpos).trim();
			String right = children.substring(splitpos + 2).trim();

			Node node = new Node(name, left, right);
			System.out.format("imported: %s\n", node);
			return node;

		}
	}

	private static class Game {
		private final String 		instructions;
		private final Map<String, Node>	nodes;

		private class Thread {
			private Node currNode;

			public Thread(String startName) {
				currNode = nodes.get(startName);
			}

			public boolean isAtEnd() {
				if ((currNode.name.charAt(currNode.name.length() - 1)) == 'Z') {
					//System.out.format("play: reached target %s\n", currNode.name);
					return true;
				}
				return false;
			}

			public void playStep(long intptr) {
				// Grab the next instruction.
				char inst = instructions.charAt((int) (intptr % instructions.length()));

				if (inst == 'L') {
					//System.out.format("play (%d): executing %s - >left (%s)\n", intptr, currNode.name, currNode.left);
					currNode = nodes.get(currNode.left);
				} else if (inst == 'R') {
					//System.out.format("play (%d): executing %s -> right (%s)\n", intptr, currNode.name, currNode.right);
					currNode = nodes.get(currNode.right);
				} else {
					throw new IllegalStateException();
				}
			}	
		}

		private Game(String i, Map<String, Node> n) {
			instructions = i;
			nodes = n;
		
		}

		public String toString() {
			return String.format("[game: %s nodes:%d]", instructions, nodes.size());
		}

		public long playParallel() {

			// Allocate the execution threads.
			List<Thread> starts = new ArrayList<Thread>();
			for (Map.Entry<String, Node> entry : nodes.entrySet()) {
				String nodeName = entry.getValue().name;
				if ((nodeName.charAt(nodeName.length() - 1)) == 'A') {
						System.out.format("playX: startNode %s\n", nodeName);
						starts.add(new Thread(nodeName));
				}
			}
			System.out.format("playEx: starting with %d starts\n", starts.size());

			long intptr = 0;
			for ( ; ; intptr++) {
				boolean allDone = true;	
				for	(Thread t : starts) {
					allDone &= t.isAtEnd();
				}
				if (allDone) {
					System.out.format("playEx: Breakout!");
					break;
				}

				// Grab the next instruction.
				for	(Thread t : starts) {
					t.playStep(intptr);
				}

				if ((intptr % 10000000) == 0) {
					System.out.format("playEx: working on %d step\n", intptr);
				}
			}

			return intptr;
		}

		public long playBasic() {

		
			long intptr = 0;
			Node curr = nodes.get("AAA");
			for ( ; ; intptr++ ) {
				if (curr.name.equals("ZZZ")) {
					System.out.format("play (%d): reached target\n", intptr);
					break;
				}
					System.out.format("play (%d): executing %s - >left (%s)\n", intptr, curr.name, curr.left);
				
				// Grab the next instruction.
				char inst = instructions.charAt((int) (intptr % instructions.length()));
				if (inst == 'L') {
					System.out.format("play (%d): executing %s - >left (%s)\n", intptr, curr.name, curr.left);
					curr = nodes.get(curr.left);
				} else if (inst == 'R') {
					System.out.format("play (%d): executing %s -> right (%s)\n", intptr, curr.name, curr.right);
					curr = nodes.get(curr.right);
				} else {
					throw new IllegalStateException();
				}
			}
			return intptr;
		}

		public static Game parse(BufferedReader br) throws IOException {
			String instructions = br.readLine();
			br.readLine();

			Map<String,Node> nodes = new HashMap<String, Node>();
			Node node;	
			while ((node = Node.parse(br)) != null) {
				nodes.put(node.name, node);
			}

			Game game = new Game(instructions, nodes);
			System.out.format("imported game: %s\n", game);
			return game;
		}
	}

	public static void main(String[] args) throws Exception { 
		long now = System.currentTimeMillis();
		System.out.format("Day Seven, Yo!\n\n");

        //String filename = "input/day08.sample3";
        String filename = "input/day08.input";       	
       	File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
		Game game = Game.parse(br);

//		long answer = game.playBasic();
		long answer = game.playParallel();

		long done = System.currentTimeMillis();
		System.out.format("answer: %d\n", answer);
		System.out.format("execution: %d\n", (done - now));
	}
}
