package com.dw.biz;

import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.gdb.datax.query.DXQCmd;
import com.dw.system.gdb.datax.query.DataXQuery;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.user.UserProfile;
import java.util.ArrayList;

public class DataXQueryBizActionType extends BizActionType
{
  public static final String SUCC_OUTPUT_NAME = "succ";

  public String getTypeName()
  {
    return "datax_query";
  }

  public String getTypeTitle()
  {
    return "DataX查询语句";
  }

  public BizActionResult runBizAction(UserProfile up, BizManager bm, BizTransaction bt, IBizEnv be, BizAction ba, XmlData ctrlxd, XmlData inputxd)
    throws Exception
  {
    String querystr = ba.getStrCont();
    DataXQuery dxq = this.dataxMgr.getDataXQuery();
    XmlData res = dxq.runCachedCmd(querystr, inputxd);
    return new BizActionResult("succ", res);
  }

  public BizInOutInfo getInOutInfoByStrCont(String strcont)
  {
    if ((strcont == null) || ((strcont = strcont.trim()).equals(""))) {
      return null;
    }
    DataXQuery dxq = this.dataxMgr.getDataXQuery();
    DXQCmd cd = dxq.parseCachedCmd(strcont);
    XmlDataStruct input = cd.getInputStruct();
    XmlDataStruct outputst = cd.getOutputStruct();
    BizOutput bo = new BizOutput("succ", outputst);
    ArrayList outputs = new ArrayList(1);
    outputs.add(bo);
    return new BizInOutInfo(input, outputs);
  }
}
