// Copyright © 2024-2024 Andy Goryachev <andy@goryachev.com>
package demo.codepad.models;
import goryachev.codepad.model.CodeModel;
import goryachev.codepad.model.CodeParagraph;


/**
 * Large Model.
 */
public class LargeModel
	extends CodeModel
{
	private final int size;
	private static final String[] LINES =
	{
		"Short Line",
		"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
		"Ut enim ad minim veniam",
		"",
		"日本語（にほんご、にっぽんご[注釈 3]）は、日本国内や、かつての日本領だった国、そして国外移民や移住者を含む日本人同士の間で使用されている言語。"
	};


	public LargeModel(int size)
	{
		this.size = size;
	}


	@Override
	public int size()
	{
		return size;
	}


	@Override
	public CodeParagraph getParagraph(int index)
	{
		String text = getPlainText(index);
		return new DemoParagraph(index, text);
	}
	
	
	@Override
	public String getPlainText(int index)
	{
		int ix = index % LINES.length;
		return (index + 1) + " " + LINES[ix];
	}
}
