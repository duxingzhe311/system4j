package com.dw.system.gdb.datax.query;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.datax.*;
import com.dw.system.gdb.datax.query.DXQCmd.InputParam;
import com.dw.system.xmldata.*;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;

/**
 * update_sep biz.work_item set {/priority}={@/pri:int32=2} 
where xd_id={@/id:int32=3} and 'abc'={@/a="abc"}

 * @author Administrator
 *
 */
public class DXQCmdUpdateSep extends DXQCmd
{
	//ArrayList<String> condIdxPath = new ArrayList<String>();
	//ArrayList<String> condInputPath = new ArrayList<String>();
	
	String[] setCols = null ;
	InputParam[] setVals = null ;
	
	//String[][] setValsInputPath = null ;
	
	String whereStr = null ;
	InputParam[] whereInputs = null ;
	
	public DXQCmdUpdateSep(DataXManager dxm,String strcmd)
	{
		super(dxm,strcmd);
	}

	/**
	 * set {/name}='ad',{/state}=@{/input_st} where xd_id=111 and {/name}='mmm'
	 */
	@Override
	protected void parseSelf(String leftstr)
	{
		leftstr = leftstr.trim();
		if(leftstr.toLowerCase().startsWith("set"))
			leftstr = leftstr.substring(3).trim();
		
		//extractIdxAndInputPath(leftstr,condIdxPath,condInputPath) ;
		
		ArrayList<String> setcols = new ArrayList<String>() ;
		ArrayList<InputParam> setvals = new ArrayList<InputParam>() ;
		
		whereStr = findSetColsReturnLeft(setcols,setvals,leftstr).trim();
		
		if(whereStr.toLowerCase().startsWith("where "))
			whereStr = whereStr.substring(6).trim();
		
		setCols = new String[setcols.size()];
		setVals = new InputParam[setcols.size()];
		
		setcols.toArray(setCols);
		setvals.toArray(setVals);
		
		initCond();
	}
	
	private void initCond()
	{
		//只需要把whereStr处理了就行
		
		whereStr = transToDBCol(whereStr,null);
		
		ArrayList<InputParam> inputps = new ArrayList<InputParam>() ;
		whereStr = transToQuestionMark(whereStr,inputps);
		whereInputs = new InputParam[inputps.size()];
		inputps.toArray(whereInputs);
	}
	
	
	//private static final int ST_BEFORE_COLNAME = 5 ;
	//private static final int ST_BEFORE_COLNAME = 6 ;
	
	/**
	 * 找到 set后面,where之前的{/xx}={@/xx:int32=1},..的内容
	 * 并且提取出剩余内容.
	 */
	private String findSetColsReturnLeft(ArrayList<String> setcols,ArrayList<InputParam> setvals,String str)
	{
		final int ST_BEFORE_COLNAME = 0 ;
		final int ST_IN_COLNAME = 1 ;
		final int ST_AFTER_COLNAME = 2 ;
		final int ST_BEFORE_COLVAL = 3 ;
		final int ST_IN_COLVAL = 4 ;
		final int ST_IN_COLSTRVAL = 5 ;
		final int ST_AFTER_COLVAL = 6 ;
		
		int p = 0 ;
		int st = ST_BEFORE_COLNAME;
		String curCol = "" ;
		String curVal = "" ;
		boolean bend = false ;
		while(p<str.length()&&!bend)
		{
			char c = str.charAt(p);
			switch(st)
			{
			case ST_BEFORE_COLNAME:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++;
					break;
				}
				
				if(c=='{')
				{
					p ++ ;
					st = ST_IN_COLNAME;
					curCol = "" ;
					break;
				}
				
				throw new IllegalArgumentException("invalid cmd str unknow char["+c+"] before path name");
			case ST_IN_COLNAME:
				if(c=='}')
				{
					p ++;
					st = ST_AFTER_COLNAME;
					if(curCol.equals(""))
						throw new IllegalArgumentException("invalid cmd with invalid set path name");;
					setcols.add(curCol);
					curCol = "" ;
					break;
				}
				
				curCol += c ;
				p ++ ;
				break;
			case ST_AFTER_COLNAME:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++;
					break;
				}
				
				if(c=='=')
				{
					st = ST_BEFORE_COLVAL;
					p ++ ;
					break;
				}
				
				throw new IllegalArgumentException("invalid cmd str unknow char["+c+"] before path value");
			case ST_BEFORE_COLVAL:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++;
					break;
				}
				
				if(c=='{')
				{
					int[] eps = new int[1];
					StringBuffer fr = new StringBuffer();
					InputParam ip = parseInputParam(str, p, eps, fr);
					if(ip==null)
						throw new IllegalArgumentException(fr.toString()) ;
					
					setvals.add(ip);
					p  = eps[0] + 1 ;
					st = ST_AFTER_COLVAL;
					break;
				}
				
				throw new IllegalArgumentException("col value must like {@/xxx:type=default_val}");
				
//				curVal = ""+c ;
//				st = ST_IN_COLVAL;
//				p ++ ;
//				break;
//			case ST_IN_COLVAL:
//				if(WHITE_SPACE.indexOf(c)>=0)
//				{
//					setvals.add(curVal) ;
//					curVal = "";
//					st = ST_AFTER_COLVAL ;
//					p++ ;
//					break;
//				}
//				
//				if(c==',')
//				{
//					setvals.add(curVal) ;
//					curVal = "";
//					st=ST_BEFORE_COLNAME;
//					p++ ;
//					break;
//				}
//				
//				
//				if(p+1>=str.length())
//				{//有可能结束
//					setvals.add(curVal) ;
//					curVal = "";
//					p ++ ;
//					break;
//				}
//				
//				curVal += c ;
//				p ++ ;
//				break;
//			case ST_IN_COLSTRVAL:
//				if(c=='\'')
//				{
//					if(p+1<str.length())
//					{
//						if(str.charAt(p+1)=='\'')
//						{
//							curVal+="\'\'";
//							p += 2 ;
//							break;
//						}
//					}
//					
//					curVal += '\'';
//					setvals.add(curVal) ;
//					curVal = "";
//					st = ST_AFTER_COLVAL ;
//					p++ ;
//					break;
//				}
//				
//				curVal += c ;
//				p ++ ;
//				break;
			case ST_AFTER_COLVAL:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++;
					break;
				}
				
				if(c==',')
				{
					st = ST_BEFORE_COLNAME;
					p ++ ;
					break;
				}
				
				//碰到其他字符则结束
				bend = true ;
				break;
			default:
				throw new RuntimeException("unknow state");
			}
		}
		
		if(setcols.size()<=0)
			throw new IllegalArgumentException("invalid cmd with no set cols to be found");
		if(setcols.size()!=setvals.size())
			throw new IllegalArgumentException("invalid cmd with set cols and set vals is not match");
		
		if(p>=str.length())
			return "" ;
		
		return str.substring(p);
	}

	@Override
	public String getName()
	{
		return "update_sep";
	}

	@Override
	public XmlData doCmd(XmlData inputxd) throws Exception
	{
		Object[] set_col_vals = null ;
		if(setVals!=null)
		{
			set_col_vals = new Object[setVals.length];
			for(int i = 0 ;  i< setVals.length ; i ++)
			{
				set_col_vals[i] = setVals[i].getValueFromXmlData(inputxd);
			}
		}
		
		Object[] where_vals = null ;
		if(whereInputs!=null)
		{
			where_vals = new Object[whereInputs.length];
			for(int i = 0 ;  i< whereInputs.length ; i ++)
			{
				where_vals[i] = whereInputs[i].getValueFromXmlData(inputxd);
			}
		}
		
		int r = getRelatedDataXClass().updateSeparateValueByCond(setCols,set_col_vals, whereStr,where_vals);
		XmlData retxd = new XmlData();
		retxd.setParamValue("updated_num", r);
		return retxd;
	}

	static XmlDataStruct outputXD = new XmlDataStruct();
	static
	{
		outputXD.setXmlDataMember("updated_num", XmlVal.VAL_TYPE_INT32);
	}
	
	@Override
	public XmlDataStruct getOutputStruct()
	{
		return outputXD;
	}
}
