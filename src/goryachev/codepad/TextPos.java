// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;
import goryachev.common.util.FH;


/// Position within the text, characterized by the two values:
///
/// - paragraph model index
/// - insertion point offset within the paragraph plain text
///
public final class TextPos
	implements Comparable<TextPos>
{
	public static final TextPos ZERO = new TextPos(0, 0);
	
	private final int index;
	private final int offset;


	/// Creates the TextPos at the specified index and cell index.
	public TextPos(int index, int offset)
	{
		this.index = index;
		this.offset = offset;
	}
	

	/// Returns 0-based paragraph index in the model.
	public int index()
	{
		return index;
	}


	/// Returns 1-based line number, equaling to {@link #index} + 1.
	public int getLineNumber()
	{
		return index + 1;
	}
	
	
	/// Returns the insertion point offset within the paragraph plain text.
	public int offset()
	{
		return offset;
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
	
	
	/// Returns a value > 0 if the position at (ix, cix) is before this TextPos,
	/// == 0 if the position is the same,
	/// < 0 if the position at (ix, cix) is after this TextPos.
	///
	public int compareTo(int ix, int cellIndex)
	{
		if(index < ix)
		{
			return -1;
		}
		else if(index == ix)
		{
			return cellIndex - cellIndex;
		}
		return 1;
	}
}
