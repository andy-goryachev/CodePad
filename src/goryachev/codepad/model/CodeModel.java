// Copyright © 2024-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.common.log.Log;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


// TODO
// - editable
// - add/remove listener
// - replace


/// [CodePad] Text Model.
public class CodeModel
{
	private static Log log = Log.get("CodeModel");
	protected final CodeModelContent content;
	private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<>();
	private boolean undoRedoEnabled;
	
	
	public CodeModel(CodeModelContent content)
	{
		this.content = content;
	}
	
	
	/// Determines whether the model can be modified by the user (i.e. supports editing).
	public boolean isWritable()
	{
		return content.isWritable();
	}
	
	
	private void checkWritable()
	{
		if(!isWritable())
		{
			throw new UnsupportedOperationException("not writable");
		}
	}
	

	/// Determines whether the model can grow programmatically
	public final boolean isAppendable()
	{
		return content.isAppendable();
	}
	
	
	public final boolean isUndoRedoEnabled()
	{
		return undoRedoEnabled;
	}
	
	
	public final void setUndoRedoEnabled(boolean on)
	{
		undoRedoEnabled = on;
	}
	
	
	/// Returns the number of paragraphs.
	public final int size()
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
	
	
	public final TextPos getDocumentEnd()
	{
		int ix = size() - 1;
		if(ix < 0)
		{
			return TextPos.ZERO;
		}
		return getEndOfParagraph(ix);
	}
	
	
	public final void addListener(ChangeListener li)
	{
		listeners.add(li);
	}
	
	
	public final void removeListener(ChangeListener li)
	{
		listeners.remove(li);
	}
	
	
	public final TextPos getEndOfParagraph(int ix)
	{
		int cix = getParagraphLength(ix);
		return new TextPos(ix, cix);
	}
	
	
	protected void removeRange()
	{
		// TODO
	}


	public final TextPos replace(TextPos start, TextPos end, String text) throws Exception
	{
		log.trace("start={0} end={1} text={2}", start, end, text);
		Objects.nonNull(start);
		Objects.nonNull(end);
		
		checkWritable();
		
		start = clamp(start);
		end = clamp(end);
		int cmp = start.compareTo(end);
		if(cmp > 0)
		{
			TextPos t = start;
			start = end;
			end = t;
		}

		boolean undoEnabled = isUndoRedoEnabled();
		InsertResult r = content.replace(start, end, text, undoEnabled);
		
		if(undoEnabled)
		{
			// TODO update undo
		}
		
		// TODO fire event
		TextPos newEnd = r.getNewEnd();
		fireEvent(start, end, newEnd);
		
		return newEnd;
	}


	private void fireEvent(TextPos start, TextPos end, TextPos newEnd)
	{
		for(ChangeListener li: listeners)
		{
			li.onContentChange();
		}
	}
}
