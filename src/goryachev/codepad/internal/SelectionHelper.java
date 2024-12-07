// Copyright © 2019-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.SelectionRange;


/**
 * Selection Helper.
 */
public class SelectionHelper
{
	private static final int NONE = 0x0000;
	private static final int CARET = 0x0001;
	private static final int CARET_LINE = 0x0002;
	private static final int SELECTED = 0x0004;
	
	
	public static int getFlags(CellGrid vflow, boolean highlightCaretLine, SelectionRange sel, WrapInfo wi, int cix)
	{
		if(sel == null)
		{
			return NONE;
		}
		
		int ix = wi.getIndex();
		if(ix < 0)
		{
			return NONE;
		}
		
		// flags
		
		int flags = NONE;
		
		if(sel.isCaretLine(ix))
		{
			if(highlightCaretLine)
			{
				flags |= CARET_LINE;
			}
			
			if(sel.isCaret(ix, cix))
			{
				flags |= CARET;
			}
		}
		
		if(sel.contains(ix, cix))
		{
			flags |= SELECTED;
		}
		
		return flags;
	}
	
	
//	public static boolean isCaretLine(SelectionRange seg, ScreenRow row)
//	{
//		if(row != null)
//		{
//			if(seg != null)
//			{
//				int line = row.getLineNumber();
//				if(seg.isCaretLine(line))
//				{
//					return true;
//				}
//			}
//		}
//		return false;
//	}


	public static boolean isCaret(int flags)
	{
		return (flags & CARET) != 0;
	}
	
	
	public static boolean isCaretLine(int flags)
	{
		return (flags & CARET_LINE) != 0;
	}
	
	
	public static boolean isSelected(int flags)
	{
		return (flags & SELECTED) != 0;
	}
}
