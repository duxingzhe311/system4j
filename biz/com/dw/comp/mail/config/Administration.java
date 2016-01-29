package com.dw.comp.mail.config;

import com.dw.comp.mail.util.StringUtil;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Administration
  implements Serializable
{
  private Set m_Admins;
  private boolean m_Enabled = false;

  public Administration()
  {
    this.m_Admins = Collections.synchronizedSet(new TreeSet());
  }

  public boolean isEnabled()
  {
    return this.m_Enabled;
  }

  public void setEnabled(boolean b)
  {
    this.m_Enabled = b;
  }

  public String getAdminList()
  {
    return StringUtil.join(listAdmins(), ",");
  }

  public void setAdminList(String list)
  {
    String[] admins = StringUtil.split(list, ",");
    this.m_Admins.clear();
    for (int i = 0; i < admins.length; i++)
      addAdmin(admins[i]);
  }

  public String[] listAdmins()
  {
    String[] admins = new String[this.m_Admins.size()];
    return (String[])this.m_Admins.toArray(admins);
  }

  public void addAdmin(String username)
  {
    this.m_Admins.add(username);
  }

  public void removeAdmin(String username)
  {
    this.m_Admins.remove(username);
  }

  public boolean isAdmin(String username)
  {
    return this.m_Admins.contains(username);
  }
}
