import static org.junit.Assert.*;

import java.io.IOException;
import java.util.LinkedList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;


public class GeneralizedCubeLatticeTest {

	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException, incorrectDimensionType, incorrectFieldType {
		GeneralizedCubeLattice G= new GeneralizedCubeLattice("/home/user/Documents/CubeMat/doc/sampleschema.xml");
		G.buildCubeLattice();
		Digraph DG= G.getDigraph();
		Iterable<Integer> It=DG.adj(2);
		LinkedList<Integer> L =new LinkedList<Integer>();
		for(int i:It)
		{
			L.add(i);
		}
		
		if(!( (L.size()==3)&&(L.contains(7)&&(L.contains(6))&&(L.contains(4)))))
		fail("Test failed");
		
		
	}

}
