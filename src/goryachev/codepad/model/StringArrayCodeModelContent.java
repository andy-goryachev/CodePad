// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import java.util.Arrays;


/// StringArray CodeModelContent.
public class StringArrayCodeModelContent
	extends DecoratedContent
{
	private final String[] paragraphs;
	
	
	public StringArrayCodeModelContent(String[] paragraphs, ParagraphDecorator d)
	{
		super(d);
		this.paragraphs = Arrays.copyOf(paragraphs, paragraphs.length);
	}


	@Override
	public boolean isWritable()
	{
		return false;
	}


	@Override
	public boolean isAppendable()
	{
		return false;
	}


	@Override
	public int size()
	{
		return paragraphs.length;
	}


	@Override
	public String getPlainText(int index)
	{
		return paragraphs[index];
	}
}
