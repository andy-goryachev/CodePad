// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.skin;
import goryachev.codepad.CodePad;
import goryachev.codepad.TextPos;
import goryachev.codepad.internal.CellGrid;
import goryachev.codepad.internal.CodePadBehavior;
import goryachev.codepad.internal.Defaults;
import goryachev.fx.FX;
import goryachev.fx.FxDisconnector;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;


/**
 * CodePad Skin.
 */
public class CodePadSkin
	extends SkinBase<CodePad>
{
	private final ScrollBar vscroll;
	private final ScrollBar hscroll;
	private final CellGrid grid;
	private final CodePadBehavior behavior;
	private FxDisconnector disconnector;


	public CodePadSkin(CodePad ed)
	{
		super(ed);

		vscroll = createVScrollBar();
		vscroll.setOrientation(Orientation.VERTICAL);
		FX.consumeAllEvents(ScrollEvent.ANY, vscroll);

		hscroll = createHScrollBar();
		hscroll.setOrientation(Orientation.HORIZONTAL);
		FX.consumeAllEvents(ScrollEvent.ANY, hscroll);

		grid = new CellGrid(this, vscroll, hscroll);
		getChildren().add(grid);

		behavior = new CodePadBehavior(ed, grid);
		
		disconnector = new FxDisconnector();
		disconnector.addChangeListener(ed.aspectRatioProperty(), true, grid::setAspectRatio);
		disconnector.addInvalidationListener
		(
			grid::paintAll,
			ed.backgroundColorProperty(),
			ed.caretColorProperty(),
			ed.caretLineColorProperty(),
			ed.selectionColorProperty(),
			ed.textColorProperty()
		);
		disconnector.addChangeListener(ed.contentPaddingProperty(), true, grid::setContentPadding);
		disconnector.addChangeListener(ed.fontProperty(), true, grid::setFont);
		disconnector.addInvalidationListener(ed.lineSpacingProperty(), grid::handleLineSpacingChange);
		disconnector.addInvalidationListener(ed.modelProperty(), grid::handleModelChange);
		disconnector.addChangeListener(ed.wrapTextProperty(), true, grid::setWrapText);
		disconnector.addInvalidationListener(grid::handleVerticalScroll, vscroll.valueProperty());
		disconnector.addInvalidationListener(grid::handleHorizontalScroll, hscroll.valueProperty());
		disconnector.addChangeListener(ed.selectionProperty(), false, grid::handleSelectionChange);
		disconnector.addEventFilter(ed, KeyEvent.KEY_PRESSED, (ev) -> grid.suppressBlinking(true));
		disconnector.addEventFilter(ed, KeyEvent.KEY_RELEASED, (ev) -> grid.suppressBlinking(false));
		disconnector.addEventFilter(vscroll, MouseEvent.MOUSE_PRESSED, grid::handleScrollBarMousePressed);
		disconnector.addEventFilter(vscroll, MouseEvent.MOUSE_RELEASED, grid::handleScrollBarMouseReleased);
		disconnector.addEventFilter(hscroll, MouseEvent.MOUSE_PRESSED, grid::handleScrollBarMousePressed);
		disconnector.addEventFilter(hscroll, MouseEvent.MOUSE_RELEASED, grid::handleScrollBarMouseReleased);

	}


	/**
	 * Subclasses can override this method to provide a custom vertical scroll bar.
	 */
	protected ScrollBar createVScrollBar()
	{
		// TODO support uncinditionally hiding the scrollbar
		return new ScrollBar();
	}


	/**
	 * Subclasses can override this method to provide a custom horizontal scroll bar.
	 */
	protected ScrollBar createHScrollBar()
	{
		// TODO support uncinditionally hiding the scrollbar
		return new ScrollBar();
	}


	@Override
	public void install()
	{
		super.install();
		getSkinnable().getInputMap().setSkinInputMap(behavior.getSkinInputMap());
	}
	
	
	@Override
	public void dispose()
	{
		if(disconnector != null)
		{
			disconnector.disconnect();
			disconnector = null;
			
			getSkinnable().getInputMap().setSkinInputMap(null);

			super.dispose();
		}
	}


	@Override
	protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return Defaults.PREF_HEIGHT;
	}


	@Override
	protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return Defaults.PREF_WIDTH;
	}


	@Override
	protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return Defaults.MIN_HEIGHT;
	}


	@Override
	protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
	{
		return Defaults.MIN_WIDTH;
	}
	
	
	public TextPos getTextPositionFor(double screenx, double screeny)
	{
		if(getSkinnable().getModel() == null)
		{
			return TextPos.ZERO;
		}
		Point2D p = grid.screenToLocal(screenx, screeny);
		return grid.textPosAtPoint(p);
	}


	public void clearPhantomX()
	{
		grid.clearPhantomX();
	}
}
