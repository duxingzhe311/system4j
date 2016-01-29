package com.dw.comp.mail;

import com.dw.comp.AbstractComp;
import com.dw.comp.CompAction;
import com.dw.comp.mail.model.WBMailFolder;
import com.dw.comp.mail.model.WBMailStore;
import com.dw.user.sso.ModuleInfo;
import com.dw.user.sso.SSOManager;
import java.io.PrintStream;
import java.util.ArrayList;

public class WBMailComp extends AbstractComp
{
  public static final String COMP_NAME = "mail";

  public WBMailComp()
  {
    SSOManager.getInstance().registerModuleInfo(new ModuleInfo("mail", "Mail System User Account!"));
  }

  public String getCompName()
  {
    return "mail";
  }

  public ArrayList<CompAction> getActions()
  {
    return null;
  }

  public void doCmd(String[] cmds)
    throws Exception
  {
    if ((cmds == null) || (cmds.length <= 1)) {
      return;
    }
    if ("test".equals(cmds[0]))
    {
      WBMailManager.test(cmds[1]);
    }
    else if ("list_folder".equals(cmds[0]))
    {
      StringBuilder failedreson = new StringBuilder();
      WBMailStore wbms = WBMailManager.getInstance().getUserMailStore(cmds[1], failedreson);
      if (wbms == null)
      {
        System.out.println("cannot get mail store with user=" + cmds[1]);
        return;
      }

      WBMailFolder mf = wbms.getWBMailFolder(cmds[2]);
      if (mf == null)
      {
        System.out.println("no folder found with path=" + cmds[2]);
        return;
      }

      WBMailManager.printListMsgInfo(mf);
    }
    else if (!"stat".equals(cmds[0]))
    {
      if (!"session_open".equals(cmds[0]))
      {
        if (!"inbox_close".equals(cmds[0]))
        {
          "inbox_open".equals(cmds[0]);
        }
      }
    }
  }
}
