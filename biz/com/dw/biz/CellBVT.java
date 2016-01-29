package com.dw.biz;

import com.dw.biz.platform.BizViewRtCompilerEnv;
import com.dw.biz.platform.BizViewRunner;
import com.dw.biz.platform.compiler.RuntimeCompiler;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class CellBVT extends BizViewType
{
  public static final String TYPE_NAME = "view_cell";

  public String getTypeName()
  {
    return "view_cell";
  }

  public String getTypeTitle()
  {
    return "View Cell";
  }

  public void runBizView(HttpServletRequest req, HttpServletResponse resp, UserProfile up, String uniqueid, BizManager bm, BizTransaction bt, BizView bv, XmlData ctrlxd, XmlData inputxd, PrintWriter outputs)
    throws Exception
  {
    BizViewRtCompilerEnv brce = new BizViewRtCompilerEnv(bm, bv);

    RuntimeCompiler rc = new RuntimeCompiler(brce);
    BizViewRunner brv = (BizViewRunner)rc.getJspInstance();
    brv.setViewId(uniqueid);

    brv.prepareRunner(req, resp, up, outputs, bm, inputxd);

    brv.runIt();
  }

  public BizInOutInfo getInOutInfoByStrCont(String strcont)
  {
    return null;
  }
}
