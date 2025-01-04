// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.TextPos;
import goryachev.common.util.CList;


/**
 * Paragraph Arrangement provides wrapping of paragraphs inside the viewport
 * as well as inside the "sliding window" outside of the viewport.
 * This class facilitates conversion between the screen and model coordinates. 
 */
public class Arrangement
{
	private final WrapCache cache;
	private final int modelSize;
	private final int viewRows;
	private final int viewCols;
	private final int wrapLimit;
	private final int startIndex;
	private final int startCellIndex;
	private final CList<WrapInfo> rows = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private final CList<Integer> offsets = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private int visibleRowCount;
	private int lastViewIndex;
	private int topRows;
	private int bottomRows;
	private int maxCellCount;
	private int bottomIndex;
	private int topIndex;
	
	
	public Arrangement(WrapCache cache, int modelSize, int viewRows, int viewCols, int wrapLimit, int startIndex, int startCellIndex)
	{
		this.cache = cache;
		this.modelSize = modelSize;
		this.viewRows = viewRows;
		this.viewCols = viewCols;
		this.wrapLimit = wrapLimit;
		this.startIndex = startIndex;
		this.startCellIndex = startCellIndex;		
	}
	
	
	@Override
	public String toString()
	{
		return
			"Arrangement{topIndex=" + topIndex +
			", startIndex=" + startIndex +
			", bottomIndex=" + bottomIndex +
			", topRows=" + topRows +
			", visibleRows=" + visibleRowCount +
			", bottomRows=" + bottomRows +
			"}";
	}
	
	
	public int getStartIndex()
	{
		return startIndex;
	}


	public void layoutViewPort(int numRows)
	{
		int rc = 0;
		WrapInfo wi = null;
		int cix = startCellIndex;
		int ix = startIndex;
		
		while((numRows >= 0) && (ix < modelSize))
		{
			if(wi == null)
			{
				wi = cache.getWrapInfo(ix, wrapLimit);
			}
			
			rows.add(wi);
			offsets.add(cix);
			numRows--;
			rc++;
			
			if(isWrap())
			{
				cix += wrapLimit;
				if(cix < wi.getCellCount())
				{
					// wrapping the same paragraph
					continue;
				}
				cix = 0;
			}
			else
			{
				int w = wi.getCellCount();
				if(w > maxCellCount)
				{
					maxCellCount = w;
				}
				cix = startCellIndex;
			}
			
			ix++;
			wi = null;
		}
		
		lastViewIndex = ix;
		visibleRowCount = rc;
	}
	
	
	public int layoutSlidingWindow(int startIndex, int count, boolean forBelow)
	{
		int nrows = 0;
		int ix = startIndex;
		if(!forBelow)
		{
			topIndex = startIndex;
		}
		
		for(int i=0; i<count; i++)
		{
			if(forBelow)
			{
				if(ix >= modelSize)
				{
					break;
				}
			}
			else
			{
				if(ix <= 0)
				{
					break;
				}
			}

			WrapInfo wi = cache.getWrapInfo(ix, wrapLimit);
			nrows += wi.getRowCount();
			ix++;
			
			if(!isWrap())
			{
				int w = wi.getCellCount();
				if(w > maxCellCount)
				{
					maxCellCount = w;
				}
			}
		}
		
		if(forBelow)
		{
			bottomRows = nrows;
			bottomIndex = ix;
		}
		else
		{
			topRows = nrows;
		}
		return nrows;
	}
	
	
	public boolean isHsbNeeded()
	{
		if(isWrap())
		{
			return false;
		}
		return maxCellCount > viewCols;
	}
	
	
	public int maxCellCount()
	{
		return maxCellCount;
	}
	
	
	public int getLastViewIndex()
	{
		return lastViewIndex;
	}


	public WrapInfo wrapInfoAtViewRow(int row)
	{
		if(row < 0)
		{
			return null;
		}
		else if(row >= rows.size())
		{
			return null;
		}
		return rows.get(row);
	}
	
	
	public int indexAtViewRow(int row)
	{
		WrapInfo wi = wrapInfoAtViewRow(row);
		if(wi == null)
		{
			return -1;
		}
		return wi.getIndex();
	}


	public int cellIndexAtViewRow(int row)
	{
		return offsets.get(row);
	}


	/**
	 * Number of full and partial rows visible in the viewport.
	 */
	public int getVisibleRowCount()
	{
		return visibleRowCount;
	}
	
	
	/**
	 * Number of wrapped columns or -1 if unwrapped.
	 */
	public int getWrapLimit()
	{
		return wrapLimit;
	}
	
	
	private boolean isWrap()
	{
		return wrapLimit > 0;
	}
	
	
	public int getTopRowCount()
	{
		return topRows;
	}
	
	
	public int getBottomRowcount()
	{
		return bottomRows;
	}
	
	
	public int getTopIndex()
	{
		return topIndex;
	}
	
	
	public int getBottomIndex()
	{
		return bottomIndex;
	}

	
	public int getRowCount()
	{
		return topRows + bottomRows + visibleRowCount;
	}
	
	
	public int getSlidingWindowRowCount()
	{
		// TODO some rows are missing (first and last visible paragraphs are unaccounted for)
		return topRows + bottomRows + visibleRowCount;
	}


	/**
	 * Finds wrap coordinates for the row relative to the top of the sliding window.
	 * Returns [ix, cix]
	 */
	public int[] findRow(int row)
	{
		int ix = topIndex;
		int cix = 0;
		int count = row;
		
		while(count > 0)
		{
			WrapInfo w = cache.getWrapInfo(ix, wrapLimit);
			int h = w.getRowCount();
			if(count < h)
			{
				cix = w.getCellIndexAtRow(count);
				break;
			}
			else
			{
				count -= h;
				ix++;
			}
		}
		
		return new int[]
		{
			ix,
			cix
		};
	}


	public RelativePosition getRelativePosition(TextPos p)
	{
		int last = viewRows - 1;
		if(last < 0)
		{
			return RelativePosition.UNDETERMINED;
		}
		
		if(isWrap())
		{
			WrapInfo wi = rows.get(0);
			int cix = offsets.get(0);
			if(p.compareTo(wi.getIndex(), cix) < 0)
			{
				return RelativePosition.ABOVE;
			}
	
			wi = rows.get(last);
			cix = offsets.get(last);
			if(p.compareTo(wi.getIndex(), cix + viewCols) >= 0)
			{
				return RelativePosition.BELOW;
			}
		}
		else
		{
			int x = p.cellIndex() - startCellIndex; 
			int y = p.index() - startIndex;
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
			else if(x >= viewCols)
			{
				if(y < 0)
				{
					return RelativePosition.ABOVE_RIGHT;
				}
				else if(y >= viewRows)
				{
					return RelativePosition.BELOW_RIGHT;
				}
				else if(x == viewCols) // FIX does not work!
				{
					if(!p.isLeading())
					{
						return RelativePosition.VISIBLE;
					}
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


	public GridPos getCoordinates(TextPos p)
	{
		int ix = p.index();
		int cix = p.paintCellIndex();
		
		for(int i=rows.size()-1; i>=0; --i)
		{
			WrapInfo wi = rows.get(i);
			if(wi.getIndex() == ix)
			{
				int x = cix - cellIndexAtViewRow(i);
				if((x >= 0) && (x < viewCols))
				{
					return new GridPos(i, x);
				}
			}
		}
		return null;
	}
	
	
	// returns [index, cellIndex] or null
	public int[] getPosition(int row, int col)
	{
		WrapInfo wi = wrapInfoAtViewRow(row);
		if(wi != null)
		{
			int cix = cellIndexAtViewRow(row) + col;
			return new int[]
			{
				wi.getIndex(),
				Math.min(cix, wi.getCellCount())
			};
		}
		return null;
	}
}
