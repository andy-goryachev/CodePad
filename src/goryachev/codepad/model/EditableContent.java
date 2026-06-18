// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.Defaults;
import goryachev.common.util.CList;
import goryachev.fx.FxBoolean;
import javafx.beans.property.BooleanProperty;


/// Editable In-Memory Content.
public class EditableContent
	implements CodeModelContent
{
	private final CList<String> paragraphs = new CList<>();
	private FxBoolean writable;

	
	public EditableContent()
	{
	}
	
	
	@Override
	public int size()
	{
		return paragraphs.size();
	}

	
	public final void replace(TextPos start, TextPos end, String text)
	{
		// TODO
	}
	

	@Override
	public boolean isAppendable()
	{
		return true;
	}


	@Override
	public String getPlainText(int index)
	{
		return paragraphs.get(index);
	}

	
	@Override
	public final boolean isWritable()
	{
		return writable == null ? Defaults.WRITABLE : writable.get();
	}
	
	
	public final void setWritable(boolean on)
	{
		if((writable == null) && (on != Defaults.WRITABLE))
		{
			writableProperty().set(on);
		}
	}

	
	public final BooleanProperty writableProperty()
	{
		if(writable == null)
		{
			writable = new FxBoolean(Defaults.WRITABLE);
		}
		return writable;
	}


	@Override
	public CodeParagraph getParagraph(int index)
	{
		String text = getPlainText(index);
		return CodeParagraph.fast(index, text);
	}
}
