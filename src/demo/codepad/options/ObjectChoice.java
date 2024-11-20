// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.common.util.HasDisplayText;
import goryachev.fx.FX;
import javafx.scene.control.ComboBox;


/**
 * Object Choice.
 */
public class ObjectChoice<T>
	extends ComboBox<Object>
{
	public ObjectChoice(String name)
	{
		FX.setName(this, name);
		setConverter(FX.standardConverter());
	}
	
	
	public void add(T item)
	{
		getItems().add(item);
	}
	
	
	public void add(String text, T item)
	{
		getItems().add(new Entry(text, item));
	}
	
	
	public T getChoice()
	{
		Object v = getSelectionModel().getSelectedItem();
		if(v != null)
		{
			if(v instanceof Entry en)
			{
				return (T)en.item;
			}
			else
			{
				return (T)v;
			}
		}
		return null;
	}
	
	
	private static class Entry implements HasDisplayText
	{
		public final String text;
		public final Object item;
		
		
		public Entry(String text, Object item)
		{
			this.item = item;
			this.text = text;
		}


		@Override
		public String getDisplayText()
		{
			return text;
		}
	}
}
