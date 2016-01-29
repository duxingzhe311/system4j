package com.dw.portal;

import com.dw.biz.BizManager;
import com.dw.biz.platform.BizViewRunner;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataMember;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;
import com.dw.user.UserProfile;
import com.dw.user.right.RightRule;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@XORMClass(table_name="portal_page_block")
public class PageBlock
  implements Comparable<PageBlock>
{

  @XORMProperty(name="BlockId", has_col=true, is_pk=true, is_auto=true, order_num=1)
  long blockId = -1L;

  @XORMProperty(name="PageId", has_col=true, has_fk=true, fk_table="portal_page", fk_column="PageId", order_num=5)
  long pageId = -1L;

  @XORMProperty(name="ContainerName", has_col=true, has_idx=true, max_len=20, order_num=10)
  String containerName = null;

  @XORMProperty(name="Title", has_col=true, max_len=100, order_num=15)
  String title = null;

  @XORMProperty(name="Sequence", has_col=true, order_num=20)
  int sequence = 0;

  @XORMProperty(name="ViewPath", has_col=true, max_len=50, order_num=25)
  String viewPath = null;

  @XORMProperty(name="RefreshButton", has_col=true, order_num=35)
  boolean refreshButton = true;

  @XORMProperty(name="MinButton", has_col=true, order_num=40)
  boolean minButton = true;

  @XORMProperty(name="MaxButton", has_col=true, order_num=45)
  boolean maxButton = true;

  @XORMProperty(name="EditUrl", has_col=true, max_len=100, order_num=50)
  String editUrl = null;

  @XORMProperty(name="EditRight", has_col=true, max_len=500, order_num=55)
  String editRight = null;

  @XORMProperty(name="IgnoreBorder", has_col=true, default_str_val="0", order_num=70)
  boolean ignoreBorder = false;

  @XORMProperty(name="IgnoreBlock", has_col=true, default_str_val="0", order_num=80)
  boolean ignoreBlock = false;

  @XORMProperty(name="InputParam", has_col=true, update_as_single=true, order_num=90)
  byte[] inputParam = null;

  transient RightRule _editRR = null;

  transient XmlData _inner_param = null;

  transient XmlData _fixed_param = null;
  transient HashMap<String, String> _name2var = null;

  private transient Block _tmpBk = null;

  public static String VAR_PREFIX_PARAM = "@req.param.";

  public PageBlock()
  {
  }

  public PageBlock(String containerName)
  {
    this.containerName = containerName;
  }

  public long getBlockId()
  {
    return this.blockId;
  }

  public void setBlockId(long blockId)
  {
    this.blockId = blockId;
  }

  public String getEditUrl()
  {
    return this.editUrl;
  }

  public void setEditUrl(String u)
  {
    this.editUrl = u;
  }

  public String getEditRight()
  {
    return this.editRight;
  }

  public void setEditRight(String er)
  {
    this.editRight = er;
  }

  public boolean isIgnoreBorder()
  {
    return this.ignoreBorder;
  }

  public void setIgnoreBorder(boolean ib)
  {
    this.ignoreBorder = ib;
  }

  public boolean isIgnoreBlock()
  {
    return this.ignoreBlock;
  }

  public void setIgnoreBlock(boolean b)
  {
    this.ignoreBlock = b;
  }

  public RightRule getEditRightRule()
  {
    if (this._editRR != null) {
      return this._editRR;
    }
    if (Convert.isNullOrEmpty(this.editRight)) {
      return null;
    }
    this._editRR = RightRule.Parse(this.editRight);
    return this._editRR;
  }

  public String getContainerName()
  {
    return this.containerName;
  }

  public void setContainerName(String containerName)
  {
    this.containerName = containerName;
  }

  public byte[] getInputParam()
  {
    return this.inputParam;
  }

  public XmlData getInputParamXmlData()
    throws Exception
  {
    if (this._inner_param != null)
      return this._inner_param;
    this._inner_param = XmlData.parseFromByteArray(getInputParam(), "UTF-8");
    return this._inner_param;
  }

  private synchronized void getFixedVarXmlDataParam()
    throws Exception
  {
    XmlData fix = new XmlData();
    HashMap n2var = new HashMap();
    XmlData xd = getInputParamXmlData();
    for (String pn : xd.getParamNames())
    {
      Object ov = xd.getParamValue(pn);
      if (!(ov instanceof String))
      {
        fix.setParamValue(pn, ov);
      }
      else
      {
        String sv = (String)ov;
        if (!sv.startsWith("@"))
        {
          fix.setParamValue(pn, ov);
        }
        else
        {
          n2var.put(pn, sv);
        }
      }
    }
    this._fixed_param = fix;
    this._name2var = n2var;
  }

  public XmlData getFixedInputParamXmlData()
    throws Exception
  {
    if (this._fixed_param != null) {
      return this._fixed_param;
    }
    getFixedVarXmlDataParam();
    return this._fixed_param;
  }

  public HashMap<String, String> getVarInputParam()
    throws Exception
  {
    if (this._name2var != null) {
      return this._name2var;
    }
    getFixedVarXmlDataParam();
    return this._name2var;
  }

  public boolean isMaxButton()
  {
    return this.maxButton;
  }

  public void setMaxButton(boolean maxButton)
  {
    this.maxButton = maxButton;
  }

  public boolean isMinButton()
  {
    return this.minButton;
  }

  public void setMinButton(boolean minButton)
  {
    this.minButton = minButton;
  }

  public long getPageId()
  {
    return this.pageId;
  }

  public void setPageId(long pageId)
  {
    this.pageId = pageId;
  }

  public boolean isRefreshButton()
  {
    return this.refreshButton;
  }

  public void setRefreshButton(boolean refreshButton)
  {
    this.refreshButton = refreshButton;
  }

  public int getSequence()
  {
    return this.sequence;
  }

  public void setSequence(int sequence)
  {
    this.sequence = sequence;
  }

  public String getTitle()
  {
    return this.title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getViewPath()
  {
    return this.viewPath;
  }

  public void setViewPath(String viewPath)
  {
    this.viewPath = viewPath;
  }

  public Block getRelatedBlock()
  {
    if (this._tmpBk != null) {
      return this._tmpBk;
    }
    this._tmpBk = PortalManager.getInstance().getBlockByPath(this.viewPath);
    return this._tmpBk;
  }

  public int compareTo(PageBlock o)
  {
    return this.sequence - o.sequence;
  }

  public HashMap<String, String> getBorderProps(HttpServletRequest req, UserProfile up)
    throws Exception
  {
    HashMap rets = new HashMap();
    if (Convert.isNotNullEmpty(this.title)) {
      rets.put("title", this.title);
    }
    else {
      Block bk = PortalManager.getInstance().getBlockByPath(getViewPath());
      if (bk != null) {
        rets.put("title", bk.getTitle());
      }
    }
    RightRule rr = getEditRightRule();
    if ((up != null) && (
      (up.isAdministrator()) || ((rr != null) && (rr.CheckUserRight(up)))))
    {
      String editid = UUID.randomUUID().toString();

      rets.put("edit_id", editid);
      rets.put("edit", "<span id='" + editid + "'></span>");
    }

    return rets;
  }

  public String render(HttpServletRequest req, HttpServletResponse resp, UserProfile up, Writer jw, XmlData inputxd)
    throws Exception
  {
    String vp = getViewPath();
    if (Convert.isNullOrEmpty(vp)) {
      return null;
    }
    BizViewRunner bvr = BizManager.getInstance().RT_getBizViewRunner(UUID.randomUUID().toString(), vp);
    if (bvr == null) {
      return null;
    }
    XmlData xd = new XmlData();

    XmlData pxd = getFixedInputParamXmlData();
    if (pxd != null)
    {
      xd.combineAppend(pxd);
    }

    HashMap name2var = getVarInputParam();
    if ((name2var != null) && (name2var.size() > 0))
    {
      XmlDataStruct xds = bvr.getRunnerInputXmlDataStruct();

      for (Map.Entry n2var : name2var.entrySet())
      {
        String n = (String)n2var.getKey();
        String v = (String)n2var.getValue();
        if (v.startsWith(VAR_PREFIX_PARAM))
        {
          String reqn = v.substring(VAR_PREFIX_PARAM.length());
          String tv = req.getParameter(reqn);
          if (tv != null)
          {
            XmlDataMember xdm = xds.getXmlDataMember(n);
            if (xdm == null)
            {
              xd.setParamValue(n, tv);
            }
            else
            {
              XmlVal xv = new XmlVal(xdm.getValType(), tv);
              xd.setParamXmlVal(n, xv);
            }
          }
        }
      }
    }

    if (inputxd != null)
      xd.combineAppend(inputxd);
    bvr.prepareRunner(req, resp, up, null, BizManager.getInstance(), xd);
    bvr.render(jw);
    jw.flush();
    return bvr.getPortalEditUrl(xd);
  }

  public BizViewRunner getCommonBizViewRunner()
    throws Exception
  {
    String vp = getViewPath();
    if (Convert.isNullOrEmpty(vp)) {
      return null;
    }
    return BizManager.getInstance().RT_getBizViewRunner(UUID.randomUUID().toString(), vp);
  }
}
