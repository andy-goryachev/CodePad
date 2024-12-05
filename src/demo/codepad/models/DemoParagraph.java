// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.models;
import goryachev.codepad.model.CellStyle;
import goryachev.codepad.model.CodeParagraph;
import javafx.scene.paint.Color;


/**
 * Demo Paragraph.
 */
public class DemoParagraph
	extends CodeParagraph
{
	private static final Color GREEN_BG = Color.rgb(0, 255, 0, 0.2);
	private static final CellStyle GRAY = CellStyle.builder().textColor(Color.GRAY).build();
	private static final CellStyle RED = CellStyle.builder().textColor(Color.RED).build();
	private static final CellStyle STRIKETHROUGH = CellStyle.builder().strikeThrough().build();
	private static final CellStyle UNDERLINE = CellStyle.builder().underline().build();

	private final int index;
	private final String text;


	public DemoParagraph(int index, String text)
	{
		this.index = index;
		this.text = text;
	}


	@Override
	public int getIndex()
	{
		return index;
	}


	@Override
	public Color getBackgroundColor()
	{
		return index % 10 == 0 ? GREEN_BG : null;
	}


	@Override
	public String getPlainText()
	{
		return text;
	}


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
	public String getCellText(int ix)
	{
		return String.valueOf(text.charAt(ix));
	}


	@Override
	public CellStyle getCellStyle(int cix)
	{
		String s = getCellText(cix);
		switch(s)
		{
		case "u":
		case "U":
			return UNDERLINE;
		case "n":
			return STRIKETHROUGH;
		case "a":
		case "e":
		case "i":
		case "o":
			return GRAY;
		case "0":
		case "1":
		case "2":
		case "3":
		case "4":
		case "5":
		case "6":
		case "7":
		case "8":
		case "9":
			return RED;
		}
		return null;
	}


	@Override
	public boolean hasTabs()
	{
		return false;
	}


	@Override
	public boolean hasComplexCells()
	{
		return false;
	}
}