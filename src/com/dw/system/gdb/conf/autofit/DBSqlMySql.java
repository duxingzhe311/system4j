package com.dw.system.gdb.conf.autofit;

import com.dw.system.gdb.conf.DBType;
import com.dw.system.xmldata.XmlVal;
import com.dw.system.xmldata.XmlVal.XmlValType;

public class DBSqlMySql extends DbSql
{

	public DBType getDBType()
	{
		return DBType.derby;
	}

	public String getSqlType(XmlValType vt, int maxlen)
	{
		if(vt==XmlValType.vt_int32)
		{
			return "integer";
		}
		else if(vt==XmlValType.vt_byte_array)
		{//blob
			return "longblob";
		}
		else if(vt==XmlValType.vt_date)
		{
			return "datetime";
		}
		else if(vt==XmlValType.vt_double)
		{
			return "double";
		}
		else if(vt==XmlValType.vt_float)
		{
			return "float";
		}
		else if(vt==XmlValType.vt_int64)
		{
			return "bigint";
		}
		else if(vt==XmlValType.vt_int16)
		{
			return "SMALLINT";
		}
		else if(vt==XmlValType.vt_byte)
		{
			return "TINYINT";
		}
		else if(vt==XmlValType.vt_string)
		{
			if(maxlen<=0)
				throw new IllegalArgumentException("max len must >0");
			
			if(maxlen==Integer.MAX_VALUE)
			{//clob
				return "LONGVARCHAR";
			}
			
			return "varchar("+maxlen+")";
		}
		else if(vt==XmlValType.vt_bool)
		{
			return "TINYINT(1)";
		}
		else// if(vt==XmlValType.vt_xml_schema)
		{
			throw new IllegalArgumentException("unsupported val type="+vt);
		}
	}
	

	@Override
	protected StringBuffer[] constructCreationTable(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		//StringBuffer auto_inc_sb = null ;
		if(pkcol!=null)
		{
			if(pkcol.isAutoVal()&& pkcol.getValType()!=XmlVal.XmlValType.vt_string)
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
					.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
					.append(" AUTO_INCREMENT primary key ");
				
//				if(pkcol.getAutoValStart()>0)
//				{
//					auto_inc_sb = new StringBuffer() ;
//					auto_inc_sb.append("ALTER TABLE ")
//						.append(jti.getTableName())
//						.append(" AUTO_INCREMENT=").append(pkcol.getAutoValStart()) ;
//				}
			}
			else
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
				.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
				.append(" primary key");
			}
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
		
		if(pkcol!=null&&pkcol.isAutoVal()&&pkcol.getAutoValStart()>0)
		{
			tmpsb.append(")").append(" TYPE=MyISAM AUTO_INCREMENT=").append(pkcol.getAutoValStart());
		}
		else
		{
			tmpsb.append(") ENGINE=InnoDB");
		}
		return new StringBuffer[]{tmpsb} ;
	}
	
	@Override
	protected StringBuffer[] constructCreationTableDistributedMode1(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		tmpsb.append("_ProxyId integer,");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		//StringBuffer auto_inc_sb = null ;
		if(pkcol!=null)
		{
			if(pkcol.isAutoVal())
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
					.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()));
				
//				if(pkcol.getAutoValStart()>0)
//				{
//					auto_inc_sb = new StringBuffer() ;
//					auto_inc_sb.append("ALTER TABLE ")
//						.append(jti.getTableName())
//						.append(" AUTO_INCREMENT=").append(pkcol.getAutoValStart()) ;
//				}
			}
			else
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
				.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()));
			}
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
		
		//if(pkcol!=null&&pkcol.isAutoVal()&&pkcol.getAutoValStart()>0)
		//{
		//	tmpsb.append(")").append(" TYPE=MyISAM AUTO_INCREMENT=").append(pkcol.getAutoValStart());
		//}
		//else
		{
			tmpsb.append(") ENGINE=InnoDB");
		}
		return new StringBuffer[]{tmpsb} ;
	}
	
	@Override
	protected StringBuffer[] constructCreationTableDistributedMode2(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("create table ").append(jti.getTableName())
			.append("(");
		
		JavaColumnInfo pkcol = jti.getPkColumnInfo() ;
		//StringBuffer auto_inc_sb = null ;
		if(pkcol!=null)
		{
			if(pkcol.isAutoVal())
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
					.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
					.append(" AUTO_INCREMENT primary key ");
				
//				if(pkcol.getAutoValStart()>0)
//				{
//					auto_inc_sb = new StringBuffer() ;
//					auto_inc_sb.append("ALTER TABLE ")
//						.append(jti.getTableName())
//						.append(" AUTO_INCREMENT=").append(pkcol.getAutoValStart()) ;
//				}
			}
			else
			{
				tmpsb.append(pkcol.getColumnName()).append(" ")
				.append(getSqlType(pkcol.getValType(),pkcol.getMaxLen()))
				.append(" primary key");
			}
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
		
//		增加时间戳列
		tmpsb.append(",_ServerUpdateDT bigint") ;
		
		//if(pkcol!=null&&pkcol.isAutoVal()&&pkcol.getAutoValStart()>0)
		//{
		//	tmpsb.append(")").append(" TYPE=MyISAM AUTO_INCREMENT=").append(pkcol.getAutoValStart());
		//}
		//else
		{
			tmpsb.append(") ENGINE=InnoDB");
		}
		return new StringBuffer[]{tmpsb} ;
	}
	
	public StringBuffer constructAddColumnToTable(JavaTableInfo jti,JavaColumnInfo jci)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("ALTER TABLE ")
			.append(jti.getTableName())
			.append(" ADD COLUMN ").append(jci.getColumnName()).append(" ")
			.append(getSqlType(jci.getValType(),jci.getMaxLen()));
		return tmpsb;
	}
	
	/**
	 * 重载构造删除索引的sql指令。缺省格式是drop index idxn if exists
	 * @param jti
	 * @param jci
	 * @return
	 */
	public StringBuffer constructDropIndex(JavaTableInfo jti,JavaColumnInfo jci)
	{
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append("DROP INDEX ")
			.append(calIndexName(jti,jci)).append(" IF EXISTS");
		return tmpsb ;
	}
	
//	private String getPkSeqName(JavaTableInfo jti)
//	{
//		JavaColumnInfo pkcol = jti.getPkColumnInfo();
//		if(pkcol==null)
//			return null ;
//		
//		return "dx_seq_"+jti.getTableName()+"_"+pkcol.getColumnName() ;
//	}
//	@Override
//	public List<String> getCreationSqls(JavaTableInfo jti)
//	{
//		List<String> rets = super.getCreationSqls(jti);
//		
//		String seqn = getPkSeqName(jti) ;
//		if(seqn==null)
//			return rets ;
//		
//		//加入sequence
//		rets.add("CREATE SEQUENCE "+seqn+" AS BIGINT START WITH 1");
//		return rets ;
//	}
	
	public StringBuffer[] getInsertSqlWithNewIdReturn(JavaTableInfo jti)
	{
		StringBuffer tmpsb = new StringBuffer();
		JavaColumnInfo[] jcis = jti.getNorColumnInfos() ;
		tmpsb.append("insert into ").append(jti.getTableName())
			.append(" (").append(jcis[0].getColumnName());
		
		for(int i = 1 ; i < jcis.length ; i ++)
		{
			tmpsb.append(",").append(jcis[i].getColumnName());
		}
		tmpsb.append(") values (?");
		for(int i = 1 ; i < jcis.length ; i ++)
		{
			tmpsb.append(",?");
		}
		tmpsb.append(")");
		
		StringBuffer sbgetid=  new StringBuffer();
		sbgetid.append("select LAST_INSERT_ID()");
		
		return new StringBuffer[]{tmpsb,sbgetid} ;
	}

	@Override
	public SqlAndInputVals getSelectSqlWithPage(JavaTableInfo jti,
			boolean distinct,String[] selectcols,
			String wherestr,Object[] input_vals,
			String groupby,String orderbystr,
			int pageidx,int pagesize)
	{
		String limstr = " limit " + pageidx * pagesize + "," + pagesize;
		StringBuffer sqlsb = new StringBuffer();
		sqlsb.append("select ");
		if(distinct)
			sqlsb.append("DISTINCT ");

		if(selectcols==null||selectcols.length<=0)
		{
			JavaColumnInfo pkcol = jti.getPkColumnInfo();
			JavaColumnInfo[] norcols = jti.getNorColumnInfos() ;
			int i = 0 ;
			if(pkcol!=null)
			{
				sqlsb.append(pkcol.getColumnName()) ;
			}
			else
			{
				sqlsb.append(norcols[0].getColumnName());
				i ++ ;
			}
			
			for(;i<norcols.length ; i ++)
			{
				sqlsb.append(',').append(norcols[i].getColumnName());
			}
		}
		else
		{
			sqlsb.append(selectcols[0]);
			for(int i = 1 ; i < selectcols.length ; i ++)
			{
				sqlsb.append(',').append(selectcols[i]);
			}
		}

		sqlsb.append(" from ").append(jti.getTableName());
		if(wherestr!=null&&!(wherestr=wherestr.trim()).equals(""))
		{
			sqlsb.append(" where ").append(wherestr);
		}

		if(groupby!=null&&!(groupby=groupby.trim()).equals(""))
		{
			sqlsb.append(" group by ").append(groupby);
		}
		
		if(orderbystr!=null&&!(orderbystr=orderbystr.trim()).equals(""))
		{
			sqlsb.append(" order by ").append(orderbystr);
		}
		
		sqlsb.append(limstr);
		return new SqlAndInputVals(sqlsb,input_vals);
	}
}