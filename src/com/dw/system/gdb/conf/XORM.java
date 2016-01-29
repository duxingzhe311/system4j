package com.dw.system.gdb.conf;

import java.util.*;

import org.w3c.dom.Element;

import com.dw.system.Convert;
import com.dw.system.gdb.ConnPoolMgr;
import com.dw.system.gdb.conf.autofit.*;
import com.dw.system.gdb.connpool.IConnPool;
import com.dw.system.gdb.syn_client.ISynClientable;
import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.gdb.xorm.XORMUtil.XORMPropWrapper;
import com.dw.system.xmldata.XmlDataStruct;

public class XORM
{
	public static final String VAR_SUB_ALL_COLS="all_cols"; //���е���(����������xorm_ext��)
	public static final String VAR_SUB_PK_COL="pk_col"; //������
	public static final String VAR_SUB_NOR_COLS="nor_cols"; //��ͨ��(������������xorm_ext�е�has_col=true��)
	public static final String VAR_SUB_PK_NOR_COLS="pk_nor_cols"; //�����к���ͨ��
	public static final String VAR_SUB_M_DOT_PK_NOR_COLS="m_dot_pk_nor_cols"; //�����к���ͨ��
	public static final String VAR_SUB_TABLE_NAME="table_name"; //������
    
	public static XORM parseContent(Gdb g,Element ele)
	throws Exception
	{
		String varname = ele.getAttribute(Gdb.ATTR_NAME);
		if(varname==null||varname.equals(""))
			throw new Exception("Tag XORM must has name attribute!");
		if(!varname.startsWith("#"))
			throw new Exception("Tag XORM must has name attribute like name=\"#xxx\"");
		
		String cn = ele.getAttribute(Gdb.ATTR_CLASS) ;
		if(cn==null||cn.equals(""))
			return null ;
		XORM rets = new XORM();
		rets.varName = varname ;
		rets.belongToGdb = g ;
		rets.xormClass=  cn ;
		rets.bNoInstall = "true".equals(ele.getAttribute(Gdb.ATTR_NOINSTALL)) ;
		return rets ;
	}
	
	Gdb belongToGdb = null ;
	String varName = null ;
	String xormClass = null ;
	boolean bNoInstall = false;//�жϽ���������,����Ϊ��װ����
	
	private XORM()
	{

	}
	
	
	transient XORMUtil.XORMPropWrapper pkXORMProp = null ;
	/**
	 * ���������Ա������
	 * @return
	 */
	public XORMUtil.XORMPropWrapper getPkXORMPropWrapper()
	{
		if(pkXORMProp!=null)
			return pkXORMProp;
		
		Class c = getXORMClass();
		pkXORMProp = XORMUtil.extractPkXORMPropWrapper(c) ;
		return pkXORMProp;
	}
	
	public String getVarName()
	{
		return varName ;
	}

	public String getXORMClassStr()
	{
		return xormClass;
	}
	
	/**
	 * �жϸ�XORM�����Ƿ񲻲���԰�װ���ݿ��֧��
	 * @return
	 */
	public boolean IsNoInstall()
	{
		return bNoInstall ;
	}
	
	private transient Class xormC = null ;
	
	public Class getXORMClass()// throws ClassNotFoundException
	{
		if(xormC!=null)
			return xormC ;
		
		ClassLoader cl = belongToGdb.getRelatedClassLoader();
		try
		{
		if (cl == null)
			xormC = Class.forName(xormClass);
		else
			xormC = Class.forName(xormClass, true, cl);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException("get XORM Class with name="+xormClass+" error!");
		}
		
		return xormC;
	}
	
	public boolean isSupportSynClient()
	{
		return ISynClientable.class.isAssignableFrom(getXORMClass()) ;
	}
	
	ISynClientable synCT = null ;
	
	public ISynClientable getSynClientImpl() throws InstantiationException, IllegalAccessException
	{
		if(!isSupportSynClient())
			return null ;
		
		if(synCT==null)
		{
			synCT = (ISynClientable)getXORMClass().newInstance() ;
		}
		
		return synCT ;
	}
	
	private transient XORMClass xormCC = null ;
	
	public XORMClass getXORMClassClass()
	{
		if(xormCC!=null)
			return xormCC ;
		
		xormCC = (XORMClass)  getXORMClass().getAnnotation(XORMClass.class);
		return xormCC ;
	}
	
	
	
	private transient XmlDataStruct xormXDS = null ;
	
	public XmlDataStruct getXORMClassXDS()
	{
		if(xormXDS!=null)
			return xormXDS ;
		
		xormXDS = XORMUtil.extractXDSFromClass(getXORMClass()) ;
		return xormXDS;
	}
	
	private transient HashMap<String,XORMProperty> pn2prop = null ;
	
	public HashMap<String,XORMProperty> getXORMProp()
	{
		if(pn2prop!=null)
		{
			return pn2prop;
		}
		
		HashMap<String,XORMProperty> tt = new HashMap<String,XORMProperty>() ;
		
		HashMap<XORMPropWrapper, Class> p2c = XORMUtil.extractXORMProperties(getXORMClass());
		for(XORMPropWrapper pw:p2c.keySet())
		{
			XORMProperty p = pw.getXORMProperty() ;
			tt.put(p.name(),p) ;
		}
		pn2prop = tt ;
		return pn2prop;
	}
	
	public XORMProperty getXORMProp(String propn)
	{
		HashMap<String,XORMProperty> p2n = getXORMProp() ;
		return p2n.get(propn);
	}
	
	transient List<XORMProperty> prop_list = null ;
	
	public List<XORMProperty> getXORMPropOrderList()
	{
		if(prop_list!=null)
			return prop_list ;
		
		ArrayList<XORMPropWrapper> ss = new ArrayList<XORMPropWrapper>() ;
		HashMap<XORMPropWrapper, Class> p2c = XORMUtil.extractXORMProperties(getXORMClass());
		for(XORMPropWrapper pw:p2c.keySet())
		{
			ss.add(pw) ;
		}
		
		Convert.sort(ss) ;
		ArrayList<XORMProperty> rrr  =new ArrayList<XORMProperty>() ;
		for(XORMPropWrapper s:ss)
		{
			rrr.add(s.getXORMProperty()) ;
		}
		prop_list = rrr ;
		return rrr ;
	}
	
	public XORMProperty getXORMPropFirstFile()
	{
		HashMap<String,XORMProperty> p2n = getXORMProp() ;
		for(XORMProperty p:p2n.values())
		{
			if(p.store_as_file())
				return p;
		}
		
		return null ;
	}
	
	
	transient ArrayList<XORMProperty> storeAsFiles = null ;
	
	public ArrayList<XORMProperty> getStoreAsFileXORMProps()
	{
		if(storeAsFiles!=null)
			return storeAsFiles ;
		
		ArrayList<XORMProperty> ps = new ArrayList<XORMProperty>() ;
		HashMap<String,XORMProperty> p2n = getXORMProp() ;
		for(XORMProperty p:p2n.values())
		{
			if(p.store_as_file())
				ps.add(p) ;
		}
		
		storeAsFiles = ps ;
		return storeAsFiles ;
	}
	
	public Gdb getBelongToGdb()
	{
		return belongToGdb ;
	}
	
	/**
	 * �õ���XORM�Դ��Ĳ���sql����
	 * @return
	 * @throws ClassNotFoundException 
	 */
	public HashMap<String,String> getSqlCatParamValues() throws ClassNotFoundException
	{
		HashMap<String,String> v2cs =  getXORMVarColumnStr();
		HashMap<String,String> rets = new HashMap<String,String>() ;
		for(Map.Entry<String,String> v2c : v2cs.entrySet())
		{
			rets.put(varName+"."+v2c.getKey(), v2c.getValue());
		}
		return rets;
	}
	
	
	transient HashMap<String,String> varColMap = null ;
	/**
	 * ����XORM��,���XORM������Ϣ��Ҫ��Sql��������
	 * @param xormc
	 * @return
	 * @throws ClassNotFoundException
	 */
	public HashMap<String,String> getXORMVarColumnStr() throws ClassNotFoundException
	{
		if(varColMap!=null)
			return varColMap;
		
		StringBuilder sb = new StringBuilder();
		JavaTableInfo jti = XORMUtil.extractJavaTableInfo(getXORMClass(),sb);
		
		if(jti==null)
			throw new RuntimeException(this.xormClass+"->"+sb.toString());
		
		HashMap<String,String> ret = new HashMap<String,String>() ;
		ret.put(XORM.VAR_SUB_TABLE_NAME, jti.getTableName());
		String pkcol = jti.getPkColumnInfo().getColumnName();
		ret.put(XORM.VAR_SUB_PK_COL, pkcol);
		StringBuilder norcolsb = new StringBuilder();
		StringBuilder m_norcolsb = new StringBuilder();
		StringBuilder nor_all_colsb = new StringBuilder();
		JavaColumnInfo[] notpkcols = jti.getNorColumnInfos() ;
		int norcols_num = notpkcols.length ;
		JavaColumnInfo xorm_ext_col = null ;
		if(notpkcols[notpkcols.length-1].getColumnName().equals(XORMUtil.COL_XORM_EXT))
		{
			xorm_ext_col = notpkcols[notpkcols.length-1];
			norcols_num -- ;
		}
		for(int i = 0 ; i < norcols_num ; i ++)
		{
			nor_all_colsb.append(',').append(notpkcols[i].getColumnName());
			
			if(notpkcols[i].isReadOnDemand())
				continue ;
			
			norcolsb.append(',').append(notpkcols[i].getColumnName());
			m_norcolsb.append(",m.").append(notpkcols[i].getColumnName());
		}
		String norcols = norcolsb.substring(1);
		String m_norcols= m_norcolsb.substring(1);
		String nor_all_cols = nor_all_colsb.substring(1);
		ret.put(XORM.VAR_SUB_NOR_COLS, norcols);
		ret.put(XORM.VAR_SUB_PK_NOR_COLS, pkcol+","+norcols);
		ret.put(XORM.VAR_SUB_M_DOT_PK_NOR_COLS, "m."+pkcol+","+m_norcols) ;
		if(xorm_ext_col==null)
			ret.put(XORM.VAR_SUB_ALL_COLS, pkcol+","+nor_all_cols);
		else
			ret.put(XORM.VAR_SUB_ALL_COLS, pkcol+","+nor_all_cols+","+xorm_ext_col.getColumnName());
		varColMap = ret ;
		return varColMap;
	}
	
	public String getListXORMSql(String wherestr,String orderby) throws ClassNotFoundException
	{
		HashMap<String,String> v2c = getXORMVarColumnStr();
		String pknor = v2c.get(XORM.VAR_SUB_PK_NOR_COLS);
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(pknor).append(" from ").append(getJavaTableInfo().getTableName());
		if(!Convert.isNullOrTrimEmpty(wherestr))
			sb.append(" where ").append(wherestr);
		if(!Convert.isNullOrTrimEmpty(orderby))
		{
			sb.append(" order by ").append(orderby);
//			if(b_order_desc)
//				sb.append(" desc");
		}
		return sb.toString();
	}
	
	
//	public String getListXORMSqlByColOpers(String[] cols,String[] opers,String orderby) throws ClassNotFoundException
//	{
//		HashMap<String,String> v2c = getXORMVarColumnStr();
//		String pknor = v2c.get(XORM.VAR_SUB_PK_NOR_COLS);
//		StringBuilder sb = new StringBuilder();
//		sb.append("select ").append(pknor).append(" from ").append(getJavaTableInfo().getTableName());
//		sb.append(getXORMWhereSqlByColOpers(cols,opers,orderby));
//		return sb.toString();
//	}
	
	public String getListXORMSqlByColOpers(String[] cols,String[] opers,
			Object[] vals,boolean[] null_ignores,//�ж�ÿ����ֵ�е�vals��ֵ�Ƿ���Ҫ����
			String orderby) throws ClassNotFoundException
	{
		HashMap<String,String> v2c = getXORMVarColumnStr();
		String pknor = v2c.get(XORM.VAR_SUB_PK_NOR_COLS);
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(pknor).append(" from ").append(getJavaTableInfo().getTableName());
		sb.append(getXORMWhereSqlByColOpers(cols,opers,vals,null_ignores,orderby));
		return sb.toString();
	}
	
	public String getXORMWhereSqlByColOpers(String[] cols,String[] opers,
			Object[] vals,boolean[] ignore_nulls,
			String orderby)
	{
		StringBuilder sb = new StringBuilder() ;
		if(vals==null)
		{
			if(cols.length>0)
			{
				sb.append(" where ");
				sb.append(cols[0]).append(opers[0]).append("?") ;
				for(int k = 1 ; k < cols.length ; k ++)
					sb.append(" and ").append(cols[k]).append(opers[k]).append("?") ;
			}
		}
		else
		{//���������ݣ�Ҳ����û����
			StringBuffer tmpsb = new StringBuffer() ;
			for(int k = 0 ; k < cols.length ; k ++)
			{
				if(vals[k]==null && 
						ignore_nulls!=null && ignore_nulls[k])
					continue ;//ignore null
				if(tmpsb.length()==0)
					tmpsb.append(" where ").append(cols[k]).append(opers[k]).append("?") ;
				else
					tmpsb.append(" and ").append(cols[k]).append(opers[k]).append("?") ;
			}
			sb.append(tmpsb) ;
		}
		
		if(!Convert.isNullOrTrimEmpty(orderby))
		{
			sb.append(" order by ").append(orderby);
//			if(b_order_desc)
//				sb.append(" desc");
		}
		return sb.toString();
	}
	
	
	public String getListXORMPkIdsSql(String wherestr,String orderby) throws ClassNotFoundException
	{
		HashMap<String,String> v2c = getXORMVarColumnStr();
		String pkcol = v2c.get(XORM.VAR_SUB_PK_COL);
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(pkcol).append(" from ").append(getJavaTableInfo().getTableName());
		if(!Convert.isNullOrTrimEmpty(wherestr))
			sb.append(" where ").append(wherestr);
		if(!Convert.isNullOrTrimEmpty(orderby))
		{
			sb.append(" order by ").append(orderby);
//			if(b_order_desc)
//				sb.append(" desc");
		}
		return sb.toString();
	}
	
//	public HashMap<String,String> getSqlCatParamValues0() throws ClassNotFoundException
//	{
//		StringBuilder sb = new StringBuilder();
//		JavaTableInfo jti = XORMUtil.extractJavaTableInfo(getXORMClass(),sb);
//		
//		if(jti==null)
//			throw new RuntimeException(sb.toString());
//		
//		HashMap<String,String> ret = new HashMap<String,String>() ;
//		ret.put(varName+"."+VAR_SUB_TABLE_NAME, jti.getTableName());
//		String pkcol = jti.getPkColumnInfo().getColumnName();
//		ret.put(varName+"."+VAR_SUB_PK_COL, pkcol);
//		StringBuilder norcolsb = new StringBuilder();
//		JavaColumnInfo[] notpkcols = jti.getNorColumnInfos() ;
//		int norcols_num = notpkcols.length ;
//		JavaColumnInfo xorm_ext_col = null ;
//		if(notpkcols[notpkcols.length-1].getColumnName().equals(XORMUtil.COL_XORM_EXT))
//		{
//			xorm_ext_col = notpkcols[notpkcols.length-1];
//			norcols_num -- ;
//		}
//		for(int i = 0 ; i < norcols_num ; i ++)
//		{
//			norcolsb.append(',').append(notpkcols[i].getColumnName());
//		}
//		String norcols = norcolsb.substring(1);
//		ret.put(varName+"."+VAR_SUB_NOR_COLS, norcols);
//		ret.put(varName+"."+VAR_SUB_PK_NOR_COLS, pkcol+","+norcols);
//		if(xorm_ext_col==null)
//			ret.put(varName+"."+VAR_SUB_ALL_COLS, pkcol+","+norcols);
//		else
//			ret.put(varName+"."+VAR_SUB_ALL_COLS, pkcol+","+norcols+","+xorm_ext_col.getColumnName());
//		return ret ;
//	}
	
	transient JavaTableInfo javaTableInfo = null;
	
	public JavaTableInfo getJavaTableInfo() throws ClassNotFoundException
	{
		if(javaTableInfo!=null)
			return javaTableInfo ;
		
		StringBuilder fd = new StringBuilder();
		javaTableInfo = XORMUtil.extractJavaTableInfo(getXORMClass(), fd);
		if(javaTableInfo==null)
			throw new RuntimeException(fd.toString());
		
		return javaTableInfo;
	}
	
	
	private transient String selectByPkSql = null ;
	
	public String getSelectByPkSql() throws Exception
	{
		if(selectByPkSql!=null)
			return selectByPkSql ;
		
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		//StringBuilder sqlsb = XORMUtil.getSelectByPkSql(dbt, getXORMClass(), sb) ;
		StringBuilder sqlsb = DbSql.getDbSqlByDBType(dbt).getSelectByPkIdSql(jti);
		
		selectByPkSql = sqlsb.toString();
		return selectByPkSql;
	}
	
	
	public String getSelectColsByPkSql(String[] col_names) throws Exception
	{
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		//StringBuilder sqlsb = XORMUtil.getSelectByPkSql(dbt, getXORMClass(), sb) ;
		StringBuilder sqlsb = DbSql.getDbSqlByDBType(dbt).getSelectColsByPkIdSql(jti,col_names);
		return sqlsb.toString() ;
	}
	
	private transient String maxPkIdSql = null ;
	
	public String getMaxPkIdSql() throws Exception
	{
		if(maxPkIdSql!=null)
			return maxPkIdSql ;
		
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		//StringBuilder sqlsb = XORMUtil.getSelectByPkSql(dbt, getXORMClass(), sb) ;
		StringBuilder sqlsb = DbSql.getDbSqlByDBType(dbt).getMaxPkIdSql(jti);
		
		maxPkIdSql = sqlsb.toString();
		return maxPkIdSql;
	}
	
	transient HashMap<String,String> uniqueColSqls = new HashMap<String,String>() ;
	public String getSelectByUniqueColSql(String unique_colname) throws Exception
	{
		String sql = uniqueColSqls.get(unique_colname);
		if(sql!=null)
			return sql ;
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		//StringBuilder sqlsb = XORMUtil.getSelectByPkSql(dbt, getXORMClass(), sb) ;
		StringBuilder sqlsb = DbSql.getDbSqlByDBType(dbt).getSelectByUniqueColSql(jti,unique_colname);
		
		sql = sqlsb.toString() ;
		uniqueColSqls.put(unique_colname, sql) ;
		//selectByPkSql = sqlsb.toString();
		return sql;
	}
	
	private transient String[] insertWithNewIdRetSqls = null ;
	public String[] getInsertWithNewIdReturnSqls() throws ClassNotFoundException
	{
		if(insertWithNewIdRetSqls!=null)
			return insertWithNewIdRetSqls;
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo();
		if(pkcol!=null&&pkcol.isAutoStringValuePk())
		{
			StringBuilder sb = DbSql.getDbSqlByDBType(dbt).getInsertSqlWithInputId(jti) ;
			String[] s1 = new String[]{sb.toString()};
			insertWithNewIdRetSqls = s1 ;
			return insertWithNewIdRetSqls;
		}
		
		StringBuffer[] sbs = DbSql.getDbSqlByDBType(dbt).getInsertSqlWithNewIdReturn(jti);
		if(sbs==null)
			throw new RuntimeException("no insert sql");
		
		String[] rets = new String[sbs.length];
		for(int i = 0 ; i < rets.length ; i ++)
			rets[i] = sbs[i].toString();
		return rets;
	}
	
	
	public boolean isAutoStringValuePk() throws ClassNotFoundException
	{
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return false;
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo();
		if(pkcol==null)
			return false;
		
		return pkcol.isAutoStringValuePk();
	}
	
	private transient String insertWithInputIdSql = null ;
	public String getInsertSqlWithInputId() throws ClassNotFoundException
	{
		if(insertWithInputIdSql!=null)
			return insertWithInputIdSql;
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		
		StringBuilder sb = DbSql.getDbSqlByDBType(dbt).getInsertSqlWithInputId(jti) ;
		
		insertWithInputIdSql = sb.toString() ;
		return insertWithInputIdSql;
	}
	
	
	String autoUpdateDTCol = null ;
	/**
	 * ����Զ�ʱ�������
	 * @return
	 */
	public String getAutoUpdateDTCol()
	{
		if(autoUpdateDTCol!=null)
			return autoUpdateDTCol;
		
		HashMap<String,XORMProperty> pn2prop = getXORMProp();
		for(Map.Entry<String, XORMProperty> n2p:pn2prop.entrySet())
		{
			if(n2p.getValue().is_auto_update_dt())
			{
				autoUpdateDTCol= n2p.getKey() ;
				return autoUpdateDTCol;
			}
		}
		
		autoUpdateDTCol = "" ;
		return autoUpdateDTCol ;
	}
	
	private transient String updateSql = null ;
	public String getUpdateByPkSql() throws ClassNotFoundException
	{
		if(updateSql!=null)
			return updateSql ;
		
		DBType dbt = ConnPoolMgr.getConnPool(belongToGdb).getDBType();
		JavaTableInfo jti = getJavaTableInfo();
		if (jti == null)
			return null;
		StringBuilder sb = DbSql.getDbSqlByDBType(dbt).getUpdateByPkIdSql(jti);
		if(sb==null)
			throw new RuntimeException(sb.toString());
		updateSql = sb.toString() ;
		return updateSql ;
	}
}
