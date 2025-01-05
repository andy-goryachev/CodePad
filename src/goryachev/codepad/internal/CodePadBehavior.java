// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.CodePad;
import goryachev.codepad.CodePad.FN;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CodeParagraph;
import goryachev.common.util.D;
import goryachev.fx.input.BehaviorBase;
import goryachev.fx.input.KB;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
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
		// functions
		func(FN.BACKSPACE, this::backspace);
		func(FN.COPY, this::copy);
		func(FN.COPY_PLAIN_TEXT, this::copyPlainText);
		func(FN.CUT, this::cut);
		func(FN.DELETE, this::delete);
		func(FN.DELETE_PARAGRAPH, this::deleteParagraph);
		func(FN.DELETE_TO_PARAGRAPH_START, this::deleteToParagraphStart);
		func(FN.DELETE_WORD_NEXT, this::deleteWordNext);
		func(FN.DELETE_WORD_PREVIOUS, this::deleteWordPrevious);
		func(FN.FOCUS_NEXT, this::focusNext);
		func(FN.FOCUS_PREVIOUS, this::focusPrevious);
		func(FN.INSERT_LINE_BREAK, this::insertLineBreak);
		func(FN.INSERT_TAB, this::insertTab);
		func(FN.MOVE_DOWN, this::moveDown);
		func(FN.MOVE_LEFT, this::moveLeft);
		func(FN.MOVE_RIGHT, this::moveRight);
		func(FN.MOVE_TO_DOCUMENT_END, this::moveToDocumentEnd);
		func(FN.MOVE_TO_DOCUMENT_START, this::moveToDocumentStart);
		func(FN.MOVE_TO_LINE_END, this::moveToLineEnd);
		func(FN.MOVE_TO_LINE_START, this::moveToLineStart);
		func(FN.MOVE_TO_PARAGRAPH_END, this::moveToParagraphEnd);
		func(FN.MOVE_TO_PARAGRAPH_START, this::moveToParagraphStart);
		func(FN.MOVE_UP, this::moveUp);
		func(FN.MOVE_WORD_LEFT, this::moveWordLeft);
		func(FN.MOVE_WORD_RIGHT, this::moveWordRight);
		func(FN.PAGE_DOWN, this::pageDown);
		func(FN.PAGE_UP, this::pageUp);
		func(FN.PASTE, this::paste);
		func(FN.PASTE_PLAIN_TEXT, this::pastePlainText);
		func(FN.REDO, this::redo);
		func(FN.SELECT_ALL, this::selectAll);
		func(FN.SELECT_DOWN, this::selectDown);
		func(FN.SELECT_LEFT, this::selectLeft);
		func(FN.SELECT_PAGE_DOWN, this::selectPageDown);
		func(FN.SELECT_PAGE_UP, this::selectPageUp);
		func(FN.SELECT_PARAGRAPH, this::selectParagraph);
		func(FN.SELECT_RIGHT, this::selectRight);
		func(FN.SELECT_TO_DOCUMENT_END, this::selectToDocumentEnd);
		func(FN.SELECT_TO_DOCUMENT_START, this::selectToDocumentStart);
		func(FN.SELECT_TO_LINE_END, this::selectToLineEnd);
		func(FN.SELECT_TO_LINE_START, this::selectToLineStart);
		func(FN.SELECT_TO_PARAGRAPH_END, this::selectToParagraphEnd);
		func(FN.SELECT_TO_PARAGRAPH_START, this::selectToParagraphStart);
		func(FN.SELECT_UP, this::selectUp);
		func(FN.SELECT_WORD, this::selectWord);
		func(FN.SELECT_WORD_LEFT, this::selectWordLeft);
		func(FN.SELECT_WORD_RIGHT, this::selectWordRight);
		func(FN.UNDO, this::undo);
		
		// key bindings
		key(KB.of(KeyCode.BACK_SPACE), FN.BACKSPACE);
		key(KB.of(KeyCode.COPY), FN.COPY);
		key(KB.of(KeyCode.CUT), FN.CUT);
		key(KB.of(KeyCode.DELETE), FN.DELETE);
		key(KB.of(KeyCode.DOWN), FN.MOVE_DOWN);
		key(KB.of(KeyCode.END), FN.MOVE_TO_LINE_END);
		key(KB.of(KeyCode.ENTER), FN.INSERT_LINE_BREAK);
		key(KB.of(KeyCode.HOME), FN.MOVE_TO_LINE_START);
		key(KB.of(KeyCode.LEFT), FN.MOVE_LEFT);
		key(KB.of(KeyCode.PAGE_DOWN), FN.PAGE_DOWN);
		key(KB.of(KeyCode.PAGE_UP), FN.PAGE_UP);
		key(KB.of(KeyCode.PASTE), FN.PASTE);
		key(KB.of(KeyCode.RIGHT), FN.MOVE_RIGHT);
		key(KB.of(KeyCode.TAB), FN.INSERT_TAB);
		key(KB.of(KeyCode.UNDO), FN.UNDO);
		key(KB.of(KeyCode.UP), FN.MOVE_UP);
		// ctrl
		key(KB.ctrl(KeyCode.END), FN.MOVE_TO_DOCUMENT_END);
		key(KB.ctrl(KeyCode.HOME), FN.MOVE_TO_DOCUMENT_START);
		// ctrl-shift
		key(KB.ctrlShift(KeyCode.END), FN.SELECT_TO_DOCUMENT_END);
		key(KB.ctrlShift(KeyCode.HOME), FN.SELECT_TO_DOCUMENT_START);
		// shift
		key(KB.shift(KeyCode.BACK_SPACE), FN.BACKSPACE);
		key(KB.shift(KeyCode.DOWN), FN.SELECT_DOWN);
		key(KB.shift(KeyCode.END), FN.SELECT_TO_LINE_END);
		key(KB.shift(KeyCode.HOME), FN.SELECT_TO_LINE_START);
		key(KB.shift(KeyCode.LEFT), FN.SELECT_LEFT);
		key(KB.shift(KeyCode.PAGE_DOWN), FN.SELECT_PAGE_DOWN);
		key(KB.shift(KeyCode.PAGE_UP), FN.SELECT_PAGE_UP);
		key(KB.shift(KeyCode.RIGHT), FN.SELECT_RIGHT);
		key(KB.shift(KeyCode.UP), FN.SELECT_UP);
		// shortcut
		key(KB.shortcut(KeyCode.A), FN.SELECT_ALL);
		key(KB.shortcut(KeyCode.C), FN.COPY);
		key(KB.shortcut(KeyCode.D), FN.DELETE_PARAGRAPH);
		key(KB.shortcut(KeyCode.V), FN.PASTE);
		key(KB.shortcut(KeyCode.X), FN.CUT);
		key(KB.shortcut(KeyCode.Z), FN.UNDO);
		// shift-shortcut
		key(KB.shiftShortcut(KeyCode.V), FN.PASTE_PLAIN_TEXT);
		
		if(isLinux())
		{
			key(KB.ctrlShift(KeyCode.Z), FN.REDO);
		}
		
		if(isMac())
		{
			// command
			key(KB.command(KeyCode.DOWN), FN.MOVE_TO_DOCUMENT_END);
			key(KB.command(KeyCode.LEFT), FN.MOVE_TO_LINE_START);
			key(KB.command(KeyCode.RIGHT), FN.MOVE_TO_LINE_END);
			key(KB.command(KeyCode.UP), FN.MOVE_TO_DOCUMENT_START);
			// command-shift
			key(KB.commandShift(KeyCode.DOWN), FN.SELECT_TO_DOCUMENT_END);
			key(KB.commandShift(KeyCode.LEFT), FN.SELECT_TO_LINE_START);
			key(KB.commandShift(KeyCode.RIGHT), FN.SELECT_TO_LINE_END);
			key(KB.commandShift(KeyCode.UP), FN.SELECT_TO_DOCUMENT_START);
			key(KB.commandShift(KeyCode.Z), FN.REDO);
			// option
			key(KB.option(KeyCode.BACK_SPACE), FN.DELETE_WORD_PREVIOUS);
			key(KB.option(KeyCode.DELETE), FN.DELETE_WORD_NEXT);
			key(KB.option(KeyCode.LEFT), FN.MOVE_WORD_LEFT);
			key(KB.option(KeyCode.RIGHT), FN.MOVE_WORD_RIGHT);
			// option-shift
			key(KB.optionShift(KeyCode.LEFT), FN.SELECT_WORD_LEFT);
			key(KB.optionShift(KeyCode.RIGHT), FN.SELECT_WORD_RIGHT);
		}
		else
		{
			// ctrl
			key(KB.ctrl(KeyCode.BACK_SPACE), FN.DELETE_WORD_PREVIOUS);
			key(KB.ctrl(KeyCode.DELETE), FN.DELETE_WORD_NEXT);
			key(KB.ctrl(KeyCode.LEFT), FN.MOVE_WORD_LEFT);
			key(KB.ctrl(KeyCode.RIGHT), FN.MOVE_WORD_RIGHT);
			// ctrl-shift
			key(KB.ctrlShift(KeyCode.LEFT), FN.SELECT_WORD_LEFT);
			key(KB.ctrlShift(KeyCode.RIGHT), FN.SELECT_WORD_RIGHT);
		}
		
		if(isWindows())
		{
			// ctrl
			key(KB.ctrl(KeyCode.Y), FN.REDO);
		}
		
		grid.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleMouseClicked);
		grid.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
		grid.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
		grid.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
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
		stopAutoScroll();
		grid.suppressBlinking(false);
		grid.clearPhantomX();
	}
	
	
	public TextPos getTextPositionFor(MouseEvent ev)
	{
		double x = ev.getScreenX();
		double y = ev.getScreenY();
		return control().getTextPositionFor(x, y);
	}
	
	
	private TextPos lineEnd()
	{
		TextPos ca = control().getCaretPosition();
		if(ca != null)
		{
			if(control().isWrapText())
			{
				return grid.lineEnd(ca);
			}
			else
			{
				CodeParagraph par = control().getParagraph(ca.index());
				return TextPos.trailing(ca.index(), par.getCellCount());
			}
		}
		return null;		
	}
	
	
	private TextPos lineStart()
	{
		TextPos ca = control().getCaretPosition();
		if(ca != null)
		{
			if(control().isWrapText())
			{
				return grid.lineStart(ca);
			}
			else
			{
				return TextPos.of(ca.index(), 0);
			}
		}
		return null;
	}

	
	private TextPos paragraphEnd()
	{
		TextPos ca = control().getCaretPosition();
		if(ca != null)
		{
			CodeParagraph par = control().getParagraph(ca.index());
			return TextPos.of(ca.index(), par.getCellCount());
		}
		return null;
	}
	
	
	private TextPos paragraphStart()
	{
		TextPos ca = control().getCaretPosition();
		if(ca != null)
		{
			return TextPos.of(ca.index(), 0);
		}
		return null;
	}
	
	
	public void backspace()
	{
		// TODO
		D.print("backspace");
	}
	
	
	public void copy()
	{
		// TODO
		D.print("copy");
	}
	
	
	public void copyPlainText()
	{
		// TODO
		D.print("copyPlainText");
	}
	
	
	public void cut()
	{
		// TODO
		D.print("cut");
	}
	
	
	public void delete()
	{
		// TODO
		D.print("delete");
	}
	
	
	public void deleteParagraph()
	{
		// TODO
		D.print("deleteParagraph");
	}
	
	
	public void deleteToParagraphStart()
	{
		// TODO
		D.print("deleteToParagraphStart");
	}
	
	
	public void deleteWordNext()
	{
		// TODO
		D.print("deleteWordNext");
	}
	
	
	public void deleteWordPrevious()
	{
		// TODO
		D.print("deleteWordPrevious");
	}
	
	
	public void focusNext()
	{
		// TODO
		D.print("focusNext");
	}
	
	
	public void focusPrevious()
	{
		// TODO
		D.print("focusPrevious");
	}
	
	
	public void insertLineBreak()
	{
		// TODO
		D.print("insertLineBreak");
	}
	
	
	public void insertTab()
	{
		// TODO
		D.print("insertTab");
	}
	
	
	public void moveDown()
	{
		moveVertically(1, false);
	}
	
	
	public void moveLeft()
	{
		moveHorizontally(-1, false);
	}
	
	
	public void moveRight()
	{
		moveHorizontally(1, false);
	}


	public void moveToDocumentEnd()
	{
		TextPos p = control().getDocumentEnd();
		control().moveCaret(p, false, true);
	}


	public void moveToDocumentStart()
	{
		control().moveCaret(TextPos.ZERO, false, true);
	}
	
	
	public void moveToLineEnd()
	{
		TextPos p = lineEnd();
		control().moveCaret(p, false, true);
	}
	
	
	public void moveToLineStart()
	{
		TextPos p = lineStart();
		control().moveCaret(p, false, true);
	}
	
	
	public void moveToParagraphEnd()
	{
		TextPos p = paragraphEnd();
		control().moveCaret(p, false, true);
	}
	
	
	public void moveToParagraphStart()
	{
		TextPos p = paragraphStart();
		control().moveCaret(p, false, true);
	}


	public void moveUp()
	{
		moveVertically(-1, false);
	}
	
	
	public void moveWordLeft()
	{
		// TODO
		D.print("moveWordLeft");
	}
	
	
	public void moveWordRight()
	{
		// TODO
		D.print("moveWordRight");
	}
	
	
	public void pageDown()
	{
		moveVertically(grid.getPageSize(), false);
	}
	
	
	public void pageUp()
	{
		moveVertically(-grid.getPageSize(), false);
	}
	
	
	public void paste()
	{
		// TODO
		D.print("paste");
	}
	
	
	public void pastePlainText()
	{
		// TODO
		D.print("pastePlainText");
	}

	
	public void redo()
	{
		// TODO
		D.print("redo");
	}
	
	
	public void selectDown()
	{
		moveVertically(1, true);
	}
	
	
	public void selectLeft()
	{
		moveHorizontally(-1, true);
	}
	
	
	public void selectPageDown()
	{
		moveVertically(grid.getPageSize(), true);
	}
	
	
	public void selectPageUp()
	{
		moveVertically(-grid.getPageSize(), true);
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
		moveHorizontally(1, true);
	}
	
	
	public void selectToDocumentEnd()
	{
		TextPos p = control().getDocumentEnd();
		control().moveCaret(p, true, true);
	}

	
	public void selectToDocumentStart()
	{
		control().moveCaret(TextPos.ZERO, true, true);
	}
	
	
	public void selectToLineEnd()
	{
		TextPos p = lineEnd();
		control().moveCaret(p, true, true);
	}
	
	
	public void selectToLineStart()
	{
		TextPos p = lineStart();
		control().moveCaret(p, true, true);
	}
	
	
	public void selectToParagraphEnd()
	{
		TextPos p = paragraphEnd();
		control().moveCaret(p, true, true);
	}
	
	
	public void selectToParagraphStart()
	{
		TextPos p = paragraphStart();
		control().moveCaret(p, true, true);
	}
	
	
	public void selectUp()
	{
		moveVertically(-1, true);
	}
	
	
	public void selectWord()
	{
		// TODO
		D.print("selectWord");
	}
	
	
	public void selectWordLeft()
	{
		// TODO
		D.print("selectWordLeft");
	}
	
	
	public void selectWordRight()
	{
		// TODO
		D.print("selectWordRight");
	}
	
	
	public void undo()
	{
		// TODO
		D.print("undo");
	}

	
	private void moveHorizontally(int delta, boolean select)
	{
		TextPos caret = control().getCaretPosition();
		if(caret != null)
		{
			TextPos p = grid.moveHorizontally(caret, delta);
			control().moveCaret(p, select, true);
		}
	}
	
	
	private void moveVertically(int delta, boolean select)
	{
		TextPos caret = control().getCaretPosition();
		if(caret != null)
		{
			TextPos p = grid.moveVertically(caret, delta, true);
			control().moveCaret(p, select, false);
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
		fastAutoScroll = Math.abs(delta) > Defaults.AUTO_SCROLL_THRESHOLD;
		
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
