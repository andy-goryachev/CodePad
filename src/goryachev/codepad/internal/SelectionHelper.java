// Copyright © 2019-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.SelectionRange;


/**
 * Selection Helper.
 */
public class SelectionHelper
{
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


	// TODO remove
	@Deprecated
	public static boolean isCaret(SelectionRange sel, int ix, int cix)
	{
		if(sel != null)
		{
			return sel.isCaret(ix, cix);
		}
		return false;
	}
}
