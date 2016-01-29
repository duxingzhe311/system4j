package com.dw.system;

import java.util.*;
/**
 * 在很多应用中,内存里会保存很多对象
 * 
 * 并且会根据对象的多个属性构造出不同对象之间的相关性
 * 如:树型数据结构的节点信息. 保存的时候是按照每个不同的树节点进行保存
 * 但使用过程中,就需要根据一个节点的id获取所有子节点的对象等
 * 
 * ObjTable就是支持这种情况的数据结构.
 * 
 * 每个对象都有自己的唯一id.同时,又有多个需要作为索引的属性信息
 * @author Jason
 */
public class ObjTable<T,ID_TYPE>
{
	private static class ObjWrapper<T,ID_TYPE>
	{
		T objVal = null ;
		
		ID_TYPE objId = null ;
		Hashtable<String,Object> idxName2idxVal = null ;
		
		public ObjWrapper(T objval,ID_TYPE objid,Hashtable<String,Object> idxn2idxv)
		{
			objVal = objval ;
			objId = objid ;
			idxName2idxVal = idxn2idxv;
		}
	}
	/**
	 * 存放id到对象的表
	 */
	private Hashtable<ID_TYPE,ObjWrapper<T,ID_TYPE>> id2objw = new Hashtable<ID_TYPE,ObjWrapper<T,ID_TYPE>>();
	
	/**
	 * 存放索引名称到索引内的存储数据
	 * 
	 * 索引内的存储数据结构是,索引值-到对象包装的集合
	 */
	private Hashtable<String,Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>>> idxn2map = new Hashtable<String,Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>>>();
	
	/**
	 * 构造一个对象表
	 * @param idx_attrs 作为索引的属性信息
	 */
	public ObjTable(String[] idx_names)
	{
		for(String idxn:idx_names)
		{
			Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = new Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>>();
			idxn2map.put(idxn, idxv2objwrap_set);
		}
	}
	
	/**
	 * 往表中设置一个对象
	 * @param objv 对象本身
	 * @param idval 对象的唯一id值
	 * @param idxn2idxv 对象在该表中的索引属性对应值
	 */
	public void setObject(T objv,ID_TYPE idval,Hashtable<String,Object> idxn2idxv)
	{
		//获取旧的数据
		unsetObjectById(idval);
		if(objv==null)
			return ;
		
		ObjWrapper<T,ID_TYPE> objw = new ObjWrapper<T,ID_TYPE>(objv,idval,idxn2idxv);
		id2objw.put(idval, objw);
		
		for(Map.Entry<String, Object> meidxnv:idxn2idxv.entrySet())
		{
			String idxname = meidxnv.getKey();
			Object idxval = meidxnv.getValue();
			
			//根据索引名称获得索引map
			Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
			if(idxv2objwrap_set==null)
				throw new IllegalArgumentException("cannot find idx name="+idxname);
			
			
			//根据索引值获得和该值相关的ObjWrapper集合
			HashSet<ObjWrapper<T,ID_TYPE>> objwset = idxv2objwrap_set.get(idxval);
			if(objwset==null)
			{
				objwset = new HashSet<ObjWrapper<T,ID_TYPE>>() ;
				idxv2objwrap_set.put(idxval, objwset);
			}
			
			//删除集合中的对象包装
			objwset.add(objw);
		}
	}
	
	/**
	 * 根据对象的id,从表中删除之
	 * @param idval
	 * @return 如果返回null,表示不存在该对象,如果!=null,则表示是对象的本身
	 */
	public T unsetObjectById(ID_TYPE idval)
	{
//		获取旧的数据
		ObjWrapper<T,ID_TYPE> oldobjw = id2objw.get(idval);
		if(oldobjw==null)
			return null;
		
		id2objw.remove(idval);
		Hashtable<String,Object> idxn2idxv = oldobjw.idxName2idxVal;
		if(idxn2idxv!=null)
		{//删除索引中的数据
			for(Map.Entry<String, Object> meidxnv:idxn2idxv.entrySet())
			{
				String idxname = meidxnv.getKey();
				Object idxval = meidxnv.getValue();
				
				//根据索引名称获得索引map
				Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
				if(idxv2objwrap_set==null)
					continue ;//不存在这个索引项
				
				//根据索引值获得和该值相关的ObjWrapper集合
				HashSet<ObjWrapper<T,ID_TYPE>> objwset = idxv2objwrap_set.get(idxval);
				if(objwset==null)
					continue ;
				
				//删除集合中的对象包装
				objwset.remove(oldobjw);
				if(objwset.isEmpty())//如果索引值对应的集合已经为空,则把该集合也删除
					idxv2objwrap_set.remove(idxval);
			}
		}
		
		return oldobjw.objVal;
	}
	
	/**
	 * 根据id值获取对应的对象
	 * @param idval
	 * @return
	 */
	public T getObjById(ID_TYPE idval)
	{
		ObjWrapper<T,ID_TYPE> oldobjw = id2objw.get(idval);
		if(oldobjw==null)
			return null;
		
		return oldobjw.objVal;
	}
	
	/**
	 * 根据索引名称和索引值,获取相关的对象列表
	 * @param idx_attrn
	 * @return
	 */
	public ArrayList<T> getObjsByIdx(String idxname,Object idxval)
	{
		//根据索引名称获得索引map
		Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
		if(idxv2objwrap_set==null)
			throw new IllegalArgumentException("cannot find idx name="+idxname);
		
		
		//根据索引值获得和该值相关的ObjWrapper集合
		HashSet<ObjWrapper<T,ID_TYPE>> objwset = idxv2objwrap_set.get(idxval);
		if(objwset==null)
		{
			return new ArrayList<T>(0);
		}
		
		ArrayList<T> rets = new ArrayList<T>(objwset.size());
		
		for(Iterator<ObjWrapper<T,ID_TYPE>> ir = objwset.iterator();ir.hasNext();)
		{
			rets.add(ir.next().objVal);
		}
		
		return rets;
	}
	
	public ArrayList<T> getAllObjs()
	{
		ArrayList<T> rets = new ArrayList<T>(id2objw.size());
		
		for(ObjWrapper<T,ID_TYPE> ow:id2objw.values())
		{
			rets.add(ow.objVal);
		}
		
		return rets;
	}
}
