// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.fx.input;
import goryachev.common.util.CKit;
import goryachev.common.util.FH;
import goryachev.fx.input.internal.KMod;
import java.util.EnumSet;
import javafx.scene.input.KeyCode;


/**
 * Key Binding represents a key press (or release, or typed) event
 * with the optional modifier keys.
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
	
	
	public static Builder b(KeyCode k)
	{
		return new Builder(k);
	}
	
	
	public static Builder b(String ch)
	{
		return new Builder(ch);
	}
	
	
	private static KB create(Object k, KMod ... mods)
	{
		return new Builder(k, mods).build();
	}
	
	
	public static KB of(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED);
	}
	

	public static KB alt(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.ALT);
	}


	public static KB command(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.COMMAND);
	}
	
	
	public static KB ctrl(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.CTRL);
	}

	
	public static KB ctrlShift(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.CTRL, KMod.SHIFT);
	}
	
	
	public static KB meta(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.META);
	}
	
	
	public static KB option(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.OPTION);
	}
	
	
	public static KB shift(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.SHIFT);
	}
	
	
	public static KB shiftOption(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.SHIFT, KMod.OPTION);
	}
	
	
	public static KB shiftShortcut(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.SHIFT, KMod.SHORTCUT);
	}
	
	
	public static KB shortcut(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.SHORTCUT);
	}
	
	
	public static KB windows(KeyCode k)
	{
		return create(k, KMod.KEY_PRESSED, KMod.WINDOWS);
	}
	
	
	/**
	 * Builder.
	 */
	public static class Builder
	{
		private final Object key;
		private final EnumSet<KMod> mods = EnumSet.noneOf(KMod.class);
		
		
		public Builder(Object key)
		{
			this.key = key;
		}
		
		
		public Builder(Object key, KMod ... ms)
		{
			this(key);
			for(KMod m: ms)
			{
				mods.add(m);
			}
		}
		
		
		public Builder alt()
		{
			mods.add(KMod.ALT);
			return this;
		}
		
		
		public Builder command()
		{
			mods.add(KMod.COMMAND);
			return this;
		}
		
		
		public Builder ctrl()
		{
			mods.add(KMod.CTRL);
			return this;
		}
		
		
		public Builder meta()
		{
			mods.add(KMod.META);
			return this;
		}
		
		
		public Builder option()
		{
			mods.add(KMod.OPTION);
			return this;
		}
		
		
		public Builder shift()
		{
			mods.add(KMod.SHIFT);
			return this;
		}
		
		
		public Builder shortcut()
		{
			mods.add(KMod.ALT);
			return this;
		}
		
		
		public Builder keyReleased()
		{
			mods.add(KMod.KEY_RELEASED);
			return this;
		}
		
		
		public Builder keyTyped()
		{
			mods.add(KMod.KEY_TYPED);
			return this;
		}
		
		
		public KB build()
		{
			// TODO
			return null;
		}
	}
}
