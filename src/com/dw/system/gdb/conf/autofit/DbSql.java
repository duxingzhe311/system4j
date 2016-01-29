package com.dw.system.gdb.conf.autofit;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.conf.DBType;
import com.dw.system.gdb.xorm.XORMUtil;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.XmlVal.XmlValType;

public abstract class DbSql
{
	public static DbSql getDbSqlByDBType(DBType dbt)
	{
		if(dbt==DBType.derby)
		{
			return new DBSqlDerby();
		}
		else if (dbt == DBType.hsql)
		{
			//dbSql = new DBSqlHsql();
			return new DBSqlDerby();
		}
		else if (dbt == DBType.sqlserver)
		{
			return new DBSqlSqlServer();
		}
		else if(dbt==DBType.mysql)
		{
			return new DBSqlMySql();
		}
		else
			throw new IllegalArgumentException("not support for db type="+dbt);
	}
	/**
	 * 根据java类型定义的数据库表信息，生成对应数据库的创建语句脚本
	 * @param jti
	 * @return
	 */
	public List<String> getCreationSqls(JavaTableInfo jti)
	{
		ArrayList<String> rets = new ArrayList<String>();
		StringBuffer[] sbs = constructCreationTable(jti) ;
		for(StringBuffer sb :sbs)
		{
			rets.add(sb.toString());
		}
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(!tmpjci.hasIdx())
				continue ;
			
			StringBuffer sb = constructIndexTable(jti,tmpjci) ;
			rets.add(sb.toString());
		}
		
		for(JavaForeignKeyInfo tmpfki:jti.getForeignKeyInfos())
		{
			StringBuffer sb = constructForeignKeyTable(jti,tmpfki);
			rets.add(sb.toString());
		}
		
		return rets ;
	}
	
	
	public List<String> getCreationSynClientLogSql(Class xormc)
	{
		StringBuilder sb = new StringBuilder();
		JavaTableInfo jti = XORMUtil.extractJavaTableInfo(xormc, sb);
		return getCreationSynClientLogSql(jti.getTableName());
	}
	
	public List<String> getCreationSynClientLogSql(String tablename)
	{
		ArrayList<String> rets = new ArrayList<String>() ;
		tablename += "_syn_client_log";
		
		rets.add("create table "+tablename
				+"("+
				"ClientId "+getSqlType(XmlValType.vt_string, 40)+
				",ChgDT "+getSqlType(XmlValType.vt_int64, -1)+
				",PkId "+getSqlType(XmlValType.vt_string, 30)+
				")") ;
		rets.add("create unique index "+calIndexName(tablename, "ClientId")+
				" on "+tablename+" (ClientId,PkId)");
		rets.add("create index "+calIndexName(tablename, "ChgDT")+
				" on "+tablename+" (ChgDT)");
		//rets.add("create index "+calIndexName(tablename, "PkId")+
		//		" on "+tablename+" (PkId)");
		
		return rets ;
	}
	
	public List<String> getCreationDistributedMode1Sqls(JavaTableInfo jti)
	{
		ArrayList<String> rets = new ArrayList<String>();
		StringBuffer[] sbs = constructCreationTableDistributedMode1(jti) ;
		for(StringBuffer sb :sbs)
		{
			rets.add(sb.toString());
		}
		
		//原来的主键需要索引,并且不唯一
		StringBuffer sb = constructIndexTableNoUnique(jti,jti.getPkColumnInfo()) ;
		rets.add(sb.toString());
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(!tmpjci.hasIdx())
				continue ;
			
			sb = constructIndexTable(jti,tmpjci) ;
			rets.add(sb.toString());
		}
		
//		for(JavaForeignKeyInfo tmpfki:jti.getForeignKeyInfos())
//		{
//			StringBuffer sb = constructForeignKeyTable(jti,tmpfki);
//			rets.add(sb.toString());
//		}
		
		return rets ;
	}
	
	public List<String> getCreationDistributedMode2Sqls(JavaTableInfo jti)
	{
		ArrayList<String> rets = new ArrayList<String>();
		StringBuffer[] sbs = constructCreationTableDistributedMode2(jti) ;
		for(StringBuffer sb :sbs)
		{
			rets.add(sb.toString());
		}
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(!tmpjci.hasIdx())
				continue ;
			
			StringBuffer sb = constructIndexTable(jti,tmpjci) ;
			rets.add(sb.toString());
		}
		
//		for(JavaForeignKeyInfo tmpfki:jti.getForeignKeyInfos())
//		{
//			StringBuffer sb = constructForeignKeyTable(jti,tmpfki);
//			rets.add(sb.toString());
//		}
		
		return rets ;
	}
	
	/**
	 * 
	 * @param proxyserver 如果系统运行在分布式环境下的server
	 * @param jti
	 * @return
	 */
	protected StringBuffer[] constructCreationTable(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		if(pkcol!=null)
		{
			tmpsb.append(pkcol.getColumnName()).append(" ")
				.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
				.append(" primary key");
		}
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(tmpsb.charAt(tmpsb.length()-1)!=',')
				tmpsb.append(',');
			
			tmpsb.append(tmpjci.getColumnName()).append(" ")
				.append(getSqlType(tmpjci.getValType(),tmpjci.getMaxLen()));
			
			String defvstr = tmpjci.getDefaultValStr() ;
			if(defvstr!=null)
			{
				XmlVal.XmlValType vt = tmpjci.getValType();
				if(vt==XmlVal.XmlValType.vt_string)
					tmpsb.append(" default '").append(defvstr).append("'");
				else if(!defvstr.equals(""))
					tmpsb.append(" default ").append(defvstr);
			}
		}
		
		tmpsb.append(")");
		return new StringBuffer[]{tmpsb} ;
	}
	
	
	protected StringBuffer[] constructCreationTableDistributedMode1(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		tmpsb.append("_ProxyId integer,");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		if(pkcol!=null)
		{
			tmpsb.append(pkcol.getColumnName()).append(" ")
				.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()));
		}
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(tmpsb.charAt(tmpsb.length()-1)!=',')
				tmpsb.append(',');
			
			tmpsb.append(tmpjci.getColumnName()).append(" ")
				.append(getSqlType(tmpjci.getValType(),tmpjci.getMaxLen()));
			
			String defvstr = tmpjci.getDefaultValStr() ;
			if(defvstr!=null)
			{
				XmlVal.XmlValType vt = tmpjci.getValType();
				if(vt==XmlVal.XmlValType.vt_string)
					tmpsb.append(" default '").append(defvstr).append("'");
				else if(!defvstr.equals(""))
					tmpsb.append(" default ").append(defvstr);
			}
		}
		
		tmpsb.append(")");
		return new StringBuffer[]{tmpsb} ;
	}
	
	
	protected StringBuffer[] constructCreationTableDistributedMode2(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		if(pkcol==null)
		{
			throw new RuntimeException("distributed mode2 must has pk column!") ;
		}
		
		tmpsb.append(pkcol.getColumnName()).append(" ")
			.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
			.append(" primary key");
		
		for(JavaColumnInfo tmpjci:jti.getNorColumnInfos())
		{
			if(tmpsb.charAt(tmpsb.length()-1)!=',')
				tmpsb.append(',');
			
			tmpsb.append(tmpjci.getColumnName()).append(" ")
				.append(getSqlType(tmpjci.getValType(),tmpjci.getMaxLen()));
			
			String defvstr = tmpjci.getDefaultValStr() ;
			if(defvstr!=null)
			{
				XmlVal.XmlValType vt = tmpjci.getValType();
				if(vt==XmlVal.XmlValType.vt_string)
					tmpsb.append(" default '").append(defvstr).append("'");
				else if(!defvstr.equals(""))
					tmpsb.append(" default ").append(defvstr);
			}
		}
		
		//增加时间戳列
		tmpsb.append(",_ServerUpdateDT bigint") ;
		
		tmpsb.append(")");
		return new StringBuffer[]{tmpsb} ;
	}
	
	/**
	 * 构造在一个已经存在的表中增加列的sql一句
	 * @param jti
	 * @param jci
	 * @return
	 */
	public abstract StringBuffer constructAddColumnToTable(JavaTableInfo jti,JavaColumnInfo jci);
	
	public StringBuffer constructIndexTable(JavaTableInfo jti,JavaColumnInfo jci)
	{
		//create index idx_security_users_email on security_users (Email)
		StringBuffer tmpsb = new StringBuffer();
		if(jci.isUnique())
			tmpsb.append("create unique index ");
		else
			tmpsb.append("create index ");
		
		tmpsb.append(calIndexName(jti, jci));
		tmpsb.append(" on ").append(jti.getTableName())
			.append(" (").append(jci.getColumnName()).append(")");
		
		return tmpsb ;
	}
	
	private StringBuffer constructIndexTableNoUnique(JavaTableInfo jti,JavaColumnInfo jci)
	{
		//create index idx_security_users_email on security_users (Email)
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create index ");
		
		tmpsb.append(calIndexName(jti, jci));
		tmpsb.append(" on ").append(jti.getTableName())
			.append(" (").append(jci.getColumnName()).append(")");
		
		return tmpsb ;
	}
	
	protected String calIndexName(JavaTableInfo jti,JavaColumnInfo jci)
	{
		return "idx_"+jti.getTableName()+"_"+jci.getColumnName();
	}
	
	protected String calIndexName(String tn,String coln)
	{
		return "idx_"+tn+"_"+coln;
	}
	
	/**
	 * 构造删除索引的sql指令。缺省格式是drop index tablen.idxn
	 * @param jti
	 * @param jci
	 * @return
	 */
	public abstract StringBuffer constructDropIndex(JavaTableInfo jti,JavaColumnInfo jci);
	
	public StringBuffer constructForeignKeyTable(JavaTableInfo jti,JavaForeignKeyInfo jfki)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("ALTER TABLE ").append(jti.getTableName())
			.append(" ADD CONSTRAINT FK_").append(jti.getTableName()).append('_').append(jfki.getRefTableName())
			.append(" FOREIGN KEY ")
			.append("(").append(jfki.getLocalColName()).append(") REFERENCES ")
			.append(jfki.getRefTableName()).append(" (").append(jfki.getRefColName()).append(") ON DELETE CASCADE");
		
		return tmpsb ;
	}
	
	public StringBuffer constructDropForeignKeyTable(JavaTableInfo jti,JavaForeignKeyInfo jfki)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("ALTER TABLE ").append(jti.getTableName())
			.append(" DROP CONSTRAINT FK_").append(jti.getTableName()).append('_').append(jfki.getRefTableName());
		return tmpsb ;
	}
	
	/**
	 * 获得根据pkid查找数据库的sql语句
	 * @param jti
	 * @return
	 */
	public StringBuilder getSelectByPkIdSql(JavaTableInfo jti)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(jti.getPkColumnInfo().getColumnName());
		for(JavaColumnInfo jci:jti.getNorColumnInfos())
		{
			sb.append(',').append(jci.getColumnName());
		}
		sb.append(" from ").append(jti.getTableName())
			.append(" where ").append(jti.getPkColumnInfo().getColumnName()).append("=?");
		return sb ;
	}
	
	
	public StringBuilder getSelectColsByPkIdSql(JavaTableInfo jti,String[] colnames)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(colnames[0]);
		for(int i = 1 ; i < colnames.length ; i ++)
		{
			sb.append(',').append(colnames[i]);
		}
		sb.append(" from ").append(jti.getTableName())
			.append(" where ").append(jti.getPkColumnInfo().getColumnName()).append("=?");
		return sb ;
	}
	
	public StringBuilder getMaxPkIdSql(JavaTableInfo jti)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select max(").append(jti.getPkColumnInfo().getColumnName());
		sb.append(") from ").append(jti.getTableName());
		return sb ;
	}
	
	
	public StringBuilder getSelectByUniqueColSql(JavaTableInfo jti,String unique_col)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(jti.getPkColumnInfo().getColumnName());
		for(JavaColumnInfo jci:jti.getNorColumnInfos())
		{
			sb.append(',').append(jci.getColumnName());
		}
		sb.append(" from ").append(jti.getTableName())
			.append(" where ").append(unique_col).append("=?");
		return sb ;
	}
	
	public final StringBuilder getUpdateByPkIdSql(JavaTableInfo jti)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(jti.getTableName()).append(" set ");
		JavaColumnInfo[] jcis = jti.getUpdateNorColumnInfos();
		sb.append(jcis[0].getColumnName()).append("=?") ;
		for(int i = 1 ; i < jcis.length ; i ++)
		{
			sb.append(',').append(jcis[i].getColumnName()).append("=?");
		}
		sb.append(" where ").append(jti.getPkColumnInfo().getColumnName()).append("=?");
		return sb ;
	}
	
	public StringBuilder getInsertSqlWithInputId(JavaTableInfo jti)
	{
		StringBuilder tmpsb = new StringBuilder();
		JavaColumnInfo[] jcis = jti.getNorColumnInfos() ;
		tmpsb.append("insert into ").append(jti.getTableName()).append("(");
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		if(pkcol!=null)
			tmpsb.append(pkcol.getColumnName()).append(',').append(jcis[0].getColumnName());
		else
			tmpsb.append(jcis[0].getColumnName());
		
		for(int i = 1 ; i < jcis.length ; i ++)
		{
			tmpsb.append(",").append(jcis[i].getColumnName());
		}
		if(pkcol!=null)
			tmpsb.append(") values (?,?");
		else
			tmpsb.append(") values (?");
		
		for(int i = 1 ; i < jcis.length ; i ++)
		{
			tmpsb.append(",?");
		}
		tmpsb.append(")");
		return tmpsb ;
	}
	
	public abstract StringBuffer[] getInsertSqlWithNewIdReturn(JavaTableInfo jti);
	
	public abstract SqlAndInputVals getSelectSqlWithPage(JavaTableInfo jti,
			boolean distinct,String[] selectcols,
			String wherestr,Object[] input_vals,//where 条件和输入的参数
			String groupby,String orderbystr,
			int pageidx,int pagesize);
//	public String SQL_select(String tablename,)
//	{
//		StringBuffer sqlsb = new StringBuffer();
//		sqlsb.append("select ");
//		if (bHasKeyCol)
//		{
//			sqlsb.append(mon.getDBKeyColumn());
//			String[] norcols = mon.getDBNorColumns();
//			for (int i = 0; i < norcols.length; i++)
//			{
//				sqlsb.append(",").append(norcols[i]);
//			}
//		}
//		else
//		{
//			String[] norcols = mon.getDBNorColumns();
//			sqlsb.append(norcols[0]);
//			for (int i = 1; i < norcols.length; i++)
//			{
//				sqlsb.append(",").append(norcols[i]);
//			}
//		}
//
//		sqlsb.append(" from ").append(mon.getDBTableName());
//		if (condcols != null && condcols.length > 0)
//		{
//			sqlsb.append(" where");
//			for (int i = 0; i < condcols.length; i++)
//			{
//				sqlsb.append(" ");
//				if (i > 0)
//				{
//					sqlsb.append("AND ");
//
//				}
//				sqlsb.append(condcols[i]).append("=?");
//			}
//		}
//
//		return sqlsb.toString();
//	}
	
	public static class SqlAndInputVals
	{
		public StringBuffer sqlSB = null ;
		public Object[] inputVals = null ;
		
		public SqlAndInputVals(StringBuffer sb,Object[] inputvs)
		{
			sqlSB = sb ;
			inputVals = inputvs ;
		}
	}
	/**
	 * 得到数据库类型
	 * @return
	 */
	public abstract DBType getDBType();
	
	/**
	 * 根据java值类型，获得本数据库对应的sql语句类型
	 * @param vt
	 * @return
	 */
	public abstract String getSqlType(XmlVal.XmlValType vt,int maxlen) ;
}
