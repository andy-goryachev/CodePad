// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.fx.FX;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;


/**
 * Boolean Choice.
 */
public class BooleanChoice
	extends CheckBox
{
	public BooleanChoice(String fxName, String text, BooleanProperty p)
	{
		super(text);
		FX.setName(this, fxName);
		
		if(p == null)
		{
			setDisable(true);
		}
		else
		{
			selectedProperty().bindBidirectional(p);
		}
	}
}
