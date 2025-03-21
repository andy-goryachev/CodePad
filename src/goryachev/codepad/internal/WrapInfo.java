// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
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
public abstract class WrapInfo
{
	/**
	 * Returns the number of visual rows in this paragraph.
	 */
	public abstract int getRowCount();

	/**
	 * Returns the cell text for the given cell index.
	 */
	public abstract String getCellText(int cix);
	
	public abstract int getCellIndexAtRow(int row);

	/** assumes leading bias */
	public abstract int getRowAtCellIndex(int cix);
	
	//
	
	protected final CodeParagraph paragraph;
	
	
	private WrapInfo(CodeParagraph p)
	{
		this.paragraph = p;
	}
	
	
	public int getIndex()
	{
		return paragraph.getIndex();
	}
	
	
	public int getCellCount()
	{
		return paragraph.getCellCount();
	}
	
	
	public Color getBackgroundColor()
	{
		return paragraph.getBackgroundColor();
	}
	
	
	/**
	 * Returns the cell style for the given cell index.
	 */
	public CellStyle getCellStyle(int cix)
	{
		if(cix < paragraph.getTextLength())
		{
			return paragraph.getCellStyle(cix);
		}
		return null;
	}
	
	
	public TextPos clamp(int cix)
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
		boolean complex = p.hasTabs() || p.hasComplexCells();
		
		if(wrapLimit > 0)
		{
			// needs wrapping
			if(complex)
			{
				// complex wrapped case
				return new ComplexWrapped(p, tabSize, wrapLimit);
			}
			else
			{
				// simple wrapped case
				return new SimpleWrapped(p, wrapLimit);
			}
		}
		else
		{
			// no wrapping
			if(complex)
			{
				// complex single line
				return new ComplexSingleLine(p, tabSize);
			}
			else
			{
				// single line
				return new SingleLine(p);
			}
		}
	}


	/** no tabs, no complex symbols, one line */
	private static class SingleLine extends WrapInfo
	{
		SingleLine(CodeParagraph p)
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
	}
	
	
	/** tabs or complex symbols, one line */
	private static class ComplexSingleLine extends WrapInfo
	{
		ComplexSingleLine(CodeParagraph p, int tabSize)
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
	}
	
	
	/** no tabs or complex symbols, wrapped */
	private static class SimpleWrapped extends WrapInfo
	{
		private final int cols;
		
		
		SimpleWrapped(CodeParagraph p, int cols)
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
	}
	
	
	/** tabs or complex symbols, wrapped */
	private static class ComplexWrapped extends WrapInfo
	{
		ComplexWrapped(CodeParagraph p, int tabSize, int wrapLimit)
		{
			super(p);
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
	}
}
