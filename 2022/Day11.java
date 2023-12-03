
import java.io.*;
import java.util.*;
import java.util.stream.*;
import java.text.ParseException;
import java.math.*;

public class Day11 {

	
    static class Item {
    	public BigAssNumber worry_level;
    	
    	public Item(BigAssNumber w) {
    		worry_level = w;
    		System.out.format("   created Item with worry=%s\n", worry_level);
    	}
    }
    
	static class Monkey {
		int monkey_num;
		public List<Item> queue;
		char operation_operator;
		Integer operation_operand;
		int test_divisor;
		int monkey_true;
		int monkey_false;
		
		long inspection_count;
		
		public Monkey(int num, List<Item> q, char op_op, Integer op_operand, int divisor, int t_true, int t_false) {
			queue = new ArrayList<Item>();
			monkey_num = num;
			queue = q;
			operation_operator = op_op;
			operation_operand = op_operand;
			test_divisor = divisor;
			monkey_true = t_true;
			monkey_false = t_false;
			
			inspection_count = 0;
		}
		
		public String toString() {
			return String.format("[Monkey %d, queue=%d, op=%s, operand=%s, divisor=%d, throw-true:%d, throw-false:%d]", 
				monkey_num, queue.size(), operation_operator, (operation_operand == null) ? "self" : Integer.toString(operation_operand),
				test_divisor, monkey_true, monkey_false);
		}
		
		public String printQueue() {
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("[monkey %d]: ", monkey_num));
			for (Iterator<Item> iter = queue.iterator() ; iter.hasNext() ; ) {
				sb.append(iter.next().worry_level);
				sb.append(' ');
			}
			
			return sb.toString();
		}
		
		
		// Monkey 1:
  		// Starting items: 78, 74, 88, 89, 50
  		// Operation: new = old * 11
  		// Test: divisible by 5
    	// If true: throw to monkey 3
    	// If false: throw to monkey 5
		public static Monkey create(BufferedReader br) throws Exception {
			String line;
			int pos1;
			
			// Read monkey number line.
			line = br.readLine();
			pos1 = line.indexOf(' ');
			int monkey_num = Integer.parseInt(line.substring(pos1 + 1, line.length() - 1));
		
			// Read in starting items.
			line = br.readLine();
			pos1 = line.indexOf(':');
			line = line.substring(pos1 + 2);
			StringTokenizer st = new StringTokenizer(line, ",");
			List<Item> queue = new ArrayList<Item>();
			while (st.hasMoreTokens()) {
				queue.add(new Item(new BigAssNumber(st.nextToken().trim())));
			}
		
			// Read in operation.
			line = br.readLine();
			pos1 = line.indexOf("new = old ");
			pos1 += 10;
			char op_operator = line.charAt(pos1);
			String sz_operand = line.substring(pos1 + 2);
			Integer op_operand = null;
			if (!sz_operand.equals("old")) {
				op_operand = Integer.parseInt(sz_operand);
			}
		
			// Read test condition.
			line = br.readLine(); 
			pos1 = line.indexOf("divisible by ");
			pos1 += 13;
			int test_divisor = Integer.parseInt(line.substring(pos1));
		
			// Read true throw.
			line = br.readLine(); 
			pos1 = line.indexOf("throw to monkey ");
			pos1 += 16;
			int throw_true = Integer.parseInt(line.substring(pos1));
		
			// Read false throw.
			line = br.readLine(); 
			pos1 = line.indexOf("throw to monkey ");
			pos1 += 16;
			int throw_false = Integer.parseInt(line.substring(pos1));
			
			Monkey monkey = new Monkey(monkey_num, queue, op_operator, op_operand, test_divisor, throw_true, throw_false);
			System.out.format("  imported: %s\n", monkey);
			return monkey;
		}
	}
	
	
	private static List<Monkey> monkeys = new ArrayList<Monkey>();

  
  public static void main(String[] args) throws Exception { 
  	System.out.format("Day 11 Mutha Fuckah:\n\n");
	
	// Open input file.
   	File file = new File("day11.input");
   	BufferedReader br = new BufferedReader(new FileReader(file));
   	
   	// Importing monkeys loop.
   	System.out.format("Importing: monkeys start...\n");
   	while (true) {
		// Read in a monkey over multiple lines.
		Monkey monkey = Monkey.create(br);
		monkeys.add(monkey);
		System.out.format("  monkey-list newsize=%d\n", monkeys.size());
		
		// Read blank line delimiter, or break out.
		String blankline = br.readLine(); 
		if (blankline == null) {
			System.out.format("Importing: found EOF\n");
			break;
		}
		if (blankline.length() != 0) {
			throw new Exception(String.format("shit: expected monkey delimiter, got \"%s\"", blankline));
		}
	}
	
	
	// Start iterating through rounds.
	for (int round = 0; round < 10000 ; round++) {
		// Iterate through the monkeys
		for (Iterator<Monkey> iter = monkeys.iterator() ; iter.hasNext() ; ) {
			Monkey monkey = iter.next();
			
			// Iterate through the queue.
			while (!monkey.queue.isEmpty()) {
			
				monkey.inspection_count++;
				Item item = monkey.queue.remove(0);
				BigAssNumber prev_worry_level = item.worry_level;
				
				
				BigAssNumber operation_param;
				if (monkey.operation_operand == null) {
					operation_param = item.worry_level;
				} else {
					operation_param = BigAssNumber.valueOf(monkey.operation_operand);
				}
				
				//System.out.format("Processing: monkey %d, item=%d, op=%c, op_param=%d\n", 
				//	monkey.monkey_num, item.worry_level, monkey.operation_operator, operation_param);
				
				try {
				
					// Change the worry level based on operation.
					switch (monkey.operation_operator) {
						case '+':
							item.worry_level = item.worry_level.add(operation_param);
							break;
						case '*':
							item.worry_level = item.worry_level.multiply(operation_param);
							break;
						default:
							throw new Exception(String.format("unexpected operation, got \"%c\"", monkey.operation_operator));
					}
				} catch (Exception e) {
					throw new Exception(String.format("shit: exception, round=%d, monkey=%d", round, monkey.monkey_num), e);
				}
				
				//System.out.format("Processing: monkey %d, item (%d) changed to (%d), op=%c, op_param=%d (%s)\n", 
				//	monkey.monkey_num, prev_worry_level, item.worry_level, monkey.operation_operator, operation_param, monkey.operation_operand);
				
				// Divide it by 3. PART 1 ONLY
				//item.worry_level = item.worry_level.divide(3);
				//System.out.format("Processing: monkey %d, item changed to %d\n", monkey.monkey_num, item.worry_level);
				
				// Run the test.
				boolean run_test = item.worry_level.divisible(monkey.test_divisor);
				
				// throw it to the correct monkey.
				int throw_index = run_test ? monkey.monkey_true : monkey.monkey_false;
				//System.out.format("Processing: monkey %d, item (%d) thrown to monkey %d\n", monkey.monkey_num, item.worry_level, throw_index);
				monkeys.get(throw_index).queue.add(item);
			}
			
			
			
			//System.out.format("Processing: round %d, monkey %d COMPLETE\n", round, monkey.monkey_num);
			
		}
		
		/*System.out.format("Processing: round %d ALL COMPLETE\n\n", round);
		for (Iterator<Monkey> iter2 = monkeys.iterator() ; iter2.hasNext() ; ) {
			Monkey mon = iter2.next();
			System.out.format("  %s\n", mon.printQueue());
		} */
		
		if ((((round + 1)  % 1000) == 0) || (round < 20)) {
			System.out.format("Processing: round %d ALL COMPLETE\n\n", round);
			for (Iterator<Monkey> iter2 = monkeys.iterator() ; iter2.hasNext() ; ) {
				Monkey mon = iter2.next();
				System.out.format(" Monkey %d inspected %d\n", mon.monkey_num, mon.inspection_count);
			}
		}
	}
	

	// Compute monkey business.
    long biggest1 = 0;
    long biggest2 = 0;

    for(int m = 0; m < monkeys.size(); m++){
    	Monkey monkey  = monkeys.get(m);
    	System.out.format("Analyzing: monkey %d, inspection_count=%d\n", monkey.monkey_num, monkey.inspection_count);
    
		if (monkey.inspection_count > biggest1){
			biggest2 = biggest1;
			biggest1 = monkey.inspection_count;
		} else if (monkey.inspection_count > biggest2){
			biggest2 = monkey.inspection_count;   
		}
    }
    
    long business = (biggest1 * biggest2); 
	System.out.format("Importing: monkeys end with size of %d, business=%d (%d, %d)\n", monkeys.size(), business, biggest1, biggest2);
  }
}
