// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import java.util.Objects;


// TODO
// - editable
// - add/remove listener
// - replace


/// [CodePad] Text Model.
public final class CodeModel
{
	private final CodeModelContent content;
	
	
	public CodeModel(CodeModelContent content)
	{
		this.content = content;
	}
	
	
	/// Determines whether the model can be modified by the user (i.e. supports editing).
	public boolean isWritable()
	{
		return content.isWritable();
	}
	

	/// Determines whether the model can grow programmatically
	public boolean isAppendable()
	{
		return content.isAppendable();
	}
	
	
	/// Returns the number of paragraphs.
	public int size()
	{
		return content.size();
	}
	
	
	/// Returns the [CodeParagraph] at the specified `index`.
	///
	/// This index should never go beyond the number of paragraphs as determined by [#size()].
	/// Doing so might result in an undetermined behavior (most likely an exception).
	public CodeParagraph getParagraph(int index)
	{
		// TODO clamp
		return content.getParagraph(index);
	}
	
	
	/// Returns the plain text (always non-null) of the paragraph at the specified `index`.
	///
	/// This index should never go beyond the number of paragraphs as determined by [#size()].
	/// Doing so might result in an undetermined behavior (most likely an exception).
	/// @implNote
	/// The default implementation retrieves the [CodeParagraph] and obtains the plain text from it.
	/// The subclasses may override this method if a more efficient way of obtaining the plain text exist.
	public String getPlainText(int index)
	{
		CodeParagraph p = getParagraph(index);
		return p.getPlainText();
	}

	
	/// Returns the length of the paragraph text (the character count) at the specified `index`.
	///
	/// The base class implementation simply invokes 'getParagraph(index).getTextLength();',
	/// but subclasses may override this method if a more optimal implementation can be provided.
	public int getParagraphLength(int index)
	{
		return getParagraph(index).getTextLength();
	}
	

	/// Clamps the text position to the document limits.
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
			if(p.cellIndex() > len)
			{
				return new TextPos(ix, len);
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
			return new TextPos(ix, len);
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
		return new TextPos(ix, cix);
	}
}
