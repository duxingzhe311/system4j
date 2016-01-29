package com.dw.system;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.dw.system.dyn_auth.UrlRightItem;
import com.dw.system.dyn_auth.UrlRightManager;
import com.dw.system.xmldata.XmlHelper;
import com.dw.user.Role;
import com.dw.user.UserProfile;
import com.dw.user.access.AccessManager;
import com.dw.user.right.RightRule;

/**
 * 放在webapp页面目录中,用来定义里面控制配置信息的对象
 * 
 * 如:security等
 * @author Jason Zhu
 */
public class AppWebConfig
{
	static ArrayList<IAppWebConfigLoadedListener> configLoadLis = new ArrayList<IAppWebConfigLoadedListener>() ;
	
	public static void registerConfigLoadLis(IAppWebConfigLoadedListener l)
	{
		configLoadLis.add(l) ;
	}
	
	public static final String TAG_AUTH = "authorization";
	public static final String TAG_DEFAULT = "default";
	public static final String TAG_LOC = "location";
	
	public static final String ATTRN_TYPE = "type";
	public static final String ATTRN_IS_LOGIN = "is_login";
	public static final String ATTRN_SUPPORT_PORTAL = "support_portal";
	public static final String ATTRN_PATH = "path";
	public static final String ATTRN_USRES = "users";
	public static final String ATTRN_ROLES = "roles";
	public static final String ATTRN_INNER_ACCESS_ONLY = "inner_access_only";
	
	
	public static final String ATTRV_ALLOW = "allow";
	public static final String ATTRV_DENY = "deny";
	
	private static HashMap<String,AppWebConfig> unique2webconf = new HashMap<String,AppWebConfig>() ;
	
	private static AppWebConfig getAppWebConfigByPath(String cxt_dir,String servletdir)
	{
		String ud = cxt_dir + servletdir ;
		
		AppWebConfig wc = unique2webconf.get(ud);
		if(wc==null)
		{
			//File f = new File(sc.getRealPath(servletdir+"web.conf"));
			if(AppConfig.isSole())
			{
				File webf = new File(AppConfig.getSoleWebAppRoot()+servletdir);
				File f = new File(AppConfig.getSoleWebAppRoot()+servletdir+"web.xml");
				if(!f.exists())
					f = new File(AppConfig.getSoleWebAppRoot()+servletdir+"web.conf");
				wc = new AppWebConfig(webf,"",f);
			}
			else
			{
				File webf = new File(AppConfig.getTomatoWebappBase()+ud);
				File f = new File(AppConfig.getTomatoWebappBase()+ud+"web.xml");
				if(!f.exists())
					f = new File(AppConfig.getTomatoWebappBase()+ud+"web.conf");
				wc = new AppWebConfig(webf,"",f);
			}
			unique2webconf.put(ud, wc);
		}
		return wc ;
	}
	
	public static int checkRequestAllow(UserProfile up,boolean bportal_req,ServletContext sc,HttpServletRequest req)
		throws Exception
	{
		//String cxtname = sc.getServletContextName() ;
		String sp = req.getServletPath();
		int p = sp.lastIndexOf('/');
		String cxt_dir = req.getContextPath() ;
		if(cxt_dir==null||"".equals(cxt_dir))
			cxt_dir = "/ROOT" ;
		String servletdir = sp.substring(0,p+1);
		String req_file = sp.substring(p+1);
		

		//查看是否有动态权限控制
		UrlRightItem urit = UrlRightManager.getInstance().getRightItem(cxt_dir, sp, req);
		if(urit!=null)
		{
			if(AccessManager.getInstance().isOutterAccessNever())
			{
				if(!AccessManager.getInstance().isInnerRequest(req))
					return 0;
			}
			
			if(up!=null&&up.isAdministrator())
				return 1 ;
			
			RightRule rr = urit.getRightRule() ;
			if(rr==null)
				return 1 ;
			
			if(rr.CheckUserRight(up))
				return 1 ;
			else
				return 0 ;
		}
		
		//静态权限判定
		Auth a = null;//getDynAuthConf(cxt_dir,servletdir);
		if(a==null)
		{
			AppWebConfig wc = getAppWebConfigByPath(cxt_dir,servletdir);
			a = wc.getAuth() ;
		}
		
		if(a==null)
		{
//			寻找缺省的配置是否需要登陆
			if(!AppConfig.isAuthDefaultLogin())
				return 1;
			
			//if(bportal_req)
			//{//如果一个目录没有任何权限定制，并且必须登录，则不允许portal请求
			//	return -1;
			//}
			
			//UserProfile up = UserProfile.getUserProfile(req);
			if(up!=null)
				return 1 ;
			else
				return 0 ;
		}
		
		//UserProfile up = UserProfile.getUserProfile(req);
		return a.checkAllow(req,bportal_req,up, req_file);
	}
	
	/**
	 * 根据View或Action绝对路径，判断是否有权限,该权限由web.xml文件控制
	 *  比如：在portal页面中，就可以在调用view之前，调用此方法判断权限
	 * @param bportal_req
	 * @param viewpath
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public static int checkViewAllow(boolean bportal_req,String viewpath,HttpServletRequest req) throws Exception
	{
		int p = viewpath.indexOf('/',1) ;
		String cxt_dir = viewpath.substring(0,p) ;
		int q = viewpath.lastIndexOf('/') ;
		String servletdir = viewpath.substring(p,q+1) ;
		String locpath = viewpath.substring(q+1) ;
		
//		查看是否有动态权限控制
		Auth a = null;//getDynAuthConf(cxt_dir,servletdir);
		if(a==null)
		{
			AppWebConfig wc = getAppWebConfigByPath(cxt_dir,servletdir);
			a = wc.getAuth() ;
		}
		
		if(a==null)
		{
//			寻找缺省的配置是否需要登陆
			if(!AppConfig.isAuthDefaultLogin())
				return 1;
			
			//if(bportal_req)
			//{//如果一个目录没有任何权限定制，并且必须登录，则不允许portal请求
			//	return -1;
			//}
			
			UserProfile up = UserProfile.getUserProfile(req);
			if(up!=null)
				return 1 ;
			else
				return 0 ;
		}
		
		UserProfile up = UserProfile.getUserProfile(req);
		return a.checkAllow(req,bportal_req,up, locpath);
	}
	
	
	private static HashMap<String,AppWebConfig> module2webconf = new HashMap<String,AppWebConfig>() ;
	
	/**
	 * 根据模块名称获得对应的WebConfig对象
	 * @param modulen
	 * @return
	 * @throws Exception
	 */
	public static AppWebConfig getModuleWebConfig(String modulen)
		//throws Exception
	{
		AppWebConfig wc = module2webconf.get(modulen);
		if(wc!=null)
			return wc ;
		
		File webf = new File(AppConfig.getTomatoWebappBase()+"/"+modulen) ;
		File f = new File(AppConfig.getTomatoWebappBase()+"/"+modulen+"/web.xml");
		if(!f.exists())
			f = new File(AppConfig.getTomatoWebappBase()+"/"+modulen+"/web.conf");
		wc = new AppWebConfig(webf,modulen,f);
		module2webconf.put(modulen, wc);
		return wc ;
	}
	
	
	public static ArrayList<AppWebConfig> getModuleWebConfigAll()
	{
		ArrayList<AppWebConfig> rets = new ArrayList<AppWebConfig>() ;
		for(String mn :AppConfig.getTomatoWebappModules())
		{
			AppWebConfig awc = getModuleWebConfig(mn) ;
			rets.add(awc) ;
		}
		return rets ;
	}
	
	
	public static String transAbsPath(String appn,String p)
	{
		if(p==null)
			return null ;
		
		p = p.trim() ;
		if(!p.startsWith("/")&&!p.startsWith("http://"))
		{
			p = "/"+appn+"/"+p ;
		}
		return p ;
	}
	
	private File webPathDir = null ;
	
	private String moduleName = null ;
	
	private Element confRootEle = null;
	
	private String titleCn = null ;
	
	private String titleEn = null ;
	
	private transient Auth auth = null ;
	/**
	 * 
	 * @param b
	 */
	private AppWebConfig(File webpath,String modulen,File f)
	{
		webPathDir = webpath ;
		
		moduleName = modulen ;
		
		loadConf(f);
		
		loadAuthorization();
	}
	
	private Element loadConf(File f)
	{
		if(!f.exists())
			return null ;
		
		if (confRootEle != null)
			return confRootEle;

		synchronized (this)
		{
			if (confRootEle != null)
				return confRootEle;

			try
			{
				DocumentBuilderFactory docBuilderFactory = null;
				DocumentBuilder docBuilder = null;
				Document doc = null;

				// parse XML XDATA File
				docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setValidating(false);
				docBuilder = docBuilderFactory.newDocumentBuilder();

				doc = docBuilder.parse(f);

				confRootEle = doc.getDocumentElement();
				
				titleCn = confRootEle.getAttribute("title_cn") ;
				titleEn = confRootEle.getAttribute("title_en") ;
				return confRootEle;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
	
	
	public static Element loadConfElementFromFile(File f) throws Exception
	{
		if(!f.exists())
			return null ;
		
		
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		// parse XML XDATA File
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating(false);
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(f);

		return doc.getDocumentElement();
	}
	
	//
	//TODO: 有可能引入可以后台配置的功能
	private void loadAuthorization()
	{
		Element ele = getConfElement(TAG_AUTH);
		if(ele==null)
			return ;
		
		auth = new Auth(ele);
	}
	
	/**
	 * 获得模块对应的文件目录
	 * @return
	 */
	public File getModuleDirPath()
	{
		return webPathDir ;
	}
	
	public String getModuleName()
	{
		return moduleName ;
	}
	
	public String getTitleCn()
	{
		return titleCn ;
	}
	
	public String getTitleEn()
	{
		return titleEn ;
	}
	
	public Element getConfElement(String name)
	{
		if(confRootEle==null)
			return null ;
		
		NodeList nl = confRootEle.getElementsByTagName(name);
		if (nl == null)
			return null;

		return (Element) nl.item(0);
	}
	
	public Auth getAuth()
	{
		return auth ;
	}
	
	
	
	/**
	 * 对应 web.conf中的验证节点 &lt;authorization desc=""&gt;<br/>
	 * @author Jason Zhu
	 *
	 */
	public static class Auth
	{
		private AuthLocation defaultLoc = null ;
		//private boolean bDefaultAllow = true ;
		private HashMap<String,AuthLocation> path2loc = new HashMap<String,AuthLocation>() ;
		
		Auth(Element ele)
		{
			Element[] eles = XmlHelper.getSubChildElement(ele, TAG_DEFAULT) ;
			if(eles!=null&&eles.length>0)
			{
				//bDefaultLogin = "true".equalsIgnoreCase(eles[0].getAttribute(ATTRN_IS_LOGIN));
				defaultLoc = new AuthLocation(eles[0]) ;
				//bDefaultAllow = !ATTRV_DENY.equalsIgnoreCase(eles[0].getAttribute(ATTRN_TYPE));
			}
			else
			{
				//bDefaultLogin = AppConfig.isAuthDefaultLogin() ;
				defaultLoc = new AuthLocation(AppConfig.isAuthDefaultLogin(),true,null,null,false);
				//bDefaultAllow = AppConfig.isAuthDefaultAllow() ;
			}
			
			eles = XmlHelper.getSubChildElement(ele, TAG_LOC) ;
			if(eles!=null)
			{
				for(Element tmpe:eles)
				{
					AuthLocation al = new AuthLocation(tmpe);
					String p = al.getPath() ;
					if(p==null||p.equals(""))
						continue ;
					path2loc.put(al.getPath(), al);
				}
			}
			
		}
		
//		public boolean isDefaultAllow()
//		{
//			return bDefaultAllow ;
//		}
		
		/**
		 * 检测某个用户是否允许
		 */
		public int checkAllow(HttpServletRequest req,boolean bprotal_req,UserProfile up,String locpath)
		{
			AuthLocation al = path2loc.get(locpath);
			if(al==null)
			{
				//if(bprotal_req &&!defaultLoc.bSupportPortal)
				//{
				//	return -1;
				//}
				if(defaultLoc.fitUser(req,up))
					return 1 ;
				else
					return 0 ;
			}
			
			//if(bprotal_req &&!al.bSupportPortal)
			//{
			//	return -1;
			//}
			
			if(al.isAllow())
			{
				if(al.fitUser(req,up))
					return 1 ;
				else
					return 0;
			}
			else
			{
				if(al.fitUser(req,up))
					return 0 ;
				else
					return 1;
			}
		}
	}
	
	
	
	/**
	 * 对应 web.conf中的验证节点 &lt;location path="quickline_mgr.jsp" type="allow" roles="root" /&gt;<br/>
	 * @author Jason Zhu
	 *
	 */
	public static class AuthLocation
	{
		private String path = null ;
		private boolean bAllow = true ;
		private boolean bLogin = false;
		private boolean bSupportPortal = false;
		private HashSet users = null;
		private HashSet roles = null;
		
		/**
		 * 判断是否只能是内部访问
		 */
		private boolean bInnerAccessOnly = false;
		
		/**
		 * 如果users==null && roles==null
		 */
		private transient boolean bFitAll = false ;
		
		public AuthLocation(Element ele)
		{
			String p = ele.getAttribute(ATTRN_PATH);
			if(p==null)
				p = "" ;
//			if(p==null||p.equals(""))
//				throw new RuntimeException("no path found in location in web.conf!");
			
			boolean ballow = !ATTRV_DENY.equalsIgnoreCase(ele.getAttribute(ATTRN_TYPE));
			boolean blog = "true".equalsIgnoreCase(ele.getAttribute(ATTRN_IS_LOGIN)) ;
			boolean bsp = "true".equalsIgnoreCase(ele.getAttribute(ATTRN_SUPPORT_PORTAL)) ;
			String us = ele.getAttribute(ATTRN_USRES);
			String rs = ele.getAttribute(ATTRN_ROLES);
			boolean binner = "true".equalsIgnoreCase(ele.getAttribute(ATTRN_INNER_ACCESS_ONLY)) ;
			init(p,bsp,blog,ballow,us,rs,binner);
		}
		
		private AuthLocation(boolean blogin,boolean ballow,String users,String roles,boolean binner_access)
		{
			init(null,false,blogin,ballow,users,roles,binner_access);
		}
		
		private void init(String path,boolean bportal,boolean blogin,boolean ballow,
				String users,String roles,boolean binner_only)
		{
			this.path = path ;
			this.bSupportPortal = bportal ;
			this.bAllow = ballow ;
			this.bLogin = blogin ;
			if(users!=null&&!users.equals("")&&!users.equals("*")&&!users.equals("?"))
			{
				this.users = new HashSet() ;
				StringTokenizer st = new StringTokenizer(users,",|");
				while(st.hasMoreTokens())
				{
					this.users.add(st.nextToken());
				}
			}
			if(roles!=null&&!roles.equals(""))
			{
				this.roles = new HashSet() ;
				StringTokenizer st = new StringTokenizer(roles,",|");
				while(st.hasMoreTokens())
				{
					this.roles.add(st.nextToken());
				}
			}
			
			bFitAll = (this.users==null && this.roles == null) ;
			this.bInnerAccessOnly = binner_only ; 
		}
		
		public String getPath()
		{
			return path ;
		}
		
		public boolean isLogin()
		{
			return bLogin ;
		}
		
		public boolean isAllow()
		{
			return bAllow ;
		}
		
		public boolean isInnerAccessOnly()
		{
			return bInnerAccessOnly ;
		}
		/**
		 * 判定本条件是否满足一个用户
		 * @param up
		 * @return
		 */
		public boolean fitUser(HttpServletRequest req,UserProfile up)
		{
			if(!bLogin)
				return true ;
			
			//以下必须要登陆
			if(up==null)
				return false;
			
			if(bInnerAccessOnly)
			{
				if(!AccessManager.getInstance().isInnerRequest(req))
					return false;
			}
			
			if(bFitAll)
				return true ;
			
			if(users!=null)
			{
				if(users.contains(up.getUserName()))
					return true ;
			}
			
			if(roles!=null)
			{
				List<Role> rs = up.getRoleInfo();
				if(rs!=null)
				{
					for(Role r:rs)
					{
						if(roles.contains(r.getName()))
							return true ;
					}
				}
			}
			
			
			return false ;
		}
		
	}
	
	
	//static HashMap<String,HashMap<String,Auth>> cxt2servlet_auth = new HashMap<String,HashMap<String,Auth>>() ;
	/**
	 * 根据上下文及请求的路径，判断动态权限
	 * 动态权限存储在data/dyn_auth 目录中。每个module
	 * @param cxt_dir
	 * @param servletdir
	 * @return
	 */
	public static Auth getDynAuthConf(String cxt_dir,String servletpath,HttpServletRequest req)
	{
		if(true)
			return null ;
		
		return null;
		/*
		HashMap<String,Auth> servlet_auth = cxt2servlet_auth.get(cxt_dir) ;
		if(servlet_auth!=null)
		{
			return servlet_auth.get(servletdir) ;
		}
		
		//load from data/dyn_auth
		
		servlet_auth = new HashMap<String,Auth>() ;
		cxt2servlet_auth.put(cxt_dir,servlet_auth) ;

		return servlet_auth.get(servletdir) ;
		*/
	}
	
	
}

