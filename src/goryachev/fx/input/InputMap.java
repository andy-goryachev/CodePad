// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import goryachev.common.util.CMap;
import goryachev.fx.input.internal.HPriority;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;


/**
 * Input Map class serves as a repository of event handlers and key mappings,
 * arbitrating the event processing between application and the skin. 
 */
public class InputMap
{
	private final Control control;
	// FID -> Runnable
	// KB -> FID or Runnable
	// EventType -> EHandlers
	private final CMap<Object,Object> map = new CMap<>(16);
	private final EventHandler<Event> eventHandler = this::handleEvent;
	private SkinInputMap skinInputMap;
	
	
	public InputMap(Control c)
	{
		this.control = c;
	}
	
	
	public void regKey(KB k, Runnable r)
	{
		map.put(k, r);
		// add key handler
	}
	
	
	public void regKey(KB k, FID f)
	{
		map.put(k, f);
		// add key handler
	}


	public void regFunc(FID f, Runnable r)
	{
		map.put(f, r);
	}
	

	/**
	 * Adds a user event handler which is guaranteed to be called before any of the skin's event handlers.
	 */
	public <T extends Event> void addHandler(EventType<T> type, EventHandler<T> h)
	{
		addHandler(type, h, HPriority.USER_EH);
	}

	
	private void handleEvent(Event ev)
	{
		// TODO
	}
	
	
	private <T extends Event> void addHandler(EventType<T> type, EventHandler<T> h, HPriority pri)
	{
		// TODO
	}
	
	
	public void setSkinInputMap(SkinInputMap m)
	{
		if(skinInputMap != null)
		{
			// TODO remove skin handlers
		}
		
		skinInputMap = m;
		
		if(skinInputMap != null)
		{
			// TODO add skin handlers
		}
	}


	public void exec(FID f)
	{
		Object v = map.get(f);
		if(v instanceof Runnable r)
		{
			r.run();
		}
	}
}
