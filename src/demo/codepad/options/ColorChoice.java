// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.fx.FX;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;


/**
 * Color Choice.
 */
public class ColorChoice
	extends ColorPicker
{
	private final SimpleObjectProperty<Color> prop = new SimpleObjectProperty<>();
	
	
	public ColorChoice(String fxName, ObjectProperty<Color> p)
	{
		FX.setName(this, fxName);
		
		valueProperty().bindBidirectional(p);
	}
	
	
	public void selectInitialValue()
	{
		Color v = prop.get();
		setValue(v);
	}
}
