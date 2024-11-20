// Copyright © 2017-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.codepad.model.CodeModel;
import goryachev.common.util.Parsers;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.FxAction;
import goryachev.fx.FxBoolean;
import goryachev.fx.FxComboBox;
import goryachev.fx.FxFramework;
import goryachev.fx.FxMenuBar;
import goryachev.fx.FxPopupMenu;
import goryachev.fx.FxToggleButton;
import goryachev.fx.FxToolBar;
import goryachev.fx.FxWindow;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


/**
 * CodePad Tester Window.
 */
public class CodePadTesterWindow
	extends FxWindow
{
	public static final CssStyle PANE = new CssStyle("CodePadDemoPane_PANE");

	public final StatusBar statusBar;
	protected final FxComboBox<DemoModels> modelSelector = new FxComboBox();
	protected final FxComboBox fontSelector = new FxComboBox();
	private final FxToggleButton wrap = new FxToggleButton("wr");
	private final FxBoolean contentPadding = new FxBoolean();
	private final BorderPane pane;
	public final CodePad editor;

	
	public CodePadTesterWindow()
	{
		super("CodePadTesterWindow");
		
		modelSelector.setItems(DemoModels.values());
		modelSelector.valueProperty().addListener((s,p,c) -> onModelSelectionChange(c));
		FX.setName(modelSelector, "modelSelector");
		
		fontSelector.setItems
		(
			"9",
			"10",
			"11",
			"12",
			"13",
			"14",
			"16",
			"18",
			"20",
			"24",
			"28",
			"32"
		);
		fontSelector.valueProperty().addListener((s,p,c) -> handleFontChange(c));
		FX.setName(fontSelector, "fontSelector");
		
		editor = new CodePad(null);
		editor.setContentPadding(FX.insets(2, 4));
//		editor.setBlinkRate(Duration.millis(600));
//		editor.setWrapLines(false);
//		editor.setTabPolicy(TabPolicy.create(4));
		
		pane = new BorderPane();
		pane.setCenter(editor);
		FX.style(pane, PANE);
		
		statusBar = new StatusBar();
		
		setTitle("CodePad Tester");
		setTop(createMenu());
		setTop(createToolbar());
		setCenter(pane);
		setBottom(statusBar);
		setSize(600, 700);
		
		fontSelector.setEditable(true);
		fontSelector.select("12");
		
		wrap.selectedProperty().bindBidirectional(editor.wrapTextProperty());
		
		statusBar.attach(editor);
		
		FX.setPopupMenu(editor, this::createPopupMenu);
		
		FX.addChangeListener(contentPadding, true, this::updateContentPadding);
	}
	
	
	protected Node createMenu()
	{
		FxMenuBar m = new FxMenuBar();
//		Actions a = editor().actions;
		
		// file
		m.menu("File");
		m.item("New Window, Same Model", new FxAction(this::newWindow));
		m.separator();
		m.item("Preferences");
		m.separator();
		m.item("Exit", FxFramework::exit);
		
		// edit
		m.menu("Edit");
		m.item("Undo");
		m.item("Redo");
		m.separator();
		m.item("Cut");
//		m.item("Copy", a.copy());
		m.item("Paste");
		m.separator();
//		m.item("Select All", a.selectAll());
		m.item("Select Line");
		m.item("Split Selection into Lines");
		m.separator();
		m.item("Indent");
		m.item("Unindent");
		m.item("Duplicate");
		m.item("Delete Line");
		m.item("Move Line Up");
		m.item("Move Line Down");

		// find
		m.menu("Find");
		m.item("Find");
		m.item("Regex");
		m.item("Replace");
		m.separator();
		m.item("Find Next");
		m.item("Find Previous");
		m.item("Find and Select");
		m.separator();
		m.item("Go to Line");
		
		// view
//		m.menu("View");
//		m.item("Show Line Numbers", editor().showLineNumbersProperty());
//		m.item("Wrap Lines", editor().wrapLinesProperty());
		
		// help
		m.menu("Help");
		m.item("About");
		
		return m;
	}
	
	
	protected Node createToolbar()
	{
		FxToolBar t = new FxToolBar();
		t.addToggleButton("wr", "wrap lines", editor.wrapTextProperty());
		t.addToggleButton("cp", "content padding", contentPadding);
		// TODO
//		t.addToggleButton("ln", "line numbers", editor().showLineNumbersProperty());
		t.fill();
		t.add(new Label("Font:"));
		t.add(fontSelector);
		t.space();
		t.add(new Label("Model:"));
		t.space(2);
		t.add(modelSelector);
		return t;
	}
	
	
	protected FxPopupMenu createPopupMenu()
	{
		FxPopupMenu p = new FxPopupMenu();
//		FxMenu m = p.menu("Copy", editor.actions.copy());
//		{
//			m.item("Copy Plain Text", editor.actions.copyPlainText());
//			m.item("RTF", editor.actions.copyRtf());
//			m.item("HTML", editor.actions.copyHtml());
//		}
//		m = p.menu("Smart Copy", editor.actions.smartCopy());
//		{
//			m.item("Plain Text", editor.actions.smartCopyPlainText());
//			m.item("RTF", editor.actions.smartCopyRtf());
//			m.item("HTML", editor.actions.smartCopyHtml());
//		}
//		p.separator();
//		p.item("Select All", editor.actions.selectAll());
		p.item("Select All");
		return p;
	}
	
	
	protected void preferences()
	{
	}
	
	
	protected void newWindow()
	{
		CodePadTesterWindow w = new CodePadTesterWindow();
		editor.setModel(editor.getModel());
		w.open();
	}
	
	
	protected void onModelSelectionChange(DemoModels x)
	{
		CodeModel m = DemoModels.getModel(x);
		editor.setModel(m);
	}
	
	
	protected void handleFontChange(Object x)
	{
		double sz = Parsers.parseDouble(x, Font.getDefault().getSize());
		Font f = editor.getFont();
		f = Font.font(f.getFamily(), sz);
		editor.setFont(f);
	}
	
	
	void updateContentPadding(boolean on)
	{
		editor.setContentPadding(on ? new Insets(20, 30, 40, 50) : null);
	}
}