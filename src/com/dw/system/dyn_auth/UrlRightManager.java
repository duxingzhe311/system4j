package com.dw.system.dyn_auth;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import com.dw.user.right.RightRule;

/**
 * 
 * @author Jason Zhu
 *
 */
public class UrlRightManager
{
	static Object locker = new Object() ;
	static UrlRightManager ins = null ;
	
	public static UrlRightManager getInstance()
	throws Exception
	{
		if(ins!=null)
			return ins ;
		
		synchronized(locker)
		{
			if(ins!=null)
				return ins ;
			
			ins = new UrlRightManager() ;
			return ins ;
		}
	}
	/**
	 * 
	 */
	HashMap<String,List<UrlRightItem>> path2rightitems = new HashMap<String,List<UrlRightItem>>() ;
	
	
	private UrlRightManager() throws Exception
	{
		loadConf();
	}
	/**
	 * 根据配置文件的装载内容，生成内存快速查找映像
	 * 以方面系统运行中的权限判断
	 * @throws Exception
	 */
	void loadConf() throws Exception
	{
		HashMap<String,List<UrlRightItem>> p2r = new HashMap<String,List<UrlRightItem>>() ;
		
		List<ServerMgr2Node> sm2ns = ServerMgr.getInstance().getServerMgr2Nodes() ;
		for(ServerMgr2Node sm2n:sm2ns)
		{
			List<MgrNode> mns = sm2n.getLeafNodes() ;
			if(mns==null)
				continue ;
			
			for(MgrNode mn:mns)
			{
				loadNode(p2r,sm2n.getModuleName(),mn) ;
			}
		}
		
		path2rightitems = p2r ;
	}
	
	private void loadNode(HashMap<String,List<UrlRightItem>> p2r,String modulen,MgrNode mn)
	{
		String u = mn.getUrl() ;
		String[] pnv = parseUrlToPathAndParam(u) ;
		
		List<UrlRightItem> uris = p2r.get(pnv[0]) ;
		if(uris==null)
		{
			uris = new ArrayList<UrlRightItem>() ;
			p2r.put(pnv[0],uris) ;
		}
		
		UrlRightItem uri = new UrlRightItem(modulen,pnv[0],
				pnv[1],pnv[2],mn.getUsingRightRule()) ;
		uris.add(uri) ;
		
		//setup related url
		List<String> rus = mn.getRelatedUrls() ;
		if(rus==null)
			return ;
		
		for(String ru:rus)
		{
			pnv = parseUrlToPathAndParam(ru) ;
			uris = p2r.get(pnv[0]) ;
			if(uris==null)
			{
				uris = new ArrayList<UrlRightItem>() ;
				p2r.put(pnv[0],uris) ;
			}
			
			uri = new UrlRightItem(modulen,pnv[0],
					pnv[1],pnv[2],mn.getUsingRightRule()) ;
			uris.add(uri) ;
		}
	}
	
	/**
	 * 解析url里面的jsp路径，及参数，参数最多只允许有一个，一旦设置了此参数
	 * 也会被用来做权限处理
	 * @param url
	 */
	private String[] parseUrlToPathAndParam(String url)
	{
		int p = url.indexOf('?') ;
		if(p<0)
		{
			return new String[]{url,null,null} ;
		}
		
		String path = url.substring(0,p) ;
		String pstr = url.substring(p+1) ;
		
		StringTokenizer st = new StringTokenizer(pstr,"&") ;
		int c  = st.countTokens() ;
		if(c<=0)
		{
			return new String[]{path,null,null};
		}
		
		String firstpv = st.nextToken() ;
		p = firstpv.indexOf('=');
		if(p<=0)
		{
			return new String[]{path,null,null};
		}
		
		String pn = firstpv.substring(0,p) ;
		String pv = firstpv.substring(p+1) ;
		return new String[]{path,pn,pv} ;
	}
	
	/**
	 * 根据http请求，获得对应的权限设定项
	 * @param cxt_path 如/system  /ctc
	 * @param servletpath webapp路径模块内部路径，如 /ttt/a.jsp
	 * @param req http请求，之所以用这个对象作为参数，是需要其内部的提交参数
	 * @return
	 */
	public UrlRightItem getRightItem(String modulen,String servletpath,HttpServletRequest req)
	{
		String p = modulen+servletpath;
		List<UrlRightItem> items = path2rightitems.get(p);
		if(items==null||items.size()==0)
			return null ;
		
		if(items.size()==1)
			return items.get(0) ;
		
		UrlRightItem def_uri = null ;
		
		for(UrlRightItem uri:items)
		{
			if(uri.matchPN==null)
			{
					def_uri = uri ;
					continue ;
			}
			
			String v = req.getParameter(uri.matchPN) ;
			if(v==null)
			{
				if(uri.matchPV==null||uri.matchPV.equals(""))
					return uri ;
			}
			else if(v.equals(uri.matchPV))
			{
				return uri ;
			}
		}

		return def_uri ;
	}
}
