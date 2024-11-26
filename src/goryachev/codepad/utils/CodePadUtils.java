// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.utils;
import goryachev.fx.FX;
import javafx.scene.paint.Color;


/**
 * CodePad Utilities.
 */
public class CodePadUtils
{
	public static Color mixColor(Color base, Color added, double fraction)
	{
		if(base == null)
		{
			return added;
		}
		else if(added == null)
		{
			return base;
		}
		
		return FX.mix(base, added, fraction);
	}
}
