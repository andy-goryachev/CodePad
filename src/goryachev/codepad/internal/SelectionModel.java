// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package goryachev.codepad.internal;
import goryachev.codepad.SelectionRange;
import goryachev.codepad.TextPos;
import goryachev.codepad.model.CodeModel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;


/**
 * Selection Model, supports a single selection range.
 */
public final class SelectionModel
{
    private final ReadOnlyObjectWrapper<SelectionRange> range = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPos> anchor = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPos> caret = new ReadOnlyObjectWrapper<>();
    
    
	public SelectionModel()
	{
	}


	public void clear()
	{
		set(null, null);
	}
	
	
	public ReadOnlyProperty<TextPos> anchorPositionProperty()
	{
		return anchor.getReadOnlyProperty();
	}
	
	
	public ReadOnlyProperty<TextPos> caretPositionProperty()
	{
		return caret.getReadOnlyProperty();
	}


	public ReadOnlyProperty<SelectionRange> selectionProperty()
	{
		return range.getReadOnlyProperty();
	}


	public SelectionRange getSelectionRange()
	{
		return range.get();
	}
	
	
	public void setSelectionRange(CodeModel m, TextPos anchor, TextPos caret)
	{
        anchor = m.clamp(anchor);
        caret = m.clamp(caret);
        SelectionRange sel;

        boolean caretAtMin = caret.compareTo(anchor) < 0;
        if(caretAtMin)
        {
        	sel = new SelectionRange(caret, anchor, true);
        }
        else
        {
        	sel = new SelectionRange(anchor, caret, caretAtMin);
        }
        set(m, sel);
	}
	
	
	public void extendSelection(CodeModel m, TextPos p)
	{
		// TODO if model is different
		
		SelectionRange sel = getSelectionRange();
		TextPos an;
		if(sel == null)
		{
			an = p;
		}
		else
		{
			if(p.compareTo(sel.getMin()) < 0)
			{
				an = sel.getMax();
			}
			else if(p.compareTo(sel.getMax()) <= 0)
			{
				an = sel.getAnchor();
			}
			else
			{
				an = sel.getMin();
			}
		}
		setSelectionRange(m, an, p);
	}
	
	
	private void set(CodeModel m, SelectionRange s)
	{
		// TODO can I set all three properties and then call invalidate() on them?
		if(s == null)
		{
			anchor.set(null);
			caret.set(null);
		}
		else
		{
			anchor.set(s.getAnchor());
			caret.set(s.getCaret());
		}
		range.set(s);
	}
}
