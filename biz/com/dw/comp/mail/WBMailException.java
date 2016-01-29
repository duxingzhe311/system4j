package com.dw.comp.mail;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WBMailException extends Exception
{
  private boolean inline;
  private boolean displayed = false;

  private Set m_Descriptions = new HashSet(5);
  private Exception m_Exception;

  public WBMailException(String msg)
  {
    super(msg);
    addDescription(msg);
    this.inline = false;
  }

  public WBMailException(String msg, boolean inlined)
  {
    super(msg);
    addDescription(msg);
    this.inline = inlined;
  }

  public WBMailException setInlineError(boolean b)
  {
    this.inline = b;
    return this;
  }

  public boolean isInlineError()
  {
    return this.inline;
  }

  public void addDescription(String description)
  {
    this.m_Descriptions.add(description);
  }

  public String[] getDescriptions()
  {
    String[] desc = new String[this.m_Descriptions.size()];
    return (String[])this.m_Descriptions.toArray(desc);
  }

  public Iterator iterator()
  {
    return this.m_Descriptions.iterator();
  }

  public void setDisplayed(boolean b)
  {
    this.displayed = b;
  }

  public boolean isDisplayed()
  {
    return this.displayed;
  }

  public WBMailException setException(Exception ex)
  {
    this.m_Exception = ex;
    return this;
  }

  public boolean hasException()
  {
    return this.m_Exception != null;
  }

  public String getExceptionTrace()
  {
    if (hasException())
    {
      StringWriter trace = new StringWriter();
      this.m_Exception.printStackTrace(new PrintWriter(trace));
      return trace.toString();
    }

    return "";
  }

  public Exception getException()
  {
    return this.m_Exception;
  }

  public void printStackTrace(PrintStream s)
  {
    if (this.m_Exception != null) {
      this.m_Exception.printStackTrace(s);
    }
    super.printStackTrace(s);
  }

  public void printStackTrace(PrintWriter s)
  {
    if (this.m_Exception != null) {
      this.m_Exception.printStackTrace(s);
    }
    super.printStackTrace(s);
  }
}
