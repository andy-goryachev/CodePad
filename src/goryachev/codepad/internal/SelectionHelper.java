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
	private static final int SELECTED = 0x0002;
	
	
	public static int getFlags(CellGrid vflow, SelectionRange sel, int ix, int cix)
	{
		if(sel == null)
		{
			return NONE;
		}
		else if(ix < 0)
		{
			return NONE;
		}
		
		int flags = NONE;
		
		if(sel.isCaretLine(ix))
		{
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
	
	
	public static boolean isCaretLine(SelectionRange sel, int ix)
	{
		if(sel != null)
		{
			if(sel.isCaretLine(ix))
			{
				return true;
			}
		}
		return false;
	}


	public static boolean isCaret(int flags)
	{
		return (flags & CARET) != 0;
	}
	
	
	public static boolean isSelected(int flags)
	{
		return (flags & SELECTED) != 0;
	}
}
