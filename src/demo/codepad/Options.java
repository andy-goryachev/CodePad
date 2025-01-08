// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.Defaults;
import goryachev.codepad.model.CodeModel;
import goryachev.fx.FX;
import demo.codepad.models.DemoModels;
import demo.codepad.options.BooleanChoice;
import demo.codepad.options.ColorChoice;
import demo.codepad.options.DoubleChoice;
import demo.codepad.options.FontChoice;
import demo.codepad.options.IntChoice;
import demo.codepad.options.ObjectChoice;
import demo.codepad.options.OptionsPane;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;


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
		op.option(new BooleanChoice("editable", "Editable", null)); // TODO

		// view
		op.section("View");
		op.option("Aspect Ratio:", DoubleChoice.of("aspectRatio", ed.aspectRatioProperty(), 0, Defaults.ASPECT_RATIO_MIN, Defaults.ASPECT_RATIO, Defaults.ASPECT_RATIO_MAX));
		op.option("Content padding:", contentPaddingOption("contentPadding", ed.contentPaddingProperty()));
		op.option(new BooleanChoice("displayCaret", "display caret", ed.displayCaretProperty()));
		op.option("Font:", new FontChoice("font", ed.fontProperty()));
		op.option(new BooleanChoice("lineNumbers", "line numbers", null)); // TODO
		op.option("Line Spacing:", DoubleChoice.of("lineSpacing", ed.lineSpacingProperty(), 0, 1, 2, 5, 10, 33.3));
		op.option("Tab Size:", IntChoice.of("tabSize", ed.tabSizeProperty(), 0, 1, 3, 4, 8, 16));
		// FIX
		op.option(new BooleanChoice("wrapText", "wrap text", ed.wrapTextProperty()));
		{
			Button b1 = new Button("Select 0");
			b1.setOnAction((ev) ->
			{
				ed.select(TextPos.ZERO);
			});
			Button b2 = new Button("Select Lines");
			b2.setOnAction((ev) ->
			{
				ed.select(TextPos.ZERO, TextPos.of(2, 1));
			});
			op.option(new HBox(2, b1, b2));
		}

		// colors
		op.section("Colors");
		op.option("Background:", new ColorChoice("backgroundColor", ed.backgroundColorProperty()));
		op.option("Caret Color:", new ColorChoice("caretColor", ed.caretColorProperty()));
		op.option("Caret Line Color:", new ColorChoice("caretLineColor", ed.caretLineColorProperty()));
		op.option("Selection Color:", new ColorChoice("selectionColor", ed.selectionColorProperty()));
		op.option("Text Color:", new ColorChoice("textColor", ed.textColorProperty()));
		
		// control
		op.section("Control");
		op.option(new BooleanChoice("disabled", "disabled", ed.disableProperty()));
		op.option(new BooleanChoice("focused", "focused", ed.focusedProperty()));

		return op;
	}


	private static Node contentPaddingOption(String name, ObjectProperty<Insets> p)
	{
		ObjectChoice<Insets> c = new ObjectChoice<>(name);
		c.add("<null>", null);
		c.add("0", new Insets(0));
		c.add("1", new Insets(1));
		c.add("2", new Insets(2));
		c.add("3", new Insets(3));
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
		
		Button b = new Button("Reload");
		b.setOnAction((ev) ->
		{
			DemoModels s = c.getSelectionModel().getSelectedItem();
			CodeModel m = DemoModels.getModel(s);
			p.set(m);
		});
		return new HBox(c, b);
	}
}
