// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.CodePad.FN;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CodeParagraph;
import goryachev.fx.input.BehaviorBase;
import goryachev.fx.input.KB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;


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
		//func(FN.BACKSPACE, this::);
		//func(FN.COPY, this::);
		//func(FN.COPY_PLAIN_TEXT, this::);
		//func(FN.CUT, this::);
		//func(FN.DELETE, this::);
		//func(FN.DELETE_PARAGRAPH, this::);
		//func(FN.DELETE_TO_PARAGRAPH_START, this::);
		//func(FN.DELETE_WORD_NEXT, this::);
		//func(FN.DELETE_WORD_PREVIOUS, this::);
		//func(FN.FOCUS_NEXT, this::);
		//func(FN.FOCUS_PREVIOUS, this::);
		//func(FN.INSERT_LINE_BREAK, this::);
		//func(FN.INSERT_TAB, this::);
		func(FN.MOVE_DOWN, this::moveDown);
		func(FN.MOVE_LEFT, this::moveLeft);
		func(FN.MOVE_RIGHT, this::moveRight);
		//func(FN.MOVE_TO_DOCUMENT_END, this::);
		//func(FN.MOVE_TO_DOCUMENT_START, this::);
		//func(FN.MOVE_TO_LINE_END, this::);
		//func(FN.MOVE_TO_LINE_START, this::);
		//func(FN.MOVE_TO_PARAGRAPH_END, this::);
		//func(FN.MOVE_TO_PARAGRAPH_START, this::);
		func(FN.MOVE_UP, this::moveUp);
		//func(FN.MOVE_WORD_LEFT, this::);
		//func(FN.MOVE_WORD_RIGHT, this::);
		func(FN.PAGE_DOWN, this::pageDown);
		func(FN.PAGE_UP, this::pageUp);
		//func(FN.PASTE, this::);
		//func(FN.PASTE_PLAIN_TEXT, this::);
		//func(FN.REDO, this::);
		func(FN.SELECT_ALL, this::selectAll);
		func(FN.SELECT_DOWN, this::selectDown);
		func(FN.SELECT_LEFT, this::selectLeft);
		func(FN.SELECT_PAGE_DOWN, this::selectPageDown);
		func(FN.SELECT_PAGE_UP, this::selectPageUp);
		func(FN.SELECT_PARAGRAPH, this::selectParagraph);
		func(FN.SELECT_RIGHT, this::selectRight);
		//func(FN.SELECT_TO_DOCUMENT_END, this::);
		//func(FN.SELECT_TO_DOCUMENT_START, this::);
		//func(FN.SELECT_TO_LINE_END, this::);
		//func(FN.SELECT_TO_LINE_START, this::);
		func(FN.SELECT_UP, this::selectUp);
		//func(FN.SELECT_WORD, this::);
		//func(FN.SELECT_WORD_LEFT, this::);
		//func(FN.SELECT_WORD_RIGHT, this::);
		//func(FN.UNDO, this::);
		
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
		
		grid.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
		grid.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		grid.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		grid.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
		
		// FIX does not work???
		//grid.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
		//grid.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
	}
	
	
	private void handleMouseClicked(MouseEvent ev)
	{
		if(ev.getButton() == MouseButton.PRIMARY)
		{
			int clicks = ev.getClickCount();
			switch(clicks)
			{
			case 2:
				control().selectWord();
				ev.consume();
				break;
			case 3:
				control().selectParagraph();
				ev.consume();
				break;
			}
		}
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
		ev.consume();
	}

	
	private void handleMouseReleased(MouseEvent ev)
	{		
		// TODO
		grid.suppressBlinking(false);
		grid.clearPhantomX();
	}
	
	
	// FIX dnw
	private void handleKeyPressed(KeyEvent ev)
	{
		grid.suppressBlinking(true);
	}
	
	
	// FIX dnw
	private void handleKeyReleased(KeyEvent ev)
	{
		grid.suppressBlinking(false);
	}
	
	
	public TextPos getTextPositionFor(MouseEvent ev)
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
	
	
	public boolean selectParagraph()
	{
		TextPos p = control().getCaretPosition();
		if(p != null)
		{
			int ix = p.index();
			TextPos start = TextPos.of(ix, 0);
			ix++;
			TextPos end;
			if(ix <= control().getParagraphCount())
			{
				end = TextPos.of(ix, 0);
			}
			else
			{
				end = start;
			}
			control().select(start, end);
			return true;
		}
		return false;
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
		if(p != null)
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
			autoScrollTimer = new Timeline(new KeyFrame(Defaults.AUTO_SCROLL_PERIOD, (ev) ->
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
		double delta = fastAutoScroll ? Defaults.AUTO_SCROLL_STEP_FAST : Defaults.AUTO_SCROLL_STEP_SLOW;
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
