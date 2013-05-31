import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class Parser
{
	
	public static Schema getSchemaFromXMLFile(String xmlFileName) throws ParserConfigurationException, SAXException, IOException, incorrectDimensionType, incorrectFieldType
	{
		File xmlFile = new File(xmlFileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc =db.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		Schema finalSchema= new Schema();
		
		NodeList dList = doc.getElementsByTagName("dimension");
		int l=dList.getLength();
		System.out.println("l="+l);
		for(int i=0;i<l;i++)
		{
	
			Node dim = dList.item(i);
			Element e= (Element) dim;
			String dname=e.getElementsByTagName("D_name").item(0).getTextContent();
			String dtype=e.getElementsByTagName("D_type").item(0).getTextContent();
			Dimension D= new Dimension(dname, dtype);
			NodeList fList = e.getElementsByTagName("field");
			int fl=fList.getLength();
			for (int j=0;j<fl;j++)
			{
				String flname= ((Element)fList.item(j)).getElementsByTagName("name").item(0).getTextContent();
				String fltype= ((Element)fList.item(j)).getElementsByTagName("type").item(0).getTextContent();
				Field f= new Field(flname, fltype);
				D.addField(f);
			}
			finalSchema.addDimension(D);
			
		}
		
		NodeList mList= doc.getElementsByTagName("measure");
		int ml=mList.getLength();
		
		for (int k=0;k<ml;k++)
		{
			Element em = (Element)mList.item(k);
			String mname = em.getElementsByTagName("name").item(0).getTextContent();
			String mfun = em.getElementsByTagName("function").item(0).getTextContent();
			Measure M = new Measure(mname, mfun);
			finalSchema.addMeasure(M);
		}
		return finalSchema;
	}
}