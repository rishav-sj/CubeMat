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
	LinkedList<Field> allFields;
	String D_name;
	String D_type;
	
	
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
		allFields=new LinkedList<Field>();
	}
	
	public void addField(Field field)
	{
		allFields.add(field);
	}
	
	public LinkedList<Field> getFieldList()
	{
		return allFields;
	}
}

class Measure
{
	String m_name;
	String m_function;
	
	Measure(String name,String function)
	{
		m_name=name;
		m_function=function;
	}
}

public class Schema {
	
	LinkedList<Dimension> allDimensions;
	LinkedList<Measure> allMeasures;
	
	Schema()
	{
		allDimensions= new LinkedList<Dimension>();
		allMeasures=new LinkedList<Measure>();
	}
	
	public void addDimension(Dimension dimension)
	{
		allDimensions.add(dimension);
	}
	
	public void addMeasure(Measure measure)
	{
		allMeasures.add(measure);
	}
	
	public LinkedList<Dimension> getDimensions()
	{
		return allDimensions;
	}
	public LinkedList<Measure> getMeasures()
	{
		return allMeasures;
	}
	

}
