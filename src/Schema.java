import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


class incorrectFieldType extends Exception
{
	
}

class incorrectDimensionType extends Exception
{
	
}


class Field
{
	String field_name;
	String field_type;
	
	Field(String name,String type) throws incorrectFieldType
	{
		if (!isValid(type)) throw new incorrectFieldType();
		field_name=name;
		field_type=type;
	}
	
	public boolean isValid(String type)
	{
		return (type.equalsIgnoreCase("Numeric") || type.equalsIgnoreCase("Date")|| type.equalsIgnoreCase("String") || type.equalsIgnoreCase("Time"));
	}
	
	public String getName()
	{
		return field_name;
	}
	
	public String getType()
	{
		return field_type;
	}
	
}


class Dimension
{
	protected LinkedList<Field> allFields;
	protected String D_name;
	protected String D_type;
	protected int no_of_fields;
	
	public String getName()
	{
		return D_name;
	}
	public boolean isHierarchial()
	{
		return D_type.equalsIgnoreCase("hierarchial");
	}
	public int no_of_fields()
	{
		return no_of_fields;
	}
	public boolean isValid(String type)
	{
		return (type.equalsIgnoreCase("Simple")||type.equalsIgnoreCase("Hierarchial")||type.equalsIgnoreCase("NonHierarchial"));
	}
	
	Dimension(String name,String type) throws incorrectDimensionType
	{
		if(!isValid(type))
		{
			throw new incorrectDimensionType();
		}
		D_name=name;
		D_type=type;
		no_of_fields=0;
		allFields=new LinkedList<Field>();
	}
	
	public void addField(Field field)
	{
		if(D_type.equalsIgnoreCase("hierarchial"))
		no_of_fields++;
		else no_of_fields=1;
		allFields.add(field);
	}
	
	public LinkedList<Field> getFieldList()
	{
		return allFields;
	}
}

class Measure
{
	
	protected String a_function;
	protected String m_name;
	protected String cf;
	
	Measure(String name,String function,String col_fam)
	{
		m_name=name;
		a_function=function;
		cf=col_fam;
	}
	
	public String cf(){return cf;}
	public String m_name(){return m_name;}
	public String a_function(){return a_function;}
}

public class Schema {
	
	ArrayList<Dimension> allDimensions;
	ArrayList<Measure> allMeasures;
	int no_of_dimensions;
	int no_of_measures;
	
	
	public int no_of_dimensions()
	{
		return no_of_dimensions;
	}
	
	public int no_of_measures()
	{
		return no_of_measures;
	}
	Schema()
	{
		allDimensions= new ArrayList<Dimension>();
		allMeasures=new ArrayList<Measure>();
		no_of_dimensions=0;
		no_of_measures=0;
	}
	
	public void addDimension(Dimension dimension)
	{
		allDimensions.add(dimension);
		no_of_dimensions++;
	}
	
	public void addMeasure(Measure measure)
	{
		allMeasures.add(measure);
		no_of_measures++;
	}
	
	public ArrayList<Dimension> getDimensions()
	{
		return allDimensions;
	}
	public ArrayList<Measure> getMeasures()
	{
		return allMeasures;
	}
	

}
