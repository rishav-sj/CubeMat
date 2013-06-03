import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.xyz.bigcube.lib.common.CubeConstants;
import org.xyz.temp.fundamentals.RegionST;
import org.xyz.temp.graph.algos.BreadthFirstDirectedPaths;
import org.xyz.temp.graph.algos.Digraph;


public class GeneralizedCubeLattice {
	protected RegionST<Region, Integer> st; // string -> index
	protected Region[] keys; // index -> string
	protected Digraph G;

	// get the info of all dimensions. Size of each dimension.
	// This has to be done from DimensionReader
	// protected static Map<String, List<String>> dimensionsMap = new
	// HashMap<String, List<String>>();
	protected static Map<String, List<String>> dimensionsMap = new LinkedHashMap<String, List<String>>();
	
	protected static Map<String, Integer> attrVsColumnIndexMap = new LinkedHashMap<String, Integer>();

	// This map will maintain sum as key, and values as combinations of subsets
	// whose sum as key
	private static Map<Integer, List<List<Integer>>> combinations = new LinkedHashMap<Integer, List<List<Integer>>>();
	// private static List<Integer> numberList = new ArrayList<Integer>();

	// no of vertices = (pi over i =1 to n)(1+(L suffix i)) where "(L suffix i)"
	// is no of dimensions in each hierarchy
	private static int noOfVertices = 0;

	private static Dimensions dimensions;

	// consructing the dimensionsMap from json object.
	static {
		/*InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(CubeConstants.DIMENSIONS_JSON);*/
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(CubeConstants.DIMENSION_VALUES_JSON);
		try {
			DimensionReader reader = new DimensionReader(is);
			dimensions = (Dimensions) reader.readDimensions(Dimensions.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Dimension dim : dimensions.getDimensions()) {
			System.out.println(dim);
			ArrayList<String> list = (ArrayList<String>) dim.getAttributes();
			list.add(0, "*");
			dimensionsMap.put(dim.getDimensionName(), list);
			ArrayList<Integer> columnList = (ArrayList<Integer>)dim.getColumnPositions();
			if(list.size() != columnList.size()+1)
			{
				throw new RuntimeException("Number of attributes (excluding * ) and it's curresponding column values must be same... ");
			}

			/*
			 * Column indexes related to actual raw table...
			 */
			
			for(int i = 0; i< columnList.size();++i)
			{
				attrVsColumnIndexMap.put(list.get(i+1), columnList.get(i));
			}
		}

		// calculate noOfVertices.
		int prod = 1;
		for (String key : dimensionsMap.keySet()) {
			prod *= (1 + dimensionsMap.get(key).size() - 1);
		}
		noOfVertices = prod;
	}

	public GeneralizedCubeLattice() {
	}

	public void buildCubeLattice() {
		G = new Digraph(noOfVertices);
		st = new RegionST<Region, Integer>();
		generateAnnotatedLatticeGraph(G, st);
	}

	public GeneralizedCubeLattice(int dim) {
		G = new Digraph(dim);
		st = new RegionST<Region, Integer>();
		generateAnnotatedLatticeGraph(G, st);
	}

	public boolean contains(Region s) {
		return st.contains(s);
	}

	public int index(Region s) {
		return st.get(s);
	}

	public Region name(int v) {
		return keys[v];
	}

	public Digraph G() {
		return G;
	}

	/**
	 * This will generate the hypercube graph
	 */
	public void generateAnnotatedLatticeGraph(Digraph target,
			RegionST<Region, Integer> st) {

		// This map maintains the vertex id with tuple
		// example: Vertex Vs ordinates : {0=[0, 0], 1=[1, 0], 2=[0, 1], 3=[2,
		// 0], 4=[1, 1], 5=[0, 2], 6=[3, 0], 7=[2, 1], 8=[1, 2], 9=[0, 3],
		// 10=[3, 1], 11=[2, 2], 12=[1, 3], 13=[3, 2], 14=[2, 3], 15=[3, 3]}

		// Continue from here............................................
		/*
		 * int total = topic.size() + location.size(); for (int k = 0; k <
		 * total; k++) { for (int i = 0; i < topic.size(); i++) { for (int j =
		 * 0; j < location.size(); ++j) { if (i + j == k) { List<Integer> temp =
		 * new ArrayList<Integer>(); temp.add(0, j); temp.add(1, i); Region r =
		 * new Region("<" + location.get(j) + " , " + topic.get(i) + ">");
		 * st.put(r, count++); vertexCount.put(count - 1, temp); } } } }
		 */

		Map<Integer, List<Integer>> vertexCount = new LinkedHashMap<Integer, List<Integer>>();
		List<List<Integer>> inputDimensions = new ArrayList<List<Integer>>();
		for (Map.Entry<String, List<String>> dimension : dimensionsMap
				.entrySet()) {
			List<Integer> temp = new ArrayList<Integer>();
			for (int i = 0; i < dimension.getValue().size(); ++i) {
				temp.add(i);
			}
			inputDimensions.add(temp);
		}

		for (List<Integer> tuple : getAllSumCombinations(inputDimensions, 0)) {
			int tempSum = 0;
			for (Integer ordinate : tuple) {
				tempSum += ordinate;
			}
			if (!combinations.containsKey(tempSum)) {
				List<List<Integer>> temp = new ArrayList<List<Integer>>();
				temp.add(tuple);
				combinations.put(tempSum, temp);
			} else {
				List<List<Integer>> temp = combinations.get(tempSum);
				temp.add(tuple);
				combinations.put(tempSum, temp);
			}
		}

		// This part of the code will construct the cubelattice's vertices id's
		int tempCount = 0;
		StringBuilder stb = new StringBuilder();
		for (int i = 0; i < combinations.size(); ++i) {
			stb.delete(0, stb.length());
			for (List<Integer> tempList : combinations.get(i)) {
				System.out.print(tempList + "     ");
				vertexCount.put(tempCount++, tempList);
				for (int j = 0; j < tempList.size(); j++) {
					int temp = 0;
					for (String key : dimensionsMap.keySet()) {
						if (temp == j) {
							stb.append(dimensionsMap.get(key).get(
									tempList.get(j)));
							break;
						}
						temp++;
					}
					stb.append(",");
				}
				// ToDo need to change the regionID....
				Region r = new Region("<"
						+ stb.toString().substring(0, stb.length() - 1) + ">");
				System.out.println(r);
				st.put(r, tempCount - 1);
				stb.delete(0, stb.length());
			}
		}

		// inverted index to get string keys in an array
		keys = new Region[st.size()];
		for (Region name : st.keys()) {
			keys[st.get(name)] = name;
		}

		// each vertex viewd as 3 ways,
		// 1---> vertex id as count
		// 2---> based on topic and location indices i.e., <*,*> is (0,0) and
		// <state, category> is (2,2)
		// 3 --> symbol view ex: <country,*>
		/*
		 * for (Map.Entry<Integer, List<Integer>> entry :
		 * vertexCount.entrySet()) { Integer vertex = entry.getKey();
		 * List<Integer> tuple = entry.getValue(); int i = tuple.get(0); int j =
		 * tuple.get(1); if (i + 1 != topic.size()) { int adj_i_vertex =
		 * getVertex(vertexCount, i + 1, j); if (adj_i_vertex != -1) {
		 * target.addEdge(vertex, adj_i_vertex); } } if (j + 1 !=
		 * location.size()) { int adj_j_vertex = getVertex(vertexCount, i, j +
		 * 1); if (adj_j_vertex != -1) { target.addEdge(vertex, adj_j_vertex); }
		 * } }
		 */

		for (Map.Entry<Integer, List<Integer>> entry : vertexCount.entrySet()) {
			Integer vertex = entry.getKey();
			List<Integer> tuple = entry.getValue();
			List<Integer> destVertices = getVertex(vertexCount, vertex, tuple);
			for (int i = 0; i < destVertices.size(); ++i) {
				int adj_i_vertex = destVertices.get(i);
				target.addEdge(vertex, adj_i_vertex);
			}
		}
	}

	// generate Batch Area's, this is for Annotated Lattice.
	public void generateBatches() {
	}

	public List<BatchArea> getBatches() {
		// return batches;
		return null;
	}

	public void setBatches(List<BatchArea> batches) {
		// this.batches = batches;

	}

	public Map<String, Integer> getAlgebraicAttributeMap() {
		// return algebraicAttributeMap;
		return null;
	}

	public void setAlgebraicAttributeMap(
			Map<String, Integer> algebraicAttributeMap) {
		// this.algebraicAttributeMap = algebraicAttributeMap;
	}

	/*
	 * protected int getVertex(Map<Integer, List<Integer>> vertexCount, int i,
	 * int j) { for (Map.Entry<Integer, List<Integer>> entry :
	 * vertexCount.entrySet()) { List<Integer> value = entry.getValue(); if
	 * ((value.get(0) == i) && (value.get(1) == j)) { return entry.getKey(); } }
	 * return -1; }
	 */

	protected List<Integer> getVertex(Map<Integer, List<Integer>> vertexCount,
			Integer vertex, List<Integer> sourceTuple) {
		List<Integer> tempResult = new ArrayList<Integer>();
		for (Map.Entry<Integer, List<Integer>> entry : vertexCount.entrySet()) {
			int noOfMatches = 0;
			int noOf1Differences = 0;
			int destVertex = entry.getKey();
			if (destVertex == vertex) {
				continue;
			}
			List<Integer> destTuple = entry.getValue();
			for (int j = 0; j < destTuple.size(); ++j) {
				if (destTuple.get(j) == sourceTuple.get(j)) {
					noOfMatches++;
				} else if ((destTuple.get(j) == (sourceTuple.get(j) + 1))
						|| ((destTuple.get(j) + 1) == sourceTuple.get(j))) {
					noOf1Differences++;
				}
			}
			if (noOf1Differences == 1 && noOfMatches == destTuple.size() - 1) {
				tempResult.add(destVertex);
			}
		}
		return tempResult;
	}

	public Region[] getKeys() {
		return keys;
	}

	public List<String> getQuery_Topic() {
		// return Query_Topic;
		return null;
	}

	public List<String> getLocation() {
		// return Location;
		return null;
	}

	public RegionST<Region, Integer> getSt() {
		return st;
	}

	public void setSt(RegionST<Region, Integer> st) {
		this.st = st;
	}

	public Digraph getG() {
		return G;
	}

	public void setG(Digraph g) {
		G = g;
	}

	public void setKeys(Region[] keys) {
		this.keys = keys;
	}

	/*
	 * public static void main(String[] args) { GeneralizedCubeLattice hg = new
	 * GeneralizedCubeLattice(); hg.buildCubeLattice();
	 * 
	 * Digraph G = hg.G(); StdOut.println(G); StdOut.print("ST: ");
	 * System.out.println(hg.st); StdOut.print("Keys:   "); for (int i = 0; i <
	 * hg.keys.length; i++) { System.out.print(hg.keys[i] + " , "); }
	 * 
	 * 
	 * // System.out.println(getAbsolutePath(hg, hg.getKeys()[6])); }
	 */

	// this method will returns the all the regions which
	// occured in the path of root to current region
	public Region getAbsolutePath(Region toRegion) {
		int toVertex = this.getSt().get(toRegion);
		Region r = new FullRegion();
		Set<String> regionSet = new LinkedHashSet<String>();
		StringBuffer temp = new StringBuffer();
		Digraph G = this.getG();
		BreadthFirstDirectedPaths bfs = new BreadthFirstDirectedPaths(G, 0);
		if (bfs.hasPathTo(toVertex)) {
			for (int x : bfs.pathTo(toVertex)) {
				if (x != 0) {
					String temp1 = this.getKeys()[x].getRegionId();
					StringTokenizer stringTocken = new StringTokenizer(temp1
							.substring(1, temp1.length() - 1).trim(), ",");
					while (stringTocken.hasMoreTokens()) {
						regionSet.add(stringTocken.nextToken().trim());
					}
				}
			}
		}

		for (String str : regionSet) {
			temp.append(str);
			temp.append(" ");
		}
		if (temp.toString().equals(""))
			r.setRegionId("*");
		else
			r.setRegionId(temp.toString());
		return r;
	}

	// this method will returns the all the regions which
	// occured in the path of root to current region
	public Region getAbsolutePath(Region fromRegion, Region toRegion) {
		int fromVertex = this.getSt().get(fromRegion);
		int toVertex = this.getSt().get(toRegion);
		Region r = new FullRegion();
		Set<String> regionSet = new LinkedHashSet<String>();
		StringBuffer temp = new StringBuffer();
		Digraph G = this.getG();
		BreadthFirstDirectedPaths bfs = new BreadthFirstDirectedPaths(G,
				fromVertex);
		if (bfs.hasPathTo(toVertex)) {
			for (int x : bfs.pathTo(toVertex)) {
				if (x != 0) {
					String temp1 = this.getKeys()[x].getRegionId();
					StringTokenizer stringTocken = new StringTokenizer(temp1
							.substring(1, temp1.length() - 1).trim(), ",");
					while (stringTocken.hasMoreTokens()) {
						regionSet.add(stringTocken.nextToken().trim());
					}
				}
			}
		}

		for (String str : regionSet) {
			temp.append(str);
			temp.append(" ");
		}
		if (temp.toString().equals(""))
			r.setRegionId("*");
		else
			r.setRegionId(temp.toString());
		return r;
	}

	public static void main(String[] args) {
		// CubeLattice hg = new CubeLattice();
		// hg.buildCubeLattice();
		GeneralizedCubeLattice genCubeLattice = new GeneralizedCubeLattice();
//		genCubeLattice.buildCubeLattice();

		/*
		 * Digraph G = hg.G(); StdOut.println(G); StdOut.print("ST: ");
		 * System.out.println(hg.st); StdOut.print("Keys:   "); for (int i = 0;
		 * i < hg.keys.length; i++) { System.out.print(hg.keys[i] + " , "); }
		 */

		// System.out.println(getAbsolutePath(hg, hg.getKeys()[6]));

		/*
		 * List<Integer> l1 = new ArrayList<Integer>(); l1.add(0); l1.add(1);
		 * l1.add(2); l1.add(3);
		 * 
		 * List<Integer> l2 = new ArrayList<Integer>(); l2.add(0); l2.add(1);
		 * l2.add(2); l2.add(3);
		 * 
		 * List<Integer> l3 = new ArrayList<Integer>(); l3.add(0); l3.add(1);
		 * l3.add(2); l3.add(3);
		 * 
		 * List<List<Integer>> ll = new ArrayList<List<Integer>>(); ll.add(l1);
		 * ll.add(l2); ll.add(l3);
		 * 
		 * System.out.println(getAllSumCombinations(ll, 0));
		 */
	}

	private static List<List<Integer>> getAllSumCombinations(
			List<List<Integer>> input, int i) {
		if (i == input.size()) {
			// return a list with an empty list
			List<List<Integer>> result = new ArrayList<List<Integer>>();
			result.add(new ArrayList<Integer>());
			return result;
		}

		List<List<Integer>> result = new ArrayList<List<Integer>>();
		List<List<Integer>> recursive = getAllSumCombinations(input, i + 1); // recursive
																				// call

		// for each element of the first list of input
		for (int j = 0; j < input.get(i).size(); j++) {
			// add the element to all combinations obtained for the rest of the
			// lists
			for (int k = 0; k < recursive.size(); k++) {
				// copy a combination from recursive
				List<Integer> newList = new ArrayList<Integer>();
				for (Integer integer : recursive.get(k)) {
					newList.add(integer);
				}
				// add element of the first list
				newList.add(input.get(i).get(j));
				// add new combination to result
				result.add(newList);
			}
		}
		return result;
	}

}
