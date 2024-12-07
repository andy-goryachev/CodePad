// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import javafx.scene.control.Control;


/**
 * Behavior Base.
 */
public abstract class BehaviorBase<C extends Control>
{
	protected abstract void populateSkinInputMap();
	
	
	private final C control;
	private SkinInputMap skinInputMap;
	
	
	public BehaviorBase(C c)
	{
		this.control = c;
	}
	
	
	public C control()
	{
		return control;
	}
	
	
	public SkinInputMap getSkinInputMap()
	{
		if(skinInputMap == null)
		{
			skinInputMap = new SkinInputMap();
			populateSkinInputMap();
		}
		return skinInputMap;
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
