// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;


/**
 * Grid Utilities.
 */
public class GridUtils
{
	private static final double EPSILON = 0.00001;
	
	
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
	
	public static boolean notClose(double a, double b)
	{
		return Math.abs(a - b) >= EPSILON;
	}
}
