// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.Defaults;
import goryachev.common.util.CList;
import goryachev.fx.FxBoolean;
import javafx.beans.property.BooleanProperty;


/// Editable CodeModel.
public class EditableCodeModel
	extends CodeModel
{
	private FxBoolean writable;


	public EditableCodeModel()
	{
		super(new EditableContent());
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
	
	
	// TODO decorator property


	private static class EditableContent extends DecoratedContent
	{
		private final CList<String> paragraphs = new CList<>();


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
			return isWritable();
		}


		@Override
		public String getPlainText(int index)
		{
			return paragraphs.get(index);
		}


		@Override
		public boolean isWritable()
		{
			// ignored
			return true;
		}


		@Override
		public InsertResult replace(TextPos start, TextPos end, String text, boolean undoEnabled)
		{
			// TODO
			return null;
		}
	}
}
