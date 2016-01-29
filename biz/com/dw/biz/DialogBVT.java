package com.dw.biz;

import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.biz.platform.BizViewRunner;
import com.dw.biz.platform.compiler.RuntimeCompiler;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class DialogBVT extends BizViewType
{
  public static final String TYPE_NAME = "dialog";

  public String getTypeName()
  {
    return "dialog";
  }

  public String getTypeTitle()
  {
    return "Dialog";
  }

  public void runBizView(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String viewid, BizManager bm, BizTransaction bt, BizView bv, XmlData ctrlxd, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    BizViewRtCompilerEnv brce = new BizViewRtCompilerEnv(bm, bv);

    RuntimeCompiler rc = new RuntimeCompiler(brce);
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
