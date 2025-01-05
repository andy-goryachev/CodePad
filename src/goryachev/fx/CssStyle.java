// Copyright © 2016-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx;
import goryachev.common.util.CKit;
import goryachev.common.util.FH;
import goryachev.fx.internal.CssLoader;
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
			name = CKit.resolveStaticFinalFieldName(this, s, "_");
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
}
