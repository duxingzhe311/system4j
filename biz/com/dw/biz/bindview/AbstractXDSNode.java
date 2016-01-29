package com.dw.biz.bindview;

import com.dw.mltag.AbstractNode;
import com.dw.mltag.XmlNode;
import java.util.Enumeration;

public abstract class AbstractXDSNode extends XmlNode
{
  public AbstractXDSNode(XmlNode normal_node)
  {
    super(normal_node.getNodeName());

    XmlNode pn = (XmlNode)normal_node.getParent();

    for (Enumeration en = normal_node.getAtributeNames(); en.hasMoreElements(); )
    {
      String attrn = (String)en.nextElement();
      String v = normal_node.getAttribute(attrn);
      setAttribute(attrn.toLowerCase(), v);
    }

    int cc = normal_node.getChildCount();
    for (int i = 0; i < cc; i++)
    {
      AbstractNode cn = (AbstractNode)normal_node.getChildAt(0);
      normal_node.remove(0);
      addChild(cn);
    }

    if (pn != null)
    {
      int p = pn.getIndex(normal_node);
      pn.setChild(this, p);
    }
  }
}
