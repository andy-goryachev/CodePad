// Copyright © 2017-2025 Andy Goryachev <andy@goryachev.com>
package demo.codepad;
import goryachev.codepad.CodePad;
import goryachev.fx.CssStyle;
import goryachev.fx.FX;
import goryachev.fx.FxFramework;
import goryachev.fx.FxMenuBar;
import goryachev.fx.FxPopupMenu;
import goryachev.fx.FxWindow;
import goryachev.fx.settings.FxSettingsSchema;
import goryachev.fx.settings.LocalSettings;
import demo.codepad.options.OptionsPane;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;


/**
 * CodePad Tester Window.
 */
public class TesterWindow
	extends FxWindow
{
	public static final CssStyle PANE = new CssStyle();

	public final StatusBar statusBar;
	public final CodePad editor;
	private final OptionsPane options;
	private BorderPane pane;
	private final SimpleBooleanProperty viewProperties = new SimpleBooleanProperty(true);

	
	public TesterWindow()
	{
		super("TesterWindow");
		
		editor = new CodePad(null);
		editor.setContentPadding(FX.insets(2, 4));
		
		options = Options.create(editor);
		
		pane = new BorderPane();
		pane.setCenter(editor);
		PANE.set(pane);
		
		statusBar = new StatusBar();
		
		setTitle("CodePad Tester");
		setTop(createMenu());
		setCenter(pane);
		setBottom(statusBar);
		setSize(600, 700);
		
		statusBar.attach(editor);
		
		FX.setPopupMenu(editor, this::createPopupMenu);
		FX.addChangeListener(viewProperties, true, this::handleViewProperties);
		
		LocalSettings.get(this).add("viewProperties", viewProperties);
	}
	
	
	private void handleViewProperties(boolean on)
	{
		if(on)
		{
			pane.setRight(options);
			FxFramework.restore(pane);
		}
		else
		{
			FxFramework.store(pane);
			pane.setRight(null);
		}
	}
	
	
	protected Node createMenu()
	{
		FxMenuBar m = new FxMenuBar();
//		Actions a = editor().actions;
		
		// file
		m.menu("File");
		m.item("New Window, Same Model", this::newWindow);
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
//		m.item("Select Line");
//		m.item("Split Selection into Lines");
//		m.separator();
//		m.item("Indent");
//		m.item("Unindent");
//		m.item("Duplicate");
//		m.item("Delete Line");
//		m.item("Move Line Up");
//		m.item("Move Line Down");
		
		// view
		m.menu("View");
		m.checkItem("Properties Pane", viewProperties);		

		// help
		m.menu("Help");
		m.item("About");
		
		return m;
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

	
	protected void newWindow()
	{
		TesterWindow w = new TesterWindow();
		w.editor.setModel(editor.getModel());
		w.open();
	}
}