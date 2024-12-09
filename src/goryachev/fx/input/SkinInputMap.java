// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import goryachev.common.util.CMap;
import goryachev.fx.input.internal.EHandlers;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javafx.event.EventType;


/**
 * Skin InputMap.
 */
public class SkinInputMap
{
	// FID -> Runnable, BooleanSupplier
	// KB -> FID
	private final CMap<Object,Object> map = new CMap<>(16);
	
	
	public SkinInputMap()
	{
		
	}
	
	
	public void func(FID f, Runnable r)
	{
		map.put(f, r);
	}
	
	
	public void func(FID f, BooleanSupplier r)
	{
		map.put(f, r);
	}
	
	
	public void key(KB k, FID f)
	{
		map.put(k, f);
		registerType(k);
	}
	
	
	private void registerType(KB k)
	{
		EventType t = k.getEventType();
		// TODO register
	}


	void apply(InputMap m)
	{
		for(Map.Entry<Object,Object> en: map.entrySet())
		{
			if(en.getKey() instanceof EventType t)
			{
				Object v = en.getValue();
				if(v instanceof EHandlers h)
				{
					
				}
			}
		}
		// TODO
	}


	boolean execFunc(FID f)
	{
		Object v = map.get(f);
		if(v instanceof Runnable r)
		{
			r.run();
			return true;
		}
		else if(v instanceof BooleanSupplier b)
		{
			return b.getAsBoolean();
		}
		return false;
	}


	Object valueFor(KB k)
	{
		return map.get(k);
	}
}
