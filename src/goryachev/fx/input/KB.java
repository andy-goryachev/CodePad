// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import goryachev.common.util.CKit;
import goryachev.common.util.FH;
import goryachev.fx.input.internal.KMod;
import java.util.EnumSet;
import javafx.scene.input.KeyCode;


/**
 * Key Binding represents a key press (or release, or typed) event
 * with optional modifier keys.
 */
public class KB
{
	private final Object key;
	private final EnumSet<KMod> modifiers;
	
	
	private KB(Object key, EnumSet<KMod> modifiers)
	{
		this.key = key;
		this.modifiers = modifiers;
	}
	
	
	@Override
	public int hashCode()
	{
		int h = FH.hash(KB.class);
		h = FH.hash(h, key);
		return FH.hash(h, modifiers);
	}
	
	
	@Override
	public boolean equals(Object x)
	{
		if(x == this)
		{
			return true;
		}
		else if(x instanceof KB k)
		{
			return CKit.equals(key, k.key) && modifiers.equals(k.modifiers);
		}
		return false;
	}
	
	
	private static KB create(Object k, KMod ... mods)
	{
		// TODO I need a builder, because there are too many possible combinations with key pressed/released/typed
		return null;
	}
	
	
	public static KB of(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED);
	}
}
