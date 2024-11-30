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
	private final CList<WrapInfo> rows = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private final CList<Integer> offsets = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private int viewStartIndex;
	private int viewStartCellIndex;
	private int visibleRowCount;
	private final int wrapLimit;
	private int lastViewIndex;
	private int topRowCount;
	private int bottomRowCount;
	private int maxCellCount;
	private int bottomParagraphCount;
	private int topParagraphCount;
	
	
	public Arrangement(WrapCache cache, int modelSize, int viewCols, int wrapLimit)
	{
		this.cache = cache;
		this.modelSize = modelSize;
		this.viewCols = viewCols;
		this.wrapLimit = wrapLimit;
	}


	/**
	 * Lays out {@code rowCount} paragraphs.
	 * Returns the number of paragraphs actually laid out.
	 */
	// TODO move these args to the contructor?
	// TODO num columns for hsb
	public void layoutViewPort(int startIndex, int startCellIndex, int numRows)
	{
		this.viewStartIndex = startIndex;
		this.viewStartCellIndex = startCellIndex;
		
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
		for(int i=0; i<count; i++)
		{
			if(forBelow)
			{
				ix++;
				if(ix >= modelSize)
				{
					break;
				}
			}
			else
			{
				ix--;
				if(ix < 0)
				{
					break;
				}
			}

			WrapInfo wi = cache.getWrapInfo(ix, wrapLimit);
			nrows += wi.getRowCount();
			
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
			bottomRowCount = nrows;
			bottomParagraphCount = (ix - startIndex);
		}
		else
		{
			topRowCount = nrows;
			topParagraphCount = (ix - startIndex);
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
		return topRowCount;
	}
	
	
	public int getBottomRowcount()
	{
		return bottomRowCount;
	}
	
	
	public int getTopParagraphCount()
	{
		return topParagraphCount;
	}
	
	
	public int getTopIndex()
	{
		return viewStartIndex - topParagraphCount;
	}
	
	
	public int getBottomIndex()
	{
		return lastViewIndex + bottomParagraphCount;
	}
	
	
	public int getBottomParagraphCount()
	{
		return bottomParagraphCount;
	}


	public double averageRowsPerParagraph()
	{
		// TODO some rows are unaccouned for (before the first visible row, and after the last visible row), fix it later
		return (topRowCount + bottomRowCount + visibleRowCount) / (topParagraphCount + bottomParagraphCount + lastViewIndex - viewStartIndex);
	}
	
	
	public int getModelSize()
	{
		return modelSize;
	}
	
	
	public int getSlidingWindowRowCount()
	{
		return topRowCount + bottomRowCount + visibleRowCount;
	}


	/**
	 * Finds wrap coordinates for the row relative to the top of the sliding window.
	 * Returns [ix, cix]
	 */
	public int[] findRow(int row)
	{
		int ix = viewStartIndex - topParagraphCount;
		int cix = 0;
		while(row > 0)
		{
			WrapInfo w = cache.getWrapInfo(ix, wrapLimit);
			int h = w.getRowCount();
			if(row < h)
			{
				cix = w.getCellIndexAtRow(row);
			}
			else
			{
				row -= h;
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
