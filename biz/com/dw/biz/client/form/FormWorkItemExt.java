package com.dw.biz.client.form;

import com.dw.biz.api.IExtable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.ArrayList;
import java.util.List;

@XRmi(reg_name="form_workitem_ext")
public class FormWorkItemExt
  implements IExtable
{
  public static final String CTRL_TYPE_READ = "read";
  public static final String CTRL_TYPE_WRITE = "write";
  public static final String CTRL_TYPE_HIDDEN = "hidden";
  private String defaultCtrlType = "read";

  private String formId = null;

  private List<FormCtrl> readCtrls = new ArrayList();

  private List<FormCtrl> writeCtrls = new ArrayList();

  private List<FormCtrl> hiddenCtrls = new ArrayList();

  private XmlData curData = null;

  public String getFormId()
  {
    return this.formId;
  }

  public void setFormId(String form_id)
  {
    this.formId = form_id;
  }

  public String getDefaultCtrlType()
  {
    if ((this.defaultCtrlType == null) || (this.defaultCtrlType.equals(""))) {
      return "read";
    }
    return this.defaultCtrlType;
  }

  public void setDefaultCtrlType(String ct)
  {
    this.defaultCtrlType = ct;
  }

  public List<FormCtrl> getReadCtrlNames()
  {
    return this.readCtrls;
  }

  public List<FormCtrl> getWriteCtrlNames()
  {
    return this.writeCtrls;
  }

  public List<FormCtrl> getHiddenCtrlNames()
  {
    return this.hiddenCtrls;
  }

  public XmlData toXmlData()
  {
    XmlData xd = new XmlData();

    xd.setParamValue("form_id", this.formId);
    if (this.defaultCtrlType != null) {
      xd.setParamValue("default_ctrl_type", this.defaultCtrlType);
    }
    if ((this.readCtrls != null) && (this.readCtrls.size() > 0))
    {
      List localList = xd.getOrCreateSubDataArray("read_ctrls");
    }

    return xd;
  }

  public void fromXmlData(XmlData xd)
  {
    this.formId = xd.getParamValueStr("form_id");
    this.defaultCtrlType = xd.getParamValueStr("default_ctrl_type", "read");
  }

  public String getExtType()
  {
    return "form";
  }
}
