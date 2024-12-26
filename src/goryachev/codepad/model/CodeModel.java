// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import java.util.Objects;


/**
 * CodePad Text Model.
 */
public abstract class CodeModel
{
	/**
	 * Returns the number of paragraphs. 
	 */
	public abstract int size();
	
	
	/**
	 * Returns the {@link CodeParagraph} at the specified {@code index}.
	 * <p>
	 * This index should never go beyond the number of paragraphs as determined by {@link #size()}.
	 * Doing so might result in an undetermined behavior (most likely an exception).
	 */
	public abstract CodeParagraph getParagraph(int index);
	
	
	/**
	 * Returns the length of the paragraph text at the specified {@code index}.
	 * <p>
	 * The base class simply invokes {@code getParagraph(index).getTextLength();},
	 * but subclasses may override this method if a more optimal implementation can be provided. 
	 */
	public int getParagraphLength(int index)
	{
		return getParagraph(index).getTextLength();
	}

	
	public CodeModel()
	{
	}
	
	
	/**
	 * Returns the plain text (always non-null) of the paragraph at the specified {@code index}.
	 * <p>
	 * This index should never go beyond the number of paragraphs as determined by {@link #size()}.
	 * Doing so might result in an undetermined behavior (most likely an exception).
	 * @implNote
	 * The default implementation retrieves the {@link CodeParagraph} and obtains the plain text from it.
	 * The subclasses may override this method if a more efficient way of obtaining the plain text exist. 
	 */
	public String getPlainText(int index)
	{
		CodeParagraph p = getParagraph(index);
		return p.getPlainText();
	}
	

	/**
	 * Clamps the text position to the document limits.
	 */
	public final TextPos clamp(TextPos p)
	{
		Objects.nonNull(p);
		int sz = size();
		int ix = p.index();
		if(ix < 0)
		{
			return TextPos.ZERO;
		}
		else if(ix < sz)
		{
			int len = getParagraphLength(ix);
			if(p.offset() > len)
			{
				return new TextPos(ix, len, true);
			}
			return p;
		}
		else if(sz == 0)
		{
			return TextPos.ZERO;
		}
		else
		{
			ix = sz - 1;
			int len = getParagraphLength(ix);
			return new TextPos(ix, len, false);
		}
	}
	
	
	public TextPos getDocumentEnd()
	{
		int ix = size() - 1;
		if(ix < 0)
		{
			return TextPos.ZERO;
		}
		return getEndOfParagraph(ix);
	}
	
	
	public TextPos getEndOfParagraph(int ix)
	{
		int cix = getParagraphLength(ix);
		return new TextPos(ix, cix, false);
	}
}
