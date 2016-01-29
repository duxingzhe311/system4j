package com.dw.biz;

import com.dw.system.xmldata.XmlData;
import java.io.Writer;

public abstract class AbstractBizRenderCtrl
{
  public String calculateAjaxPostEventUrl(String event_name, XmlData eventxd)
    throws Exception
  {
    return "javascript:void;";
  }

  public String calculateAjaxPostSelfUrl(XmlData postxd)
    throws Exception
  {
    return "javascript:void;";
  }

  public void renderIncludeAjaxView(String uniqueid, Writer w, String viewpath, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
  }
}
