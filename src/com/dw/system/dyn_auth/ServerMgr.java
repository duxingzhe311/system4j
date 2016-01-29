package com.dw.system.dyn_auth;

import java.util.*;

import org.w3c.dom.*;
import com.dw.system.*;
import com.dw.system.gdb.GdbException;
import com.dw.user.UserProfile;
import com.dw.user.right.RightRule;

public class ServerMgr
{
	

	private static Object locker = new Object() ; 
	private static ServerMgr ccMgr = null ;
	
	public static ServerMgr getInstance()
	{
		if(ccMgr!=null)
			return ccMgr ;
		
		synchronized(locker)
		{
			if(ccMgr!=null)
				return ccMgr ;
			
			ccMgr = new ServerMgr() ;
			return ccMgr ;
		}
	}
	
	
	List<ServerMgr2Node> serverMgr2Nodes=null;
	
	
	public List<ServerMgr2Node> getServerMgr2Nodes() throws GdbException, Exception
	{
		if(serverMgr2Nodes!=null)
			return serverMgr2Nodes;
		
		serverMgr2Nodes=new ArrayList<ServerMgr2Node>();
		for(AppWebConfig awc:AppWebConfig.getModuleWebConfigAll())
		{
			Element servermgr2ele = awc.getConfElement("server_mgr2");
			if(servermgr2ele==null)
				continue ;
			ServerMgr2Node smn=getServerMgr2Node(awc.getModuleName(),servermgr2ele);
			serverMgr2Nodes.add(smn);
		}
		
//		reload right path map
		UrlRightManager.getInstance().loadConf();
		
		return serverMgr2Nodes;
	}
	
	public void reload() throws GdbException, Exception
	{
		serverMgr2Nodes=null;
		getServerMgr2Nodes();
	}

	
//     void setServerMgr2Nodes(List<ServerMgr2Node> serverMgr2Nodes)
//	{
//		this.serverMgr2Nodes = serverMgr2Nodes;
//	}
	
	ServerMgr2Node getServerMgr2Node(String modulen,Element serverMgr2ele) throws GdbException, Exception
	{
		ServerMgr2Node smn=new ServerMgr2Node();
		if(serverMgr2ele==null)
			return smn;
		String id=UUID.randomUUID().toString();
		//String name=serverMgr2ele.getAttribute("name");
		String desc=serverMgr2ele.getAttribute("desc");
		smn.setId(id);
		smn.setDesc(desc);
		//smn.setName(name);
		Element[] eles = Convert.getSubChildElement(serverMgr2ele,"mgr_node") ;
		if(eles!=null)
		{
			List<MgrNode> mns=new ArrayList<MgrNode>();
			for(Element mne:eles)
			{
				mns.add(getMgrNode(null,modulen,mne));
			}
			smn.setSubMgrNodes(mns);
		}
		return smn;
	}
	
	 MgrNode getMgrNode(MgrNode pmn,String modulen,Element mgrele) throws GdbException, Exception
	{
		MgrNode mn=new MgrNode();
		if(mgrele==null)
			return mn;
		
		String name = mgrele.getAttribute("name") ;
		String title = mgrele.getAttribute("title") ;
		String url = mgrele.getAttribute("url") ;
		String targ = mgrele.getAttribute("target") ;
		//¼ÆËã¾ø¶ÔÂ·¾¶
		if(Convert.isNotNullEmpty(url)&&!url.startsWith("/")&&!url.startsWith("http://"))
		{
			url = Convert.calAbsPath("/"+modulen, url);
		}
		
		//String id=pid+"."+mgrele.getAttribute("name");
		List<String> related_url=new ArrayList<String>();
		Element[] rus = Convert.getSubChildElement(mgrele,"related_url") ;
		if(rus!=null)
		{
			for(Element ru:rus)
			{
				String tmpu = ru.getAttribute("url");
				if(Convert.isNotNullEmpty(url)&&!tmpu.startsWith("/")&&!tmpu.startsWith("http://"))
					tmpu = Convert.calAbsPath("/"+modulen, tmpu);
				
				related_url.add(tmpu);
			}
		}
		
		
		String titleSize=mgrele.getAttribute("title_size") ;
		String logoImage=mgrele.getAttribute("logo_image") ;
		//mn.setId(id);
		mn.name = name;
		mn.setTitle(title);
		mn.setUrl(url);
		mn.setRelated_url(related_url);
		mn.setTitleSize(titleSize);
		mn.setLogoImage(logoImage);
		mn.target = targ ;
		
		mn.parentMN = pmn ;
		
		NamedNodeMap mnm = mgrele.getAttributes();
		if(mnm!=null)
		{
			int len = mnm.getLength();
			for(int k = 0 ; k < len ; k ++)
			{
				Node n = mnm.item(k) ;
				mn.extAttrs.put(n.getNodeName(), n.getNodeValue()) ;
			}
		}
		
		MgrNodeRightRoleItem mnrr=MgrNodeRightRoleManager.getInstance().getMgrNodeRightRoleByMgrNodeId(mn.getName());
		RightRule r=null;
		if(mnrr!=null&&mnrr.getRightRuleStr()!=null)
		{
			r=RightRule.parse(mnrr.getRightRuleStr());
		}
		
		mn.right = r;
		
		Element[] eles = Convert.getSubChildElement(mgrele,"mgr_node") ;
		if(eles!=null&&eles.length>0)
		{
			List<MgrNode> mns=new ArrayList<MgrNode>();
			for(Element e:eles)
			{
				MgrNode mgn= getMgrNode(mn,modulen,e);
				mgn.setPNodeName(mn.getName());
				
				MgrNodeRightRoleItem mnrri=MgrNodeRightRoleManager.getInstance().getMgrNodeRightRoleByMgrNodeId(mgn.getName());
				RightRule rr=null;
				if(mnrri!=null&&mnrri.getRightRuleStr()!=null)
				{
					rr=RightRule.parse(mnrri.getRightRuleStr());
				}
				
				mgn.right = rr;
				
				mns.add(mgn);
			}
			mn.setSubMgrNodes(mns);
		}
		

		return mn;
	}
	 
	 
	 /**
	  * 
	  * @param name
	  * @return
	 * @throws Exception 
	 * @throws GdbException 
	  */
	 public MgrNode gerMgrNodeByName(String name) throws GdbException, Exception
	 {
		 MgrNode mn=null;
		 
		 List<ServerMgr2Node> sss= getServerMgr2Nodes();
		 
		 for(ServerMgr2Node smne:sss)
		 {
			 mn = smne.findNodeByName(name) ;
			 if(mn!=null)
				 return mn ;
		 }
		 return mn;
	 }
//	 
//	 MgrNode getMgrNodeByNameFromMgrNode(String name,MgrNode mne)
//	 { 
//		 MgrNode mn=null;
//		 if(name==null)
//			 return null;
//		 if(name.equals(mne.getName()))
//		    mn=mne;
//		 else 
//	     {
//			 if(mne.getSubMgrNodes().size()>0)
//		   {
//			 for(MgrNode m:mne.getSubMgrNodes())
//			 {
//				 mn=getMgrNodeByNameFromMgrNode(name,m);
//				 if(mn!=null&&name.equals(mn.getName()))
//					 break;
//			 }
//		   }
//	     }
//		 return mn;
//	 }
	  
	  public List<MgrNode> getMgrNodesByRightRule(UserProfile up) throws GdbException, Exception
	  {
		  List<MgrNode> mns=new ArrayList<MgrNode>() ;
		  
		  for(ServerMgr2Node smn:getServerMgr2Nodes())
			{
				List<MgrNode> mnss=smn.getSubMgrNodes();
				for(MgrNode mno:mnss)
				{
					MgrNode mnde=getMgrNodeByRightRuleFromMgrNode(null,up,mno);
					if(mnde!=null)
					{
						mns.add(mnde);
					}
				}
			}
		  
		  return mns;
	  }
	 
	  
	  MgrNode getMgrNodeByRightRuleFromMgrNode(MgrNode pmn,UserProfile up,MgrNode mn) throws Exception
	  {
		  int subc = mn.subMgrNodes.size() ;
		  boolean has_r = false;
		  if(subc<=0)
		  {
			  RightRule rr = mn.getUsingRightRule() ;
			  if(rr==null)
				  return null ;
			  
			  if(!rr.CheckUserRight(up))
			  {
				  return null ;
			  }
			  else
			  {
				  has_r = true ;
			  }
		  }
		  
		  MgrNode tmpmn =  mn.copyMe() ;
		  tmpmn.parentMN = pmn ;
		  
		  ArrayList<MgrNode> cmns = new ArrayList<MgrNode>() ;
		  for(MgrNode cmn:mn.subMgrNodes)
		  {
			  MgrNode tmpmn0 = getMgrNodeByRightRuleFromMgrNode(tmpmn,up,cmn) ;
			  if(tmpmn0==null)
			  {
				  continue ;
			  }
			  
			  cmns.add(tmpmn0) ;
		  }
		  
		  if(cmns.size()<=0&&!has_r)
			  return null ;
		  
		  tmpmn.subMgrNodes = cmns ;
		  
		  return tmpmn ;
	  }
	  
	  MgrNode getMgrNodeByRightRuleFromMgrNode0(UserProfile up,MgrNode mn) throws Exception
	  {
		  	MgrNode m=null;
		  	if(mn==null)
		  	{
		  		m=null;
		  		return m;
		  	}
		  
		  	RightRule rr_mn=mn.getUsingRightRule();
		  	if(rr_mn!=null&&rr_mn.CheckUserRight(up))
		  	{
		  		m=mn;
	  
		  	}
		  	else
		  	{
		  		m=null;
		  	}
		  	
		  if(mn.getSubMgrNodes().size()>0)
		  {
			  RightRule rr2_mn=mn.getUsingRightRule();
			  if(rr2_mn!=null&&rr2_mn.CheckUserRight(up))
			  {
				  m=mn;
				  return m;
			  }
			  else
			  {
				 MgrNode mn2=null;
			     for(MgrNode mne:mn.getSubMgrNodes())
			     {
				    if(getMgrNodeByRightRuleFromMgrNode0(up,mne)!=null)
				    {
				    	mn2=getMgrNodeByRightRuleFromMgrNode0(up,mne);
				    	break;
				    }
			     }
			     if(mn2!=null)
			     {
			    	  m=new MgrNode();
			    	  List<MgrNode> mns=new ArrayList<MgrNode>();
			    	  m.name = mn.getName();
					  m.setPNodeName(mn.getPNodeName());
					  m.setRelated_url(mn.getRelated_url());
					  m.right = mn.getUsingRightRule();
					  m.setTitle(mn.getTitle());
					  m.setUrl(mn.getUrl());
					  m.setTitleSize(mn.getTitleSize());
					  m.setLogoImage(mn.getLogoImage());
					  for(MgrNode mne:mn.getSubMgrNodes())
					  {
						  MgrNode mn3=getMgrNodeByRightRuleFromMgrNode0(up,mne);
						  if(mn3!=null)
						  {
							  mns.add(mn3);
						  }
					  }
					  m.setSubMgrNodes(mns);
			     }
			     else
			     {
			    	 m=null;
			     }
			  }
		  }
		  return m;
	  }
	  
	  
	
	  public boolean isNameExistAtMgrNode(String name,MgrNode mn)
	  {
		  if(mn==null||name==null)
			  return false;
		  if(name.equals(mn.getName()))
			  return true;
		  if(mn.getSubMgrNodes().size()>0)
		  {
			  for(MgrNode mne:mn.getSubMgrNodes())
			  {
				  if(isNameExistAtMgrNode(name,mne))
					  return true;
			  }
		  }
			  return false;
	  }
	  
	  
	  public String getLeafUrl(MgrNode mn)
	  {
		  return mn.getFirstLeafUrl();
	  }
	  
	  
	  
	  /*
	   MgrNode getMgrNodeByRightRuleFromMgrNode(UserProfile up,MgrNode mn) throws Exception
	  {
		  MgrNode m=null;
		  if(mn==null)
		  {
			m=null;  
		  }		  

		  if(mn.getRight()!=null&&mn.getRight().CheckUserRight(up))
		  {
			m=mn; 
			return m;
		  }
		  
		  if(mn.getSubMgrNodes().size()<1)
		  {
			  m=null;
			  return m;
		  }
		  else
		  {
			  for(MgrNode mne:mn.getSubMgrNodes())
			  {
				  if(mne.getRight()!=null&&mne.getRight().CheckUserRight(up))
				  {
					  m=new MgrNode();
					  m.setName(mn.getName());
					  m.setPNodeName(mn.getPNodeName());
					  m.setRelated_url(mn.getRelated_url());
					  m.setRight(mn.getRight());
					  m.setTitle(mn.getTitle());
					  m.setUrl(mn.getUrl());
					  break;
				  }
			  }
			  
			  if(m!=null)
			  {
				  List<MgrNode> subMgrNodes = new ArrayList<MgrNode>();
				  for(MgrNode mnee:mn.getSubMgrNodes())
				  {
					  MgrNode mneee=getMgrNodeByRightRuleFromMgrNode(up,mnee);
					  if(mneee!=null)
						  subMgrNodes.add(mneee);
				  }
				  m.setSubMgrNodes(subMgrNodes);
				  return m;
			  }
			  
		  }
		  return m;
	  }
	   */
	  
}
