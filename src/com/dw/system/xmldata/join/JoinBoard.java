package com.dw.system.xmldata.join;

import java.util.*;

import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.*;

/**
 * 连接板块
 * 
 * 它集中管理所有的JoinUnit,并且提供相应的数据转换支持
 * 
 * 它好比数据总线控制器
 * 又如,BizFlow中的DataFields使用时的统一管理
 * 
 * 所有外界程序及模块,如果要使用一些集中管理的数据,都得通过JoinBoard打交道
 * 1,通过JoinBoard提供的信息,定制Joiner信息
 *   a,可以获得所有的JoinUnit信息
 *   b,可以检查一个定制好的Joiner是否有效
 *   
 * 2,通过定制好的Joiner信息,根据JoinBoard及具体的数据实例运行器IJoinInsRunner
 * 		就可以方便的交换数据了 : pulseXxx方法
 * 
 * @author Jason Zhu
 *
 */
public abstract class JoinBoard
{
	/**
	 * 获得所有的JoinUnit
	 * @return
	 */
	public abstract ArrayList<JoinUnit> getAllJoinUnits() ;
	
	/**
	 * 根据接口名称获得对应的接口对象
	 * @param name
	 * @return
	 */
	public abstract JoinUnit getJoinUnit(String name);
	
	/**
	 * 检查连接转换器是否有效
	 * 
	 * @param j
	 * @param failedreson
	 * @return
	 */
	public boolean checkJoiner(JoinAdapter j,StringBuffer failedreson)
	{
		if(j.bValid>0)
			return true ;
		
		if(j.bValid<0)
			return false;
		
		//=0表明还没有被判断过
		XmlDataStruct adp_xds = j.getAdapterXmlDataStruct();
		if(adp_xds==null)
			return false;
		
		ArrayList<JoinAdapter.JoinItem> jits = j.getJoinItems();
		if(jits.size()<=0)
		{
			j.bValid = 1;
			return true ;//true and it will do nothing
		}
		
		for(JoinAdapter.JoinItem jit:jits)
		{
			JoinInterface tmpji = getJoinInterface(jit) ;
			if(tmpji==null)
			{
				failedreson.append("cannot find JoinInterface with name="+jit.getInterfaceName()+" in JoinUnit="+jit.getJoinUnitName());
				j.bValid = -1 ;
				return false;
			}
			
			if(j.getJoinType()!=tmpji.getJoinInterfaceType())
			{
				failedreson.append("interface name="+tmpji.getJoinInterfaceName()+" join type is not match adapter's join type");
				j.bValid = -1 ;
				return false;
			}
			
			XmlDataStruct inter_xds = tmpji.getJoinInterfaceStruct();
			ArrayList<JoinAdapter.MapItem> mis = jit.getTransMapItem();
			if(mis.size()<=0)
			{
				//该接口映射 do nothing
				continue;
			}
			//判断
			for(JoinAdapter.MapItem mi:mis)
			{
				String adpp = mi.getJoinAdapterXdsPath();
				if(adpp==null||adpp.equals(""))
				{
					failedreson.append("interface name="+tmpji.getJoinInterfaceName()+" has map with no adapter XDS path!");
					j.bValid = -1 ;
					return false;
				}
				String interp = mi.getJoinInterfaceXdsPath() ;
				if(interp==null||interp.equals(""))
				{
					failedreson.append("interface name="+tmpji.getJoinInterfaceName()+" has map with no interface XDS path!");
					j.bValid = -1 ;
					return false;
				}
				
				XmlDataPath adpxp = new XmlDataPath(adpp);
				if(adpxp.isRelative())
				{
					failedreson.append("adapter map path="+adpp+" is relative which in invalid!");
					j.bValid = -1 ;
					return false;
				}
				XmlDataPath interxp = new XmlDataPath(interp);
				if(interxp.isRelative())
				{
					failedreson.append("interface map path="+interp+" is relative which in invalid!");
					j.bValid = -1 ;
					return false;
				}
				
				
				
				if(adpxp.isStruct()!=interxp.isStruct())
				{
					failedreson.append("interface map path="+interp+" is struct="+interxp.isStruct()+" while adapter path="+adpp+" is struct="+adpxp.isStruct());
					j.bValid = -1 ;
					return false;
				}
				
				if(adpxp.isStruct())
				{
					XmlDataStruct adp_sub_xds = adp_xds.getSubStructByPath(adpxp);
					if(adp_sub_xds==null)
					{
						failedreson.append("cannot find adapter map path="+adpp+" sub struct!");
						j.bValid = -1 ;
						return false;
					}
					
					if(adp_sub_xds.isArrayWithParent())
					{
						XmlDataStruct tmpxds = adp_sub_xds.getParent();
						if(tmpxds!=null&&tmpxds.isArrayWithParent())
						{//结构的父结构是数组的话不允许
							failedreson.append("adapter map path="+adpp+" 's parent struct is array!");
							j.bValid = -1 ;
							return false;
						}
						
						if(!adpxp.isValueArray())
						{
							failedreson.append("adapter map path="+adpp+" 's struct is array,but path is not!");
							j.bValid = -1 ;
							return false;
						}
					}
					
					XmlDataStruct inter_sub_xds = inter_xds.getSubStructByPath(interxp);
					if(inter_sub_xds==null)
					{
						failedreson.append("cannot find interface map path="+interp+" sub struct!");
						j.bValid = -1 ;
						return false;
					}
					
					if(inter_sub_xds.isArrayWithParent())
					{
						XmlDataStruct tmpxds = inter_sub_xds.getParent();
						if(tmpxds!=null&&tmpxds.isArrayWithParent())
						{//结构的父结构是数组的话不允许
							failedreson.append("interface map path="+adpp+" 's parent struct is array!");
							j.bValid = -1 ;
							return false;
						}
						
						if(!interxp.isValueArray())
						{
							failedreson.append("interface map path="+interp+" 's struct is array,but path is not!");
							j.bValid = -1 ;
							return false;
						}
					}
					
					if(adp_sub_xds.isArray()!=inter_sub_xds.isArray())
					{
						failedreson.append("interface map path=")
							.append(adpp)
							.append(" array=").append(inter_sub_xds.isArray())
							.append(" while adapter map path=")
							.append(adpp).append(" array=").append(adp_sub_xds.isArray());
						j.bValid = -1 ;
						return false;
					}
					
					if(j.getJoinType()==JoinType.in)
					{//adp -> interface
						if(!adp_sub_xds.checkFitFor(inter_sub_xds, failedreson))
						{
							j.bValid = -1;
							return false;
						}
						
						if(!inter_sub_xds.isNullable()&&adp_sub_xds.isNullable())
						{
							failedreson.append("interface map path=")
								.append(interp).append(" 's sub struct is not nullable!");
							j.bValid = -1 ;
							return false;
						}
					}
					else if(j.getJoinType()==JoinType.out)
					{// interface -> adp
						if(!inter_sub_xds.checkFitFor(adp_sub_xds, failedreson))
						{
							j.bValid = -1;
							return false;
						}
						
						if(!adp_sub_xds.isNullable()&&inter_sub_xds.isNullable())
						{
							failedreson.append("adapter map path=")
								.append(interp).append(" 's sub struct is not nullable!");
							j.bValid = -1 ;
							return false;
						}
					}
				}
				else
				{//member
					XmlDataMember adp_xvd = adp_xds.getXmlDataMemberByPath(adpxp.getPath());
					if(adp_xvd==null)
					{
						failedreson.append("cannot find adapter map path="+adpp+" member!");
						j.bValid = -1 ;
						return false;
					}
					XmlDataMember inter_xvd = inter_xds.getXmlDataMemberByPath(interxp.getPath());
					if(inter_xvd==null)
					{
						failedreson.append("cannot find interface map path="+interp+" member!");
						j.bValid = -1 ;
						return false;
					}
					
					XmlDataStruct adp_xvd_p = adp_xvd.getBelongTo();
					if(adp_xvd_p!=null&&adp_xvd_p.isArrayWithParent())
					{
						failedreson.append("adapter map path="+adpp+" 's parent struct is array!");
						j.bValid = -1 ;
						return false;
					}
					
					XmlDataStruct inter_xvd_p = inter_xvd.getBelongTo();
					if(inter_xvd_p!=null&&inter_xvd_p.isArrayWithParent())
					{
						failedreson.append("interface map path="+interp+" 's parent struct is array!");
						j.bValid = -1 ;
						return false;
					}
					
					if(adp_xvd.isArray()!=inter_xvd.isArray())
					{
						failedreson.append("interface map path=")
							.append(interp).append(" array=").append(inter_xvd.isArray())
							.append("while adpater map path=").append(adpp)
							.append(" array=").append(adp_xvd.isArray());
						j.bValid = -1 ;
						return false;
					}
					
					if(!adp_xvd.getValType().equals(inter_xvd.getValType())
						&&!mi.isForceTransfer())
					{
						failedreson.append("interface map path=")
						.append(interp).append(" XmlValType=").append(inter_xvd.getValType())
						.append("while adpater map path=").append(adpp)
						.append(" XmlValType=").append(adp_xvd.getValType());
						j.bValid = -1 ;
						return false;
					}
					
					if(j.getJoinType()==JoinType.in)
					{//adp -> interface
						if(!inter_xvd.isNullable()&&adp_xvd.isNullable())
						{
							failedreson.append("interface map path=")
								.append(interp).append(" 's member is not nullable!");
							j.bValid = -1 ;
							return false;
						}
					}
					else if(j.getJoinType()==JoinType.out)
					{// interface -> adp
						if(!adp_xvd.isNullable()&&inter_xvd.isNullable())
						{
							failedreson.append("adapter map path=")
								.append(interp).append(" 's sub struct is not nullable!");
							j.bValid = -1 ;
							return false;
						}
					}
				}
				
				//adp_xds.get
			}
		}
		
		j.bValid = 1 ;
		return true;
	}
	
	public JoinInterface getJoinInterface(JoinAdapter.JoinItem jit)
	{
		return getJoinInterface(jit.getJoinUnitName(),jit.getInterfaceName());
	}
	
	public JoinInterface getJoinInterface(String unit_n,String inter_n)
	{
		JoinUnit ju = getJoinUnit(unit_n);
		if(ju==null)
			return null ;
		
		return ju.getJoinInterface(inter_n);
	}
	//public static XmlData transferXmlData(XmlData tobetrans,)
	/**
	 * 转换数据
	 * 
	 * 根据连接项及被转换的数据,做数据格式转换
	 * @param ji 
	 * @param tb_xd
	 * @return
	 */
	public XmlData transferJoinData(JoinAdapter jadp,JoinAdapter.JoinItem ji,XmlData tb_xd)
		//throws Exce
	{
		if(tb_xd==null)
			return null ;
		
		String jun = ji.getJoinUnitName();
		JoinUnit ju = getJoinUnit(jun);
		if(ju==null)
			throw new RuntimeException("cannot find JoinUnit with name="+jun);
		String jitn = ji.getInterfaceName();
		JoinInterface jit = ju.getJoinInterface(jitn);
		if(jit==null)
			throw new RuntimeException("cannot find JoinInterface with name="+jitn+" in JoinUnit with name="+jun);
		
		//输入数据必须满足的格式
		XmlDataStruct adpxds = jadp.getAdapterXmlDataStruct();
		
		//转换后必须满足的格式
		XmlDataStruct xds = jit.getJoinInterfaceStruct() ;
		
		XmlData retxd = new XmlData();
		
		JoinType jt = jadp.getJoinType();
		String from_path = null,to_path = null ;
		XmlDataStruct to_xds = null ;
		for(JoinAdapter.MapItem mi:ji.getTransMapItem())
		{
			if(jt==JoinType.in)
			{//adp -> interface
				from_path = mi.getJoinAdapterXdsPath();
				to_path = mi.getJoinInterfaceXdsPath();
				to_xds = jit.getJoinInterfaceStruct();
			}
			else if(jt==JoinType.out)
			{//interface -> adp
				to_path = mi.getJoinAdapterXdsPath();
				from_path = mi.getJoinInterfaceXdsPath();
				to_xds = jadp.getAdapterXmlDataStruct() ;
			}
			else
			{
				continue ;
			}
			
			XmlDataPath fromxdp = new XmlDataPath(from_path) ;
			XmlDataPath toxdp = new XmlDataPath(to_path) ;
			if(fromxdp.isStruct())
			{
				if(fromxdp.isValueArray())
				{
					List<XmlData> subxds = tb_xd.getSubDataArrayByPath(fromxdp);
					if(subxds!=null)
					{
						retxd.setSubDataArrayByPath(toxdp, subxds);
					}
				}
				else
				{
					XmlData subxd = tb_xd.getSubDataByPath(fromxdp);
					if(subxd!=null)
					{
						retxd.setSubDataSingleByPath(toxdp, subxd);
					}
				}
			}
			else
			{
				XmlVal xv = tb_xd.getParamXmlValByPath(fromxdp);
				if(xv!=null)
				{
					XmlDataMember toxvd = to_xds.getXmlDataMemberByPath(toxdp.getPath());
					if(toxvd==null)
						throw new RuntimeException("cannot find XmlValDef with target path"+to_path);
					
					if(!toxvd.isArray())
						throw new RuntimeException("XmlValDef with target path"+to_path+" is not array!");
					
					String tovt = toxvd.getValType();
					if(!tovt.equals(xv.getValType())&&!mi.isForceTransfer())
						throw new RuntimeException("transfer join data meet XmlVal Type from="+xv.getValType()+" to="+tovt);
					
					XmlVal newxv = xv.copyMeWithNewType(tovt);
					retxd.setParamXmlValByPath(toxdp, newxv);
				}
			}
			//mi.g
		}
		return retxd;
	}
	/**
	 * 根据具体的运行时数据实例和指定的输入连接器
	 * 输入外界数据到对应的运行时数据实例中
	 * 
	 * 1,连接器首先根据内部的映射关系对输入的数据进行分解转换
	 * 2,根据分解转换的数据,分别调用运行实例中不同JoinUnit和JoinInterface的输入数据方法
	 * @param jir
	 * @param j
	 * @param in_data 外界数据
	 */
	public void pulseInData(IJoinInsRunner jir,JoinAdapter in_j,XmlData in_data)
		throws Exception
	{
		if(in_j.getJoinType()!=JoinType.in)
			throw new IllegalArgumentException("the join adapter is not in!");
		
		StringBuffer fr = new StringBuffer();
		if(!checkJoiner(in_j,fr))
			throw new Exception("invalid JoinAdapter:"+fr.toString());
		
		//对输入的XmlData根据每个JoinItem进行分解转换,并插入到不同的接口中
		ArrayList<JoinAdapter.JoinItem> jits = in_j.getJoinItems();
		if(jits.size()<=0)
		{// do nothing
			return ;
		}
		
		for(JoinAdapter.JoinItem jit:jits)
		{
			JoinUnit ju = getJoinUnit(jit.getJoinUnitName());
			JoinInterface ji = ju.getJoinInterface(jit.getInterfaceName());
			
			XmlData resxd = transferJoinData(in_j,jit,in_data);
			jir.onPulseInData(ju, ji, resxd);
		}
	}
	
	public XmlData pulseOutData(IJoinInsRunner jir,JoinAdapter out_j)
		throws Exception
	{
		if(out_j.getJoinType()!=JoinType.out)
			throw new IllegalArgumentException("the join adapter is not in!");
		
		StringBuffer fr = new StringBuffer();
		if(!checkJoiner(out_j,fr))
			throw new Exception("invalid JoinAdapter:"+fr.toString());
		
		//对输入的XmlData根据每个JoinItem进行分解转换,并插入到不同的接口中
		ArrayList<JoinAdapter.JoinItem> jits = out_j.getJoinItems();
		if(jits.size()<=0)
		{// do nothing
			return null;
		}
		
		XmlData retxd = new XmlData();
		for(JoinAdapter.JoinItem jit:jits)
		{
			JoinUnit ju = getJoinUnit(jit.getJoinUnitName());
			JoinInterface ji = ju.getJoinInterface(jit.getInterfaceName());
			
			XmlData outxd = jir.onPulseOutData(ju, ji);
			XmlData resxd = null ;
			if(outxd!=null)
				resxd = transferJoinData(out_j,jit,outxd);
			
			retxd.combineAppend(resxd);
		}
		
		return retxd ;
	}
}
