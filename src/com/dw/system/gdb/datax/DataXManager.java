package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.AppConfig;
import com.dw.system.gdb.datax.query.*;
import com.dw.system.gdb.db_idx.Database;
import com.dw.system.logger.*;
import com.dw.system.xmldata.*;

/**
 * 数据层的统一管理——在一个进程空间内
 * 
 * 
 * 
 * 对应IO的实现
 * 
 * 经过仔细考虑，如果DataX的基本io都用数据库实现的话，可以为系统实现带来很大的灵活性 并且开发复杂程度降低
 * 
 * 如果对XmlData内容做存储压力很大的情况（海量用户访问情况下），可以考虑顶层使用文件方式做 临时的文件缓冲
 * 
 * @author Jason Zhu
 * 
 */
public class DataXManager implements IDataXContainer
{
	static ILogger log = LoggerManager.getLogger(DataXManager.class);
	static DataXManager dxMgr = null ;
	
	public static DataXManager getInstance()
	{
		if(dxMgr!=null)
			return dxMgr ;
		
		synchronized(log)
		{
			try
			{
				if(dxMgr!=null)
					return dxMgr ;
				
				dxMgr = new DataXManager() ;
				return dxMgr ;
			}
			catch(Exception e)
			{
				log.error(e);
				return null ;
			}
		}
	}
	
	private IDataXIO dxbIO = null;

	//private String defaultHsqlBase = null;

	private ArrayList<DataXBase> dxbs = new ArrayList<DataXBase>();

	private transient DataXQuery dataxQuery = null;

	private DataXDB dataxDB = null;

	private DataXManager()//, String default_hsql_base)
			throws Exception
	{
		String dataxbase = AppConfig.getDataDirBase()+"datax" ;
		dxbIO = new LocalFileDataXIO(dataxbase);
		
		
//		defaultHsqlBase = default_hsql_base;
//		if (!defaultHsqlBase.endsWith("/"))
//			defaultHsqlBase += "/";
//
//		File f = new File(defaultHsqlBase);
//		if (!f.exists())
//			f.mkdirs();

		dataxQuery = new DataXQuery(this);

		loadDataXDB();

		loadBase();
	}

	public DataXQuery getDataXQuery()
	{
		return dataxQuery;
	}

//	public String getDefaultHsqlBase()
//	{
//		return defaultHsqlBase;
//	}

	synchronized public DataXDB getDataXDB()
	{
		return dataxDB;
	}
	
	public Database getDatabase() throws Exception
	{
		return Database.getDefaultDatabase();
	}
	
	public IDataXIO getDataXIO()
	{
		return dxbIO ;
	}

	private void loadDataXDB() throws Exception
	{
		dataxDB = new DataXDB();

		try
		{
			byte[] cont = dxbIO.loadDataXDB();
			if (cont == null || cont.length <= 0)
				return;

			XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");
			dataxDB.fromXmlData(xd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 根据外界配置好的信息更新
	 * 
	 * @param outter_dataxdbinfo
	 * @throws Exception
	 */
	public void updateDataXDB(XmlData outter_dataxdbinfo) throws Exception
	{
		// save
		byte[] cont = outter_dataxdbinfo.toXmlString().getBytes("UTF-8");
		dxbIO.saveDataXDB(cont);
		// close old
		dataxDB.close();

		// load new
		dataxDB = new DataXDB();
		dataxDB.fromXmlData(outter_dataxdbinfo);
	}

	private void loadBase() throws Exception
	{
		int[] ids = dxbIO.loadBaseAllIds();
		if (ids == null)
			return;

		for (int id : ids)
		{
			byte[] cont = dxbIO.loadBaseById(id);
			if (cont == null)
				continue;

			XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");

			DataXBase dxb = new DataXBase();
			dxb.fromXmlData(xd);

			dxb.init(this);

			loadClass(dxb);

			dxbs.add(dxb);
		}
	}

	private void loadClass(DataXBase dxb) throws Exception
	{
		int[] ids = dxbIO.loadClassAllIds(dxb.getId());
		if (ids == null)
			return;

		for (int id : ids)
		{// 先装载所有的类
			byte[] cont = dxbIO.loadClassById(dxb.getId(), id);
			XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");

			DataXClass dxc = new DataXClass();
			dxc.fromXmlData(xd);
			dxc.dataxBase = dxb;

			dxb.dataxClass.add(dxc);
		}

		for (DataXClass dxc : dxb.dataxClass)
		{// 再对类进行初始化，这样可以保证能够初始化类相关性内容
			dxc.init();

			loadIndex(dxc);
		}
	}

	private void loadIndex(DataXClass dxc) throws Exception
	{
		DataXBase dxb = dxc.getDataXBase();
		int baseid = dxb.getId();
		int classid = dxc.getId();
		int[] ids = dxbIO.loadIndexAllIds(baseid, classid);
		if (ids == null)
			return;

		for (int id : ids)
		{
			byte[] cont = dxbIO.loadIndexById(baseid, classid, id);
			XmlData xd = XmlData.parseFromByteArray(cont, "UTF-8");

			DataXIndex dxi = DataXIndex.transFromXmlData(xd);

			dxi.init(dxb, dxc);
			dxc.dataXIndex.add(dxi);
		}
	}

	// //////////////////////////////////////////////////
	// 运行时提供的XmlData的数据存储和查找
	// //////////////////////////////////////////////////

	public DataXClass getDataXClass(int baseid, int classid)
	{
		DataXBase dxb = getDataXBaseById(baseid);
		if (dxb == null)
			return null;

		return dxb.getDataXClassById(classid);
	}

	public DataXClass getDataXClass(String basen, String classn)
	{
		DataXBase dxb = this.getDataXBaseByName(basen);
		if (dxb == null)
			return null;

		return dxb.getDataXClassByName(classn);
	}

	/**
	 * 获得或自动创建DataXBase和DataXClass
	 * 该方法为其他自动使用DataX的模块提供直接的数据访问支持。如果对应的数据已经创建，并通过管理器进行了设置 则也不会影响原有的设置信息。
	 * 
	 * 如：业务模块支持的系统，可以通过该方法获得对应的DataXClass以满足自己使用的需要
	 * 
	 * @param base_name
	 * @param class_name
	 * @return
	 * @throws Exception
	 */
	public DataXClass getOrCreateDataXClass(String base_name, String class_name)
			throws Exception
	{
		return getOrCreateDataXClass(base_name, class_name, null, null);
	}

	public DataXClass getOrCreateDataXClass(String base_name,
			String class_name, XmlDataStruct xds, List<DataXIndex> idx)
			throws Exception
	{
		DataXBase dxb = getDataXBaseByName(base_name);
		DataXClass dxc = null;
		if (dxb != null)
		{
			dxc = dxb.getDataXClassByName(class_name);
			if (dxc != null)
				return dxc;

		}

		synchronized (this)
		{
			// 避免多个线程之间冲突
			dxb = getDataXBaseByName(base_name);
			if (dxb == null)
			{
				dxb = addNewDataXBase(base_name, false);
			}

			dxc = dxb.getDataXClassByName(class_name);
			if (dxc != null)
				return dxc;

			if (xds == null)
				xds = new XmlDataStruct();

			dxc = addNewDataXClass(dxb.getId(), class_name, xds);

			if (idx != null)
			{
				for (DataXIndex tmpdxi : idx)
				{
					newDataXIndex(dxb, dxc, tmpdxi);
				}
			}

			return dxc;
		}
	}

	public XmlData getXmlData(int baseid, int classid, long xdid)
			throws Exception
	{
		DataXClass dxc = getDataXClass(baseid, classid);
		if (dxc == null)
			throw new IllegalArgumentException(
					"cannot find DataXClass with id=" + classid);

		return dxc.getXmlDataById(xdid);
	}

	public void saveXmlData(int baseid, int classid, long xdid, long refid,
			XmlData xd) throws Exception
	{
		DataXClass dxc = getDataXClass(baseid, classid);
		if (dxc == null)
			throw new IllegalArgumentException(
					"cannot find DataXClass with id=" + classid);

		dxc.saveXmlData(xdid, refid, xd);
	}

	public long addXmlData(int baseid, int classid, long refid, XmlData xd)
			throws Exception
	{
		DataXClass dxc = getDataXClass(baseid, classid);
		if (dxc == null)
			throw new IllegalArgumentException(
					"cannot find DataXClass with id=" + classid);

		return dxc.addNewXmlData(refid, xd);
	}

	public XmlData QUERY_Cmd(String querystr, XmlData inputxd) throws Exception
	{
		return dataxQuery.runCmd(querystr, inputxd);
	}

	/**
	 * [/age]>20 and [/email] like 'aa%'
	 * 
	 * @param query_str
	 * @return
	 */
	public DataXItemList queryXmlData(int baseid, int classid, String idxname,
			String query_str, String[] related_paths, int pageidx, int pagesize)
			throws Exception
	{
		DataXClass dxc = getDataXClass(baseid, classid);
		if (dxc == null)
			throw new IllegalArgumentException(
					"cannot find DataXClass with id=" + classid);

		if (related_paths != null)
		{
			StringBuffer tmpsb = new StringBuffer(query_str);

			for (String rp : related_paths)
			{
				XmlDataPath xdp = new XmlDataPath(rp);
				String coln = xdp.toColumnName();
				query_str = query_str.replaceAll("[" + rp + "]", coln);
			}
		}
		//
		return dxc.findByIdx(idxname, query_str, null, null, pageidx, pagesize);
	}

	public DataXItemList queryXmlData(int baseid, int classid,
			String query_str, String[] related_paths, int pageidx, int pagesize)
			throws Exception
	{
		DataXClass dxc = getDataXClass(baseid, classid);
		if (dxc == null)
			throw new IllegalArgumentException(
					"cannot find DataXClass with id=" + classid);

		if (related_paths != null)
		{
			StringBuffer tmpsb = new StringBuffer(query_str);

			for (String rp : related_paths)
			{
				XmlDataPath xdp = new XmlDataPath(rp);
				String coln = xdp.toColumnName();
				query_str = query_str.replaceAll("[" + rp + "]", coln);
			}
		}
		//
		return dxc
				.findBySeparateStore(query_str, null, null, pageidx, pagesize);
	}

	// public List<XmlData> queryXml

	// //////////////////////////////////////////////////
	// 对DataX进行维护的方法
	// //////////////////////////////////////////////////

	public DataXBase[] getAllDataXBase()
	{
		DataXBase[] rets = new DataXBase[dxbs.size()];
		dxbs.toArray(rets);
		return rets;
	}

	public DataXBase getDataXBaseById(int id)
	{
		for (DataXBase b : dxbs)
		{
			if (b.getId() == id)
				return b;
		}
		return null;
	}

	public DataXBase getDataXBaseByName(String name)
	{
		for (DataXBase b : dxbs)
		{
			if (b.getName().equals(name))
				return b;
		}
		return null;
	}

	public DataXClass[] getAllDataXClassesInBase(String basen)
	{
		DataXBase dxb = getDataXBaseByName(basen);
		if (dxb == null)
			return null;

		return dxb.getAllDataXClasses();
	}

	// public DataXClass getDataXClass(String basen,String classn)
	// {
	// DataXBase dxb = getDataXBaseByName(basen);
	// if(dxb==null)
	// return null ;
	//		
	// return dxb.getDataXClassByName(classn);
	// }

	/**
	 * 注册系统集的DataX库，其注册信息中的id无效
	 * 
	 * 系统判断是否已经存在对应的DataX库信息，如果已经存在，则不做任何动作返回
	 * 
	 * 如果被注册的信息不存在（以DataXBase的name为基准），则系统自动为相关的DataXBase，DataXClass，DataXIndex生成新的id
	 * 
	 * 该方法为其他业务模块提供自动定义特定的DataX结构。其他业务模块通过构造DataXBase信息提供
	 * 
	 * @param sysdxb
	 * @return
	 */
	synchronized public DataXBase registerSystemDataXBase(DataXBase sysdxb)
			throws Exception
	{
		String n = sysdxb.getName();
		DataXBase dxb = getDataXBaseByName(n);
		if (dxb != null)
			return dxb;// 已经存在,不做任何动作

		// 不存在，则通过自动建立，产生对应的DataX结构
		// dxb = addNewDataXBase(sysdxb.getName(), sysdxb.getDBInfo(),true) ;
		dxb = addNewDataXBase(sysdxb.getName(), true);
		DataXClass[] dxcs = sysdxb.getAllDataXClasses();
		// 按照引用依赖进行排序，以满足建立Class的顺序,依赖的类越多，值越大，排序在后
		Arrays.sort(dxcs, new Comparator<DataXClass>()
		{
			public int compare(DataXClass arg0, DataXClass arg1)
			{
				HashSet<DataXClass> hs0 = arg0.getRefClassSet();
				HashSet<DataXClass> hs1 = arg1.getRefClassSet();
				if (hs0 == null || hs0.size() <= 0)
				{
					if (hs1 == null || hs1.size() <= 0)
						return 0;

					return -1;
				}
				else
				{
					if (hs1 == null || hs1.size() <= 0)
						return 1;

					if (hs0.contains(arg1))
						return 1;

					if (hs1.contains(arg0))
						return -1;

					return 0;
				}
			}
		});

		for (DataXClass dxc : dxcs)
		{
			DataXClass ndxc = addNewDataXClass(dxb.getId(), dxc.getName(), dxc
					.getDataStruct());
			DataXClass refdxc = sysdxb.getDataXClassById(dxc.getRefClassId());
			updateDataXClass(dxb.getId(), ndxc.getId(), refdxc.getName(), dxc
					.getName(), dxc.getDataStruct(), dxc.dataXIndex);
		}
		return dxb;
	}

	public DataXBase addNewDataXBase(String name) throws Exception
	{
		return addNewDataXBase(name, false);
	}

	/**
	 * 增加新的DataXBase
	 * 
	 * @param name
	 * @param dbinfo
	 * @throws Exception
	 */
	public DataXBase addNewDataXBase(String name, boolean bsys)
			throws Exception
	{
		DataXBase dxb = getDataXBaseByName(name);
		if (dxb != null)
			throw new IllegalArgumentException("DataXBase with name=" + name
					+ " is already existed");

		int newid = dxbIO.getNewId();

		dxb = new DataXBase(newid, name, bsys);
		dxb.init(this);// 先初始化

		byte[] cont = dxb.toXmlData().toXmlString().getBytes("UTF-8");
		dxbIO.saveBaseById(newid, cont);

		dxbs.add(dxb);

		return dxb;
	}

	public DataXBase updateDataXBase(int baseid, String name) throws Exception
	{
		DataXBase dxb = getDataXBaseById(baseid);
		if (dxb == null)
			throw new IllegalArgumentException("DataXBase with id=" + baseid
					+ " cannot be found!");

		dxb = new DataXBase(baseid, name, false);
		// 初始化查看效果
		dxb.init(this);

		// 装载其他内容
		loadClass(dxb);

		// save it
		byte[] cont = dxb.toXmlData().toXmlString().getBytes("UTF-8");
		dxbIO.saveBaseById(baseid, cont);

		// 替换内存中的
		int c = dxbs.size();
		for (int i = 0; i < c; i++)
		{
			if (dxbs.get(i).getId() == baseid)
			{
				// 关闭旧的对象——释放数据库连接等
				// DataXBase olddxb = dxbs.get(i);
				// olddxb.close() ;

				dxbs.set(i, dxb);
				break;
			}
		}

		return dxb;
	}

	public void removeDataXBase(int dxbid) throws Exception
	{
		DataXBase dxb = getDataXBaseById(dxbid);
		if (dxb == null)
			throw new Exception("cannot find DataXBase with id=" + dxbid);

		DataXClass[] dxcs = dxb.getAllDataXClasses();
		if (dxcs != null && dxcs.length > 0)
			throw new Exception(
					"DataX Base cannot be removed because has classes");

		int c = dxbs.size();
		for (int i = 0; i < c; i++)
		{
			if (dxbs.get(i).getId() == dxbid)
			{
				dxbs.remove(i);
				break;
			}
		}

		dxbIO.saveBaseById(dxbid, null);
	}

	/**
	 * 增加新的DataXClass
	 * 
	 * @param xb_id
	 * @param classn
	 */
	public DataXClass addNewDataXClass(int xb_id, String classn,
			XmlDataStruct xds) throws Exception
	{
		DataXBase dxb = getDataXBaseById(xb_id);
		if (dxb == null)
			throw new Exception("cannot find DataXBase with id=" + xb_id);

		DataXClass dxc = dxb.getDataXClassByName(classn);
		if (dxc != null)
			throw new Exception("DataXBase with id=" + xb_id
					+ " already has class name=" + classn);

		int newid = dxbIO.getNewId();
		dxc = new DataXClass(newid, -1, classn, xds);
		dxc.dataxBase = dxb;

		byte[] cont = dxc.toXmlData().toXmlString().getBytes("UTF-8");
		dxbIO.saveClassById(xb_id, newid, cont);

		dxc.init();

		dxb.dataxClass.add(dxc);

		return dxc;
	}

	/**
	 * 删除DataXClass，同时删除对应的数据表
	 * 
	 * @param xb_id
	 * @param dxc_id
	 */
	public void removeDataXClass(int xb_id, int dxc_id) throws Exception
	{
		DataXClass dxc = this.getDataXClass(xb_id, dxc_id);
		if (dxc == null)
			throw new Exception("cannot find DataXClass with id=" + dxc_id);

		DataXBase dxb = dxc.getDataXBase();
		// 先删除索引表
		for (DataXIndex dxi : dxc.getDataXIndexes())
		{
			delDataXIndex(dxb, dxc, dxi.getId());
		}

		// 删除主表
		dxc.delMainTable();

		// 删除DataXClass信息
		dxbIO.saveClassById(xb_id, dxc_id, null);

		// 清理内存对象
		dxb.dataxClass.remove(dxc);
	}

	public DataXClass updateDataXClass(int xb_id, int classid, String ref_cn,
			String classn, XmlDataStruct xds, List idx) throws Exception
	{
		DataXBase dxb = getDataXBaseById(xb_id);
		if (dxb == null)
			throw new Exception("cannot find DataXBase with id=" + xb_id);

		int refid = -1;
		if (ref_cn != null && !"".equals(ref_cn))
		{
			DataXClass refdxc = dxb.getDataXClassByName(ref_cn);
			if (refdxc == null)
				throw new Exception("Cannot find ref datax class with name="
						+ ref_cn);

			refid = refdxc.getId();
		}

		return updateDataXClass(xb_id, classid, refid, classn, xds, idx);
	}

	/**
	 * 更新DataXClass，其中同步更新结构和索引，该方法满足管理端通过本地定义好的 结构信息和索引信息进行一次处理
	 * 
	 * @param xb_id
	 * @param classid
	 * @param classn
	 * @param xds
	 * @param idx
	 *            存放XmlDataIndex的列表，里面id<0表示是新增的索引， 如果现有的索引id在该列表中不存在，则认为是删除的索引
	 * @return
	 * @throws Exception
	 */
	public DataXClass updateDataXClass(int xb_id, int classid, int ref_id,
			String classn, XmlDataStruct xds, List idx) throws Exception
	{
		if (classn == null || classn.equals(""))
			throw new Exception("class name cannot be null or empty!");

		DataXBase dxb = getDataXBaseById(xb_id);
		if (dxb == null)
			throw new Exception("cannot find DataXBase with id=" + xb_id);

		DataXClass dxc = dxb.getDataXClassById(classid);
		if (dxc == null)
			throw new Exception("cannot find DataXClass with id=" + classid);

		// 判断引用是否改变
		if (dxc.refId != ref_id && ref_id > 0)
		{//
			DataXClass refdxc = dxb.getDataXClassById(ref_id);
			if (refdxc == null)
				throw new Exception("cannot get ref DataXClass with id="
						+ ref_id);

			if (!dxb.isCanRefClass(dxc, refdxc))
				throw new Exception("class [" + dxc.getName()
						+ "] cannot reference class [" + refdxc.getName() + "]");

		}

		// 更新主表结构
		dxc.name = classn;
		if (!dxc.dataStruct.equals(xds))
		{// 结构被改变
			XmlDataStruct oldxds = dxc.dataStruct;

			if (!dxc.checkExistedTable())
			{
				dxc.dataStruct = xds;
				dxc.init();
			}
			else
			{
				// 判断就结构中的分离存储是否和新的分离存储不一样，如果不一样，则做分离存储改变
				dxc.changeXmlDataStruct(xds);
				dxc.dataStruct = xds; // 如果
			}
		}

		// 建立一个临时的对象进行IO保存
		DataXClass tmpdxc = new DataXClass(classid, ref_id, classn, xds);
		byte[] cont = tmpdxc.toXmlData().toXmlString().getBytes("UTF-8");
		dxbIO.saveClassById(xb_id, classid, cont);

		// 判断引用是否改变
		if (dxc.refId != ref_id)
		{//
			DataXClass refdxc = dxb.getDataXClassById(ref_id);
			dxc.updateReference(refdxc);
		}

		// 更新现存的索引,从idx中查找新增索引（id=-1），修改索引和删除索引
		List<DataXIndex> newidx = new ArrayList<DataXIndex>();
		List<DataXIndex> updateidx = new ArrayList<DataXIndex>();
		HashSet<Integer> delidx_id = new HashSet<Integer>();

		for (DataXIndex tmpdxi : dxc.getDataXIndexes())
		{
			delidx_id.add(tmpdxi.getId());
		}

		if (idx != null)
		{
			for (Object o : idx)
			{
				DataXIndex dix = (DataXIndex) o;
				int id = dix.getId();
				if (id <= 0)
				{
					newidx.add(dix);
					continue;
				}

				delidx_id.remove(id);

				DataXIndex olddix = dxc.getDataXIndexById(id);
				if (olddix != null && !olddix.equals(dix))
				{
					updateidx.add(dix);
				}
			}
		}

		for (DataXIndex tmpdxi : updateidx)
		{
			updateDataXIndex(dxb, dxc, tmpdxi);
		}

		for (Integer tmpid : delidx_id)
		{
			delDataXIndex(dxb, dxc, tmpid);
		}

		for (DataXIndex tmpdxi : newidx)
		{
			newDataXIndex(dxb, dxc, tmpdxi);
		}

		// dxc.init(dxb);

		return dxc;
	}

	private void updateDataXIndex(DataXBase dxb, DataXClass dxc, DataXIndex dxi)
			throws Exception
	{
		DataXIndex oldidx = dxc.getDataXIndexById(dxi.getId());
		if (oldidx == null)
			return;

		if (oldidx.equals(dxi))
			return;

		byte[] cont = DataXIndex.transToXmlData(dxi).toXmlString().getBytes(
				"UTF-8");
		dxbIO.saveIndexById(dxb.getId(), dxc.getId(), dxi.getId(), cont);

		dxi.init(dxb, dxc);

		dxc.setDataXIndex(dxi);

		dxc.recreateIndex(dxi.getId());
	}

	private void delDataXIndex(DataXBase dxb, DataXClass dxc, int id)
			throws Exception
	{
		DataXIndex oldidx = dxc.getDataXIndexById(id);
		if (oldidx == null)
			return;

		dxc.delIndex(id);

		dxbIO.saveIndexById(dxb.getId(), dxc.getId(), id, null);
		dxc.unsetDataXIndex(id);
	}

	private void newDataXIndex(DataXBase dxb, DataXClass dxc, DataXIndex dxi)
			throws Exception
	{
		int newid = dxbIO.getNewId();
		dxi.id = newid;

		String n = dxi.getName();
		DataXIndex olddxi = dxc.getDataXIndex(n);
		if (olddxi != null)
		{
			throw new Exception("DataX Index with name =" + n
					+ " is already existed!");
		}

		byte[] cont = DataXIndex.transToXmlData(dxi).toXmlString().getBytes(
				"UTF-8");

		dxbIO.saveIndexById(dxb.getId(), dxc.getId(), dxi.getId(), cont);

		dxi.init(dxb, dxc);

		dxc.setDataXIndex(dxi);

		dxc.recreateIndex(dxi.getId());
	}

	/**
	 * 更加现有的索引id，把关系数据库中的索引表进行重新生成
	 * 
	 * 
	 * @param xb_id
	 * @param classid
	 * @param idxid
	 * @throws Exception
	 */
	public void recreateIndex(int xb_id, int classid, int idxid)
			throws Exception
	{
		DataXBase dxb = getDataXBaseById(xb_id);
		if (dxb == null)
			throw new Exception("cannot find DataXBase with id=" + xb_id);

		DataXClass dxc = dxb.getDataXClassById(classid);
		if (dxc == null)
			throw new Exception("DataXClass with id=" + classid
					+ " is not existed!");

		dxc.recreateIndex(idxid);
	}

	// public DataXIndex addNewDataXIndexSVMC(int xb_id, int classid, String
	// name,
	// List<String> idxsps) throws Exception
	// {
	// DataXBase dxb = getDataXBaseById(xb_id);
	// if (dxb == null)
	// throw new Exception("cannot find DataXBase with id=" + xb_id);
	//
	// DataXClass dxc = dxb.getDataXClassById(classid);
	// if (dxc == null)
	// throw new Exception("DataXClass with id=" + classid
	// + " is not existed!");
	//
	// DataXIndex dxi = dxc.getDataXIndex(name);
	// if (dxi != null)
	// throw new Exception("DataXIndex with name=" + name
	// + " is already existed");
	//
	// int newid = dxbIO.getNewId();
	//
	// dxi = new DataXIndexSVMC(newid, name, idxsps);
	//
	// byte[] cont = DataXIndex.transToXmlData(dxi).toXmlString().getBytes(
	// "UTF-8");
	// dxbIO.saveIndexById(xb_id, classid, newid, cont);
	//
	// dxi.init(dxb, dxc);
	//
	// dxc.dataXIndex.add(dxi);
	// return dxi;
	// }
	//
	// public void updateDataXIndexSVMC(int xb_id, int classid, int idxid,
	// String name, List<String> idxsps) throws Exception
	// {
	// DataXBase dxb = getDataXBaseById(xb_id);
	// if (dxb == null)
	// throw new Exception("cannot find DataXBase with id=" + xb_id);
	//
	// DataXClass dxc = dxb.getDataXClassById(classid);
	// if (dxc == null)
	// throw new Exception("DataXClass with id=" + classid
	// + " is not existed!");
	//
	// DataXIndex dxi = dxc.getDataXIndexById(idxid);
	// if (dxi == null)
	// throw new Exception("DataXIndex with id=" + idxid
	// + " is not existed");
	//
	// dxi = new DataXIndexSVMC(idxid, name, idxsps);
	//
	// byte[] cont = DataXIndex.transToXmlData(dxi).toXmlString().getBytes(
	// "UTF-8");
	// dxbIO.saveIndexById(xb_id, classid, idxid, cont);
	//
	// dxi.init(dxb, dxc);
	//
	// // update mem
	// int c = dxc.dataXIndex.size();
	// for (int i = 0; i < c; i++)
	// {
	// if (dxc.dataXIndex.get(i).getId() == idxid)
	// {
	// dxc.dataXIndex.set(i, dxi);
	// return;
	// }
	// }
	//		
	// //update index
	// dxc.recreateIndex(idxid);
	// }
}
