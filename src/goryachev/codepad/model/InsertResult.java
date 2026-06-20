// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;


/// Result of a CodeModelContent.replace() operation.
public class InsertResult
{
	// could be a record
	private final TextPos start;
	private final TextPos end;
	private final int top;
	private final int added;
	private final int bottom;
	private TextPos newEnd;


	public InsertResult(TextPos start, TextPos end, int top, int added, int bottom, TextPos newEnd)
	{
		this.start = start;
		this.end = end;
		this.top = top;
		this.added = added;
		this.bottom = bottom;
		this.newEnd = newEnd;
	}
	

	public TextPos getNewEnd()
	{
		return newEnd;
	}
}
