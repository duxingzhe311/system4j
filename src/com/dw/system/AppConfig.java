package com.dw.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.mltag.util.XmlUtil;
import com.dw.web_ui.WebRes;
import com.dw.web_ui.temp.WebPageTemplate;

public class AppConfig
{
//	public static enum RunDistributedType
//	{
//		not,//非分布式系统
//		distributed_server,//分布式系统中的服务器
//		distributed_proxy;//分布式系统中的代理
//	}
	
	public static class DistributedProxyInfo
	{//<distributed_proxy proxy_id="xv" server_addr="sng.org" user_org_path="/sng/xxv">
		/**
		 * 代理id
		 * 只允许 0-9 a-z 的两位字符串 ，也就是36进制表示的值不能过ZZ
		 */
		public String proxyId = null ;
		/**
		 * 代理标题
		 */
		public String proxyTitle = null ;
		/**
		 * 服务器地址
		 */
		public String serverAddr = null ;
		/**
		 * 本系统所属的用户系统中组织机构路径
		 */
		public String userOrgPath = null ;
	}
	
	static String configFileBase = null ;
	/**
	 * 判定本应用是否是独立运行:也就是system.jar和配置信息都在一个webapp下面
	 * 可以独立运行的一个webapp
	 * 
	 * 
	 */
	static boolean bSole = false;
	
	static String bSoleWebAppRoot = null ;
	
	/**
	 * 在一些不可控制的环境中,发布失败无法知道错误信息
	 * 所以,只能时之初始化成功后,才能通过相关页面查看
	 * 
	 * 在此,根据web.xml配置,可以避免发布失败,其方法是通过try catch,把错误信息存储在
	 * 该变量中,便于远程调试
	 */
	static String appConfigInitError = "" ;
	
	static String lastConfigError = "" ;
	
	static boolean appConfigInitSucc = false;
	
	static String dataDirBase = null ;
	
	static
	{
		//缺省情况下的设定
		configFileBase = System.getProperties().getProperty("user.dir");
		//如果在独立运行情况下,会被AppConfigFilter进行修改
		//其目录为../WEB-INF/ 和web.xml在同一个目录下
	}
	
	/**
	 * 判断AppConfig初始化是否成功
	 * @return
	 */
	public static boolean isAppConfigInitSucc()
	{
		return appConfigInitSucc ;
	}
	
	
	public static String getAppConfigInitError()
	{
		return appConfigInitError;
	}
	
	public static String getConfigFileBase()
	{
		return configFileBase ;
	}
	
	
	public static void setConfigFileBase(String fb)
	{
		configFileBase = fb.trim() ;
		configFileBase = configFileBase.replace('\\', '/');
		if(!configFileBase.endsWith("/"))
			configFileBase += "/" ;
	}
	/**
	 * 判断本应用是否是独立运行的
	 * @return
	 */
	public static boolean isSole()
	{
		return bSole ;
	}
	
	public static void setIsSole(boolean bs)
	{
		bSole = bs ;
	}
	
	
	public static String getSoleWebAppRoot()
	{
		return bSoleWebAppRoot;
	}
	/**
	 * 根据配置文件名称，获得对应的File对象
	 * 如果不存在，则返回null
	 * @param conffn
	 * @return
	 */
	public static File getConfFile(String conffn)
	{
		String sysconffn = configFileBase
		+ "/"+conffn;
		return new File(sysconffn);
	}
	
	public static File getRelatedFile(String path)
	{
		if(!path.startsWith("."))
			return new File(path) ;
		
		String sysconffn = configFileBase
		+ "/"+path;
		return new File(sysconffn);
	}
	
	
	private static String tomatoWebApp = null ;
	/**
	 * 如果是Tomato运行模式,非sole,那么该方法返回对应的webapps目录后面有 '/'
	 * @return
	 */
	public static String getTomatoWebappBase()
	{
		if(isSole())
			return null;//throw new RuntimeException("app is running in sole mode!");
		
		if(tomatoWebApp!=null)
			return tomatoWebApp;
		
		Element tmpe = loadConf() ;
		String tmps = tmpe.getAttribute("tomato_webapp");
		if(tmps!=null&&!tmps.equals(""))
		{
			try
			{
				tmps = getRelatedFile(tmps).getCanonicalPath();
				tomatoWebApp = tmps ;
			}
			catch(Exception ee)
			{
				throw new RuntimeException(ee.getMessage());
			}
		}
		else
		{
			tomatoWebApp= configFileBase + "/webapps/";
		}
		
		return tomatoWebApp;
	}
	
	/**
	 * 获得所有的模块名称
	 * @return
	 */
	public static ArrayList<String> getTomatoWebappModules()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		String twb = getTomatoWebappBase() ;
		if(Convert.isNullOrEmpty(twb))
			return rets ;
		File f = new File(twb) ;
		if(!f.exists())
			return rets ;
		
		for(File tmpf:f.listFiles())
		{
			if(tmpf.isDirectory())
				rets.add(tmpf.getName()) ;
		}
		return rets ;
	}
	
	
	public static int getTomatoServerPort()
	{
		if(isSole())
			throw new RuntimeException("app is running in sole mode!");
		
		Element tmpe = loadConf() ;
		String tmps = tmpe.getAttribute("tomato_port");
		if(tmps!=null&&!tmps.equals(""))
		{
			return Integer.parseInt(tmps);
		}
		
		return -1 ;
	}
	
	
	public static int getTomatoServerCtrlPort()
	{
		if(isSole())
			throw new RuntimeException("app is running in sole mode!");
		
		Element tmpe = loadConf() ;
		String tmps = tmpe.getAttribute("tomato_ctrl_port");
		if(tmps!=null&&!tmps.equals(""))
		{
			return Integer.parseInt(tmps);
		}
		
		return -1 ;
	}
	
	
	public static int getGridServerPort()
	{
		if(isSole())
			throw new RuntimeException("app is running in sole mode!");
		
		Element tmpe = loadConf() ;
		String tmps = tmpe.getAttribute("grid_port");
		if(tmps!=null&&!tmps.equals(""))
		{
			return Integer.parseInt(tmps);
		}
		
		return -1 ;
	}
	
	public static String getDataDirBase()
	{
		if(dataDirBase!=null)
			return dataDirBase;
		
		
		return configFileBase + "/data/";
	}
	
	public static File getOrCreateDirInDataDirBase(String relateddir)
	{
		File f = new File(getDataDirBase(),relateddir);
		if(f.exists())
			return f ;
		
		f.mkdirs() ;
		return f ;
	}
	
	/**
	 * 根据页面上下文,获得某一个路径的真实文件路径
	 * @param pc
	 * @param p
	 * @return
	 */
	public static File getWebPageContextRealPath(PageContext pc,String p)
	{
		File realp = null;
		if(pc==null)
		{//尝试使用tomato平台信息
			if(isSole())
				throw new IllegalArgumentException("sole app must has PageContext input!") ;
			
			String tmpp = getTomatoWebappBase()+p ;
			realp = new File(tmpp) ;
			if(realp.exists())
				return realp ;
			
			return realp ;
		}
		// 尝试通过本webapp上下文获取绝对路径
		HttpServletRequest hsr = (HttpServletRequest) pc.getRequest();

		String tmpp = pc.getServletContext().getRealPath(p);
		File f = new File(tmpp);
		if (f.exists())
		{
			realp = f;
//			cxtRoot = WebRes
//					.getContextRootPath((HttpServletRequest) this.pageContext
//							.getRequest());
		}

		if (realp == null && !AppConfig.isSole() && p.startsWith("/"))
		{// tomato方式下,通过全局运行
			tmpp = AppConfig.getTomatoWebappBase();
			File tmpf = new File(tmpp, p);
			if (tmpf.exists())
			{
				realp = tmpf;
//				cxtRoot = "/";
			}
		}

		if (realp == null)
			throw new RuntimeException("no template found with path input=" + p);

		return realp ;
	}
	
	/**
	 * 读取一个页面路径中的文本内容
	 * @param pc
	 * @param p
	 * @param enc
	 * @return
	 */
	public static String readWebPageTxtByPath(PageContext pc,String p,String enc)
	{
		File f = getWebPageContextRealPath(pc,p) ;
		if(f==null)
			return null ;
		
		if(!f.exists())
			return null ;
		
			FileInputStream fis = null;
			if (enc == null || enc.equals(fis))
				enc = "UTF-8";

			try
			{
				fis = new FileInputStream(f);
				byte[] buf = new byte[(int) f.length()];
				fis.read(buf);
				String s = new String(buf, enc);
				//s = s.replaceAll("\\[\\$CXT_ROOT\\]", cxt_root);
				return s ;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
			finally
			{
				try
				{
					if (fis != null)
						fis.close();
				}
				catch (IOException ioe)
				{
				}
			}
		
	}
	
	private static Object locker = new Object();

	private static Element confRootEle = null;
	
	private static boolean bDebug = false;
	
	/**
	 * 
	 */
	private static boolean bAuthDefaultAllow = true;
	
	/**
	 * 判断页面缺省情况下是否需要登陆
	 */
	private static boolean bAuthDefaultLogin = false;
	
	private static String appTitle = "Tomato Server" ;
	
	/**
	 * 系统运行在分布式下的类型
	 */
	//private static RunDistributedType distributedType = RunDistributedType.not ;
	
	/**
	 * 系统运行在分布式代理下信息
	 */
	private static DistributedProxyInfo distributedProxyInfo = null ;
	
//	/**
//	 * 存放整数id对应的域名。
//	 * TBSserver在启动邮件服务器组件的时候，至少需要一个域名
//	 * 通过配置文件，形如<domain int32_id="0" name="souln.com.cn"/>
//		<domain int32_id="1" name="gojetsoft.com"/>
//		的配置来定义
//		
//		用户系统中每个用户也需要指定属于哪个域
//	 */
//	private static HashMap<Integer,String> id2domain = new HashMap<Integer,String>() ;
	
	/**
	 * 一些情况下，服务器需要知道自己是如何被外界访问的
	 * 本信息就是存储服务器自身的访问前缀
	 */
	private static ArrayList<String> httpBases = new ArrayList<String>() ;
	
	private static String appLang = "cn";
	
	private static String appCopyRight = "" ;

	public static Element loadConf()
	{
		if (confRootEle != null)
			return confRootEle;

		synchronized (locker)
		{
			if (confRootEle != null)
				return confRootEle;

			try
			{
				File f = getConfFile("app.xml");
				if (!f.exists())
				{
					f = getConfFile("app.conf");
					if(!f.exists())
						f = getConfFile("app.conf.xml");
					//throw new RuntimeException("no app.conf file found!");
				}
				
				if(!f.exists())
					throw new RuntimeException("no app.xml app.conf or app.conf.xml file found,last check file="+f.getAbsolutePath());

				DocumentBuilderFactory docBuilderFactory = null;
				DocumentBuilder docBuilder = null;
				Document doc = null;

				// parse XML XDATA File
				docBuilderFactory = DocumentBuilderFactory.newInstance();
				docBuilderFactory.setValidating(false);
				docBuilder = docBuilderFactory.newDocumentBuilder();

				doc = docBuilder.parse(f);

				confRootEle = doc.getDocumentElement();
				
				String tdata = confRootEle.getAttribute("tomato_data") ;
				if(Convert.isNotNullEmpty(tdata))
				{
					tdata = tdata.replace('\\', '/');
					File fp = null ;
					if(tdata.startsWith("/")||tdata.indexOf(':')>0)
					{
						fp = new File(tdata) ;
					}
					else
					{
						fp = new File(configFileBase+"/"+tdata) ;
					}
					
					fp.mkdirs();
					dataDirBase = fp.getCanonicalPath() ;
					dataDirBase = dataDirBase.replace('\\', '/');
					if(!dataDirBase.endsWith("/"))
						dataDirBase += "/" ;
				}
				
				bDebug = "true".equalsIgnoreCase(confRootEle.getAttribute("is_debug"));
				appTitle = confRootEle.getAttribute("title");
				if(appTitle==null||appTitle.equals(""))
					appTitle = "Tomato Biz Server" ;
				
				appCopyRight = confRootEle.getAttribute("copyright");
				if(appCopyRight==null)
					appCopyRight = "" ;
				
				appLang = confRootEle.getAttribute("lang");
				if(Convert.isNullOrEmpty(appLang))
					appLang = "cn";
				
				//String dist_tstr = confRootEle.getAttribute("distributed_type") ;
				
				initProxyInfo();
//				if(Convert.isNotNullEmpty(dist_tstr))
//				{
//					distributedType = RunDistributedType.valueOf(dist_tstr) ;
//					if(distributedType==RunDistributedType.distributed_proxy)
//					{
//						
//						
//					}
//				}
				
				
				Element userele = getConfElement("user");
				if(userele!=null)
				{
					bAuthDefaultAllow = !AppWebConfig.ATTRV_DENY.equalsIgnoreCase(userele.getAttribute("authorization_default"));
					bAuthDefaultLogin = "true".equalsIgnoreCase(userele.getAttribute("authorization_is_login"));
				}
				
				loadSystemConfig();
				
				return confRootEle;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				lastConfigError = e.getMessage()+"\r\n"+sw.toString();
				return null;
			}
		}
	}
	
	private static void initProxyInfo()
	{
		Element dp_ele = getConfElement("distributed_proxy");
		if(dp_ele!=null)
		{
			String proxyid = dp_ele.getAttribute("proxy_id");
			//检查id有效性
			if(proxyid==null||proxyid.equals(""))
			{
				System.out.println(" Alert::distributed_proxy conf element has no proxy_id") ;
				return ;
			}
			proxyid = proxyid.trim().toUpperCase() ;
			if(proxyid.length()>2||proxyid.length()<=0)
			{
				System.out.println(" Alert::distributed_proxy proxy_id must like XX (0-9 a-z) and length must less 2") ;
				return ;
			}
			
			for(int i = 0 ; i < proxyid.length() ; i ++)
			{
				int c = proxyid.charAt(i) ;
				if(c>='0'&&c<='9')
					continue ;
				if(c>='A'&&c<='Z')
					continue ;
				System.out.println(" Alert::distributed_proxy proxy_id must like XX (0-9 a-z) and length must less 2") ;
				return ;
			}
			
			distributedProxyInfo = new DistributedProxyInfo() ;
			distributedProxyInfo.proxyId = proxyid;
			distributedProxyInfo.proxyTitle = dp_ele.getAttribute("proxy_title") ;
			distributedProxyInfo.serverAddr = dp_ele.getAttribute("server_addr");
			distributedProxyInfo.userOrgPath = dp_ele.getAttribute("user_org_path");
		}
	}
	
	private static void loadSystemConfig() throws IOException
	{
		Element sysele = getConfElement("system");
		
		//获得本系统可能使用的域名--该属性一般情况下没有用处
//		Element[] edoms = XmlUtil.getSubChildElement(sysele, "domain") ;
//		if(edoms!=null)
//		{
//			for(Element edom:edoms)
//			{
//				int id = 0 ;
//				String int32_id = edom.getAttribute("int32_id") ;
//				if(Convert.isNotNullEmpty(int32_id))
//					id = Integer.parseInt(int32_id) ;
//				String n = edom.getAttribute("name") ;
//				if(Convert.isNotNullEmpty(n))
//				{
//					id2domain.put(id,n) ;
//				}
//			}
//		}
		
		Element[] hbdoms = XmlUtil.getSubChildElement(sysele, "http_base") ;
		if(hbdoms!=null)
		{
			for(Element hbd:hbdoms)
			{
				String u = hbd.getAttribute("url") ;
				if(Convert.isNotNullEmpty(u))
				{
					httpBases.add(u) ;
				}
			}
		}
		
		
		
		Element[] envs = XmlUtil.getSubChildElement(sysele, "env") ;
		if(envs!=null)
		{
			for(Element env:envs)
			{
				String n = env.getAttribute("name") ;
				if(Convert.isNullOrEmpty(n))
					continue ;
				
				String v = env.getAttribute("value");
				if(v==null)
					v = "" ;
				
				System.setProperty(n, v);
			}
		}
		
		Element[] libdirs = XmlUtil.getSubChildElement(sysele, "lib") ;
		StringBuilder sb = new StringBuilder() ;
		String pathsep = System.getProperty("path.separator") ;
		if(Convert.isNullOrEmpty(pathsep))
			pathsep = ";" ;
		
		if(libdirs!=null)
		{
			for(Element libd:libdirs)
			{
				String dir = libd.getAttribute("dir") ;
				if(Convert.isNullOrEmpty(dir))
					continue ;
				
				File f = new File(dir) ;
				if(!f.exists())
					continue ;
				
				if(!f.isDirectory())
					continue ;
				
				File[] fs = f.listFiles(new FilenameFilter(){

					public boolean accept(File dir, String name)
					{
						String n = name.toLowerCase() ;
						if(n.endsWith(".jar"))
							return true ;
						if(n.endsWith(".zip"))
							return true ;
						return false;
					}}) ;
				
				if(fs==null||fs.length<=0)
					continue ;
				
				for(File tmpf : fs)
				{
					sb.append(tmpf.getCanonicalPath()).append(pathsep) ;
				}
				
			}
		}
		
		sb.append(System.getProperty("java.class.path")) ;
		System.setProperty("java.class.path", sb.toString()) ;
		
		System.out.println("java.class.path="+System.getProperty("java.class.path")) ;
	}
	
	
	public static String getLastConfigError()
	{
		return lastConfigError;
	}
	
	public static boolean isDebug()
	{
		return bDebug ;
	}
	
	public static String getAppTitle()
	{
		return appTitle ;
	}
	
//	public static RunDistributedType getRunDistributedType()
//	{
//		return distributedType ;
//	}
	
	public static DistributedProxyInfo getDistributedProxyInfo()
	{
		return distributedProxyInfo ;
	}
	
	public static boolean isRunAsDistributedProxy()
	{
		return distributedProxyInfo!=null ;
	}
	
	/**
	 * 获得本服务器的外部访问请求的Http可能前缀
	 * @return
	 */
	public static List<String> getHttpBases()
	{
		return httpBases;
	}
	
	
//	public static Collection<String> getAppDomains()
//	{
//		return id2domain.values();
//	}
//	
//	public static String getDomain(int domainid)
//	{
//		String s = id2domain.get(domainid) ;
//		if(s!=null)
//			return s ;
//		
//		//get default
//		return id2domain.get(0) ;
//	}
//	
//	public static int getDomainId(String domain)
//	{
//		for(Map.Entry<Integer,String> id2m:id2domain.entrySet())
//		{
//			if(id2m.getValue().equalsIgnoreCase(domain))
//				return id2m.getKey() ;
//		}
//		
//		return -1 ;
//	}
	
	public static String getAppCopyRight()
	{
		return appCopyRight ;
	}
	
	public static String getAppLang()
	{
		return appLang ;
	}
	
	/**
	 * 根据Jsp PageContext获得对应的当前语言环境
	 * @param pc
	 * @return
	 */
	public static String getAppLang(PageContext pc)
	{
		String lan = null ;
		if(Convert.isNullOrEmpty(lan))
		{
			lan = pc.getRequest().getParameter("lang");
		}
		
		//AppConfig
		if(!Convert.isNullOrEmpty(lan))
			return lan ;
		
		return getAppLang() ;
	}
	
	/**
	 * 判定缺省情况下是否需要登陆
	 * @return
	 */
	public static boolean isAuthDefaultLogin()
	{
		return bAuthDefaultLogin ;
	}
	
	
	/**
	 * 判定页面是否需要检查是否要登陆才能访问
	 * 如果为true,则需要
	 * false,则不需要 - 
	 *   web.conf中可以对特定的页面进行check或uncheck设置
	 * @return
	 */
	public static boolean isAuthDefaultAllow()
	{
		return bAuthDefaultAllow ;
	}

	public static Element getConfElement(String name)
	{
		Element re = loadConf();
		NodeList nl = re.getElementsByTagName(name);
		if (nl == null)
			return null;

		return (Element) nl.item(0);
	}
	
}
