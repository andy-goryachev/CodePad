// Copyright © 2020-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.codepad.SelectionRange;
import goryachev.codepad.TextPos;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.Formatters;
import goryachev.fx.FxFormatter;
import goryachev.fx.HPane;
import javafx.beans.binding.Bindings;
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
		
		trailing = FX.label(LABEL_TRAILING, Pos.CENTER_RIGHT, CodePadTesterApp.COPYRIGHT);
		
		add(leading);
		fill();
		add(trailing);
	}


	public void attach(CodePad ed)
	{
		leading.textProperty().bind(Bindings.createStringBinding
		(
			() ->
			{
				SelectionRange sel = ed.getSelection();
				if(sel == null)
				{
					return null;
				}
				
				FxFormatter fmt = Formatters.integerFormatter();
				
				TextPos p = sel.getCaret();
				String line = fmt.format(p.getLineNumber());
				String ix = fmt.format(p.getColumn());
				String bias = p.isLeading() ? "(leading)" : "(trailing)";
				
				return String.format("line: %s  char: %s %s", line, ix, bias);  
			},
			ed.selectionProperty()
		));
	}
}
