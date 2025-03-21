// Copyright © 2019-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor.internal;
import goryachev.common.util.SB;
import goryachev.fx.TextCellStyle;
import goryachev.fxtexteditor.ITextLine;
import javafx.scene.paint.Color;


/**
 * Screen Row translates sequence of glyphs obtained from the model (ITextLine) 
 * to the cells on screen.
 */
public class ScreenRow
{
	private FlowLine flowLine = FlowLine.BLANK;
	private WrapInfo wrap;
	private int lineNumber;
	private int wrapRow;
	private int startGlyphIndex;
	
	
	public ScreenRow()
	{
	}
	
	
	public FlowLine getFlowLine()
	{
		return flowLine;
	}
	
	
	public boolean isBOL()
	{
		return wrapRow == 0;
	}
	

	public ITextLine getTextLine()
	{
		return flowLine.getTextLine();
	}
	
	
	public Color getLineColor()
	{
		ITextLine t = getTextLine();
		return t == null ? null : t.getLineColor();
	}
	

	@Override
	public String toString()
	{
		SB sb = new SB();
		
		sb.append("(");
		sb.append(lineNumber);
		sb.append(",");
		sb.append(wrapRow);
		sb.append(") ");
		
		return sb.toString();
	}
	

	public void init(FlowLine fline, WrapInfo wrap, int lineNumber, int wrapRow, int startGlyphIndex)
	{
		this.flowLine = fline;
		this.wrap = wrap;
		this.lineNumber = lineNumber;
		this.wrapRow = wrapRow;
		this.startGlyphIndex = startGlyphIndex;
	}
	

	/** returns line number (starts at 0) or -1 if line number should not be displayed */
	public int getLineNumber()
	{
		return lineNumber;
	}
	
	
	public int getStartGlyphIndex()
	{
		return startGlyphIndex;
	}
	
	
	/** returns the text cell at the specified column */
	public TextCell getCell(int column)
	{
		return wrap.getCell(TextCell.globalInstance(), wrapRow, column);
	}
	
	
	public String getCellText(TextCell cell)
	{
		switch(cell.getGlyphType())
		{
		case EOF:
		case EOL:
		case TAB:
			return null;
		}
		
		int gix = cell.getGlyphIndex();
		return flowLine.glyphInfo().getGlyphText(gix);
	}
	

	public TextCellStyle getCellStyles(TextCell cell)
	{
		switch(cell.getGlyphType())
		{
		case EOF:
		case EOL:
			return null;
		}
		
		int gix = cell.getGlyphIndex();
		int charIndex = flowLine.glyphInfo().getCharIndex(gix);
		return flowLine.getCellStyle(charIndex);
	}
	
	
	public int getGlyphCount()
	{
		return wrap.getGlyphCountAtRow(wrapRow);
	}
	
	
	public int getWrapRow()
	{
		return wrapRow;
	}
}
