
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day16 {


  static class Valve {
  	String 			name;
  	int 			flow_rate;
  	List<String>	tunnels;
  	
  	int			simulationState;
  	
  	public Valve(String n, int f, List<String> t) {
  		name = n;
  		flow_rate = f;
  		tunnels = t;
  	}
  	
  	public String toString() {
  		return String.format("[%s:%d tunnels=%s]", name, flow_rate, tunnels);
  	}
  	
  	public void simulationReset() {
  		simulationState = 0;
  	}
  	
  	public boolean simulationIncrement() {
  		return false;
  	}
  } 
  
  
  static class ValveInspection {
  	String 	name;
  	int		cost;
  	
  	public ValveInspection(String n, int c) {
  		name = n;
  		cost = c;
  	}
  }
  
  static class SimulationRun {
  	int time_remaining = 30;
  }
  
  
  static String generateDistanceKey(String startName, String endName) {
  	return String.format("%s:%s", startName, endName);
  }
  
  
  static class DistanceMeta {
  	int 	cost;
  	String 	prevName;
  	
  	
  	public DistanceMeta(int c, String p) {
  		cost = c;
  		prevName = p;
  	}
  }
  
  
  static List<String> computeShortestPath(String startName, String endName, HashMap<String, Valve> valvesMap) throws Exception {
  	HashSet<String> unvisited = new HashSet<String>(valvesMap.keySet());
  	HashMap<String, DistanceMeta> distances = new HashMap<String, DistanceMeta>();
  	PriorityQueue<ValveInspection> pQueue = new PriorityQueue<ValveInspection>((a,b) -> (a.cost - b.cost));
  	
  	distances.put(generateDistanceKey(startName, startName), new DistanceMeta(0, null));
  	pQueue.add(new ValveInspection(startName, 0));
  	Valve endValve = valvesMap.get(endName);
  	
  	ValveInspection nextStop;
  	while ((nextStop = pQueue.poll()) != null) {
  	
		if (!unvisited.contains(nextStop.name))
			continue;
				
  		Valve currValve = valvesMap.get(nextStop.name);
  	
				
  		String dk = generateDistanceKey(startName, currValve.name);
  		DistanceMeta currDm = distances.get(dk);
  		//System.out.format("computeShortestPath: processing %s, cost=%d\n", currValve.name, currDm.cost);
  		
  		if (currValve.name.equals(endName)) {
  			// end trigger: we have found the path to the end.
			List<String> path = new ArrayList<String>();
			path.add(0, currValve.name);
			
			DistanceMeta nextDm = currDm;
			while ((nextDm != null) && (nextDm.prevName != null)) {
				path.add(0, nextDm.prevName);
  				nextDm = distances.get(generateDistanceKey(startName, nextDm.prevName));
			}
			
  			//System.out.format("computeShortestPath: found END: %s, cost=%d, path=%s\n", currValve.name, currDm.cost, path);
  			return path;
  		}
  		
  		// Update cost to all nodes connected to this one.
  		int nextCost = nextStop.cost + 1;
		for (Iterator<String> iter = currValve.tunnels.iterator() ; iter.hasNext() ; ) {
			String nextName = iter.next();
		
			// skip already-visited nodes; by definition, we already have a quicker or equal path to those kinds of nodes.
			if (!unvisited.contains(nextName))
				continue;
			
			// update the distance calculated,
			String distancekey = generateDistanceKey(startName, nextName);
			DistanceMeta dm = distances.getOrDefault(distancekey, null);
			if ((dm == null) || (nextCost < dm.cost)) {
				distances.put(distancekey, new DistanceMeta(nextCost, currValve.name));
			}
			
			// Add it to our queue of nodes to inspect.
			pQueue.add(new ValveInspection(nextName, nextCost));
			//System.out.format("computeShortestPath: queueing %s\n", nextName);
		}
		
		boolean actualyRemoved = unvisited.remove(currValve.name);
		if (!actualyRemoved) {
			throw new Exception(String.format("illegal state for unvisited: could not remove %s from %s", currValve.name, unvisited));
		} else {
			//System.out.format("computeShortestPath: removed %s from unvisited %s\n", currValve.name, unvisited);
		}
	}
	
	// if we got here, no more nodes to inspect!
	throw new Exception("no path available to destination");
  }
  
  static long best_simulation_run = 0;
  
  static long permutations_count = 0;
  
  static long max_simulations = 100;
  
  static int END_TIME = 30;
  
  static HashMap<String, List<String>> pathCache = new HashMap<String, List<String>>();
  
   
  static void runSimulation(List<String> valveSequence, HashMap<String, Valve> valvesMap) throws Exception {
  	permutations_count++;
  	
  	if ((permutations_count % 10000) == 0) {
  		System.out.format("runSimulation %d: START: sequence: %s\n", permutations_count, valveSequence);
  	}
  	
  	//String nextValve = valveSequence.get(0);
  	//List<String> path = computeShortestPath("AA", nextValve, valvesMap);
  	//System.out.format("runSimulation %d: path is: %s\n", permutations_count, path);
  	
  	int time = 0;
  	int total_pressure_release = 0;
  	String stepStartName = "AA";
  	String stepEndName = null;
  	
  	for (Iterator<String> iter = valveSequence.iterator() ; iter.hasNext() ; ) {
  		stepEndName = iter.next();
  		//stepStartValue = valvesMap.get(stepStartName);
  		Valve stepEndValve = valvesMap.get(stepEndName);
  		
  		String pathCacheKey = String.format("%s:%s", stepStartName, stepEndName);
  		List<String> path = pathCache.get(pathCacheKey);
  		if (path == null) {
  			path = computeShortestPath(stepStartName, stepEndName, valvesMap);
  			pathCache.put(pathCacheKey, path);
  			System.out.format("runSimulation %d: cache fail for %s, now %d entries\n", permutations_count, pathCacheKey, pathCache.size());
  		}
  		
  		time += path.size() - 1; // time to move there
  		time++; // time to open the valve there
  		if (time >= END_TIME) {
  			//System.out.format("runSimulation %d: hit endtime! time=%d, pressure=%d\n", permutations_count, time, total_pressure_release);
  			break;
  		}
  		
  		int curr_pressure_release = (END_TIME - time) * stepEndValve.flow_rate;
  		total_pressure_release += curr_pressure_release;
  		//System.out.format("runSimulation %d: made it to valve %s, time=%d, newpressure=%d, totpressure=%d\n", 
  		//	permutations_count, stepEndName, time, curr_pressure_release, total_pressure_release);
  		
  		stepStartName = stepEndName;
  	}
  	
  	
  	if ((permutations_count % 10000) == 0) {
		System.out.format("runSimulation %d: END  : sequence: %s, time=%d, pressure_release=%d, best-run=%d\n", 
			permutations_count, valveSequence, time, total_pressure_release, best_simulation_run);
	}
  		
  		
  	if (total_pressure_release > best_simulation_run) {
  		best_simulation_run = total_pressure_release;
  		System.out.format("runSimulation %d: NEW BEST pressure=%d, sequence=%s\n", permutations_count, best_simulation_run, valveSequence);
  	}
  	
  	
  	/*if (permutations_count >= max_simulations) {
  		System.out.format("EARLY EXIT");
  		System.exit(5);
  	} */
  }
  
  
  static int generateNext(List<String> availableList, List<String> currOutput, HashMap<String, Valve> valvesMap) throws Exception {
  
  	// If no more valves to process, break out.
  	if (availableList.isEmpty()) {
  		runSimulation(currOutput, valvesMap);
  		return 1;
  	}
  	
  	// Loop through the remaining available stuff.
  	List<String> stableCopy = new ArrayList<String>(availableList);
  	int generated_runs = 0;
  	for (int i = 0 ; i < stableCopy.size() ; i++) {
  		String name = stableCopy.get(i);
  		
  		currOutput.add(name);
  		availableList.remove(name);
  		
  		generated_runs += generateNext(availableList, currOutput, valvesMap);
  		
  		availableList.add(name);
  		currOutput.remove(name);
  	}
  	
  	return generated_runs;
  }
  
  
  static int generateSimulationSequences(HashMap<String, Valve> valvesMap) throws Exception {
  	List<String> currOutput = new ArrayList<String>();
  	List<String> availableList =  new ArrayList<String>();
  	
  	for (Iterator<Valve> iter = valvesMap.values().iterator() ; iter.hasNext() ; ) {
  		Valve v = iter.next();
  		if (v.flow_rate <= 0) {
  			System.out.format("generateSimulationSequences skipping valve %s because %s\n", v.name, v);
  		} else {
  			availableList.add(v.name);
  		}
  	}
  	
  	Collections.sort(availableList);
  	System.out.format("generateSimulationSequences starting with valves %s \n", availableList);
  	
  	return generateNext(availableList, currOutput, valvesMap);
  }
  
  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 16 Mutha Fuckah:\n\n");
  	
  	long start = System.currentTimeMillis();
  	
  	HashMap<String, Valve> valvesMap = new HashMap<String, Valve>();
	
	// Open input file & import paths.
   	File file = new File("day16.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
	String line;
	
	while ((line = br.readLine()) != null) {		
		// Example:
		// Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
		String name = line.substring(6, 8);	
		int endNum = line.indexOf(";");
		int flow_rate = Integer.parseInt(line.substring(23, endNum));

		int tunnelListBegin = line.indexOf("tunnels lead to valves "); 
		if (tunnelListBegin >= 0)  {
			tunnelListBegin += 23;
		} else {
			tunnelListBegin = line.indexOf("tunnel leads to valve ") + 22;
		}
		String tunnelListString = line.substring(tunnelListBegin);
		StringTokenizer st = new StringTokenizer(tunnelListString, ", ");
		List<String> tunnelList = new ArrayList<String>();
		while (st.hasMoreTokens())
			tunnelList.add(st.nextToken());
	
		Valve valve = new Valve(name, flow_rate, tunnelList);
		valvesMap.put(name, valve);

		System.out.format("Import: valve=%s\n", valve);
	 }
	 System.out.format("Import End: with valve-count=%d\n", valvesMap.size());

     // Start simulation engine.
     int runs = generateSimulationSequences(valvesMap);
     System.out.format("Analysis End: with permutation-count=%d, runs=%d, BEST pressure=%d\n", 
     	permutations_count, runs, best_simulation_run);
  }
}
