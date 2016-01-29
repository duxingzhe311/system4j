package com.dw.system.xmldata.join;

import java.util.*;

import com.dw.system.xmldata.*;

/**
 * XmlData的连接器
 * 
 * 它自身描述了一个支持的数据结构
 * 并且以JoinBoard为基础,定制了如何根据自身的数据结构访问JoinBoard的数据
 * 
 * 外界如果要和JoinBoard打交道,必须通过定制JoinAdapter
 * 
 * @author Jason Zhu
 */
public class JoinAdapter implements IXmlDataable
{
	
	/**
	 * 连接两个接口之间转换的映射关系
	 * 其主要内容是输出接口的数据结构中的路径-输入接口的数据结构中的路径
	 * @author Jason Zhu
	 */
	public static class MapItem implements IXmlDataable
	{
		/**
		 * 转换适配器的XmlDataStruct路径
		 */
		String adpXdsPath = null ;
		/**
		 * JoinInterface的XmlDataStruct路径
		 */
		String interfaceXdsPath = null ;
		
		/**
		 * 是否要进行强制数据类型转换
		 */
		boolean bForceTrans = false;
		
		/**
		 * 如果输入接口路径对应的是string类型,
		 * 则对应太长的字符串是否要进行自动截断.
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
	 * 对应连接一个JoinUnit中的一个JoinInterface
	 * 及内部的数据转换映射关系
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
	 * 该转换器被使用之前,需要判断是否有效
	 * 由于JoinBoard可以提供判断方法,并且JoinAdapter的更新一般是通过
	 * 底层XmlData传输更新,可以保证每次更新后,都可以获得新对象
	 * 
	 * 该变量只给JoinBoard使用
	 */
	transient int bValid = 0;
	/**
	 * 连接类型
	 */
	JoinType joinType = JoinType.in ;
	
	/**
	 * 连接转换器支持的结构
	 * 如果该内容为null,则表示该转换器不起作用
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
	 * 判断该转换器是否需要工作
	 * 该方法存在的原因是,很多节点内部都有From To转换器对象
	 * 但实际中,他们是不需要运行的,通过人转换器自己处理这件事,可以使得一致
	 * @return
	 */
	public boolean isWillWork()
	{
		if(joinItems.size()<=0)//没有配置连接信息,也被认为不起作用
			return true ;
		
		return adapterXDS!=null&&!adapterXDS.isEmptyStruct();
	}
	/**
	 * 设置转换适配器的数据结构
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
	 * 设置连接信息
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
