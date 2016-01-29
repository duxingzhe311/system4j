package com.dw.system;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.w3c.dom.Element;

import com.dw.comp.AbstractComp;
import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.comp.CompTask;
import com.dw.system.gdb.GDB;
import com.dw.system.gdb.syn.GDBLogManager;
import com.dw.system.gdb.syn_client.*;
import com.dw.system.logger.*;
import com.dw.system.task.Task;
import com.dw.system.task.TaskManager;
import com.dw.system.util.Mime;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlHelper;
import com.dw.user.*;
import com.dw.user.access.AccessManager;
import com.dw.user.util.SessionVar;
import com.dw.web_ui.WebRes;

/*整个系统使用的唯一过滤器
 * 
 * 0,自己判定是否是独立运行,还是
 * 1,支持权限控制
 * 2,支持页面模板
 * 3,支持大构件的装载初始化
 */
public class AppConfigFilter implements Filter
{
	static
	{
		try
		{//安装用户和字典表--可以保证此安装优先级最高
			GDB.getInstance().installGdb("/com/dw/user/provider/gdb_Security.xml");
			GDB.getInstance().installGdb("/com/dw/system/dict/ioimpl/gdb_dd.xml");
		}
		catch(Exception eee)
		{
			eee.printStackTrace() ;
		}
	}
	/**
	 * portal管理页面中需要的输入参数-页面id
	 */
	public static final String REQ_PN_PORTAL_PAGE_ID = "_portal_pid" ;
	
	/**
	 * portal管理页面中需要的输入参数-页面块id
	 */
	public static final String REQ_PN_PORTAL_BLOCK_ID = "_portal_bid" ;
	
	public static class PortalReq
	{
		String pageDomain = null ;
		String pageName = null ;
		boolean isTemp = false;
		boolean isMgr = false;
		/**
		 * 确定某一页面是非法的portal页面
		 */
		boolean isEmpty = false;
		
		public PortalReq(String servern,String pagen,boolean istemp)
		{
			pageDomain = servern ;
			pageName = pagen ;
			isTemp = istemp ;
		}
		
		private PortalReq(boolean ismgr,boolean isempty)
		{
			isMgr = ismgr ;
			isEmpty = isempty ;
		}
		
		public String getPageDomain()
		{
			return pageDomain ;
		}
		
		public String getPageName()
		{
			return pageName ;
		}
		
		public boolean isTemplate()
		{
			return isTemp ;
		}
		
		public boolean isPageManager()
		{
			return isMgr ;
		}
		
		public boolean isEmptyPage()
		{
			return isEmpty;
		}
	}
	
	public static final PortalReq PORTAL_MGR_REQ = new PortalReq(true,false);
	public static final PortalReq PORTAL_EMPTY_REQ = new PortalReq(false,true);
	
	public static interface PortalHandler
	{
		/**
		 * 监测请求是否是portal请求
		 * @param req
		 * @return
		 */
		public PortalReq checkPortalReq(HttpServletRequest req) ;
		
		/**
		 * 根据portal请求，做内容处理输出
		 * @param req
		 * @param resp
		 * @throws ServletException
		 * @throws IOException
		 */
		public void renderPortalReq(PortalReq preq,HttpServletRequest req,HttpServletResponse resp) throws Exception;
		
		
		public boolean checkMgrRight(PortalReq preq,
				HttpServletRequest req,HttpServletResponse resp,
				UserProfile up,
				StringBuilder failedr) throws Exception;
	}
	
	public static interface InitActionHander
	{
		public void handleAction(String actp) throws Exception ;
	}
	
	static InitActionHander initActHandlerIns = null ;
	static PortalHandler portalHandler = null ;
	
	public static InitActionHander getInitActionHandlerIns() throws Exception
	{
		if(initActHandlerIns!=null)
			return initActHandlerIns ;
		
		Class c = Class.forName("com.dw.biz.InitActionHandler") ;
		if(c==null)
			return null ;
		initActHandlerIns = (InitActionHander)c.newInstance() ;
		return initActHandlerIns ;
	}
	
	public static PortalHandler getPortalHandler()
	{
		if(portalHandler!=null)
			return portalHandler ;
		
		try
		{
			Class c = Class.forName("com.dw.portal.BizPortalHandler") ;
			if(c==null)
				return null ;
			portalHandler = (PortalHandler)c.newInstance() ;
			return portalHandler ;
		}
		catch(Exception ee)
		{
			return null ;
		}
	}
	
	static ILogger log = LoggerManager.getLogger(AppConfigFilter.class);
	
	public static final String KEY_AUTH_SESSION_NAME = "access_auth_timelimit" ;
	
	
	
	
	public AppConfigFilter()
	{

	}

	public void init(FilterConfig config) throws ServletException
	{
		String fsucc = config.getInitParameter("force_succ");

		if ("true".equalsIgnoreCase(fsucc))
		{
			try
			{
				init0(config);

				AppConfig.appConfigInitError = "succ";
				AppConfig.appConfigInitSucc = true;
			}
			catch (Throwable eee)
			{
				//
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				eee.printStackTrace(pw);
				AppConfig.appConfigInitError = "App Root="
						+ AppConfig.getSoleWebAppRoot() + "\r\n"
						+ eee.getMessage() + "\r\n" + sw.toString();
			}
		}
		else
		{
			try
			{
				init0(config);
			}
			catch(Exception eeee)
			{
				if(log.isErrorEnabled())
					log.error(eeee) ;
			}
			
			AppConfig.appConfigInitSucc = true;
		}
	}

	private void init0(FilterConfig config)
	{
		initAppConfig(config);

		// 确定运行方式之后才初始化GDB
		try
		{
			GDB.getInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// 如果不是独立运行则初始化构件
		// if(!AppConfig.isSole())
		initComp(config);
	}

	private static String getWebAppRootPath(ServletContext sc,
			String classresfile)
	{
		String realpath = sc.getRealPath("/");
		if (!Convert.isNullOrEmpty(realpath))
			return realpath;

		// 使用class定位
		if (Convert.isNullOrEmpty(classresfile))
			classresfile = "cres.conf";
		URL url = new Object()
		{
		}.getClass().getResource("/" + classresfile);
		if (url == null)
			throw new RuntimeException("cannot location webapp root!");

		String abspath = new File(url.getFile()).getParentFile()
				.getParentFile().getParentFile().getAbsolutePath();
		return abspath.replace('\\', '/');
	}

	/**
	 * 初始化AppConfig
	 * 
	 * @param config
	 */
	private void initAppConfig(FilterConfig config)
	{
		String crsfile = config.getInitParameter("class_res_file");

		String realpath = getWebAppRootPath(config.getServletContext(), crsfile);
		AppConfig.bSoleWebAppRoot = realpath;

		String appconf_file = config.getInitParameter("app_conf_file");
		if (!Convert.isNullOrEmpty(appconf_file))
		{// web.xml 通过强制指定的路径确定app.xml app.conf.xml app.conf文件的位置
			// 在这种情况下,该目录下的 data template等基于app conf base的目录都应该在那里
			// 此情况下缺省为独立运行情况
			appconf_file = appconf_file.replace('\\', '/') ;
			String fb = null ;
			if(appconf_file.startsWith("/")||appconf_file.indexOf(':')>=0)
				fb = (new File(appconf_file)).getParentFile()
					.getAbsolutePath();
			else
				fb = (new File(realpath+"/WEB-INF/",appconf_file)).getParentFile()
				.getAbsolutePath();
			
			AppConfig.setConfigFileBase(fb);
			AppConfig.setIsSole(true);
			System.out.println("App Config For WebApp [SOLE],File Base="
					+ AppConfig.getConfigFileBase());
			return;
		}

		// String realpath =
		// AppConfig.getWebAppRootPath(config.getServletContext());//config.getServletContext().getRealPath("/");
		// System.out.println("realpath==="+realpath);
		String comp_rootname = realpath = realpath.replace('\\', '/');
		if (realpath.endsWith("/"))
			comp_rootname = comp_rootname.substring(0, realpath.length() - 1);

		int p = comp_rootname.lastIndexOf('/');
		comp_rootname = comp_rootname.substring(p + 1);

		String tmpfb = null;
		if (realpath.endsWith("/"))
			tmpfb = realpath + "WEB-INF/";
		else
			tmpfb = realpath + "/WEB-INF/";

		if ("true".equalsIgnoreCase(config.getInitParameter("web_sole")))
		{
			AppConfig.setConfigFileBase(tmpfb);
			AppConfig.setIsSole(true);
			System.out.println("App Config For WebApp [SOLE],File Base="
					+ AppConfig.getConfigFileBase());
		}
		else
		{
			File f1 = new File(tmpfb + "app.xml");
			File f2 = new File(tmpfb + "app.conf");
			File f3 = new File(tmpfb + "app.conf.xml");
			if (f1.exists() || f2.exists() || f3.exists())
			{
				// 该方法可能引起Tomato和独立发布的webapp的配置冲突 TODO 需要解决
				AppConfig.setConfigFileBase(tmpfb);
				AppConfig.setIsSole(true);
				System.out.println("App Config For WebApp [SOLE],File Base="
						+ AppConfig.getConfigFileBase());
			}
			else
			{
				System.out.println("App Config For WebApp [COMP],File Base="
						+ AppConfig.getConfigFileBase());
			}
		}
		AppConfig.loadConf();
	}

	private void initComp(FilterConfig config)
	{
		CompManager cm = CompManager.getInstance();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		String realpath = config.getServletContext().getRealPath("/");
		if (Convert.isNullOrEmpty(realpath))
			return;

		String comp_rootname = realpath = realpath.replace('\\', '/');
		if (realpath.endsWith("/"))
			comp_rootname = comp_rootname.substring(0, realpath.length() - 1);

		int p = comp_rootname.lastIndexOf('/');
		comp_rootname = comp_rootname.substring(p + 1);

		AppInfo ci = new AppInfo(realpath, comp_rootname, cl);
		
		CompManager.getInstance().fireAppFinding(ci);
		
		AppWebConfig awc = AppWebConfig.getModuleWebConfig(comp_rootname) ;
		
		Element install_ele = awc.getConfElement("install") ;
		if(install_ele!=null)
		{
			processInstall(comp_rootname, install_ele);
		}
		
		//终端同步
		Element client_syn_ele = awc.getConfElement("client_syn");
		if(client_syn_ele!=null)
		{
			processClientSyn(comp_rootname,cl, client_syn_ele);
		}
		
		//处理用户插件
		Element sec_ele = awc.getConfElement("security_plug") ;
		if(sec_ele!=null)
		{
			processSecurityPlug(cl, sec_ele);
		}
		
		
		// System.out.println(">>>load comp by webapp context
		// name=["+config.getServletContext().getServletContextName()+"] real
		// path /="+config.getServletContext().getRealPath("/")+" class
		// loader--"+cl.getClass().getCanonicalName());
		String cs = config.getInitParameter("comp-classes");
		if (!Convert.isNullOrEmpty(cs))
		{
			StringTokenizer tmpst = new StringTokenizer(cs, ",|");
			while (tmpst.hasMoreTokens())
			{
				try
				{
					String n = tmpst.nextToken();
					AbstractComp ac = (AbstractComp) cl.loadClass(n)
							.newInstance();
					cm.registerComp(cl, ac);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		
		//IAppWebConfigLoad listener
		for(IAppWebConfigLoadedListener ll:AppWebConfig.configLoadLis)
		{
			//System.out.println("trig app web config loaded="+ll.getClass().getCanonicalName()) ;
			ll.onAppWebConfigLoaded(awc, cl) ;
		}
		
		InitActionHander iah = null;
		
		try
		{
			iah = getInitActionHandlerIns();
		}
		catch(Exception eeee)
		{
			eeee.printStackTrace() ;
		}
		
		String ias = config.getInitParameter("init-actions");
		if (iah!=null&&!Convert.isNullOrEmpty(ias))
		{
			StringTokenizer tmpst = new StringTokenizer(ias, ",|");
			while (tmpst.hasMoreTokens())
			{
				try
				{
					String act_p = tmpst.nextToken();
					//
					if(act_p.startsWith("../"))
					{
						act_p = "/"+comp_rootname+act_p.substring(2) ;
					}
					else if(act_p.startsWith("./"))
					{
						act_p = "/"+comp_rootname+"/WEB-INF"+act_p.substring(1) ;
					}
					else if(!act_p.startsWith("/"))
					{
						act_p = "/"+comp_rootname+"/WEB-INF/"+act_p ;
					}
					
					//
					iah.handleAction(act_p) ;
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		
		

		if (!AppConfig.isSole())
		{
			initTask(comp_rootname,config);
		}
	}

	private void processInstall(String comp_rootname, Element install_ele)
	{
		Element[] eles=XmlHelper.getSubChildElement(install_ele, "gdb") ;
		if(eles!=null)
		{
			for(Element ele:eles)
			{
				String path = ele.getAttribute("path") ;
				if(Convert.isNullOrEmpty(path))
					continue ;
				
				try
				{
					GDB.getInstance().installGdb(comp_rootname,path,null) ;
				}
				catch(Exception ee)
				{
					System.out.println("install gdb path="+path+" failed-"+ee.getMessage()) ;
					if(ee instanceof SQLException)
					{
						SQLException nxte = ((SQLException)ee).getNextException() ;
						if(nxte!=null)
							System.out.println("	Next Exception="+ee.getMessage()) ;
					}
				}
			}
		}
	}
	
	private void processClientSyn(String modulen,ClassLoader cl, Element syn_ele)
	{
		Element[] eles = XmlHelper.getSubChildElement(syn_ele, "gdb_syn_table");
		if(eles!=null)
		{
			for(Element ele:eles)
			{
				String cn = ele.getAttribute("impl_class") ;
				if(Convert.isNullOrEmpty(cn))
					continue ;
				
				try
				{
					Class ccc = cl.loadClass(cn) ;
					if(ccc==null)
						continue ;
					
					Object nobj = ccc.newInstance();
					if(nobj!=null)
					{
						if(nobj instanceof ISynClientable)
							SynClientManager.getInstance().registerSynTable(modulen, (ISynClientable)nobj);
						
						if(nobj instanceof ISimpleSynTable)
						{
							SynClientManager.getInstance().registerSimpleSynTable(modulen, (ISimpleSynTable)nobj);
						}
					}
				}
				catch(Exception ee)
				{
					ee.printStackTrace();
				}
			}
		}
	}

	private void processSecurityPlug(ClassLoader cl, Element sec_ele)
	{
		Element[] eles = XmlHelper.getSubChildElement(sec_ele, "org_node_sub_def");
		if(eles!=null)
		{
			for(Element ele:eles)
			{
				String tmpp = ele.getAttribute("path") ;
				if(Convert.isNullOrEmpty(tmpp))
					continue ;
				
				try
				{
					StringTokenizer st = new StringTokenizer(tmpp,"/") ;
					int cc = st.countTokens() ;
					String curppath = "/";
					for(int k = 0 ; k < cc - 1 ; k ++)
					{
						String nn = st.nextToken() ;
						OrgManager.getDefaultIns().createOrgNodeWhenNotExists(curppath,nn,nn,"") ;
						curppath += (nn+"/") ;
					}
					
					String lastn = st.nextToken() ;
					String tmpt = ele.getAttribute("title") ;
					if(Convert.isNullOrEmpty(tmpt))
						tmpt = lastn;
					
					String desc = ele.getAttribute("desc") ;
					
					OrgNode tmpon = OrgManager.getDefaultIns().createOrgNodeWhenNotExists(curppath,lastn,tmpt,desc) ;
					String ext_class = ele.getAttribute("ext_class") ;
					if(Convert.isNotNullEmpty(ext_class))
					{
						Class ccl = cl.loadClass(ext_class) ;
						if(ccl!=null)
							OrgManager.getDefaultIns().setSubNodeExtClass(tmpon.getOrgNodeId(),ccl);
					}
				}
				catch(Exception ee)
				{
					ee.printStackTrace();
				}
			}
		}
		
		
		eles=XmlHelper.getSubChildElement(sec_ele, "org_node_sub_ext") ;
		if(eles!=null)
		{
			for(Element ele:eles)
			{
				String cn = ele.getAttribute("class") ;
				if(Convert.isNullOrEmpty(cn))
					continue ;
				
				try
				{
					Class ccc = cl.loadClass(cn) ;
					if(ccc==null)
						continue ;
					
					OrgManager.getDefaultIns().setSubNodeExtClass(ccc) ;
				}
				catch(Exception ee)
				{
					System.out.println("init security org_node_sub_ext load class="+cn+" failed") ;
				}
			}
		}
		
		
		
		eles=XmlHelper.getSubChildElement(sec_ele, "user_ext") ;
		if(eles!=null&&eles.length>0)
		{
			String cn = eles[0].getAttribute("class") ;
			if(Convert.isNotNullEmpty(cn))
			{
				try
				{
					Class ccc = cl.loadClass(cn) ;
					if(ccc!=null)
					{
						UserManager.getDefaultIns().setUserExtItemClass(ccc) ;
					}
				}
				catch(Exception ee)
				{
					System.out.println("init security org_node_sub_ext load class="+cn+" failed") ;
				}
			}
		}
	}
	
	
	private ArrayList<String> appTaskIds = new ArrayList<String>() ;

	private void initTask(String compn,FilterConfig config)
	{
		String ts = config.getInitParameter("comp-tasks");
		if (!Convert.isNullOrEmpty(ts))
		{// 形如 /xx/xx.action?dx_/pp:int32=1?30000
			// :之前是action路径,之后是运行时间间隔毫秒数
			int sp = 0, ep = 0;
			ArrayList<CompTask> cts = new ArrayList<CompTask>();
			do
			{
				sp = ts.indexOf('{', ep);
				if (sp < 0)
					break;
				ep = ts.indexOf('}', sp);
				if (ep < 0)
					break;

				String tmps = ts.substring(sp + 1, ep).trim();
				StringReader sr = new StringReader(tmps);
				// Properties props = new Properties() ;
				BufferedReader br = new BufferedReader(sr);
				String line = null;
				HashMap<String, String> taskinfo = new HashMap<String, String>();
				HashMap<String, String> taskpms = new HashMap<String, String>();

				try
				{
					while ((line = br.readLine()) != null)
					{
						line = line.trim();
						if ("".equals(line))
							continue;
						
						if(line.startsWith("#"))
							continue ;

						int k = line.indexOf('=');
						if (k <= 0)
						{
							if (line.startsWith("_"))
								taskinfo.put(line, "");
							else
								taskpms.put(line, "");
						}
						else
						{
							String pn = line.substring(0, k).trim();
							if (pn.startsWith("_"))
								taskinfo.put(pn, line.substring(k + 1)
										.trim());
							else
								taskpms.put(pn, line.substring(k + 1)
										.trim());
						}
					}

					String taskid = "["+compn+"]"+taskinfo.get(Task.T_NAME);
					Task t = new Task(taskid,taskinfo, taskpms);
					// cm.getTaskTable().setTask(t);
					if(log.isInfoEnabled())
						log.info("find task="+t.toString());
					TaskManager.getInstance().setTask(t);
					
					appTaskIds.add(taskid) ;
				}
				catch (Exception ioe)
				{
					//ioe.printStackTrace();
					System.out.println("Warn : set task error="+ioe.getMessage()) ;
					
					//if(log.isErrorEnabled())
					//	log.error(ioe);
					continue;
				}
			}
			while (sp >= 0);
		}
	}
	
	
	
	private void initProcess(String compn,FilterConfig config)
	{
		String ts = config.getInitParameter("comp-process");
		if (!Convert.isNullOrEmpty(ts))
		{// 形如 /xx/xx.action?dx_/pp:int32=1?30000
			// :之前是action路径,之后是运行时间间隔毫秒数
			int sp = 0, ep = 0;
			ArrayList<CompTask> cts = new ArrayList<CompTask>();
			do
			{
				sp = ts.indexOf('{', ep);
				if (sp < 0)
					break;
				ep = ts.indexOf('}', sp);
				if (ep < 0)
					break;

				String tmps = ts.substring(sp + 1, ep).trim();
				StringReader sr = new StringReader(tmps);
				// Properties props = new Properties() ;
				BufferedReader br = new BufferedReader(sr);
				String line = null;
				HashMap<String, String> taskinfo = new HashMap<String, String>();
				HashMap<String, String> taskpms = new HashMap<String, String>();

				try
				{
					while ((line = br.readLine()) != null)
					{
						line = line.trim();
						if ("".equals(line))
							continue;
						
						if(line.startsWith("#"))
							continue ;

						int k = line.indexOf('=');
						if (k <= 0)
						{
							if (line.startsWith("_"))
								taskinfo.put(line, "");
							else
								taskpms.put(line, "");
						}
						else
						{
							String pn = line.substring(0, k).trim();
							if (pn.startsWith("_"))
								taskinfo.put(pn, line.substring(k + 1)
										.trim());
							else
								taskpms.put(pn, line.substring(k + 1)
										.trim());
						}
					}

					Task t = new Task("["+compn+"]"+taskinfo.get(Task.T_NAME),taskinfo, taskpms);
					// cm.getTaskTable().setTask(t);
					if(log.isInfoEnabled())
						log.info("find task="+t.toString());
					TaskManager.getInstance().setTask(t);

				}
				catch (Exception ioe)
				{
					//ioe.printStackTrace();
					System.out.println("Warn : set task error="+ioe.getMessage()) ;
					
					if(log.isErrorEnabled())
						log.equals(ioe);
					continue;
				}
			}
			while (sp >= 0);
		}
	}
	
	

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain fc) throws ServletException, IOException
	{
		HttpServletRequest req = (HttpServletRequest) request;
		
		if(log.isDebugEnabled())
		{
			System.out.println("--------req heads---------") ;
			for(Enumeration en1 = req.getHeaderNames();en1.hasMoreElements();)
			{
				String attrn = (String)en1.nextElement() ;
				String attrv = req.getHeader(attrn) ;
				System.out.println(attrn+"="+attrv) ;
			}
			System.out.println("---------------------------") ;
		}
		
		String sp = req.getServletPath() ;
		if(sp!=null&&sp.startsWith("/web.xml"))
		{
			return ;
		}
		
		HttpServletResponse res = (HttpServletResponse) response;
		req.setCharacterEncoding("UTF-8");
		
		res.setHeader( "Pragma", "no-cache" );
		res.setHeader( "Cache-Control", "no-cache" );
		res.setHeader( "Cache-Control", "no-store" );
		res.setDateHeader( "Expires", 0 );
		
		PortalHandler ph = getPortalHandler() ;
		PortalReq pr = null ;
		if(ph!=null)
			pr = ph.checkPortalReq(req) ;
		if(pr!=null&&!pr.isEmpty)
		{//portal page
			try
			{
				if(pr.isMgr)
				{//检查Portal *.mgr.jsp页面请求的权限
					StringBuilder sb = new StringBuilder() ;
					if(ph.checkMgrRight(pr,req,res,UserProfile.getUserProfile(req),sb))
					{
						fc.doFilter(req, res);
						return ;
					}
					else
					{//没有足够的权限
						res.getWriter().print(sb.toString()) ;
						return ;
					}
				}
				
				ph.renderPortalReq(pr,req, res);
			}
			catch(Exception ee)
			{
				ee.printStackTrace(res.getWriter()) ;
				//ee.printStackTrace();
			}
			return ;
		}
		
		if (!AppConfig.isAppConfigInitSucc())
		{// 在初始化出错的情况下,所有的页面都可以访问
			// 其目的是保证一些snoop.jsp等页面能够提供远程运行调试
			//fc.doFilter(req, res);
			//return;
		}
		
		String cxt_root = WebRes.getContextRootPath(req);
		// LoginSession ls = SessionManager
		// .getCurrentLoginSession((HttpServletRequest) request);

		String uri = req.getRequestURI();
		
		
		// System.out.println("User Right Filter uri="+uri) ;
		try
		{
			
			//访问验证
			//
			
			AccessManager am = AccessManager.getInstance() ;
			if(am.isNeedAuth(req))
			{
				if(uri.startsWith("/system/user/outer_auth/"))
				{
					fc.doFilter(req, res);
					return ;
				}
				//
				UserProfileAuth upa = UserProfileAuth.getUserProfileAuth(req) ;
				//Long aatl_obj = (Long)session.getAttribute(KEY_AUTH_SESSION_NAME) ;
				if(upa==null)
				{//need outer access auth
					//
					
					res.sendRedirect("/system/user/outer_auth/outer_access_auth.jsp");
					return ;
				}
				else
				{
					long aatl = upa.getTimeOut() ;
					if(aatl<0)
					{
						response.getWriter().write("failed,use a new window!!!");
						return;
					}
					
					if(aatl>0)
					{
						long cur_t = System.currentTimeMillis() ;
						if(cur_t>aatl)
						{
							response.getWriter().write("failed,temporarily auth timeout!");
							return;
						}
					}
					
					//
				}
			}
			
			
			HttpSession session = req.getSession();
			UserProfile up = UserProfile.getUserProfile(req);

			int chres = AppWebConfig.checkRequestAllow(up,pr!=null,session.getServletContext(),
					req);
			if(chres<0)
			{
				response.getWriter().write("portal request for this page is not allowed!");
				return ;
			}
			
			if (chres==0)
			{
				if (up != null)
				{
					response.getWriter().write("you have no right!!");
					return;
				}

				String reqpath = req.getPathInfo() ;
				String query = req.getQueryString();
				// System.out.println("uri="+uri) ;
				// System.out.println("que="+query) ;
				if (query != null && !query.equals(""))
				{
					query = "?" + query;
				}
				String url = null;
				if (query == null)
				{
					url = uri;
				}
				else
				{
					url = uri + query;
				}

				// RequestDispatcher rd = req.getRequestDispatcher(
				// "/user/forcelogin.jsp?url=" + url);
				// rd.include(request, response);
				
				String lp = SessionManager.getLoginPage() ;
//				if(Convert.isNullOrEmpty(lp))
//				{
//					lp = 
//				}
				
				if(Convert.isNotNullEmpty(lp))
				{
					if (AppConfig.isSole())
						res.sendRedirect(lp+"?r=" + URLEncoder.encode(url,"UTF-8"));
					else
					{
						// res.sendRedirect("/system/user/login.jsp?r="+url);
						if(lp.equals(uri))
						{//may be this is login page
							fc.doFilter(req, res);
							return ;
						}
						else
						{
							res.setContentType("text/html");
							res.getWriter().print(
									"<script>document.location.href='"
											+lp+ "?r=" + URLEncoder.encode(url,"UTF-8")
											+ "';</script>");
						}
					}
				}
				else
				{
					if (AppConfig.isSole())
						res.sendRedirect(cxt_root + "user/login.jsp?r=" + url);
					else
					{
						// res.sendRedirect("/system/user/login.jsp?r="+url);
						res.setContentType("text/html");
						res.getWriter().print(
								"<script>document.location.href='"
										+ "/system/user/login.jsp?r=" + URLEncoder.encode(url,"UTF-8")
										+ "';</script>");
					}
				}
				return;
			}//end of if (chres==0)

			// session.setAttribute(SessionVar.LOGIN_SESSION, ls);

			if(up!=null)
			{
				session.setAttribute(SessionVar.USER_PROFILE, up);
				UserOnlineTable.accessUserProfile(up) ;
			}
		}
		catch (Exception eee)
		{
			// eee.printStackTrace();
			PrintWriter pw = response.getWriter();
			pw.write("check right error!");
			eee.printStackTrace(pw);
			return;
		}

		// 查看权限
		// PageHelper.PageItem pi = PageHelper.getPageItemByUrlPath(uri) ;
		// if(pi==null)
		// {
		// chain.doFilter(req, res);
		// return ;
		// }
		//
		// if(!PageHelper.checkCanAccess(pi,session))
		// {
		// res.getWriter().print("You have no right to access!");
		// return ;
		// }

		// System.out.println("right ok:"+uri);
		

		if (sp.endsWith(".view"))
		{
			String query = req.getQueryString();

			// /system/biz/biz_view_show.jsp?vp=/app_case_mgr/case_accept.view&prev_action=/app_case_mgr/case_get_by_id.action&dx_/CaseId:int32=2
			res.setContentType("text/html");
			if (query != null && !query.equals(""))
				res.getWriter().print(
						"<script>document.location.href='"
								+ "/system/biz/biz_view_show.jsp?vp=" + uri
								+ "&" + query + "';</script>");
			else
				res.getWriter().print(
						"<script>document.location.href='"
								+ "/system/biz/biz_view_show.jsp?vp=" + uri
								+ "';</script>");
			return;
		}
		if (sp.endsWith(".flow"))
		{
			String query = req.getQueryString();
			res.setContentType("text/html");
			if (query != null && !query.equals(""))
				res.getWriter().print(
						"<script>document.location.href='"
								+ "/system/biz/biz_flow_start.jsp?fp=" + uri
								+ "&" + query + "';</script>");
			else
				res.getWriter().print(
						"<script>document.location.href='"
								+ "/system/biz/biz_flow_start.jsp?fp=" + uri
								+ "';</script>");
			return;
		}
		// res.setContentType("text/html;charset=UTF-8");
		// 做页面模板
		// res.getWriter().print("1111111111111111");
		
//		res.setHeader( "Pragma", "no-cache" );
//		res.setHeader( "Cache-Control", "no-cache" );
//		res.setHeader( "Cache-Control", "no-store" );
//		res.setDateHeader( "Expires", 0 );
		   
		fc.doFilter(req, res);
		// res.getWriter().print("222222222");
	}

	public void destroy()
	{
		//unset task
		for(String tid:appTaskIds)
		{
			System.out.println("unset task ->"+tid) ;
			TaskManager.getInstance().unsetTask(tid) ;
		}
	}
}