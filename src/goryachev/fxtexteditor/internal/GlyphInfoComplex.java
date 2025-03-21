// Copyright © 2020-2025 Andy Goryachev <andy@goryachev.com>
package goryachev.fxtexteditor.internal;
import goryachev.fxtexteditor.GlyphType;
import java.util.Arrays;


/**
 * Complex AGlyphInfo when text does not have 1:1 correspondence between
 * glyph and characters (i.e. contains combining characters, emoji, etc.)
 */
public class GlyphInfoComplex
	extends AGlyphInfo
{
	private final int[] charOffsets;
	
	
	public GlyphInfoComplex(String text, boolean hasTabs, int[] charOffsets)
	{
		super(text, hasTabs);
		this.charOffsets = charOffsets;
	}


	@Override
	public boolean hasComplexGlyphs()
	{
		return true;
	}
	

	@Override
	public int getCharIndex(int glyphIndex)
	{
		if(glyphIndex >= charOffsets.length)
		{
			// FIX emoji
			return -1;
		}
		return charOffsets[glyphIndex];
	}
	
	
	@Override
	public int getGlyphIndex(int charIndex)
	{
		// this can be replaced either by a separate array
		// possibly created on demand.
		// but for now, let's assume the binary search should be faster and easier on memory
		int ix = Arrays.binarySearch(charOffsets, charIndex);
		if(ix < 0)
		{
			ix = -ix;
		}
		return ix;
	}


	@Override
	public String getGlyphText(int ix)
	{
		try
		{
			if(ix >= charOffsets.length)
			{
				return null;
			}
			else
			{
				int start = charOffsets[ix];
				ix++;
				if(ix == charOffsets.length)
				{
					return text.substring(start);
				}
				else
				{
					int end = charOffsets[ix];
					return text.substring(start, end);
				}
			}
		}
		catch(Exception e)
		{
			throw e; // FIX
		}
	}
	
	
	@Override
	public GlyphType getGlyphType(int glyphIndex)
	{
		String s = getGlyphText(glyphIndex);
		if(s == null)
		{
			return GlyphType.EOL;
		}
		else if(s.length() == 1)
		{
			if("\t".equals(s))
			{
				return GlyphType.TAB;
			}
		}
		return GlyphType.REG;
	}


	@Override
	public int getGlyphCount()
	{
		return charOffsets.length;
	}
}