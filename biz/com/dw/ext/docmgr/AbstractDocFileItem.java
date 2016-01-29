package com.dw.ext.docmgr;

import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMProperty;
import java.io.InputStream;
import java.util.Date;

public class AbstractDocFileItem
  implements Comparable<AbstractDocFileItem>
{

  @XORMProperty(name="FileId", has_col=true, is_pk=true, is_auto=true)
  long fileId = -1L;

  @XORMProperty(name="DocId", has_col=true, has_fk=true, fk_base_class=true)
  long docId = -1L;

  @XORMProperty(name="Ver", max_len=20, has_col=true)
  String ver = null;

  @XORMProperty(name="ModifyDate", has_col=true)
  Date modifyDate = new Date();

  @XORMProperty(name="FileLen", has_col=true)
  long fileLen = -1L;

  @XORMProperty(name="FileExt", has_col=true, max_len=10)
  String fileExt = null;

  @XORMProperty(name="UserName", max_len=50, has_col=true)
  String userName = null;

  @XORMProperty(name="Content", store_as_file=true, read_on_demand=true)
  byte[] content = null;

  public long getFileId()
  {
    return this.fileId;
  }

  public long getDocId()
  {
    return this.docId;
  }

  public void setDocId(long docid)
  {
    this.docId = docid;
  }

  public String getVersion()
  {
    return this.ver;
  }

  public void setVerstion(String v)
  {
    this.ver = v;
  }

  public DocVer getVer()
  {
    return new DocVer(this.ver);
  }

  public void setVer(DocVer v)
  {
    this.ver = v.toString();
  }

  public String getShowVer()
  {
    return DocVer.transToShowStr(this.ver);
  }

  public Date getModifyDate()
  {
    return this.modifyDate;
  }

  public void setModifyDate(Date d)
  {
    this.modifyDate = d;
  }

  public String getModifyDateStr()
  {
    return Convert.toFullYMDHMS(this.modifyDate);
  }

  public long getFileLen()
  {
    return this.fileLen;
  }

  public void setFileLen(long fl)
  {
    this.fileLen = fl;
  }

  public String getFileExt()
  {
    return this.fileExt;
  }

  public void setFileExt(String fe)
  {
    this.fileExt = fe;
  }

  public String getUserName()
  {
    return this.userName;
  }

  public void setUserName(String un)
  {
    this.userName = un;
  }

  public String toListString()
  {
    return "版本[" + getShowVer() + "]�? + Convert.toFullYMDHMS(this.modifyDate) + "�? + getUserName() + "修改";
  }

  public InputStream AcquireReadContentStream()
  {
    return null;
  }

  public int compareTo(AbstractDocFileItem o)
  {
    return this.modifyDate.compareTo(o.modifyDate);
  }
}
