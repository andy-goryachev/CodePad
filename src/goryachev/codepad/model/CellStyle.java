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
	
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	
	public static class Builder
	{
		private Color backgroundColor;
		private Color textColor;
		private boolean bold;
		private boolean italic;
		private boolean strikethrough;
		private boolean underline;
		
		
		public Builder()
		{
		}
		
		
		public Builder background(Color c)
		{
			backgroundColor = c;
			return this;
		}
		
		
		public Builder textColor(Color c)
		{
			textColor = c;
			return this;
		}
		
		
		public CellStyle build()
		{
			return new CellStyle()
			{
				@Override
				public Color getTextColor()
				{
					return textColor;
				}


				@Override
				public Color getBackgroundColor()
				{
					return backgroundColor;
				}


				@Override
				public boolean isUnderline()
				{
					return underline;
				}


				@Override
				public boolean isStrikeThrough()
				{
					return strikethrough;
				}


				@Override
				public boolean isBold()
				{
					return bold;
				}


				@Override
				public boolean isItalic()
				{
					return italic;
				}
			};
		}
	}
}
