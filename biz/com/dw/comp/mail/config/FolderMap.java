package com.dw.comp.mail.config;

import java.io.Serializable;

public class FolderMap
  implements Serializable
{
  private String rootFolderPath = "";

  private String draftFolderName = "draft";
  private String trashFolderName = "trash";
  private String sentFolderName = "sent";

  public String getRootFolderPath()
  {
    return this.rootFolderPath;
  }

  public void setRootFolderPath(String fp)
  {
    this.rootFolderPath = fp;
  }

  public String getDraftFolderName()
  {
    return this.draftFolderName;
  }

  public void setDraftFolderName(String n)
  {
    this.draftFolderName = n;
  }

  public String getTrashFolderName()
  {
    return this.trashFolderName;
  }

  public void setTrashFolderName(String n)
  {
    this.trashFolderName = n;
  }

  public String getSentFolderName()
  {
    return this.sentFolderName;
  }

  public void setSentFolderName(String n)
  {
    this.sentFolderName = n;
  }
}
