// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad;


/**
 * Selection Range.
 */
public final class SelectionRange
{
    private final TextPos min;
    private final TextPos max;
    private final boolean caretAtMin;
    
    
    public SelectionRange(TextPos min, TextPos max, boolean caretAtMin)
    {
    	this.min = min;
    	this.max = max;
    	this.caretAtMin = caretAtMin;
    }
    
    
    public TextPos getAnchor()
    {
    	return caretAtMin ? max : min;
    }
    
    
    public TextPos getCaret()
    {
    	return caretAtMin ? min : max;
    }
    
    
    public TextPos getMax()
    {
    	return max;
    }
    
    
    public TextPos getMin()
    {
    	return min;
    }
    
    
    public boolean isCaretLine(int ix)
    {
    	TextPos p = getCaret();
    	if(p == null)
    	{
    		return false;
    	}
    	return p.index() == ix;
    }


	public boolean contains(int ix, int cix)
	{
		if(min == null || max == null)
		{
			return false;
		}
		return
			(min.compareTo(ix, cix) <= 0) &&
			(max.compareTo(ix, cix) > 0);
	}
}
