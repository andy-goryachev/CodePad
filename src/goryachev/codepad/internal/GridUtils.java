// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;


/**
 * GridUtils.
 */
public class GridUtils
{
	private static final double EPSILON = 0.00001;
	
	
	public static boolean notClose(double a, double b)
	{
		return Math.abs(a - b) >= EPSILON;
	}
}
