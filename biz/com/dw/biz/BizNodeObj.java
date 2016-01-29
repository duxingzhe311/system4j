package com.dw.biz;

public abstract class BizNodeObj
{
  private BizNode belongToNode = null;

  public final String getName()
  {
    return this.belongToNode.getNodeName();
  }

  public BizNode getBelongToBizNode()
  {
    return this.belongToNode;
  }

  public BizPath getBizPath()
  {
    return this.belongToNode.getBizPath();
  }

  public final String getBizPathStr()
  {
    return getBizPath().toString();
  }

  public final BizModule getBizModule()
  {
    return this.belongToNode.getBelongToBizModule();
  }

  public final BizModuleIO getBizModuleIO()
  {
    return getBizModule().getBizModuleIO();
  }

  public final BizContainer getBizContainer()
  {
    return getBizModuleIO().getBizContainer();
  }

  void setBelongToBizNode(BizNode bn)
  {
    this.belongToNode = bn;
  }
}
