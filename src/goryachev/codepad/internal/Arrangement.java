// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.common.util.CList;
import goryachev.common.util.JW;


/// Represents the arrangment of the visible cells,
/// as well as a number of paragraph before and after,
/// which form the sliding window.
/// The sliding window enables better scrolling experience
/// which avoids sudden jumps when large paragraphs appear in the view. 
public class Arrangement
{
	private record Row(int index, int cellIndex) { }
	
	private final int availableCols;
	private final int availableRows;
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
	private final CList<Row> rows = new CList<>();
	
	
	public Arrangement
	(
		int availableCols,
		int availableRows,
		int wrapLimit,
		double canvasWidth, 
		double canvasHeight, 
		double hsbHeight, 
		double vsbWidth
	)
	{
		this.availableCols = availableCols;
		this.availableRows = availableRows;
		this.wrapLimit = wrapLimit;
		this.canvasWidth = canvasWidth;
		this.canvasHeight = canvasHeight;
		this.hsbHeight = hsbHeight;
		this.vsbWidth = vsbWidth;
	}
	
	
	@Override
	public String toString()
	{
		return new JW("Arrangement").
			value("viewCols", availableCols).
			value("wrapLimit", wrapLimit).
			value("lastColumn", lastColumn).
			value("topIndex", topIndex).
			value("bottomIndex", bottomIndex).
			value("topRowCount", topRowCount).
			value("slidingWindowRowCount", slidingWindowRowCount).
			toString();
	}
	
	
	public void addRow(int index, int cellIndex)
	{
		rows.add(new Row(index, cellIndex));
	}


	public int rowCount()
	{
		return rows.size();
	}
	
	
	public int availableColumns()
	{
		return availableCols;
	}
	
	
	public int availableRows()
	{
		return availableRows;
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


	// index at the specified view row, or -1 if beyond model size
	public int indexAtRow(int ix)
	{
		if((ix >= 0) && (ix < rows.size()))
		{
			return rows.get(ix).index();
		}
		return -1;
	}


	public int cellIndexAtRow(int ix)
	{
		return rows.get(ix).cellIndex();
	}
	
	
	/// Finds wrap coordinates for the row relative to the top of the sliding window.
	// TODO perhaps replace with getrow, getcolumn?
	public CellPos cellPosAtRow(int row)
	{		
		if(row >= rowCount())
		{
			row = rowCount() - 1;
		}
	
		if(row < 0)
		{
			row = 0;
		}

		Row r = rows.get(row);
		return new CellPos(r.index(), r.cellIndex());
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
	
	
	/// Returns the `RelativePosition` for the purposes of scrolling to visible.
	public RelativePosition getRelativePosition(int index, int cellIndex)
	{
		int viewRows = availableRows();
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
			int d = index - r.index();
			if(d < 0)
			{
				return RelativePosition.ABOVE;
			}
			else if(d == 0)
			{
				if(cellIndex < r.cellIndex())
				{
					return RelativePosition.ABOVE;
				}
			}
			
			int ix = indexAtRow(availableRows - 1);
			if(ix < 0)
			{
				return RelativePosition.VISIBLE;
			}
			
			d = index - ix; 
			if(d > 0)
			{
				return RelativePosition.BELOW;
			}
			else if(d == 0)
			{
				// FIX also wrong, need to check the last column
				if(r.cellIndex() >= (cellIndex + availableCols))
				{
					return RelativePosition.BELOW;
				}
			}
		}
		else
		{
			// not wrapped
			r = rows.get(0);
			int x = cellIndex - r.cellIndex(); 
			int y = index - r.index();
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
			else if(x > availableCols)
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
