
import java.io.*;
import java.util.*;

public class Day10 {

    private static int DAY_NUM = 10;
    private static boolean SAMPLE_INPUT = false;
    
    static long nextLog = 0;
	static long logPeriod = 1000000;

	private static ArrayDeque<QueueNode> 	workQueue = new ArrayDeque<QueueNode>();
	private static boolean					workSolved = false;
	private static HashSet<Integer>			workKeys = new HashSet<Integer>();
	private static HashSet<String>			workKeys2 = new HashSet<String>();
	
	static long pullCount = 0;
	
	protected static class QueueNode {

		public final FileParser.Machine 	machine;
		public final FileParser.Button		nextButton;
		public int							currentState;
		public int							nextState;
		public int[]						currentCounterState;
		public int[]						nextCounterState;
		public long							historyCount;
		
		public QueueNode(FileParser.Machine machine, FileParser.Button nextButton, int currentState, int nextState, long historyCount) {
			this.machine = machine;
			this.nextButton = nextButton;
			this.currentState = currentState;
			this.nextState = nextState;
			this.historyCount = historyCount;
		}
		
		public QueueNode(FileParser.Machine machine, FileParser.Button nextButton, int[] currentCounterState, int[] nextCounterState, long historyCount) {
			this.machine = machine;
			this.nextButton = nextButton;
			this.currentCounterState = currentCounterState;
			this.nextCounterState = nextCounterState;
			this.historyCount = historyCount;
		}
		
		@Override
		public String toString() {
			if (currentCounterState == null)
				return String.format("[qn machine=%02d, state=%s, nextState=%d, nextButton=%d, hist=%d]", 
					machine.id, currentState, nextState, nextButton.id, historyCount);
			return String.format("[q2 machine=%02d, state=%s, nextState=%s, nextButton=%d, hist=%d]", 
				machine.id, Arrays.toString(currentCounterState), Arrays.toString(nextCounterState), nextButton.id, historyCount);
		}
	}
	
	/*
	*/
	protected static void queuePush(FileParser.Machine machine, FileParser.Button nextButton, int currentState, long historyCount) {
	
		// Apply light changes to get the next state.
		int nextState = currentState;
		for (var light : nextButton.lights) {
			boolean isOn = (nextState & (1 << light)) != 0;
			if (isOn) {
				// Turn the bit off.
				nextState = nextState & (~(1 << light));
			} else {
				// set the bit on.
				nextState = (nextState | (1 << light));
			}
		}
		
		// Short-circuity if we already explored this possibility.
		if (!workKeys.add(nextState)) {
			//System.out.format(" queue: push-reject: already have state=%d, nextState=%d, len=%d, keys=%s\n", currentState, nextState, workQueue.size(), workKeys);
			return;
		}
		
		var node = new QueueNode(machine, nextButton, currentState, nextState, historyCount);
		workQueue.add(node);
		
		if ((nextLog++ % logPeriod) == 0) {
			System.out.format(" queue: push: node=%s, q-len=%d\n", node, workQueue.size());
		}
	}
	
	
	
	/*
	*/
	protected static QueueNode queuePull() {
		QueueNode node = workQueue.poll();
		if (node == null) {
			System.out.format(" queue: pull hit bottom\n", node);
			return null;
		}
			
		pullCount++;
			
		if ((nextLog++ % logPeriod) == 0) {
			System.out.format(" queue: pull: node=%s, q-pull-count=%d, q-len=%d\n", node, pullCount, workQueue.size());
		}
		return node;
	}

	/*
	*/
	protected static void queuePush2(FileParser.Machine machine, FileParser.Button nextButton, int[] currentCounterState, long historyCount) {
	
		// Apply light changes to get the next state.
		int[] nextCounterState = new int[currentCounterState.length];
		for (int i = 0 ; i < currentCounterState.length ; i++) {
			nextCounterState[i] = currentCounterState[i];
		}
		for (var counter : nextButton.lights) {
			int curr = nextCounterState[counter];
			nextCounterState[counter] = ++curr;
		}
	
		// Short-circuit if we already explored this possibility.
		String mapkey = Arrays.toString(nextCounterState);
		if (!workKeys2.add(Arrays.toString(nextCounterState))) {
			//System.out.format(" queue: push-reject: already considered (%s), nextState=%s, q-len=%d\n", 
			//  mapkey, Arrays.toString(nextCounterState), workQueue.size());
			return;
		}
			
		// Short circuit if any of the counters have gone over the limit.
		for (int i = 0 ; i < nextCounterState.length ; i++) {
			if (nextCounterState[i] > machine.desiredJoltageTarget[i]) {
				//System.out.format(" ** queue: push-overflow: counter %d is overflowed, nextState=%s, q-len=%d\n", 
				//	i, Arrays.toString(nextCounterState), workQueue.size());
				return;
			}
		}
		
		var node = new QueueNode(machine, nextButton, currentCounterState, nextCounterState, historyCount);
		workQueue.add(node);
		
		if ((nextLog++ % logPeriod) == 0) {
			System.out.format(" queue: push: node=%s, q-len=%d\n", node, workQueue.size());
		}
	}
	
	
	
	protected static long processMachine2(FileParser.Machine machine) {
		System.out.format("\n* processing machine2: %s\n\n", machine);

		// Reset the global queue.
		workQueue.clear();
		workKeys.clear();
		workKeys.add(0); // dont bother coming back to the initial setting.
		workKeys2.clear();
		workSolved = false;
		
		// init the initial state of the counters all off.
		var initialState = new int[machine.desiredJoltageLen];
		
		// Queue up the beginning for each.
		var buttons = machine.buttons;
		for (var button : buttons) {
			queuePush2(machine, button, initialState, 1);
		}
		
		QueueNode popNode;
		while ((popNode = queuePull()) != null) {
			//System.out.format("  * processing node start: %s\n", popNode);
			
			if ((nextLog++ % logPeriod) == 0) {
				System.out.format("  * processing node complete: %s\n", popNode);
			}
			
			// Loop termination when we actually find the sequence.
			if (Arrays.equals(popNode.nextCounterState, machine.desiredJoltageTarget)) {
				System.out.format("  * solution discovered! (%d) %s\n", popNode.historyCount, popNode);
				workSolved = true;
				return popNode.historyCount;
			}
			
			// Otherwise, lets keep looking.
			var butts = machine.buttons;
			for (var button : butts) {
				queuePush2(machine, button, popNode.nextCounterState, (popNode.historyCount + 1));
			}
		}
		
		// Ran out of nodes, target is impossible!
		return 0;
	}
	
	protected static long processMachine(FileParser.Machine machine) {
		System.out.format("\n* processing machine: %s\n\n", machine);

		// Reset the global queue.
		workQueue.clear();
		workKeys.clear();
		workKeys.add(0); // dont bother coming back to the initial setting.
		workKeys2.clear();
		workSolved = false;
		
		// init the initial state of the light circuit: all off.
		int initialState = 0;
		
		// Queue up the beginning for each.
		var buttons = machine.buttons;
		for (var button : buttons) {
			queuePush(machine, button, 0, 1);
		}
		
		QueueNode popNode;
		while ((popNode = queuePull()) != null) {
			//System.out.format("  * processing node start: %s\n", popNode);
			
			if ((nextLog++ % logPeriod) == 0) {
				System.out.format("  * processing node complete: %s\n", popNode);
			}
			
			// Loop termination when we actually find the sequence.
			if (popNode.nextState == machine.desiredBits) {
				System.out.format("  * solution discovered! (%d) %s\n", popNode.historyCount, popNode);
				workSolved = true;
				return popNode.historyCount;
			}
			
			// Other wise, lets keep looking.
			var butts = machine.buttons;
			for (var button : butts) {
				queuePush(machine, button, popNode.nextState, (popNode.historyCount + 1));
			}
		}
		
		// TODO: pull formt he queue until it is empty, or we have a solution.
		return 0;
	}
	
	/*
	*/
    public static void main(String[] args) throws Exception { 
		long startTime = System.currentTimeMillis();
		
		boolean part1 = false;
		
		// Import the data; will throw if no file or data makes no sense.
		var fp = new FileParser.MachineListParser(DAY_NUM, SAMPLE_INPUT);
	    fp.parse();
	    
	   	var machineList = fp.getMachines();
		System.out.format("imported machine list: %s\n\n", machineList);

		if (part1) {
			long buttonPresses = 0;
			long maxLights = 0;
			long maxButtons = 0;
			for (var machine : machineList ) {
				int numLights = machine.desiredBitsLen;
				if (numLights > maxLights)
					maxLights = numLights;
				int numButtons = machine.buttons.size();
				if (numButtons > maxButtons)
					maxButtons = numButtons;
				System.out.format("processed machine %02d in lights=%d/%d, buttons=%d/%d\n", 
					machine.id, numLights, maxLights, numButtons, maxButtons);
				
				var thisPresses = processMachine(machine);
				buttonPresses += thisPresses;
				
				System.out.format("processed machine %02d in %d presses, tot=%d\n\n", machine.id, thisPresses, buttonPresses);
			}
		} else {
			long buttonPresses = 0;
			
			for (var machine : machineList ) {
				
				var thisPresses = processMachine2(machine);
				buttonPresses += thisPresses;
				
				System.out.format("processed machine %02d in %d presses, tot=%d\n\n", machine.id, thisPresses, buttonPresses);
			}
		}
 
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		System.out.format("runtime: %d millis\n", duration);
	}
}
