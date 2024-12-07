// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import javafx.scene.control.Control;


/**
 * Input Map class serves as a repository of event handlers and key mappings,
 * arbitrating the event processing between application and the skin. 
 */
public class InputMap
{
	private final Control control;
	
	
	public InputMap(Control c)
	{
		this.control = c;
	}
}
