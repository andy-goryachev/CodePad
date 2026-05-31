// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.TextPos;
import goryachev.common.util.CList;


/// Represents the configuration of visible cells, parameters of paragraphs
/// immediately preceing and following the visible area, and the visibility of
/// the scroll bars.
public class Arrangement
{
	record Row(int index, int cellIndex) { }
	
	private final int viewCols;
	private final int wrapLimit;
	private final double canvasWidth;
	private final double canvasHeight;
	private final double hsbHeight;
	private final double vsbWidth;
	private int lastColumn;
	private int topIndex;
	private int bottomIndex;
	private int slidingWindowRowCount;
	private int topRowCount;
	// TODO replace with elastic int array [index, cellIndex]
	private final CList<Row> rows = new CList<>();
	
	
	public Arrangement
	(
		int viewCols,
		int wrapLimit,
		double canvasWidth, 
		double canvasHeight, 
		double hsbHeight, 
		double vsbWidth
	)
	{
		this.viewCols = viewCols;
		this.wrapLimit = wrapLimit;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.hsbHeight = hsbHeight;
		this.vsbWidth = vsbWidth;
	}
	
	
	public void addRow(int index, int cellIndex)
	{
		rows.add(new Row(index, cellIndex));
	}


	public int viewPortRowCount()
	{
		return rows.size();
	}
	
	
	public int viewPortColumnCount()
	{
		return viewCols;
	}


	public double getHSBHeight()
	{
		return hsbHeight;
	}


	public double getVSBWidth()
	{
		return vsbWidth;
	}


	public int wrapLimit()
	{
		return wrapLimit;
	}


	public int indexAtRow(int ix)
	{
		return rows.get(ix).index();
	}


	public int cellIndexAtRow(int ix)
	{
		return rows.get(ix).cellIndex();
	}
	
	
	/// Finds wrap coordinates for the row relative to the top of the sliding window.
	public TextPos textPosAtRow(int row)
	{		
		if(row >= viewPortRowCount())
		{
			row = viewPortRowCount() - 1;
		}
	
		if(row < 0)
		{
			row = 0;
		}

		Row r = rows.get(row);
		return new TextPos(r.index(), r.cellIndex());
	}


	public void setLastColumn(int v)
	{
		lastColumn = v;
	}
	
	
	public int lastColumn()
	{
		return lastColumn;
	}
	
	
	public double canvasWidth()
	{
		return canvasWidth;
	}
	
	
	public double canvasHeight()
	{
		return canvasHeight;
	}
	
	
	/// Model index at the top of the sliding window.
	public int getTopIndex()
	{
		return topIndex;
	}


	/// Model index after the last paragraph of the sliding window.
	public int getBottomIndex()
	{
		return bottomIndex;
	}


	/// Number of rows within the sliding window.
	public int getSlidingWindowRowCount()
	{
		return slidingWindowRowCount;
	}


	/// Number of rows counted from the top of the sliding window to the first visible row.
	public int getTopRowCount()
	{
		return topRowCount;
	}


	public void setSlidingWindow(int topIndex, int bottomIndex, int slidingWindowRowCount, int topRowCount)
	{
		this.topIndex = topIndex;
		this.bottomIndex = bottomIndex;
		this.slidingWindowRowCount = slidingWindowRowCount;
		this.topRowCount = topRowCount;
	}
	
	
	private boolean isWrap()
	{
		return wrapLimit > 0;
	}
	
	
	public RelativePosition getRelativePosition(TextPos p)
	{
		int viewRows = viewPortRowCount();
		int last = viewRows - 1;
		if(last < 0)
		{
			return RelativePosition.UNDETERMINED;
		}

		Row r;
		if(isWrap())
		{
			// wrapped
			r = rows.get(0);
			int cix = r.cellIndex();
			if(p.compareTo(r.index(), cix) < 0)
			{
				return RelativePosition.ABOVE;
			}
	
			last = Math.min(last, rows.size() - 1);
			r = rows.get(last);
			cix = r.cellIndex();
			if(p.compareTo(r.index(), cix + viewCols) >= 0)
			{
				return RelativePosition.BELOW;
			}
		}
		else
		{
			// not wrapped
			r = rows.get(0);
			int x = p.cellIndex() - r.cellIndex(); 
			int y = p.index() - r.index();
			if(x < 0)
			{
				if(y < 0)
				{
					return RelativePosition.ABOVE_LEFT;
				}
				else if(y >= viewRows)
				{
					return RelativePosition.BELOW_LEFT;
				}
				return RelativePosition.LEFT;
			}
			else if(x > viewCols)
			{
				if(y < 0)
				{
					return RelativePosition.ABOVE_RIGHT;
				}
				else if(y >= viewRows)
				{
					return RelativePosition.BELOW_RIGHT;
				}
				return RelativePosition.RIGHT;
			}
			else
			{
				if(y < 0)
				{
					return RelativePosition.ABOVE;
				}
				else if(y >= viewRows)
				{
					return RelativePosition.BELOW;
				}
			}
		}
		return RelativePosition.VISIBLE;
	}
}
