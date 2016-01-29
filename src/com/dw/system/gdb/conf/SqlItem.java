package com.dw.system.gdb.conf;

import java.io.*;
import java.util.*;

import com.dw.system.Convert;
import com.dw.system.codedom.BoolExp;
import com.dw.system.gdb.*;

import org.w3c.dom.Element;

/**
 * ֧��һ��sql���е�������
 * 
 * ��Ȼ��Content��Step�е�sql���ᱻ�����SqlItem������֧������ʱ��Sql���װ��
 * �Ͳ���������
 * 
 * sql����еģ�������������[@xxx] [$xxx] {@xxx} {$xxx}
 * ����ʹ�� {}��ʾ�������ֵΪnull�������λᱻ����
 * @author Jason Zhu
 */
public class SqlItem extends IOperItem
{
	/**
	 * �������ʱ��
	 * @author jasonzhu
	 *
	 */
	public static class RunCost
	{
		public long runDT =-1 ;
		
		public long costMS = -1 ;
	}
	
	public static class AutoFit
	{
		String name = null ;
		ExeType exeType = null ;
		HashMap<DBType,String> dbtype2cont = new HashMap<DBType,String>() ;
		
		public AutoFit(String n)
		{
			name = n ;
			int p = name.indexOf('_');
			exeType = ExeType.valueOf(name.substring(0,p));
		}
		
		public String getName()
		{
			return name ;
		}
		
		public ExeType getExeType()
		{
			return exeType ;
		}
		
		public String getFitContent(DBType n)
		{
			return dbtype2cont.get(n) ;
		}
	}
	//װ��autofit��Ϣ
	private static HashMap<String,AutoFit> name2AutoFit = new HashMap<String,AutoFit>() ;
	
	
	
	static
	{
		//ClassLoader cl = Thread.currentThread().getContextClassLoader();//.getResourceAsStream(name)
		//Class c = new Object().getClass();
		Class c = new SqlItem().getClass();
		InputStream ips = null ;
		for(DBType dbt:DBType.values())
		{
			try
			{
				ips = c.getResourceAsStream("/com/dw/system/gdb/conf/autofit/"+dbt.toString()+".prop");
				if(ips==null)
					continue ;
				
				System.out.println("load >>"+"autofit/"+dbt.toString()+".prop");
				
				
				Properties p = new Properties();
				p.load(ips);
				for(Enumeration en = p.propertyNames();en.hasMoreElements();)
				{
					String pn = (String)en.nextElement() ;
					String pv = p.getProperty(pn);
					
					
					AutoFit af = name2AutoFit.get(pn);
					if(af==null)
					{
						af = new AutoFit(pn);
						name2AutoFit.put(pn,af);
					}
					
					af.dbtype2cont.put(dbt,pv);
				}
			}
			catch(IOException ioe)
			{
				
			}
			finally
			{
				if(ips!=null)
				{
					try
					{
					ips.close();
					}
					catch(IOException ioe)
					{}
					ips = null ;
				}
			}
		}
	}
	
	private static final int ST_NORMAL = 0 ;
	private static final int ST_IN_RT_PARAM = 1 ;
	private static final int ST_IN_CAT_PARAM = 2 ;
	
	public static SqlItem parseSqlItem(Gdb g,Func fi,Element sqlele) throws Exception
	{
		SqlItem si = new SqlItem();
		
		si.gdb = g ;
		si.func = fi ;
		
		String exet = sqlele.getAttribute(Gdb.ATTR_EXE_TYPE);
		if(exet!=null&&!exet.equals(""))
			si.exeType = ExeType.valueOf(exet);
		
		si.resultTableName = sqlele.getAttribute(Gdb.ATTR_RES_TABLE_NAME) ;
		
		String callt = sqlele.getAttribute(Gdb.ATTR_TYPE);
		if(callt!=null&&!callt.equals(""))
			si.callType = CallType.valueOf(callt);
		
		si.parseEle(sqlele);
		
		String str_cont = XDataHelper.getElementFirstTxt(sqlele);
		if(str_cont==null||(str_cont=str_cont.trim()).equals(""))
			return null ;
		
		if(Convert.isNullOrTrimEmpty(exet))
		{//guess exe type
			StringTokenizer st = new StringTokenizer(str_cont," \t\r\n") ;
			String firstword = st.nextToken() ;
			if("select".equalsIgnoreCase(firstword))
			{
				si.exeType = ExeType.select ;
			}
			else if("update".equalsIgnoreCase(firstword))
			{
				si.exeType = ExeType.update ;
			}
			else if("delete".equalsIgnoreCase(firstword))
			{
				si.exeType = ExeType.delete ;
			}
		}
		
		if(si.callType==CallType.auto_fit)
		{
			si.autoFit = name2AutoFit.get(str_cont);
			if(si.autoFit==null)
				throw new RuntimeException("cannot get auto fit with name="+str_cont);
			return si ;
		}
		//����#var ��������Ӧ��sql����ַ�����
		for(Map.Entry<String, String> vn2vv:si.gdb.getAllVarName2Value().entrySet())
		{
			str_cont = str_cont.replaceAll(vn2vv.getKey(), vn2vv.getValue());
		}
		
		//��sql�����зֽ⣬�����ɶ�Ӧ��Ƭ�κͲ�����Ϣ
		//����еĲ�����Ϣ����[@xxx]-����ʱת��Ϊ? [$xxx]-����ʱ���Զ�ƴװ
		int p = 0 ;
		int st = 0 ;
		int len = str_cont.length();
		char c;
		StringBuilder tmpsb = new StringBuilder();
		//���null�����ǵ�ǰ���ں��Զ�֮��
		SqlSegIgnore curSegIngore = null ;
		while(p<len)
		{
			c = str_cont.charAt(p);
			switch(st)
			{
			case ST_NORMAL: //-normal state
				if(c=='['&&p+1<len)
				{
					char tmpc = str_cont.charAt(p+1) ;
					if(tmpc=='@')
					{
						if(tmpsb.length()>0)
						{
							SqlSeg ss = si.new SqlSeg(tmpsb.toString());
							if(curSegIngore!=null)
								curSegIngore.segs.add(ss);
							else
								si.sqlSegs.add(ss);
							tmpsb.delete(0, tmpsb.length());
						}
						
						p +=2 ;
						st = ST_IN_RT_PARAM;
						break;
					}
					else if(tmpc=='$')
					{
						if(tmpsb.length()>0)
						{
							SqlSeg ss = si.new SqlSeg(tmpsb.toString());
							if(curSegIngore!=null)
								curSegIngore.segs.add(ss);
							else
								si.sqlSegs.add(ss);
							tmpsb.delete(0, tmpsb.length());
						}
						
						p +=2 ;
						st = ST_IN_CAT_PARAM;
						break;
					}
				}
				
				if(c=='{')
				{
					if(curSegIngore!=null)
						throw new GdbException("ignore segment cannot contain segment!");
					
					SqlSeg ss = si.new SqlSeg(tmpsb.toString());
					si.sqlSegs.add(ss);
					tmpsb.delete(0, tmpsb.length());
					
					curSegIngore = si.new SqlSegIgnore();
					p++;
					break;
				}
				
				if(c=='}'&&curSegIngore!=null)
				{
					if(tmpsb.length()>0)
					{
						SqlSeg ss = si.new SqlSeg(tmpsb.toString());
						curSegIngore.segs.add(ss);
						tmpsb.delete(0, tmpsb.length());
					}
					si.sqlSegs.add(curSegIngore);
					curSegIngore = null ;
					p++ ;
					break;
				}
				
				
				tmpsb.append(c);
				p ++ ;
				break;
			case ST_IN_RT_PARAM: //-����@������״̬
				if(c==']')
				{//����
					if(tmpsb.length()<=0)
						throw new RuntimeException("illegal runtime param [@] ,no name found");
					
					SqlSeg ss = si.new SqlSegRtParam("@"+tmpsb.toString().trim());
					if(curSegIngore!=null)
						curSegIngore.segs.add(ss);
					else
						si.sqlSegs.add(ss);
					tmpsb.delete(0, tmpsb.length());
					p ++ ;
					st = ST_NORMAL ;
					break;
				}
				
				tmpsb.append(c);
				p ++ ;
				break;
			case ST_IN_CAT_PARAM: //-����$������״̬
				if(c==']')
				{//����
					if(tmpsb.length()<=0)
						throw new RuntimeException("illegal runtime param [$] ,no name found");
					
					SqlSeg ss = si.new SqlSegCatParam("$"+tmpsb.toString().trim());
					if(curSegIngore!=null)
						curSegIngore.segs.add(ss);
					else
						si.sqlSegs.add(ss);
					tmpsb.delete(0, tmpsb.length());
					p ++ ;
					st = ST_NORMAL ;
					break;
				}
				
				tmpsb.append(c);
				p ++ ;
				break;
			}
		}//end of while
		
		if(tmpsb.length()>0)
		{//���һ��
			SqlSeg ss = si.new SqlSeg(tmpsb.toString());
			si.sqlSegs.add(ss);
		}
		
		return si ;
	}
	
	Gdb gdb = null ;
	Func func = null ;
	ExeType exeType = ExeType.nonquery;
	CallType callType = CallType.sql;
	
	
	
	private transient AutoFit autoFit = null ;
	private String resultTableName = null ;
	private ArrayList<SqlSeg> sqlSegs = new ArrayList<SqlSeg>();
	
	/**
	 * �����������ʱ��������
	 */
	int RUNCOST_MAX = 10 ; 
	
	transient LinkedList<RunCost> runCosts = new LinkedList<RunCost>() ;
	
	private SqlItem()
	{}
	
	
	public synchronized void setRunCostStEt(long st,long et)
	{
		RunCost rc = new RunCost() ;
		rc.runDT = st ;
		rc.costMS = et-st ;
		runCosts.addLast(rc) ;
		
		if(runCosts.size()>RUNCOST_MAX)
		{
			runCosts.removeFirst() ;
		}
	}
	
	public List<RunCost> getRunCostList()
	{
		return runCosts ;
	}
	
	public ExeType getExeType()
	{
		return exeType ;
	}
	
	
	/**
	 * ��ñ�sql��ѯ������е�DataTable����
	 * @return
	 */
	public String getResultTableName()
	{
		return resultTableName ;
	}
	
	@Override
	public boolean isChangedData()
	{
		if(exeType==ExeType.select||exeType==ExeType.dataset||exeType==ExeType.scalar)
			return false;
		return true ;
	}
	
	public CallType getCallType()
	{
		return callType ;
	}
	
//	public List<ParamSetter> getParamSetters()
//	{
//		return paramSetters ;
//	}
//	
//	public BoolExp getIfBoolExp()
//	{
//		return ifBoolExp ;
//	}
	
	public ArrayList<SqlSeg> getSqlSegs()
	{
		return sqlSegs;
	}
	
	public AutoFit getAutoFit()
	{
		return autoFit ;
	}
	
	private transient RuntimeItem _fixRT = null ;
	/**
	 * ��������Ĳ������jdbc����ʱ��֧������
	 * 
	 * @param parms
	 * @return
	 */
	public RuntimeItem getRuntimeItem(DBType dbt,Hashtable parms)
	{
		if(_fixRT!=null)//�̶���sql���
			return _fixRT ;
		
		RuntimeItem ri = new RuntimeItem();
		if(this.callType==CallType.auto_fit)
		{
			ri.jdbcSql = autoFit.getFitContent(dbt);
			if(ri.jdbcSql==null)
				throw new RuntimeException("cannot get auto fit content with name="+autoFit.name+" on db type="+dbt);
			ri.exeType = autoFit.getExeType() ;
			_fixRT = ri ;
			return ri ;
		}
		boolean bfix = true ;
		
		if(func!=null)
			ri.exeType = this.exeType;
		
		StringBuilder sb = new StringBuilder();
		ArrayList<InParam> rtpns = new ArrayList<InParam>() ;
		for(SqlSeg ss:sqlSegs)
		{
			if(ss instanceof SqlSegCatParam || ss instanceof SqlSegIgnore)
			{
				bfix = false;
			}
			
			RuntimeItem tmpri = ss.getRuntimeItem(parms);
			if(tmpri==null)
				continue ;
			
			if(tmpri.jdbcSql!=null)
				sb.append(tmpri.jdbcSql);
			
			if(tmpri.rtParams!=null&&tmpri.rtParams.size()>0)
				rtpns.addAll(tmpri.rtParams);
		}
		
		ri.jdbcSql = sb.toString() ;
		ri.rtParams = rtpns ;
		
		if(bfix)
			_fixRT = ri ;
		
		return ri ;
	}
	
	
	/**
	 * Ϊ����ʱ�ṩ��jdbc������Ϣ
	 * ��sql��䣬�Ͱ�װ˳����Ҫ����Ĳ�������@xxx
	 * @author Jason Zhu
	 *
	 */
	public static class RuntimeItem
	{
		//CallType callType = CallType.sql;
		ExeType exeType = null;
		String jdbcSql = null ;
		ArrayList<InParam> rtParams = null;
		
		RuntimeItem()
		{
			
		}
		
		public ExeType getExeType()
		{
			return exeType ;
		}
		
		public String getJdbcSql()
		{
			return jdbcSql ;
		}
		
		public ArrayList<InParam> getRtParams()
		{
			return rtParams ;
		}
	}
	/**
	 * sql����Ƭ�Σ����п������ַ��������������ƴװ������
	 */
	public class SqlSeg
	{
		String sorTxt = null ;
		
		public SqlSeg(String str)
		{
			sorTxt = str ;
		}

		public String getSorTxt()
		{
			return sorTxt ;
		}
		
		public String getParamName()
		{
			return null ;
		}
		
		public RuntimeItem getRuntimeItem(Hashtable parms)
		{
			RuntimeItem ri = new RuntimeItem();
			ri.jdbcSql = sorTxt ;
			return ri;
		}
	}
	
	/**
	 * ���磺[@xxx]�Ĳ���
	 * @author Jason Zhu
	 *
	 */
	class SqlSegRtParam extends SqlSeg
	{
		String paramName = null ;
		
		public SqlSegRtParam(String str)
		{
			super(str);
			
			if(str.startsWith("["))
				str = str.substring(1);
			if(str.endsWith("]"))
				str = str.substring(0,str.length()-1);
			
			if(!str.startsWith("@"))
				throw new IllegalArgumentException("Runtime In Param Name must like @xxx");
			
			paramName = str ;
		}
		
		public String getParamName()
		{
			return paramName ;
		}
		
		public RuntimeItem getRuntimeItem(Hashtable parms)
		{
			RuntimeItem ri = new RuntimeItem();
			ri.jdbcSql = "?" ;
			ri.rtParams = new ArrayList<InParam>(0);
			InParam ip = func.getInParam(paramName);
			if(ip==null)
				throw new RuntimeException("cannot find InParam ["+paramName+"] definiation in func["+func.getUniqueKey()+"]");
			
			ri.rtParams.add(ip);
			return ri;
		}
	}
	
	/**
	 * ���磺[$xxx]�Ĳ���
	 * @author Jason Zhu
	 *
	 */
	class SqlSegCatParam extends SqlSeg
	{
		String paramName = null ;
		
		public SqlSegCatParam(String str)
		{
			super(str);
			
			if(str.startsWith("["))
				str = str.substring(1);
			if(str.endsWith("]"))
				str = str.substring(0,str.length()-1);
			
			if(!str.startsWith("$"))
				throw new IllegalArgumentException("Cat In Param Name must like $xxx");
			
			paramName = str ;
		}
		
		public String getParamName()
		{
			return paramName ;
		}
		
		public RuntimeItem getRuntimeItem(Hashtable parms)
		{
			RuntimeItem ri = new RuntimeItem();
			
			Object pv = parms.get(paramName);
			if(pv==null)
			{
				InParam ip = func.getInParam(paramName) ;
				pv = ip.getDefaultVal() ;
			}
			
			ri.jdbcSql = "" ;
			if(pv!=null)
				ri.jdbcSql = pv.toString();
			
			return ri;
		}
	}
	
	/**
	 * ����{xsdf [@xxx] xdfgg [$yyy]}����䣬�������ı���ֻҪ��һ��Ϊnull
	 * ���������ݶζ������
	 * @author Jason Zhu
	 */
	class SqlSegIgnore extends SqlSeg
	{
		ArrayList<SqlSeg> segs = new ArrayList<SqlSeg>() ;
		
		public SqlSegIgnore()
		{
			super("");
		}
		
		public ArrayList<SqlSeg> getInnerSegs()
		{
			return segs;
		}
		
		public RuntimeItem getRuntimeItem(Hashtable parms)
		{
			StringBuilder sb = new StringBuilder();
			RuntimeItem ri = new RuntimeItem();
			for(SqlSeg s:segs)
			{
				if((s instanceof SqlSegRtParam)||(s instanceof SqlSegCatParam))
				{
					
					String pn = s.getParamName() ;
					if(parms.get(pn)==null)
						return null ;//�öα�����
					
					RuntimeItem tmpri = s.getRuntimeItem(parms);
					String js = tmpri.getJdbcSql();
					if(js!=null)
						sb.append(js);
					ArrayList<InParam> pns = tmpri.getRtParams() ;
					if(pns!=null&&pns.size()>0)
					{
						if(ri.rtParams==null)
							ri.rtParams = new ArrayList<InParam>() ;
						
						ri.rtParams.addAll(pns);
					}
				}
				else
				{
					RuntimeItem tmpri = s.getRuntimeItem(parms);
					String js = tmpri.getJdbcSql();
					if(js!=null)
						sb.append(js);
				}
			}
			
			ri.jdbcSql = sb.toString();
			
			return ri;
		} 
	}
}
