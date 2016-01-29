package com.dw.comp.webmail.util;

public class SingleTaskQueue
{
  ISingleTaskHandler sth = null;

  public SingleTaskQueue(ISingleTaskHandler h)
  {
    this.sth = h;
  }

  public void enqueue(Object o)
  {
  }
}
