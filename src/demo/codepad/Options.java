// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.fx.FX;
import demo.codepad.options.BooleanChoice;
import demo.codepad.options.OptionsPane;
import javafx.beans.property.SimpleBooleanProperty;


/**
 * Options.
 */
public class Options
{
	public static OptionsPane create(CodePad ed)
	{
		OptionsPane op = new OptionsPane();
		FX.setName(op, "Options");
		
		// model
		op.section("Model");
		op.option(new BooleanChoice("editable", "editable", null)); // TODO
		
		// view
		op.section("View");
		op.option("Content padding:", null); // TODO
		op.option("Font:", null); // TODO
		op.option(new BooleanChoice("lineNumbers", "line numbers", null)); // TODO
		op.option(new BooleanChoice("wrapText", "wrap text", ed.wrapTextProperty()));

		return op;
	}
}
