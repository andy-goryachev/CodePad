// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import javafx.scene.paint.Color;


/**
 * Collects individual cell attributes.
 */
public interface CellStyle
{
	public Color getTextColor();
	
	public Color getBackgroundColor();
	
	public boolean isUnderline();
	
	public boolean isStrikeThrough();
	
	public boolean isBold();
	
	public boolean isItalic();
	
	// TODO squiggle color
	// TODO user data?
	
	public static final CellStyle EMPTY = new CellStyle()
	{
		@Override
		public Color getTextColor()
		{
			return null;
		}

		@Override
		public Color getBackgroundColor()
		{
			return null;
		}

		@Override
		public boolean isUnderline()
		{
			return false;
		}

		@Override
		public boolean isStrikeThrough()
		{
			return false;
		}

		@Override
		public boolean isBold()
		{
			return false;
		}

		@Override
		public boolean isItalic()
		{
			return false;
		}
	};
}
