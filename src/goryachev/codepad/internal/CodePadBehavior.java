// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.CodePad.FN;
import goryachev.codepad.TextPos;
import goryachev.fx.input.BehaviorBase;
import goryachev.fx.input.KB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;


/**
 * CodePad Behavior.
 */
public class CodePadBehavior
	extends BehaviorBase<CodePad>
{
	private final CellGrid grid;
	private boolean autoScrollUp;
	private boolean fastAutoScroll;
	private Timeline autoScrollTimer;
	
	
	public CodePadBehavior(CodePad c, CellGrid g)
	{
		super(c);
		this.grid = g;
	}


	@Override
	protected void populateSkinInputMap()
	{
		func(FN.MOVE_DOWN, this::moveDown);
		func(FN.MOVE_LEFT, this::moveLeft);
		func(FN.MOVE_RIGHT, this::moveRight);
		func(FN.MOVE_UP, this::moveUp);
		func(FN.PAGE_DOWN, this::pageDown);
		func(FN.PAGE_UP, this::pageUp);
		func(FN.SELECT_DOWN, this::selectDown);
		func(FN.SELECT_LEFT, this::selectLeft);
		func(FN.SELECT_PAGE_DOWN, this::selectPageDown);
		func(FN.SELECT_PAGE_UP, this::selectPageUp);
		func(FN.SELECT_RIGHT, this::selectRight);
		func(FN.SELECT_UP, this::selectUp);
		func(FN.SELECT_ALL, this::selectAll);
		
		key(KB.of(KeyCode.DOWN), FN.MOVE_DOWN);
		key(KB.of(KeyCode.LEFT), FN.MOVE_LEFT);
		key(KB.of(KeyCode.PAGE_DOWN), FN.PAGE_DOWN);
		key(KB.of(KeyCode.PAGE_UP), FN.PAGE_UP);
		key(KB.of(KeyCode.RIGHT), FN.MOVE_RIGHT);
		key(KB.of(KeyCode.UP), FN.MOVE_UP);
		// shift
		key(KB.shift(KeyCode.DOWN), FN.SELECT_DOWN);
		key(KB.shift(KeyCode.LEFT), FN.SELECT_LEFT);
		key(KB.shift(KeyCode.PAGE_DOWN), FN.SELECT_PAGE_DOWN);
		key(KB.shift(KeyCode.PAGE_UP), FN.SELECT_PAGE_UP);
		key(KB.shift(KeyCode.RIGHT), FN.SELECT_RIGHT);
		key(KB.shift(KeyCode.UP), FN.SELECT_UP);
		// shortcut
		key(KB.shortcut(KeyCode.A), FN.SELECT_ALL);
		
		grid.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
		grid.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		grid.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		grid.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
	}
	
	
	private void handleMouseClicked(MouseEvent ev)
	{
		// TODO primary button, 2 clicks: select word, 3 clicks: select paragraph 
	}

	
	private void handleMouseDragged(MouseEvent ev)
	{
		if(ev.getButton() != MouseButton.PRIMARY)
		{
			return;
		}

		double y = ev.getY();
		if(y < 0)
		{
			// above the view port
			autoScroll(y);
			return;
		}
		else if(y > grid.getHeight())
		{
			// below the view port
			autoScroll(y - grid.getHeight());
			return;
		}
		else
		{
			stopAutoScroll();
		}

		TextPos p = getTextPositionFor(ev);
		control().extendSelection(p);
	}

	
	private void handleMousePressed(MouseEvent ev)
	{
		control().requestFocus();
		
		if(ev.isPopupTrigger() || (ev.getButton() != MouseButton.PRIMARY))
		{
			return;
		}
		
		TextPos p = getTextPositionFor(ev);
		if(p == null)
		{
			return;
		}
		
		grid.suppressBlinking(true);
		if(ev.isShiftDown())
		{
			control().extendSelection(p);
		}
		else
		{
			control().select(p, p);
		}
	}

	
	private void handleMouseReleased(MouseEvent ev)
	{		
		// TODO
		grid.suppressBlinking(false);
		grid.clearPhantomX();
	}
	
	
	protected TextPos getTextPositionFor(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return control().getTextPositionFor(x, y);
	}
	
	
	public void moveDown()
	{
		move(true, 1, false);
	}
	
	
	public void moveLeft()
	{
		move(false, -1, false);
	}
	
	
	public void moveRight()
	{
		move(false, 1, false);
	}
	
	
	public void moveUp()
	{
		move(true, -1, false);
	}
	
	
	public void pageDown()
	{
		move(true, grid.getPageSize(), false);
	}
	
	
	public void pageUp()
	{
		move(true, -grid.getPageSize(), false);
	}
	
	
	public void selectDown()
	{
		move(true, 1, true);
	}
	
	
	public void selectLeft()
	{
		move(false, -1, true);
	}
	
	
	public void selectPageDown()
	{
		move(true, grid.getPageSize(), true);
	}
	
	
	public void selectPageUp()
	{
		move(true, -grid.getPageSize(), true);
	}
	
	
	public void selectRight()
	{
		move(false, 1, true);
	}
	
	
	public void selectUp()
	{
		move(true, -1, true);
	}

	
	private void move(boolean vertical, int delta, boolean select)
	{
		TextPos caret = control().getCaretPosition();
		if(caret != null)
		{
			TextPos p;
			if(vertical)
			{
				p = grid.verticalMove(caret, delta);
			}
			else
			{
				p = grid.horizontalMove(caret, delta);
			}
			moveCaret(p, select);
		}
	}
	
	
	// combine with previous method?
	private void moveCaret(TextPos p, boolean extendSelection)
	{
		if(extendSelection)
		{
			control().extendSelection(p);
		}
		else
		{
			control().select(p);
		}
	}
	
	
	public void selectAll()
	{
        TextPos end = control().getDocumentEnd();
        control().select(TextPos.ZERO, end);
	}
	
	
	private void autoScroll(double delta)
	{
		autoScrollUp = delta < 0;
		fastAutoScroll = Math.abs(delta) > Defaults.FAST_SCROLL_THRESHOLD;
		
		if(autoScrollTimer == null)
		{
			Duration autoScrollPeriod = Duration.millis(100); // arbitrary number
			autoScrollTimer = new Timeline(new KeyFrame(autoScrollPeriod, (ev) ->
			{
				autoScroll();
			}));
			autoScrollTimer.setCycleCount(Timeline.INDEFINITE);
			autoScrollTimer.play();
		}
	}
	
	
	private void stopAutoScroll()
	{
		if(autoScrollTimer != null)
		{
			autoScrollTimer.stop();
			autoScrollTimer = null;
		}
	}
	
	
	private void autoScroll()
	{
		// TODO move to defaults?
		double autoScrollStepFast = 200; // arbitrary
		double autoScrollStepSlow = 20; // arbitrary
		double delta = fastAutoScroll ? autoScrollStepFast : autoScrollStepSlow;
		if(autoScrollUp)
		{
			delta = -delta;
		}
		grid.blockScroll(delta);
		
		Point2D pt;
		if(autoScrollUp)
		{
			pt = grid.localToScreen(0, 0);
		}
		else
		{
			pt = grid.localToScreen(0, grid.getHeight());
		}
		
		TextPos p = control().getTextPositionFor(pt.getX(), pt.getY());
		if(p != null)
		{
			control().extendSelection(p);
		}
	}
}
