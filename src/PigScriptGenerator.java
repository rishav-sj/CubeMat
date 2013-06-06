import java.security.acl.Group;
import java.util.List;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;



public class PigScriptGenerator {

	protected Schema dataSchema;
	protected String HBaseDBPath;
	
	
	public PigScriptGenerator(Schema schema, String dbpath)
	{
		dataSchema = schema;
		HBaseDBPath = dbpath;
	}
	
	public String generateScript(String outputFile)
	{
		String script= new String("");
		
		
		script=script.concat("data = load "+ HBaseDBPath+" ");
		script=script.concat("using "+"org.apache.pig.backend.hadoop.hbase.HBaseStorage(");
		script=script.concat("'");
		
		for(Dimension dim: dataSchema.getDimensions())
		{
			if(dim.isHierarchial())
			for(Field field: dim.getFieldList())
			{
				script=script.concat(dim.getName()+":"+field.getName()+",");
			}
			
			else
				
			script=script.concat(dim.getName()+":"+dim.getFieldList().get(0).getName()+",");
			
		}
		
		
		for(Measure measure: dataSchema.getMeasures())
		{
				
			script=script.concat(measure.cf()+":"+measure.m_name()+",");
			
		}
		script=script.substring(0, script.length()-1);//Get rid of last comma
		script=script.concat("')");
		script=script.concat(" as (");
		for(Dimension dim: dataSchema.getDimensions())
		{
			if(dim.isHierarchial())
			for(Field field: dim.getFieldList())
			{
				script=script.concat(field.getName()+",");
			}
			
			else
				
			script=script.concat(dim.getFieldList().get(0).getName()+",");
			
		}
		for(Measure measure: dataSchema.getMeasures())
		{
				
			script=script.concat(measure.m_name()+",");
			
		}
		
		script=script.substring(0, script.length()-1);//Get rid of last comma
		script=script.concat(");\n");
	
		
		int f_no=0; //File number
		GeneralizedCubeLattice GCL = new GeneralizedCubeLattice(dataSchema);
		GCL.buildCubeLattice();
		List<Dimension> allDimensions = dataSchema.getDimensions();

		for(int i=0;i<GCL.order();i++)
		{
			if(i==0)
			{
				script=script.concat("grpd = group data all;\n");
				script=script.concat("aggr = foreach grpd generate group, ");
				for(Measure meas: dataSchema.getMeasures())
				{
					script=script.concat(meas.a_function()+"("+ meas.m_name +"),");
				}
				
				script=script.substring(0,script.length()-1);
				script=script.concat(";\n");
				
				script=script.concat("store aggr into file"+f_no+";\n");
				f_no++;
			}				
			else
			{
				String group=new String("");
				group=group.concat("(");
				List<Integer> tuple = GCL.entryAt(i);
				for (int j=0;j<dataSchema.no_of_dimensions();j++)
				{
					if(tuple.get(j)!=0)
					{
						for(int k=1;k<=tuple.get(j);k++)
						{
							group=group.concat(allDimensions.get(j).getFieldList().get(k-1).getName()+",");
							
						}
					}
				}
				
				group=group.substring(0, group.length()-1);
				group=group.concat(")");
				
				script=script.concat("grpd = group data by "+ group +";\n");
				script=script.concat("aggr = foreach grpd generate group, ");
				for(Measure meas: dataSchema.getMeasures())
				{
					script=script.concat(meas.a_function()+"("+ meas.m_name +"),");
				}
				
				script=script.substring(0,script.length()-1);
				script=script.concat(";\n");
				
				script=script.concat("store aggr into file"+f_no+";\n");
				f_no++;
				
				
				
			}	
			
			
			
		}
		
		return script;
		
	}
	
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, incorrectDimensionType, incorrectFieldType
	{
		PigScriptGenerator G = new PigScriptGenerator(Parser.getSchemaFromXMLFile("/home/user/Documents/CubeMat/doc/sampleschema.xml"),"somepath");
		System.out.println(G.generateScript("anything"));;
	}
}
