// Copyright © 2016-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor;
import goryachev.common.util.Assert;
import goryachev.common.util.FH;


/**
 * A single Selection Segment.
 */
public class SelectionSegment
{
	protected final Marker min;
	protected final Marker max;
	protected final boolean caretAtMin;
	
	
	public SelectionSegment(Marker min, Marker max, boolean caretAtMin)
	{
		Assert.notNull(min, "min");
		Assert.notNull(max, "max");
		Assert.isLessThanOrEqual(min, max, "min", "max");

		this.min = min;
		this.max = max;
		this.caretAtMin = caretAtMin;
	}
	
	
	public SelectionSegment(Marker anchor, Marker caret)
	{
		Assert.notNull(anchor, "anchor");
		Assert.notNull(caret, "caret");
		
		if(anchor.compareTo(caret) <= 0)
		{
			this.min = anchor;
			this.max = caret;
			this.caretAtMin = false;
		}
		else
		{
			this.min = caret;
			this.max = anchor;
			this.caretAtMin = true;	
		}
	}
	
	
	public SelectionSegment copy()
	{
		return new SelectionSegment(min, max, caretAtMin);
	}
	
	
	@Override
	public int hashCode()
	{
		int h = FH.hash(SelectionSegment.class);
		h = FH.hash(h, min);
		h = FH.hash(h, max);
		return FH.hash(h, caretAtMin);
	}
	
	
	@Override
	public boolean equals(Object x)
	{
		if(x == this)
		{
			return true;
		}
		else if(x instanceof SelectionSegment)
		{
			SelectionSegment s = (SelectionSegment)x;
			return
				(caretAtMin == s.caretAtMin) &&
				min.equals(s.min) && 
				max.equals(s.max);
		}
		else
		{
			return false;
		}
	}


	@Override
	public String toString()
	{
		if(min.equals(max))
		{
			return "[" + min + "]";
		}
		
		if(caretAtMin)
		{
			return "[" + min + "^-" + max + "]";
		}
		else
		{
			return "[" + min + "-" + max + "^)";
		}
	}
	
	
	/** returns the anchor position (opposite of caret) */
	public Marker getAnchor()
	{
		return caretAtMin ? max : min;
	}
	
	
	/** returns the caret position */
	public Marker getCaret()
	{
		return caretAtMin ? min : max;
	}
	
	
	public boolean isCaretAtMin()
	{
		return caretAtMin;
	}
	
	
	/** returns a marker which is closer to the beginning of the text */
	public Marker getMin()
	{
		return min;
	}
	
	
	/** returns a marker which is further from the beginning of the text */
	public Marker getMax()
	{
		return max;
	}
	
	
	public int getMinLine()
	{
		return min.getLine();
	}
	
	
	public int getMaxLine()
	{
		return max.getLine();
	}
	
	
	public int getMinCharIndex()
	{
		return min.getCharIndex();
	}
	
	
	public int getMaxCharIndex()
	{
		return max.getCharIndex();
	}
	
	
	public int getCaretLine()
	{
		return getCaret().getLine();
	}
	
	
	public int getCaretCharIndex()
	{
		return getCaret().getCharIndex();
	}


	public boolean contains(Marker p)
	{
		if(p != null)
		{
			if(isEmpty())
			{
				return (p.compareTo(min) == 0);
			}
			
			int st = p.compareTo(min);
			if(st >= 0)
			{
				int en = p.compareTo(max);
				if(en <= 0)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	public boolean contains(int line, int pos)
	{
		if(min.isAfter(line, pos))
		{
			return false;
		}
		else if(max.isAtOrBefore(line, pos))
		{
			return false;
		}
		return true;
	}
	

	public boolean isEmpty()
	{
		return min.equals(max);
	}
	
	
	public boolean isSameLine()
	{
		return min.getLine() == max.getLine();
	}


	/** returns combined segment if two segments overlap, null otherwise. */
	public SelectionSegment merge(SelectionSegment s)
	{
		Marker m0 = s.getMin();
		Marker m1 = s.getMax();
		
		if(contains(m0))
		{
			if(contains(m1))
			{
				// full overlap [  ***  ]
				return this;
			}
			else
			{
				// [ ***]***
				return new SelectionSegment(min, m1, caretAtMin);
			}
		}
		else
		{
			if(contains(m1))
			{
				// ***[***  ]
				return new SelectionSegment(m0, max, caretAtMin);
			}
			else
			{
				// no overlap **** [   ]
				return null;
			}
		}
	}


	/** returns true if two segments do not overlap and this segment is before the specified one */
	public boolean isBefore(SelectionSegment s)
	{
		return getMax().compareTo(s.getMin()) < 0;
	}
	
	
	public boolean overlaps(SelectionSegment s)
	{
		return contains(s.getMin()) || contains(s.getMax());
	}


	public boolean isCaretLine(int line)
	{
		Marker m = getCaret();
		return m.getLine() == line;
	}
	
	
	public boolean isCaret(int line, int pos)
	{
		Marker m = getCaret();
		if(m.getLine() == line)
		{
			if(m.getCharIndex() == pos)
			{
				return true;
			}
		}
		return false;
	}
}
