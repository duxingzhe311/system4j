package com.dw.grid;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.cache.Cacher;
import com.dw.system.gdb.*;

/**
 * 管理所有的终端
 * 
 * 每个终端都有一个id，并且都有一个密钥
 * 
 * @author Jason Zhu
 */
public class GridClientManager
{
	private static Object locker = new Object() ;
	
	private static GridClientManager instance = null ;
	
	public static GridClientManager getInstance()
	{
		if(instance!=null)
			return instance ;
		
		synchronized(locker)
		{
			if(instance!=null)
				return instance ;
			
			instance = new GridClientManager() ;
			return instance ;
		}
	}
	
	List<GridClientCat> gridClientCats = null;
	
	GridClientActiveTable activeTable = new GridClientActiveTable() ;
	
	Cacher id2item = Cacher.getCacher(GridClientManager.class.getCanonicalName());
	//HashMap<Long,GridClientItem> id2item = null ;
	
	private GridClientManager()
	{
		id2item.setMaxBufferLength(1000) ;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<GridClientCat> getAllGridClientCat()
		throws Exception
	{
		List<GridClientCat> rets = gridClientCats ;
		if(rets!=null)
			return rets ;
		
		synchronized(this)
		{
			if(gridClientCats!=null)
				return gridClientCats ;
			
			gridClientCats = (List<GridClientCat>)GDB.getInstance().listXORMAsObjList(GridClientCat.class, null, null, 0, -1) ;
			return gridClientCats;
		}
	}
	
	
	public GridClientCat getGridClientCatById(String catid)
		throws Exception
	{
		for(GridClientCat gcc:getAllGridClientCat())
		{
			if(gcc.getCatId().equals(catid))
				return gcc ;
		}
		
		return null ;
	}
	
	/**
	 * 根据名称或的
	 * @param catname
	 * @return
	 * @throws Exception
	 */
	public GridClientCat getGridClientCatByName(String catname)
		throws Exception
	{
		for(GridClientCat gcc:getAllGridClientCat())
		{
			if(gcc.getCatName().equals(catname))
				return gcc ;
		}
		
		return null ;
	}
	
	public void addOrUpdateGridClientCat(String catid,GridClientCat gcc)
		throws Exception
	{
		if(Convert.isNullOrEmpty(catid))
			GDB.getInstance().addXORMObjWithNewId(gcc) ;
		else
			GDB.getInstance().updateXORMObjToDB(catid,gcc) ;
		
		clearCache() ;
	}
	
	/**
	 * 获得的活动表
	 * @return
	 */
	public GridClientActiveTable getActiveTable()
	{
		return activeTable ;
	}
	
	/**
	 * 
	 * @param gci
	 * @return
	 * @throws Exception
	 */
	public GridClientItem addNewClient(GridClientItem gci)
		throws Exception
	{
		GDB.getInstance().addXORMObjWithNewId(gci) ;
		clearCache();
		return gci ;
	}
	
	
	public void updateClient(long cid,GridClientItem gci) throws Exception
	{
		GDB.getInstance().updateXORMObjToDBWithHasColNameValues(cid, GridClientItem.class, 
				new String[]{"Title","PhoneNum","Email"},
				new Object[]{gci.getTitle(),gci.getPhoneNum(),gci.getEmail()});
		clearCache();
	}
	
	public void updateClientPhoneNum(long cid,String phonen)
		throws Exception
	{
		GDB.getInstance().updateXORMObjToDBWithHasColNameValues(cid, GridClientItem.class, 
				new String[]{"PhoneNum","PhoneNumUpDT"},
				new Object[]{phonen,new Date()});
		
		clearCache();
	}
	
	public void clearCache()
	{
		//id2item = null ;
		id2item.clear() ;
		gridClientCats = null ;
	}
	
//	public HashMap<Long,GridClientItem> getAllClientMap()
//		throws Exception
//	{
//		HashMap<Long,GridClientItem> ret = id2item ;
//		if(ret!=null)
//			return ret ;
//		
//		List<GridClientItem> gcis = GDB.getInstance().listXORMAsObjList(GridClientItem.class, null, null, 0, -1, null) ;
//		HashMap<Long,GridClientItem> ms = new HashMap<Long,GridClientItem>() ;
//		if(gcis!=null)
//		{
//			for(GridClientItem gci:gcis)
//			{
//				ms.put(gci.getClientId(), gci) ;
//			}
//		}
//		
//		id2item = ms ;
//		return id2item ;
//	}
	
	public GridClientItem getClientById(long id)
		throws Exception
	{
		GridClientItem gci = (GridClientItem)id2item.get(id) ;
		if(gci!=null)
			return gci ;
		
		//return getAllClientMap().get(id) ;
		gci = (GridClientItem)GDB.getInstance().getXORMObjByPkId(GridClientItem.class, id) ;
		if(gci==null)
			return null ;
		
		id2item.cache(id, gci) ;
		return gci ;
	}
	
	/**
	 * 
	 * @param username
	 * @return
	 * @throws GdbException
	 * @throws Exception
	 */
	public GridClientItem getClientByUserName(String username) throws GdbException, Exception
	{
		if(Convert.isNullOrEmpty(username))
			return null ;
		
		return (GridClientItem)GDB.getInstance().getXORMObjByUniqueColValue(GridClientItem.class,"UserName",username,true) ;
	}
	
	public static String calOneSecKey()
	{
		StringBuilder sb = new StringBuilder() ;
		Random r = new Random(System.currentTimeMillis()) ;
		for(int i = 0 ; i < 8 ; i ++)
		{
			//字符从48-57 65-90 97-122 total=10+26*2=62//40 - 126
			int tmpv = Math.abs(r.nextInt()) % 62 ;
			char c ;
			if(tmpv>=0&&tmpv<10)
				c = (char)(tmpv +48) ;
			else if(tmpv>=10&&tmpv<36)
				c = (char)(tmpv-10+65) ;
			else
				c = (char)(tmpv-36+97) ;
			
			sb.append(c) ;
		}
		
		return sb.toString() ;
	}
	/**
	 * 为一个终端生产新的密钥
	 * @param id
	 * @return
	 */
	public String createClientNewSecKey(long id)
		throws Exception
	{
		//生产8为随机密钥
		String nkey = calOneSecKey() ;
		boolean b = GDB.getInstance().updateXORMObjToDBWithHasColNameValues(id, 
				GridClientItem.class, 
				new String[]{"SecKey"}, 
				new Object[]{nkey}) ;
		
		clearCache() ;
		return nkey ;
	}
}
