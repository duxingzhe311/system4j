package com.dw.system.gdb;

import java.sql.*;
import java.util.*;

import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.conf.Func;
import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.Module;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.connpool.IConnPool;

/**
 * 在一些情况下，需要对数据库的多个更新语句做事务中访问
 * 可以先定义本对象，并设置多个更新或删除语句
 * 
 * 所有的语句都应该保证在一个数据库连接池中。
 * 
 * @author Jason Zhu
 */
public class GDBMulti
{
	static interface IDBAccess
	{
		void accessWithConn(GDB g,IConnPool cp, Connection conn) throws Exception ;
	}
	
	static class ItemAccessDB implements IDBAccess
	{
		String uniqueKey = null ;
		Hashtable parms = null ;
		
		public ItemAccessDB(String uniquekey, Hashtable parms)
		{
			this.uniqueKey = uniquekey ;
			this.parms = parms ;
		}
		
		public void accessWithConn(GDB g,IConnPool cp, Connection conn)
			throws Exception
		{
			g.accessDBInConn(cp.getDBType(), conn, uniqueKey, parms, 0, -1,null,null) ;
		}
	}
	
	static class ItemAddXORMObj  implements IDBAccess
	{
		String newid = null ;
		Object xormObj = null ;
		
		public ItemAddXORMObj(Object o)
		{
			xormObj = o ;
		}
		
		/**
		 * 支持外接的输入的新id
		 * 为了支持string作为主键的情况下，多表数据插入事务操作
		 * 增加本构造方法用来支持自动id的生成,为有外接关系的表
		 * 直接提供id支持，以支持事务
		 * @param newid
		 * @param o
		 */
		//public static ItemAddXORMObj create
		public ItemAddXORMObj(String newid,Object o)
		{
			this.newid = newid ;
			xormObj = o ;
		}
		
		public void accessWithConn(GDB g,IConnPool cp, Connection conn)
			throws Exception
		{
			g.addXORMObjWithNewIdInConn(newid,conn, xormObj);
		}
	}
	
	static class ItemUpdateXORMObjToDB implements IDBAccess
	{
		Object pkId = null ;
		Object xormObj = null ;
		
		public ItemUpdateXORMObjToDB(Object pkid,Object o)
		{
			this.pkId = pkid ;
			xormObj = o ;
		}
		
		public void accessWithConn(GDB g,IConnPool cp, Connection conn)
			throws Exception
		{
			g.updateXORMObjToDBInConn(conn, pkId, xormObj);
		}
	}
	
	static class ItemDeleteXORMObjFromDB implements IDBAccess
	{
		Object pkId = null ;
		Class xormC = null ;
		
		public ItemDeleteXORMObjFromDB(Object pkid,Class xorm_c)
		{
			this.pkId = pkid ;
			xormC = xorm_c ;
		}
		
		public void accessWithConn(GDB g,IConnPool cp, Connection conn)
			throws Exception
		{
			g.deleteXORMObjFromDBInConn(conn, pkId, xormC);
		}
	}
	
	static class DeleteXORMObjByCondWithNoFile implements IDBAccess
	{
		String whereStr = null ;
		Class xormC = null ;
		
		public DeleteXORMObjByCondWithNoFile(Class xorm_c,String wherestr)
		{
			this.whereStr = wherestr ;
			xormC = xorm_c ;
		}
		
		public void accessWithConn(GDB g,IConnPool cp, Connection conn)
			throws Exception
		{
			g.deleteXORMObjByCondWithNoFile(conn,xormC,whereStr);
			//g.deleteXORMObjFromDBInConn(conn, pkId, xormC);
		}
	}
	
	
	
	
	ArrayList<IDBAccess> multiPs = new ArrayList<IDBAccess>() ;
	
	IConnPool connPool = null ;
	
	public GDBMulti()
	{
		
	}
	
	private void checkDBByUniqueKey(String uk)
	{
		Func f = null ;

		try
		{
			f = GDB.getInstance().key2Func.get(uk);
		}
		catch(Exception ee)
		{
			throw new RuntimeException(ee.getMessage()) ;
		}
		
		if (f == null)
			throw new RuntimeException("cannot find Func with key [" + uk
					+ "],make sure it is to be loaded!");

		//Module m = f.getBelongTo();
		//Gdb g = m.getBelongTo();

		IConnPool cp = ConnPoolMgr.getConnPool(f.getRealUsingDBName());
		if(cp==null)
			throw new RuntimeException("no connpool found!") ;
		
		if(connPool==null)
			connPool = cp ;
		
		if(cp!=connPool)
			throw new RuntimeException("not same conn pool!") ;
	}
	
	
	private XORM checkDBByXORMC(Class xormc)
	{
		XORM xorm_conf = Gdb.getXORMByGlobal( xormc);
		if (xorm_conf == null)
			throw new IllegalArgumentException(
					"cannot get XORM config info with class="
							+  xormc.getCanonicalName());

		Gdb g = xorm_conf.getBelongToGdb();

		IConnPool cp = ConnPoolMgr.getConnPool(g);
		if(cp==null)
			throw new RuntimeException("no connpool found!") ;
		
		if(connPool==null)
			connPool = cp ;
		
		if(cp!=connPool)
			throw new RuntimeException("not same conn pool!") ;
		
		return xorm_conf;
	}
	/**
	 * 增加普通访问数据库的参数
	 * @param uniquekey
	 * @param parms
	 * @param idx
	 * @param count
	 */
	public void addAccessDB(String uniquekey, Hashtable parms)
	{
		checkDBByUniqueKey(uniquekey);
		
		multiPs.add(new ItemAccessDB(uniquekey,parms)) ;
	}
	
	/**
	 * 增加增加XORM对象到数据库的参数
	 * @param o
	 */
	public void addAddXORMObj(Object o)
	{
		checkDBByXORMC(o.getClass());
		
		multiPs.add(new ItemAddXORMObj(o)) ;
	}
	
	public String addAddXORMObjWithStrIdReturn(Object o)
		throws Exception
	{
		XORM c = checkDBByXORMC(o.getClass());
		if(!c.isAutoStringValuePk())
			throw new Exception("XORM Object must has xorm class definition of auto string value pk");
		
		String nid = GDB.newAutoStringKeyVal() ;
		multiPs.add(new ItemAddXORMObj(nid,o)) ;
		return nid ;
	}
	
	/**
	 * 增加更新XORM对象到数据库的参数
	 * @param pkid
	 * @param xorm_obj
	 */
	public void addUpdateXORMObjToDB(long pkid,Object xorm_obj)
	{
		checkDBByXORMC(xorm_obj.getClass());
		
		multiPs.add(new ItemUpdateXORMObjToDB(pkid,xorm_obj)) ;
	}
	
	public void addUpdateXORMObjToDB(String pkid,Object xorm_obj)
	{
		checkDBByXORMC(xorm_obj.getClass());
		
		multiPs.add(new ItemUpdateXORMObjToDB(pkid,xorm_obj)) ;
	}
	
	/**
	 * 增加从数据中删除XORM的参数
	 * @param pkid
	 * @param xorm_c
	 */
	public void addDeleteXORMObjFromDB(long pkid,Class xorm_c)
	{
		checkDBByXORMC(xorm_c);
		
		multiPs.add(new ItemDeleteXORMObjFromDB(pkid,xorm_c)) ;
	}
	
	public void addDeleteXORMObjFromDB(String pkid,Class xorm_c)
	{
		checkDBByXORMC(xorm_c);
		
		multiPs.add(new ItemDeleteXORMObjFromDB(pkid,xorm_c)) ;
	}
	/**
	 * 增加
	 * @param xorm_c
	 * @param wherestr
	 */
	public void addDeleteXORMObjByCondWithNoFile(Class xorm_c,String wherestr)
	{
		checkDBByXORMC(xorm_c);
		
		multiPs.add(new DeleteXORMObjByCondWithNoFile(xorm_c,wherestr)) ;
	}
}
