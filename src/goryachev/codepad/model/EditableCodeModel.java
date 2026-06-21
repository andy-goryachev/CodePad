// Copyright © 2026-2026 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.model;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.internal.ParReader;
import goryachev.common.log.Log;
import goryachev.common.util.CList;
import goryachev.fx.FxBoolean;
import goryachev.fx.FxObject;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;


/// Editable CodeModel.
public class EditableCodeModel
	extends CodeModel
{
	static Log log = Log.get("EditableCodeModel");
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
					ParagraphDecorator d = get();
					((EditableContent)content).setDecorator(d);
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
		
		
		private void removeRange(TextPos start, TextPos end)
		{
			int index = start.index();
			String s = paragraphs.get(index);
			if(index == end.index())
			{
				s = s.substring(0, start.cellIndex()) + s.substring(end.cellIndex());
				paragraphs.set(index, s);
			}
			else
			{
				// last line
				int eix = end.index();
				String last = paragraphs.get(eix);
				paragraphs.set(eix, last.substring(end.cellIndex()));
				
				// first line
				paragraphs.set(index, s.substring(0, start.cellIndex()));
				index++;
				
				// in-between
				int ct = eix - index;
				for(int i=0; i<ct; i++)
				{
					paragraphs.remove(index);
				}
			}
		}
		
		
		private void insertLineBreak(int index, int off)
		{
			String s = paragraphs.get(index);
			if(off == 0)
			{
				paragraphs.add(index, "");
			}
			else
			{
				int len = s.length();
				if(off < len)
				{
					String s1 = s.substring(0, off);
					String s2 = s.substring(off);
					paragraphs.set(index, s2);
					paragraphs.add(index, s1);
				}
				else
				{
					paragraphs.add(index + 1, "");
				}
			}
		}
		
		
		// inserts text into a single paragraph
		private int insertText(int index, int off, String text)
		{
			String s = paragraphs.get(index);
			if(off == 0)
			{
				s = text + s;
			}
			else
			{
				int len = s.length();
				if(off < len)
				{
					s = s.substring(0, off) + text + s.substring(off);
				}
				else
				{
					s = s + text;
				}
			}
			paragraphs.set(index, s);
			return text.length();
		}


		@Override
		public InsertResult replace(TextPos start, TextPos end, String text, boolean undoEnabled) throws Exception
		{
			// TODO cell index != character offset!
			
			if(!start.equals(end))
			{
				// TODO undo info?
				removeRange(start, end);
			}

			int top = 0;
			int bottom = 0;
			int startIndex = start.index();
			int index = startIndex;
			int cix = start.cellIndex();

			try(ParReader rd = ParReader.of(text))
			{
				Object x;
				while((x = rd.nextToken()) != null)
				{
					if(x == ParReader.NEWLINE)
					{
						insertLineBreak(index, cix);
						index++;
						cix = 0;
						bottom = 0;
					}
					else if(x instanceof String s)
					{
						int len = insertText(index, cix, s);
						if(index == startIndex)
						{
							top += len;
						}
						cix += len;
						bottom += len;
					}
					else
					{
						log.error("unexpected token {0}", x);
					}
				}
			}

			int added = index - startIndex;
			if(added == 0)
			{
				bottom = 0;
			}

			TextPos newEnd = new TextPos(index, cix);
			return new InsertResult(start, end, top, added, bottom, newEnd);
		}
	}
}
