import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.xyz.temp.fundamentals.ST;
import org.xyz.temp.graph.algos.Digraph;
import org.xyz.temp.stdlib.StdOut;


public class HyperCubeGraph {
	private ST<String, Integer> st; // string -> index
	private String[] keys; // index -> string
	private Digraph G;

	public HyperCubeGraph(int dim, String set) {
		int card_powerset = (int) Math.pow(2, dim);
		G = new Digraph(card_powerset);
		Map<String , Integer> resultMap = new HashMap<String, Integer>(); 
		generateGraph(G, resultMap, dim, set);
	}

	public boolean contains(String s) {
		return st.contains(s);
	}

	public int index(String s) {
		return st.get(s);
	}

	public String name(int v) {
		return keys[v];
	}

	public Digraph G() {
		return G;
	}


	/**
	 * This will generate the hypercube graph
	 * @param set 
	 * 
	 * @param st
	 */
	public void generateGraph(Digraph target, Map<String, Integer> resultMap,
			int dim, String set) {
		st = new ST<String, Integer>();
		// Vertices are created, and they are included in the resultmap as their
		// bitstring representation
		int order = target.V();
		LinkedList<Integer> vertices = new LinkedList<Integer>();
		for (int i = 0; i < order; i++) {
			vertices.add(i);
			// target.addVertex(newVertex);
			if (resultMap != null) {
				String s = "";
				for (int j = 0; j < dim; j++) {
					if(((1<<j)&i) != 0)
					{
						s = s+set.charAt(j);
					}
				}
				if(s.equalsIgnoreCase(""))
				{
					s = "*";
				}
				resultMap.put(s, i);
				// First pass builds the index by reading strings to associate
				// distinct strings with an index
				st.put(s, i);
			}
		}

		System.out.println("Result Map: "+resultMap);
		
		// inverted index to get string keys in an aray
		keys = new String[st.size()];
		for (String name : st.keys()) {
			keys[st.get(name)] = name;
		}

		// Two vertices will have an edge if their bitstrings differ by exactly
		// 1 element
		for (int i = 0; i < order; i++) {
			for (int j = i + 1; j < order; j++) {
				for (int z = 0; z < dim; z++) {
					if ((j ^ i) == (1 << z)) {
						target.addEdge(vertices.get(i), vertices.get(j));
						break;
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		HyperCubeGraph hg = new HyperCubeGraph(4, "ABCD");
		Digraph G = hg.G();
		StdOut.println(G);
		StdOut.print("ST: ");
		System.out.println(hg.st);
		StdOut.print("Keys:   ");
		for (int i = 0; i < hg.keys.length; i++) {
			System.out.print(hg.keys[i]+ " , ");
		}
	}
}
