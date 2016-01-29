package com.dw.biz.client.form;

import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.xrmi.XRmi;
import java.util.ArrayList;
import java.util.List;

@XRmi(reg_name="form_info")
public class FormInfo
  implements IXmlDataable
{
  public static final String VIEW_TYPE_HTML = "html";
  public static final String VIEW_TYPE_XML = "xml";
  private String formId = null;
  private String formName = null;
  private String formDesc = null;

  private String formViewType = "html";
  private String formViewContent = null;

  private List<FormCtrl> formControls = new ArrayList();

  public String getFormId()
  {
    return this.formId;
  }

  public String getFormName()
  {
    return this.formName;
  }

  public String getFormDesc()
  {
    return this.formDesc;
  }

  public String getFormViewType()
  {
    return this.formViewType;
  }

  public String getFormViewContent()
  {
    return this.formViewContent;
  }

  public List<FormCtrl> getFormControls()
  {
    return this.formControls;
  }

  public XmlData toXmlData()
  {
    return null;
  }

  public void fromXmlData(XmlData xd)
  {
  }
}
