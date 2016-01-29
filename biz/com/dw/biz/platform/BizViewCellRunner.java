package com.dw.biz.platform;

import com.dw.biz.AbstractBizRenderCtrl;
import com.dw.biz.BizManager;
import com.dw.biz.BizViewCell.CtrlType;
import com.dw.mltag.AbstractNode;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.logger.ILogger;
import com.dw.system.xmldata.IXmlDataDef;
import com.dw.system.xmldata.IXmlStringable;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import com.dw.web_ui.WebUtil;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

public abstract class BizViewCellRunner
{
  Writer outputWriter = null;

  String cellName = null;

  private String strVal = null;

  private boolean bIgnore = false;

  private boolean bNullable = true;

  private boolean bReadonly = false;

  private boolean bWriteNo = false;

  private boolean bVisiable = true;

  BizViewRunner belongToViewRunner = null;

  String containerId = null;

  protected UserProfile userProfile = null;

  protected AbstractBizRenderCtrl renderCtrl = null;

  protected String bizItemPath = null;

  protected BizManager bizMgr = null;

  protected XmlData ctrlXmlData = null;

  protected IXmlStringable inputData = null;

  protected DataXManager dataxMgr = null;

  private Hashtable<String, String> additionAttr = new Hashtable();
  protected ILogger log;
  boolean bLock = false;

  public void setContainerId(String cid)
  {
    this.containerId = cid;
  }

  public String calAbsPath(String path)
  {
    if (!path.startsWith("/"))
    {
      int p = this.bizItemPath.lastIndexOf('/');
      path = this.bizItemPath.substring(0, p + 1) + path;
    }

    return path;
  }

  public final void setIgnore(boolean b)
  {
    this.bIgnore = b;
  }

  final boolean isIgnore()
  {
    return this.bIgnore;
  }

  public final void setNullable(boolean bnullable)
  {
    this.bNullable = bnullable;
  }

  public final boolean isNullable()
  {
    return this.bNullable;
  }

  public final void setReadonly(boolean b)
  {
    this.bReadonly = b;
  }

  public final boolean isReadonly()
  {
    return this.bReadonly;
  }

  public final void setVisiable(boolean b)
  {
    this.bVisiable = b;
  }

  public final boolean isVisiable()
  {
    return this.bVisiable;
  }

  public final boolean isWriteNo()
  {
    return this.bWriteNo;
  }

  public final void setWriteNo(boolean wn)
  {
    this.bWriteNo = wn;
  }

  public final boolean canWrite()
  {
    if (this.bReadonly) {
      return false;
    }
    if (!this.bVisiable) {
      return false;
    }
    if (this.bIgnore) {
      return false;
    }
    if (this.bWriteNo) {
      return false;
    }
    return true;
  }

  public void setStrValue(String strv)
  {
    this.strVal = strv;
  }

  public String getStrValue()
  {
    return this.strVal;
  }

  public String getDisValue()
  {
    return getStrValue();
  }

  public void setValueByXmlData(String cellname, XmlData xd)
  {
  }

  public void getValueToXmlData(String cellname, XmlData xd)
  {
  }

  public String getViewId()
  {
    if (this.belongToViewRunner != null) {
      return this.belongToViewRunner.getViewId();
    }
    return this.containerId;
  }

  public void setCellName(String n)
  {
    this.cellName = n;
  }

  public String getCellName()
  {
    return this.cellName;
  }

  public String getValueInputId()
  {
    return "vc_val$" + getViewId() + "$" + this.cellName;
  }

  public String getUniqueId()
  {
    return getViewId() + "$" + this.cellName;
  }

  public String getCheckFormatSpanId()
  {
    return "vc_check_format$" + getViewId() + "$" + this.cellName;
  }

  public String getValidateRequiredSpanId()
  {
    if (this.bNullable) {
      return "";
    }
    return "vc_check_required$" + getViewId() + "$" + this.cellName;
  }

  public IXmlDataDef getCellXmlDataDef()
  {
    return null;
  }

  public final void setAttribute(String name, String val)
  {
    if (!processAttr(name, val))
    {
      this.additionAttr.put(name, val);
    }
  }

  protected boolean processAttr(String name, String val)
  {
    if ("nullable".equalsIgnoreCase(name))
    {
      if ("false".equalsIgnoreCase(val)) {
        this.bNullable = false;
      }
      return true;
    }

    if ("id".equalsIgnoreCase(name))
    {
      return true;
    }

    if ("runas".equalsIgnoreCase(name)) {
      return true;
    }
    if ("value".equals(name))
    {
      setStrValue(val);
      return true;
    }

    if ("type".equalsIgnoreCase(name)) {
      return true;
    }
    if ("readonly".equalsIgnoreCase(name))
    {
      if ("true".equalsIgnoreCase(val))
        this.bReadonly = true;
      return true;
    }

    if ("write_no".equalsIgnoreCase(name))
    {
      if ("true".equalsIgnoreCase(val))
        this.bWriteNo = true;
      return true;
    }

    if ("visiable".equalsIgnoreCase(name))
    {
      if ("false".equalsIgnoreCase(val)) {
        this.bVisiable = false;
      }
      return true;
    }

    return false;
  }

  public void processSubNode(List<AbstractNode> subns)
  {
  }

  public String renderCellViewOutputUrl(String outputname, XmlData xd)
    throws Exception
  {
    return null;
  }

  public void lock()
  {
    this.bLock = true;
  }

  public boolean isLock()
  {
    return this.bLock;
  }

  public void unlock()
  {
    this.bLock = false;
  }

  public void runIt(BizViewCell.CtrlType ct) throws Exception
  {
    if (ct == BizViewCell.CtrlType.write)
      renderReadWrite(this.outputWriter);
    else if (ct == BizViewCell.CtrlType.read)
      renderReadOnly(this.outputWriter);
    else
      renderHidden(this.outputWriter);
  }

  protected void renderAdditionAttr()
    throws Exception
  {
    for (Map.Entry n2v : this.additionAttr.entrySet())
    {
      this.outputWriter.write(" ");
      this.outputWriter.write((String)n2v.getKey());
      this.outputWriter.write("=\"");
      this.outputWriter.write((String)n2v.getValue());
      this.outputWriter.write("\"");
    }
  }

  protected void beforeRender()
    throws Exception
  {
  }

  protected void afterRender(boolean render_succ)
    throws Exception
  {
  }

  protected abstract void renderCell(Writer paramWriter)
    throws Exception;

  private String getXmlDataValueType()
  {
    IXmlDataDef xdd = getCellXmlDataDef();
    if (xdd == null) {
      return "string";
    }
    return xdd.getValueTypeStr();
  }

  public void render(Writer w) throws Exception
  {
    if (this.bIgnore) {
      return;
    }
    beforeRender();
    boolean brender_succ = false;
    try
    {
      if (this.bReadonly)
      {
        String dv = getDisValue();
        if (dv != null)
          w.write(dv);
        return;
      }

      if (this.bWriteNo)
      {
        String dv = getDisValue();
        String vv = getStrValue();
        if (dv != null)
        {
          w.write(dv);
        }
        if (vv != null)
        {
          w.write("<input name='dx_/");
          w.write(getCellName());
          w.write(":");
          w.write(getXmlDataValueType());
          w.write("' type='hidden' value='");
          w.write(WebUtil.plainToHtml(vv));
          w.write("'/>");
        }
        return;
      }

      renderCell(w);
      brender_succ = true;
    }
    finally
    {
      afterRender(brender_succ); } afterRender(brender_succ);
  }

  public void renderHidden(Writer w) throws Exception
  {
  }

  public void renderReadOnly(Writer w) throws Exception
  {
  }

  public void renderReadWrite(Writer w) throws Exception
  {
  }

  public abstract int getWriteLineNumber();

  public void prepareRunner(UserProfile up, Writer output, BizManager bm, String biz_itempath, XmlData ctrlxd, IXmlStringable inputxd)
  {
    this.userProfile = up;
    this.outputWriter = output;
    this.bizMgr = bm;
    this.dataxMgr = bm.getDataXManager();
    this.bizItemPath = biz_itempath;
    this.ctrlXmlData = ctrlxd;
    this.inputData = inputxd;
  }

  public void setLog(ILogger log)
  {
    this.log = log;
  }
}
