// Copyright © 2020-2026 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.HPane;
import javafx.geometry.Pos;
import javafx.scene.control.Label;


/**
 * CodePad Status Bar.
 */
public class StatusBar
	extends HPane
{
	public static final CssStyle PANE = new CssStyle();
	public static final CssStyle LABEL_LEADING = new CssStyle();
	public static final CssStyle LABEL_TRAILING = new CssStyle();

	private final Label leading;
	private final Label trailing;
	
	
	public StatusBar()
	{
		PANE.set(this);
		
		leading = FX.label(LABEL_LEADING);
		
		trailing = FX.label(LABEL_TRAILING, Pos.CENTER_RIGHT);
		
		add(leading);
		fill();
		add(trailing);
	}
	
	
	public void setLeadingText(String s)
	{
		leading.setText(s);
	}
	
	
	public void setTrailingText(String s)
	{
		trailing.setText(s);
	}
}
