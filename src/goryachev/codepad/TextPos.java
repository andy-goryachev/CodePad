// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.common.util.FH;


/**
 * Position within the text, characterized by the two values:
 * <pre>
 * - paragraph index
 * - cell index which corresponds to the insertion point
 * </pre>
 */
public final class TextPos
	implements Comparable<TextPos>
{
	public static final TextPos ZERO = new TextPos(0, 0);
	
	private final int index;
	private final int cix;


	private TextPos(int index, int cellIndex)
	{
		this.index = index;
		this.cix = cellIndex;
	}
	
	
	/**
	 * Creates the TextPos at the specified index and cell index.
	 */
	public static TextPos of(int index, int cellIndex)
	{
		return new TextPos(index, cellIndex);
	}


	/**
	 * Returns 0-based paragraph index in the model.
	 */
	public int index()
	{
		return index;
	}


	/**
	 * Returns 0-based cell index.
	 */
	public int cellIndex()
	{
		return cix;
	}
	
	
	/**
	 * Returns 1-based line number, equaling to {@link #index} + 1.
	 */
	public int getLineNumber()
	{
		return index + 1;
	}
	
	
	/**
	 * Returns 1-based visual column number, equaling to {@link #offset} + 1.
	 */
	public int getColumn()
	{
		return cix + 1;
	}


	@Override
	public boolean equals(Object x)
	{
		if(x == this)
		{
			return true;
		}
		else if(x instanceof TextPos p)
		{
			return (index == p.index) && (cix == p.cix);
		}
		return false;
	}


	@Override
	public int hashCode()
	{
		int h = FH.hash(TextPos.class);
		h = FH.hash(h, index);
		return FH.hash(h, cix);
	}


	@Override
	public int compareTo(TextPos p)
	{
		int d = index - p.index;
		if(d == 0)
		{
			return cix - p.cix;
		}
		return d;
	}


	@Override
	public String toString()
	{
		return "TextPos{index=" + index + ", offset=" + cix + "}";
	}
	
	
	/**
	 * Returns a value > 0 if the position at (ix, cix) is before this TextPos,
	 * == 0 if the position is the same,
	 * < 0 if the position at (ix, cix) is after this TextPos.
	 */
	public int compareTo(int ix, int cellIndex)
	{
		if(index < ix)
		{
			return -1;
		}
		else if(index == ix)
		{
			return cix - cellIndex;
		}
		return 1;
	}
}
