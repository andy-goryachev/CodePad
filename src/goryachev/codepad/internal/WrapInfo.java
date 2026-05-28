// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeParagraph;
import javafx.scene.paint.Color;


/**
 * WrapInfo represents a CodeParagraph laid out within the view port with wrapping
 * and tab stops computed when necessary.
 * 
 * There are several type:
 * - simple, with 1:1 correspondence between characters and cells and no tabulation applied
 * - simple, wrapped
 * - complex, where tabs and/or combined characters are present.
 */
// TODO rename: CellUnit?
// TODO cell index <--> string offset
public abstract class WrapInfo
{
	/// Returns the number of visual rows in this paragraph.
	public abstract int getRowCount();

	/// Returns the cell text for the given cell index.
	public abstract String getCellText(int cellIndex);

	/// Returns cell index at the beginning of the specified row.
	public abstract int getCellIndexAtRow(int row);

	/** assumes leading bias */
	public abstract int getRowAtCellIndex(int cellIndex);
	
	/// returns cell index at the next row, or -1 if the last row in this paragraph
	protected abstract int nextRow(int cellIndex);
	
	//
	
	protected final CodeParagraph paragraph;
	
	
	WrapInfo(CodeParagraph p)
	{
		this.paragraph = p;
	}
	
	
	public final int getIndex()
	{
		return paragraph.getIndex();
	}
	
	
	public final int getCellCount()
	{
		return paragraph.getCellCount();
	}
	
	
	public final Color getBackgroundColor()
	{
		return paragraph.getBackgroundColor();
	}
	
	
	/// Returns the cell style for the given cell index.
	public final CellStyle getCellStyle(int cix)
	{
		if(cix < paragraph.getTextLength())
		{
			return paragraph.getCellStyle(cix);
		}
		return null;
	}
	
	
	public final TextPos clamp(int cix)
	{
		if(cix < 0)
		{
			cix = 0;
		}
		else
		{
			int len = getCellCount();
			if((len != 0) && (cix >= len))
			{
				return TextPos.of(getIndex(), len);
			}
		}
		return TextPos.of(getIndex(), cix);
	}
	
	
	/**
	 * Creates cell to cell position mapping by wrapping when wrapLimit > 0,
	 * accounting for tabs when CodeParagraph.hasTabs() is true.
	 */
	public static WrapInfo create(CodeParagraph p, int tabSize, int wrapLimit)
	{
		boolean tabs = p.hasTabs();
		if(wrapLimit > 0)
		{
			// needs wrapping
			if(tabs)
			{
				return new WrappedTabs(p, tabSize, wrapLimit);
			}
			else
			{
				return new WrappedSimple(p, wrapLimit);
			}
		}
		else
		{
			// no wrapping
			if(tabs)
			{
				return new SingleRowTabs(p, tabSize);
			}
			else
			{
				return new SingleRow(p);
			}
		}
	}


	/// Single row, no tabs.
	private static class SingleRow extends WrapInfo
	{
		SingleRow(CodeParagraph p)
		{
			super(p);
		}

		
		@Override
		public int getRowCount()
		{
			return 1;
		}


		@Override
		public String getCellText(int cix)
		{
			return paragraph.getCellText(cix);
		}


		@Override
		public int getCellIndexAtRow(int row)
		{
			return 0;
		}


		@Override
		public int getRowAtCellIndex(int cix)
		{
			return 0;
		}


		@Override
		protected int nextRow(int cellIndex)
		{
			return -1;
		}
	}
	
	
	/// Single row, with tabs.
	private static class SingleRowTabs extends WrapInfo
	{
		SingleRowTabs(CodeParagraph p, int tabSize)
		{
			super(p);
			// TODO process tabs
		}

		
		@Override
		public int getRowCount()
		{
			return 1;
		}
		
		
		@Override
		public String getCellText(int cix)
		{
			// TODO
			return paragraph.getCellText(cix);
		}


		@Override
		public int getCellIndexAtRow(int row)
		{
			return 0;
		}


		@Override
		public int getRowAtCellIndex(int cix)
		{
			return 0;
		}
		
		
		@Override
		protected int nextRow(int cellIndex)
		{
			return -1;
		}
	}
	
	
	/// Wrapped, no tabs.
	private static class WrappedSimple extends WrapInfo
	{
		private final int cols;
		
		
		WrappedSimple(CodeParagraph p, int cols)
		{
			super(p);
			this.cols = cols;
		}

		
		@Override
		public int getRowCount()
		{
			return 1 + (getCellCount() - 1) / cols;
		}
		
		
		@Override
		public String getCellText(int cix)
		{
			return paragraph.getCellText(cix);
		}


		@Override
		public int getCellIndexAtRow(int row)
		{
			return row * cols;
		}


		@Override
		public int getRowAtCellIndex(int cix)
		{
			return cix / cols;
		}
		
		
		@Override
		protected int nextRow(int cellIndex)
		{
			int r = (cellIndex / cols) + 1;
			if(r < getRowCount())
			{
				return r * cols;
			}
			return -1;
		}
	}
	
	
	/// Wrapped with tabs.
	private static class WrappedTabs extends WrapInfo
	{
		WrappedTabs(CodeParagraph p, int tabSize, int wrapLimit)
		{
			super(p);
			// TODO process tabs
		}

		
		@Override
		public int getRowCount()
		{
			return 1;
		}
		
		
		@Override
		public String getCellText(int ix)
		{
			// TODO
			return paragraph.getCellText(ix);
		}

		
		@Override
		public int getCellIndexAtRow(int row)
		{
			// TODO
			return 0;
		}


		@Override
		public int getRowAtCellIndex(int cix)
		{
			// TODO
			return 0;
		}
		
		
		@Override
		protected int nextRow(int cellIndex)
		{
			// TODO
			return -1;
		}
	}
}
