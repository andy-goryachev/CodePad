// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.common.util.CKit;
import java.util.Arrays;


/// StringArray CodeModelContent.
public class StringArrayCodeModelContent
	extends DecoratedContent
{
	private final String[] paragraphs;
	
	
	private StringArrayCodeModelContent(String[] paragraphs, ParagraphDecorator d)
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
	

	public static StringArrayCodeModelContent of(ParagraphDecorator d, String text)
	{
		String[] lines = CKit.split(text, "\n");
		return new StringArrayCodeModelContent(lines, d);
	}


	@Override
	public InsertResult replace(TextPos start, TextPos end, String text, boolean undoEnabled)
	{
		return null;
	}
}
