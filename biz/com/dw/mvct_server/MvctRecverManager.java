package com.dw.mvct_server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MvctRecverManager
{
  private static Object locker = new Object();

  private static MvctRecverManager instance = null;

  HashMap<String, MvctRecverInfo> recverId2Recver = new HashMap();

  public static MvctRecverManager getInstance()
  {
    if (instance != null) {
      return instance;
    }
    synchronized (locker)
    {
      if (instance != null) {
        return instance;
      }
      instance = new MvctRecverManager();
      return instance;
    }
  }

  public synchronized void pulseRecver(MvctRecverInfo mri)
  {
    this.recverId2Recver.put(mri.getRecverId(), mri);
  }

  public synchronized ArrayList<MvctRecverInfo> listAllRecvers()
  {
    ArrayList rets = new ArrayList(this.recverId2Recver.size());

    for (MvctRecverInfo mri : this.recverId2Recver.values())
    {
      if (!mri.isTimeout())
      {
        rets.add(mri);
      }
    }
    return rets;
  }

  public synchronized MvctRecverInfo selectRecverByNotify(String clientid)
  {
    MvctRecverInfo ret = null;
    for (MvctRecverInfo mri : this.recverId2Recver.values())
    {
      if (ret == null)
      {
        ret = mri;
      }
      else if (mri.getClientIds().size() < ret.getClientIds().size())
      {
        ret = mri;
      }
    }

    return ret;
  }
}
