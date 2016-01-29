package com.dw.system.gdb.syn;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.conf.Gdb;
import com.dw.system.gdb.conf.XORM;
import com.dw.system.gdb.conf.autofit.JavaTableInfo;
import com.dw.system.xmldata.*;

/**
 * ��־��
 * @author Jason Zhu
 */
public class GDBLogItem implements IXmlDataable
{
	static long LAST_DT = -1 ;
	static long LAST_COUNT = 0 ;
	
	
	/**
	 * ����ʱ�����Ψһ��ʱ���ֵ
	 * @return
	 */
	private static synchronized long[] calNewDT()
	{
		long ctm = System.currentTimeMillis() ;
		if(ctm==LAST_DT)
		{
			LAST_COUNT ++ ;
			return new long[]{ctm,LAST_COUNT} ;
		}
		
		LAST_DT = ctm ;
		LAST_COUNT = 0 ;
		return new long[]{ctm,LAST_COUNT} ;
	}
	/**
	 * ��־ʱ���
	 */
	long logDT = -1 ;
	
	/**
	 * ����ٶ�̫��,�������п���ͬһ�����ڳ��ֶ�μ�¼��������ĳЩ���������������¾��������ٶȣ�
	 * Ϊ�˱�������ظ���������һ�������ڼ�����
	 * 
	 * �������ʱ�����һ���ظ����򱾼���ֵ��һ��������0��
	 */
	long logInDTCount = 0 ;
	
	/**
	 * ��־������Ӧ�ı�
	 */
	String tableName = null ;
	
	/**
	 * ��Ӧ��Sql���
	 */
	String sqlStr = null ;
	
	/**
	 * 
	 */
	HashMap<Integer,GDBLogParam> idx2Param = new HashMap<Integer,GDBLogParam>() ;
	
	public GDBLogItem()
	{}
	
	/**
	 * ��
	 * @param tablen
	 * @param sqlstr
	 */
	public GDBLogItem(String tablen,String sqlstr)
	{
		tableName = tablen ;
		sqlStr = sqlstr ;
		//sqlParam = sqlp;
	}
	
	/**
	 * �ڲ�ʹ�ô����ݿ�ָ���ʱ���õ��Ĺ��캯��
	 * @param tablen
	 * @param dt
	 * @param dtcount
	 * @param sqlstr
	 * @param parms
	 * @throws Exception 
	 */
	GDBLogItem(String tablen,long dt,long dtcount,String sqlstr,byte[] parms) throws Exception
	{
		tableName = tablen ;
		logDT = dt;
		logInDTCount = dtcount ;
		
		sqlStr = sqlstr ;
		
		setLogParamByteArray(parms);
	}
	
	public boolean isInsertSql()
	{
		if(sqlStr==null)
			return false;
		
		return sqlStr.trim().toLowerCase().startsWith("insert") ;
	}
	/**
	 * ��Conn Commit��ʱ�򣬸÷��������ã��������еľ���ֵ
	 *
	 */
	public void calNewLotDT()
	{
		long[] dt = calNewDT() ;
		logDT = dt[0];
		logInDTCount = dt[1] ;
	}
	
	
	public long getLogDT()
	{
		return logDT ;
	}
	
	public long getLogInDTCount()
	{
		return logInDTCount;
	}
	
	public String getTableName()
	{
		return tableName ;
	}
	
	public String getSqlStr()
	{
		return sqlStr ;
	}
	
	
	public JavaTableInfo getJavaTableInfo() throws ClassNotFoundException
	{
		Class c = GDBLogManager.getInstance().getXORMClassByTableName(tableName) ;
		if(c==null)
			return null ;
		XORM x = Gdb.getXORMByGlobal(c) ;
		if(x==null)
			return null ;
		
		return x.getJavaTableInfo() ;
	}
	/**
	 * ���ݴ���id�����update ��insert ���Ĵ��оֲ����µ�sql���
	 * �Է�ֹ����Ӱ���������������
	 */
	String getSqlStrInProxy(String pkcoln,String proxyid)
	{
		String sqlss = sqlStr.trim() ;
		String lstr = sqlss.toLowerCase() ;
		if(lstr.startsWith("update")||lstr.startsWith("delete"))
		{
			int wherep = lstr.indexOf("where") ;
			if(wherep<0)
			{
				return sqlss +" where "+pkcoln+" like '"+proxyid+"%'" ;
			}
			else
			{
				return sqlss.substring(0,wherep)
					+" where "+pkcoln+" like '"+proxyid+"%' and ("+sqlss.substring(wherep+5)+")";
			}
		}
		else
		{
			return sqlss ;
		}
	}
	
	public HashMap<Integer,GDBLogParam> getLogParam()
	{
		return idx2Param ;
	}
	
	public String getLogParamXmlString()
	{
		XmlData xd = new XmlData() ;
		extractLogParamIntoXmlData(xd);
		return xd.toXmlString() ;
	}
	
	
	private void extractLogParamIntoXmlData(XmlData xd)
	{
		xd.setParamValue("table", this.tableName) ;
		
		if(idx2Param!=null&&idx2Param.size()>0)
		{
			List<XmlData> xds = xd.getOrCreateSubDataArray("ps");
			for(GDBLogParam p:idx2Param.values())
			{
				xds.add(p.toXmlData()) ;
			}
		}
	}
	
	private void injectLogParamFromXmlData(XmlData xd)
	{
		tableName = xd.getParamValueStr("table");
		
		List<XmlData> xds = xd.getSubDataArray("ps");
		if(xds==null||xds.size()<=0)
			return ;
		
		for(XmlData xd0:xds)
		{
			GDBLogParam p = new GDBLogParam() ;
			p.fromXmlData(xd0) ;
			
			idx2Param.put(p.getIdx(), p) ;
		}
	}
	
	
	byte[] getLogParamByteArray()
	{
		XmlData xd = new XmlData() ;
		
		extractLogParamIntoXmlData(xd);
//		xd.setParamValue("table", this.tableName) ;
//		List<XmlData> xds = xd.getOrCreateSubDataArray("ps");
//		for(GDBLogParam p:idx2Param.values())
//		{
//			xds.add(p.toXmlData()) ;
//		}
		return xd.toBytesWithUTF8();
	}
	
	
	void setLogParamByteArray(byte[] bs) throws Exception
	{
		if(bs==null)
			return ;
		
		XmlData xd = XmlData.parseFromByteArrayUTF8(bs);
		if(xd==null)
			return ;
		
		injectLogParamFromXmlData(xd);
	}
	
	
	
	public void setLogParam(int idx,int type,Object v)
	{
		GDBLogParam lp = new GDBLogParam(idx,type,v) ;
		idx2Param.put(idx,lp) ;
	}
	
	public void setLogParam(int idx,int type,Object v,int sqlt)
	{
		GDBLogParam lp = new GDBLogParam(idx,type,v,sqlt) ;
		idx2Param.put(idx,lp) ;
	}
	
	public void clearLogParam()
	{
		idx2Param.clear() ;
	}

	public XmlData toXmlData()
	{
		XmlData xd = new XmlData() ;
		
		xd.setParamValue("dt", logDT) ;
		xd.setParamValue("dtc", logInDTCount) ;
		xd.setParamValue("table", tableName) ;
		xd.setParamValue("sql", sqlStr) ;
		
		extractLogParamIntoXmlData(xd);
		
		return xd;
	}

	public void fromXmlData(XmlData xd)
	{
		logDT = xd.getParamValueInt64("dt", -1) ;
		logInDTCount = xd.getParamValueInt64("dtc", -1) ;
		tableName = xd.getParamValueStr("table") ;
		sqlStr = xd.getParamValueStr("sql") ;
		
		injectLogParamFromXmlData(xd);
	}
}
