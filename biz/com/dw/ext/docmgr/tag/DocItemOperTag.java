package com.dw.ext.docmgr.tag;

import com.dw.ext.docmgr.AbstractDocItem;
import com.dw.ext.docmgr.AbstractDocItem.State;
import com.dw.user.UserProfile;
import com.dw.web_ui.table.XORMTableTag;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class DocItemOperTag extends BodyTagSupport
{
  public static final String ATTRN_DocItemOperTag = "DocItemOperTag";
  public static String FT_OBJ_ID = "doc_oper";

  private String compName = "";

  private String path = null;

  public void setPath(String p)
  {
    this.path = p;
  }

  public void setComp_name(String cn)
  {
    this.compName = cn;
  }

  public int doStartTag() throws JspTagException
  {
    try
    {
      if (!"true".equals(this.pageContext.getAttribute("DocItemOperTag")))
      {
        JspWriter jw = this.pageContext.getOut();
        jw.write("<script type='text/javascript' src='/system/ui/dlg.js'></script>");

        this.pageContext.setAttribute("DocItemOperTag", "true");
      }
    }
    catch (Exception localException)
    {
    }
    return 2;
  }

  public int doEndTag()
    throws JspTagException
  {
    XORMTableTag xormtt = (XORMTableTag)getParent();
    if (xormtt == null) {
      throw new JspTagException("");
    }
    Object o = xormtt.getCurRowXormObj();
    if (o == null) {
      return 6;
    }
    if (!(o instanceof AbstractDocItem))
    {
      return 6;
    }

    AbstractDocItem di = (AbstractDocItem)o;

    long docid = di.getDocId();

    JspWriter jw = this.pageContext.getOut();
    String oldfn = di.getDocFileName() + "." + di.getLastFileExt();
    try
    {
      UserProfile up = UserProfile.getUserProfile((HttpServletRequest)this.pageContext.getRequest());

      AbstractDocItem.State st = di.getState();

      if (st == AbstractDocItem.State.Normal)
      {
        jw.write("<span class='ms-vh2'>");
        jw.write("<span id='doc_op_item_ViewDocFile" + docid + "_ocx' onclick='ViewDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>查看</span>&nbsp;");
        jw.write("<span id='doc_op_item_DownloadDocFile" + docid + "_ocx' onclick='DownloadDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>下载</span>&nbsp;");
        jw.write("<span id='doc_op_item_AddNewDocFileVer" + docid + "_ocx' onclick='AddNewDocFileVer(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + oldfn + "\")' style='cursor:hand;'>添加新版�?/span>&nbsp;");
        jw.write("<span id='doc_op_item_UploadNewDocFileVer" + docid + "_br' onclick='UploadNewDocFileVer(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + oldfn + "\")' style='cursor:hand;'>上传新版�?/span>&nbsp;");
        jw.write("<span id='doc_op_item_ChangeDocFileName" + docid + "' onclick='ChangeDocFileName(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + oldfn + "\")' style='cursor:hand;'>修改名称</span>&nbsp;");
        jw.write("<span id='doc_op_item_CheckOutDocFile" + docid + "_ocx' onclick='CheckOutDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>签出修改</span>&nbsp;");
        jw.write("<span id='doc_op_item_CheckHis" + docid + "' onclick='CheckHis(" + di.getDocId() + ")' style='cursor:hand;'>查看历史</span>&nbsp;");
        jw.write("<span id='doc_op_item_DelDoc" + docid + "' onclick='DelDoc(\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>删除</span>&nbsp;");
        if ((up != null) && (up.isAdministrator()))
        {
          jw.write("<span id='doc_op_item_DelDocP" + docid + "' onclick='DelDoc(\"" + this.compName + "\"," + di.getDocId() + ",true)' style='cursor:hand;'>永久删除</span>&nbsp;");
        }
        jw.write("</span>");
      }
      else if (st == AbstractDocItem.State.Locked)
      {
        jw.write("<span class='ms-vh2'>");
        jw.write("<span id='doc_op_item_DownDocFile_br" + docid + "_br' onclick='DownloadDocFileByBr(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>下载</span>&nbsp;");
        if ((up != null) && (up.getUserName().equals(di.getLockUserName())))
        {
          jw.write("<span id='doc_op_item_OpenCheckedOutDocFile" + docid + "_ocx' onclick='OpenCheckedOutDocFile(" + FT_OBJ_ID + "," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>打开已签出文�?/span>&nbsp;");

          jw.write("<span id='doc_op_item_CheckInDocFile" + docid + "_ocx' onclick='CheckInDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>签入</span>&nbsp;");
          jw.write("<span id='doc_op_item_UpCheckInDocFile" + docid + "_br' onclick='CheckInDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>上传并签�?/span>&nbsp;");

          jw.write("<span id='doc_op_item_UncheckOutDocFile" + docid + "_ocx' onclick='UncheckOutDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>撤销签出</span>&nbsp;");
          jw.write("<span id='doc_op_item_UncheckOutDocFile" + docid + "_br' onclick='UncheckOutDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ",\"" + di.getLastFileExt() + "\")' style='cursor:hand;'>取消签出</span>&nbsp;");
        }
        else
        {
          jw.write("<span id='doc_op_item_ViewDocFile" + docid + "_ocx' onclick='ViewDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand'>查看</span>&nbsp;");
          jw.write("<span id='doc_op_item_DownloadDocFile" + docid + "_ocx' onclick='DownloadDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>保存到本�?/span>&nbsp;");
        }

        jw.write("<span onclick='CheckHis(" + di.getDocId() + ")' style='cursor:hand;'>查看历史</span>&nbsp;");
        jw.write("</span>");
      }
      else if (st == AbstractDocItem.State.Delete)
      {
        if ((up != null) && (up.isAdministrator()))
        {
          jw.write("<span class='ms-vh2'>");
          jw.write("<span id='doc_op_item_ViewDocFile" + docid + "_ocx' onclick='ViewDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>查看</span>&nbsp;");
          jw.write("<span id='doc_op_item_DownloadDocFile" + docid + "_ocx' onclick='DownloadDocFile(" + FT_OBJ_ID + ",\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>下载</span>&nbsp;");
          jw.write("<span id='doc_op_item_CheckHis" + docid + "' onclick='CheckHis(" + di.getDocId() + ")' style='cursor:hand;'>查看历史</span>&nbsp;");
          jw.write("<span id='doc_op_item_Recover" + docid + "' onclick='RecoverDoc(\"" + this.compName + "\"," + di.getDocId() + ")' style='cursor:hand;'>恢复</span>&nbsp;");
          jw.write("<span id='doc_op_item_DelDocP" + docid + "' onclick='DelDoc(\"" + this.compName + "\"," + di.getDocId() + ",true)' style='cursor:hand;'>永久删除</span>&nbsp;");
          jw.write("</span>");
        }

      }

    }
    catch (Exception e)
    {
      throw new JspTagException(e);
    }
    return 6;
  }
}
