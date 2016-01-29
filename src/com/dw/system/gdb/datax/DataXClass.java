package com.dw.system.gdb.datax;

import com.dw.system.gdb.datax.DataXIndex.JavaColumnAStructItem;
import com.dw.system.gdb.db_idx.*;
//import com.dw.system.gdb.datax.db.connpool.IConnPoolToucher;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.gdb.connpool.IConnPoolToucher;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.xrmi.XRmi;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * 数据类——代表数据空间中的同类数据存放的集合（对应关系数据库中的表）
 * 数据类通过定义自身的数据结构和索引信息（Schema）类达到存储数据和快速查找的效果
 * DataX类之间可以有依赖关系
 * 
 * @author Jason Zhu
 */
@XRmi(reg_name = "datax_class")
public class DataXClass implements IXmlDataable,IConnPoolToucher
{
	public static final String COL_XD_ID = "xd_id";
	public static final String COL_REF_ID = "ref_id";
	public static final String COL_ADD_DATE = "add_date";
	public static final String COL_CHANGE_DATE = "change_date";
	public static final String COL_MAIN_CONT = "main_cont";
	
	private static ILogger log = LoggerManager.getLogger(DataXClass.class.getCanonicalName());
	
	transient DataXBase dataxBase = null;

	private int id = -1;

	/**
	 * 引用其他DataXClass的id。 如果从被引用的数据id对应ID被删除，则本类中相关的数据也会被删除
	 * 类比：关系数据库的外键
	 */
	int refId = -1;

	String name = null;
	
	/**
	 * XmlData缓冲对象长度。
	 * 如果该长度设置恰当，可以加快XmlData的数据访问
	 * TODO : 以后实现XmlData缓冲对象长度
	 */
	int bufferLen = 0 ;

	XmlDataStruct dataStruct = null;// new XmlDataStruct() ;
	
	/**
	 * 该数据分类是否是文件,如果是,则Xml信息仅仅是文件的索引信息.
	 * 主内容都存储在文件系统中.
	 */
	boolean isFileClass = false;
	/**
	 * 如果该类存储文件,则判断是否要记录文件修改历史
	 */
	boolean isKeepFileHis = false;

	transient List<DataXIndex> dataXIndex = new ArrayList<DataXIndex>();
	
	transient List<DataXForm> dataXForm = new ArrayList<DataXForm>();

	public DataXClass(int id, int refid, String name, XmlDataStruct xds)
	{
		this.id = id;
		refId = refid;
		this.name = name;
		dataStruct = xds;
	}

	public DataXClass()
	{

	}

	public boolean equals(Object o)
	{
		if (!(o instanceof DataXClass))
			return false;

		DataXClass dxc = (DataXClass) o;
		if (id != dxc.id)
			return false;

		if (!name.equals(dxc.name))
			return false;

		if (!dataStruct.equals(dxc.dataStruct))
			return false;

		if (dataXIndex.size() != dxc.dataXIndex.size())
			return false;

		for (DataXIndex dxi : dataXIndex)
		{
			DataXIndex odxi = dxc.getDataXIndexById(getId());
			if (odxi == null)
				return false;

			if (!dxi.equals(odxi))
				return false;
		}

		return true;
	}

	boolean checkExistedTable() throws Exception
	{
		Database database = dataxBase.getDatabase() ;
		
		return database.isExistedTable(getTableName()) ;
	}
	/**
	 * 对象被创建后，如果通过要被使用前，init()必须被调用
	 * 
	 * 该方法查找数据库中的内容,查找表是否存在，如果不存在，则建立。
	 */
	public void init() throws Exception
	{
		dataxBase.getDatabase().getConnPool().putToucher(this);
	}

	public void OnMeBeTouched()
	{
		try
		{
			JavaTableInfo jti = getJavaTableInfo();

			dataxBase.initNotExistedTable(jti);

			DataXIndex[] xdis = getDataXIndexes();
			for (DataXIndex idx : xdis)
			{
				JavaTableInfo idxjti = idx.getJavaTableInfo();
				dataxBase.initNotExistedTable(idxjti);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			log.error(e);
		}
	}
	/**
	 * 检查当前数据结构的设置和索引的定义是否有冲突
	 * 
	 * @param failedreson
	 * @return
	 */
	public boolean checkValid(StringBuffer failedreson)
	{
		if (dataStruct == null)
		{
			failedreson.append("not data struct defined!");
			return false;
		}
		if (dataXIndex == null || dataXIndex.size() <= 0)
		{
			return true;
		}

		XmlDataStruct xds = getDataStruct();
		for (DataXIndex dxi : dataXIndex)
		{
			if (!dxi.checkValid(xds, failedreson))
				return false;
		}
		return true;
	}

	/**
	 * 对索引表进行drop，并扫描所有内容，对该索引表内容进行重新生成
	 * 
	 */
	void recreateIndex(int idxid) throws Exception
	{
		DataXIndex dxi = delIndex(idxid);
		JavaTableInfo jti = dxi.getJavaTableInfo();
		// recreate index table
		dataxBase.initNotExistedTable(jti);

		// 提取现有的数据，并生成对应的索引信息加入到表中
		PreparedStatement stat = null;
		Connection conn = null;
		try
		{
			conn = dataxBase.getDatabase().getConnection();
			String sql = "select "+COL_XD_ID+","+COL_MAIN_CONT+" from " + getTableName();
			stat = conn.prepareStatement(sql);

			ResultSet rs = stat.executeQuery();
			while (rs.next())
			{ // empty
				long id = rs.getLong(1);
				byte[] cont = (byte[]) rs.getObject(2);
				if (cont == null)
					continue;

				XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");
				dxi.OnSetXmlData(conn, id, xd);

				// conn.prepareStatement("update " + getTableName() + " set ");
			}
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				dataxBase.getDatabase().freeConnection(conn);
			}
		}
	}
	
	DataXIndex delIndex(int idxid) throws Exception
	{
		DataXIndex dxi = this.getDataXIndexById(idxid);
		if (dxi == null)
			throw new Exception("cannot find idx with id=" + idxid);

		// drop index table
		JavaTableInfo jti = dxi.getJavaTableInfo();
		String dropsql = "drop table " + jti.getTableName();
		dataxBase.getDatabase().runNoResultSqls(new String[] { dropsql });
		return dxi;
	}
	
	/**
	 * 删除主表
	 *
	 */
	void delMainTable() throws Exception
	{
		String dropsql = "drop table " + getTableName();
		dataxBase.getDatabase().runNoResultSqls(new String[] { dropsql });
	}
	/**
	 * 改变DataXClass的分离存储结构
	 * 
	 * 步骤：1，扫描整个表，把分离存储的数据和cont数据组合并保存
	 * 	    2，删除旧的分离存储列
	 * 		3，新建新的分离存储列
	 * 		4，扫描整个表，从cont数据中提取出分离存储数据，并写入到新列中
	 * @param dxc
	 * @param oldxds
	 * @param newxds
	 * @throws Exception
	 */
	void changeXmlDataStruct(XmlDataStruct newxds)
		throws Exception
	{//改变
		XmlDataStruct oldxds = dataStruct ;
		List<XmlDataMember> oldsis = oldxds.getSeparateXmlValDefs() ;
		List<XmlDataMember> newsis = newxds.getSeparateXmlValDefs() ;
		
		//判断是否真的不一致
		boolean equal = true;
		
		equal = (oldsis.size()==newsis.size());
		if(equal)
		{
			Hashtable<String,XmlDataMember> oldp2si = new Hashtable<String,XmlDataMember>();
			for(XmlDataMember si:oldsis)
			{
				oldp2si.put(si.getPath(), si);
			}
			
			for(XmlDataMember newsi:newsis)
			{
				String npath = newsi.getPath() ;
				XmlDataMember oldsi = oldp2si.get(npath);
				if(oldsi==null)
				{
					equal = false ;
					break;
				}
				
				if(!oldsi.equalsByBelongTo(newsi))
				{
					equal = false;
					break;
				}
			}
		}
		
		if(equal)
		{
			dataStruct = newxds ;
			javaTableInfo = null ;
			return ;
		}
		
		//开始做上面的步骤
		
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		
		if(sepcols!=null&&sepcols.size()>0)
		{
			//扫描整个表，把分离存储的数据和cont数据组合并保存
			absorbSeparateData();
			
			//删除旧的列
			if(sepcols!=null&&sepcols.size()>0)
			{
				ArrayList<String> dropcolsqls = new ArrayList<String>();
				DbSql dbs = dataxBase.getDatabase().getDBSql();
				JavaTableInfo jti = getJavaTableInfo();
				for(JavaColumnAStructItem jcsi:sepcols)
				{
					if(jcsi.columnInfo.hasIdx())
					{
						dropcolsqls.add(dbs.constructDropIndex(jti, jcsi.columnInfo).toString());
					}
					dropcolsqls.add("ALTER TABLE "+getTableName()+" DROP COLUMN "+jcsi.columnInfo.getColumnName());
				}
				dataxBase.getDatabase().runNoResultSqls(dropcolsqls);
			}
		}
		
		
		dataStruct = newxds ;
		javaTableInfo = null ;
		
		sepcols = xmlDataStructStoreSep() ;
		if(sepcols!=null&&sepcols.size()>0)
		{
//			建立新的列
			ArrayList<String> addcolsqls = new ArrayList<String>();
			JavaTableInfo jti = this.getJavaTableInfo() ;
			DbSql dbs = dataxBase.getDatabase().getDBSql() ;
			int c = sepcols.size();
			for(int i = 0 ; i < c ; i ++)
			{
				JavaColumnInfo jci = sepcols.get(i).columnInfo;
				addcolsqls.add(dbs.constructAddColumnToTable(jti, jci).toString());
				if(jci.hasIdx())
				{
					addcolsqls.add(dbs.constructIndexTable(jti,jci).toString()) ;
				}
			}
			dataxBase.getDatabase().runNoResultSqls(addcolsqls);
			
			//扫描整个表，从cont数据中提取出分离存储数据，并写入到新列中
			dispatchSeparateData();
		}
	}
	
	/**
	 * 吸收分离存储数据到主数据cont中
	 * 
	 * 该方法扫描整个表，把主数据和分离数据进行组合，并更新到主数据中
	 * @throws Exception
	 */
	void absorbSeparateData() throws Exception
	{
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		if(sepcols==null||sepcols.size()<=0)
			return ;
		
		PreparedStatement stat = null;
		Connection conn = null;
		ResultSet rs = null ;
		try
		{
			conn = dataxBase.getDatabase().getConnection();
			StringBuffer tmpsb = new StringBuffer();
			tmpsb.append("select ").append(COL_XD_ID).append(",").append(COL_MAIN_CONT);
			for(JavaColumnAStructItem sepcol:sepcols)
			{
				tmpsb.append(',').append(sepcol.columnInfo.getColumnName());
			}
			
			tmpsb.append(" from ").append(getTableName());
			String sql = tmpsb.toString();
			stat = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

			rs = stat.executeQuery();
			while (rs.next())
			{ // empty
				long id = rs.getLong(1);
				
				XmlContItem xci = new XmlContItem();
				xci.mainCont = (byte[]) rs.getObject(2);
				xci.sepStoreCols = sepcols;
				int c = sepcols.size();
				for(int i = 0 ; i < c ; i ++)
				{
					Object o = rs.getObject(3+i);
					if(o!=null)
						xci.col2Val.put(sepcols.get(i), o);
				}
				
				XmlData xd = xci.constructXmlData() ;
				byte[] buf = null;
				if(xd!=null)
					buf = xd.toXmlString().getBytes("UTF-8");
				
				rs.updateObject(2, buf);
				rs.updateRow();
			}
		}
		finally
		{
			if(rs!=null)
			{
				rs.close();
			}
			
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				dataxBase.getDatabase().freeConnection(conn);
			}
		}
	}
	
	/**
	 * 和absorbSeparateData相反
	 * 扫描整个表，从cont数据中提取出分离存储数据，并写入到列中
	 * @throws Exception
	 */
	void dispatchSeparateData() throws Exception
	{
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		if(sepcols==null||sepcols.size()<=0)
			return ;
		
		PreparedStatement stat = null;
		Connection conn = null;
		ResultSet rs = null ;
		try
		{
			conn = dataxBase.getDatabase().getConnection();
			StringBuffer tmpsb = new StringBuffer();
			tmpsb.append("select ").append(COL_XD_ID).append(",").append(COL_MAIN_CONT);
			for(JavaColumnAStructItem sepcol:sepcols)
			{
				tmpsb.append(',').append(sepcol.columnInfo.getColumnName());
			}
			
			tmpsb.append(" from ").append(getTableName());
			String sql = tmpsb.toString();
			stat = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

			rs = stat.executeQuery();
			while (rs.next())
			{ // empty
				long id = rs.getLong(1);
				
				XmlContItem xci = new XmlContItem();
				byte[] cont = (byte[]) rs.getObject(2);
				XmlData xd = null ;
				if(cont!=null)
					xd = XmlData.parseFromByteArray(cont, "UTF-8");
				int c = sepcols.size();
				boolean brm = false;
				for(int i = 0 ; i < c ; i ++)
				{
					Object o = null;
					if(xd!=null)
					{
						//o = xd.getSingleParamValueByPath(sepcols.get(i).getMemberPath().getPath());
						o = xd.removeSingleParamValueByPath(sepcols.get(i).getMemberPath().getPath());
						if(o!=null)
							brm = true ;
					}
					rs.updateObject(3+i,o);
				}
				
				if(brm)
				{//更新主内容（保存删除了分离存储的信息）－－这一步可以省略
					//一开始为了方便调试，先省略
					//rs.updateObject(2, xd.toXmlString().getBytes("UTF-8"));
				}
				
				if(c>0)
					rs.updateRow();
			}
		}
		finally
		{
			if(rs!=null)
			{
				rs.close();
			}
			
			if (stat != null)
			{
				stat.close();
			}

			if (conn != null)
			{
				dataxBase.getDatabase().freeConnection(conn);
			}
		}
	}

	public int getId()
	{
		return id;
	}

	public int getRefClassId()
	{
		return refId;
	}
	
	/**
	 * 得到改类依赖及依赖的依赖集合
	 * @return
	 */
	public HashSet<DataXClass> getRefClassSet()
	{
		if(refId<=0)
			return null ;
		
		HashSet<DataXClass> hs = new HashSet<DataXClass>();
		
		DataXClass refdxc = this ;
		do
		{
			refdxc = dataxBase.getDataXClassById(refdxc.refId) ;
			if(refdxc!=null)
				hs.add(refdxc);
		}
		while(refdxc!=null);
		
		return hs ;
	}

	public DataXBase getDataXBase()
	{
		return dataxBase;
	}

	void setDataXBase(DataXBase dxb)
	{
		dataxBase = dxb;
	}

	public XmlDataStruct getDataStruct()
	{
		return dataStruct;
	}
	
	public void setDataStruct(XmlDataStruct xds)
	{
		dataStruct = xds ;
	}

	public String getName()
	{
		return name;
	}

	public String getTableName()
	{
		if (id < 0)
			throw new RuntimeException("invalid DataXClass id");

		return "dx_" + id;
	}
	
	public String[] getSeparateColumnNames()
	{
		List<JavaColumnAStructItem> lls = xmlDataStructStoreSep();
		if(lls==null)
			return null ;
		
		String[] rets = new String[lls.size()];
		for(int i = 0 ; i < rets.length ; i ++)
		{
			rets[i] = lls.get(i).columnInfo.getColumnName();
		}
		return rets;
	}
	
	
	public String getRefForeignKeyName()
	{
		if (refId <= 0)
			return null;

		return "fk_ref_" + id + "_" + refId;
	}

	public DataXIndex[] getDataXIndexes()
	{
		if (dataXIndex == null)
			return new DataXIndex[0];

		DataXIndex[] rets = new DataXIndex[dataXIndex.size()];
		dataXIndex.toArray(rets);
		return rets;
	}

	public void setDataXIndex(List<DataXIndex> dxis)
	{
		XmlDataStruct xds = getDataStruct();
		for (DataXIndex dxi : dxis)
		{
			StringBuffer failedreson = new StringBuffer();
			if (!dxi.checkValid(xds, failedreson))
				throw new IllegalArgumentException("Invalid DataX Index["
						+ dxi.getName() + "] " + failedreson.toString());
		}

		dataXIndex = dxis;
	}

	public DataXIndex getDataXIndexById(int id)
	{
		for (DataXIndex dxi : dataXIndex)
		{
			if (dxi.getId() == id)
				return dxi;
		}

		return null;
	}

	void setDataXIndex(DataXIndex dxi)
	{
		int id = dxi.getId();
		if (id <= 0)
			return;

		int c = dataXIndex.size();
		for (int i = 0; i < c; i++)
		{
			DataXIndex tmpdxi = dataXIndex.get(i);
			if (tmpdxi.getId() == id)
			{
				dataXIndex.set(i, dxi);
				return;
			}
		}

		dataXIndex.add(dxi);
	}

	void unsetDataXIndex(int id)
	{
		DataXIndex dxi = getDataXIndexById(id);
		if (dxi == null)
			return;

		dataXIndex.remove(dxi);
	}

	/**
	 * 根据名称获得DataX索引
	 * 
	 * @param idxname
	 * @return
	 */
	public DataXIndex getDataXIndex(String idxname)
	{
		for (DataXIndex dxi : dataXIndex)
		{
			if (dxi.getName().equals(idxname))
				return dxi;
		}

		return null;
	}

	private transient JavaTableInfo javaTableInfo = null;

	public JavaTableInfo getJavaTableInfo()
	{
		if (javaTableInfo != null)
			return javaTableInfo;

		JavaColumnInfo pkcol = new JavaColumnInfo(COL_XD_ID,true,
				XmlVal.XmlValType.vt_int64, -1, true, true, true,-1);

		ArrayList<JavaColumnInfo> jcis = new ArrayList<JavaColumnInfo>();

		// 外键引用id
		jcis.add(new JavaColumnInfo(COL_REF_ID,false, XmlVal.XmlValType.vt_int64, -1,
				true, false, true,-1));

		jcis.add(new JavaColumnInfo(COL_ADD_DATE,false, XmlVal.XmlValType.vt_int64, -1,
				false, false, false,-1));
		jcis.add(new JavaColumnInfo(COL_CHANGE_DATE,false, XmlVal.XmlValType.vt_int64,
				-1, false, false, false,-1));
		
		jcis.add(new JavaColumnInfo(COL_MAIN_CONT,false, XmlVal.XmlValType.vt_byte_array,
				-1, false, false, false,-1));
		
		//根据XmlDataStruct中的StoreType定义分离存储数据库的列
		List<JavaColumnAStructItem> jcsis = xmlDataStructStoreSep();
		for(JavaColumnAStructItem jcsi:jcsis)
		{
			jcis.add(jcsi.columnInfo);
		}

		ArrayList<JavaForeignKeyInfo> fkinfos = null;
		if (refId > 0)
		{// 引用删除外键
			DataXClass refdxc = dataxBase.getDataXClassById(refId);
			if(refdxc!=null)
			{
				fkinfos = new ArrayList<JavaForeignKeyInfo>();
				fkinfos.add(new JavaForeignKeyInfo(COL_REF_ID, refdxc.getTableName(), COL_XD_ID));
			}
		}

		javaTableInfo = new JavaTableInfo(getTableName(), pkcol, jcis, fkinfos);
		return javaTableInfo;
	}

	private List<JavaColumnAStructItem> xmlDataStructStoreSep()
	{
		return xmlDataStruct2StoreSepCols(dataStruct);
	}
	
	/**
	 * 根据XmlDataStruct结构，获取分离存储的数据库列
	 * @param xds
	 * @return
	 */
	public static List<JavaColumnAStructItem> xmlDataStruct2StoreSepCols(XmlDataStruct xds)
	{
		List<JavaColumnAStructItem> tmpjcis = new ArrayList<JavaColumnAStructItem>();
		List<XmlDataMember> sis = xds.getSeparateXmlValDefs();
		for(XmlDataMember si:sis)
		{
			XmlDataPath xdp = new XmlDataPath(si.getPath());
			
			XmlDataStruct.StoreType st = si.getStoreTypeWithBelongTo();
			if(st==XmlDataStruct.StoreType.Separate)
				tmpjcis.add(new JavaColumnAStructItem(null,xdp,si,false));
			else if(st==XmlDataStruct.StoreType.SeparateIdx)
				tmpjcis.add(new JavaColumnAStructItem(null,xdp,si,true));
		}
		
		return tmpjcis ;
	}
	
	/**
	 * 根据数据id值获得具体的XmlData数据对象
	 * 
	 * 该方法是一个数据类的主要方法
	 * 
	 * @param did
	 * @return
	 * @throws Exception
	 */
	public XmlData getXmlDataById(long did) throws Exception
	{
		if (did <= 0)
			throw new IllegalArgumentException("invalid xml data id");

		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();

			XmlContItem xci = loadXmlContentIO(conn, did);
			if (xci == null)
				return null;

			return xci.constructXmlData();
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	/**
	 * 设置XmlData到类中
	 * 
	 * 该方法会自动更新设置的索引
	 * 
	 * @param did
	 * @param xd
	 */
	public void saveXmlData(long did, long ref_xdid, XmlData xd)
			throws Exception
	{
		if (did <= 0)
			throw new IllegalArgumentException("invalid xml data id");

		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();

			if (xd == null)
			{// remove it
				conn.setAutoCommit(false);

				delXmlContIO(conn, did);
				for (DataXIndex idx : dataXIndex)
				{
					idx.OnDelXmlData(conn, did);
				}

				conn.commit();
				conn.setAutoCommit(true);
				db.freeConnection(conn);
				conn = null;
				return;
			}

			StringBuffer fr = new StringBuffer();
			if (!dataStruct.checkMatchStruct(xd, fr))
			{
				throw new Exception(fr.toString());
			}

			//byte[] buf = xd.toXmlString().getBytes("UTF-8");

			conn.setAutoCommit(false);
			
			//TODO 先判断旧数据和新数据
			
			updateXmlContIO(conn, did, ref_xdid, xd);
			for (DataXIndex idx : dataXIndex)
			{
				idx.OnSetXmlData(conn, did, xd);
			}

			conn.commit();
			conn.setAutoCommit(true);
			db.freeConnection(conn);
			conn = null;
		}
		finally
		{
			if (conn != null)
			{
				conn.rollback();
				conn.setAutoCommit(true);
				db.freeConnection(conn);
			}
		}
	}
	
	public void changeFileName(long did,String filename)
	throws Exception
	{
		if(!isFileClass)
			throw new Exception("datax class is not for file!");
	}
	
	public void saveFileXmlData(long did, long ref_xdid, XmlData xd,String filename,byte[] cont)
	throws Exception
{
		if(!isFileClass)
			throw new Exception("datax class is not for file!");
		
if (did <= 0)
	throw new IllegalArgumentException("invalid xml data id");

Connection conn = null;
Database db = dataxBase.getDatabase();
try
{
	conn = db.getConnection();

	if (xd == null)
	{// remove it
		//先删除文件
		this.delFile(did);
		
		conn.setAutoCommit(false);

		delXmlContIO(conn, did);
		for (DataXIndex idx : dataXIndex)
		{
			idx.OnDelXmlData(conn, did);
		}

		conn.commit();
		conn.setAutoCommit(true);
		db.freeConnection(conn);
		conn = null;
		return;
	}

	StringBuffer fr = new StringBuffer();
	if (!dataStruct.checkMatchStruct(xd, fr))
	{
		throw new Exception(fr.toString());
	}

	//xd.setParamValue(pn, v)

	conn.setAutoCommit(false);
	
	//TODO 先判断旧数据和新数据
	
	updateXmlContIO(conn, did, ref_xdid, xd);
	for (DataXIndex idx : dataXIndex)
	{
		idx.OnSetXmlData(conn, did, xd);
	}

	conn.commit();
	conn.setAutoCommit(true);
	db.freeConnection(conn);
	conn = null;
}
finally
{
	if (conn != null)
	{
		conn.rollback();
		conn.setAutoCommit(true);
		db.freeConnection(conn);
	}
}
}

	/**
	 * 根据一个指向单值成员的路径，更新对应的数据值
	 * 如果对应的路径被定义成分离存储的话，则只需要更新分离存储的内容即可
	 * 性能较好
	 * 如果不是，则需要读取旧的数据，更新以后再保存
	 * @param path 指向单值成员的路径
	 * @param v 对应的值
	 */
	public void updateSingleValueByPath(long dxid,String path,Object v)
		throws Exception
	{
		XmlDataPath xdp = new XmlDataPath(path);
		if(xdp.isStruct())
			throw new IllegalArgumentException("invalid value path,it is a struct path");
		
		XmlDataMember si = this.dataStruct.getXmlDataMemberByPath(xdp.getPath());
		if(si==null)
			throw new Exception("cannot find single memeber with path="+path);
		
		if(si.isArrayWithBelongTo())
			throw new Exception("the memeber with path="+path+" is array!");
		
		XmlDataStruct.StoreType st = si.getStoreTypeWithBelongTo();
		if(st==XmlDataStruct.StoreType.Normal)
		{
			//？？？？？？？？？？
		}
	}
	
	/**
	 * 根据条件更新分离数据的一些值,其中要求被更新的路径和值必须形如:
	 * 	{/state}={@/st:int32=1}  --也就是说左边列必须是路径,右边必须是输入变量
	 * 
	 * update_sep b.c [set] {/state}={@/st:int32=1} where xd_id=111
	 * @param sep_paths 被更新的路径
	 * @param sqlstrvals sql语句字符串表示的值
	 * @param wherestr
	 */
	public int updateSeparateValueByCond(
			String[] sep_paths,Object[] setcol_vals,
			String wherestr,Object[] where_vals)
		throws Exception
	{
		if(sep_paths.length!=setcol_vals.length)
			throw new IllegalArgumentException("set separate path number must equals setcol vals") ;
		//判断被更新的路径是否是合法的
		String[] cols = new String[sep_paths.length];
		
		for(int i = 0 ; i < sep_paths.length ; i ++)
		{
			XmlDataMember si = dataStruct.getSingleXmlDataMemberByPath(sep_paths[i]);
			if(si==null)
				throw new Exception("no single StructItem with path="+sep_paths[i]);
			
			XmlDataStruct.StoreType st = si.getStoreTypeWithBelongTo();
			if(st!=XmlDataStruct.StoreType.Separate&&st!=XmlDataStruct.StoreType.SeparateIdx)
				throw new Exception("not separate StructItem with path="+sep_paths[i]);
			
			XmlDataPath xdp = new XmlDataPath(sep_paths[i]);
			String coln = xdp.toColumnName() ;
			cols[i] = coln + "=?";
		}
		
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
			return updateSeparateData(conn,cols,setcol_vals,wherestr,where_vals);
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}
	/**
	 * 增加一个新的Xml数据
	 * 
	 * 该方法会自动更新设置的索引
	 * 
	 * @param xd
	 * @return
	 * @throws Exception
	 */
	public long addNewXmlData(long refid, XmlData xd) throws Exception
	{
		if (xd == null)
			throw new IllegalArgumentException(
					"add new xml data cannot be null");

		StringBuffer failr = new StringBuffer();
		if (!dataStruct.checkMatchStruct(xd, failr))
			throw new Exception(failr.toString());
		
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			//System.out.println("--------222 before getConn");
			conn = db.getConnection();
			
			//System.out.println("--------222 before setAutoCommit");
			conn.setAutoCommit(false);

			//System.out.println("--------222 before insertXmlContentIO");
			long retid = insertXmlContentIO(conn, refid, xd);
			if (retid <= 0)
				throw new Exception("add xml content io error!");

			// update index
			for (DataXIndex idx : dataXIndex)
			{//System.out.println("--------222 before OnSetXmlData");
				idx.OnSetXmlData(conn, retid, xd);
			}
			//System.out.println("--------222 before commit");
			
			conn.commit();
			conn.setAutoCommit(true);
			db.freeConnection(conn);
			conn = null;
			return retid;
		}
		finally
		{
			if (conn != null)
			{
				conn.rollback();
				conn.setAutoCommit(true);
				db.freeConnection(conn);
			}
		}
	}

	public long addNewFileXmlData(long refid, XmlData xd,String filename,byte[] filecont) throws Exception
	{
		if(!isFileClass)
			throw new Exception("datax class is not for file!");
		
		if(filename==null||(filename=filename.trim()).equals(""))
			throw new IllegalArgumentException("file name cannot be null or empty!");
		
		if(filecont==null)
			throw new IllegalArgumentException("file content cannot be null!");
		
		if (xd == null)
			xd = new XmlData();
		
		xd.setParamValue("file_name", filename);
		xd.setParamValue("file_modify", new Date());

		StringBuffer failr = new StringBuffer();
		if (!dataStruct.checkMatchStruct(xd, failr))
			throw new Exception(failr.toString());
		
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
//			保存文件信息先
			long fid = addNewFile(filecont);
			xd.setParamValue("file_id", fid);
			
			conn.setAutoCommit(false);

			
			//System.out.println("--------222 before insertXmlContentIO");
			long retid = insertXmlContentIO(conn, refid, xd);
			if (retid <= 0)
				throw new Exception("add xml content io error!");

			// update index
			for (DataXIndex idx : dataXIndex)
			{//System.out.println("--------222 before OnSetXmlData");
				idx.OnSetXmlData(conn, retid, xd);
			}
			
			conn.commit();
			conn.setAutoCommit(true);
			db.freeConnection(conn);
			conn = null;
			return retid;
		}
		finally
		{
			if (conn != null)
			{
				conn.rollback();
				conn.setAutoCommit(true);
				db.freeConnection(conn);
			}
		}
	}
	
	/**
	 * 对一个已经存在的XmlData的特定路径下,增加子结构内容.
	 * @param dataid 已经存在的数据id
	 * @param parentp 子结构存放的路径
	 * @param subxd 对应的数据
	 */
	public void appendSubXmlData(long dataid,String parentp,XmlData subxd)
	{
		//判断输入的数据是否结构满足结构的定义
		
		//判断是否是分离存储
		
		//
	}
	/**
	 * 根据索引条件和排序查找对应的XmlData
	 * 
	 * @param idxname
	 * @param wherestr
	 * @param orderbystr
	 * @param pageidx
	 * @param pagesize
	 * @return
	 * @throws Exception
	 */
	public DataXItemList findByIdx(String idxname, String wherestr,Object[] where_vals,
			String orderbystr, int pageidx, int pagesize) throws Exception
	{
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
			int[] cc = new int[1];
			List<Long> ids = loadXmlDataIdsByIndex(conn, idxname, wherestr,where_vals,
					orderbystr, pageidx, pagesize, cc);

			List<DataXItem> rets = new ArrayList<DataXItem>(ids.size());
			for (Long l : ids)
			{
				XmlData xd = getXmlDataById(l);
				if (xd == null)
					continue;

				rets.add(new DataXItem(l, xd));
			}

			DataXItemList dxil = new DataXItemList(rets, pageidx, pagesize,
					cc[0]);
			return dxil;
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}
	
	public DataXItemList findBySeparateStore(String wherestr,Object[] where_vals,
			String orderbystr, int pageidx, int pagesize) throws Exception
	{
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
				
			int[] cc = new int[1];
			
			List<XmlContItem> xcis = loadXmlDataBySeparateStore(conn,
					wherestr,where_vals, orderbystr, pageidx, pagesize,cc);
			
			List<DataXItem> rets = new ArrayList<DataXItem>(xcis.size());
			for(XmlContItem xci:xcis)
			{
				rets.add(xci.constructDataXItem());
			}

			DataXItemList dxil = new DataXItemList(rets, pageidx, pagesize,
					cc[0]);
			return dxil;
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
		
	}
	
	public XmlDataList queryIdxByIdx(String idxname,
			String[] cols,
			String wherestr,Object[] where_vals,
			String groupbystr,
			String orderbystr, int pageidx, int pagesize) throws Exception
	{
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
			return this.queryIdxByIndex(conn, idxname, cols, wherestr,where_vals, groupbystr, orderbystr, pageidx, pagesize);
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}
	
	public DataXItemList queryIdxBySeparate(
			String wherestr,Object[] where_vals,String orderbystr, int pageidx, int pagesize) throws Exception
	{
		Connection conn = null;
		Database db = dataxBase.getDatabase();
		try
		{
			conn = db.getConnection();
			int c[] = new int[1];
			List<XmlContItem> xcis = this.queryIdxBySeparateStore(conn, wherestr,where_vals, orderbystr, pageidx, pagesize,c);
			
			List<DataXItem> rets = new ArrayList<DataXItem>(xcis.size());
			for(XmlContItem xci:xcis)
			{
				DataXItem dxi = xci.constructDataXItem() ;
				if(dxi==null)
					continue ;
				rets.add(dxi);
			}

			DataXItemList dxil = new DataXItemList(rets, pageidx, pagesize,
					c[0]);
			return dxil;
		}
		finally
		{
			if (conn != null)
			{
				db.freeConnection(conn);
			}
		}
	}

	// //////////////////////////
	// 数据库支持方法
	// //////////////////////////

	void updateReference(DataXClass refdxc) throws Exception
	{
		if (refId <= 0 && refdxc == null)
			return;

		if (refdxc!=null&&refId == refdxc.getId())
			return;

		String sqldelold = null;
		String sqlnew = null;

		Database db = dataxBase.getDatabase();

		if (refId > 0)
		{// 删除旧的引用
			JavaForeignKeyInfo oldfk = new JavaForeignKeyInfo(COL_REF_ID,
					dataxBase.getDataXClassById(refId).getTableName(), COL_XD_ID);
			sqldelold = db.getDBSql().constructDropForeignKeyTable(
					getJavaTableInfo(), oldfk).toString();
		}

		if (refdxc != null)
		{// 添加引用
			JavaForeignKeyInfo newfk = new JavaForeignKeyInfo(COL_REF_ID, refdxc
					.getTableName(), COL_XD_ID);
			sqlnew = db.getDBSql().constructForeignKeyTable(getJavaTableInfo(),
					newfk).toString();
		}

		db.runNoResultSqls(new String[] { sqldelold,
				sqlnew });

		if(refdxc==null)
			refId = -1 ;
		else
			refId = refdxc.getId();
	}

	private List<Long> loadXmlDataIdsByIndex(Connection conn, String idxname,
			String wherestr,Object[] where_vals, String orderbystr, int pageidx, int pagesize,
			int[] totalcount) throws Exception
	{
		DataXIndex idx = getDataXIndex(idxname);
		if (idx == null)
			throw new Exception("cannot find index with name=" + idxname);

		JavaTableInfo idxjti = idx.getJavaTableInfo();
		Database db = dataxBase.getDatabase();
		
		DbSql.SqlAndInputVals sql_input = null ;
		if(idx instanceof DataXIndexSVMC)
		{
			sql_input = db.getDBSql().getSelectSqlWithPage(idxjti, false,
				new String[] { COL_XD_ID }, wherestr,where_vals,null, orderbystr, pageidx,
				pagesize);
		}
		else
		{
			sql_input = db.getDBSql().getSelectSqlWithPage(idxjti, true,
					new String[] { COL_XD_ID }, wherestr,where_vals,null, orderbystr, pageidx,
					pagesize);
		}

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();

		List<Long> rets = new ArrayList<Long>();
		ResultSet rs = null;
		try
		{
			String countsql = "select count(*) from "
				+ idxjti.getTableName();
			if(wherestr!=null&&!wherestr.equals(""))
				countsql += (" where " + wherestr);
			
			stat = conn.prepareStatement(countsql);
			if(where_vals!=null&&where_vals.length>0)
			{
				for(int k = 0 ; k < where_vals.length ; k ++)
				{
					stat.setObject(k+1, where_vals[k]);
				}
			}
			
			rs = stat.executeQuery();
			if (rs.next())
			{
				int cc = rs.getInt(1);
				if (totalcount != null && totalcount.length > 0)
					totalcount[0] = cc;
			}
			stat.close();

			String tmpsql = sql_input.sqlSB.toString() ;
			stat = conn.prepareStatement(tmpsql);
			Object[] inputvs = sql_input.inputVals;
			if(inputvs!=null&&inputvs.length>0)
			{
				for(int k = 0 ; k < inputvs.length ; k ++)
				{
					stat.setObject(k+1, inputvs[k]);
				}
			}
			rs = stat.executeQuery();

			while (rs.next())
			{
				rets.add((Long) rs.getObject(1));
			}

			return rets;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	
	
	private List<XmlContItem> loadXmlDataBySeparateStore(Connection conn,
			String wherestr,Object[] where_vals, String orderbystr, int pageidx, int pagesize,
			int[] totalcount) throws Exception
	{
		
		JavaTableInfo idxjti = getJavaTableInfo();
		Database db = dataxBase.getDatabase();
		
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		int c = sepcols.size();
		String[] selectcols = new String[c+2];
		selectcols[0] = COL_XD_ID;
		selectcols[1] = COL_MAIN_CONT;
		for(int i = 0 ; i < c ; i ++)
		{
			selectcols[2+i] = sepcols.get(i).columnInfo.getColumnName();
		}
		
		
		DbSql.SqlAndInputVals sql_input = db.getDBSql().getSelectSqlWithPage(idxjti, false,
				selectcols, wherestr,where_vals,null, orderbystr, pageidx,
				pagesize);
		

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();

		List<Long> rets = new ArrayList<Long>();
		ResultSet rs = null;
		try
		{
			String countsql = "select count(*) from "
				+ idxjti.getTableName();
			if(wherestr!=null&&!wherestr.equals(""))
				countsql += (" where " + wherestr);
			
			stat = conn.prepareStatement(countsql);
			if(where_vals!=null&&where_vals.length>0)
			{
				for(int k = 0 ; k < where_vals.length ; k ++)
				{
					stat.setObject(k+1, where_vals[k]);
				}
			}
			
			rs = stat.executeQuery();
			if (rs.next())
			{
				int cc = rs.getInt(1);
				if (totalcount != null && totalcount.length > 0)
					totalcount[0] = cc;
			}
			stat.close();

			String tmpsql = sql_input.sqlSB.toString();//idsql.toString() ;
			System.out.println("loadXmlDataBySeparateStore sql->\n\t"+tmpsql);
			stat = conn.prepareStatement(tmpsql);
			Object[] inputvs = sql_input.inputVals;
			if(inputvs!=null&&inputvs.length>0)
			{
				for(int k = 0 ; k < inputvs.length ; k ++)
				{
					stat.setObject(k+1, inputvs[k]);
				}
			}
			
			rs = stat.executeQuery();

			ArrayList<XmlContItem> retals = new ArrayList<XmlContItem>();
			while (rs.next())
			{
				XmlContItem xci = new XmlContItem();
				xci.xdId = ((Number) rs.getObject(1)).longValue();
				xci.mainCont = (byte[]) rs.getObject(2);
				xci.sepStoreCols = sepcols;
				for(int i = 0 ; i < c ; i ++)
				{
					Object o = rs.getObject(3+i);
					if(o!=null)
						xci.col2Val.put(sepcols.get(i), o);
				}
				
				retals.add(xci) ;
			}

			return retals;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	
	private XmlDataList queryIdxByIndex(Connection conn, String idxname,
			String[] cols,
			String wherestr,Object[] where_vals,String groupbystr, String orderbystr, int pageidx, int pagesize
			) throws Exception
	{
		DataXIndex idx = getDataXIndex(idxname);
		if (idx == null)
			throw new Exception("cannot find index with name=" + idxname);

		JavaTableInfo idxjti = idx.getJavaTableInfo();
		Database db = dataxBase.getDatabase();
		
		DbSql.SqlAndInputVals sql_input = null ;
		if(idx instanceof DataXIndexSVMC)
		{
			sql_input = db.getDBSql().getSelectSqlWithPage(idxjti, false,
					cols, wherestr,where_vals,groupbystr, orderbystr, pageidx,
				pagesize);
		}
		else
		{
			sql_input = db.getDBSql().getSelectSqlWithPage(idxjti, false,
					cols, wherestr,where_vals,groupbystr, orderbystr, pageidx,
					pagesize);
		}

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();

		List<Long> rets = new ArrayList<Long>();
		ResultSet rs = null;
		try
		{
			int totalcount = -1 ;
			if(groupbystr==null||groupbystr.equals(""))
			{
				String countsql = "select count(*) from "
					+ idxjti.getTableName();
				if(wherestr!=null&&!wherestr.equals(""))
					countsql += (" where " + wherestr);
				
				stat = conn.prepareStatement(countsql);
				if(where_vals!=null&&where_vals.length>0)
				{
					for(int k = 0 ; k < where_vals.length ; k ++)
					{
						stat.setObject(k+1, where_vals[k]);
					}
				}
				
				rs = stat.executeQuery();
				if (rs.next())
				{
					totalcount = rs.getInt(1);
				}
				
				stat.close();
			}
			
			ArrayList<XmlData> dxis = new ArrayList<XmlData>();
			
			String tmpsql = sql_input.sqlSB.toString();//idsql.toString() ;
			System.out.println("listidx sql--->\n"+tmpsql);
			Object[] inputvs = sql_input.inputVals;
			stat = conn.prepareStatement(tmpsql);
			if(inputvs!=null&&inputvs.length>0)
			{
				for(int k = 0 ; k < inputvs.length ; k ++)
				{
					stat.setObject(k+1, inputvs[k]);
				}
			}
			rs = stat.executeQuery();

			int num = 0 ;
			while (rs.next())
			{
				int c = rs.getMetaData().getColumnCount() ;
				XmlData tmpxd = new XmlData();
				for(int i = 1 ; i <= c ; i ++)
				{
					String cn = rs.getMetaData().getColumnLabel(i);
					Object o = rs.getObject(i);
					if(o==null)
						continue ;
					
					tmpxd.setParamValue(cn, o);
				}
				
				dxis.add( tmpxd);
				
				num ++ ;
			}
			
			if(totalcount<0)
				totalcount = num ;

			return new XmlDataList(dxis,pageidx,num,totalcount);
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	
	/**
	 * 根据DataX的id和对应分离单值列和值更新一个DataX id对应的分离值
	 * 
	 * 该方法可能会被saveXmlData使用
	 * @param conn
	 * @param did
	 * @param refid
	 * @param singlepv_paths
	 * @param single_vals
	 * @throws Exception
	 */
	private boolean updateSeparateDataById(Connection conn,long did,
			String[] singlepv_paths,Object[] single_vals)
		throws Exception
	{
		String[] set_cols = new String[singlepv_paths.length];
//		int res = updateSeparateData(conn,
//				String[] set_cols,
//				String wherestr)
		return true ;
	}
	
	private int updateSeparateData(Connection conn,
			String[] set_cols,Object[] setcol_vals,
			String wherestr,Object[] where_vals) throws Exception
	{
		Database db = dataxBase.getDatabase();
		
		StringBuffer sqlsb = new StringBuffer() ;
		sqlsb.append("update ").append(getTableName()).append(" set ")
			.append(set_cols[0]);
		
		for(int i = 1 ; i < set_cols.length ; i ++)
		{
			sqlsb.append(',').append(set_cols[i]);
		}
		
		if(wherestr!=null&&!wherestr.equals(""))
			sqlsb.append(" where ").append(wherestr);
		
		String sql = sqlsb.toString();

		PreparedStatement stat = null;
		try
		{
			
			System.out.println("updateSeparateData sql--->\n"+sql);
			stat = conn.prepareStatement(sql);
			int wsp = 0 ;
			if(setcol_vals!=null&&setcol_vals.length>0)
			{
				wsp = setcol_vals.length;
				for(int k = 0 ; k < wsp ; k ++)
				{
					stat.setObject(k+1, setcol_vals[k]);
				}
			}
			
			if(where_vals!=null&&where_vals.length>0)
			{
				for(int k = 0 ; k < where_vals.length ; k ++)
				{
					stat.setObject(wsp+k+1, where_vals[k]);
				}
			}
			return stat.executeUpdate();
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	/**
	 * 提取仅仅是分离存储的数据。他是主XmlStruct的一个子集
	 * @param conn
	 * @param wherestr
	 * @param groupbystr
	 * @param orderbystr
	 * @param pageidx
	 * @param pagesize
	 * @param totalcount
	 * @return
	 * @throws Exception
	 */
	private List<XmlContItem> queryIdxBySeparateStore(Connection conn,
			String wherestr,Object[] where_vals,
			String orderbystr,
			int pageidx, int pagesize,int[] totalcount
			) throws Exception
	{
		JavaTableInfo idxjti = getJavaTableInfo();
		Database db = dataxBase.getDatabase();
		
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		int c = sepcols.size();
		String[] selectcols = new String[c+1];
		selectcols[0] = COL_XD_ID;
		//不获取主内容
		//selectcols[1] = COL_MAIN_CONT;
		for(int i = 0 ; i < c ; i ++)
		{
			selectcols[1+i] = sepcols.get(i).columnInfo.getColumnName();
		}
		
		
		//StringBuffer idsql = null ;
		DbSql.SqlAndInputVals sql_inputvs = db.getDBSql().getSelectSqlWithPage(idxjti, false,
				selectcols, wherestr,where_vals,null, orderbystr, pageidx,
				pagesize);
		

		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		
		ResultSet rs = null;
		try
		{
			String countsql = "select count(*) from "
				+ idxjti.getTableName();
			if(wherestr!=null&&!wherestr.equals(""))
				countsql += (" where " + wherestr);
			
			stat = conn.prepareStatement(countsql);
			if(where_vals!=null&&where_vals.length>0)
			{
				for(int k = 0 ; k < where_vals.length ; k ++)
				{
					stat.setObject(k+1, where_vals[k]);
				}
			}
			rs = stat.executeQuery();
			if (rs.next())
			{
				int cc = rs.getInt(1);
				if (totalcount != null && totalcount.length > 0)
					totalcount[0] = cc;
			}
			stat.close();

			String tmpsql = sql_inputvs.sqlSB.toString() ;
			System.out.println("queryIdxBySeparateStore sql->\n\t"+tmpsql);
			stat = conn.prepareStatement(tmpsql);
			Object[] inputvs = sql_inputvs.inputVals;
			if(inputvs!=null&&inputvs.length>0)
			{
				for(int k = 0 ; k < inputvs.length ; k ++)
				{
					stat.setObject(k+1, inputvs[k]);
				}
			}
			
			rs = stat.executeQuery();

			ArrayList<XmlContItem> retals = new ArrayList<XmlContItem>();
			while (rs.next())
			{
				XmlContItem xci = new XmlContItem();
				xci.xdId = ((Number) rs.getObject(1)).longValue();
				//xci.mainCont = (byte[]) rs.getObject(2);
				xci.sepStoreCols = sepcols;
				for(int i = 0 ; i < c ; i ++)
				{
					Object o = rs.getObject(2+i);
					if(o!=null)
						xci.col2Val.put(sepcols.get(i), o);
				}
				
				retals.add(xci) ;
			}

			return retals;
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}
	
	private long addNewFile(byte[] cont) throws Exception
	{
		IDataXIO dxio = dataxBase.getDataXIO() ;
		long fnid = dxio.getFileNewId(dataxBase.getId(), this.getId());
		dxio.saveFile(dataxBase.getId(), this.getId(), fnid, cont, this.isKeepFileHis);
		return fnid ;
	}
	
	private String saveFile(long fnid,byte[] cont) throws Exception
	{
		IDataXIO dxio = dataxBase.getDataXIO() ;
		return dxio.saveFile(dataxBase.getId(), this.getId(), fnid, cont, this.isKeepFileHis);
	}
	
	private void delFile(long fnid)
	{
		IDataXIO dxio = dataxBase.getDataXIO() ;
		dxio.delFile(dataxBase.getId(), this.getId(), fnid);
	}
	
	static class XmlContItem
	{
		long xdId = -1 ;
		//主存储内容
		byte[] mainCont = null ;
		//所有的分离存储列名称
		List<JavaColumnAStructItem> sepStoreCols = null ;
		//分离存储列名称和获得的值映射
		Hashtable<JavaColumnAStructItem,Object> col2Val = new Hashtable<JavaColumnAStructItem,Object>() ;
		
		public DataXItem constructDataXItem() throws Exception
		{
			XmlData xd = constructXmlData();
			if(xd==null)
				return null ;
			
			return new DataXItem(xdId,xd);
		}
		
		public XmlData constructXmlData() throws Exception
		{
//			if(mainCont==null&&col2Val.size()<=0)
//				return null ;
			
			XmlData xd = null ;
			if(mainCont==null)
				xd = new XmlData();
			else
				xd = XmlData.parseFromByteArray(mainCont, "UTF-8");
			
			if(sepStoreCols!=null)
			{
				for(JavaColumnAStructItem s:sepStoreCols)
				{
					Object v = col2Val.get(s);
					if(v==null)
					{//如果值为null，则认为删除
						xd.removeSingleParamValueByPath(s.getMemberPath().getPath());
					}
					else
					{
						xd.setSingleParamValueByPath(s.getMemberPath().getPath(), v);
					}
				}
			}
			
			return xd ;
		}
	}

	private XmlContItem loadXmlContentIO(Connection conn, long did) throws Exception
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		//String[] sepcols = getSeparateColumnNames();
		try
		{
			StringBuffer tmpsb = new StringBuffer();
			tmpsb.append("select ").append(COL_MAIN_CONT);
			if(sepcols!=null)
			{
				for(JavaColumnAStructItem sepcol:sepcols)
				{
					tmpsb.append(',').append(sepcol.columnInfo.getColumnName());
				}
			}
			tmpsb.append(" from ").append(getTableName())
				.append(" where ").append(COL_XD_ID).append("=?");
			String sql = tmpsb.toString();
			System.out.println("[loadXmlContentIO sql]==" + sql);
			stat = conn.prepareStatement(sql);
			stat.setObject(1, did);

			ResultSet rs = stat.executeQuery();

			if (!rs.next())
			{ // empty
				return null;
			}
			else
			{
				XmlContItem xci = new XmlContItem();
				xci.xdId = did ;
				xci.mainCont = (byte[]) rs.getObject(1);
				if(sepcols!=null)
				{
					xci.sepStoreCols = sepcols;
					int c = sepcols.size();
					for(int i = 0 ; i < c ; i ++)
					{
						Object o = rs.getObject(2+i);
						if(o!=null)
							xci.col2Val.put(sepcols.get(i), o);
					}
				}
				
				return xci;
			}
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}

		}
	}

	private boolean delXmlContIO(Connection conn, long did) throws Exception
	{
		PreparedStatement stat = null;
		// boolean oldcommit = conn.getAutoCommit();
		try
		{
			String sql = "delete from " + getTableName() + " where "+COL_XD_ID+"=?";
			// System.out.println("[readSingleRecord sql]==" + sql);
			stat = conn.prepareStatement(sql);
			stat.setObject(1, did);
			return stat.execute();
		}
		finally
		{
			if (stat != null)
			{
				stat.close();
			}
		}
	}

	private long insertXmlContentIO(Connection conn, long refid, XmlData xd)
			throws Exception
	{
		Database db = dataxBase.getDatabase();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		
		Hashtable<JavaColumnAStructItem,Object> jcsi2val = null;//new Hashtable<JavaColumnAStructItem,Object>();
		if(sepcols!=null&&sepcols.size()>0)
		{//从数据中提取出分离存储的内容
			xd = xd.copyMe();
			jcsi2val = new Hashtable<JavaColumnAStructItem,Object>();
			for(JavaColumnAStructItem jcsi:sepcols)
			{
				Object v = xd.removeSingleParamValueByPath(jcsi.getMemberPath().getPath());
				//Object v = xd.getSingleParamValueByPath(jcsi.getMemberPath().getPath());
				if(v!=null)
					jcsi2val.put(jcsi, v);
			}
		}

		byte[] buf = xd.toXmlString().getBytes("UTF-8");
		
		// boolean oldcommit = conn.getAutoCommit();
		try
		{
			StringBuffer[] sqls = db.getDBSql().getInsertSqlWithNewIdReturn(
					getJavaTableInfo());
			// System.out.println("[addRecord sql]=="+sqlstr.toString());
			if (sqls.length == 1)
			{
				String sqlstr = sqls[0].toString();
				System.out.println("[addRecord sql]==" + sqlstr);
				ps = conn.prepareStatement(sqlstr);

				long tt = System.currentTimeMillis();
				
				ps.setObject(1, refid);
				ps.setObject(2, tt);
				ps.setObject(3, tt);
				ps.setObject(4, buf);
				
				if(sepcols!=null&&sepcols.size()>0)
				{
					int c = sepcols.size();
					for(int i = 0 ; i < c ; i ++)
					{
						JavaColumnAStructItem jcsi = sepcols.get(i);
						Object tmpo = jcsi2val.get(jcsi) ;
						if(tmpo!=null)
							ps.setObject(5+i, tmpo);
						else
							ps.setNull(5+i, jcsi.columnInfo.getSqlValType());
					}
				}

				rs = ps.executeQuery();
				if (!rs.next())
				{ // empty
					throw new Exception("Cannot get new id!");
				}
				else
				{
					Number num = (Number) rs.getObject(1);
					return num.longValue();
				}
			}
			else if (sqls.length == 2)
			{
				String sqlstr = sqls[0].toString();
				System.out.println("[addRecord sql]==" + sqlstr);

				ps = conn.prepareStatement(sqls[0].toString());

				long tt = System.currentTimeMillis();
				ps.setObject(1, refid);
				ps.setObject(2, tt);
				ps.setObject(3, tt);
				ps.setObject(4, buf);

				if(sepcols!=null&&sepcols.size()>0)
				{
					int c = sepcols.size();
					for(int i = 0 ; i < c ; i ++)
					{
						//ps.setObject(5+i, jcsi2val.get(sepcols.get(i)));
						
						JavaColumnAStructItem jcsi = sepcols.get(i);
						Object tmpo = jcsi2val.get(jcsi) ;
						if(tmpo!=null)
							ps.setObject(5+i, tmpo);
						else
							ps.setNull(5+i, jcsi.columnInfo.getSqlValType());
					}
				}
				
				ps.execute();

				ps.close();

				sqlstr = sqls[1].toString();
				System.out.println("[addRecord sql]==" + sqlstr);
				ps = conn.prepareStatement(sqlstr);
				rs = ps.executeQuery();
				if (!rs.next())
				{ // empty
					throw new Exception("Cannot get new id!");
				}
				else
				{
					Number num = (Number) rs.getObject(1);
					return num.longValue();
				}
			}

			throw new Exception("invalid insert sql!");
		}
		finally
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
	}

	private void updateXmlContIO(Connection conn, long did, long refid,
			XmlData xd) throws Exception
	{
		Database db = dataxBase.getDatabase();
		PreparedStatement ps = null;
		// boolean oldcommit = conn.getAutoCommit();
		
		List<JavaColumnAStructItem> sepcols = xmlDataStructStoreSep() ;
		
		Hashtable<JavaColumnAStructItem,Object> jcsi2val = null;//new Hashtable<JavaColumnAStructItem,Object>();
		if(sepcols!=null&&sepcols.size()>0)
		{//从数据中提取出分离存储的内容
			xd = xd.copyMe();
			jcsi2val = new Hashtable<JavaColumnAStructItem,Object>();
			for(JavaColumnAStructItem jcsi:sepcols)
			{
				Object v = xd.removeSingleParamValueByPath(jcsi.getMemberPath().getPath());
				if(v!=null)
					jcsi2val.put(jcsi, v);
			}
		}

		byte[] buf = xd.toXmlString().getBytes("UTF-8");
		
		try
		{
			StringBuffer tmpsb = new StringBuffer();
			tmpsb.append("update ")
				.append(getTableName())
				.append(" set ").append(COL_REF_ID).append("=?,")
				.append(COL_MAIN_CONT).append("=?,")
				.append(COL_CHANGE_DATE).append("=?");
			
			if(sepcols!=null&&sepcols.size()>0)
			{
				for(JavaColumnAStructItem jcsi:sepcols)
				{
					tmpsb.append(',').append(jcsi.columnInfo.getColumnName())
						.append("=?");
				}
			}
			
			tmpsb.append(" where ").append(COL_XD_ID).append("=?");
			String sqlstr = tmpsb.toString();

			System.out.println("[update updateXmlContIO]=="+sqlstr.toString());
			ps = conn.prepareStatement(sqlstr);
			ps.setObject(1, refid);
			ps.setObject(2, buf);
			ps.setObject(3, System.currentTimeMillis());
			
			int c = 0 ;
			if(sepcols!=null&&sepcols.size()>0)
			{
				c = sepcols.size();
				
				for(int i = 0 ; i < c ; i ++)
				{
					//ps.setObject(4+i, jcsi2val.get(sepcols.get(i)));
					
					JavaColumnAStructItem jcsi = sepcols.get(i);
					Object tmpo = jcsi2val.get(jcsi) ;
					if(tmpo!=null)
						ps.setObject(4+i, tmpo);
					else
						ps.setNull(4+i, jcsi.columnInfo.getSqlValType());
				}
			}
			
			ps.setObject(4+c, did);

			ps.execute();
		}
		finally
		{
			if (ps != null)
			{
				ps.close();
			}
		}
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		xd.setParamValue("id", id);
		xd.setParamValue("name", name);
		xd.setParamValue("ref_id", refId);
		if(isFileClass)
			xd.setParamValue("is_file_class", isFileClass);
		if(isKeepFileHis)
			xd.setParamValue("is_keep_fis_his", isKeepFileHis);
		
		xd.setSubDataSingle("xmldata_struct", dataStruct.toXmlData());

		// if(dataXIndex!=null&&dataXIndex.size()>0)
		// {
		// List<XmlData> xds = xd.getOrCreateSubDataArray("index");
		// for(DataXIndex dxi:dataXIndex)
		// {
		// xds.add(Idx2XmlData(dxi)) ;
		// }
		// }

		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		id = xd.getParamValueInt32("id", -1);
		name = xd.getParamValueStr("name");
		refId = xd.getParamValueInt32("ref_id", -1);
		XmlData dsxd = xd.getSubDataSingle("xmldata_struct");
		dataStruct = new XmlDataStruct();
		dataStruct.fromXmlData(dsxd);

		isFileClass = xd.getParamValueBool("is_file_class", false);
		isKeepFileHis = xd.getParamValueBool("is_keep_fis_his", false);
		// List<XmlData> idxxds = xd.getSubDataArray("index");
		// if(idxxds!=null)
		// {
		// for(XmlData tmpxd:idxxds)
		// {
		// DataXIndex tmpxdi = XmlData2Idx(tmpxd) ;
		// dataXIndex.add(tmpxdi);
		// }
		// }
	}
	
}