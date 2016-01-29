package com.dw.biz;

import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.biz.platform.BizViewRunner;
import com.dw.biz.platform.compiler.BizViewRuntime;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class JavaJspBVT extends BizViewType
{
  public static final String TYPE_NAME = "java_jsp";

  public String getTypeName()
  {
    return "java_jsp";
  }

  public String getTypeTitle()
  {
    return "Jsp";
  }

  public void runBizView(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String viewid, BizManager bm, BizTransaction bt, BizView bv, XmlData ctrlxd, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    BizViewRtCompilerEnv brce = new BizViewRtCompilerEnv(bm, bv);

    BizViewRuntime rc = new BizViewRuntime(bm, brce);
    BizViewRunner brv = (BizViewRunner)rc.getJspInstance();
    brv.setViewId(viewid);

    brv.prepareRunner(req, resp, up, outputs, bm, inputxd);

    brv.runIt();
  }

  public BizInOutInfo getInOutInfoByStrCont(String strcont)
  {
    return null;
  }
}
