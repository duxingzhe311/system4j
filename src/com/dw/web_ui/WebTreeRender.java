package com.dw.web_ui;

import java.io.*;
import java.util.*;

public class WebTreeRender
{

	private static void renderSubTn(PrintWriter pw, WebTreeNode wtn)
	{
		// <!-- self -->
		int childnum = 0;
		ArrayList<WebTreeNode> children = wtn.getChildNodes();
		if (children != null)
			childnum = children.size();

		String uuid = UUID.randomUUID().toString() ;
		pw.println("<table class=\"small\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		pw.println("  <tr>");
//		if(level>0)
//			pw.println("<td><img src=\"/WebRes?r=/com/dw/web_ui/res/tree_transp.gif\" border=\"0\"></td>") ;
		
		ArrayList<WebTreeNode> ans = wtn.getAncestors();
		if(ans!=null)
		{
			for(WebTreeNode tmptn:ans)
			{
				if(tmptn.isLastInParent())
					pw.println("<td><img src=\"./WebRes?r=com/dw/web_ui/res/tree_transp.gif\" border=\"0\"></td>") ;
				else
					pw.println("<td><img src=\"./WebRes?r=com/dw/web_ui/res/tree_line.gif\" border=\"0\"></td>") ;
			}
		}
//		for(int k = 1 ; k < level ; k ++)
//		{
//			if(is_parent_last)
//				pw.println("<td><img src=\"/WebRes?r=/com/dw/web_ui/res/tree_transp.gif\" border=\"0\"></td>") ;
//			else
//				pw.println("<td><img src=\"/WebRes?r=/com/dw/web_ui/res/tree_line.gif\" border=\"0\"></td>") ;
//		}
		
		pw.println("      <td>");
		if (childnum <= 0)
		{
			if (wtn.isLastInParent())
				pw.println("      	<img src=\"./WebRes?r=com/dw/web_ui/res/tree_blankl.gif\" id=\"MEMU_"+uuid+"\" class=\"outline\">");
			else
				pw.println("      	<img src=\"./WebRes?r=com/dw/web_ui/res/tree_blank.gif\" id=\"MEMU_"+uuid+"\" class=\"outline\">");
		}
		else
		{
			pw.println("      	<img src=\"./WebRes?r=com/dw/web_ui/res/tree_minusl.gif\" id=\"MEMU_"+uuid+"\" class=\"outline\" style=\"cursor:hand\" onclick=\"myclick(this)\">");
			
//			if(level<=0)
//				pw.println("      	<img src=\"/WebRes?r=/com/dw/web_ui/res/tree_minusl.gif\" id=\"MEMU_"+uuid+"\" class=\"outline\" style=\"cursor:hand\" onclick=\"myclick(this)\">");
//			else
//				pw.println("      	<img src=\"/WebRes?r=/com/dw/web_ui/res/tree_plus.gif\" id=\"MEMU_"+uuid+"\" class=\"outline\" style=\"cursor:hand\" onclick=\"myclick(this)\">");
		}
		pw.println("      </td>");
		pw.println("      <td><img src=\"" + wtn.getIcon()
				+ "\" border=\"0\" WIDTH=\"17\" HEIGHT=\"13\" alt=\""
				+ wtn.getTitle() + "\"></td>");
		pw.println("      <td colspan=\"3\">");
		if (wtn.getUrl() != null)
		{
			pw.println("      <a href=\"" + wtn.getUrl() + "\">&nbsp;"
					+ wtn.getTitle() + "</a>");
		}
		else
		{
			pw.println(wtn.getTitle());
		}
		pw.println("    </td>");
		pw.println("     </tr>");
		pw.println("   </table>");

		if (childnum > 0)
		{
			pw
					.println("   <table class=\"small\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" id=\"MEMU_"+uuid+"d\" style=\"display:\">");
			pw.println("     <tr><td>");
			pw.println("     <!-- 二级菜单 -->");

			for (int i = 0; i < childnum; i++)
			{
				WebTreeNode tmp_wtn = children.get(i);
				renderSubTn(pw, tmp_wtn);

			}// end of for

			pw.println("     </td></tr>");
			pw.println("   </table>");
		}// end of if
	}

	/**
	 * 根据输入的根节点输出固定的web tree
	 * 
	 * @param roottn
	 */
	public static void renderFixTree(PrintWriter pw, WebTreeNode roottn)
	{
		// <!-- 树开始-->
		pw.println("<table class=\"small\" border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\"><tr><td>");

		renderSubTn(pw, roottn);

		pw.println("</td></tr></table>");

		pw.println("<SCRIPT language=JavaScript src=\"./WebRes?r=com/dw/web_ui/res/web_tree.js\"></SCRIPT>");
	}
}
