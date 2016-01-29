package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.db_idx.*;
import com.dw.system.logger.*;
import com.dw.system.xmldata.*;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * ���ݿ⣨�൱�ڹ�ϵ���ݿ��һ�����ݿ⣩�������������ص��û���Ȩ�����Ƶ���Ϣ
 * 
 * ����ÿ��DataXBase��ʹ���Լ������ݿ����ú����ӳ�,�ڲ�ʵ��Ҫ��������ݿ�������һЩ���󳡺�
 * ����ʱ,ֻ�ܰ�����������ڲ�,����Ӱ�����������ģ�������.������ݿ����ӻָ�,��Ҫ���ܹ��Զ�
 * �ָ�.
 * 
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_base")
public class DataXBase implements IXmlDataable
{
	static ILogger log = LoggerManager.getLogger(DataXBase.class.getCanonicalName());
	
	// private int id = -1;
	// private String name = null ;
	private int id = -1;

	private String name = null;

	//private DBInfo dbInfo = null;
	
	private boolean bSystem = false;

	ArrayList<DataXClass> dataxClass = new ArrayList<DataXClass>();

	//transient private Database database = null;
	transient private DataXManager dataxMgr = null ;

	public DataXBase()
	{
	}

	/**
	 * ����DataXBase��������ȫ���ڹ�ϵ���ݿ�ʵ��
	 * 
	 * @param db
	 */
	public DataXBase(int id, String name, boolean bsys,ArrayList<DataXClass> dxcs)
	{
		this.id = id;
		this.name = name;
		//dbInfo = dbi;
		bSystem = bsys ;
		
		dataxClass = dxcs ;
	}
	
	public DataXBase(int id, String name, boolean bsys)
	{
		this.id = id;
		this.name = name;
		//dbInfo = dbi;
		bSystem = bsys ;
	}

	/**
	 * ʹ��ǰ���г�ʼ���������ݶ������Ϣ��������ʱ��Ϣ��װ�� ʹ�ÿ��Ա���������
	 * 
	 * @throws Exception
	 */
	public void init(DataXManager dxm) throws Exception
	{
		dataxMgr = dxm ;
//		DBInfo dbi = dbInfo;
//		// �ж��Ƿ���ȱʡHsql���ݿ�
//		if (dbInfo.dbType == Database.DBType.hsql)
//		{
//			if (dbInfo.connProp == null || dbInfo.connProp.size() <= 0)
//			{
//				dbi = new DBInfo();
//				dbi.dbType = Database.DBType.hsql;
//				Properties p = dbi.connProp;
//				p.setProperty("db.driver", "org.hsqldb.jdbcDriver");
//				// ʹ��id��Ϊ���ݿ�����
//				p.setProperty("db.url", "jdbc:hsqldb:file:"
//						+ dxm.getDefaultHsqlBase() + id);
//				p.setProperty("db.username", "sa");
//				p.setProperty("db.password", "");
//				p.setProperty("db.initnumber", "2");
//				p.setProperty("db.maxnumber", "5");
//			}
//		}
//
//		database = new Database(dbi);
	}
	
	

	void initNotExistedTable(JavaTableInfo jti) throws Exception
	{
		try
		{
			Database database = getDatabase() ;
			
			if (database.isExistedTable(jti.getTableName()))
				return;
	
			List<String> sqllist = database.getDBSql().getCreationSqls(jti);
	
			String[] sqls = new String[sqllist.size()];
			sqllist.toArray(sqls);
			database.runNoResultSqls(sqls);
		}
		catch(Throwable _t)
		{
			log.error(_t);
		}
	}

	public int getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}


//	public DBInfo getDBInfo()
//	{
//		return dbInfo;
//	}

	/**
	 * 
	 * @param cont_dirbase
	 * @param idxdb
	 */
	public Database getDatabase() throws Exception
	{
		return dataxMgr.getDatabase();
	}

	public IDataXIO getDataXIO()
	{
		return dataxMgr.getDataXIO();
	}
	// public IXmlContentIO getXmlContentIO()
	// {
	// return contIO ;
	// }

	public DataXClass[] getAllDataXClasses()
	{
		DataXClass[] rets = new DataXClass[dataxClass.size()];
		dataxClass.toArray(rets);
		return rets;
	}

	/**
	 * ��ø����ݿ��µ���������������
	 * 
	 * @return
	 */
	public String[] getDataXClassNames()
	{
		String[] rets = new String[dataxClass.size()];
		for (int i = 0; i < rets.length; i++)
		{
			rets[i] = dataxClass.get(i).getName();
		}
		return rets;
	}

	public DataXClass getDataXClassById(int id)
	{
		for (DataXClass dxc : dataxClass)
		{
			if (dxc.getId() == id)
				return dxc;
		}
		return null;
	}

	/**
	 * ����һ����id��ø������������������
	 * @param classid
	 * @return
	 */
	public List<DataXClass> getCanRefOtherClasses(int classid)
	{
		DataXClass dxc = this.getDataXClassById(classid);
		if(dxc==null)
			return null ;
		
		ArrayList<DataXClass> al = new ArrayList<DataXClass>();
		for(DataXClass tmpdxc:dataxClass)
		{
			if(isCanRefClass(dxc,tmpdxc))
				al.add(tmpdxc);
		}
		
		return al ;
	}
	
	/**
	 * �ж�һ��DataXClass�Ƿ����������һ��DataXClass�����Ƿ������������
	 * 
	 * @param dxc
	 * @param be_ref_dxc
	 * @return
	 */
	public boolean isCanRefClass(DataXClass dxc,DataXClass be_ref_dxc)
	{
		if(dxc==be_ref_dxc)
			return false;
		
		DataXClass tmpdxc = be_ref_dxc ;
		while((tmpdxc=getDataXClassById(tmpdxc.getRefClassId()))!=null)
		{
			if(tmpdxc==dxc)
				return false;
			if(tmpdxc==be_ref_dxc)
				return false;
		}
		
		return true ;
	}
	/**
	 * �������ݿ������ƣ���ö�Ӧ��DataX��
	 * 
	 * @param classn
	 * @return
	 */
	public DataXClass getDataXClassByName(String classn)
	{
		for (DataXClass dxc : dataxClass)
		{
			if (dxc.getName().equals(classn))
				return dxc;
		}
		return null;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("id", id);
		xd.setParamValue("name", name);
		xd.setParamValue("is_sys", bSystem);
		//xd.setSubDataSingle("db_info", dbInfo.toXmlData());

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueInt32("id", -1);
		name = xd.getParamValueStr("name");
		bSystem = xd.getParamValueBool("is_sys", false);
//		XmlData tmpxd = xd.getSubDataSingle("db_info");

//		dbInfo = new DBInfo();
//		dbInfo.fromXmlData(tmpxd);
	}

//	public void close()
//	{
//		if (database != null)
//			this.database.close();
//	}
	
	
}
