// Copyright © 2016-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import javafx.scene.Node;


/**
 * CSS Style.
 * 
 * Usage example:
 * <pre>
 * public static final CssStyle EXAMPLE = new CssStyle();
 * ...
 * {
 *     Pane pane = new Pane();
 *     EXAMPLE.set(pane);
 * }
 * <pre>
 */
public class CssStyle
{
	private Object name;
	
	
	public CssStyle()
	{
		name = new Throwable().getStackTrace()[1];
	}
	
	
	public String getName()
	{
		if(name instanceof StackTraceElement s)
		{
			name = resolveName(s);
		}
		return name.toString();
	}
	
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	
	public void set(Node n)
	{
		n.getStyleClass().add(getName());
	}
	
	
	private String resolveName(StackTraceElement st)
	{
		String className = st.getClassName();
		try
		{
			Class c = Class.forName(className);
			className = className.replace('.', '_');
			
			Field[] fs = c.getDeclaredFields();
			for(Field f: fs)
			{
				int m = f.getModifiers();
				if(Modifier.isStatic(m) && Modifier.isFinal(m))
				{
					Object v = f.get(null);
					if(v == this)
					{
						return className + '_' + f.getName();
					}
				}
			}
		}
		catch(Exception e)
		{ }
		
		return className + '_' + st.getLineNumber(); 
	}
}
