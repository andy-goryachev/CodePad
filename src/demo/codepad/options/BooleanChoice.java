// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package demo.codepad.options;
import goryachev.fx.FX;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBox;


/**
 * Boolean Choice.
 */
public class BooleanChoice
	extends CheckBox
{
	public BooleanChoice(String fxName, String text, BooleanExpression p)
	{
		super(text);
		FX.setName(this, fxName);
		
		if(p == null)
		{
			setDisable(true);
		}
		else
		{
			if(p instanceof BooleanProperty b)
			{
				selectedProperty().bindBidirectional(b);
			}
			else
			{
				setDisable(true);
				FX.addChangeListener(p, this::setSelected);
			}
		}
	}
}
