package com.dw.system.dict;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.dict.ioimpl.DBDDIO;
import com.dw.system.gdb.GFile;
import com.dw.system.util.Sorter;

/**
 * ����xml�ļ����ֵ������
 * 
 * ����һ����״�ṹ ÿ���ļ��Ǹ��ֵ��� �������ֵ�������ݽڵ�
 * ��һ���ֵ�����,��ÿ���ֵ����һ��Ψһid--Ҳ����ÿ���ֵ������ͨ��Ψһid���,�ֵ�������� �ӽڵ�
 * 
 * @author Jason Zhu
 */
public class DictManager
{
	
	private static HashMap<String,DataClassIO> name2io = new HashMap<String,DataClassIO>() ;
	static
	{
		name2io.put("db", new DBDDIO());
	}
	
	public static DataClassIO getDataClassIO(String n)
	{
		return name2io.get(n);
	}
	
	private static Object locker = new Object();

	private static DictManager dictMgr = null;

	public static DictManager getInstance()
	{
		if (dictMgr != null)
			return dictMgr;

		synchronized (locker)
		{
			if (dictMgr != null)
				return dictMgr;

			try
			{
				dictMgr = new DictManager();
				return dictMgr;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	static FilenameFilter DD_FF = new FilenameFilter()
	{

		public boolean accept(File dir, String name)
		{
			String n = name.toLowerCase();
			return n.startsWith("dd_") && n.endsWith(".xml");
		}
	};
	
	static class ModuleDictMap
	{
		HashMap<Integer, DataClass> id2class = new HashMap<Integer, DataClass>();

		HashMap<String, DataClass> name2class = new HashMap<String, DataClass>();
	}

	HashMap<Integer, DataClass> id2class = new HashMap<Integer, DataClass>();

	HashMap<String, DataClass> name2class = new HashMap<String, DataClass>();
	
	HashMap<String,ModuleDictMap> module2dict = new HashMap<String,ModuleDictMap>() ;

	private DictManager() throws Exception,
			IOException
	{
		String fp = GFile.getInstance().GetFilePath("[DICT]");
		if (fp == null)
		{
			fp = AppConfig.getDataDirBase() + "dict/";
			// fp = "./data/dict";
		}

		

		for (File tmpf : listDDFiles(fp))
		{
			DataClass dc = loadDataClass(null,tmpf);

			id2class.put(dc.getClassId(), dc);
			name2class.put(dc.getClassName(), dc);
		}
		
		//װ��ģ���е��ֵ�
		String webappr = AppConfig.getTomatoWebappBase() ;
		ArrayList<String> mus = AppConfig.getTomatoWebappModules() ;
		for(String mn:mus)
		{
			String mfp = webappr+"/"+mn+"/WEB-INF/dict/" ;
			
			ModuleDictMap mdm = module2dict.get(mn) ;
			if(mdm==null)
			{
				mdm = new ModuleDictMap() ;
				module2dict.put(mn,mdm) ;
			}
			
			for (File tmpf : listDDFiles(mfp))
			{
				DataClass dc = loadDataClass(mn,tmpf);

				mdm.id2class.put(dc.getClassId(), dc);
				mdm.name2class.put(dc.getClassName(), dc);
			}
		}
	}
	
	private File[] listDDFiles(String dir)
	{
		File f = new File(dir);
		if (!f.exists())
			return new File[0];

		if (!f.isDirectory())
			return new File[0];

		File[] fs = f.listFiles(DD_FF);
		if (fs == null)
			return new File[0];
		return fs ;
	}

	/**
	 * �����ļ�װ�ض�Ӧ��������
	 * ��װ�ص��಻�ᱻ����
	 * �÷���������ļ�ϵͳ
	 * 
	 * @param tmpf
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static DataClass loadDataClass(String modulen,File tmpf)
			throws Exception
	{
		if (!tmpf.exists())
			return null;
		String tmpfn = tmpf.getName();
		// class name
		String cn = tmpfn.substring(3, tmpfn.length() - 7);

		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		// parse XML XDATA File
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating(false);
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(tmpf);

		Element rootele = doc.getDocumentElement();

		DataClass dc = new DataClass(modulen,rootele);
		return dc;
	}
	
	
	public static DataClass loadLangDataClassByClass(Class c)
	throws Exception
	{
		String cnn = c.getCanonicalName();
		cnn = "/"+cnn.replace('.', '/')+".lang.xml" ;
		DataClass dc = loadDataClassByResPath(c,cnn) ;
		if(dc!=null)
		{
			String ulang = System.getProperty("user.language") ;
			if("zh".equals(ulang)||"cn".equals(ulang))
				ulang = "cn" ;
			else
				ulang = "en" ;
			
			dc.setDefaultLang(ulang) ;
		}
		
		return dc ;
	}
	/**
	 * ������Դ·��(���磺 /com/dw/xxx.xml),��ö�Ӧ���ֵ���
	 * @param resp
	 * @return
	 * @throws Exception
	 */
	public static DataClass loadDataClassByResPath(Class c,String resp)
	throws Exception
	{
		URL u = c.getResource(resp) ;
		if(u==null)
			return null ;
		
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		// parse XML XDATA File
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating(false);
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(u.toString());

		Element rootele = doc.getDocumentElement();

		DataClass dc = new DataClass(null,rootele);
		return dc;
	}

	public static DataClass loadDataClass(String modulen,byte[] cont)
			throws Exception
	{
		if (cont == null && cont.length <= 0)
			return null;

		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		// parse XML XDATA File
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setValidating(false);
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(new ByteArrayInputStream(cont));

		Element rootele = doc.getDocumentElement();

		DataClass dc = new DataClass(modulen,rootele);
		return dc;
	}

	public DataClass[] getAllDataClasses()
	{
		DataClass[] rets = new DataClass[id2class.size()];
		id2class.values().toArray(rets);
		return rets;
	}
	
	

	/**
	 * �������id��ö�Ӧ�������
	 * 
	 * @param cid
	 * @return
	 */
	public DataClass getDataClass(int cid)
	{
		return id2class.get(cid);
	}

	/**
	 * �������Ψһ����,�����Ķ���
	 * 
	 * @param classn
	 * @return
	 */
	public DataClass getDataClass(String classn)
	{
		return name2class.get(classn);
	}
	
	/**
	 * ��ȡ�����ֵ������ģ������
	 * @return
	 */
	public ArrayList<String> getAllHasDictModules()
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		for(Map.Entry<String, ModuleDictMap> n2mdm:module2dict.entrySet())
		{
			if(n2mdm.getValue().id2class.size()<=0)
				continue ;
			
			rets.add(n2mdm.getKey()) ;
		}
		return rets ;
	}
	
	/**
	 * ���ĳ��ģ���µ������ֵ���
	 * @param modulen
	 * @return
	 */
	public DataClass[] getAllDataClasses(String modulen)
	{
		ModuleDictMap mdm = module2dict.get(modulen) ;
		if(mdm==null)
			return null ;
		
		DataClass[] rets = new DataClass[mdm.id2class.size()];
		mdm.id2class.values().toArray(rets);
		return rets;
	}
	
	public DataClass getDataClass(String modulen,int cid)
	{
		ModuleDictMap mdm = module2dict.get(modulen) ;
		if(mdm==null)
			return null ;
		
		return mdm.id2class.get(cid) ;
	}
	
	public DataClass getDataClass(String modulen,String classn)
	{
		ModuleDictMap mdm = module2dict.get(modulen) ;
		if(mdm==null)
			return null ;
		
		return mdm.name2class.get(classn) ;
	}
	
	/**
	 * ����ģ���еĶ������,��ö�Ӧ������ģ������
	 * @param c
	 * @return
	 */
	private String getModuleNameByClass(Class c)
	{
		ClassLoader cl = c.getClassLoader();
		
		AppInfo ai = null;
		do
		{
			ai = CompManager.getInstance().getAppInfo(cl) ;
			if(ai!=null)
				break ;
			
			cl = cl.getParent() ;
		}while(cl!=null) ;
		
		if(ai==null)
			return null ;
		
		return ai.getContextName();
	}
	
	/**
	 * ����ģ���ж������,���ֵ�id���ģ���ж�����ֵ���
	 * @param moduleobjc
	 * @param cid
	 * @return
	 */
	public DataClass getModuleDataClass(Class moduleobjc,int cid)
	{
		String mn = getModuleNameByClass(moduleobjc) ;
		if(mn==null)
			return null ;
		return getDataClass(mn,cid) ;
	}
	
	/**
	 * ����ģ���ж������,���ֵ������ƻ��ģ���ж�����ֵ���
	 * @param moduleobjc
	 * @param classn
	 * @return
	 */
	public DataClass getModuleDataClass(Class moduleobjc,String classn)
	{
		String mn = getModuleNameByClass(moduleobjc) ;
		if(mn==null)
			return null ;
		return getDataClass(mn,classn) ;
	}
	
	
	/**
	 * ����http������ֵ���id��ö�Ӧ��ģ���ж����ֵ���
	 * @param req
	 * @param cid
	 * @return
	 */
	public DataClass getModuleDataClass(HttpServletRequest req,int cid)
	{
		String mn = Convert.getModuleNameByHttpReq(req);
		if(mn==null)
			return null ;
		return getDataClass(mn,cid) ;
	}
	
	/**
	 * ����Http������ֵ������ƻ��ģ���ж�����ֵ���
	 * @param req
	 * @param classn
	 * @return
	 */
	public DataClass getModuleDataClass(HttpServletRequest req,String classn)
	{
		String mn =  Convert.getModuleNameByHttpReq(req);
		if(mn==null)
			return null ;
		return getDataClass(mn,classn) ;
	}
	
}
