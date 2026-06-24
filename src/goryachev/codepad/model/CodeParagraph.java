// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import javafx.scene.paint.Color;


/// Code Paragraph is an immutable object that encapsulates a single paragraph of
/// text and its breakdown into cells.
public abstract class CodeParagraph
{
	/// Returns the model index of this paragraph.
	public abstract int getIndex();
	
	
	/// Returns the background color of this paragraph, or `null`.
	/// This method may return a non-opaque color in which case it will be mixed
	/// with the view port background.
	public abstract Color getBackgroundColor();


	/// Returns the plain text (non-null) of this paragraph.
	public abstract String getPlainText();
	
	
	/// Returns the length of the paragraph plain text.
	public abstract int getTextLength();
	
	
	/// Returns the number of cells.
	public abstract int getCellCount();
	
	
	/// Returns the string to be rendered in the given cell.
	public abstract String getCellText(int cellIndex);
	
	
	/// Returns the cell style for the given cell index.
	public abstract CellStyle getCellStyle(int cellIndex);
	
	
	/// Returns true when the text contains tab characters.
	public abstract boolean hasTabs();
	
	
	/// Returns cellIndex at the specified offset (clamped to the paragraph bounds)
	public abstract int cellIndexAtOffset(int offset);
	
	
	public final TextPos getEnd()
	{
		return new TextPos(getIndex(), getTextLength());
	}


	// TODO provide several methods:
	// 1. simple (1:1 chars to cells)
	// 2. complex
	@Deprecated // replace with of(), plain text, with attributes etc.
	public static CodeParagraph fast(int index, String text)
	{
		return new CodeParagraph()
		{
			@Override
			public int getTextLength()
			{
				return text.length();
			}
			
			
			@Override
			public int getCellCount()
			{
				return text.length();
			}
			
			
			@Override
			public String getPlainText()
			{
				return text;
			}
			
			
			@Override
			public int getIndex()
			{
				return index;
			}
			
			
			@Override
			public Color getBackgroundColor()
			{
				return null;
			}


			@Override
			public boolean hasTabs()
			{
				return false;
			}


			@Override
			public String getCellText(int ix)
			{
				char c = text.charAt(ix);
				return String.valueOf(c);
			}


			@Override
			public CellStyle getCellStyle(int cix)
			{
				return null;
			}


			@Override
			public int cellIndexAtOffset(int offset)
			{
				if(offset < 0)
				{
					return 0;
				}
				int len = text.length();
				if(offset < len)
				{
					return offset;
				}
				return len;
			}
		};
	}
}
