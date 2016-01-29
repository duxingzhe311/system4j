package com.dw.biz.client.user_agent;

import java.util.List;

public abstract interface IUserInvoker
{
  public abstract List<Role> getAllRoles();

  public abstract OrgUnit getRootOrgUnit();

  public abstract List<OrgUnit> getSubOrgUnits(String paramString);

  public abstract List<Human> getHuman(String paramString);

  public abstract void getRoleAndOrgByHuman(String paramString, List<Role> paramList, List<OrgUnit> paramList1);
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.client.user_agent.IUserInvoker
 * JD-Core Version:    0.6.2
 */