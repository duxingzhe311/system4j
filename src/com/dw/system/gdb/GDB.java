package com.dw.system.gdb;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.lang.reflect.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dw.comp.AppInfo;
import com.dw.comp.CompManager;
import com.dw.comp.ICompListener;
import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.conf.*;
import com.dw.system.gdb.conf.SqlItem.RuntimeItem;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.gdb.connpool.DBConnPool;
import com.dw.system.gdb.connpool.GDBConn;
import com.dw.system.gdb.connpool.GDBPreparedStatement;
import com.dw.system.gdb.connpool.IConnPool;
import com.dw.system.gdb.connpool.InnerConnPool;
import com.dw.system.gdb.datax.DataXIndex.JavaColumnAStructItem;
import com.dw.system.gdb.syn.*;
import com.dw.system.gdb.syn_client.ISynClientable;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.gdb.xorm.XORMUtil;

import com.dw.system.codedom.BoolExp;
import com.dw.system.logger.*;
import com.dw.system.util.IdCreator;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataWithFile;

public class GDB implements ICompListener
{
	private static ILogger log = LoggerManager.getLogger(GDB.class
			.getCanonicalName());

	private static Object locker = new Object();

	private static GDB gdb = null;

	public static final GDB getInstance() throws Exception
	{
		if (gdb != null)
			return gdb;

		synchronized (locker)
		{
			if (gdb != null)
				return gdb;

			gdb = new GDB();
			return gdb;
		}
	}

	/**
	 * ��Դ����·����Gdb������Ϣ
	 */
	private HashMap<String, Gdb> absResPath2Gdb = new HashMap<String, Gdb>();

	private String fileBaseDir = null;

	private File baseDirFile = null;

	private String innerDbPath = "./data/inner_db";

	HashMap<String, Func> key2Func = new HashMap<String, Func>();
	
	/**
	 * ��ŵ�ǰ�߳��е���������
	 * ���������
	 */
	private ThreadLocal<Connection> transConnInThreadLocal = new ThreadLocal<Connection>() ;

	private GDB() throws Exception
	{
		innerDbPath = System.getProperties().getProperty("user.dir")
				+ "/data/inner_db";

		// ���Լ�ע���CompManager,�Ա��ڹ���װ�غ�,�ܹ��Զ��Ĳ��������Ϣ
		CompManager.getInstance().registerCompListener(this);

		// String sysconffn = System.getProperties().getProperty("user.dir")
		// + "/xdata.conf";
		// File f = new File(sysconffn);
		// if (!f.exists())
		// {
		// throw new RuntimeException("no xdata.conf file found!");
		// }
		//
		// DocumentBuilderFactory docBuilderFactory = null;
		// DocumentBuilder docBuilder = null;
		// Document doc = null;
		//
		// // parse XML XDATA File
		// docBuilderFactory = DocumentBuilderFactory.newInstance();
		// docBuilderFactory.setValidating(false);
		// docBuilder = docBuilderFactory.newDocumentBuilder();
		//
		// doc = docBuilder.parse(f);

		Element gdbele = AppConfig.getConfElement("gdb");
		if (gdbele == null)
			return;

		String idbpath = gdbele.getAttribute("inner_db_path");
		if (idbpath != null && !idbpath.equals(""))
			innerDbPath = idbpath;

		//
		fileBaseDir = gdbele.getAttribute("file_base");
		if (fileBaseDir == null || fileBaseDir.equals(""))
		{
			fileBaseDir = AppConfig.getDataDirBase() + "gdb_file/";
			baseDirFile = new File(fileBaseDir);
		}
		else
		{
			if (fileBaseDir.startsWith("."))
			{
				baseDirFile = AppConfig.getRelatedFile(fileBaseDir);
			}
			else
			{
				baseDirFile = new File(fileBaseDir);
			}
		}
		// baseDirFile = AppConfig.getDataDirBase()+"gdb_file/";//
		// AppConfig.getRelatedFile(file_base);

		baseDirFile.mkdirs();
		fileBaseDir = baseDirFile.getCanonicalPath() + "/";

		// װ��xdata_xxx.xml�ļ�
		loadSqlXml(gdbele);

		initConnPool(gdbele);

		// loadModuleDBConnMap(gdbele);

		perpareKey2Func();
		
		try
		{
			//�����ֲ�ʽ֧��
			GDBLogManager.startDistributedSyn();
		}
		catch(Throwable syne)
		{
			syne.printStackTrace() ;
		}
	}

	private void initConnPool(Element gdbele) throws SQLException, GdbException
	{
		Element[] db_eles = XDataHelper.getCurChildElement(gdbele, "db");
		for (Element db_ele : db_eles)
		{
			String name = db_ele.getAttribute("name");
			if (name == null || name.equals(""))
				continue;

			DBType dbt = DBType.derby;
			String str_dbt = db_ele.getAttribute("type");
			if (str_dbt != null && !str_dbt.equals(""))
				dbt = DBType.valueOf(str_dbt);

			String driver = db_ele.getAttribute("driver");
			String url = db_ele.getAttribute("url");
			String usern = db_ele.getAttribute("username");
			String psw = db_ele.getAttribute("password");
			String init_num = db_ele.getAttribute("initnumber");
			String max_num = db_ele.getAttribute("maxnumber");
			DBConnPool db_cp = new DBConnPool(dbt, name, driver, url, usern,
					psw, init_num, max_num);

			loadDBConnMap(db_cp, db_ele);

			String strdef = db_ele.getAttribute("default");
			if ("true".equalsIgnoreCase(strdef) || "1".equals(strdef))
				ConnPoolMgr.defaultConnPool = db_cp;

			ConnPoolMgr.dbname2pool.put(name, db_cp);
		}

		if (ConnPoolMgr.defaultConnPool == null)
		{
			ConnPoolMgr.defaultConnPool = createSystemInnerDB();
		}
	}

	private void loadDBConnMap(IConnPool icp, Element dbele)
			throws GdbException
	{
		Element[] for_eles = XDataHelper.getCurChildElement(dbele, "for");
		if (for_eles == null || for_eles.length <= 0)
			return;

		for (Element tmpe : for_eles)
		{
			String mn = tmpe.getAttribute("gdb_res_path");
			if (mn == null || mn.equals(""))
				continue;

			Gdb g = this.absResPath2Gdb.get(mn);
			if (g != null)
				g.usingDBName = icp.getDBName();
		}
	}

	/**
	 * ʹ��ϵͳ�ڲ��Դ������ݿ�
	 * 
	 * @return
	 */
	private IConnPool createSystemInnerDB()
	{
		return InnerConnPool.getInnerConnPool(innerDbPath);
	}

	static FilenameFilter fnf = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			if (name == null)
				return false;
			name = name.toLowerCase();
			return (name.startsWith("xorm_") || name.startsWith("xdata_") || name
					.startsWith("gdb_"))
					&& name.endsWith(".xml");
		}
	};

	/**
	 * ���������ļ�ָ�������г���Ŀ¼.jar,classes�� ������װ�������xdata_xx.xml�ļ�
	 * 
	 * @param gdbele
	 * @throws IOException
	 */
	private void loadSqlXml(Element gdbele) throws IOException
	{
		ArrayList<File> fss = new ArrayList<File>();

		String solewebapproot = AppConfig.getSoleWebAppRoot();
		if (!Convert.isNullOrEmpty(solewebapproot))
		{
			if (!solewebapproot.endsWith("/"))
				solewebapproot += "/";

			fss.add(new File(solewebapproot + "WEB-INF/classes/"));
			fss.add(new File(solewebapproot + "WEB-INF/lib/"));
		}

		Element[] lsx_eles = XDataHelper.getCurChildElement(gdbele,
				"load_sql_xml");

		for (Element lsx_ele : lsx_eles)
		{
			String path = lsx_ele.getAttribute("path");
			if (path == null || (path = path.trim()).equals(""))
				continue;

			fss.add(AppConfig.getRelatedFile(path));
		}
		File[] fs = new File[fss.size()];
		fss.toArray(fs);
		loadSqlXmlInFiles(null, fs);
	}

	private ArrayList<Gdb> loadSqlXmlInFiles(AppInfo ai, File[] fs)
			throws IOException
	{
		XDataHelper.FileDataItem[] fdis = XDataHelper.readFiles(fs, fnf);
		ArrayList<Gdb> gdbs = new ArrayList<Gdb>();
		for (XDataHelper.FileDataItem fdi : fdis)
		{
			try
			{
				log.info(" xdata.gdb load..." + fdi.toString());
				ByteArrayInputStream bais = new ByteArrayInputStream(fdi
						.getContent());
				Gdb g = new Gdb(ai, fdi.getAbsResPath(), fdi.getName(), bais);
				absResPath2Gdb.put(g.getAbsResPath(), g);
				gdbs.add(g);
			}
			catch (Exception e)
			{
				log.error(e);
			}
		}
		return gdbs;
	}

	public void onCompFinding(AppInfo ci)
	{
		String realpath = ci.getRealPath();
		if (!realpath.endsWith("/"))
			realpath += "/";

		File[] fs = new File[2];
		fs[0] = new File(realpath + "WEB-INF/classes/");
		fs[1] = new File(realpath + "WEB-INF/lib/");

		try
		{
			List<Gdb> gdbs = loadSqlXmlInFiles(ci, fs);
			for (Gdb g : gdbs)
			{
				perpareKey2Func(g);
			}
		}
		catch (Exception e)
		{
			log.error(e);
		}
	}
	

	/**
	 * ����key��Func��ӳ�䣬Ϊ������׼��
	 * 
	 */
	private void perpareKey2Func()
	{
		for (Gdb g : absResPath2Gdb.values())
		{
			perpareKey2Func(g);

		}
	}

	private void perpareKey2Func(Gdb g)
	{
		for (Module m : g.getModules())
		{
			for (Func f : m.getFuncs())
			{
				// System.out.println("gdb func uk["+f.getUniqueKey());
				key2Func.put(f.getUniqueKey(), f);
			}
		}
	}
	
	/**
	 * ��setObject֮ǰ������ֵ����Ԥ�����Ա����������
	 * @param o
	 * @return
	 */
	static Object prepareObjVal(Object o)
	{
		if(o==null)
			return null;
		
		if ((o instanceof java.util.Date)
				&& !(o instanceof java.sql.Timestamp))
		{
			return new java.sql.Timestamp(((java.util.Date) o)
					.getTime());
		}
		
		return o ;
	}

	/**
	 * �Զ������ַ������͵�����ֵ--��ֵ��˳���С��������
	 * ���Ӧ��������Proxy�ˣ����Զ����ɵ�ֵ�Զ�������idǰ׺
	 * @return
	 */
	public static String newAutoStringKeyVal()
	{
		AppConfig.DistributedProxyInfo pi = AppConfig.getDistributedProxyInfo() ;
		if(pi==null)
		{
			return IdCreator.newSeqId() ;
		}
		else
		{
			return pi.proxyId + IdCreator.newSeqId() ;
		}
	}
	
	/**
	 * �����Զ����ɵ��ַ���ֵ������ʱ��ͼ���������ַ���ֵ
	 * 
	 * ��ֵ�������������ֱ�ʶ������Ҫע��ֲ�ʽ�����µ��ظ�
	 * 
	 * @param keyv
	 * @return
	 */
	public static String readDateAndCount(String keyv)
	{
		return null ;
	}
	// /**
	// * ����ģ���ö�Ӧ�����ݿ����ӳ�
	// *
	// * @param f
	// * @return
	// */
	// private IConnPool getConnPool(Module m)
	// {
	// String modulen = m.getName();
	// IConnPool cp = module2pool.get(modulen);
	// if (cp != null)
	// return cp;
	//
	// return defaultConnPool;
	// }

	/**
	 * �������ݿ���ܷ���
	 * 
	 * @param uniquekey
	 * @param parms
	 * @return
	 */
	public DBResult accessDB(String uniquekey, Hashtable parms)
		throws GdbException
	{
			return accessDB(null,uniquekey, parms);
	}
	
	public DBResult accessDB(String dbname,String uniquekey, Hashtable parms)
			throws GdbException
	{
		return accessDB(dbname,uniquekey, parms, 0, -1,null,null);
	}

//	public DBResult T_accessDB(String uniquekey,Hashtable parms)
//		throws Exception
//	{
//		Func f = key2Func.get(uniquekey);
//		if (f == null)
//			throw new GdbException("cannot find Func with key [" + uniquekey
//					+ "],make sure it is to be loaded!");
//
//		Module m = f.getBelongTo();
//		//Gdb g = m.getBelongTo();
//
//		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;
//		if(tit==null)
//			throw new RuntimeException("no trans obj in thread,please use create first!") ;
//		
//		
//		Connection conn = tit.getConn(f);
//
//		try
//		{
//			
//			return accessDBInConn(tit.getDBType(f), conn, uniquekey, parms, 0,
//					-1,null);
//		}
//		catch (Exception eee)
//		{
//			if(log.isErrorEnabled())
//			{
//				log.error(eee) ;
//			}
//			
//			throw new GdbException(uniquekey + "\n" + cur_sql.toString(), eee);
//		}
//		
//	}
	/**
	 * ֧�ֲ�ѯ��ҳ�����ݿ����
	 * 
	 * @param uniquekey
	 * @param parms
	 * @param pageidx
	 * @param pagesize
	 * @return
	 * @throws GdbException
	 */
	public DBResult accessDBPage(String uniquekey, Hashtable parms,
			int pageidx, int pagesize) throws GdbException
	{
		return accessDBPage(null,uniquekey, parms,
				pageidx, pagesize);
	}
	
	public DBResult accessDBPage(String dbname,String uniquekey, Hashtable parms,
			int pageidx, int pagesize) throws GdbException
	{
		return accessDBPage(dbname,uniquekey, parms,
				null,pageidx, pagesize);
	}
	
	/**
	 * ��ҳ�������ݿ�����
	 * 
	 * @param dbname
	 * @param uniquekey
	 * @param parms
	 * @param more_where_cond ���ӵ�sql����
	 * @param pageidx
	 * @param pagesize
	 * @return
	 * @throws GdbException
	 */
	public DBResult accessDBPage(String dbname,String uniquekey, Hashtable parms,
			String more_where_cond,int pageidx, int pagesize) throws GdbException
	{
		return accessDB(dbname,uniquekey, parms, pageidx * pagesize, pagesize,null,more_where_cond);
	}

	/**
	 * ��ѯ�������ݿ⣬���ѽ��ӳ���ORMaping�����б�
	 * 
	 * @param uniquekey
	 * @param parms
	 * @param objc
	 * @return
	 */
	public List accessDBAsObjList(String uniquekey, Hashtable parms, Class objc)
			throws Exception
	{
		DBResult dbr = accessDB(uniquekey, parms);
		return dbr.transTable2ObjList(0, objc);
	}

	public List accessDBPageAsObjList(String uniquekey, Hashtable parms,
			Class objc, int pageidx, int pagesize) throws Exception
	{
		return accessDBPageAsObjList(uniquekey, parms,
				objc, pageidx, pagesize,null) ;
	}
	
	public List accessDBPageAsObjList(String uniquekey, Hashtable parms,
			Class objc,int pageidx, int pagesize,DataOut dout) throws Exception
	{
		return accessDBPageAsObjList(uniquekey, parms,
				objc,null, pageidx, pagesize,dout);
	}
	
	/**
	 * ���ݷ��ʵĲ�ѯ���,ֱ����Ϊobjcָ����������Ķ����б���
	 * @param uniquekey
	 * @param parms
	 * @param objc
	 * @param more_where_cond ���ӵ�sql����
	 * @param pageidx
	 * @param pagesize
	 * @param dout
	 * @return
	 * @throws Exception
	 */
	public List accessDBPageAsObjList(String uniquekey, Hashtable parms,
			Class objc,String more_where_cond, int pageidx, int pagesize,DataOut dout) throws Exception
	{
		DBResult dbr = accessDB(null,uniquekey, parms, pageidx * pagesize, pagesize,null,more_where_cond);
		if(dout!=null)
		{
			dout.pageSize = pagesize ;
			dout.pageCur = pageidx ;
		}
		return dbr.transTable2ObjList(0, objc,dout);
	}
	
	
	public void accessDBWithSelectCallback(String uniquekey, Hashtable parms,
			int pageidx,int pagesize,IDBSelectCallback cb)
		throws Exception
	{
		accessDB(null,uniquekey, parms, pageidx * pagesize, pagesize,cb,null);
	}

	/**
	 * ���ݷ��ʵĲ�ѯ���,ֱ����ΪXORM�����б���� ����,��Ӧ�����Ӧ��ʹ�õ� XORM���� ���Ӧ�� �б���
	 * 
	 * @param uniquekey
	 * @param parms
	 * @param xormc
	 * @param pageidx
	 * @param pagesize
	 * @return
	 * @throws Exception
	 */
	public List accessDBPageAsXORMObjList(String uniquekey, Hashtable parms,
			Class xormc, int pageidx, int pagesize) throws Exception
	{
		return accessDBPageAsXORMObjList(uniquekey,  parms,
				xormc, pageidx, pagesize,null) ;
	}
	
	
	public List accessDBPageAsXORMObjList(String uniquekey, Hashtable parms,
			Class xormc, int pageidx, int pagesize,DataOut dout)
	 throws Exception
	{
		return accessDBPageAsXORMObjList(uniquekey, parms,
				xormc,null, pageidx, pagesize,dout);
	}
	
	/**
	 * �������ݿ⣬���ѽ������ΪXORM�����б�
	 * @param uniquekey
	 * @param parms
	 * @param xormc
	 * @param more_where_cond ���uniquekey��Ӧ��sql����Ҫ�����sql��������Ӵ�����
	 *   �˲���ֵ���� and XXX , or XXX �ȡ���������sql����
	 * @param pageidx
	 * @param pagesize
	 * @param dout
	 * @return
	 * @throws Exception
	 */
	public List accessDBPageAsXORMObjList(String uniquekey, Hashtable parms,
			Class xormc,String more_where_cond, int pageidx, int pagesize,DataOut dout) throws Exception
	{
		DBResult dbr = accessDB(null,uniquekey, parms, pageidx * pagesize, pagesize,null,more_where_cond);
		if (log.isDebugEnabled())
		{
			DataTable dt = dbr.getResultFirstTable() ;
			String tmps = "" ;
			if(dt!=null)
				tmps = dt.toString() ;
			log.debug("accessDBPageAsXORMObjList res table--\r\n"
					+ dt);
		}
		if(dout!=null)
		{
			dout.pageSize = pagesize ;
			dout.pageCur = pageidx ;
		}
		return dbr.transTable2XORMObjList(0, xormc,dout);
	}

	/**
	 * ��ѯ���ݿ⣬����ɨ�����������У����ɶ�Ӧ��XORM���󣬲��ص�
	 * @param uniquekey
	 * @param parms
	 * @param xormc
	 * @param pageidx
	 * @param pagesize
	 * @param cb
	 * @throws Exception
	 */
	public void accessDBWithXORMObjSelectCallback(String uniquekey, Hashtable parms,
			Class xormc, int pageidx, int pagesize,IDBSelectObjCallback cb) throws Exception
	{
		accessDBWithXORMObjSelectCallback(null,uniquekey, parms,
				xormc, pageidx, pagesize,cb);
	}
	
	public void accessDBWithXORMObjSelectCallback(String dbname,String uniquekey, Hashtable parms,
			Class xormc, int pageidx, int pagesize,IDBSelectObjCallback cb) throws Exception
	{
		XORMSelCallback scb = new XORMSelCallback(xormc,cb) ;
		accessDB(dbname,uniquekey, parms, pageidx * pagesize, pagesize,scb,null);
	}
	/**
	 * ���ݷ��ʵĲ�ѯ���,ֱ�Ӱѽ�����еĵ�һ����ΪXORM�������
	 * 
	 * @param uniquekey
	 * @param parms
	 * @param xormc
	 * @return
	 * @throws Exception
	 */
	public Object accessDBAsXORMObj(String uniquekey, Hashtable parms,
			Class xormc) throws Exception
	{
		List ll = accessDBPageAsXORMObjList(uniquekey, parms, xormc, 0, -1);
		if (ll == null || ll.size() <= 0)
			return null;
		return ll.get(0);
	}

	/**
	 * ����������󼯶�Ӧ���࣬����ORMapӳ�䣬�Զ���������������������ݿ�
	 * 
	 * @param uniquekey
	 * @param objc
	 * @param objparm
	 * @return
	 */
	public DBResult accessDBWithObjParm(String uniquekey, Class t,
			Object objparm) throws Exception
	{
		Func f = key2Func.get(uniquekey);
		if (f == null)
			throw new GdbException("cannot find Func with key [" + uniquekey
					+ "],make sure it is to be loaded!");

		ORMap sm = f.getBelongTo().getBelongTo().getORMap(t.getCanonicalName());
		if (sm == null)
			throw new GdbException("Cannot find SelectMap with type="
					+ t.getCanonicalName());

		Hashtable ht = new Hashtable();
		for (Method m : t.getDeclaredMethods())
		{
			String n = m.getName();
			if (!n.startsWith("get"))
				continue;

			n = n.substring(3);
			String coln = sm.getColumnByProperty(n);
			if (coln == null)
				continue;

			String pn = '@' + coln;
			InParam ip = f.getInParam(pn);
			if (ip == null)
			{
				pn = '$' + coln;
				ip = f.getInParam(pn);
			}
			if (ip == null)
				continue;

			m.setAccessible(true);
			Object v = m.invoke(objparm, (Object[]) null);
			if (v != null)
				ht.put(pn, v);
		}

		return accessDB(uniquekey, ht);
	}

	/**
	 * ����XORM������Ϊ�������,�������ݿ��Ӧ��Func ��Ҫ����XORM���󹹳ɵ�Hashtable����,��������� Func����Ĳ���
	 * 
	 * @param uniquekey
	 * @param xorm_obj
	 * @return
	 * @throws Exception
	 */
	public DBResult accessDBWithXORMObjParm(String uniquekey, Object xorm_obj)
			throws Exception
	{
		Func f = key2Func.get(uniquekey);
		if (f == null)
			throw new GdbException("cannot find Func with key [" + uniquekey
					+ "],make sure it is to be loaded!");

		Hashtable ht = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj, "@");

		return accessDB(uniquekey, ht);
	}

	/**
	 * ֧�ַ�ҳ�Ĳ�ѯ�ķ������ݿⷽ������ǰ��Ҫ����ʵ����ý����ǵ���Select Sql���
	 * 
	 * @param uniquekey
	 * @param parms
	 * @param idx
	 *            ���ؽ����ʼ��¼��λ��
	 * @param count
	 *            ���ؼ�¼����&lt;0 ��ʾ����ȫ��
	 * @return
	 * @throws GdbException
	 */
	private DBResult accessDB(String dbname,String uniquekey, Hashtable parms, int idx,
			int count,IDBSelectCallback cb,String more_sql_cond) throws GdbException
	{
		Func f = key2Func.get(uniquekey);
		if (f == null)
			throw new GdbException("cannot find Func with key [" + uniquekey
					+ "],make sure it is to be loaded!");

		Module m = f.getBelongTo();
		Gdb g = m.getBelongTo();

		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;
		
		if(dbname==null||dbname.equals(""))
			dbname = f.getRealUsingDBName() ;
		
		IConnPool cp = ConnPoolMgr.getConnPool(dbname);

		DBType dbt = cp.getDBType();
		FuncContentForDB fcdb = f.getFuncContent(dbt);
		// �ж�������Ƿ��и������ݵ����
		boolean b_change_data = fcdb.isChangeData();
		ArrayList<IOperItem> sqlis = fcdb.getOperItems();

		Connection conn = null;

		boolean b_autocommit = true;

		long c_st = System.currentTimeMillis();
		StringBuilder cur_sql = new StringBuilder();

		try
		{
			if(tit!=null)
			{//in trans
				conn = tit.getConn(f.getRealUsingDBName()) ;
			}
			else
			{
				conn = cp.getConnection();

				if (b_change_data)
				{
					b_autocommit = conn.getAutoCommit();
					conn.setAutoCommit(false);
				}
			}

			DBResult dbr = accessDBInConn(dbt, conn, uniquekey, parms, idx,
					count,cb,more_sql_cond);

			if(tit==null)
			{
				if (b_change_data)
				{
					conn.commit();
					conn.setAutoCommit(b_autocommit);
				}
	
				cp.free(conn);
				conn = null;
			}
			return dbr;
		}
		catch (Exception eee)
		{
			if(log.isErrorEnabled())
			{
				log.error(eee) ;
			}
			
			throw new GdbException(uniquekey + "\n" + cur_sql.toString(), eee);
		}
		finally
		{
			if(tit==null)
			{
				if (conn != null)
				{// ������˵�����г���
					if (b_change_data)
					{// ����޸�����,��rollback
						try
						{
							conn.rollback();
							conn.setAutoCommit(b_autocommit);
						}
						catch (Throwable sqle)
						{
							if(log.isErrorEnabled())
								log.error(sqle) ;
						}
					}
					cp.free(conn);
				}
			}

			if (log.isDebugEnabled())
			{
				log.debug("Access DB Cost="
						+ (System.currentTimeMillis() - c_st));
			}
		}
	}

	/**
	 * Ϊ��������һ���������ṩ֧��
	 * 
	 * @param conn
	 * @param uniquekey
	 * @param parms
	 * @param idx
	 * @param count
	 * @return
	 * @throws Exception
	 */
	DBResult accessDBInConn(DBType dbt, Connection conn,
			String uniquekey, Hashtable parms, int idx, int count,
			IDBSelectCallback cb,String more_sql_cond)
			throws Exception
	{
		Func f = key2Func.get(uniquekey);
		if (f == null)
			throw new GdbException("cannot find Func with key [" + uniquekey
					+ "],make sure it is to be loaded!");

		for (InParam nip : f.getNeedParams())
		{
			if (parms == null || parms.get(nip.getName()) == null)
				throw new GdbException("param with name=" + nip.getName()
						+ " in [" + uniquekey + "] cannot be null!");
		}

		// Module m = f.getBelongTo();
		// Gdb g = m.getBelongTo();

		FuncContentForDB fcdb = f.getFuncContent(dbt);
		ArrayList<IOperItem> sqlis = fcdb.getOperItems();

		long c_st = System.currentTimeMillis();
		StringBuilder cur_sql = new StringBuilder();

		// f.getInParam(uniquekey)
		DBResult dbr = new DBResult(f);

		int sss = sqlis.size() ;
		for (int sqlidx = 0 ; sqlidx < sss; sqlidx++)
		{
			IOperItem si = sqlis.get(sqlidx);
			String morecond = null ;
			
			BoolExp be = si.getIfBoolExp();
			if (be != null && !be.checkWithParam(parms))
				continue;// �ж�sql����е�if�����Ƿ�����

			if(morecond==null)//��һ�����е���䡾TODO:�Ժ���Ҫ�Զ��������и�ϸ�µĴ���
				morecond = more_sql_cond;
			
			DataTable dt = null;
			if (si instanceof SqlItem)
			{
				dt = doSqlItem(sqlidx,uniquekey, cur_sql, parms, idx, count, dbt,
						conn, dbr, (SqlItem) si,cb,morecond);
			}
			else if (si instanceof FileItem)
				dt = doFileItem((FileItem) si, parms);

			if (dt != null)
			{
				List<SqlItem.ParamSetter> paramsetters = si.getParamSetters();
				if (paramsetters != null)
				{// ���ݱ��ν��,�Ͳ�����������,��������������޸�
					for (SqlItem.ParamSetter tmpps : paramsetters)
					{
						Object tmpv = null;
						DataRow dr = dt.getRow(tmpps.getResultTableRow());
						if (dr != null)
						{
							tmpv = dr.getValue(tmpps.getResultTableCol());
						}
						if (tmpv != null)
							parms.put(tmpps.getParamName(), tmpv);
						else
							parms.remove(tmpps.getParamName());
					}
				}
			}
		}
		return dbr;
	}

	private static void printInParam(Hashtable ht)
	{
		if (ht == null)
			return;

		for (Object o : ht.keySet())
		{
			Object ov = ht.get(o);
			if (ov instanceof Date)
			{
				Date d = (Date) ov;
				log.info(o.toString() + "=" + Convert.toFullYMDHMS(d));
			}
			else
			{
				log.info(o.toString() + "=" + ov.toString());
			}
		}
	}

	/**
	 * ���ļ�����
	 * 
	 * @param fi
	 * @param parms
	 * @return
	 * @throws IOException
	 */
	private DataTable doFileItem(FileItem fi, Hashtable parms)
			throws IOException
	{
		FileItem.OperType ot = fi.getOperType();

		String idv = fi.getIdVar();
		Long n = (Long) parms.get(idv);
		if (n == null)
			throw new IllegalArgumentException("cannot find param with name="
					+ idv);

		long fid = n.longValue();
		if (fid <= 0)
			throw new IllegalArgumentException("illegal FileItem id=" + fid);

		String fb = fi.getBase();

		String fp = autoId2Path(fb, fid);
		if (ot == FileItem.OperType.write)
		{
			String vv = fi.getValVar();
			byte[] cont = (byte[]) parms.get(vv);
			if (cont == null)
				throw new IllegalArgumentException(
						"cannot find param with name=" + vv);

			FileOutputStream fos = null;
			try
			{
				File f = new File(fp);
				File df = f.getParentFile();
				if (!df.exists())
					df.mkdirs();

				fos = new FileOutputStream(f);
				fos.write(cont);
				fos.flush();

				DataTable dt = new DataTable();
				dt.addColumn(new DataColumn("res", Integer.class));
				DataRow dr = dt.createNewRow();
				dr.putValue(0, 1);
				return dt;
			}
			finally
			{
				if (fos != null)
					fos.close();
			}
		}

		if (ot == FileItem.OperType.read)
		{
			File f = new File(fp);
			DataTable dt = new DataTable();
			if (!f.exists())
			{
				return dt;
			}

			FileInputStream fis = null;
			try
			{
				byte[] buf = new byte[(int) f.length()];
				fis = new FileInputStream(f);
				fis.read(buf);

				dt.addColumn(new DataColumn("res", byte[].class));
				DataRow dr = dt.createNewRow();
				dr.putValue(0, buf);
				return dt;
			}
			finally
			{
				if (fis != null)
					fis.close();
			}
		}

		if (ot == FileItem.OperType.delete)
		{
			File f = new File(fp);
			int res = 0;
			if (f.exists())
			{
				f.delete();
				res = 1;
			}

			DataTable dt = new DataTable();
			dt.addColumn(new DataColumn("res", Integer.class));
			DataRow dr = dt.createNewRow();
			dr.putValue(0, res);
			return dt;
		}

		throw new RuntimeException("unknown oper type in FileItem");
	}

	private static final long MAX_DIR_NUM = 1000;

	private static final int FILENAME_LEN = 3;

	private static final long DIR_LEVEL = 3;

	private static long tmplll = 1000;

	private static long MAX_ID = tmplll * tmplll * tmplll * tmplll - 1;

	private String autoId2Path(String dd, long autoid)
	{
		if (autoid <= 0 || autoid > MAX_ID)
			throw new IllegalArgumentException("invalid autoid=" + autoid);

		String tmps = "";
		long tmpl = autoid;
		for (int i = 0; i <= DIR_LEVEL; i++)
		{
			String fdname = "" + tmpl % MAX_DIR_NUM;
			// ������ǰ��λ��������в�0
			int c = FILENAME_LEN - fdname.length();
			for (int j = 0; j < c; j++)
			{
				fdname = "0" + fdname;
			}

			tmpl = tmpl / MAX_DIR_NUM;

			tmps = "/" + fdname + tmps;
		}

		return fileBaseDir + dd + "/" + tmps;
	}

	/**
	 * 
	 * @param sqlidx
	 * @param uniquekey
	 * @param cur_sql
	 * @param parms
	 * @param idx
	 * @param count
	 * @param dbt
	 * @param conn
	 * @param dbr
	 * @param si
	 * @param cb
	 * @param more_sql_cond �����Ӧ����ǵ�����ѯ��䣬��������������sql����
	 * 	���в�ѯ
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws Exception
	 */
	private DataTable doSqlItem(int sqlidx,String uniquekey, StringBuilder cur_sql,
			Hashtable parms, int idx, int count, DBType dbt, Connection conn,
			DBResult dbr, SqlItem si,IDBSelectCallback cb,String more_sql_cond) throws SQLException, IOException,
			Exception
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
		try
		{
			RuntimeItem ri = si.getRuntimeItem(dbt, parms);
			String sql = ri.getJdbcSql();
			if(Convert.isNotNullTrimEmpty(more_sql_cond))
			{
				String and_or = "and" ;
				String msc = more_sql_cond.toLowerCase().trim();
				if(msc.startsWith("and"))
					more_sql_cond = more_sql_cond.substring(3).trim() ;
				else if(msc.startsWith("or"))
				{
					and_or = "or" ;
					more_sql_cond = more_sql_cond.substring(2).trim() ;
				}
				
				sql = sql.trim() ;
				String tmps = sql.toLowerCase();
				int pw = tmps.indexOf("where") ;
				if(pw<0)
				{
					sql += "where "+more_sql_cond;
				}
				else
				{
					sql = sql.substring(0,pw)
						+" where ("+sql.substring(pw+5)+")"
						+and_or+" "+more_sql_cond;
				}
			}
				
			cur_sql.delete(0, cur_sql.length());
			cur_sql.append(sql);
			if (log.isDebugEnabled())
			{
				log.debug("gdb access [" + uniquekey + "]:" + sql);
				log.debug("input param------------->\n");
				printInParam(parms);
			}
			
			long run_cost_st = System.currentTimeMillis() ;
			
			CallType ct = si.getCallType();
			if (ct == CallType.sql || ct == CallType.auto_fit)
			{
				ps = conn.prepareStatement(sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			}
			else if (ct == CallType.pro)
			{
				ps = conn.prepareCall(sql);
				// TODO �Ժ����Ӷ����������֧��
			}
			else
			{
				ps = conn.prepareStatement(sql);
			}

			// ���ò���
			prepareStatement(ps, ri, parms);

			DataTable dt = null;
			ExeType et = ri.getExeType();
			if (et == ExeType.dataset || et == ExeType.select
					|| et == ExeType.scalar)
			{
				if (count > 0)
				{// ��Ҫ֧�ַ�ҳ
					ps.setMaxRows(idx + count);
					// if (sql.fetchSize > 0)//fetch size
					// ��ʾ�����һ�η������ݿ�ȡ�ض�������¼
					// rs.setFetchSize (sql.fetchSize) ;
				}

				rs = ps.executeQuery();
				dt = dbr.appendResultSet(si.getResultTableName(),sqlidx,rs, idx, count,cb);

			}
			else if (et == ExeType.update || et == ExeType.insert
					|| et == ExeType.delete)
			{
				if (count > 0)
					throw new GdbException("not page select db access!");

				dbr.rowsAffected = ps.executeUpdate();
				dt = dbr.appendRowsAffected(dbr.rowsAffected);
			}
			else
			{
				if (count > 0)
					throw new GdbException("not page select db access!");

				ps.execute();
			}
			
			si.setRunCostStEt(run_cost_st,System.currentTimeMillis()) ;

			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			ps.close();
			ps = null;
			return dt;
		}
		finally
		{
			if (rs != null)
				rs.close();

			if (ps != null)
				ps.close();
		}
	}

	private void prepareStatement(PreparedStatement ps, RuntimeItem ri,
			Hashtable parms) throws IOException, SQLException, GdbException
	{
		ArrayList<InParam> rtpns = ri.getRtParams();
		if (rtpns != null)
		{
			int pnn = rtpns.size();
			for (int i = 0; i < pnn; i++)
			{
				InParam ip = rtpns.get(i);
				String pName = ip.getName();
				// System.out.println("ps pn=====" + pName);
				int type = ip.getJdbcType();
				Object pValue = parms.get(ip.getName());

				if (pValue == null)
					pValue = ip.getDefaultVal();

				if (pValue == null)
				{
					ps.setNull(i + 1, type);
					continue;
				}

				switch (type)
				{
				case Types.BLOB:
				case Types.LONGVARBINARY:
					InputStream ins = null;
					if (pValue instanceof InputStream)
						ins = (InputStream) pValue;
					else if (pValue instanceof byte[])
						ins = new ByteArrayInputStream((byte[]) pValue);
					else
						ins = new ByteArrayInputStream(String.valueOf(pValue)
								.getBytes("UTF8"));

					ps.setBinaryStream(i + 1, ins, ins.available());
					break;
				case Types.CLOB:
				case Types.LONGVARCHAR:
					// Reader reader = null ;
					String valueString = String.valueOf(pValue);
					/*
					 * if (pValue instanceof Reader) { reader = (Reader) pValue ; }
					 * 
					 * else
					 */
					StringReader reader = new StringReader(valueString);

					ps.setCharacterStream(i + 1, reader, valueString.length());
					break;
				case Types.DATE:
					java.sql.Date theDate = null;
					if (pValue instanceof java.util.Date)
						theDate = new java.sql.Date(((java.util.Date) pValue)
								.getTime());
					else if (pValue instanceof java.util.Calendar)
						theDate = new java.sql.Date(
								((java.util.Calendar) pValue).getTime()
										.getTime());
					else
						throw new GdbException("Unsupport Parameter Type ["
								+ pValue.getClass().getName() + "] value ["
								+ pValue + "], can't convert it into Date ["
								+ pName + "]");
					ps.setDate(i + 1, theDate);
					break;
				case Types.TIME:
					java.sql.Time theTime = null;
					if (pValue instanceof java.util.Date)
						theTime = new java.sql.Time(((java.util.Date) pValue)
								.getTime());
					else if (pValue instanceof java.util.Calendar)
						theTime = new java.sql.Time(
								((java.util.Calendar) pValue).getTime()
										.getTime());
					else
						throw new GdbException("Unsupport Parameter Type ["
								+ pValue.getClass().getName() + "] value ["
								+ pValue + "], can't convert it into Time ["
								+ pName + "]");
					ps.setTime(i + 1, theTime);
					break;
				case Types.NULL:

					break;
				case Types.TIMESTAMP:
					java.sql.Timestamp theTimestamp = null;
					if (pValue instanceof java.util.Date)
						theTimestamp = new java.sql.Timestamp(
								((java.util.Date) pValue).getTime());
					else if (pValue instanceof java.util.Calendar)
						theTimestamp = new java.sql.Timestamp(
								((java.util.Calendar) pValue).getTime()
										.getTime());
					else
						throw new GdbException("Unsupport Parameter Type ["
								+ pValue.getClass().getName() + "] value ["
								+ pValue
								+ "], can't convert it into Timestamp ["
								+ pName + "]");
					ps.setTimestamp(i + 1, theTimestamp);
					break;
				case Types.FLOAT:
					float _float = 0;
					if (pValue instanceof Number)
						_float = ((Number) pValue).floatValue();
					else
					{
						try
						{
							_float = Float.parseFloat(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a float: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "], can't convert it into float ["
									+ pName + "]");
						}
					}
					ps.setFloat(i + 1, _float);
					break;
				case Types.REAL:
				case Types.DOUBLE:
					double _double = 0;
					if (pValue instanceof Number)
						_double = ((Number) pValue).doubleValue();
					else
					{
						try
						{
							_double = Double
									.parseDouble(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a double: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "], can't convert it into double ["
									+ pName + "]");
						}
					}
					ps.setDouble(i + 1, _double);
					break;
				case Types.BIGINT:
					long _long = 0;
					if (pValue instanceof Number)
						_long = ((Number) pValue).longValue();
					else
					{
						try
						{
							_long = Long.parseLong(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a long: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "], can't convert it into long [" + pName
									+ "]");
						}
					}
					ps.setLong(i + 1, _long);
					break;
				case Types.INTEGER:
					int _int = 0;
					if (pValue instanceof Number)
						_int = ((Number) pValue).intValue();
					else
					{
						try
						{
							_int = Integer.parseInt(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a int: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "] , can't convert it into int [" + pName
									+ "]");
						}
					}
					ps.setInt(i + 1, _int);
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					BigDecimal dec = null;

					// if (pValue instanceof Number)
					// dec = new BigDecimal (((Number) pValue).doubleValue ()) ;
					// else
					{
						try
						{
							dec = new BigDecimal(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a DECIMAL: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "] , can't convert it into DECIMAL ["
									+ pName + "]");
						}
					}
					// System.out.println ("Dec : " + dec.scale ()) ;
					ps.setBigDecimal(i + 1, dec);
					break;
				case Types.SMALLINT:
					short _short = 0;
					if (pValue instanceof Number)
						_short = ((Number) pValue).shortValue();
					else
					{
						try
						{
							_short = Short.parseShort(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a short: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "] , can't convert it into short ["
									+ pName + "]");
						}
					}
					ps.setShort(i + 1, _short);
					break;
				case Types.TINYINT:
					byte _byte = 0;
					if (pValue instanceof Number)
						_byte = ((Number) pValue).byteValue();
					else if( (pValue instanceof String) &&
							("true".equalsIgnoreCase((String)pValue)||"false".equalsIgnoreCase((String)pValue)))
					{
						if("true".equalsIgnoreCase((String)pValue))
							_byte = 1 ;
						else if("false".equalsIgnoreCase((String)pValue))
							_byte = 0 ;
					}
					else
					{
						try
						{
							_byte = Byte.parseByte(String.valueOf(pValue));
						}
						catch (Throwable _t)
						{
							if (log.isDebugEnabled())
								log
										.debug(
												"When try to convert parameter as a byte: ",
												_t);

							throw new GdbException("Unsupport Parameter Type ["
									+ pValue.getClass().getName() + "] value ["
									+ pValue
									+ "] , can't convert it into byte ["
									+ pName + "]");
						}
					}
					ps.setByte(i + 1, _byte);
					break;
				case Types.BINARY:
				case Types.VARBINARY:
					byte[] bytes = null;
					if (pValue instanceof byte[])
						bytes = (byte[]) pValue;
					else
						bytes = String.valueOf(pValue).getBytes("UTF8");

					ps.setBytes(i + 1, bytes);
					break;
				case Types.BIT:
					if(pValue instanceof Boolean)
						ps.setBoolean(i+1, ((Boolean)pValue).booleanValue()) ;
					else if(pValue instanceof Number)
						ps.setBoolean(i+1, ((Number)pValue).intValue()>0) ;
					else if(pValue instanceof String)
					{
						if("true".equalsIgnoreCase((String)pValue))
							ps.setBoolean(i+1, true) ;
						else if("false".equalsIgnoreCase((String)pValue))
							ps.setBoolean(i+1, false) ;
					}
					else
						ps.setObject(i+1, pValue);
					break ;
				case Types.CHAR:
				case Types.VARCHAR:
				default:
					ps.setString(i + 1, String.valueOf(pValue));

					break;
				}
			}
		}

	}

	public Gdb[] getAllGdb()
	{
		Gdb[] rets = new Gdb[absResPath2Gdb.size()];
		absResPath2Gdb.values().toArray(rets);
		return rets;
	}

	/**
	 * ����db���ͼ�XORM���XORM���Ӧ���������ݽ��а�װ
	 * 
	 * @param dbt
	 * @param xorm_c
	 * @return
	 */
	public List<String> installByXORMClassTable(IConnPool cp, Class xorm_c,String tablename)
			throws GdbException, ClassNotFoundException
	{
		if (cp == null)
			throw new IllegalArgumentException("conn pool cannot be null!");

		StringBuilder sb = new StringBuilder();
		List<String> tmpsqls = XORMUtil.extractCreationDBSqls(tablename,cp.getDBType(),
				xorm_c, sb);
		if (tmpsqls == null)
			throw new GdbException("Install For XORM Error::" + sb.toString());

		if(ISynClientable.class.isAssignableFrom(xorm_c))
		{
			List<String> logsqls = DbSql.getDbSqlByDBType(cp.getDBType()).getCreationSynClientLogSql(xorm_c);
			tmpsqls.addAll(logsqls);
		}
		executeNoResultSqls(cp, tmpsqls);
		return tmpsqls;
	}
	
	public List<String> installByXORMClassTable(IConnPool cp, Class xorm_c)
		throws GdbException, ClassNotFoundException
	{
			return installByXORMClassTable(cp, xorm_c,null);
	}

	public List<String> installByXORMClassTable(String dbname, Class xorm_c)
			throws GdbException, ClassNotFoundException
	{
		IConnPool cp = ConnPoolMgr.getConnPool(dbname);
		return installByXORMClassTable(cp, xorm_c);
	}

	public void dropXORMClassTable(IConnPool cp, Class xorm_c) throws Exception
	{
		if (cp == null)
			throw new IllegalArgumentException("conn pool cannot be null!");

		String sql = XORMUtil.getDropXORMClassTable(xorm_c);
		ArrayList<String> sqls = new ArrayList<String>(1);
		sqls.add(sql);
		executeNoResultSqls(cp, sqls);
	}

	public void dropXORMClassTable(String dbname, Class xorm_c)
			throws Exception
	{
		IConnPool cp = ConnPoolMgr.getConnPool(dbname);
		dropXORMClassTable(cp, xorm_c);
	}

	public ArrayList<String> installGdb(String gdb_respath)
			throws GdbException, Exception
	{
		return installGdb(gdb_respath, null);
	}
	
	public ArrayList<String> installGdb(String gdb_respath, String xorm_cn)
	throws GdbException, Exception
	{
		return installGdb(null, gdb_respath, xorm_cn) ;
	}

	public ArrayList<String> installGdb(String modulename,String gdb_respath, String xorm_cn)
			throws GdbException, Exception
	{
		IConnPool cp = null ;
		DBType dbt = null ;
		
		Gdb g = absResPath2Gdb.get(gdb_respath);
		if (g == null)
			throw new GdbException("cannot find gdb res path:" + gdb_respath);

		// ClassLoader cl = g.getRelatedClassLoader();
		cp = ConnPoolMgr.getConnPool(g);
		dbt = cp.getDBType();
		
		Connection tmpconn = null ;
		ArrayList<String> install_sqls = new ArrayList<String>();
		try
		{
			tmpconn = cp.getConnection() ;
			
			for (XORM ifxorm : g.getInstallForXORMs())
			{
				if (ifxorm.IsNoInstall())
					continue;
	
				Class c = ifxorm.getXORMClass();
	
				if (xorm_cn != null && !xorm_cn.equals("")
						&& !c.getCanonicalName().equals(xorm_cn))
					continue;
				
				XORMClass xc = ifxorm.getXORMClassClass();
				String tn = ifxorm.getJavaTableInfo().getTableName() ;
				
				if(xc.distributed_mode()>0
						&&modulename!=null
						&&cp==ConnPoolMgr.getDefaultConnPool())
				{//
					GDBLogManager.getInstance().setLogTable(modulename,c,tn) ;
				}
				
				if(DBUtil.tableExists(tmpconn, tn))
					continue ;
				
				StringBuilder sb = new StringBuilder();
				List<String> tmpsqls = XORMUtil.extractCreationDBSqls(dbt, c, sb);
				if (tmpsqls == null)
					throw new GdbException("Install For XORM Error::"
							+ sb.toString());

				install_sqls.addAll(tmpsqls);
				
				if(ISynClientable.class.isAssignableFrom(c))
				{
					List<String> logsqls = DbSql.getDbSqlByDBType(cp.getDBType()).getCreationSynClientLogSql(c);
					install_sqls.addAll(logsqls);
				}
			}
		}
		finally
		{
			if(tmpconn!=null)
				cp.free(tmpconn) ;
		}

		List<InstallForDB> fds = g.getInstallForDB(dbt);
		// throw new GdbException("cannot find install info in
		// gdb:"+gdb_respath);
		if (fds != null&&fds.size()>0)
		{
			for(InstallForDB fd:fds)
			{
				String tn0 = fd.getRelatedTableName() ;
				if(Convert.isNullOrEmpty(tn0))
					continue ;
				
				if(DBUtil.tableExists(tmpconn, tn0))
					continue ;
				
				ArrayList<SqlItem> sis = fd.getSqlItems();
				if (sis != null)
				{
					for (SqlItem si : sis)
					{
						RuntimeItem ri = si.getRuntimeItem(dbt, null);
						String sql = ri.getJdbcSql();
						if (sql != null)
							install_sqls.add(sql);
					}
				}
			}
		}

		executeNoResultSqls(cp, install_sqls);
		return install_sqls;
	}

	private void executeNoResultSqls(IConnPool cp, List<String> install_sqls)
			throws GdbException
	{
		Connection conn = null;
		PreparedStatement ps = null;
		boolean b_autocommit = true;
		try
		{
			conn = cp.getConnection();
			b_autocommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			// f.getInParam(uniquekey)
			DBResult dbr = new DBResult();

			for (String sql : install_sqls)
			{
				System.out.println("install sql:" + sql);
				ps = conn.prepareStatement(sql);
				ps.execute();

				ps.close();
				ps = null;
			}

			conn.commit();
			conn.setAutoCommit(b_autocommit);
			cp.free(conn);
			conn = null;

		}
		catch (SQLException sqle)
		{
			throw new GdbException(sqle);
		}
		finally
		{
			try
			{// ������˵�����г���
				if (conn != null)
					conn.rollback();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				try
				{
					conn.setAutoCommit(b_autocommit);
				}
				catch (Throwable sqle)
				{
				}
				
				cp.free(conn);
			}

			
		}
	}

	// ////////////////////XORM

	public Object getMaxPkIdByXORM(Class xorm_class) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			sqlstr = xorm_conf.getMaxPkIdSql() ;
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb getMaxPkIdByXORM xorm class=["
						+ xorm_class.getCanonicalName() + "]" + sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			rs = ps.executeQuery();
			
			if(!rs.next())
				return null ;
			
			Object ov = rs.getObject(1) ;
			if(ov==null)
				return null ;
			
			if(ov instanceof Number)
			{
				return ((Number)ov).longValue() ;
			}
			
			if(ov instanceof String)
				return (String)ov ;
			
			return ov.toString() ;
		}

		catch (Exception eee)
		{
			throw new GdbException("gdb getMaxPkIdByXORM error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}
	
	/**
	 * ����XORM�������Ӧ���е�����������¼����
	 * �������� ���� ֵ ��ɵ����ϵ
	 * @param xorm_class
	 * @param cols ������
	 * @param opers ��������
	 * @param vals ����ֵ
	 * @return
	 * @throws GdbException
	 */
	public int getCountByXORMColOperValue(Class xorm_class,
			String[] cols,String[] opers,
			Object[] vals
			) throws GdbException
	{
		return getCountByXORMColOperValue(xorm_class,
				cols,opers,vals,null) ;
	}
	
	public int getCountByXORMColOperValue(Class xorm_class,
			String[] cols,String[] opers,
			Object[] vals,boolean[] null_ignores
			) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			sqlstr = "select count(*) from "+jti.getTableName() ;
			String where_str = xorm_conf.getXORMWhereSqlByColOpers(cols,opers,vals,null_ignores,null) ;
			if(where_str!=null&&!(where_str=where_str.trim()).equals(""))
			{
				if(where_str.toLowerCase().startsWith("where"))
					where_str = where_str.substring(5).trim() ;
				
				sqlstr += " where "+where_str ;
			}
			conn = cp.getConnection();
			

			if (log.isDebugEnabled())
			{
				log.debug("gdb getCountByXORMColOperValue xorm class=["
						+ xorm_class.getCanonicalName() + "]" + sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			//ps = conn.prepareStatement(sqls[0].toString());

			long tt = System.currentTimeMillis();
			
			for (int i = 0; i < cols.length; i++)
			{
				JavaColumnInfo jci = jti.getColumnInfoByName(cols[i]) ;
				Object tmpo = prepareObjVal(vals[i]) ;
				if (tmpo != null)
				{
					if(jci==null)
						ps.setObject(1 + i, tmpo);
					else
						ps.setObject(1+i, tmpo,jci.getSqlValType());
				}
				else
				{
					if(null_ignores!=null&& null_ignores[i])
						continue ;
					if(jci==null)
						throw new Exception("cannot get column with name="+cols[i]) ;
					
					ps.setNull(1 + i, jci.getSqlValType());
				}
			}

			

			rs = ps.executeQuery();
			
			if(!rs.next())
				return -1 ;
			
			return rs.getInt(1) ;
		}

		catch (Exception eee)
		{
			throw new GdbException("gdb getCountByXORM error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}
	
	
	public int getCountByXORM(Class xorm_class,String where_str) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			sqlstr = "select count(*) from "+xorm_conf.getJavaTableInfo().getTableName() ;
			if(where_str!=null&&!(where_str=where_str.trim()).equals(""))
			{
				if(where_str.toLowerCase().startsWith("where"))
					where_str = where_str.substring(5).trim() ;
				
				sqlstr += " where "+where_str ;
			}
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb getCountByXORM xorm class=["
						+ xorm_class.getCanonicalName() + "]" + sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			rs = ps.executeQuery();
			
			if(!rs.next())
				return -1 ;
			
			return rs.getInt(1) ;
		}

		catch (Exception eee)
		{
			throw new GdbException("gdb getCountByXORM error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}
	/**
	 * ����XORM��,������pk id�����ݿ��л�ü�¼,��ת���ɶ���ʵ��
	 * 
	 * @param xorm_class
	 * @param id
	 * @return
	 */
	public Object getXORMObjByPkId(Class xorm_class, Object pk_id)
			throws GdbException
	{
		return getXORMObjByPkId(xorm_class, pk_id, false);
	}

	public Object getXORMObjByPkId(Class xorm_class, Object pk_id,
			boolean readall) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			sqlstr = xorm_conf.getSelectByPkSql();
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb getXORMObjByPkId xorm class=["
						+ xorm_class.getCanonicalName() + "] pk=["
						+ pk_id.toString() + "]\r\n" + sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			ps.setObject(1, pk_id);

			DBResult dbr = new DBResult();
			rs = ps.executeQuery();
			DataTable dt = dbr.appendResultSet(null,0,rs, 0, -1,null);
			ArrayList<XORMProperty> f_xormps = xorm_conf
					.getStoreAsFileXORMProps();
			DataRow dr = dt.getRow(0);
			if (dr != null)
			{// �����ļ����������
				for (XORMProperty fp : f_xormps)
				{
					//
					if (!readall && fp.read_on_demand())
						continue;

					String fpn = fp.name();
					dr.getBelongToTable().addColumn(
							new DataColumn(fpn, byte[].class));
					byte[] cont = loadXORMFileCont(xorm_class, fpn,
							(Long) pk_id);
					if (cont != null)
					{
						dr.putValue(fpn, cont);
					}
				}
			}
			if (log.isDebugEnabled())
			{
				log.debug("XORM getXORMObjByPkId res table--\r\n"
						+ dt.toString());
			}
			List ll = dbr.transTable2XORMObjList(0, xorm_class);
			if (ll == null || ll.size() <= 0)
				return null;

			return ll.get(0);
		}

		catch (Exception eee)
		{
			throw new GdbException("gdb getXORMObjByPkId error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}

	
	/**
	 * ��ȡXORM���󣬲��޶��˶�ȡ��
	 * �������ļ���Ext��
	 * @param xorm_class
	 * @param pk_id
	 * @param read_colnames
	 * @return
	 * @throws GdbException
	 */
	public Hashtable getXORMObjByPkIdWithLimitCols(Class xorm_class, Object pk_id,
			String[] read_colnames) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			sqlstr = xorm_conf.getSelectColsByPkSql(read_colnames);
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb getXORMObjByPkIdWithLimitCols xorm class=["
						+ xorm_class.getCanonicalName() + "] pk=["
						+ pk_id.toString() + "]\r\n" + sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			ps.setObject(1, pk_id);

			DBResult dbr = new DBResult();
			rs = ps.executeQuery();
			DataTable dt = dbr.appendResultSet(null,0,rs, 0, -1,null);
			
			DataRow dr = dt.getRow(0);
			
			if (log.isDebugEnabled())
			{
				log.debug("XORM getXORMObjByPkIdWithLimitCols res table--\r\n"
						+ dt.toString());
			}
			//List ll = dbr.transTable2XORMObjList(0, xorm_class);
			//if (ll == null || ll.size() <= 0)
			//	return null;
//
			//return ll.get(0);
			if(dr==null)
				return null ;
			
			Hashtable ret = new Hashtable() ;
			for(String n:read_colnames)
			{
				Object ov = dr.getValue(n) ;
				if(ov==null)
					continue ;
				ret.put(n, ov) ;
			}
			return ret ;
		}
		catch (Exception eee)
		{
			throw new GdbException("gdb getXORMObjByPkId error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}
	/**
	 * ����Ψһ�к�ֵ��ö�Ӧ��XORM����
	 * 
	 * @param xorm_class
	 * @param unique_col
	 *            Ψһ������
	 * @param col_value
	 *            Ψһֵ
	 * @param readall
	 *            �ж����������ļ�����,�Ƿ�Ҫ��ȡ������
	 * @return
	 * @throws GdbException
	 */
	public Object getXORMObjByUniqueColValue(Class xorm_class,
			String unique_col, Object col_value, boolean readall)
			throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_class);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_class.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			sqlstr = xorm_conf.getSelectByUniqueColSql(unique_col);
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb getXORMObjByUniqueColValue xorm class=["
						+ xorm_class.getCanonicalName() + "] unique col=["
						+ unique_col + "]");
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			ps.setObject(1, prepareObjVal(col_value));

			DBResult dbr = new DBResult();
			rs = ps.executeQuery();
			DataTable dt = dbr.appendResultSet(null,0,rs, 0, -1,null);
			ArrayList<XORMProperty> f_xormps = xorm_conf
					.getStoreAsFileXORMProps();
			DataRow dr = dt.getRow(0);

			if (dr != null)
			{// �����ļ����������
				Object pk_id = dr.getValue(0);// get pk id
				for (XORMProperty fp : f_xormps)
				{
					//
					if (!readall && fp.read_on_demand())
						continue;

					String fpn = fp.name();
					dr.getBelongToTable().addColumn(
							new DataColumn(fpn, byte[].class));
					byte[] cont = loadXORMFileCont(xorm_class, fpn,
							(Long) pk_id);
					if (cont != null)
					{
						dr.putValue(fpn, cont);
					}
				}
			}
			if (log.isDebugEnabled())
			{
				log.debug("XORM getXORMObjByUniqueColValue res table--\r\n"
						+ dt.toString());
			}
			List ll = dbr.transTable2XORMObjList(0, xorm_class);
			if (ll == null || ll.size() <= 0)
				return null;

			return ll.get(0);
		}

		catch (Exception eee)
		{
			throw new GdbException("gdb getXORMObjByUniqueColValue error\n"
					+ sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}

	private String getFilePath(Class xorm, String xorm_prop, long pkid)
			throws ClassNotFoundException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm.getCanonicalName());

		if (xorm_prop == null || xorm_prop.equals(""))
		{
			XORMProperty p = xorm_conf.getXORMPropFirstFile();
			if (p == null)
				throw new RuntimeException(
						"no store as file XORM property found in class="
								+ xorm.getCanonicalName());

			xorm_prop = p.name();
		}
		String fb = xorm_conf.getJavaTableInfo().getTableName() + "_"
				+ xorm_prop;

		return autoId2Path(fb, pkid);
	}

	/**
	 * ����
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pk_id
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public InputStream getXORMFileStreamByPkId(Class xorm_class,
			String xorm_prop, long pkid) throws ClassNotFoundException,
			IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		File f = new File(fp);
		if (!f.exists())
			return null;

		return new FileInputStream(f);
	}

	/**
	 * ��������id�Ͷ�Ӧ��XORMProperty����ļ����� �÷���������ʹ��,ԭ�����п����ڴ�ռ��̫��
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public byte[] loadXORMFileCont(Class xorm_class, String xorm_prop, long pkid)
			throws ClassNotFoundException, IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		File f = new File(fp);
		if (!f.exists())
			return null;

		byte[] buf = new byte[(int) f.length()];
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(f);
			fis.read(buf);
			return buf;
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}

	/**
	 * ��������id�Ͷ�Ӧ��XORMProperty��������ļ�����
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @return
	 * @throws ClassNotFoundException
	 */
	public long getXORMFileContLength(Class xorm_class, String xorm_prop,
			long pkid) throws ClassNotFoundException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		File f = new File(fp);
		if (!f.exists())
			return -1;

		return f.length();
	}
	
	/**
	 * ���������Ͷ�Ӧ��XORMProperty��ö�Ӧ���ļ�����
	 * �ķ������Զ������ļ����ڵ�Ŀ¼
	 * 
	 * �����ܱ�֤�ļ��Ѿ�����
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @return
	 * @throws ClassNotFoundException
	 */
	public File getXORMFile(Class xorm_class,String xorm_prop,long pkid)
	 throws ClassNotFoundException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		File f = new File(fp);
		File pf = f.getParentFile() ;
		if(!pf.exists())
			pf.mkdirs() ;
		
		return f ;
	}

	/**
	 * ��������id�Ͷ�Ӧ��XORMProperty����ļ�����,��ֱ��������������
	 * 
	 * <b>����ʹ�ø÷���</b>--��� XORMProperty .read_on_demand=true,������Ϣ���޷���� ������,
	 * ���Ե������ø÷�������ȡ���ݵ����
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @param os
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public boolean loadXORMFileContToOutputStream(Class xorm_class,
			String xorm_prop, long pkid, OutputStream os)
			throws ClassNotFoundException, IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		File f = new File(fp);
		if (!f.exists())
			return false;

		byte[] buf = new byte[1024];
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(f);
			int len;
			while ((len = fis.read(buf)) > 0)
			{
				os.write(buf, 0, len);
			}
			return true;
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}

	/**
	 * ��������idֱ�ӱ����ļ�����
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @param filecont
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void saveXORMFileCont(Class xorm_class, String xorm_prop, long pkid,
			byte[] filecont) throws ClassNotFoundException, IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		if (filecont == null)
		{
			File f = new File(fp);
			if (!f.exists())
				return;
			f.delete();
			return;
		}

		FileOutputStream fos = null;
		try
		{
			File f = new File(fp);
			File df = f.getParentFile();
			if (!df.exists())
				df.mkdirs();

			fos = new FileOutputStream(f);
			fos.write(filecont);
		}
		finally
		{
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * ��������id,��������,�����ļ�����
	 * 
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @param filecont
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void saveXORMFileCont(Class xorm_class, String xorm_prop, long pkid,
			InputStream filecont) throws ClassNotFoundException, IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		if (filecont == null)
		{
			File f = new File(fp);
			if (!f.exists())
				return;
			f.delete();
			return;
		}

		FileOutputStream fos = null;
		try
		{
			File f = new File(fp);
			File df = f.getParentFile();
			if (!df.exists())
				df.mkdirs();

			fos = new FileOutputStream(f);
			byte[] buf = new byte[1024];
			int len;
			while ((len = filecont.read(buf)) > 0)
			{
				fos.write(buf, 0, len);
			}
		}
		finally
		{
			if (fos != null)
				fos.close();
		}
	}
	
	/**
	 * ֱ�Ӵ�������ʱ�ļ������ļ���ΪXORMProperty���ļ�ֵ
	 * @param xorm_class
	 * @param xorm_prop
	 * @param pkid
	 * @param tmp_f
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void saveXORMFileContByTempFile(Class xorm_class, String xorm_prop, long pkid,
			File tmp_f) throws ClassNotFoundException, IOException
	{
		String fp = getFilePath(xorm_class, xorm_prop, pkid);
		if (tmp_f == null||!tmp_f.exists())
		{
			File f = new File(fp);
			if (!f.exists())
				return;
			f.delete();
			return;
		}

		File f = new File(fp);
		if(!f.getParentFile().exists())
			f.getParentFile().mkdirs() ;
		
		tmp_f.renameTo(f) ;
	}

	// /**
	// * ����gdb�����Ψһ���,�����������ѯ�õ������б�-����ÿ�������е����ݽ������������������� �ĳ�Ա.
	// *
	// * @param gdb_unique
	// * @param inputpm
	// * @return
	// */
	// public List getXORMObjListFromDB(String gdb_unique,
	// Hashtable inputpm)
	// {
	// DBResult dbr = accessDB(uniquekey, parms);
	// return dbr.transTable2ObjList(0, objc);
	// return null;
	// }
	
	/**
	 * ��������������ر��ڵ��Զ�ʱ������н����Զ�����
	 * ����ʾ������ݵı仯
	 * @return ���ظ��µ����������-1��ʾʧ��
	 */
	public int fireAutoUpdateDTCol(Class xormc,String wherestr)
		throws Exception
	{
		if(Convert.isNullOrEmpty(wherestr))
			return -1;
		
		
		
		XORM x = Gdb.getXORMByGlobal(xormc);
		if (x == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getCanonicalName());

		String autodtc = x.getAutoUpdateDTCol() ;
		if(Convert.isNullOrEmpty(autodtc))
			return -1;
		
		Gdb g = x.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;

		JavaTableInfo jti = x.getJavaTableInfo();
		//x.getPkXORMPropWrapper().
		boolean oldau = true;
		try
		{
			conn = cp.getConnection();
			String sqls = "update "+jti.getTableName()+
				" set "+autodtc+"=? where "+wherestr;
			
			ps = conn.prepareStatement(sqls);
			long t = System.currentTimeMillis();
			ps.setObject(1, t);

			int r = ps.executeUpdate();

			cp.free(conn);
			conn = null;
			
			return r;
		}
		finally
		{
			try
			{

				if (ps != null)
				{
					ps.close();
				}

			}
			catch (SQLException sqle)
			{
			}
			
			if(conn!=null)
			{
				cp.free(conn);
				conn = null;
			}
		}
	}
	
	private boolean dtAutoUpdate(Connection conn,Object pkid,XORM x,Object upobj)
		throws Exception
	{
		String autodtc = x.getAutoUpdateDTCol() ;
		if(Convert.isNullOrEmpty(autodtc))
			return false;
		
		PreparedStatement ps = null;

		JavaTableInfo jti = x.getJavaTableInfo();
		//x.getPkXORMPropWrapper().
		boolean oldau = true;
		try
		{
			String sqls = "update "+jti.getTableName()+
				" set "+autodtc+"=? where "+jti.getPkColumnInfo().getColumnName()+"=?";
			
			ps = conn.prepareStatement(sqls);
			long t = System.currentTimeMillis();
			ps.setObject(1, t);
			ps.setObject(2, pkid);

			int rowaff = ps.executeUpdate();

			if( rowaff!= 1)
				return false;

			if(upobj!=null)
			{
				XmlData tmpxd = new XmlData() ;
				tmpxd.setParamValue(autodtc,t) ;
				XORMUtil.injectXmlDataToObj(upobj, tmpxd, new String[]{autodtc}) ;
			}
			return true;
		}
		finally
		{
			try
			{

				if (ps != null)
				{
					ps.close();
				}

			}
			catch (SQLException sqle)
			{
			}
		}
	}

	private void logSynClient(Connection conn,String pkid,ISynClientable sct) throws Exception
	{
		Class xorm_objc = sct.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_objc);
		
		if(Convert.isNullOrEmpty(pkid))
			pkid = sct.getSynClientPkId();
		String[] cid_cols = sct.getSynClientClientIdColNames() ;
		for(String cidc:cid_cols)
		{
			logSynClient(conn,xorm_conf,cidc,sct.getSynClientIdByColName(cidc),pkid) ;
		}
	}
	
	/**
	 * ����������û�취��ʱ��ʹ��
	 * @param conn
	 * @param pkid
	 * @param xormc
	 * @throws Exception
	 */
	private void logSynClient(Connection conn,Object pkid,Class xormc)
		throws Exception
	{
		XORM xorm = Gdb.getXORMByGlobal(xormc) ;
		ISynClientable sct = xorm.getSynClientImpl() ;
		if(sct==null)
			return ;
		
		//���cid
		String[] cid_cols = sct.getSynClientClientIdColNames();
		Hashtable ht = this.getXORMObjByPkIdWithLimitCols(xormc,pkid,cid_cols) ;
		if(ht==null)
			throw new Exception("no client value found with pk id="+pkid) ;
		
		for(String cidc:cid_cols)
		{
			Object cid_obj = ht.get(cidc) ;
			if(cid_obj==null)
				continue;//throw new Exception("no client value found with pk id="+pkid) ;
			
			logSynClient(conn,xorm,cidc,cid_obj.toString(),pkid.toString());
		}
	}
	
	private void logSynClient(Connection conn,String unique_col,Object unique_val,Class xormc)
		throws Exception
	{
		XORM xorm = Gdb.getXORMByGlobal(xormc) ;
		ISynClientable sct = xorm.getSynClientImpl() ;
		if(sct==null)
			return ;
		
		Object obj = this.getXORMObjByUniqueColValue(xormc, unique_col, unique_val, false);
		if(obj==null)
			throw new Exception("no xorm obj found with unique col value="+unique_col+","+unique_val);
		
		logSynClient(conn,null,(ISynClientable) obj) ;
	}
	
	private void logSynClient(Connection conn,XORM xorm,String client_id_col,String cid,String pkid) throws Exception
	{
		ISynClientable sct = xorm.getSynClientImpl() ;
		if(sct==null)
			return ;
		
		JavaTableInfo jti = xorm.getJavaTableInfo() ;
		//String pk_col = jti.getPkColumnInfo().getColumnName() ;
		//String clientid_col = sct.getSynClientColName() ;
		
		
		IConnPool cp = ConnPoolMgr.getConnPool(xorm.getBelongToGdb().usingDBName);

		DBType dbt = cp.getDBType();
		
		Hashtable ht = new Hashtable() ;
		ht.put("$TableName", jti.getTableName()) ;
		ht.put("@ClientId", client_id_col+"_"+cid) ;
		ht.put("@PkId",pkid) ;
		ht.put("@ChgDT",System.currentTimeMillis()) ;
		this.accessDBInConn(dbt, conn, "syn_client.InsertOrUpdateSynClient", ht,0,-1,null,null);
	}
	
	/**
	 * ����xorm����,�����ݿ������Ӽ�¼,�������µ�id
	 * 
	 * [������֧��log]
	 * @param xorm_obj
	 * @return
	 * @throws GdbException
	 */
	public Object addXORMObjWithNewId(Object xorm_obj) throws GdbException
	{
		return addXORMObjWithNewId(null,xorm_obj) ;
	}
	
	/**
	 * ����xorm����,�����ݿ������Ӽ�¼,�������µ�id
	 * 
	 * [������֧��log]
	 * @param xorm_obj
	 * @return
	 * @throws GdbException
	 */
	public Object addXORMObjWithNewId(String dbname,Object xorm_obj) throws GdbException
	{
		Class xorm_objc = xorm_obj.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_objc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();
		
		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;
		
		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;

		ArrayList<XORMProperty> f_xorm_ps = xorm_conf.getStoreAsFileXORMProps();
		boolean oldau = true;
		try
		{
			if(dbname!=null&&!"".equals(dbname))
			{
				cp = ConnPoolMgr.getConnPool(g) ;
				if(cp==null)
					throw new GdbException("No connpool with name="+dbname) ;
			}
			
			if(tit!=null)
			{
				conn = tit.getConn(g.usingDBName) ;
			}
			else
			{
				conn = cp.getConnection();
			}
			
			oldau = conn.getAutoCommit();
			conn.setAutoCommit(false);
			
			Object nid = addXORMObjWithNewIdInConn(conn, xorm_obj);
			
			//syn_client log
			if(xorm_obj instanceof ISynClientable)
				logSynClient(conn,nid.toString(),(ISynClientable)xorm_obj);
			
			dtAutoUpdate(conn,nid,xorm_conf,xorm_obj) ;
			
			conn.commit();
			conn.setAutoCommit(oldau);
			
			if(tit==null)
			{
				cp.free(conn);
			}
			
			conn = null;
			return nid;
		}
		catch (Exception e)
		{
			log.error(e);
			if(e instanceof GdbException)
				throw (GdbException)e ;
			else
				throw new GdbException(e);
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.rollback();
					conn.setAutoCommit(oldau);
				}
				catch(Exception eee)
				{}
			}
			
			if(tit==null)
			{
				try
				{
					if (conn != null && f_xorm_ps.size() > 0)
					{
						conn.rollback();
						conn.setAutoCommit(oldau);
					}
				}
				catch (Throwable sqle)
				{
				}
				
				if (conn != null)
					cp.free(conn);
			}
		}
	}
	
	Object addXORMObjWithNewIdInConn(Connection conn, Object xorm_obj)
	throws Exception
	{
		return addXORMObjWithNewIdInConn(null,conn, xorm_obj);
	}
	
	Object addXORMObjWithNewIdInConn(String newid,Connection conn, Object xorm_obj)
		throws Exception
	{
		Class xorm_objc = xorm_obj.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_objc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());
		
		Gdb g = xorm_conf.getBelongToGdb();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<XORMProperty> f_xorm_ps = xorm_conf.getStoreAsFileXORMProps();
		boolean oldau = true;
		String sqlstr = null ;
		try
		{
			Object nid = null ;
			String[] sqls = xorm_conf.getInsertWithNewIdReturnSqls();
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			sqlstr = sqls[0].toString();
			Hashtable pm = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj);
			if (log.isDebugEnabled())
			{
				log.debug("[addXORMObjWithNewId sql]==" + sqlstr);
				log.debug("input param------------->\n");
				printInParam(pm);
			}
			ps = conn.prepareStatement(sqls[0].toString());
		
			long tt = System.currentTimeMillis();
			int p_sv = 1 ;
			if(sqls.length==1)
			{//�Զ��ַ���ֵ
				
				if(Convert.isNullOrEmpty(newid))
					nid = newAutoStringKeyVal();
				else
					nid = newid;
				
				ps.setObject(1, nid);
				p_sv = 2 ;
			}
			
			JavaColumnInfo[] nor_jcis = jti.getNorColumnInfos();
			for (int i = 0; i < nor_jcis.length; i++)
			{
				Object tmpo = prepareObjVal(pm.get(nor_jcis[i].getColumnName()));
				if (tmpo != null)
					ps.setObject(p_sv + i, tmpo);
				else
				{
					int sqlt = nor_jcis[i].getSqlValType();
					if (sqlt == java.sql.Types.BLOB)// for sqlserver driver
						// NullPointer error
						ps.setObject(p_sv + i, new byte[0]);
					else
						ps.setNull(p_sv + i, sqlt);
				}
			}
			
		
			ps.executeUpdate();
		
			ps.close();
			
			ps = null;
		
			if(sqls.length==2)
			{
				sqlstr = sqls[1].toString();
				// System.out.println("[addRecord sql]==" + sqlstr);
				ps = conn.prepareStatement(sqlstr);
				rs = ps.executeQuery();
				if (!rs.next())
				{ // empty
					throw new Exception("Cannot get new id!");
				}
				else
				{
					Number num = (Number) rs.getObject(1);
					long nid_long = num.longValue();
			
					if (f_xorm_ps.size() > 0)
					{// ��Ҫ���ļ�����ɹ������ύ
						for (XORMProperty p : f_xorm_ps)
						{
							String pn = p.name();
							byte[] pv = (byte[]) pm.get(pn);
							saveXORMFileCont(xorm_objc, pn, nid_long, pv);
						}
					}
					nid = nid_long ;
				}
		
				rs.close();
				rs = null;
		
				ps.close();
				ps = null;
			}
			
//			 set new id to pk field
			Field f = (Field) xorm_conf.getPkXORMPropWrapper()
					.getFieldOrMethod();
	
			f.setAccessible(true);
			f.set(xorm_obj, nid);
			
			return nid;
		}
		catch(Exception ee)
		{
			throw new GdbException("addXORMObjWithNewIdInConn sql="+sqlstr+
					"\r\n"+xorm_objc.getCanonicalName(),ee) ;
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}
		
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
		}
	}
	
	/**
	 * ����DataTable���»����XORM��Ķ�Ӧ��¼
	 * Ҫ��DataTable�е�ÿһ�ж�������ֵ
	 * 
	 * �÷�������֧�����ݵ���Ȳ���
	 * @param conn
	 * @param dt
	 * @param b_log �Ƿ�Ҫ��¼��־
	 * @throws Exception 
	 * @throws Exception
	 */
	
	public void setOrUpdateXORMWithDataTable(
			Class xormc,
			DataTable dt,
			boolean b_log) throws Exception
	{
		int rn = dt.getRowNum() ;
		if(rn<=0)
			return ;
		
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getClass().getCanonicalName());
		
		JavaTableInfo jti = xorm_conf.getJavaTableInfo();
		JavaColumnInfo pkjci = jti.getPkColumnInfo() ;
		
		String pk_coln = pkjci.getColumnName();
		StringBuilder sb = new StringBuilder() ;
		boolean b_str_key = false;
		
		sb.append(jti.getPkColumnInfo().getColumnName())
			.append(" in (");
		for(int i = 0 ; i < rn ; i ++)
		{
			DataRow dr = dt.getRow(i) ;
			if(i>0)
				sb.append(',') ;
			
			Object k0 = dr.getValue(pk_coln) ;
			if(k0 instanceof Number)
			{
				sb.append(((Number)k0).longValue()) ;
			}
			else
			{
				sb.append("'").append(k0.toString()).append("'") ;
			}
		}
		sb.append(")");
		
		List ids = listXORMPkIds(xormc, sb.toString(), null,0,-1) ;
		
		ArrayList<DataRow> insert_row = new ArrayList<DataRow>() ;
		ArrayList<DataRow> update_row = new ArrayList<DataRow>() ;
		for(int i = 0 ; i < rn ; i ++)
		{
			DataRow dr = dt.getRow(i) ;
			Object k0 = dr.getValue(pk_coln) ;
			if(k0 instanceof Number)
			{
				if(ids.contains((Number)k0))
					update_row.add(dr) ;
				else
					insert_row.add(dr) ;
			}
			else
			{
				String sv = k0.toString();
				if(ids.contains(sv))
					update_row.add(dr) ;
				else
					insert_row.add(dr) ;
				
			}
		}
		Gdb g = xorm_conf.getBelongToGdb();
		
		IConnPool cp = ConnPoolMgr.getConnPool(g);

		GDBConn conn = null;
		boolean oldau = true ;
		try
		{
			conn = (GDBConn)cp.getConnection() ;
			oldau = conn.getAutoCommit() ;
			conn.setAutoCommit(false);
			
			insertOrUpdateOrDelXORMWithDataTableInConn(xorm_conf,jti,conn,
					insert_row,update_row,null,b_log);
			
			conn.commit() ;
			conn.setAutoCommit(oldau) ;
			cp.free(conn);
			conn = null ;
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					conn.rollback();
					conn.setAutoCommit(oldau);
				}
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
				cp.free(conn);
		}
	}
	
	/**
	 * ����DataTable�а���˳������� ���»����XORM��Ķ�Ӧ����ɾ��DataTable
	 * id��Χ֮�ڵĲ�����������¼
	 * 
	 * Ҫ��DataTable�е�ÿһ�ж��а�˳��Ψһ����ֵ���������ݱ�Ҳ��Ψһ��������
	 * 
	 * �÷������������ݿ���Ψһ�������ͬ���ǳ�����
	 * @param xormc
	 * @param dt
	 * @param b_log
	 * @throws Exception
	 */
	public void synDataTableToXORMPKUniqueOrder(
			Class xormc,
			DataTable dt,
			boolean b_log) throws Exception
	{
		int rn = dt.getRowNum() ;
		if(rn<=0)
			return ;
		
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getClass().getCanonicalName());
		
		JavaTableInfo jti = xorm_conf.getJavaTableInfo();
		JavaColumnInfo pkjci = jti.getPkColumnInfo() ;
		
		String pk_coln = pkjci.getColumnName();
		
		if(!dt.checkColumnUniqueInOrder(pk_coln))
			throw new IllegalArgumentException("data table pk is not unique order") ;
		
		Object min_pk = dt.getRow(0).getValue(pk_coln) ;
		Object max_pk = dt.getRow(rn-1).getValue(pk_coln) ;
		
		StringBuilder sb = new StringBuilder() ;
		boolean b_str_key = false;
		
		sb.append(pk_coln).append(">=");
		if(min_pk instanceof Number)
		{
			sb.append(((Number)min_pk).longValue()) ;
		}
		else
		{
			sb.append("'").append(min_pk.toString()).append("'") ;
		}
		sb.append(" and ").append(pk_coln).append("<=");
		if(max_pk instanceof Number)
		{
			sb.append(((Number)max_pk).longValue()) ;
		}
		else
		{
			sb.append("'").append(max_pk.toString()).append("'") ;
		}
		
		List ids = listXORMPkIds(xormc, sb.toString(), pk_coln,0,-1) ;
		
		ArrayList<DataRow> insert_row = new ArrayList<DataRow>() ;
		ArrayList<DataRow> update_row = new ArrayList<DataRow>() ;
		ArrayList<Object> del_row_ids = new ArrayList<Object>() ;
		del_row_ids.addAll(ids);
		
		for(int i = 0 ; i < rn ; i ++)
		{
			DataRow dr = dt.getRow(i) ;
			Object k0 = dr.getValue(pk_coln) ;
			if(k0 instanceof Number)
			{
				long lv0 = ((Number)k0).longValue() ;
				if(ids.contains(lv0))
				{
					update_row.add(dr) ;
					del_row_ids.remove(lv0) ;
				}
				else
				{
					insert_row.add(dr) ;
				}
			}
			else
			{
				String sv = k0.toString();
				if(ids.contains(sv))
				{
					update_row.add(dr) ;
					del_row_ids.remove(sv) ;
				}
				else
					insert_row.add(dr) ;
				
			}
		}
		Gdb g = xorm_conf.getBelongToGdb();
		
		IConnPool cp = ConnPoolMgr.getConnPool(g);

		GDBConn conn = null;
		boolean oldau = true ;
		try
		{
			conn = (GDBConn)cp.getConnection() ;
			oldau = conn.getAutoCommit() ;
			conn.setAutoCommit(false);
			
			insertOrUpdateOrDelXORMWithDataTableInConn(xorm_conf,jti,conn,
					insert_row,update_row,del_row_ids,b_log);
			
			conn.commit() ;
			conn.setAutoCommit(oldau) ;
			cp.free(conn);
			conn = null ;
		}
		finally
		{
			try
			{
				if (conn != null)
				{
					conn.rollback();
					conn.setAutoCommit(oldau);
				}
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
				cp.free(conn);
		}
	}
	/**
	 * ����DataTable���»����XORM��Ķ�Ӧ��¼
	 * Ҫ��DataTable�е�ÿһ�ж�������ֵ
	 * 
	 * �÷�������֧�����ݵ���Ȳ���
	 * @param conn
	 * @param dt
	 * @throws Exception
	 */
	private void insertOrUpdateOrDelXORMWithDataTableInConn(
			XORM xorm_conf,JavaTableInfo jti,GDBConn conn,
			List<DataRow> insertrs,List<DataRow> updaters,List<Object> del_pks,boolean b_log)
		throws Exception
	{
		PreparedStatement ps = null;
		ResultSet rs = null;
	
		String pkcoln = jti.getPkColumnInfo().getColumnName();
		JavaColumnInfo[] nor_jcis = jti.getNorColumnInfos();
		try
		{
			if(insertrs!=null&&insertrs.size()>0)
			{
				String sqlstr = xorm_conf.getInsertSqlWithInputId();
				
				if(b_log)
					ps = conn.prepareStatement(sqlstr) ;
				else
					ps = conn.prepareStatementNoLog(sqlstr) ;
				
				for(DataRow dr:insertrs)
				{
					ps.setObject(1, dr.getValue(pkcoln));
					
					for (int i = 0; i < nor_jcis.length; i++)
					{
						Object tmpo = prepareObjVal(dr.getValue(nor_jcis[i].getColumnName()));
						if (tmpo != null)
							ps.setObject(2 + i, tmpo);
						else
						{
							int sqlt = nor_jcis[i].getSqlValType();
							if (sqlt == java.sql.Types.BLOB)// for sqlserver driver
								// NullPointer error
								ps.setObject(2 + i, new byte[0]);
							else
								ps.setNull(2 + i, sqlt);
						}
					}
					
					ps.executeUpdate() ;
					
					ps.clearParameters();
				}
				
				ps.close();
				ps = null ;
			}
			
			if(updaters!=null&&updaters.size()>0)
			{
				String sql = xorm_conf.getUpdateByPkSql() ;
				
				if(b_log)
					ps = conn.prepareStatement(sql) ;
				else
					ps = conn.prepareStatementNoLog(sql) ;
				
				for(DataRow dr:updaters)
				{
					int colc = 0;
					for (int i = 0; i < nor_jcis.length; i++)
					{
						if (nor_jcis[i].isUpdateAsSingle())
							continue;

						colc++;

						Object tmpo = prepareObjVal(dr.getValue(nor_jcis[i].getColumnName()));
						if (tmpo != null)
							ps.setObject(colc, tmpo);
						else
							ps.setNull(colc, nor_jcis[i].getSqlValType());
					}

					ps.setObject(colc + 1, dr.getValue(pkcoln));
					
					ps.executeUpdate() ;
					
					ps.clearParameters();
				}
				ps.close();
				ps = null ;
			}
		
			if(del_pks!=null&&del_pks.size()>0)
			{
				String sqldel = "delete from "+jti.getTableName()+" where "+pkcoln+"=?";
				if(b_log)
					ps = conn.prepareStatement(sqldel) ;
				else
					ps = conn.prepareStatementNoLog(sqldel) ;
				
				for(Object pkv:del_pks)
				{
					ps.setObject(1, pkv);
					ps.executeUpdate() ;
					ps.clearParameters();
				}
				ps.close();
				ps = null ;
			}
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
		}
	}
//	/**
//	 * ����XORM���弰����� XmlDataWithFile ����һ����������XORMC�ж��������
//	 * ��XmlDataWithFileƥ��
//	 * @param xormc
//	 * @param xdwf
//	 */
//	public void addXORMObjWithNewId(Class xormc,XmlDataWithFile xdwf)
//	{
//		
//	}

	

	/**
	 * ����xorm�������ݵ����ݿ��¼�� �÷�����ʹ��ǰ,����ð���ȫ�����ݵĶ���.
	 * 
	 * [������֧��log]
	 * @param pkid
	 *            ����id
	 * @param xorm_obj
	 *            �������ڲ�������ȫ�����µ����ݿ���
	 * @throws GdbException
	 */
	public boolean updateXORMObjToDB(Object pkid, Object xorm_obj)
			throws GdbException
	{
		Class xorm_c = xorm_obj.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());

		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;
		
		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;

		ArrayList<XORMProperty> f_xorm_ps = xorm_conf.getStoreAsFileXORMProps();
		boolean oldau = true;
		try
		{
			if(tit!=null)
			{
				conn = tit.getConn(g.usingDBName);
			}
			else
			{
				conn = cp.getConnection();
				oldau = conn.getAutoCommit();
				conn.setAutoCommit(false);
			}

			boolean ret = updateXORMObjToDBInConn(conn, pkid, xorm_obj);

			if(xorm_obj instanceof ISynClientable)
			{
				this.logSynClient(conn, pkid.toString(), (ISynClientable)xorm_obj) ;
			}
			
			dtAutoUpdate(conn,pkid,xorm_conf,xorm_obj) ;
			
			if(tit==null)
			{
				conn.commit();
				conn.setAutoCommit(oldau);
	
				cp.free(conn);
				conn = null;
			}
			return ret;
		}
		catch (Exception e)
		{
			log.error(e);
			throw new GdbException(e);
		}
		finally
		{
			if(tit==null)
			{
				try
				{
					if (conn != null && f_xorm_ps.size() > 0)
					{
						conn.rollback();
						conn.setAutoCommit(oldau);
					}
				}
				catch (Throwable sqle)
				{
				}
				
				if (conn != null)
					cp.free(conn);
			}
		}
	}

	boolean updateXORMObjToDBInConn(Connection conn, Object pkid,
			Object xorm_obj) throws Exception
	{
		Class xorm_c = xorm_obj.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		PreparedStatement ps = null;

		ArrayList<XORMProperty> f_xorm_ps = xorm_conf.getStoreAsFileXORMProps();
		boolean oldau = true;
		try
		{
			String sqls = xorm_conf.getUpdateByPkSql();
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			Hashtable pm = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj);

			if (log.isDebugEnabled())
			{
				log.debug("[updateXORMObjToDB sql]==" + sqls);
				log.debug("input param------------->\n");
				printInParam(pm);
			}

			ps = conn.prepareStatement(sqls);

			long tt = System.currentTimeMillis();
			JavaColumnInfo[] nor_jcis = jti.getUpdateNorColumnInfos();
			int colc = 0;
			for (int i = 0; i < nor_jcis.length; i++)
			{
				if (nor_jcis[i].isUpdateAsSingle())
					continue;

				colc++;

				Object tmpo = prepareObjVal(pm.get(nor_jcis[i].getColumnName()));
				if (tmpo != null)
					ps.setObject(colc, tmpo);
				else
					ps.setNull(colc, nor_jcis[i].getSqlValType());
			}

			ps.setObject(colc + 1, pkid);

			int rowaff = ps.executeUpdate();

			if (rowaff > 0 && f_xorm_ps.size() > 0 && pkid instanceof Long)
			{// ��Ҫ���ļ�����ɹ������ύ
				for (XORMProperty p : f_xorm_ps)
				{
					String pn = p.name();
					byte[] pv = (byte[]) pm.get(pn);
					saveXORMFileCont(xorm_c, pn, (Long)pkid, pv);
				}

			}

			ps.close();
			ps = null;

			return rowaff == 1;
		}
		finally
		{
			try
			{

				if (ps != null)
				{
					ps.close();
				}

			}
			catch (SQLException sqle)
			{
			}
		}
	}

	/**
	 * �������ж����XORMProperty�У�support_auto=true���н����Զ�����
	 * 
	 * [������֧��log]
	 * 
	 * @param pkid
	 * @param xorm_obj
	 * @return
	 * @throws ClassNotFoundException
	 * @throws GdbException
	 */
	public boolean updateXORMObjToDBWithSupportAutoColNames(Object pkid,Object xorm_obj)
		throws ClassNotFoundException, GdbException
	{
		String[] cls = XORMUtil.getSupportAutoXORMColumns(xorm_obj.getClass()) ;
		if(cls==null||cls.length<=0)
			return false;
		
		return updateXORMObjToDBWithHasColNames(pkid, xorm_obj,cls) ;
	}
	/**
	 * ���������Ͷ�����²������ݵ����ݿ���
	 * 
	 * ���ָ�������Զ�Ӧ�����ݿ��ж��嶼����,��֮����¶�Ӧ��¼
	 * 
	 * ����ƶ������Դ���û�ж������ݿ���,��˵����Ҫ�������ݿ��¼ʣ���XmlData�ṹ����
	 * ����Ҫ�Ȳ�ѯ��������,�����ݶ����е����ݽ�������װ��,���������ݿ�--������
	 * 
	 * [������֧��log]
	 * @param xorm_obj
	 *            ������Ӧ�ô���,��Ӧ����������.
	 * @param property_names
	 *            ֻ���ǰ������ݿ��ж��������.
	 * @throws ClassNotFoundException
	 * @throws GdbException
	 */
	public boolean updateXORMObjToDBWithHasColNames(Object pkid, Object xorm_obj,
			String[] property_names) throws ClassNotFoundException,
			GdbException
	{
		if (property_names == null || property_names.length <= 0)
			throw new IllegalArgumentException("xorm property cannot be empty!");

		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_obj.getClass());
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());

		JavaTableInfo jti = xorm_conf.getJavaTableInfo();
		String tablen = jti.getTableName();

		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(tablen).append(" set ");
		sb.append(property_names[0]).append("=?");
		for (int i = 1; i < property_names.length; i++)
		{
			sb.append(',').append(property_names[i]).append("=?");
		}
		sb.append(" where ").append(jti.getPkColumnInfo().getColumnName())
				.append("=?");

		String sql = sb.toString();
		
		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;

		IConnPool cp = ConnPoolMgr.getConnPool(xorm_conf.getBelongToGdb());

		Connection conn = null;
		PreparedStatement ps = null;
		boolean old_autoc = false;
		try
		{
			if(tit!=null)
			{
				conn = tit.getConn(xorm_conf.getBelongToGdb().usingDBName);
			}
			else
			{
				conn = cp.getConnection();
				old_autoc = conn.getAutoCommit();
				conn.setAutoCommit(false);
			}
			
			
			
			
			ps = conn.prepareStatement(sql);

			Hashtable pm = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj);
			if (log.isDebugEnabled())
			{
				log.debug("[updateXORMObjToDBWithHasColNames sql]==" + sql);
				log.debug("input param------------->\n");
				printInParam(pm);
			}

			long tt = System.currentTimeMillis();
			// JavaColumnInfo[] nor_jcis = jti.getNorColumnInfos();
			for (int i = 0; i < property_names.length; i++)
			{
				Object tmpo = prepareObjVal(pm.get(property_names[i]));
				if (tmpo != null)
				{
					ps.setObject(1 + i, tmpo);
				}
				else
				{
					JavaColumnInfo jci = jti
							.getNorColumnInfo(property_names[i]);
					if (jci == null)
						throw new IllegalArgumentException(
								"no nor column found with property name="
										+ property_names[i]);
					ps.setNull(1 + i, jci.getSqlValType());
				}
			}

			ps.setObject(property_names.length + 1, pkid);

			int rowaff = ps.executeUpdate();

			ps.close();
			ps = null;
			
			if(xorm_obj instanceof ISynClientable)
			{
				this.logSynClient(conn, pkid.toString(), (ISynClientable)xorm_obj) ;
			}
			
			 dtAutoUpdate(conn,pkid,xorm_conf,xorm_obj) ;
			
			if(tit==null)
			{
				conn.commit();
				conn.setAutoCommit(old_autoc);
				cp.free(conn);
				conn = null ;
			}
			
			
			return rowaff == 1;
		}
		catch (Exception e)
		{
			log.error(e);
			throw new GdbException(e);
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (Throwable sqle)
			{
			}
			
			try
			{
				if(tit==null)
				{
					if (conn != null)
					{
						conn.rollback();
						conn.setAutoCommit(old_autoc);
						cp.free(conn);
					}
				}
			}
			catch (Throwable sqle)
			{
			}
		}
	}

	/**
	 * [������֧��log]
	 * @param pkid
	 * @param xorm_c
	 * @param property_names
	 * @param prop_vals
	 * @return
	 * @throws ClassNotFoundException
	 * @throws GdbException
	 */
	public boolean updateXORMObjToDBWithHasColNameValues(Object pkid,
			Class xorm_c, String[] property_names, Object[] prop_vals)
			throws ClassNotFoundException, GdbException
	{
		if (property_names == null || property_names.length <= 0
				|| prop_vals == null || prop_vals.length <= 0)
			throw new IllegalArgumentException(
					"xorm property and value cannot be empty!");

		if (property_names.length != prop_vals.length)
			throw new IllegalArgumentException(
					"xorm property and value length must same!");

		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_c.getCanonicalName());

		JavaTableInfo jti = xorm_conf.getJavaTableInfo();
		String tablen = jti.getTableName();

		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(tablen).append(" set ");
		sb.append(property_names[0]).append("=?");
		for (int i = 1; i < property_names.length; i++)
		{
			sb.append(',').append(property_names[i]).append("=?");
		}
		sb.append(" where ").append(jti.getPkColumnInfo().getColumnName())
				.append("=?");

		String sql = sb.toString();
		
		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;

		IConnPool cp = ConnPoolMgr.getConnPool(xorm_conf.getBelongToGdb());

		Connection conn = null;
		PreparedStatement ps = null;
		boolean old_ac = true;
		try
		{
			if(tit!=null)
			{
				conn = tit.getConn(xorm_conf.getBelongToGdb().usingDBName) ;
			}
			else
			{
				conn = cp.getConnection();
				old_ac = conn.getAutoCommit() ;
				conn.setAutoCommit(false);
			}
			
			ps = conn.prepareStatement(sql);

			// Hashtable pm = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj);
			if (log.isDebugEnabled())
			{
				Hashtable pm = new Hashtable();
				for (int k = 0; k < property_names.length; k++)
				{
					if(prop_vals[k]==null)
						continue ;
					pm.put(property_names[k], prop_vals[k]);
				}
				log.debug("[updateXORMObjToDBWithHasColNames sql]==" + sql);
				log.debug("input param------------->\n");
				printInParam(pm);
			}

			long tt = System.currentTimeMillis();
			// JavaColumnInfo[] nor_jcis = jti.getNorColumnInfos();
			for (int i = 0; i < property_names.length; i++)
			{
				Object tmpo = prepareObjVal(prop_vals[i]);
				if (tmpo != null)
				{
					ps.setObject(1 + i, tmpo);
				}
				else
				{
					JavaColumnInfo jci = jti
							.getNorColumnInfo(property_names[i]);
					if (jci == null)
						throw new IllegalArgumentException(
								"no nor column found with property name="
										+ property_names[i]);
					ps.setNull(1 + i, jci.getSqlValType());
				}
			}

			ps.setObject(property_names.length + 1, pkid);

			int rowaff = ps.executeUpdate();

			ps.close();
			ps = null;
			
			logSynClient(conn,pkid,xorm_c);
			
			dtAutoUpdate(conn,pkid,xorm_conf,null) ;
			
			if(tit==null)
			{
				conn.commit();
				conn.setAutoCommit(old_ac);
				cp.free(conn);
				conn = null ;
			}
			
			return rowaff == 1;
		}
		catch (Exception e)
		{
			log.error(e);
			throw new GdbException(e);
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (Throwable sqle)
			{
			}
			
			try
			{
				if(tit==null)
				{
					if (conn != null)
					{
						conn.rollback();
						conn.setAutoCommit(old_ac);
						cp.free(conn);
					}
				}
			}
			catch (Throwable sqle)
			{
			}
		}
	}

	/**
	 * ��XORM��Ӧ��һ��ָ���н��и���
	 * 
	 * [������֧��log]
	 * @param pkid
	 * @param xorm_c
	 * @param propname
	 * @param propval
	 * @return
	 * @throws Exception
	 */
	public boolean saveXORMSingleUpdateCol(long pkid, Class xorm_c,
			String propname, Object propval) throws Exception
	{
		return updateXORMObjToDBWithHasColNameValues(pkid, xorm_c,
				new String[] { propname }, new Object[] { propval });
	}

	public Object loadXORMSingleUpdateCol(Object pikd, Class xorm_c,
			String propname) throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_c.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlstr = null;
		try
		{
			JavaTableInfo jt = xorm_conf.getJavaTableInfo();
			sqlstr = "select " + propname + " from " + jt.getTableName()
					+ " where " + jt.getPkColumnInfo().getColumnName() + "=?";
			conn = cp.getConnection();

			if (log.isDebugEnabled())
			{
				log.debug("gdb loadXORMSingleUpdateCol xorm class=["
						+ xorm_c.getCanonicalName() + "] pk=[" + pikd + "]\r\n"
						+ sqlstr);
			}
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			ps.setObject(1, pikd);

			DBResult dbr = new DBResult();
			rs = ps.executeQuery();
			DataTable dt = dbr.appendResultSet(null,0,rs, 0, -1,null);
			return dt.getFirstColumnOfFirstRow();
		}
		catch (Exception eee)
		{
			throw new GdbException("gdb getXORMObjByPkId error\n" + sqlstr, eee);
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();

				if (ps != null)
					ps.close();
			}
			catch (Throwable sqle)
			{
			}
			
			if (conn != null)
			{
				cp.free(conn);
			}
		}
	}

	/**
	 * ���������XORM����,��Ψһֵ��������. ���»�����Ӧ��һ��
	 * 
	 * ����Ψһֵ�����ڶ�����,������ֵ
	 * 
	 * �÷��������ڲ�֪���Զ�id������,����Ψһֵ���к�ֵ���ж��Ƿ���Ҫ�����ݵĲ��� ����²���
	 * 
	 * �ںܶೡ�Ϻ�����
	 * 
	 * ע��: �÷���Ҫ������Ķ����ܹ�ȷ�����㹻����Ϣ
	 * 
	 * [������֧��log]
	 * 
	 * @param xorm_obj
	 *            ����������㹻��Ϣ�Ķ���
	 * @param unique_col_name
	 *            ��Ϊ���»�׼��Ψһ������,�����ƶ�Ӧ��xorm������,������ֵ
	 * @param tobe_update_cols
	 *            ׼�����µ�������,���=null,���ʾ����ȫ������
	 * @return ԭ����,������˲������,������������л��Զ�����
	 * 
	 * @throws GdbException
	 */
	private Object insertOrUpdateXORMObjWithUniqueColValue(Object xorm_obj,
			String unique_col_name, String[] tobe_update_cols)
			throws GdbException
	{
		Class xorm_objc = xorm_obj.getClass();
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_objc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_obj.getClass().getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();
		
		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<XORMProperty> f_xorm_ps = xorm_conf.getStoreAsFileXORMProps();
		boolean oldau = true;
		try
		{
			Object nid = null ;
			
			if(tit!=null)
				conn = tit.getConn(g.usingDBName) ;
			else
			{
				conn = cp.getConnection();
				if (f_xorm_ps.size() > 0)
				{// ��Ҫ���ļ�����ɹ������ύ
					oldau = conn.getAutoCommit();
					conn.setAutoCommit(false);
				}
			}
			
			String[] sqls = xorm_conf.getInsertWithNewIdReturnSqls();
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			String sqlstr = sqls[0].toString();
			Hashtable pm = XORMUtil.extractXORMObjAsSqlInputParam(xorm_obj);
			
			if (log.isDebugEnabled())
			{
				log.debug("[addXORMObjWithNewId sql]==" + sqlstr);
				log.debug("input param------------->\n");
				printInParam(pm);
			}
			ps = conn.prepareStatement(sqls[0].toString());

			long tt = System.currentTimeMillis();
			int p_sv = 1 ;
			if(sqls.length==1)
			{//�Զ��ַ���ֵ
				nid = newAutoStringKeyVal();
				ps.setObject(1, nid);
				p_sv = 2 ;
			}
			
			JavaColumnInfo[] nor_jcis = jti.getNorColumnInfos();
			for (int i = 0; i < nor_jcis.length; i++)
			{
				Object tmpo = prepareObjVal(pm.get(nor_jcis[i].getColumnName()));
				if (tmpo != null)
					ps.setObject(p_sv + i, tmpo);
				else
					ps.setNull(p_sv + i, nor_jcis[i].getSqlValType());
			}

			ps.execute();

			ps.close();
			
			if(sqls.length==2)
			{
				sqlstr = sqls[1].toString();
				// System.out.println("[addRecord sql]==" + sqlstr);
				ps = conn.prepareStatement(sqlstr);
				rs = ps.executeQuery();
				if (!rs.next())
				{ // empty
					throw new Exception("Cannot get new id!");
				}
				else
				{
					Number num = (Number) rs.getObject(1);
					long nid_long = num.longValue();
	
					if (f_xorm_ps.size() > 0)
					{// ��Ҫ���ļ�����ɹ������ύ
						for (XORMProperty p : f_xorm_ps)
						{
							String pn = p.name();
							byte[] pv = (byte[]) pm.get(pn);
							saveXORMFileCont(xorm_objc, pn, nid_long, pv);
						}
	
					}
					// set new id to pk field
					nid = nid_long;
	
					rs.close();
					rs = null;
	
					ps.close();
					ps = null;
				}
			}
			
			//logSynClient(conn,unique_col_name,)
			
			if(tit==null)
			{
				conn.commit();
				conn.setAutoCommit(oldau);
				
				cp.free(conn);
				conn = null;
			}
			
			Field f = (Field) xorm_conf.getPkXORMPropWrapper()
			.getFieldOrMethod();

			f.setAccessible(true);
			f.set(xorm_obj, nid);
			
			return nid ;
		}
		catch (Exception e)
		{
			log.error(e);
			throw new GdbException(e);
		}
		finally
		{
			try
			{
				if (rs != null)
				{
					rs.close();
				}

				if (ps != null)
				{
					ps.close();
				}
			}
			catch (Throwable sqle)
			{
			}
			
				if(tit==null)
				{
					if (conn != null && f_xorm_ps.size() > 0)
					{
						try
						{
							conn.rollback();
							conn.setAutoCommit(oldau);
						}
						catch (Throwable sqle)
						{
						}
					}
	
					if (conn != null)
						cp.free(conn);
				}
			
		}
	}

	/**
	 * ��������id��XORM��,����ص����ݿ��¼����ɾ��
	 * 
	 * [������֧��log]
	 * @param pkid
	 * @param xorm_c
	 * @throws GdbException
	 */
	public boolean deleteXORMObjFromDB(Object pkid, Class xorm_c)
			throws GdbException
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_c.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		GDBTransInThread tit = GDBTransInThread.getTransInThread() ;

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		Connection conn = null;
		PreparedStatement ps = null;
		
		boolean old_oc = true ;
		try
		{
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			String sql = "delete from " + jti.getTableName() + " where "
					+ jti.getPkColumnInfo().getColumnName() + "=?";
			
			if(tit!=null)
				conn = tit.getConn(g.usingDBName);
			else
			{
				conn = cp.getConnection();
				old_oc = conn.getAutoCommit() ;
				conn.setAutoCommit(false);
			}

			//log syn first,for get client first
			logSynClient(conn,pkid, xorm_c);
			
			//dtAutoUpdate(conn,pkid,xorm_conf,null) ;
			
			boolean b = deleteXORMObjFromDBInConn(conn, pkid, xorm_c);
			if(b && tit==null)
			{
				conn.commit() ;
				conn.setAutoCommit(old_oc);
				cp.free(conn) ;
				conn = null ;
			}
			
			return b;
		}
		catch (Exception e)
		{
			log.error(e);
			throw new GdbException(e);
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (Throwable sqle)
			{
			}
			
			try
			{
				if(tit==null)
				{
					if (conn != null)
					{
						conn.rollback() ;
						conn.setAutoCommit(old_oc);
						cp.free(conn) ;
					}
				}
			}
			catch (Throwable sqle)
			{
			}
		}
	}

	boolean deleteXORMObjFromDBInConn(Connection conn, Object pkid,
			Class xorm_c) throws Exception
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_c.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		PreparedStatement ps = null;
		try
		{
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			String sql = "delete from " + jti.getTableName() + " where "
					+ jti.getPkColumnInfo().getColumnName() + "=?";

			if (log.isDebugEnabled())
			{
				log.debug("[deleteXORMObjFromDB sql]==" + sql);
				log.debug("----pk id=[" + pkid + "]");
			}

			ps = conn.prepareStatement(sql);

			ps.setObject(1, pkid);

			int rowaff = ps.executeUpdate();

			ps.close();

			// delete files whenever succ or failed in del row
			ArrayList<XORMProperty> f_xorm_ps = xorm_conf
					.getStoreAsFileXORMProps();
			if (f_xorm_ps.size() > 0 && pkid instanceof Long)
			{// 
				for (XORMProperty p : f_xorm_ps)
				{
					String pn = p.name();
					saveXORMFileCont(xorm_c, pn, (Long)pkid, (byte[]) null);
				}
			}

			ps = null;

			return rowaff == 1;
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
		}
	}

	int deleteXORMObjByCondWithNoFile(Connection conn,Class xorm_c,String wherestr)
		throws Exception
	{
		if(wherestr==null)
			throw new Exception("where str cannot be null");
		
		wherestr = wherestr.trim() ;
		
		if(wherestr.toLowerCase().startsWith("where "))
			wherestr = wherestr.substring(5).trim() ;
		
		if("".equals(wherestr))
			throw new Exception("where str cannot be empty");
		
		XORM xorm_conf = Gdb.getXORMByGlobal(xorm_c);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xorm_c.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		PreparedStatement ps = null;
		try
		{
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			String sql = "delete from " + jti.getTableName()+" where "
					+ wherestr;

			if (log.isDebugEnabled())
			{
				log.debug("[deleteXORMObjByCond sql]==" + sql);
			}

			ps = conn.prepareStatement(sql);

			//ps.setObject(1, pkid);

			int rowaff = ps.executeUpdate();

			ps.close();

//			// delete files whenever succ or failed in del row
//			ArrayList<XORMProperty> f_xorm_ps = xorm_conf
//					.getStoreAsFileXORMProps();
//			if (f_xorm_ps.size() > 0)
//			{// 
//				for (XORMProperty p : f_xorm_ps)
//				{
//					String pn = p.name();
//					saveXORMFileCont(xorm_c, pn, pkid, (byte[]) null);
//				}
//			}

			ps = null;

			return rowaff;
		}
		finally
		{
			try
			{
				if (ps != null)
				{
					ps.close();
				}
			}
			catch (SQLException sqle)
			{
			}
		}
	}
	
	/**
	 * ��������ɾ��XORM��Ӧ������,�����ر�ɾ����id��
	 * 
	 * [������֧��log]
	 * 
	 * @param xorm_c
	 * @param where_str
	 * @return
	 * @throws Exception
	 */
	public List<Long> deleteXORMObjFromDBWithIdsRet(Class xorm_c,
			String where_str) throws Exception
	{
		if (Convert.isNullOrTrimEmpty(where_str))
			throw new IllegalArgumentException(
					"where condition cannot be empty");

		ArrayList<Long> rets = new ArrayList<Long>();
		List idlist = null;

		int c = 0;
		do
		{// loop delete to limit mem usage
			idlist = this.listXORMPkIds(xorm_c, where_str, null, 0, 200);
			if (idlist == null || idlist.size() <= 0)
				return rets;

			for (Object pk : idlist)
			{
				long pkid = (Long) pk;
				if (!deleteXORMObjFromDB(pkid, xorm_c))//�Ѿ�֧��SynClientLog
					return idlist;

				rets.add(pkid);
				c++;
			}
		}
		while (idlist != null || idlist.size() <= 0);

		return rets;
	}

	/**
	 * ����XORM���о��������ݵ���Ϣ
	 * 
	 * @param xormc
	 * @param where_str
	 *            sql����е�����
	 * @param orderby_propn
	 *            ���������
	 * @return
	 */
	public DataTable listXORMAsTable(Class xormc, String where_str,
			String orderby, int pageidx, int pagesize) throws Exception
	{
		return listXORMObjs(xormc, where_str, orderby, pageidx * pagesize,
				pagesize,null);
	}
	
	public List listXORMAsObjList(Class xormc, String where_str,
			String orderby, int pageidx, int pagesize) throws Exception
	{
		return listXORMAsObjList(xormc, where_str,
				orderby, pageidx, pagesize,null);
	}

	public List listXORMAsObjList(Class xormc, String where_str,
			String orderby, int pageidx, int pagesize,DataOut dout) throws Exception
	{
		DataTable dt = listXORMAsTable(xormc, where_str, orderby, pageidx,
				pagesize);
		if (dt == null)
			return null;

		if(dout!=null)
		{
			dout.pageCur = pageidx ;
			dout.pageSize = pagesize ;
			dout.totalCount = dt.totalCount ;
		}
		return DBResult.transTable2XORMObjList(xormc, dt);
	}
	
	
//	private DataTable listXORMObjs(Class xormc,
//			String[] cols,String[] opers,Object[] vals,
//			String orderby, int idx, int count,IDBSelectCallback cb) throws Exception
//	{
//		return listXORMObjs(xormc,
//				cols,opers,vals,
//				null,
//				orderby, idx, count,cb);
//	}
	
	private DataTable listXORMObjs(Class xormc,
			String[] cols,String[] opers,Object[] vals,
			boolean[] null_ignores,
			String orderby, int idx, int count,IDBSelectCallback cb) throws Exception
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		String sqlstr = xorm_conf.getListXORMSqlByColOpers(cols,opers,vals,null_ignores,orderby);
		if (log.isDebugEnabled())
		{
			log.debug("[listXORMObjs sql]==" + sqlstr);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		DBResult dbr = new DBResult();
		try
		{
			conn = cp.getConnection();
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			
			JavaTableInfo jti = xorm_conf.getJavaTableInfo();
			
			//ps = conn.prepareStatement(sqls[0].toString());

			long tt = System.currentTimeMillis();
			
			for (int i = 0; i < cols.length; i++)
			{
				JavaColumnInfo jci = jti.getColumnInfoByName(cols[i]) ;
				Object tmpo = prepareObjVal(vals[i]) ;
				
				if (tmpo != null)
				{
					if(jci==null)
						ps.setObject(1 + i, tmpo);
					else
						ps.setObject(1+i, tmpo,jci.getSqlValType());
				}
				else
				{
					if(null_ignores!=null&&null_ignores[i])
						continue ;//ignore this val
					if(jci==null)
						throw new Exception("cannot get column with name="+cols[i]) ;
					
					ps.setNull(1 + i, jci.getSqlValType());
				}
			}


			DataTable dt = null;

			if (count > 0)
			{// ��Ҫ֧�ַ�ҳ
				ps.setMaxRows(idx + count);
				// if (sql.fetchSize > 0)//fetch size
				// ��ʾ�����һ�η������ݿ�ȡ�ض�������¼
				// rs.setFetchSize (sql.fetchSize) ;
			}

			rs = ps.executeQuery();
			
			dt = dbr.appendResultSet(null,0,rs, idx, count,cb);

			if (log.isDebugEnabled())
			{
				log.debug("[listXORMObjs result]==\r\n" + dt.toString());
			}

			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			ps.close();
			ps = null;
			return dt;
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
	
				if (ps != null)
					ps.close();
			}
			catch(Throwable sqle)
			{}

			if (conn != null)
				cp.free(conn);
		}
	}
	
	/**
	 * �����У��������ϣ���ֵ�������XORM�б����
	 * @param xormc
	 * @param cols
	 * @param opers
	 * @param vals
	 * @param orderby
	 * @param pageidx
	 * @param pagesize
	 * @param dout
	 * @return
	 * @throws Exception
	 */
	public List listXORMAsObjListByColOperValue(Class xormc,
			String[] cols,String[] opers,Object[] vals,
			
			String orderby, int pageidx, int pagesize,DataOut dout) throws Exception
	{
		return listXORMAsObjListByColOperValue(xormc,
				cols, opers, vals,
				null,
				orderby, pageidx, pagesize,dout);
	}
	
	public List listXORMAsObjListByColOperValue(Class xormc,
			String[] cols,String[] opers,Object[] vals,
			boolean[] null_ignores,
			String orderby, int pageidx, int pagesize,DataOut dout) throws Exception
	{
		DataTable dt = listXORMObjs(xormc, cols,opers,vals,null_ignores,
				orderby, pageidx * pagesize, pagesize,null);
		if (dt == null)
			return null;

		if(dout!=null)
		{
			dout.pageCur = pageidx ;
			dout.pageSize = pagesize ;
			dout.totalCount = dt.totalCount ;
		}
		return DBResult.transTable2XORMObjList(xormc, dt);
	}
	/**
	 * ������������������о�XORM����
	 * 
	 * �����оٹ����У�ͨ���ص�ֱ�Ӵ����Ա����������ռ��̫���ڴ�
	 * @param xormc
	 * @param where_str
	 * @param orderby
	 * @param pageidx
	 * @param pagesize
	 * @param cb
	 * @throws Exception
	 */
	public void listXORMWithXORMObjSelectCallback(Class xormc, String where_str,
			String orderby, int pageidx, int pagesize,IDBSelectObjCallback cb)
		throws Exception
	{
		XORMSelCallback scb = new XORMSelCallback(xormc,cb) ;
		listXORMObjs(xormc, where_str, orderby, pageidx * pagesize,
				pagesize,scb);
	}

	public List listXORMPkIds(Class xormc, String where_str, String orderby,
			int idx, int count) throws Exception
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		String sqlstr = xorm_conf.getListXORMPkIdsSql(where_str, orderby);
		if (log.isDebugEnabled())
		{
			log.debug("[listPkXORMObjs sql]==" + sqlstr);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		DBResult dbr = new DBResult();
		try
		{
			ArrayList<Object> rets = new ArrayList<Object>();
			conn = cp.getConnection();
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			DataTable dt = null;

			if (count > 0)
			{// ��Ҫ֧�ַ�ҳ
				ps.setMaxRows(idx + count);
				// if (sql.fetchSize > 0)//fetch size
				// ��ʾ�����һ�η������ݿ�ȡ�ض�������¼
				// rs.setFetchSize (sql.fetchSize) ;
			}

			rs = ps.executeQuery();
			while (rs.next())
			{
				Object pko = rs.getObject(1);
				if(pko instanceof Number)
					rets.add(((Number)pko).longValue());
				else
					rets.add(pko.toString()) ;
			}

			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			ps.close();
			ps = null;
			
			return rets;
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
	
				if (ps != null)
					ps.close();
			}
			catch(Throwable sqle)
			{}

			if (conn != null)
			{
				cp.free(conn);
			}
		}

	}

	private DataTable listXORMObjs(Class xormc, String where_str,
			String orderby, int idx, int count,IDBSelectCallback cb) throws Exception
	{
		XORM xorm_conf = Gdb.getXORMByGlobal(xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+ xormc.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		String sqlstr = xorm_conf.getListXORMSql(where_str, orderby);
		if (log.isDebugEnabled())
		{
			log.debug("[listXORMObjs sql]==" + sqlstr);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		DBResult dbr = new DBResult();
		try
		{
			conn = cp.getConnection();
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);

			DataTable dt = null;

			if (count > 0)
			{// ��Ҫ֧�ַ�ҳ
				ps.setMaxRows(idx + count);
				// if (sql.fetchSize > 0)//fetch size
				// ��ʾ�����һ�η������ݿ�ȡ�ض�������¼
				// rs.setFetchSize (sql.fetchSize) ;
			}

			rs = ps.executeQuery();
			
			dt = dbr.appendResultSet(null,0,rs, idx, count,cb);

			if (log.isDebugEnabled())
			{
				log.debug("[listXORMObjs result]==\r\n" + dt.toString());
			}

			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			ps.close();
			ps = null;
			return dt;
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
	
				if (ps != null)
					ps.close();
			}
			catch(Throwable sqle)
			{}

			if (conn != null)
				cp.free(conn);
		}

	}
	
	/**
	 * ����������úõĶ������ݿ������䣬�������ݿ⣬����֤���еķ��ʶ���
	 * һ��������
	 * 
	 * [��֧��SynClientLog]
	 * @param m
	 */
	public void accessDBMulti(GDBMulti m) throws Exception
	{
		if(m.connPool==null)
			throw new IllegalArgumentException("no conn pool in GDBMulti,may be no param set");
		
		Connection conn = null;

		boolean b_autocommit = true;

		try
		{
			conn = m.connPool.getConnection();
			b_autocommit = conn.getAutoCommit() ;
			conn.setAutoCommit(false);
			
			for(GDBMulti.IDBAccess dba:m.multiPs)
			{
				dba.accessWithConn(this, m.connPool, conn) ;
			}
			
			conn.commit() ;
			
			conn.setAutoCommit(b_autocommit) ;
			m.connPool.free(conn);
			conn = null;
		}
		finally
		{
			if(conn!=null)
			{
				try
				{
					conn.rollback() ;
					conn.setAutoCommit(b_autocommit) ;
				}
				catch(Throwable sqle)
				{}
				
				m.connPool.free(conn);
				conn = null;
			}
			
			
		}
	}
	
	
	public DataTable statXORM(XORMStatDef sd) throws Exception
	{
		
		Gdb g = sd.xorm.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);

		String sqlstr = sd.calSqlString() ;
		if (log.isDebugEnabled())
		{
			log.debug("[statXORM sql]==" + sqlstr);
		}

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		DBResult dbr = new DBResult();
		try
		{
			conn = cp.getConnection();
			ps = conn.prepareStatement(sqlstr,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			
			JavaTableInfo jti = sd.xorm.getJavaTableInfo();
			
			//ps = conn.prepareStatement(sqls[0].toString());

			long tt = System.currentTimeMillis();
			
			for (int i = 0; i < sd.wherePS.length; i++)
			{
				JavaColumnInfo jci = jti.getColumnInfoByName(sd.wherePS[i]) ;
				Object tmpo = prepareObjVal(sd.whereVals[i]) ;
				if (tmpo != null)
				{
					if(jci==null)
						ps.setObject(1 + i, tmpo);
					else
						ps.setObject(1+i, tmpo,jci.getSqlValType());
				}
				else
				{
					
					if(jci==null)
						throw new Exception("cannot get column with name="+sd.whereVals[i]) ;
					
					ps.setNull(1 + i, jci.getSqlValType());
				}
			}


			DataTable dt = null;

			rs = ps.executeQuery();
			
			dt = dbr.appendResultSet(null,0,rs, 0, -1,null);

			if (log.isDebugEnabled())
			{
				log.debug("[statXORM result]==\r\n" + dt.toString());
			}

			if (rs != null)
			{
				rs.close();
				rs = null;
			}

			ps.close();
			ps = null;
			return dt;
		}
		finally
		{
			try
			{
				if (rs != null)
					rs.close();
	
				if (ps != null)
					ps.close();
			}
			catch(Throwable sqle)
			{}

			if (conn != null)
				cp.free(conn);
		}
	}
	/**
	 * ������־���󣬶Ա������ݿ���и��²���
	 * ���������ڲ�֧�ַ�������粻����ʹ��
	 * @param li
	 * @return
	 * @throws GdbException 
	 */
	void doUpdateByLog(GDBLogItem li) throws GdbException
	{
		IConnPool cp = ConnPoolMgr.getDefaultConnPool() ;

		GDBConn conn = null;

		boolean oldau = true;
		PreparedStatement ps = null ;
		try
		{
			conn = (GDBConn)cp.getConnection();
			
			oldau = conn.getAutoCommit();
			conn.setAutoCommit(false);
			
			ps = conn.prepareStatementNoLog(li.getSqlStr());
			HashMap<Integer,GDBLogParam> id2pms = li.getLogParam() ;
			if(id2pms!=null&&id2pms.size()>0)
			{
				int s = id2pms.size() ;
				for(int i = 1 ; i <= s ; i ++)
				{
					GDBLogParam lp = id2pms.get(i) ;
					lp.setToStatement(ps) ;
				}
			}

			ps.executeUpdate();
		
			ps.close();
			
			ps = null;
			
			conn.commit();
			conn.setAutoCommit(oldau);

			cp.free(conn);
			conn = null;
		}
		catch (Exception e)
		{
			//log.error(e);
			throw new GdbException(e);
		}
		finally
		{
				try
				{
					if(ps!=null)
						ps.close();
				}
				catch (Throwable sqle)
				{
				}
				
				try
				{
					if (conn != null)
					{
						conn.rollback();
						conn.setAutoCommit(oldau);
					}
				}
				catch (Throwable sqle)
				{
				}
				
				if (conn != null)
					cp.free(conn);
		}
		
	}
}