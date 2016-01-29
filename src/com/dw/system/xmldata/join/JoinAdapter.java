package com.dw.system.xmldata.join;

import java.util.*;

import com.dw.system.xmldata.*;

/**
 * XmlData��������
 * 
 * ������������һ��֧�ֵ����ݽṹ
 * ������JoinBoardΪ����,��������θ�����������ݽṹ����JoinBoard������
 * 
 * ������Ҫ��JoinBoard�򽻵�,����ͨ������JoinAdapter
 * 
 * @author Jason Zhu
 */
public class JoinAdapter implements IXmlDataable
{
	
	/**
	 * ���������ӿ�֮��ת����ӳ���ϵ
	 * ����Ҫ����������ӿڵ����ݽṹ�е�·��-����ӿڵ����ݽṹ�е�·��
	 * @author Jason Zhu
	 */
	public static class MapItem implements IXmlDataable
	{
		/**
		 * ת����������XmlDataStruct·��
		 */
		String adpXdsPath = null ;
		/**
		 * JoinInterface��XmlDataStruct·��
		 */
		String interfaceXdsPath = null ;
		
		/**
		 * �Ƿ�Ҫ����ǿ����������ת��
		 */
		boolean bForceTrans = false;
		
		/**
		 * �������ӿ�·����Ӧ����string����,
		 * ���Ӧ̫�����ַ����Ƿ�Ҫ�����Զ��ض�.
		 */
		boolean bAutoTruncate = false;
		
		MapItem()
		{}
		
		public MapItem(MapItem mi)
		{
			adpXdsPath = mi.adpXdsPath ;
			interfaceXdsPath = mi.interfaceXdsPath ;
			bForceTrans = mi.bForceTrans;
			bAutoTruncate = mi.bAutoTruncate;
		}
		
		public MapItem(String adp_xds_path,String inter_xds_path)
		{
			this(adp_xds_path,inter_xds_path,false,false);
		}
		
		public MapItem(String adp_xds_path,String inter_xds_path,
				boolean force_trans,boolean auto_truncate)
		{
			if(adp_xds_path==null||adp_xds_path.equals(""))
				throw new IllegalArgumentException("adapter XmlDataStruct path cannot be null or empty!");
			if(inter_xds_path==null||inter_xds_path.equals(""))
				throw new IllegalArgumentException("interface XmlDataStruct path cannot be null or empty!");
			
			adpXdsPath = adp_xds_path ;
			interfaceXdsPath = inter_xds_path ;
			bForceTrans = force_trans ;
			bAutoTruncate = auto_truncate; 
		}
		
		public String getJoinAdapterXdsPath()
		{
			return adpXdsPath;
		}
		
		public String getJoinInterfaceXdsPath()
		{
			return interfaceXdsPath ;
		}
		
		public boolean isForceTransfer()
		{
			return bForceTrans ;
		}
		
		public boolean isAutoTruncate()
		{
			return bAutoTruncate;
		}
		
		public String getUniqueName()
		{
			return adpXdsPath+"."+interfaceXdsPath;
		}
		
		public boolean equals(Object o)
		{
			if(!(o instanceof MapItem))
				return false;
			
			return getUniqueName().equals(((MapItem)o).getUniqueName()) ;
		}
		
		public int hashCode()
		{
			return getUniqueName().hashCode();
		}
		
		public XmlData toXmlData()
		{
			XmlData xd = new XmlData();
			xd.setParamValue("adp_xds_path",adpXdsPath);
			xd.setParamValue("inter_xds_path",interfaceXdsPath);
			xd.setParamValue("force_trans", bForceTrans);
			xd.setParamValue("auto_truncate", bAutoTruncate); 
			return xd;
		}

		public void fromXmlData(XmlData xd)
		{
			adpXdsPath = xd.getParamValueStr("adp_xds_path");
			interfaceXdsPath = xd.getParamValueStr("inter_xds_path");
			bForceTrans = xd.getParamValueBool("force_trans", false);
			bAutoTruncate = xd.getParamValueBool("auto_truncate", false); 		
		}
	}
	
	/**
	 * ��Ӧ����һ��JoinUnit�е�һ��JoinInterface
	 * ���ڲ�������ת��ӳ���ϵ
	 * @author Jason Zhu
	 */
	public static class JoinItem implements IXmlDataable
	{
		String unitName = null ;
		String interfaceName = null ;
		private ArrayList<MapItem> pinMapItems = new ArrayList<MapItem>();
		
		JoinItem()
		{}
		
		public JoinItem(JoinItem ji)
		{
			unitName = ji.unitName;
			interfaceName = ji.interfaceName;
			for(MapItem mi:ji.pinMapItems)
			{
				pinMapItems.add(new MapItem(mi));
			}
		}
		
		public JoinItem(String unit_n,String interfacen)
		{
			unitName = unit_n;
			interfaceName = interfacen ;
		}
		
		public String getUniqueName()
		{
			return unitName+"."+interfaceName;
		}
		
		public boolean equals(Object o)
		{
			if(!(o instanceof JoinItem))
				return false;
			
			return getUniqueName().equals(((JoinItem)o).getUniqueName()) ;
		}
		
		public int hashCode()
		{
			return getUniqueName().hashCode();
		}
//		public JoinAdapter getBelongToAdapter()
//		{
//			return JoinAdapter.this;
//		}
		
		public String getJoinUnitName()
		{
			return unitName ;
		}
		
		public String getInterfaceName()
		{
			return interfaceName ;
		}
		
		public ArrayList<MapItem> getTransMapItem()
		{
			return pinMapItems ;
		}
		
		public void setTransMapItem(List<MapItem> mis)
		{
			pinMapItems.clear();
			if(mis!=null)
				pinMapItems.addAll(mis);
		}
		
		public XmlData toXmlData()
		{
			XmlData xd = new XmlData();
			xd.setParamValue("unit_name", unitName);
			xd.setParamValue("interface_name", interfaceName);
			if(pinMapItems!=null)
			{
				List<XmlData> xds = xd.getOrCreateSubDataArray("map_items");
				for(MapItem mi:pinMapItems)
				{
					xds.add(mi.toXmlData());
				}
			}
			return xd;
		}

		public void fromXmlData(XmlData xd)
		{
			unitName = xd.getParamValueStr("unit_name");
			interfaceName = xd.getParamValueStr("interface_name");
			List<XmlData> mixds = xd.getSubDataArray("map_items");
			if(mixds!=null)
			{
				for(XmlData tmpxd:mixds)
				{
					MapItem mi = new MapItem();
					mi.fromXmlData(tmpxd);
					pinMapItems.add(mi);
				}
			}
		}
	}

	/**
	 * ��ת������ʹ��֮ǰ,��Ҫ�ж��Ƿ���Ч
	 * ����JoinBoard�����ṩ�жϷ���,����JoinAdapter�ĸ���һ����ͨ��
	 * �ײ�XmlData�������,���Ա�֤ÿ�θ��º�,�����Ի���¶���
	 * 
	 * �ñ���ֻ��JoinBoardʹ��
	 */
	transient int bValid = 0;
	/**
	 * ��������
	 */
	JoinType joinType = JoinType.in ;
	
	/**
	 * ����ת����֧�ֵĽṹ
	 * ���������Ϊnull,���ʾ��ת������������
	 */
	XmlDataStruct adapterXDS = null ;

	ArrayList<JoinItem> joinItems = new ArrayList<JoinItem>() ;
	
	public JoinAdapter()
	{
		
	}
	
	public JoinAdapter(JoinType jt)
	{
		joinType = jt ;
		//setAdapterXmlDataStruct(adpxds);
	}
	
	public JoinType getJoinType()
	{
		return joinType ;
	}
	
	public void setJoinType(JoinType jt)
	{
		joinType = jt ;
	}
	
	public XmlDataStruct getAdapterXmlDataStruct()
	{
		return adapterXDS ;
	}
	
	/**
	 * �жϸ�ת�����Ƿ���Ҫ����
	 * �÷������ڵ�ԭ����,�ܶ�ڵ��ڲ�����From Toת��������
	 * ��ʵ����,�����ǲ���Ҫ���е�,ͨ����ת�����Լ����������,����ʹ��һ��
	 * @return
	 */
	public boolean isWillWork()
	{
		if(joinItems.size()<=0)//û������������Ϣ,Ҳ����Ϊ��������
			return true ;
		
		return adapterXDS!=null&&!adapterXDS.isEmptyStruct();
	}
	/**
	 * ����ת�������������ݽṹ
	 * @param xds
	 */
	public void setAdapterXmlDataStruct(XmlDataStruct xds)
	{
		adapterXDS = xds ;
//		if(adapterXDS==null)
//			adapterXDS = new XmlDataStruct();
	}
	
	public ArrayList<JoinItem> getJoinItems()
	{
		return joinItems ;
	}
	
	public ArrayList<JoinItem> copyJoinItems()
	{
		ArrayList<JoinItem> rets = new ArrayList<JoinItem>(joinItems.size()) ;
		for(JoinItem ji:joinItems)
		{
			rets.add(new JoinItem(ji));
		}
		return rets;
	}
	
	public JoinItem getJoinItem(String unit_n,String interfacen)
	{
		for(JoinItem ji:joinItems)
		{
			if(!ji.getJoinUnitName().equals(unit_n))
				continue ;
			
			if(!ji.getInterfaceName().equals(interfacen))
				continue ;
			
			return ji ;
		}
		
		return null ;
	}
	/**
	 * ����������Ϣ
	 * @param out_pin_name
	 * @param in_pin_name
	 * @param mis
	 */
	public void setJoinItem(String unit_n,String interfacen)
	{
		JoinItem ji = getJoinItem(unit_n, interfacen);
		if(ji!=null)
			return ;
		
		ji = new JoinItem(unit_n,interfacen);
		joinItems.add(ji);
	}
	
	public void setJoinItems(List<JoinItem> jis)
	{
		joinItems.clear();
		
		if(jis!=null)
			joinItems.addAll(jis);
	}
		
	public JoinItem unsetJoinItem(String unit_n,String interfacen)
	{
		for(JoinItem ji:joinItems)
		{
			if(!ji.getJoinUnitName().equals(unit_n))
				continue ;
			
			if(!ji.getInterfaceName().equals(interfacen))
				continue ;
			
			joinItems.remove(ji);
			return ji ;
		}
		
		return null;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData();
		
		xd.setParamValue("join_type", joinType.toString());
		
		if(adapterXDS!=null)
			xd.setSubDataSingle("adp_data_struct", adapterXDS.toXmlData());
		
		List<XmlData> xds = xd.getOrCreateSubDataArray("join_items");
		for(JoinItem ji:joinItems)
		{
			xds.add(ji.toXmlData());
		}
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		String jtstr = xd.getParamValueStr("join_type") ;
		if(jtstr!=null)
			joinType = JoinType.valueOf(jtstr);
		
		XmlData adpxds = xd.getSubDataSingle("adp_data_struct");
		if(adpxds!=null)
		{
			adapterXDS = new XmlDataStruct();
			adapterXDS.fromXmlData(adpxds);
		}
		
		List<XmlData> jixds = xd.getSubDataArray("join_items");
		if(jixds!=null)
		{
			for(XmlData tmpxd:jixds)
			{
				JoinItem ji = new JoinItem();
				ji.fromXmlData(tmpxd);
				joinItems.add(ji);
			}
		}
	}
}
