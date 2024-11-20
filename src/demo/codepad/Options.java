// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CodeModel;
import goryachev.fx.FX;
import demo.codepad.options.BooleanChoice;
import demo.codepad.options.OptionsPane;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;


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
		op.section("Data");
		op.option("Model:", modelOption("model", ed.modelProperty()));
		op.option(new BooleanChoice("editable", "editable", null)); // TODO
		
		// view
		op.section("View");
		op.option("Content padding:", null); // TODO
		op.option("Font:", null); // TODO
		op.option(new BooleanChoice("lineNumbers", "line numbers", null)); // TODO
		op.option(new BooleanChoice("wrapText", "wrap text", ed.wrapTextProperty()));

		return op;
	}
	
	
	private static Node modelOption(String name, ObjectProperty<CodeModel> p)
	{
		ComboBox<DemoModels> n = new ComboBox<>();
		n.getItems().setAll(DemoModels.values());
		n.setConverter(FX.standardConverter());
		FX.addChangeListener(n.getSelectionModel().selectedItemProperty(), (m) ->
		{
			p.set(DemoModels.getModel(m));
		});
		return n;
	}
}
