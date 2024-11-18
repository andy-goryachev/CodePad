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
	private final int maxRows;
	private final CList<WrapInfo> rows = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private final CList<Integer> offsets = new CList<>(Defaults.VIEWPORT_ROW_COUNT_ESTIMATE);
	private int viewStartIndex;
	private int viewStartCellIndex;
	private int visibleRowCount;
	private final int visibleColCount;
	
	
	public Arrangement(WrapCache cache, int maxRows, int visibleColCount)
	{
		this.cache = cache;
		this.maxRows = maxRows;
		this.visibleColCount = visibleColCount;
	}


	/**
	 * Lays out {@code rowCount} paragraphs.
	 * Returns the number of paragraphs actually laid out.
	 */
	public void layoutViewPort(int startIndex, int startCellIndex, int numRows)
	{
		this.viewStartIndex = startIndex;
		this.viewStartCellIndex = startCellIndex;
		
		int rc = 0;
		int modelSize = cache.modelSize();
		WrapInfo wi = null;
		int cix = startCellIndex;
		int wrapLimit = cache.getWrapLimit();
		int ix = startIndex;
		
		// FIX while ix < modelsize!
		while((numRows >= 0) && (ix < modelSize))
		{
			if(wi == null)
			{
				wi = cache.getWrapInfo(ix);
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
			
			// next paragraph
			cix = 0;
			ix++;
			wi = null;
		}
		
		visibleRowCount = rc;
	}
	
	
	public int layoutSlidingWindow(int startIndex, int count, boolean forBelow)
	{
		// TODO
		return 0;
	}
	

	public boolean isVsbNeeded()
	{
		// TODO
		return false;
	}
	
	
	public boolean isHsbNeeded()
	{
		// TODO
		return false;
	}
	
	
	public int getLastIndex()
	{
		// TODO
		return 0;
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
	 * Number of full and partial columns visible in the viewport.
	 */
	public int getVisibleColumnCount()
	{
		return visibleColCount;
	}
}
