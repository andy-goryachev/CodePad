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
	
	public static final CellStyle EMPTY = new Builder().build();
	
	
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
		private boolean strikeThrough;
		private boolean underline;
		
		
		public Builder()
		{
		}
		
		
		public Builder background(Color c)
		{
			backgroundColor = c;
			return this;
		}
		
		
		public Builder bold()
		{
			bold = true;
			return this;
		}
		
		
		public Builder italic()
		{
			italic = true;
			return this;
		}
		
		
		public Builder strikeThrough()
		{
			strikeThrough = true;
			return this;
		}
		
		
		public Builder textColor(Color c)
		{
			textColor = c;
			return this;
		}
		
		
		public Builder underline()
		{
			underline = true;
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
					return strikeThrough;
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
