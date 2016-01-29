package com.dw.biz;

public abstract interface IBizIO
{
  public abstract int getNewId()
    throws Exception;

  public abstract void saveModuleById(int paramInt, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadModuleById(int paramInt)
    throws Exception;

  public abstract int[] loadModuleAllIds()
    throws Exception;

  public abstract void saveFormById(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadFormById(int paramInt)
    throws Exception;

  public abstract long loadFormLastUpdateTime(int paramInt)
    throws Exception;

  public abstract int[] loadFormAllIds()
    throws Exception;

  public abstract int getFormModuleId(int paramInt)
    throws Exception;

  public abstract void changedFormModule(int paramInt1, int paramInt2)
    throws Exception;

  public abstract void saveViewById(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadViewById(int paramInt)
    throws Exception;

  public abstract long loadViewLastUpdateTime(int paramInt)
    throws Exception;

  public abstract int[] loadViewAllIds()
    throws Exception;

  public abstract int getViewModuleId(int paramInt)
    throws Exception;

  public abstract void changedViewModule(int paramInt1, int paramInt2)
    throws Exception;

  public abstract void saveActionById(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadActionById(int paramInt)
    throws Exception;

  public abstract long loadActionLastUpdateTime(int paramInt)
    throws Exception;

  public abstract int[] loadActionAllIds()
    throws Exception;

  public abstract int getActionModuleId(int paramInt)
    throws Exception;

  public abstract void changedActionModule(int paramInt1, int paramInt2)
    throws Exception;

  public abstract void saveFunctionById(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadFunctionById(int paramInt)
    throws Exception;

  public abstract long loadFunctionLastUpdateTime(int paramInt)
    throws Exception;

  public abstract int[] loadFunctionAllIds()
    throws Exception;

  public abstract int getFunctionModuleId(int paramInt)
    throws Exception;

  public abstract void changedFunctionModule(int paramInt1, int paramInt2)
    throws Exception;

  public abstract void saveWorkItemTempById(int paramInt1, int paramInt2, byte[] paramArrayOfByte)
    throws Exception;

  public abstract byte[] loadWorkItemTempById(int paramInt)
    throws Exception;

  public abstract long loadWorkItemTempLastUpdateTime(int paramInt)
    throws Exception;

  public abstract int[] loadWorkItemTempAllIds()
    throws Exception;

  public abstract int getWorkItemTempModuleId(int paramInt)
    throws Exception;

  public abstract void changedWorkItemTempModule(int paramInt1, int paramInt2)
    throws Exception;
}

/* Location:           F:\cxb\oldworkspace\tbs240\tomato_server\lib\biz.jar
 * Qualified Name:     com.dw.biz.IBizIO
 * JD-Core Version:    0.6.2
 */