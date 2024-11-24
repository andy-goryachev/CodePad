// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
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
	
	/**
	 * Returns the cell style for the given cell index.
	 */
	public abstract CellStyle getCellStyle(int cix);
	
	public abstract int getCellIndexAtRow(int row);

	public abstract int getRowAtCellIndex(int cix);
	
	//
	
	protected final CodeParagraph paragraph;
	
	
	private WrapInfo(CodeParagraph p)
	{
		this.paragraph = p;
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
				int rows = 1 + ((p.getCellCount() - 1) / wrapLimit);
				return new SimpleWrapped(p, rows);
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
		public String getCellText(int ix)
		{
			return paragraph.getCellText(ix);
		}


		@Override
		public CellStyle getCellStyle(int ix)
		{
			return null;
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
		public CellStyle getCellStyle(int cix)
		{
			// TODO
			return null;
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
		private final int rows;
		
		
		SimpleWrapped(CodeParagraph p, int rows)
		{
			super(p);
			this.rows = rows;
		}

		
		@Override
		public int getRowCount()
		{
			return rows;
		}
		
		
		@Override
		public String getCellText(int ix)
		{
			// TODO
			return paragraph.getCellText(ix);
		}


		@Override
		public CellStyle getCellStyle(int ix)
		{
			// TODO
			return null;
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
		public CellStyle getCellStyle(int ix)
		{
			// TODO
			return null;
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
