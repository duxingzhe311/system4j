package com.dw.biz;

import com.dw.biz.platform.BizActionRtCompilerEnv;
import com.dw.biz.platform.BizRunner;
import com.dw.biz.platform.compiler.RuntimeCompiler;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;
import java.io.PrintWriter;
import java.io.StringWriter;

class JavaJspBAT extends BizActionType
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

  public BizActionResult runBizAction(UserProfile up, BizManager bm, BizTransaction bt, IBizEnv env, BizAction ba, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
    BizActionRtCompilerEnv brce = new BizActionRtCompilerEnv(ba);

    RuntimeCompiler rc = new RuntimeCompiler(brce);
    BizRunner brv = (BizRunner)rc.getJspInstance();

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    brv.prepareRunner(up, null, pw, bm, env, ba.getBizPathStr(), bt, ctrlxd, inputxd, null);
    brv.runIt();

    XmlData outxd = brv.getOutputXmlData();

    return new BizActionResult(sw.toString().trim(), outxd);
  }

  public BizInOutInfo getInOutInfoByStrCont(String strcont)
  {
    return null;
  }
}
