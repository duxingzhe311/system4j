package com.dw.comp.webmail.impl;

import com.dw.comp.webmail.AbstractMailServer;
import com.dw.comp.webmail.WebMailItem;
import com.dw.mailserver.JamesServerBootComp;
import java.util.Date;
import java.util.HashSet;

public class InnerJamesServer extends AbstractMailServer
{
  public String getMailServerHost()
  {
    return null;
  }

  public void sendMail(String usern, WebMailItem wmi)
    throws Exception
  {
    JamesServerBootComp.sendMail(wmi.toMimeMsg());
  }

  public void sendSystemMail(WebMailItem wmi)
    throws Exception
  {
    JamesServerBootComp.sendMail(wmi.toMimeMsg());
  }

  protected int recvNewMailDo(String username, Date startd, Date endd, boolean is_sent, StringBuilder failedreson)
    throws Exception
  {
    return 0;
  }

  public void delMailFromServerById(String username, HashSet<String> msgids)
    throws Exception
  {
  }
}
