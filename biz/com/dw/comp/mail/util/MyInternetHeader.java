package com.dw.comp.mail.util;

import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

class MyInternetHeader extends InternetHeaders
{
  public MyInternetHeader(InputStream is)
    throws MessagingException
  {
    super(is);
  }

  public void load(InputStream is)
    throws MessagingException
  {
    String prevline = null;

    StringBuffer lineBuffer = new StringBuffer();
    try
    {
      String line;
      do
      {
        line = StrUtil.readLine(is, 
          System.getProperty("file.encoding"));

        if ((line != null) && (
          (line.startsWith(" ")) || (line.startsWith("\t"))))
        {
          if (prevline != null)
          {
            lineBuffer.append(prevline);
            prevline = null;
          }
          lineBuffer.append("\r\n");
          lineBuffer.append(line);
        }
        else
        {
          if (prevline != null)
          {
            addHeaderLine(prevline);
          }
          else if (lineBuffer.length() > 0)
          {
            addHeaderLine(lineBuffer.toString());
            lineBuffer.setLength(0);
          }
          prevline = line;
        }

        if (line == null) break;  } while (line.length() > 0);
    }
    catch (IOException ioex)
    {
      throw new MessagingException("Error in input stream", ioex);
    }
    String line;
  }
}
