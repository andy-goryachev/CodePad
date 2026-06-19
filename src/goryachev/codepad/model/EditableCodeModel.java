// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.Defaults;
import goryachev.common.util.CList;
import goryachev.fx.FxBoolean;
import goryachev.fx.FxObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;


/// Editable CodeModel.
public class EditableCodeModel
	extends CodeModel
{
	private FxBoolean writable;
	private FxObject<ParagraphDecorator> decorator;


	public EditableCodeModel()
	{
		super(new EditableContent());
	}


	public final ObjectProperty<ParagraphDecorator> decoratorProperty()
	{
		if(decorator == null)
		{
			decorator = new FxObject<>(this, "decorator")
			{
				@Override
				protected void invalidated()
				{
					if(content instanceof EditableContent econ)
					{
						ParagraphDecorator d = get();
						econ.setDecorator(d);
					}
				}
			};
		}
		return decorator;
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


	/// EditableContent backed by a list of Strings.
	private static class EditableContent
		extends DecoratedContent
	{
		private final CList<String> paragraphs = new CList<>();


		public EditableContent()
		{
			paragraphs.add("");
		}


		@Override
		public boolean isAppendable()
		{
			return isWritable();
		}


		@Override
		public boolean isWritable()
		{
			return true;
		}


		@Override
		public int size()
		{
			return paragraphs.size();
		}


		@Override
		public String getPlainText(int index)
		{
			return paragraphs.get(index);
		}


		@Override
		public InsertResult replace(TextPos start, TextPos end, String text, boolean undoEnabled)
		{
			// TODO
			return null;
		}
	}
}
