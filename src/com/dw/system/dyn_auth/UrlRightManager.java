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
	 * ���������ļ���װ�����ݣ������ڴ���ٲ���ӳ��
	 * �Է���ϵͳ�����е�Ȩ���ж�
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
	 * ����url�����jsp·�������������������ֻ������һ����һ�������˴˲���
	 * Ҳ�ᱻ������Ȩ�޴���
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
	 * ����http���󣬻�ö�Ӧ��Ȩ���趨��
	 * @param cxt_path ��/system  /ctc
	 * @param servletpath webapp·��ģ���ڲ�·������ /ttt/a.jsp
	 * @param req http����֮���������������Ϊ����������Ҫ���ڲ����ύ����
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
