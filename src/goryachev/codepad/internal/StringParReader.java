// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import java.io.IOException;


/// String ParagraphReader.
public class StringParReader
	extends ParReader
{
	private final String text;
	private int index;
	
	
	public StringParReader(String text)
	{
		this.text = text;
	}
	
	
	@Override
	public void close()
	{
	}
	
	
	private int charAt(int ix)
	{
		if(ix < text.length())
		{
			return text.charAt(ix);
		}
		return -1;
	}
	
	
	private String nextString(int end)
	{
		String s = text.substring(index, end);
		index = end;
		return s;
	}


	@Override
	public Object nextToken()
	{
		int ix = index;
		for(;;)
		{
			int c = charAt(ix);
			switch(c)
			{
			case -1:
				if(ix == index)
				{
					return null;
				}
				else
				{
					return nextString(ix);
				}
			case '\r':
				if(ix > index)
				{
					return nextString(ix);
				}
				
				if(charAt(ix + 1) == '\n')
				{
					index += 2;
				}
				else
				{
					index++;
				}
				return NEWLINE;
			case '\n':
				if(ix > index)
				{
					return nextString(ix);
				}
				
				index++;
				return NEWLINE;
			default:
				ix++;
			}
		}
	}
}
