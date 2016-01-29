package com.dw.grid;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.dw.grid.*;
import com.dw.system.*;
import com.dw.system.gdb.GDB;
import com.dw.system.logger.ILogger;
import com.dw.system.logger.LoggerManager;
import com.dw.system.xmldata.XmlHelper;

public class GridDefManager
{
	private static ILogger log = LoggerManager.getLogger(GridDefManager.class);

	static Object locker = new Object();

	static GridDefManager instance = null;

	public static GridDefManager getInstance()
	{
		if (instance != null)
			return instance;

		synchronized (locker)
		{
			if (instance != null)
				return instance;

			instance = new GridDefManager();
			return instance;
		}
	}
	
	GridDef defDEF = null ;
	HashMap<String,GridDef> name2def = new HashMap<String,GridDef>();
	
	private GridDefManager()
	{
		Element ele = AppConfig.getConfElement("app_grid") ;
		if(ele==null)
			return ;
		
		Element[] es = XmlHelper.getSubChildElement(ele,"def") ;
		if(es==null||es.length<=0)
			return ;
		
		for(int i = 0 ; i < es.length ; i ++)
		{
			try
			{
				String n = es[i].getAttribute("name") ;
				if(Convert.isNullOrEmpty(n))
					continue ;
				
				String t = es[i].getAttribute("title") ;
				if(Convert.isNullOrEmpty(t))
					t = n ;
			
				double base_lat = Double.parseDouble(es[i].getAttribute("base_lat")) ;
			 
				double base_lng = Double.parseDouble(es[i].getAttribute("base_lng")) ;
				
				double area_lat = Double.parseDouble(es[i].getAttribute("area_lat")) ;
				 
				double area_lng = Double.parseDouble(es[i].getAttribute("area_lng")) ;
				
				GpsPos basegp = new GpsPos(base_lng,base_lat) ;
				GpsPos areagp = new GpsPos(area_lng,area_lat) ;
				
//				GpsPos basegp = new GpsPos(true,121, 29, 42,
//			    		true,31, 14, 30) ;
//				GpsPos areagp = new GpsPos(121.52149200439453,31.348945815579977d) ;
				GridDef d = new GridDef(n,t,basegp,areagp);
				
				if(i==0)
					defDEF = d ;
				
				name2def.put(n,d);
			}
			catch(Exception ee)
			{
				if(log.isWarnEnabled())
					log.warn("",ee);
			}
		}
		
	}
	
	
	public GridDef getDefaultDEF()
	{
//		GpsPos basegp = new GpsPos(true,121, 29, 42,
//	    		true,31, 14, 30) ;
//		GpsPos areagp = new GpsPos(121.49419784545898d,31.242388849582557d) ;
//		defDEF = new GridDefinition(basegp,areagp);
		
		//GridDef d = new GridDef(defDEF.getName(),defDEF.getTitle(),
		//		defDEF.getBaseGpsPos(),defDEF.getAreaBaseGpsPos());
		//defDEF = d;
		return defDEF ;
	}
	
	public GridDef getGridDef(String defn)
	{
		return name2def.get(defn);
	}
	
	
	///////////////
	
	
	HashMap<String,HashSet<String>> defname2CodeSet = new HashMap<String,HashSet<String>>();
	
	
	public HashSet<String> getDefinedCodeSet(String defname)
	throws Exception
	{
		return getDefinedCodeSet(defname,false) ;
	}
	/**
	 * 根据定义的名词获得对应的定义网格集合
	 * @param defname
	 * @param brow 是否是原始定义的网格，true表示只输出原始定义的网格，里面有大小网格之分
	 * @return
	 */
	public HashSet<String> getDefinedCodeSet(String defname,boolean brow)
		throws Exception
	{
		GridDef gd = getGridDef(defname);
		if(gd==null)
			return null ;
		
		if(brow)
		{//原始定义不需要缓冲中读取
			return loadDefFromDb(defname,brow) ;
		}
		
		HashSet<String> ret = defname2CodeSet.get(defname);
		if(ret!=null&&!brow)
			return ret ;
		
		synchronized(defname2CodeSet)
		{
			ret = defname2CodeSet.get(defname);
			if(ret!=null)
				return ret ;
			
			
			//load from db
			HashSet<String> ss = loadDefFromDb(defname,brow) ;
			
			defname2CodeSet.put(defname, ss) ;
			return ss ;
		}
	}
	
	
	private HashSet<String> loadDefFromDb(String defname,boolean brow)
		throws Exception
	{
		List<GridDefOneItem> gditems = GDB.getInstance().listXORMAsObjList(GridDefOneItem.class, "DefName='"+defname+"'", null, 0, -1);
		HashSet<String> ss = new HashSet<String>() ;
		
		if(gditems!=null)
		{
			for(GridDefOneItem gdi:gditems)
			{
				if(brow)
				{
					ss.add(gdi.getGridCode()) ;
				}
				else
				{
					ArrayList<String> tmps = gdi.getSmallGridCode() ;
					ss.addAll(tmps) ;
				}
			}
		}
		
		return ss ;
	}
	
	private void clearDefinedCache(String defname)
	{
		synchronized(defname2CodeSet)
		{
			defname2CodeSet.remove(defname);
		}
	}
	
	
	public void setDefinedCode(String defname,String code)
		throws Exception
	{
		GridDef gd = getGridDef(defname);
		if(gd==null)
			return ;
		
		List<GridDefOneItem> gditems = GDB.getInstance().listXORMAsObjList(GridDefOneItem.class, "DefName='"+defname+"' and GridCode='"+code+"'", null, 0, -1);
		if(gditems!=null&&gditems.size()>0)
			return ;
		
		GridDefOneItem gdi = new GridDefOneItem(defname,code) ;
		GDB.getInstance().addXORMObjWithNewId(gdi) ;
		
		clearDefinedCache(defname);
	}
	
	public void unsetDefinedCode(String defname,String code)
		throws Exception
	{
		GridDef gd = getGridDef(defname);
		if(gd==null)
			return ;
		
		List<GridDefOneItem> gditems = GDB.getInstance().listXORMAsObjList(GridDefOneItem.class, "DefName='"+defname+"' and GridCode='"+code+"'", null, 0, -1);
		if(gditems==null||gditems.size()<=0)
			return ;
		
		GDB.getInstance().deleteXORMObjFromDB(gditems.get(0).getAutoId(), GridDefOneItem.class);
		
		clearDefinedCache(defname);
	}
}
