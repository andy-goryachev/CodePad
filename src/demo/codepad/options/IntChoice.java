// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.common.util.CKit;
import goryachev.common.util.HasDisplayText;
import goryachev.common.util.NamedValue;
import goryachev.fx.FX;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;


/**
 * Integer Choice.
 */
public class IntChoice
	extends ComboBox<Object>
{
	private final SimpleObjectProperty<Number> prop = new SimpleObjectProperty<>();
	
	
	public IntChoice(String name, Property<Number> p)
	{
		FX.setName(this, name);
		setEditable(true);
		
		setConverter(new StringConverter<Object>()
		{
			@Override
			public String toString(Object x)
			{
				if(x instanceof HasDisplayText n)
				{
					return n.getDisplayText();
				}
				Number n = toNumber(x);
				return String.valueOf(n);
			}

			@Override
			public Object fromString(String s)
			{
				return parseValue(s);
			}
		});
		
		setOnAction((ev) ->
		{
			commit();
		});
		
		prop.bindBidirectional(p);
		
		prop.addListener((s,prev,v) ->
		{
			select(v, false);
		});
	}
	
	
	public static IntChoice of(String name, Property<Number> p, Number ... items)
	{
		IntChoice c = new IntChoice(name, p);
		for(Number n: items)
		{
			c.add(n);
		}
		c.selectInitialValue();
		return c;
	}
	
	
	public void add(Number n)
	{
		getItems().add(n);
	}
	
	
	public void add(String text, Number n)
	{
		getItems().add(new NamedValue<>(text, n));
	}
	
	
	public void select(int index)
	{
		if((index >= 0) && (index < getItems().size()))
		{
			getSelectionModel().select(index);
		}
	}
	
	
	public void selectInitialValue()
	{
		Number v = prop.get();
		select(v, true);
	}
	
	
	private void commit()
	{
		Object v = getValue();
		Number n = toNumber(v);
		if(n != null)
		{
			prop.set(n);
		}
	}
	
	
	private Number toNumber(Object x)
	{
		if(x == null)
		{
			return null;
		}
		else if(x instanceof Number n)
		{
			return n;
		}
		else if(x instanceof NamedValue n)
		{
			return (Number)n.getValue();
		}
		throw new Error("?" + x);
	}
	
	
	private Number parseValue(String s)
	{
		if(CKit.isBlank(s))
		{
			return null;
		}
		
		NamedValue<Number> n = findItem(s);
		if(n != null)
		{
			return n.getValue();
		}
		
		try
		{
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e)
		{ }
		return null;
	}


	private NamedValue<Number> findItem(String s)
	{
		for(Object x: getItems())
		{
			if(x instanceof NamedValue n)
			{
				if(s.equals(n.getDisplayText()))
				{
					return n;
				}
			}
		}
		return null;
	}
	
	
	private void select(Number value, boolean initial)
	{
		List<Object> items = getItems();
		int sz = items.size();
		for(int i=0; i<sz; i++)
		{
			Object x = items.get(i);
			Number n = toNumber(x);
			if(CKit.equals(n, value))
			{
				select(i);
				return;
			}
		}
		
		if(initial)
		{
			String text = value + " (initial)";
			items.add(new NamedValue<>(text, value));
			select(sz);
		}
	}
}
