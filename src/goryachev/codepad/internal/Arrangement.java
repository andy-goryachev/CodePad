// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
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
	
	
	public Arrangement(WrapCache cache, int modelSize, int viewCols, int wrapLimit, int startIndex, int startCellIndex)
	{
		this.cache = cache;
		this.modelSize = modelSize;
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
			
			if(wrapLimit > 0)
			{
				cix += wrapLimit;
				if(cix < wi.getCellCount())
				{
					// wrapping the same paragraph
					continue;
				}
			}
			else
			{
				int w = wi.getCellCount();
				if(w > maxCellCount)
				{
					maxCellCount = w;
				}
			}
			
			// next paragraph
			cix = 0;
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
			
			if(wrapLimit < 0)
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
		if(wrapLimit > 0)
		{
			return false;
		}
		return maxCellCount > viewCols;
	}
	
	
	public int getLastViewIndex()
	{
		return lastViewIndex;
	}


	public WrapInfo wrapInfoAtViewRow(int ix)
	{
		return rows.get(ix);
	}


	public int cellIndexAtViewRow(int ix)
	{
		return offsets.get(ix);
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


//	public double averageRowsPerParagraph()
//	{
//		int d = bottomIndex - topIndex;
//		if(d == 0)
//		{
//			return 1;
//		}
//		return getRowCount() / d;
//	}
	
	
	public int getRowCount()
	{
		return topRows + bottomRows + visibleRowCount;
	}
	
	
//	public int getModelSize()
//	{
//		return modelSize;
//	}
	
	
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
}
