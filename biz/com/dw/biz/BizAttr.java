package com.dw.biz;

public enum BizAttr
{
  logic_only(1), 
  io_read_only(2), 
  io_write_only(3), 
  io_read_write(4), 
  env_read_only(5), 
  env_write_only(6), 
  env_read_write(7), 
  io_env_read_only(8), 
  io_env_write_only(9), 
  io_env_read_write(10);

  private final int val;

  private BizAttr(int val)
  {
    this.val = val;
  }

  public int getValue()
  {
    return this.val;
  }

  public static BizAttr valueOf(int v)
  {
    switch (v)
    {
    case 1:
      return logic_only;
    case 2:
      return io_read_only;
    case 3:
      return io_write_only;
    case 4:
      return io_read_write;
    case 5:
      return env_read_only;
    case 6:
      return env_write_only;
    case 7:
      return env_read_write;
    case 8:
      return io_env_read_only;
    case 9:
      return io_env_write_only;
    case 10:
      return io_env_read_write;
    }
    throw new IllegalArgumentException("unknow Attr value=" + v);
  }

  public boolean isDependIO()
  {
    switch (this.val)
    {
    case 2:
    case 3:
    case 4:
    case 8:
    case 9:
    case 10:
      return true;
    case 5:
    case 6:
    case 7: } return false;
  }

  public boolean isDependEnv()
  {
    switch (this.val)
    {
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
      return true;
    }
    return false;
  }
}
