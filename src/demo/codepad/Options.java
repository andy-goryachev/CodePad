// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CodeModel;
import goryachev.fx.FX;
import demo.codepad.options.BooleanChoice;
import demo.codepad.options.ObjectChoice;
import demo.codepad.options.OptionsPane;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
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
		op.option("Content padding:", contentPaddingOption("contentPadding", ed.contentPaddingProperty()));
		op.option("Font:", null); // TODO
		op.option(new BooleanChoice("lineNumbers", "line numbers", null)); // TODO
		op.option(new BooleanChoice("wrapText", "wrap text", ed.wrapTextProperty()));

		return op;
	}
	
	
	private static Node contentPaddingOption(String name, ObjectProperty<Insets> p)
	{
		ObjectChoice<Insets> c = new ObjectChoice<>(name);
		c.add("<null>", null);
		c.add("10", new Insets(10));
		c.add("100", new Insets(100));
		c.add("T11.R22.B33.L44", new Insets(11, 22, 33, 44));
		FX.addChangeListener(c.getSelectionModel().selectedItemProperty(), (x) ->
		{
			Insets v = c.getChoice();
			p.set(v);
		});
		return c;
	}
	
	
	private static Node modelOption(String name, ObjectProperty<CodeModel> p)
	{
		ComboBox<DemoModels> c = new ComboBox<>();
		c.getItems().setAll(DemoModels.values());
		c.setConverter(FX.standardConverter());
		FX.addChangeListener(c.getSelectionModel().selectedItemProperty(), (m) ->
		{
			p.set(DemoModels.getModel(m));
		});
		return c;
	}
}
