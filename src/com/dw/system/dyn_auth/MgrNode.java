package com.dw.system.dyn_auth;

import java.util.ArrayList;
import java.util.*;

import com.dw.user.right.RightRule;

public class MgrNode
{
	String name = null;

	String title = null;

	String url = null;

	List<String> related_url = new ArrayList<String>();

	
	
	String pNodeName=null;
	
	
	String target= null ;
	
	RightRule right=null;
	
	String titleSize=null;
	
	String logoImage=null;
	
	HashMap<String,String> extAttrs = new HashMap<String,String>() ;
	
	transient MgrNode parentMN = null ;
	
	transient List<MgrNode> subMgrNodes = new ArrayList<MgrNode>();
	
	public MgrNode copyMe()
	{
		MgrNode r = new MgrNode() ;
		
		r.name = this.name ;
		r.title = this.title;

		r.url = this.url;

		r.related_url = this.related_url;

		
		
		r.pNodeName=this.pNodeName;
		
		r.target = this.target ;
		
		r.right=this.right;
		
		r.titleSize=this.titleSize;
		
		r.logoImage=this.logoImage;
		
		r.extAttrs=this.extAttrs;
		
		return r ;
	}
	
	public String getName()
	{
		return name;
	}

	public List<MgrNode> getSubMgrNodes()
	{
		return subMgrNodes;
	}

	public void setSubMgrNodes(List<MgrNode> subMgrNodes)
	{
		this.subMgrNodes = subMgrNodes;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getTarget()
	{
		return this.target ;
	}
	/**
	 * 不符合规范的方法
	 * @return
	 */
	public List<String> getRelated_url()
	{
		return related_url;
	}
	
	public List<String> getRelatedUrls()
	{
		return related_url ;
	}

	public void setRelated_url(List<String> related_url)
	{
		this.related_url = related_url;
	}

	public RightRule getRight()
	{
		return right;
	}

	public String getPNodeName()
	{
		return pNodeName;
	}

	public void setPNodeName(String nodeName)
	{
		pNodeName = nodeName;
	}

	public String getLogoImage()
	{
		return logoImage;
	}

	public void setLogoImage(String logoImage)
	{
		this.logoImage = logoImage;
	}

	public String getTitleSize()
	{
		return titleSize;
	}

	public void setTitleSize(String titleSize)
	{
		this.titleSize = titleSize;
	}


	public boolean isLeaf()
	{
		return this.subMgrNodes==null || subMgrNodes.size()<=0 ;
	}
	
	public MgrNode getParentNode()
	{
		return parentMN;
	}
	
	public String getFirstLeafUrl()
	  {
		return getFirstLeafUrl(this) ;
	  }
	
	
	String getFirstLeafUrl(MgrNode mn)
	  {
			if(mn.isLeaf())
				return mn.getUrl() ;
		  
			return getFirstLeafUrl(mn.subMgrNodes.get(0));
		  
	  }
	
	
	public RightRule getUsingRightRule()
	{
		if(this.right!=null)
			return right;
		
		if(this.parentMN==null)
			return null ;
		
		return parentMN.getUsingRightRule();
	}

}
