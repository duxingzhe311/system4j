package com.dw.biz.flow_ins;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;

@XORMClass(table_name="biz_flow_ins_file")
public class BizFlowInsFile
{

  @XORMProperty(name="FileId", has_col=true, is_pk=true, is_auto=true)
  long fileId = -1L;

  @XORMProperty(name="FlowInsId", nullable=false, default_str_val="-1", has_col=true, has_idx=true, has_fk=true, fk_table="biz_flow_ins", fk_column="FlowInsId", order_num=10)
  long flowInsId = -1L;

  @XORMProperty(name="FileName", has_col=true, nullable=false, has_idx=true, max_len=100, order_num=30)
  String fileName = null;

  @XORMProperty(name="FileExt", has_col=true, nullable=false, max_len=10, order_num=35)
  String fileExt = null;

  @XORMProperty(name="FileSize", has_col=true, nullable=false, order_num=40)
  int fileSize = -1;

  @XORMProperty(name="FileCont", store_as_file=true)
  byte[] fileCont = null;

  public BizFlowInsFile()
  {
  }

  public BizFlowInsFile(long flowinsid, String filename, byte[] cont, int filesize) {
    if ((filename == null) || (filename.equals(""))) {
      throw new IllegalArgumentException("file name cannot be null or empty!");
    }
    this.flowInsId = flowinsid;
    this.fileName = filename;
    this.fileCont = cont;
    this.fileSize = filesize;
  }

  public long getFileId()
  {
    return this.fileId;
  }

  public long getFlowInsId()
  {
    return this.flowInsId;
  }

  public String getFileName()
  {
    return this.fileName;
  }

  public byte[] getFileContent()
  {
    return this.fileCont;
  }

  public int getFileSize()
  {
    return this.fileSize;
  }
}
