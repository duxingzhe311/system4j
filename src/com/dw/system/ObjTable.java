package com.dw.system;

import java.util.*;
/**
 * �ںܶ�Ӧ����,�ڴ���ᱣ��ܶ����
 * 
 * ���һ���ݶ���Ķ�����Թ������ͬ����֮��������
 * ��:�������ݽṹ�Ľڵ���Ϣ. �����ʱ���ǰ���ÿ����ͬ�����ڵ���б���
 * ��ʹ�ù�����,����Ҫ����һ���ڵ��id��ȡ�����ӽڵ�Ķ����
 * 
 * ObjTable����֧��������������ݽṹ.
 * 
 * ÿ���������Լ���Ψһid.ͬʱ,���ж����Ҫ��Ϊ������������Ϣ
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
	 * ���id������ı�
	 */
	private Hashtable<ID_TYPE,ObjWrapper<T,ID_TYPE>> id2objw = new Hashtable<ID_TYPE,ObjWrapper<T,ID_TYPE>>();
	
	/**
	 * ����������Ƶ������ڵĴ洢����
	 * 
	 * �����ڵĴ洢���ݽṹ��,����ֵ-�������װ�ļ���
	 */
	private Hashtable<String,Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>>> idxn2map = new Hashtable<String,Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>>>();
	
	/**
	 * ����һ�������
	 * @param idx_attrs ��Ϊ������������Ϣ
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
	 * ����������һ������
	 * @param objv ������
	 * @param idval �����Ψһidֵ
	 * @param idxn2idxv �����ڸñ��е��������Զ�Ӧֵ
	 */
	public void setObject(T objv,ID_TYPE idval,Hashtable<String,Object> idxn2idxv)
	{
		//��ȡ�ɵ�����
		unsetObjectById(idval);
		if(objv==null)
			return ;
		
		ObjWrapper<T,ID_TYPE> objw = new ObjWrapper<T,ID_TYPE>(objv,idval,idxn2idxv);
		id2objw.put(idval, objw);
		
		for(Map.Entry<String, Object> meidxnv:idxn2idxv.entrySet())
		{
			String idxname = meidxnv.getKey();
			Object idxval = meidxnv.getValue();
			
			//�����������ƻ������map
			Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
			if(idxv2objwrap_set==null)
				throw new IllegalArgumentException("cannot find idx name="+idxname);
			
			
			//��������ֵ��ú͸�ֵ��ص�ObjWrapper����
			HashSet<ObjWrapper<T,ID_TYPE>> objwset = idxv2objwrap_set.get(idxval);
			if(objwset==null)
			{
				objwset = new HashSet<ObjWrapper<T,ID_TYPE>>() ;
				idxv2objwrap_set.put(idxval, objwset);
			}
			
			//ɾ�������еĶ����װ
			objwset.add(objw);
		}
	}
	
	/**
	 * ���ݶ����id,�ӱ���ɾ��֮
	 * @param idval
	 * @return �������null,��ʾ�����ڸö���,���!=null,���ʾ�Ƕ���ı���
	 */
	public T unsetObjectById(ID_TYPE idval)
	{
//		��ȡ�ɵ�����
		ObjWrapper<T,ID_TYPE> oldobjw = id2objw.get(idval);
		if(oldobjw==null)
			return null;
		
		id2objw.remove(idval);
		Hashtable<String,Object> idxn2idxv = oldobjw.idxName2idxVal;
		if(idxn2idxv!=null)
		{//ɾ�������е�����
			for(Map.Entry<String, Object> meidxnv:idxn2idxv.entrySet())
			{
				String idxname = meidxnv.getKey();
				Object idxval = meidxnv.getValue();
				
				//�����������ƻ������map
				Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
				if(idxv2objwrap_set==null)
					continue ;//���������������
				
				//��������ֵ��ú͸�ֵ��ص�ObjWrapper����
				HashSet<ObjWrapper<T,ID_TYPE>> objwset = idxv2objwrap_set.get(idxval);
				if(objwset==null)
					continue ;
				
				//ɾ�������еĶ����װ
				objwset.remove(oldobjw);
				if(objwset.isEmpty())//�������ֵ��Ӧ�ļ����Ѿ�Ϊ��,��Ѹü���Ҳɾ��
					idxv2objwrap_set.remove(idxval);
			}
		}
		
		return oldobjw.objVal;
	}
	
	/**
	 * ����idֵ��ȡ��Ӧ�Ķ���
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
	 * �����������ƺ�����ֵ,��ȡ��صĶ����б�
	 * @param idx_attrn
	 * @return
	 */
	public ArrayList<T> getObjsByIdx(String idxname,Object idxval)
	{
		//�����������ƻ������map
		Hashtable<Object,HashSet<ObjWrapper<T,ID_TYPE>>> idxv2objwrap_set = idxn2map.get(idxname);
		if(idxv2objwrap_set==null)
			throw new IllegalArgumentException("cannot find idx name="+idxname);
		
		
		//��������ֵ��ú͸�ֵ��ص�ObjWrapper����
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
