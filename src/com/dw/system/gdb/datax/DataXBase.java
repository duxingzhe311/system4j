package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.db_idx.*;
import com.dw.system.logger.*;
import com.dw.system.xmldata.*;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.xmldata.xrmi.XRmi;

/**
 * 数据库（相当于关系数据库的一个数据库），里面可以用相关的用户，权限限制等信息
 * 
 * 由于每个DataXBase都使用自己的数据库配置和连接池,内部实现要求如果数据库连接在一些错误场合
 * 出现时,只能把问题包含在内部,不能影响其他不相关模块的运行.如果数据库连接恢复,则要求能够自动
 * 恢复.
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
	 * 构造DataXBase对象，它完全基于关系数据库实现
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
	 * 使用前进行初始化，他根据定义的信息进行运行时信息的装配 使得可以被拿来运行
	 * 
	 * @throws Exception
	 */
	public void init(DataXManager dxm) throws Exception
	{
		dataxMgr = dxm ;
//		DBInfo dbi = dbInfo;
//		// 判断是否是缺省Hsql数据库
//		if (dbInfo.dbType == Database.DBType.hsql)
//		{
//			if (dbInfo.connProp == null || dbInfo.connProp.size() <= 0)
//			{
//				dbi = new DBInfo();
//				dbi.dbType = Database.DBType.hsql;
//				Properties p = dbi.connProp;
//				p.setProperty("db.driver", "org.hsqldb.jdbcDriver");
//				// 使用id作为数据库名称
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
	 * 获得该数据库下的所有数据类名称
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
	 * 更加一个类id获得该类可以依赖的其他类
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
	 * 判断一个DataXClass是否可以引用另一个DataXClass——是否产生数据依赖
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
	 * 根据数据库类名称，获得对应的DataX类
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
