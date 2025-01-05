// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import goryachev.common.util.CKit;


/**
 * Function Identifier which corresponds to a method in the Control behavior.
 */
public final class Func
{
	private Object name;
	
	
	public Func()
	{
		name = new Throwable().getStackTrace()[1];
	}
	
	
	@Override
	public String toString()
	{
		if(name instanceof StackTraceElement s)
		{
			name = CKit.resolveStaticFinalFieldName(this, s, ":");
		}
		return name.toString();
	}
}
