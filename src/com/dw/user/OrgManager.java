package com.dw.user;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.logger.*;
import com.dw.system.xmldata.XmlData;
import com.dw.user.provider.DefaultOrgProvider;
import com.dw.user.provider.DefaultUserProvider;

public class OrgManager
{
	public final static String ROLE_SUB_ORGNODE_MGR = "sub_orgnode_mgr" ;
	
	static Object lockObj = new Object();

	static OrgManager defOrgMgr = null;
	
	static ILogger log = LoggerManager.getLogger(OrgManager.class.getCanonicalName());

	public static OrgManager getDefaultIns()
	{
		if(defOrgMgr!=null)
			return defOrgMgr ;
		
		synchronized(lockObj)
		{
			if(defOrgMgr!=null)
				return defOrgMgr ;
			
			defOrgMgr = new OrgManager(new DefaultOrgProvider());
			return defOrgMgr ;
		}
	}
//	public static OrgManager getInstance()
//	{
//		if (orgMgr != null)
//			return orgMgr;
//
//		synchronized (lockObj)
//		{
//			if (orgMgr != null)
//				return orgMgr;
//
//			try
//			{
//				OrgProvider tmpop = null;// get from config
//				orgMgr = new OrgManager(tmpop);
//				return orgMgr;
//			}
//			catch(Exception ee)
//			{
//				log.error(ee);
//				ee.printStackTrace();
//				return null;
//			}
//		}
//	}

	private OrgProvider op = null;// new DefaultOrgProvider();

	private OrgProvider getProvider()
	{
		return op;
	}

	OrgNode rootNode = null;

	Hashtable<String, OrgNode> id2node = null;//new Hashtable<String, OrgNode>();
	
	transient private boolean bInited = false; 
	
	/**
	 * 组织结构节点到子节点扩展对象的映射
	 */
	transient HashMap<String,Class> parentNodeId2SubExtClass= new HashMap<String,Class>() ;

	public OrgManager(OrgProvider op)
	{
		this.op = op;

		//init();
	}
	
	private void init() throws Exception
	{
		if(bInited)
			return ;
		
		synchronized(this)
		{
			if(bInited)
				return ;
			
			OrgNode tmprn = new OrgNode("0", "root", "机构根", null);
			Hashtable<String, OrgNode> tmpid2n = new Hashtable<String, OrgNode>();
			tmpid2n.put("0", tmprn);
	
			// 从Provider获取所有的数据，并进行树状结构的构造
			List<OrgNode> ns = getProvider().GetAllOrgNodes();
			for (OrgNode n : ns)
			{
				tmpid2n.put(n.getOrgNodeId(), n);
			}
	
			for (OrgNode n : ns)
			{
				if (n.parentNodeId==null)
					continue;
	
				OrgNode pn = tmpid2n.get(n.parentNodeId);
				if (pn != null)
				{
//					pn.subOrgNodes.add(n);
//					n.parentNode = pn;
//	
//					sort(pn.subOrgNodes);
					pn.appendSubOrgNode(n) ;
				}
			}
		
			rootNode = tmprn ;
			id2node = tmpid2n ;
			
			bInited = true ;
		}
	}

	public OrgNode getRootOrgNode() throws Exception
	{
		if(!bInited)
			init();
		
		return rootNode;
	}

	// / <summary>
	// / 获得用户WebTree控件使用的TreeNode对象
	// / </summary>
	// public TreeNode RootWebTreeNode
	// {
	// get
	// {
	// return rootNode.ToWebTreeNode();
	// //TreeNode tn = new TreeNode();
	// //ContructTN(tn, rootNode);
	// //return tn;
	// }
	// }

	// / <summary>
	// / 获取用户web下拉列表用的ListItem[]数组
	// / </summary>
	// public ListItem[] WebListItems
	// {
	// get
	// {
	// List<ListItem> ll = new List<ListItem>();
	//
	// SetWebListItems(ll, rootNode);
	//
	// return ll.ToArray();
	// }
	// }
	//
	// private void SetWebListItems(List<ListItem> ll, OrgNode curNode)
	// {
	//            
	// String tmps = "" ;
	// int c = curNode.Level ;
	// for (int i = 0; i < c; i++)
	// tmps += " ";
	//
	// tmps += curNode.OrgNodeName;
	//
	// ListItem tmpli = new ListItem(tmps,""+curNode.OrgNodeId);
	// ll.Add(tmpli);
	//
	// foreach (OrgNode n in curNode.SubOrgNodes)
	// {
	// SetWebListItems(ll, n);
	// }
	// }
	
	/**
	 * 设置某一个组织机构节点的子节点的扩展信息
	 */
	public void setSubNodeExtClass(Class subnode_extc) throws Exception
	{
		//如果扩展子节点的父节点不存在，则自动创建
		OrgNodeExtItem subnode_ext = (OrgNodeExtItem)subnode_extc.newInstance() ;
		String[] nameps = subnode_ext.getParentOrgNodePathNameArray() ;
		String[] titleps = subnode_ext.getParentOrgNodePathTitleArray() ;
		String[] descps = subnode_ext.getParentOrgNodePathDescArray() ;
		
		String curppath = "/";
		OrgNode pnode = this.getRootOrgNode() ;
		for(int i = 0 ; i < nameps.length ; i ++)
		{
			pnode = this.createOrgNodeWhenNotExists(curppath,nameps[i],titleps[i],descps[i]) ;
			curppath += nameps[i] +"/" ;
		}
		
		parentNodeId2SubExtClass.put(pnode.getOrgNodeId(), subnode_extc) ;
	}
	
	
	public void setSubNodeExtClass(String orgnodeid,Class subnode_extc)
	{
		parentNodeId2SubExtClass.put(orgnodeid, subnode_extc) ;
	}

	public OrgNode GetOrgNodeById(String org_id) throws Exception
	{
		if(!bInited)
			init();
		
		return id2node.get(org_id);
	}
	
	/**
	 * 根据组织机构节点id获得本届的的子节点所使用的扩展信息
	 * @param nodeid
	 * @return
	 */
	public Class getOrgNodeSubNodeExtClass(String nodeid)
	{
		return this.parentNodeId2SubExtClass.get(nodeid) ;
	}
	
	
	
	public List<OrgNode> findOrgNodeByName(String name) throws Exception
	{
		if(!bInited)
			init();
		
		List<OrgNode> ns = new ArrayList<OrgNode>();
		
		for(Enumeration<OrgNode> en = id2node.elements();en.hasMoreElements();)
		{
			OrgNode tmpn = en.nextElement();
			if(tmpn.getOrgNodeName().equals(name))
				ns.add(tmpn);
		}
		
		return ns ;
	}

	/**
	 * 根据路径获得对应的机构节点
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public OrgNode getOrgNodeByPath(String path) throws Exception
	{
		if(!bInited)
			init();
		
		if(Convert.isNullOrEmpty(path))
			return null ;
		
		if(path.equals("/"))
			return this.getRootOrgNode() ;
		
		StringTokenizer st = new StringTokenizer(path,"/");
		OrgNode tmpn = this.rootNode;
		while(st.hasMoreTokens())
		{
			String n = st.nextToken() ;
			tmpn = tmpn.getSubNodeByName(n);
			if(tmpn==null)
				return null ;
		}
		return tmpn ;
	}
	
	public OrgNode CreateOrgNode(String parent_id, String name, String desc)
	throws Exception
	{
		return CreateOrgNode(parent_id, name,null, desc) ;
	}

	public OrgNode CreateOrgNode(String parent_id, String name,String title, String desc)
			throws Exception
	{
		if(!bInited)
			init();
		
		OrgNode o = GetOrgNodeById(parent_id);
		if (o == null)
			throw new Exception("cannot find orgnode with id=" + parent_id);
		return CreateOrgNode(parent_id, name,title, desc, o.getSubNodeCount() + 1);
	}

	// / <summary>
	// / 创建一个新的机构节点
	// / </summary>
	// / <param name="parent_id">必须大于或等于0</param>
	// / <param name="name">机构节点名称，它要求在同一个父节点下唯一</param>
	// / <param name="desc">机构节点描述</param>
	public OrgNode CreateOrgNode(String parent_id, String name,String title, String desc,
			int ordernum) throws Exception
	{
		if(!bInited)
			init();
		
		if (parent_id==null)
		{
			throw new IllegalArgumentException("parent id < 0");
		}

		//if (name == null || name.equals(""))
		//	throw new IllegalArgumentException(
		//			"org node name cannot null or empty!");
		Convert.checkVarName(name);

		OrgNode o = GetOrgNodeById(parent_id);
		if (o == null)
			throw new Exception("cannot find orgnode with id=" + parent_id);

		OrgNode tmpon = o.getSubNodeByName(name);
		if (tmpon != null)
		{
			throw new Exception("同一个节点下的名称不能重复！");
			// throw new Exception("the org node name=" + name + " is already
			// existed in org node="+o.OrgNodeName);
		}

		OrgNode non = getProvider().CreateOrgNode(name,title, desc, ordernum,
				parent_id);

		//orgMgr = null;
		o.subOrgNodes.add(non);
		non.parentNode = o;
		
		id2node.put(non.getOrgNodeId(), non);

		sort(o.subOrgNodes);
		
		return non;
	}
	
	public OrgNode createOrgNodeWhenNotExists(String parent_path,String name,String title,String desc)
		throws Exception
	{
		OrgNode pn = getOrgNodeByPath(parent_path) ;
		if(pn==null)
			throw new Exception("no node found with path="+parent_path) ;
		
		OrgNode n = pn.getSubNodeByName(name) ;
		if(n!=null)
			return n ;
		
		return CreateOrgNode(pn.getOrgNodeId(), name,title, desc) ;
	}
	
	public void UpdateOrgNode(String node_id,String name,String desc)
	throws Exception
	{
		UpdateOrgNode(node_id,name,null, desc);
	}
	
	public void UpdateOrgNode(String node_id,String name,String title,String desc)
	throws Exception
	{
		if(!bInited)
			init();
		
		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new IllegalArgumentException("Cannot find org node with id="
					+ node_id);
		
		Convert.checkVarName(name);
		
		UpdateOrgNode(node_id, name,title, desc,
				o.orderNum);
	}

	public void UpdateOrgNode(String node_id, String name,String title, String desc,
			int order_num) throws Exception
	{
		if(!bInited)
			init();
		
		if (node_id==null)
			throw new IllegalArgumentException("parent id is null");

		//if (name == null || name.equals(""))
		//	throw new IllegalArgumentException(
		//			"org node name cannot null or empty!");
		Convert.checkVarName(name);

		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new IllegalArgumentException("Cannot find org node with id="
					+ node_id);

		if (o.getOrgNodeName().equals(name))
		{
			if(Convert.checkStrEqualsIgnoreNullEmpty(o.getOrgNodeDesc(),desc)
					&&
					Convert.checkStrEqualsIgnoreNullEmpty(o.getOrgNodeTitle(),title)
					&&o.orderNum == order_num)
				return ;
		}
		else
		{
			OrgNode tmpo = o.getParentNode().getSubNodeByName(name);
			if (tmpo != null)
				throw new RuntimeException("the org node name=" + name
						+ " is already existed in org node="
						+ tmpo.getOrgNodeName());
		}

		getProvider().UpdateOrgNode(node_id, name,title, desc, order_num);

		o.orgNodeName = name;
		o.orgNodeTitle = title ;
		o.orgNodeDesc = desc;
		o.orderNum = order_num;
	}

	public void ChangeOrgNodeParent(String node_id, String new_parent_id)
			throws Exception
	{
		if(!bInited)
			init();
		
		if (node_id==null)
			throw new IllegalArgumentException("invalid node id");

		if (new_parent_id==null)
			throw new IllegalArgumentException("invalid new parent id");

		if (node_id.equals(new_parent_id))
			throw new IllegalArgumentException("node id = new id");

		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new RuntimeException("Cannot find node with id=" + node_id);

		OrgNode po = GetOrgNodeById(new_parent_id);
		if (po == null)
			throw new RuntimeException("Cannot find node with id="
					+ new_parent_id);

		getProvider().ChangeOrgNodeParent(node_id, new_parent_id);

		po.setSubNode(o);
	}

	public void DeleteOrgNode(String node_id) throws Exception
	{
		if(!bInited)
			init();
		
		if (node_id==null)
			throw new IllegalArgumentException("invalid node id");

		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new RuntimeException("Cannot find node with id=" + node_id);

		if (o.hasSubNode())
			throw new RuntimeException(
					"Cannot delete orgnode because it has sub nodes!");

		OrgNode po = o.getParentNode();

		getProvider().DeleteOrgNode(node_id);

		po.removeSubNode(o);
	}

	// #region 和用户相关的方法

	public void SetUserOrgNodes(String username, String[] nodeids)
			throws Exception
	{
		if(!bInited)
			init();
		
		if (username == null || username.equals(""))
			return;

		if (nodeids == null || nodeids.length <= 0)
			return;

		String[] un = new String[] { username };
		for (String n : nodeids)
		{
			SetUsersToOrgNode(un, n);
		}
	}

	// / <summary>
	// / 把用户添加到组织中
	// /
	// / 注：缺省情况下，用户并不属于机构
	// / </summary>
	// / <param name="usernames"></param>
	// / <param name="node_id"></param>
	public void SetUsersToOrgNode(String[] usernames, String node_id)
			throws Exception
	{
		if(!bInited)
			init();
		
		if (node_id==null)
			throw new IllegalArgumentException("invalid node id");

		if (usernames == null || usernames.length == 0)
			return;

		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new RuntimeException("Cannot find node with id=" + node_id);

		getProvider().SetUserNamesToOrgNode(usernames, node_id);
		if(usernames.length==1)
		{
			List<OrgNode> ons = this.GetOrgNodesByUserName(usernames[0]) ;
			if(ons!=null&&ons.size()==1)
			{
				UserManager.getDefaultIns().setUserBelongOrgNodeId(usernames[0], node_id);
			}
		}
	}

	public void UnSetUsersFromOrgNode(String node_id, String[] usernames)
			throws Exception
	{
		if(!bInited)
			init();
		
		if (node_id==null)
			throw new IllegalArgumentException("invalid node id");

		if (usernames == null || usernames.length == 0)
			return;

		OrgNode o = GetOrgNodeById(node_id);
		if (o == null)
			throw new RuntimeException("Cannot find node with id=" + node_id);

		getProvider().UnsetUserNamesFromOrgNode(usernames, node_id);
		
		if(usernames.length==1)
		{
			List<OrgNode> ons = this.GetOrgNodesByUserName(usernames[0]) ;
			if(ons==null||ons.size()==0)
			{
				UserManager.getDefaultIns().setUserBelongOrgNodeId(usernames[0], null);
			}
		}
	}

	public void UnSetUserOrgNodes(String username, String[] node_ids)
			throws Exception
	{
		if(!bInited)
			init();
		
		if (node_ids == null || node_ids.length <= 0)
			return;

		if (username == null || username.equals(""))
			return;

		String[] un = new String[] { username };
		for (String nid : node_ids)
		{
			UnSetUsersFromOrgNode(nid, un);
		}
	}

	public List<OrgNode> GetOrgNodesByUserName(String username)
			throws Exception
	{
		if(!bInited)
			init();
		
		List<String> ll = getProvider().GetOrgNodeIdsByUserName(username);
		if (ll == null)
			return null;

		List<OrgNode> rets = new ArrayList<OrgNode>(ll.size());
		for (String i : ll)
		{
			OrgNode n = GetOrgNodeById(i);
			if (n == null)
				continue;
			rets.add(n);
		}
		return rets;
	}

	public List<User> GetUsersInOrgNode(String orgnodeid) throws Exception
	{
		if(!bInited)
			init();
		
		return getProvider().GetUsersInOrgNode(orgnodeid);
	}

	public List<User> GetUsersInOrgNodes(List<String> orgnodeids) throws Exception
	{
		if(!bInited)
			init();
		
		return getProvider().GetUsersInOrgNodes(orgnodeids);
	}
	
//	/ <summary>
    /// 获取用户web下拉列表用的ListItem[]数组
    /// </summary>
    public ArrayList<OrgNode.ListItem> getWebListItems() throws Exception
    {
    	if(!bInited)
			init();
    	
    	ArrayList<OrgNode.ListItem> ll = new ArrayList<OrgNode.ListItem>();

            SetWebListItems(ll, rootNode);

            return ll;
    }

    private void SetWebListItems(ArrayList<OrgNode.ListItem> ll, OrgNode curNode)
    {
        String tmps = "" ;
        int c = curNode.getLevel() ;
        for (int i = 0; i < c; i++)
            tmps += "　";

        tmps += curNode.getOrgNodeName();

        OrgNode.ListItem tmpli = new OrgNode.ListItem(curNode.getOrgNodeId(),tmps,curNode);
        ll.add(tmpli);

        for (OrgNode n : curNode.getSubOrgNodes())
        {
            SetWebListItems(ll, n);
        }
    }
    
    /**
     * 根据节点id获得本节点的父节点的子节点扩展信息定义
     * @param nodeid
     * @return
     * @throws Exception
     */
    public Class getOrgNodeParentExtClass(String nodeid)
    throws Exception
    {
    	OrgNode n = this.GetOrgNodeById(nodeid) ;
		if(n==null)
			return null ;
		
		OrgNode pn = n.getParentNode() ;
		if(pn==null)
			return null ;
		
		return getOrgNodeSubNodeExtClass(pn.getOrgNodeId()) ;
    }
    /**
	 * 根据节点id获得本节点的扩展信息-其中扩展信息的定义由父节点决定
	 * @param nodeid
	 * @return
	 * @throws Exception
	 */
	public OrgNodeExtItem getOrgNodeExtItem(String nodeid)
		throws Exception
	{
			Class p_extc = getOrgNodeParentExtClass(nodeid);
			if(p_extc==null)
				return null ;
			
			return (OrgNodeExtItem)GDB.getInstance().getXORMObjByUniqueColValue(p_extc, "OrgNodeId", nodeid, true) ;
	}
	
	public void setOrgNodeExtItem(String nodeid,OrgNodeExtItem uei)
		throws Exception
	{
		Class p_extc = getOrgNodeParentExtClass(nodeid);
		if(p_extc==null)
			return ;
			
		OrgNodeExtItem old_uei = (OrgNodeExtItem)GDB.getInstance().getXORMObjByUniqueColValue(p_extc, "OrgNodeId", nodeid, true) ;
		if(old_uei==null)
		{//insert
			uei.setOrgNodeId(nodeid) ;
			GDB.getInstance().addXORMObjWithNewId(uei) ;
			uei.onAddedByOrgMgr() ;
		}
		else
		{
			uei.setOrgNodeId(nodeid) ;
			//GDB.getInstance().updateXORMObjToDB(old_uei.getAutoId(), uei) ;
			GDB.getInstance().updateXORMObjToDBWithSupportAutoColNames(old_uei.getAutoId(), uei) ;
			uei.onUpdatedByOrgMgr();
		}
	}

	/**
	 * 设置节点的扩展值
	 * @param username
	 * @param propname
	 * @param val
	 * @throws Exception
	 */
	public void setOrgNodeExtItemValue(String nodeid,String[] propname,Object[] val)
		throws Exception
	{
		Class p_extc = getOrgNodeParentExtClass(nodeid);
		if(p_extc==null)
			return ;
		
		OrgNodeExtItem uei = (OrgNodeExtItem)GDB.getInstance().getXORMObjByUniqueColValue(p_extc, "OrgNodeId", nodeid, true) ;
		if(uei==null)
		{//insert
			XmlData xd = new XmlData() ;
			for(int i = 0 ; i < propname.length ; i ++)
			{
				xd.setParamValue(propname[i], val[i]) ;
			}
				
			uei = (OrgNodeExtItem)p_extc.newInstance() ;
			XORMUtil.injectXmlDataToObj(uei, xd) ;
			uei.setOrgNodeId(nodeid);
			
			GDB.getInstance().addXORMObjWithNewId(uei) ;
			uei.onAddedByOrgMgr() ;
		}
		else
		{
			XmlData xd = new XmlData() ;
			for(int i = 0 ; i < propname.length ; i ++)
			{
				xd.setParamValue(propname[i], val[i]) ;
			}
			
			uei = (OrgNodeExtItem)p_extc.newInstance() ;
			XORMUtil.injectXmlDataToObj(uei, xd) ;
			uei.setOrgNodeId(nodeid);
			
			GDB.getInstance().updateXORMObjToDBWithHasColNames(uei.getAutoId(), uei, propname) ;
			uei.onUpdatedByOrgMgr();
		}
	}


	/**
	 * 根据节点扩展的唯一值来获得用户对象
	 * @param ext_propname
	 * @param ext_val
	 * @return
	 * @throws Exception
	 */
	public OrgNode getOrgNodeByUniqueExtValue(Class ext_c,String ext_propname,Object ext_val) throws Exception
	{
		OrgNodeExtItem uei = (OrgNodeExtItem)GDB.getInstance().getXORMObjByUniqueColValue(ext_c, ext_propname, ext_val, false) ;
		if(uei==null)
			return null ;
		
		OrgNode on = this.GetOrgNodeById(uei.orgNodeId) ;
		if(on==null)
			return null ;
		
		on.extItem = uei ;
		return on ;
	}
	
	
	

	// #endregion

	static void sort(List ll)
	{
		Object[] objs = ll.toArray();
		Arrays.sort(objs) ;
		ll.clear();
		for(Object o:objs)
		{
			ll.add(o);
		}
	}
}
