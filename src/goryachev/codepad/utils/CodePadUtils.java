// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.utils;
import goryachev.fx.FX;
import javafx.scene.paint.Color;


/**
 * CodePad Utilities.
 */
public class CodePadUtils
{
	private static final double EPSILON = 1e-9;
	
	
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


	public static double fromScrollBarValue(double val, double visible, double max)
	{
		return val * (max - visible);
	}

	
	public static double toScrollBarValue(double value, double visible, double max)
	{
		if(Math.abs(max - visible) < EPSILON)
		{
			return 0.0;
		}
		else
		{
			return value / (max - visible);
		}
	}
}
