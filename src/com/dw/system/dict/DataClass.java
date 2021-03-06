package com.dw.system.dict;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dw.system.Convert;
import com.dw.system.util.Sorter;
import com.dw.system.xmldata.XmlHelper;

/**
 * 字典数据类,它和一个Xml字典文件对应
 * 
 * @author Jason Zhu
 */
public class DataClass
{
	public static final String ATTRN_EXT_ATTR_NAMES = "ext_attr_names" ;
	public static final String ATTRN_EXT_ATTR_TITLES = "ext_attr_titles" ;
	public static final String ATTRN_EXT_ATTR_DESCS = "ext_attr_descs" ;
	
	/**
	 * 模块名称--如果为空则属于全局的字典
	 */
	String moduleName = null ;
	
	/**
	 * 数据类型id
	 */
	int classId = -1;

	/**
	 * 唯一类名称
	 */
	String name = null ;

	/**
	 * 类型中文名称
	 */
	String classNameCn = null;

	/**
	 * 类型英文名称
	 */
	String classNameEn = null;

	/**
	 * 版本
	 */
	String version = null;

	/**
	 * 创建时间
	 */
	Date createTime = null;

	/**
	 * 上次更新时间
	 */
	Date lastUpdateTime = null;

	/**
	 * 所有根数据节点
	 */
	DataNode[] rootDataNodes = null;

	DataNode[] validRootNodes = null;
	
	/**
	 * 当一个字典使用数据库实现时,可以定义每个节点需要的扩展属性
	 * 这样,当编辑一个字典节点时可以自动列举一些input
	 */
	ArrayList<String[]> extNameTitles = new ArrayList<String[]>() ;

	// / <summary>
	// / 所有的相关节点id到节点的映射
	// / </summary>
	transient HashMap<Integer, DataNode> allid2Nodes = new HashMap<Integer, DataNode>();
	transient HashMap<String, DataNode> allname2Nodes = new HashMap<String, DataNode>();
	
	transient DataClassIO dcIO = null ;
	
	transient DataNode defaultNode = null ;
	
	transient HashMap<String,String> extNameVals = new HashMap<String,String>() ;
	
	/**
	 * 缺省语言-如果本字典类用作多语言支持，则可以设定本对象使用的缺省语言
	 */
	transient String defaultLangStr = null ;
	
	
	transient Element relatedEle = null ;
	
	DataClass(String modulen)
	{
		moduleName = modulen ;
	}

	DataClass(String modulen,Element ele) throws Exception
	{
		moduleName = modulen ;
		
		relatedEle = ele ;
		
		classId = Integer.parseInt(ele.getAttribute("id"));
		name = ele.getAttribute("name");
		
		classNameCn = ele.getAttribute("name_cn");
		classNameEn = ele.getAttribute("name_en");
		
		version = ele.getAttribute("version");
		
		String io = ele.getAttribute("io") ;
		
		Properties ps = XmlHelper.getElementAttributes(ele) ;
		for(Enumeration tmpen =ps.propertyNames();tmpen.hasMoreElements();)
		{
			String n0 = (String)tmpen.nextElement() ;
			String v0 = ps.getProperty(n0);
			
			extNameVals.put(n0, v0);
		}
		
		String ext_attr_names = ele.getAttribute(ATTRN_EXT_ATTR_NAMES);
		if(Convert.isNotNullEmpty(ext_attr_names))
		{
			StringTokenizer nst = new StringTokenizer(ext_attr_names,"|") ;
			String ext_attr_titles = ele.getAttribute(ATTRN_EXT_ATTR_TITLES);
			String ext_attr_descs = ele.getAttribute(ATTRN_EXT_ATTR_DESCS);
			StringTokenizer tst = null ;
			StringTokenizer dst = null ;
			if(ext_attr_titles!=null)
				tst = new StringTokenizer(ext_attr_titles,"|") ;
			if(ext_attr_descs!=null)
				dst = new StringTokenizer(ext_attr_descs,"|") ;
			
			while(nst.hasMoreTokens())
			{
				String n = nst.nextToken().trim() ;
				String t = n ;
				String d = n ;
				if(tst!=null&&tst.hasMoreTokens())
					t = tst.nextToken().trim() ;
				
				if(dst!=null&&dst.hasMoreTokens())
					d = dst.nextToken().trim() ;
				
				extNameTitles.add(new String[]{n,t,d}) ;
			}
		}
		
		
		if(Convert.isNullOrEmpty(io))
		{
			Element[] eles = XmlHelper.getSubChildElement(ele,"dd_node");
			if(eles==null)
				return ;
			
			ArrayList<DataNode> tmpnds = new ArrayList<DataNode>(eles.length) ;
			for(int i = 0 ; i < eles.length ; i ++)
			{
				DataNode tmpdn = new DataNode(eles[i],null);
				if(tmpdn.getId()<=0)
					continue ;
				
				tmpnds.add(tmpdn);
				tmpdn.belongToClassId = classId;
				tmpdn.belongToModule = this.moduleName ;
				
				//设置id和名称到节点的映射
				allid2Nodes.put(tmpdn.getId(), tmpdn);
				
				String tmpn = tmpdn.getName() ;
				if(tmpn!=null&&!tmpn.equals(""))
					allname2Nodes.put(tmpn, tmpdn);
				
				DataNode[] tmpdns = tmpdn.getValidOffspringNodes() ;
				if(tmpdns!=null)
				{
					for(DataNode dn0:tmpdns)
					{
						allid2Nodes.put(dn0.getId(), dn0);
						tmpn = dn0.getName();
						if(tmpn!=null&&!tmpn.equals(""))
							allname2Nodes.put(tmpn, dn0);
					}
				}
			}
			
			rootDataNodes = new DataNode[tmpnds.size()];
			tmpnds.toArray(rootDataNodes);
		}
		else
		{
			dcIO = DictManager.getDataClassIO(io) ;
			if(dcIO==null)
				throw new RuntimeException("cannot find DataClass IO ");
			
			reloadClass() ;
		}
	}
	
	private void reloadClass() throws Exception
	{
		if(dcIO==null)
			return ;
		
		List<DataNode> dns = dcIO.loadAllDataNodes(moduleName,classId);
		if(dns!=null)
		{
			structDataNodeTree(this,dns) ;
		}
		
		this.validRootNodes = null ;
		this.allValidLeafNodes = null ;
		this.allExpandedNodes = null;
		this.allValidExpandedNodes = null ;
	}
	
	
	public Element getRelatedEle()
	{
		return relatedEle ;
	}
	
	public DataClassIO getIO()
	{
		return dcIO ;
	}
	
	public String getModuleName()
	{
		return moduleName ;
	}
	
	private static DataClass structDataNodeTree(DataClass dc,List<DataNode> dns)
    {
        ArrayList<DataNode> notroots = new ArrayList<DataNode>();

        ArrayList<DataNode> roots = new ArrayList<DataNode>() ;

        HashMap<Integer,DataNode> id2nodes = new HashMap<Integer,DataNode>();
        HashMap<String,DataNode> name2nodes = new HashMap<String,DataNode>();
        //抽取根节点，同时
        int dn_num = dns.size() ;
        for(int i = 0 ; i < dn_num ; i ++)
        {
        	DataNode tmpdn = dns.get(i) ;
        	if(tmpdn.isDefaultNode())
        		dc.defaultNode = tmpdn ;
        	
        	tmpdn.belongToClassId = dc.getClassId();
        	tmpdn.belongToModule = dc.getModuleName();
        	
        	id2nodes.put(tmpdn.id,tmpdn) ;
            
			String tmpn = tmpdn.getName() ;
			if(tmpn!=null&&!tmpn.equals(""))
				name2nodes.put(tmpn, tmpdn);
			
            if(tmpdn.parentNodeId<=0)
            {
                roots.add(tmpdn) ;
            }
            else
            {
                notroots.add(tmpdn) ;
            }
        }


        for(DataNode dn : notroots)
        {
            DataNode pdn = (DataNode)id2nodes.get(dn.parentNodeId) ;
            if(pdn!=null)
            	pdn.appendChildNode(dn) ;
        }

        Sorter.sort(roots) ;
        
        DataNode[] ss = new DataNode[roots.size()];
        dc.rootDataNodes = roots.toArray(ss) ;
        dc.allid2Nodes = id2nodes;
        dc.allname2Nodes = name2nodes;
        return dc ;
    }

	/**
	 * 数据类型id
	 * @return
	 */
	public int getClassId()
	{
		return classId;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getClassName()
	{
		return name ;
	}

	// / <summary>
	// / 类型中文名称
	// / </summary>
	public String getClassNameCn()
	{
		return classNameCn;
	}

	// / <summary>
	// / 类型英文名称
	// / </summary>
	public String getClassNameEn()
	{
		return classNameEn;
	}

	public List<String[]> getExtNameTitles()
	{
		return this.extNameTitles ;
	}
	
	public boolean isContainDefExtName(String n)
	{
		if(extNameTitles==null)
			return false;
		
		for(String[] nt:extNameTitles)
		{
			if(nt[0].equals(n))
				return true ;
		}
		return false;
	}
	
	/**
	 * 获取扩展属性
	 * @param pn
	 * @return
	 */
	public String getExtAttrValue(String pn)
	{
		return extNameVals.get(pn) ;
	}
	// / <summary>
	// / 版本
	// / </summary>
	public String getVersion()
	{
		return version;
	}

	// / <summary>
	// / 创建时间
	// / </summary>
	public Date getCreateTime()
	{
		return createTime;
	}

	// / <summary>
	// / 上次更新时间
	// / </summary>
	public Date getLastUpdateTime()
	{
		return lastUpdateTime;
	}

	/**
	 * 根据id获取对应的节点对象
	 * @param id
	 * @return
	 */
	public DataNode findDataNodeById(int id)
	{
		return findDataNodeById(id,false);
	}
	
	/**
	 * 
	 * @param id
	 * @param get_default
	 * @return
	 */
	public DataNode findDataNodeById(int id,boolean get_default)
	{
		DataNode dn = allid2Nodes.get(id);
		if(dn!=null)
			return dn ;
		
		return get_default?defaultNode:null ;
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public DataNode findDataNodeByName(String name)
	{
		return findDataNodeByName(name,false);
	}
	
	public DataNode findDataNodeByName(String name,boolean get_default)
	{
		DataNode dn = allname2Nodes.get(name);
		if(dn!=null)
			return dn ;
		
		return get_default?defaultNode:null ;
	}
	
	public void setDefaultLang(String lang)
	{
		defaultLangStr = lang ;
	}
	
	
	public String getDataNodeLangByName(String name)
	{
		DataNode dn = findDataNodeByName(name) ;
		if(dn==null)
			return "[x"+name+"x]" ;
		
		return dn.getNameByLang(defaultLangStr) ;
	}

	// / <summary>
	// / 根据输入的字符串组成的id进行对应节点的获取，id中间用' ',',','|',';'分割
	// / </summary>
	// / <param name="idstr"></param>
	// / <returns></returns>
	public List<DataNode> findDataNodeByIdsStr(String idstr)
	{
		if (idstr == null)
			return null;

		StringTokenizer st = new StringTokenizer(idstr, " ,|;");
		ArrayList<DataNode> rets = new ArrayList<DataNode>();
		while (st.hasMoreTokens())
		{
			DataNode dn = findDataNodeById(Integer.parseInt(st.nextToken()));
			if (dn == null)
				continue;

			rets.add(dn);
		}

		return rets;
	}

	// / <summary>
	// / 根据节点名称获得对应的节点
	// / </summary>
	// / <param name="namecn"></param>
	// / <returns></returns>
//	public DataNode findDataNodeByName(String name)
//	{
//		for (DataNode dn : getAllExpandedNodes())
//		{
//			if (dn.getNameCN().equals(name))
//				return dn;
//
//			if (dn.getNameEn().equals(name))
//				return dn;
//		}
//
//		return null;
//	}

	// / <summary>
	// / 如果节点不存在，则返回-1
	// / </summary>
	// / <param name="id">节点id</param>
	// / <returns>1代表根，2代表层次2。。。-1代表不存在</returns>
	public int getNodeLevelByNodeId(int id)
	{
		DataNode dn = (DataNode) allid2Nodes.get(id);
		if (dn == null)
			return -1;

		return dn.getLevel();
	}

	class TmpNode
	{
		DataNode dn = null;

		ArrayList localChild = new ArrayList();
	}

	// / <summary>
	// / 获取所有的根节点
	// / </summary>
	public DataNode[] getRootNodes()
	{
		return this.rootDataNodes;
	}

	// / <summary>
	// / 获得有效的子节点
	// / </summary>
	public DataNode[] getValidRootNodes()
	{
		if (validRootNodes != null)
			return validRootNodes;

		ArrayList<DataNode> tmpal = new ArrayList<DataNode>();
		for (DataNode dn : rootDataNodes)
		{
			if (dn.isForbidden())
				continue;

			if (dn.isVisiable())
				tmpal.add(dn);
		}

		// tmpal.Sort() ;

		DataNode[] rets = new DataNode[tmpal.size()];
		validRootNodes = tmpal.toArray(rets);
		return validRootNodes;
	}

	ArrayList<DataNode> getAllExpandedNodes(boolean bshowforbid, boolean bshowvis)
	{
		ArrayList<DataNode> tmpal = new ArrayList<DataNode>();

		DataNode[] dns = getRootNodes();
		for (DataNode dn : dns)
		{
			if (!bshowforbid && dn.isForbidden())
				continue;

			if (!bshowvis && !dn.isVisiable())
				continue;

			tmpal.add(dn);

			getAllExpandedNode(tmpal, dn, bshowforbid, bshowvis);
		}

		return tmpal;
	}

	void getAllExpandedNode(ArrayList al, DataNode pdn, boolean bshowforbid,
			boolean bshowvis)
	{
		for (DataNode dn : pdn.getChildNodes())
		{
			if (!bshowforbid && dn.isForbidden())
				continue;

			if (!bshowvis && !dn.isVisiable())
				continue;

			al.add(dn);

			getAllExpandedNode(al, dn, bshowforbid, bshowvis);
		}
	}

	DataNode[] allExpandedNodes = null;

	// / <summary>
	// / 获取按照顺序展开的所有根节点和子孙节点
	// / </summary>
	public DataNode[] getAllExpandedNodes()
	{
		if (allExpandedNodes != null)
			return allExpandedNodes;

		synchronized(this)
		{
		ArrayList<DataNode> dns = getAllExpandedNodes(true, true);
		allExpandedNodes = new DataNode[dns.size()];
		return dns.toArray(allExpandedNodes);
		}
	}

	DataNode[] allValidExpandedNodes = null;

	// / <summary>
	// / 获取按照顺序展开的所有有效的根节点和子孙节点
	// / </summary>
	public DataNode[] getAllValidExpandedNodes()
	{
		if (allValidExpandedNodes != null)
			return allValidExpandedNodes;

		synchronized(this)
		{
		ArrayList<DataNode> dns = getAllExpandedNodes(false, false);
		allValidExpandedNodes = new DataNode[dns.size()];
		return dns.toArray(allValidExpandedNodes);
		}
	}
	
	ArrayList<DataNode> allValidLeafNodes = null;
	/**
	 * 得到所有的展开的叶子节点
	 * @return
	 */
	public ArrayList<DataNode> getAllValidExpandedLeafNodes()
	{
		if (allValidLeafNodes != null)
			return allValidLeafNodes;

		synchronized(this)
		{
		ArrayList<DataNode> dns = getAllExpandedNodes(false, false);
		allValidLeafNodes = new ArrayList<DataNode>() ;
		for(DataNode dn:dns)
		{
			if(dn.isLeaf())
				allValidLeafNodes.add(dn);
		}
		return allValidLeafNodes;
		}
	}
	
	
	////////////////////////////
	public void addDataNode(DataNode dn) throws Exception
	{
		if(dcIO==null)
			throw new Exception("no io found in class,please check dd file existed io=\"db\"") ;
		
		//check id and name existed
		DataNode dn0 = this.findDataNodeById(dn.getId());
		if(dn0!=null)
			throw new Exception("DD DataNode with node id="+dn.getId()+" is already existed in class="+this.name) ;
		
		dn0 = this.findDataNodeByName(dn.getName());
		
		if(dn0!=null)
			throw new Exception("DD DataNode with node name="+dn.getName()+" is already existed in class="+this.name) ;
		
		dcIO.addDataNode(moduleName,classId, dn) ;
		
		//reload
		reloadClass() ;
	}
	
	public void updateDataNode(int nodeid,DataNode dn) throws Exception
	{
		if(dcIO==null)
			throw new Exception("no io found in class,please check dd file existed io=\"db\"") ;
		
//		check id and name existed
		//DataNode dn0 = this.findDataNodeById(dn.getId());
		//if(dn0!=null&&dn)
		//	throw new Exception("DD DataNode with node id="+dn.getId()+" is already existed in class="+this.name) ;
		DataNode olddn = this.findDataNodeById(nodeid) ;
		if(olddn==null)
			throw new Exception("cannot find old dn with id="+nodeid);
		
		dn.id = nodeid ;
		
		dn.belongToClassId = this.classId ;
		dn.belongToModule = this.moduleName ;
		
		long autoid = olddn.getAutoId() ;
		if(autoid<=0)
			throw new Exception("cannot get node io auto id!");
		
		DataNode dn0 = this.findDataNodeByName(dn.getName());
		
		if(dn0!=null&&dn0.getAutoId()!=autoid)
			throw new Exception("DD DataNode with node name="+dn.getName()+" is already existed in class="+this.name) ;
		
		
		dcIO.updateDataNode(moduleName,autoid,dn) ;
		
//		reload
		reloadClass() ;
	}
	
	
	public void changeDataNodeParent(int nodeid,int pnodeid) throws Exception
	{
		if(dcIO==null)
			throw new Exception("no io found in class,please check dd file existed io=\"db\"") ;
		
//		check id and name existed
		//DataNode dn0 = this.findDataNodeById(dn.getId());
		//if(dn0!=null&&dn)
		//	throw new Exception("DD DataNode with node id="+dn.getId()+" is already existed in class="+this.name) ;
		DataNode dn = this.findDataNodeById(nodeid) ;
		if(dn==null)
			throw new Exception("cannot find old dn with id="+nodeid);
		
		if(pnodeid>0)
		{
			DataNode pdn = this.findDataNodeById(pnodeid) ;
			if(pdn==null)
				throw new Exception("cannot find dn with id="+pnodeid);
			
			if(pdn.checkIsAncestor(dn))
			{
				throw new Exception("cannot set offspring node to be parent!") ;
			}
		}
		dn.id = nodeid ;
		
		dn.belongToClassId = this.classId ;
		dn.belongToModule = this.moduleName ;
		
		long autoid = dn.getAutoId() ;
		if(autoid<=0)
			throw new Exception("cannot get node io auto id!");
		
		dn.parentNodeId = pnodeid ;
		
		
		dcIO.updateDataNode(moduleName,autoid,dn) ;
		
//		reload
		reloadClass() ;
	}
	
	public void delDataNode(int nodeid) throws Exception
	{
		if(dcIO==null)
			throw new Exception("no io found in class,please check dd file existed io=\"db\"") ;
		
//		DataNode dn = this.findDataNodeById(nodeid) ;
//		if(dn==null)
//			return ;
		
		DataNode dn = this.findDataNodeById(nodeid) ;
		if(dn==null)
			return ;
		
		if(dn.hasChild())
			throw new Exception("Data Dict Node which has children cannot be deleted!");
		
		long autoid = dn.autoId ;
		if(autoid<=0)
			throw new Exception("cannot get node io auto id!");
//		
		
		dcIO.delDataNode(dn.autoId);
		
		reloadClass() ;
	}
	
	public void setDefaultDataNode(int nodeid) throws Exception
	{
		if(dcIO==null)
			throw new Exception("no io found in class,please check dd file existed io=\"db\"") ;
		
//		DataNode dn = this.findDataNodeById(nodeid) ;
//		if(dn==null)
//			return ;
		
		DataNode dn = this.findDataNodeById(nodeid) ;
		if(dn==null)
			return ;
		
		dcIO.setDefaultDataNode(moduleName, classId, nodeid);
		
		reloadClass() ;
	}
	
	////////////////////////////

	public void writeTo(Writer w) throws IOException
	{
		w.write("Class id="+classId);
		w.write(" name=");
		w.write(name);
		w.write(" name_cn=");
		w.write(classNameCn);
		w.write(" name_en=");
		w.write(classNameEn);
		w.write("\r\n");
		
		w.flush();
		DataNode[] dns = this.getValidRootNodes() ;
		if(dns!=null)
		{
			for(DataNode dn:dns)
			{
				dn.writeTo(w);
			}
		}
	}
	
	// / <summary>
	// / 为了兼容原来的输出Xml文件，该方法提供老my使用的xml字典格式
	// / </summary>
	// / <returns></returns>
	public Document toXmlDoc()
	{

		Document xd = null;// new Document() ;
		// Element root = xd.CreateElement("CODES") ;
		// xd.AppendChild(root) ;
		//
		// root.SetAttribute("ClassID",""+this.ClassId) ;
		// root.SetAttribute("ClassNameCn",this.ClassNameCn) ;
		// root.SetAttribute("ClassNameEn",this.ClassNameEn) ;
		//
		// for(DataNode dn : this.AllExpandedNodes)
		// {
		// XmlElement tmpe = xd.CreateElement("CODE") ;
		//
		// tmpe.SetAttribute("CodeID",""+dn.Id) ;
		// tmpe.SetAttribute("CodeNameCn",dn.NameCN) ;
		// tmpe.SetAttribute("CodeNameEn",dn.NameEn) ;
		// tmpe.SetAttribute("SearchIDs",dn.GetSearchIdsStr()) ;
		// tmpe.SetAttribute("IsVisible",(dn.IsVisiable?"1":"0")) ;
		// tmpe.SetAttribute("IsForbidden",(dn.IsForbidden?"1":"0")) ;
		// tmpe.SetAttribute("OrderNO",""+dn.OrderNo) ;
		// tmpe.SetAttribute("Lvl",""+dn.Level) ;
		// String pid = "0" ;
		// if(dn.ParentNode!=null)
		// pid = ""+dn.ParentNode.Id ;
		// tmpe.SetAttribute("ParentID",pid) ;
		// tmpe.SetAttribute("RootID","0") ;
		// tmpe.SetAttribute("CreateDate",this.CreateTime.ToString("yyyy-MM-dd
		// HH:mm:ss")) ;
		// tmpe.SetAttribute("LastUpdateDate",this.LastUpdateTime.ToString("yyyy-MM-dd
		// HH:mm:ss")) ;
		//
		// root.AppendChild(tmpe) ;
		// }

		return xd;
	}
}
