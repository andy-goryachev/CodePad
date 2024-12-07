// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import javafx.scene.control.Control;


/**
 * Behavior Base.
 */
public class BehaviorBase<C extends Control>
{
	private final C control;
	
	
	public BehaviorBase(C c)
	{
		this.control = c;
	}
	
	
	public void func(FID f, Runnable r)
	{
		// TODO
	}
	
	
	public void key(KB k, FID f)
	{
		// TODO
	}
}
