// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.common.util.CKit;
import javafx.scene.paint.Color;


/**
 * Test CodeModel.
 */
public class TestCodeModel extends CodeModel
{
	private final String[] lines;


	public TestCodeModel(String[] lines)
	{
		this.lines = lines;
	}
	
	
	public static TestCodeModel of(String text)
	{
		String[] lines = CKit.split(text, "\n");
		return new TestCodeModel(lines);
	}
	

	@Override
	public int size()
	{
		return lines.length;
	}
	

	@Override
	public CodeParagraph getParagraph(int index)
	{
		String text = lines[index];
		
		return new CodeParagraph()
		{
			private static final Color GREEN_BG = Color.rgb(0, 255, 0, 0.25);
			private static final CellStyle BLUE = CellStyle.builder().textColor(Color.BLUE).build();
			private static final CellStyle RED = CellStyle.builder().textColor(Color.RED).build();
			
			
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
				case "a":
					return BLUE;
				case "b":
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
		};
	}
}
