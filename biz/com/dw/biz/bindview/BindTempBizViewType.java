package com.dw.biz.bindview;

import com.dw.biz.BizInOutInfo;
import com.dw.biz.BizManager;
import com.dw.biz.BizTransaction;
import com.dw.biz.BizView;
import com.dw.biz.BizViewType;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BindTempBizViewType extends BizViewType
{
  public static final String TYPE_NAME = "bind_temp";

  public String getTypeName()
  {
    return "bind_temp";
  }

  public String getTypeTitle()
  {
    return "Bind Template";
  }

  public void runBizView(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String uniqueid, BizManager bm, BizTransaction bt, BizView bv, XmlData ctrlxd, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    Object ov = bv.getRunnerObj();
    HtmlBindTemp hbt = null;
    if ((ov == null) || (!(ov instanceof HtmlBindTemp)))
    {
      synchronized (this)
      {
        ov = bv.getRunnerObj();
        if ((ov != null) && ((ov instanceof HtmlBindTemp)))
        {
          hbt = (HtmlBindTemp)ov;
        }
        else
        {
          hbt = new HtmlBindTemp(bv.getStrCont());
          hbt.init();

          bv.setRunnerObj(hbt);
        }
      }
    }
    else
    {
      hbt = (HtmlBindTemp)ov;
    }

    hbt.writeInputToView(inputxd, outputs);
  }

  public BizInOutInfo getInOutInfoByStrCont(String strcont)
  {
    return null;
  }
}
