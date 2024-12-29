// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.common.util.FH;


/**
 * Position within the text, corresponding to the insertion point between the characters.
 */
public final class TextPos
	implements Comparable<TextPos>
{
	public static final TextPos ZERO = new TextPos(0, 0, true);
	
	private final int index;
	private final int offset;
	private final boolean leading;


	public TextPos(int index, int offset, boolean leading)
	{
		this.index = index;
		this.offset = offset;
		this.leading = leading;
	}
	
	
	public static TextPos of(int index, int offset)
	{
		return new TextPos(index, offset, true);
	}


	/**
	 * Returns 0-based model index.
	 */
	public int index()
	{
		return index;
	}


	/**
	 * Returns 0-based character offset.
	 */
	public int offset()
	{
		// TODO account for leading
		return offset;
	}
	
	
	public boolean isLeading()
	{
		return leading;
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
		return offset + 1;
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
			return (index == p.index) && (offset == p.offset);
		}
		return false;
	}


	@Override
	public int hashCode()
	{
		int h = FH.hash(TextPos.class);
		h = FH.hash(h, index);
		return FH.hash(h, offset);
	}


	@Override
	public int compareTo(TextPos p)
	{
		int d = index - p.index;
		if(d == 0)
		{
			return offset - p.offset;
		}
		return d;
	}


	@Override
	public String toString()
	{
		return "TextPos{index=" + index + ", offset=" + offset + "}";
	}
	
	
	/**
	 * Returns a value > 0 if the position at (ix, cix) is before this TextPos,
	 * == 0 if the position is the same
	 * < 0 if the position at (ix, cix) is after this TextPos.
	 */
	public int compareTo(int ix, int cix)
	{
		if(index < ix)
		{
			return -1;
		}
		else if(index == ix)
		{
			return offset - cix;
		}
		return 1;
	}
}
