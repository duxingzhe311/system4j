package com.dw.user;

import java.io.*;
import java.util.*;

import javax.swing.tree.*;

import com.dw.system.Convert;
import com.dw.system.xmldata.xrmi.XRmi;
import com.dw.system.xmldata.IXmlDataable;
import com.dw.system.xmldata.XmlData;

/// <summary>
/// 组织机构节点
/// </summary>
@XRmi(reg_name="security_org_node")
public class OrgNode implements Comparable,IXmlDataable
{
	public static class ListItem
	{
		String nodeId = null ;
		String title = null ;
		OrgNode orgNode = null ;
		
		public ListItem(String nid,String t)
		{
			nodeId = nid ;
			title = t ;
		}
		
		public ListItem(String nid,String t,OrgNode ond)
		{
			nodeId = nid ;
			title = t ;
			orgNode = ond ;
		}
		
		public String getNodeId()
		{
			return nodeId ;
		}
		
		public String getTitle()
		{
			return title ;
		}
		
		public OrgNode getOrgNode()
		{
			return orgNode ;
		}
	}
	// / <summary>
	// / 组织机构节点唯一id
	// / </summary>
	String orgNodeId = null;

	// / <summary>
	// / 组织机构节点名称
	// / </summary>
	String orgNodeName = null;
	
	
	String orgNodeTitle = null ;

	// / <summary>
	// / 组织机构节点描述
	// / </summary>
	String orgNodeDesc = null;

	int orderNum = 0;

	String parentNodeId = null;

//	List<String> userNameList = new ArrayList<String>();
//
//	List<Integer> roleIdList = new ArrayList<Integer>();

	List<OrgNode> subOrgNodes = new ArrayList<OrgNode>();

	OrgNode parentNode = null;
	
	//transient OrgSubDynProvider dynSubNodeProvider = null ;
	
	transient OrgNodeExtItem extItem = null ;

	public OrgNode()
	{
	}
	
	public OrgNode(String id, String name, String desc, String parentid)
	{
		this(id,name,desc,0,parentid);
	}

	public OrgNode(String id, String name,String desc,int ordern, String parentid)
	{
		this(id, name,null, desc,ordern, parentid);
	}
	
	public OrgNode(String id, String name,String title, String desc,int ordern, String parentid)
	{
		this.orgNodeId = id;
		this.orgNodeName = name;
		this.orgNodeTitle = title ;
		this.orgNodeDesc = desc;
		orderNum = ordern;
		this.parentNodeId = parentid;
	}
	
	private String get_OrgNodeId()
	{
		return orgNodeId ;
	}
	private void set_OrgNodeId(String onid)
	{
		orgNodeId = onid ;
	}
	private String get_OrgNodeName()
	{
		return orgNodeName ;
	}
	private void set_OrgNodeName(String n)
	{
		orgNodeName = n ;
	}
	private String get_OrgNodeTitle()
	{
		return orgNodeTitle ;
	}
	private void set_OrgNodeTitle(String t)
	{
		orgNodeTitle = t ;
	}
	private String get_OrgNodeDesc()
	{
		return orgNodeDesc ;
	}
	private void set_OrgNodeDesc(String d)
	{
		orgNodeDesc = d ;
	}
	private int get_OrgNodeOrder()
	{
		return orderNum ;
	}
	private void set_OrgNodeOrder(int o)
	{
		orderNum = o ;
	}
	private String get_OrgNodeParentId()
	{
		return parentNodeId ;
	}
	private void set_OrgNodeParentId(String i)
	{
		parentNodeId = i ;
	}

	public boolean equals(Object obj)
	{
		if(obj==null)
			return false;
		return orgNodeId.equals(((OrgNode) obj).orgNodeId);
	}

	public int hashCode()
	{
		return orgNodeId.hashCode();
	}


	public String getOrgNodeId()
	{
		return orgNodeId;
	}

	// / <summary>
	// / 节点在组织机构树中的层次（代）
	// / 其中，根节点level=0
	// / </summary>
	public int getLevel()
	{
		if (parentNode == null)
			return 0;

		return parentNode.getLevel() + 1;
	}

	public boolean isValid()
	{
		return orgNodeId!=null;
	}

	public String getOrgNodeName()
	{
		return orgNodeName;
		// set
		// {
		// orgNodeName = value;
		// }
	}
	
	public String getOrgNodeTitle()
	{
		if(Convert.isNotNullEmpty(orgNodeTitle))
			return orgNodeTitle ;
		
		return orgNodeName ;
	}

	public String getOrgNodeDesc()
	{
		return orgNodeDesc;

		// set
		// {
		// orgNodeDesc = value;
		// }
	}

	public int getOrderNum()
	{
		return orderNum;
	}

	// / <summary>
	// / 获取所有的子节点
	// / </summary>
	public OrgNode[] getSubOrgNodes()
	{
		OrgNode[] rets = new OrgNode[subOrgNodes.size()];
		subOrgNodes.toArray(rets);
		return rets;
	}

	// / <summary>
	// / 所有子节点id组成的字符串，它可以用来组合sql语句
	// / 如果该节点是叶子节点，则返回""
	// / 如果有子节点，则返回形如"1,2,3"
	// / </summary>
	public String getSubOrgNodeIdStr()
	{
		if (subOrgNodes == null)
			return "";

		int c = subOrgNodes.size();
		if (c == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append("'").append(subOrgNodes.get(0).getOrgNodeId()).append("'");

		for (int i = 0; i < c; i++)
			sb.append(",'").append(subOrgNodes.get(i).getOrgNodeId()).append("'");

		return sb.toString();
	}

	// / <summary>
	// / 获取本节点的所有子孙节点的id组合字符串
	// / 如果该节点是叶子节点，则返回""
	// / 如果有子节点，则返回形如"1,2,3"
	// / </summary>
	public String getOffspringOrgNodeIdStr()
	{
		List<OrgNode> ll = getOffspringOrgNodes();

		int c = ll.size();
		if (c == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		sb.append("'").append(ll.get(0).getOrgNodeId()).append("'");

		for (int i = 0; i < c; i++)
			sb.append(",'").append(ll.get(i).getOrgNodeId()).append("'");

		return sb.toString();
	}

	// / <summary>
	// / 获取本节点及所有子孙节点的id组合字符串
	// / 返回形如"1,2,3"
	// / </summary>
	public String getSelfAndOffspringOrgNodeIdStr()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("'").append(this.getOrgNodeId()).append("'");

		for (OrgNode n : getOffspringOrgNodes())
		{
			sb.append(",'").append(n.getOrgNodeId()).append("'");
		}

		return sb.toString();
	}

	// / <summary>
	// / 获得本节点的所有子孙节点
	// / </summary>
	public List<OrgNode> getOffspringOrgNodes()
	{
		List<OrgNode> ll = new ArrayList<OrgNode>();
		NestGetOffspringNodes(this, ll);
		return ll;
	}

	private void NestGetOffspringNodes(OrgNode cur_node, List<OrgNode> listns)
	{
		if (cur_node.subOrgNodes == null)
			return;

		for (OrgNode n : cur_node.subOrgNodes)
		{
			listns.add(n);

			NestGetOffspringNodes(n, listns);
		}
	}

	public OrgNode getSubNodeByName(String orgname)
	{
		for (OrgNode n : subOrgNodes)
		{
			if (n.getOrgNodeName().equals(orgname))
				return n;
		}

		return null;
	}

	public OrgNode getSubNodeById(String node_id)
	{
		for (OrgNode n : subOrgNodes)
		{
			if (n.orgNodeId.equals(node_id))
				return n;
		}

		return null;
	}

	public boolean hasSubNode()
	{
//		if(dynSubNodeProvider!=null)
//		{
//			return dynSubNodeProvider.hasSubNodes() ;
//		}
//		else
//		{
//			return subOrgNodes.size() > 0;
//		}
		
		return subOrgNodes.size() > 0;
	}

	public int getSubNodeCount()
	{
//		if(dynSubNodeProvider!=null)
//		{
//			return dynSubNodeProvider.getSubNodesCount() ;
//		}
//		else
//		{
//			return subOrgNodes.size();
//		}
		
		return subOrgNodes.size();
	}

	public OrgNode getParentNode()
	{
		return parentNode;
	}

	public boolean isRoot()
	{
		return parentNode == null;
	}

	public String getPath()
	{
		if (parentNode == null)
		{
//			if(this.getOrgNodeId()==0)
//				return "/" ;
//			else
//				return "/" + getOrgNodeName();
			return "/";
		}

		return parentNode.getPath()+ getOrgNodeName() + "/" ;
	}
	
	public String getPathDesc()
	{
		if (parentNode == null)
		{
			return "/";
		}

		return parentNode.getPathDesc()+ getOrgNodeTitle() + "/" ;
	}
	
	public void appendSubOrgNode(OrgNode sn)
	{
		subOrgNodes.add(sn);
		sn.parentNode = this;

		OrgManager.sort(subOrgNodes);
	}

	/**
	 * 判断一个节点是否是自己的祖先节点
	 * @param o
	 * @return
	 */
	public boolean isAncestorNode(OrgNode o)
	{
		if (o.orgNodeId.equals(this.orgNodeId))
			return false;

		if (this.parentNode == null)
			return false;
		
		if(this.parentNode.orgNodeId.equals(o.orgNodeId))
			return true ;

		return parentNode.isAncestorNode(o);
	}

	void removeSubNode(OrgNode o)
	{
		if (subOrgNodes.remove(o))
			o.parentNode = null;
	}

	// / <summary>
	// / 改变一个节点成为自己的子节点
	// / </summary>
	// / <param name="o"></param>
	void setSubNode(OrgNode o)
	{
		if (o.isRoot())
			throw new RuntimeException("Root node cannot be changed!");

		if (o.orgNodeId.equals(this.orgNodeId))
			throw new RuntimeException("cannot set self to be sub node");

		if (isAncestorNode(o))
			throw new RuntimeException("Cannot set ancestor to be sub node");

		if (this.getSubNodeById(o.orgNodeId) != null)
			return;

		// 从该节点的父节点摘除
		if (o.getParentNode() != null)
			o.getParentNode().removeSubNode(o);

		o.parentNode = this;

		OrgNode[] objs = new OrgNode[subOrgNodes.size() + 1];
		subOrgNodes.toArray(objs);
		objs[objs.length - 1] = o;
		Arrays.sort(objs);

		ArrayList<OrgNode> neworgnodes = new ArrayList<OrgNode>(objs.length);
		for (OrgNode tmpo : objs)
			neworgnodes.add(tmpo);

		subOrgNodes = neworgnodes;
	}

	public int compareTo(Object obj)
	{
		return orderNum - ((OrgNode) obj).orderNum;
	}
	
	/**
	 * 获得本节点对应的子节点扩展定义信息
	 * @return
	 */
	public Class getSubNodeExtClass()
	{
		return OrgManager.getDefaultIns().getOrgNodeSubNodeExtClass(this.orgNodeId) ;
	}
	
	public OrgNodeExtItem getExtItem() throws Exception
	{
		if(extItem!=null)
			return extItem;
		
		extItem = OrgManager.getDefaultIns().getOrgNodeExtItem(this.orgNodeId) ;
		return extItem;
	}

	private void ContructTN(DefaultMutableTreeNode tn, OrgNode n)
	{
		tn.setUserObject(n);

		for (OrgNode tmpn : n.getSubOrgNodes())
		{
			DefaultMutableTreeNode tmptn = new DefaultMutableTreeNode();
			tn.add(tmptn);

			ContructTN(tmptn, tmpn);
		}
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("id", orgNodeId);
		xd.setParamValue("name", orgNodeName);
		if(orgNodeDesc != null)
		{
			xd.setParamValue("desc", orgNodeDesc);
		}
		xd.setParamValue("ordernum", orderNum);
		xd.setParamValue("parent_node_id", parentNodeId);

//		if(subOrgNodes!=null)
//		{
//			List<XmlData> xds = xd.getOrCreateSubDataArray("sub_nodes");
//			for(OrgNode n:subOrgNodes)
//			{
//				xds.add(n.toXmlData());
//			}
//		}

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		orgNodeId = xd.getParamValueStr("id");
		orgNodeName = xd.getParamValueStr("name");
		orgNodeDesc = xd.getParamValueStr("desc");
		
		orderNum = xd.getParamValueInt32("ordernum", 0);
		parentNodeId = xd.getParamValueStr("parent_node_id");

//		List<XmlData> xds = xd.getSubDataArray("sub_nodes");
//		if(xds!=null)
//		{
//			for(XmlData tmpxd:xds)
//			{
//				OrgNode subn = new OrgNode();
//				subn.fromXmlData(tmpxd);
//				subn.parentNode = this;
//				subn.parentNodeId = this.orgNodeId;
//				subOrgNodes.add(subn);
//			}
//		}
	}

	// / <summary>
	// / 获得用户WebTree控件使用的TreeNode对象
	// / </summary>
	// public TreeNode ToWebTreeNode()
	// {
	// TreeNode tn = new TreeNode();
	// ContructTN(tn, this);
	// return tn;
	// }
}
