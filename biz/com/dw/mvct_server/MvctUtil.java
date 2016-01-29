package com.dw.mvct_server;

import com.dw.system.Convert;
import java.io.InputStream;
import java.util.HashMap;

public class MvctUtil
{
  public static HashMap<String, String> readStringMapFromStream(InputStream ins, String charset)
    throws Exception
  {
    return Convert.readStringMapFromStream(ins, charset);
  }
}
