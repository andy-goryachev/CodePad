// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeParagraph;
import javafx.scene.paint.Color;


/// WrapInfo represents a CodeParagraph laid out within the view port with wrapping
/// and tab stops computed when necessary.
// TODO rename: CellUnit? WrapBlock?
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
	
	/// Returns {@link TextPos} at the specified cell index.
	/// This method clamps to the paragraph bounds.
	public abstract TextPos atCell(int cellIndex);
	
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
	
	
	public final TextPos atEnd()
	{
		return new TextPos(getIndex(), getTextLength());
	}
	
	
	/// Returns cellIndex at the specified offset (clamped to the paragraph bounds)
	public final int cellIndexAtOffset(int offset)
	{
		return paragraph.cellIndexAtOffset(offset);
	}

	
	public final int getTextLength()
	{
		return paragraph.getTextLength();
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
	

	int clampCellIndex(int cellIndex)
	{
		if(cellIndex < 0)
		{
			cellIndex = 0;
		}
		else
		{
			int count = getCellCount();
			if(cellIndex > count)
			{
				return count;
			}
		}
		return cellIndex;
	}
	
	
	/// Creates mapping between `CodeParagraph` cells and their visual positioning in the grid.
	public static WrapInfo create(CodeParagraph p, int tabSize, int wrapLimit)
	{
		// TODO complex is (cellCount != charCount)
		// TODO tabs
		if(wrapLimit > 0)
		{
			return new WrappedSimple(p, wrapLimit);
		}
		else
		{
			// no wrapping
			return new SingleRow(p);
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
		
		
		@Override
		public TextPos atCell(int cellIndex)
		{
			cellIndex = clampCellIndex(cellIndex);
			int offset = cellIndex;
			return new TextPos(getIndex(), offset);
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


		@Override
		public TextPos atCell(int cellIndex)
		{
			cellIndex = clampCellIndex(cellIndex);
			int offset = cellIndex;
			return new TextPos(getIndex(), offset);
		}
	}
}
