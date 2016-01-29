package com.dw.biz;

import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.xmldata.XmlData;
import com.dw.user.UserProfile;

public abstract class BizActionType
{
  protected BizManager bizMgr = null;

  protected DataXManager dataxMgr = null;

  void init(BizManager bizmgr, DataXManager dxmgr)
  {
    this.bizMgr = bizmgr;
    this.dataxMgr = dxmgr;
  }

  public abstract String getTypeName();

  public abstract String getTypeTitle();

  public abstract BizActionResult runBizAction(UserProfile paramUserProfile, BizManager paramBizManager, BizTransaction paramBizTransaction, IBizEnv paramIBizEnv, BizAction paramBizAction, XmlData paramXmlData1, XmlData paramXmlData2)
    throws Exception;

  public abstract BizInOutInfo getInOutInfoByStrCont(String paramString);
}
