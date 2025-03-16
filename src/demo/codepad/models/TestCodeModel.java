// Copyright © 2024-2025 Andy Goryachev <andy@goryachev.com>
package demo.codepad.models;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;
import goryachev.common.util.CKit;


/**
 * Test CodeModel.
 */
public class TestCodeModel extends CodeModel
{
	private final String[] lines;


	public TestCodeModel(String[] lines)
	{
		this.lines = lines;
	}
	
	
	public static TestCodeModel of(String text)
	{
		String[] lines = CKit.split(text, "\n");
		return new TestCodeModel(lines);
	}
	

	@Override
	public int size()
	{
		return lines.length;
	}
	

	@Override
	public CodeParagraph getParagraph(int index)
	{
		String text = lines[index];
		return new DemoParagraph(index, text);
	}
}
