package com.dw.web_ui;

import java.io.*;
import java.util.*;

/**
 * 用来支持Web树型目录
 *
 * @author Jason Zhu
 */
public class WebTreeNode
{
	boolean bSep = false;

	//private ArrayList<String> paths = null ;
	
	private String id_name = null ;
	private String title = null ;
	private String url = null ;
	private String icon = null ;
	private String target = null ;
	String tips = null ;
	private int x = -1 ;
	private int y = -1 ;
	
	private ArrayList<WebTreeNode> childs = new ArrayList<WebTreeNode> () ;
	
	private int extType = -1 ;
	private String extInfo = null ;
	
	private HashSet<String> relatedLink = new HashSet<String>() ;
	
	transient WebTreeNode parentTN = null ;
	
	public WebTreeNode(boolean bsep)
	{
		bSep = bsep ;
	}
	
	public WebTreeNode(String id_n,String title,String url)
	{
		this(id_n,title,url,(String[])null);
	}
	
	public WebTreeNode(String id_n,String title,String url,String[] relatedurl)
	{
		this.id_name = id_n ;
		this.title = title ;
		this.url = url ;
		
		relatedLink.add(url) ;
		
		if(relatedurl!=null)
		{
			for(String rul:relatedurl)
				relatedLink.add(rul) ;
		}
	}
	
	public WebTreeNode(String id_n,String title,String url,String icon)
	{
		this.id_name = id_n ;
		this.title = title ;
		this.url = url ;
		this.icon = icon ;
	}
	
	public WebTreeNode(String id_n,String title,String url,String icon,String tips)
	{
		this(id_n,title,url,icon);
		this.tips = tips ;
	}
	
	public WebTreeNode(String id_n,String title,String url,String target,String icon,String tips)
	{
		this(id_n,title,url,icon,tips);
		
		this.target = target ;
	}
	
	public WebTreeNode(String id_n,String title,String url,String icon,String tips,List<WebTreeNode> subtns)
	{
		this(id_n,title,url,icon,tips);
		childs.addAll(subtns);
	}
	
	public boolean isSeperator()
	{
		return bSep ;
	}
	
	public String getIdName()
	{
		return id_name ;
	}
	
	public int getLevel()
	{
		if(parentTN==null)
			return 0 ;
		
		return parentTN.getLevel()+1;
	}
	
	public String getTitle()
	{
		return title ;
	}
	
	public void setTitle(String t)
	{
		title = t ;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public void setUrl(String u)
	{
		url = u ;
	}
	
	public int getX()
	{
		return x ;
	}
	
	public int getY()
	{
		return y ;
	}
	
	public WebTreeNode setXY(int x,int y)
	{
		this.x = x ;
		this.y = y ;
		return this ;
	}
	
	public String getIcon()
	{
		return icon ;
	}
	
	public WebTreeNode setIcon(String icon)
	{
		this.icon = icon ;
		return this ;
	}
	
	public String getTarget()
	{
		return target;
	}
	
	public void setTarget(String t)
	{
		target = t ;
	}
	
	public String getTips()
	{
		return tips ;
	}
	
	public void setTips(String t)
	{
		tips = t ;
	}
	
	public String getExtInfo()
	{
		return extInfo;
	}
	
	public int getExtType()
	{
		return extType ;
	}
	
	/**
	 * 
	 * @param rl
	 * @return
	 */
	public boolean checkRelatedLink(String rl)
	{
		return relatedLink.contains(rl) ;
	}
	
	
	public void setExtInfo(int ext_type,String ext_info)
	{
		extType = ext_type ;
		extInfo = ext_info ;
	}
	
	public WebTreeNode getParent()
	{
		return parentTN;
	}
	
	public WebTreeNode appendChild(WebTreeNode wtn)
	{
		wtn.removeFromParent() ;
		
		wtn.parentTN = this ;
		childs.add(wtn);
		
		return this;
	}
	
	public WebTreeNode getFirstChild()
	{
		if(childs==null||childs.size()<=0)
			return null ;
		
		return childs.get(0);
	}
	
	public WebTreeNode getLastChild()
	{
		if(childs==null||childs.size()<=0)
			return null ;
		
		return childs.get(childs.size()-1);
	}
	
	public void removeFromParent()
	{
		if(parentTN==null)
			return ;
		
		parentTN.childs.remove(this);
		parentTN = null ;
	}
	
	public ArrayList<WebTreeNode> getChildNodes()
	{
		return childs ;
	}
	
	public boolean hasChild()
	{
		return childs.size()>0;
	}
	
	public WebTreeNode getChildNodeById(String id)
	{
		if(id==null)
			return null ;
		
		for(WebTreeNode wtn:childs)
		{
			if(wtn.getIdName().equals(id))
				return wtn ;
		}
		return null ;
	}
	
	public int getIdxInParent()
	{
		if(parentTN==null)
			return -1 ;
		
		return parentTN.childs.indexOf(this);
	}
	
	public boolean isLastInParent()
	{
		if(parentTN==null)
			return true ;
		
		return parentTN.getLastChild()==this ;
	}
	
	/**
	 * 获得从根开始到本节点的父节点所经过的所有节点
	 * @return
	 */
	public ArrayList<WebTreeNode> getAncestors()
	{
		if(parentTN==null)
			return null ;
		
		return parentTN.getNodePath() ;
	}
	
	/**
	 * 获得从根开始到本节点所经过的所有节点
	 * @return
	 */
	public ArrayList<WebTreeNode> getNodePath()
	{
		ArrayList<WebTreeNode> wtns = new ArrayList<WebTreeNode>() ;
		getNodePaths(wtns);
		return wtns ;
	}
	private void getNodePaths(ArrayList<WebTreeNode> ans)
	{
		if(parentTN!=null)
			parentTN.getNodePaths(ans);
		
		ans.add(this);
	}
	
	public ArrayList<String> getPathIdNames()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		getPathIdNames(rets);
		return rets ;
	}
	
	private void getPathIdNames(ArrayList<String> pins)
	{
		if(parentTN!=null)
			parentTN.getPathIdNames(pins);
		
		pins.add(this.id_name);
	}
}
