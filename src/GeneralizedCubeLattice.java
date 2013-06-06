import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class GeneralizedCubeLattice {
	
	protected ST<String, Integer> st; // string -> index
	protected String[] keys; // index -> string
	protected Digraph G;
	protected Map<Integer, List<Integer>> TupleMap;//Index->Tuple
	protected Map<List<Integer>,Integer> ReverseTupleMap;//Tuple->Index
	private static int noOfVertices = 0;
	
	private static Schema schema;
	
	
	public int order(){return noOfVertices;}
	
	public List<Integer> entryAt(int index)
	{
		return TupleMap.get(index);
	}
	public GeneralizedCubeLattice(String xmlFileName) throws ParserConfigurationException, SAXException, IOException, incorrectDimensionType, incorrectFieldType
	{
		
		schema=Parser.getSchemaFromXMLFile(xmlFileName);
		int prod=1;
		ArrayList<Dimension> allDim=schema.getDimensions();
		
		for (Dimension dim:allDim)
		{
			prod*=1+dim.no_of_fields();
		}
		
		noOfVertices=prod;
		
		
	}
	

	public GeneralizedCubeLattice(Schema inputSchema) 
	{		
		//Calculate Number of Vertices
		schema=inputSchema;
		int prod=1;
		ArrayList<Dimension> allDim=schema.getDimensions();
		
		for (Dimension dim:allDim)
		{
			prod*=1+dim.no_of_fields();
		}
		
		noOfVertices=prod;
	}
	
	public void buildCubeLattice()
	{
		G=new Digraph(noOfVertices);
		st=new ST<String,Integer>();
		
		generateAnnotatedLatticeGraph(G, st);
	}
	
	
	public void generateAnnotatedLatticeGraph(Digraph target,ST<String,Integer>st)
	{
		//Generate all possible tuples
		TupleMap = new LinkedHashMap<Integer,List<Integer>>();
		ReverseTupleMap = new LinkedHashMap<List<Integer>,Integer>();
		int n=schema.no_of_dimensions();
		int lastIndex=0; //To generate map
		List<Integer> Bottom = new ArrayList<Integer>(); //The tuple (*,*....*)
		
		
		for (int i=0;i<n;i++)
		{
			Bottom.add(0);
		}
		TupleMap.put(0, Bottom);
		ReverseTupleMap.put(Bottom, 0);
		for(int j=0;j<=lastIndex;j++)
		{
				List<Integer> temp=TupleMap.get(j);
				//Look at every dimension for a possible increment
				
				for(int k=0;k<n;k++)
				{
					if(temp.get(k)<schema.getDimensions().get(k).no_of_fields())
					{
						List<Integer> copyTemp = new ArrayList<Integer>();
						for(int y=0;y<n;y++) copyTemp.add(y, temp.get(y));
						copyTemp.set(k, copyTemp.get(k)+1);
						
						if(!ReverseTupleMap.containsKey(copyTemp))
						{
							lastIndex++;
							TupleMap.put(lastIndex, copyTemp);
							ReverseTupleMap.put(copyTemp,lastIndex);
							
							G.addEdge(j, lastIndex);
							
						}
						else
						{
							G.addEdge(j, ReverseTupleMap.get(copyTemp));
						}
					}
				}
				
		}
		
		
		
	}
	
	
	public Digraph getDigraph()
	{
		return G;
	}
	

}
