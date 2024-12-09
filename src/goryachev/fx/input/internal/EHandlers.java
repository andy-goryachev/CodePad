// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input.internal;
import javafx.event.Event;
import javafx.event.EventHandler;


/**
 * Prioritized Event Handler List.
 */
public class EHandlers
{
	public static interface Client<T extends Event>
	{
		// returns true if the caller should continue iterating over handlers, false to stop
		public boolean process(HPriority pri, EventHandler<T> handler);
	}
	
	
	//
	
	
	public EHandlers()
	{
	}
	

	public void add(HPriority pri, EventHandler<?> handler)
	{
		// TODO
	}


	// returns true if no more skin event handlers left
	public boolean removeSkinHandlers()
	{
		// TODO
		return false;
	}


	public void forEachHandler(Client c)
	{
	}
}
