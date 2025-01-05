// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.common.util.FH;


/**
 * Position within the text, characterized by the three values:
 * <pre>
 * - paragraph index
 * - cell index which corresponds to the insertion point
 * - leading or trailing bias
 * </pre>
 */
public final class TextPos
	implements Comparable<TextPos>
{
	public static final TextPos ZERO = new TextPos(0, 0, true);
	
	private final int index;
	private final int cix;
	private final boolean leading;


	private TextPos(int index, int cellIndex, boolean leading)
	{
		this.index = index;
		this.cix = cellIndex;
		this.leading = leading;
	}
	
	
	/**
	 * Creates the TextPos at the specified index and cell index and a leading bias.
	 */
	public static TextPos of(int index, int cellIndex)
	{
		return new TextPos(index, cellIndex, true);
	}
	
	
	/**
	 * Creates the TextPos at the specified index and cell index and a trailing bias.
	 */
	public static TextPos trailing(int index, int cellIndex)
	{
		return new TextPos(index, cellIndex, false);
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
	
	
	public boolean isLeading()
	{
		return leading;
	}
	
	
	/**
	 * The index of the cell where caret is painted.
	 * In the insert mode, the caret is drawn as a line at the left edge of the cell (leading=true)
	 * or the right edge (leading=false).
	 * In the overwrite mode, TBD.
	 */
	public int paintCellIndex()
	{
		if(!leading)
		{
			if(cix > 0)
			{
				return cix - 1;
			}
		}
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
		return "TextPos{index=" + index + ", offset=" + cix + ", leading=" + leading + "}";
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
			return paintCellIndex() - cellIndex;
		}
		return 1;
	}
}
