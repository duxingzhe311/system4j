package com.dw.system.gdb.datax.query;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.dw.system.gdb.datax.DataXIndex;
import com.dw.system.gdb.datax.DataXItemList;
import com.dw.system.gdb.datax.DataXManager;
import com.dw.system.gdb.datax.XmlDataList;
import com.dw.system.gdb.datax.query.DXQCmd.InputParam;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;

/**
 * listidx指令的实现
 * 
 * 该指令仅仅查找索引中的数据，可以用来做统计查询等操作
 * 
 * @author Jason Zhu
 */
public class DXQCmdListIdx extends DXQCmd
{
	int limitIdx = 0 ;
	int limitSize = Integer.MAX_VALUE ;
	
	String limitIdxPath = null ;
	String limitSizePath = null ;
	
	String colsStr = null ;
	String[] cols = null ;
	
	String whereStr = null ;
	InputParam[] whereInputs = null ;
	String groupStr = null ;
	String orderByStr = null ;
	
	ArrayList<String> condIdxPath = new ArrayList<String>();
	ArrayList<String> condInputPath = new ArrayList<String>();
	
	
	private XmlDataStruct outputStruct = null ;
	private DataXIndex relatedIdx = null ;
	
	public DXQCmdListIdx(DataXManager dxm,String strcmd)
	{
		super(dxm,strcmd);
	}
	
	/**
	 *  limit(2,5) {/name} as n where {/name} like 'aa%' orderby {/name}
	 */
	@Override
	protected void parseSelf(String leftstr)
	{
		leftstr = leftstr.trim();
		if(leftstr.startsWith("limit"))
		{//has page
			int leftc = leftstr.indexOf('(');
			if(leftc<=0)
				throw new IllegalArgumentException("invalid limit,it must has limit()");
			
			int rightc = leftstr.indexOf(')',leftc+1) ;
			if(rightc<=0)
				throw new IllegalArgumentException("invalid limit,it must has limit()");
			
			String limstr = leftstr.substring(leftc+1,rightc);
			int p = limstr.indexOf(',');
			if(p<=0)
					throw new IllegalArgumentException("invalid limit,it must like limit(2,3)");
			
			String strlimitidx = limstr.substring(0,p).trim();
			if(strlimitidx.length()>0)
			{
				if(strlimitidx.charAt(0)=='@')
					limitIdxPath = extractFirstPath(strlimitidx);
				else
					limitIdx = Integer.parseInt(strlimitidx);
			}
			
			String strlimitsize = limstr.substring(p+1).trim();
			if(strlimitsize.length()>0)
			{
				if(strlimitsize.charAt(0)=='@')
					limitSizePath = extractFirstPath(strlimitsize);
				else
					limitSize = Integer.parseInt(strlimitsize);
			}
			
			leftstr = leftstr.substring(rightc+1).trim();
		}
		
		extractIdxAndInputPath(leftstr,condIdxPath,condInputPath) ;
		
		relatedIdx = judgeUsingIdx(condIdxPath);
		if(relatedIdx==null)
			throw new IllegalArgumentException("cannot find related single value index");
		
		String tmpls = leftstr.toLowerCase();
		int pendcol = tmpls.indexOf(" where") ;
		if(pendcol<0)
			pendcol = tmpls.indexOf(" groupby");
		if(pendcol<0)
			pendcol = tmpls.indexOf(" orderby");
		//查询列信息
		colsStr = leftstr;
		if(pendcol>0)
		{
			colsStr = colsStr.substring(0,pendcol).trim();
			leftstr = leftstr.substring(pendcol).trim();
		}
		else
		{
			leftstr = "" ;
		}
		
		if(colsStr.equals(""))
			throw new IllegalArgumentException("no list idx column found!");
		//{/name} as n,{/title}
		
		//count(*),{/xxx}
		
		if(leftstr.toLowerCase().startsWith("where "))
		{
			int k = leftstr.indexOf(" groupby ");
			if(k<0)
				k = leftstr.indexOf(" orderby ") ;
			
			if(k>0)
			{
				whereStr = leftstr.substring(6,k);
				leftstr = leftstr.substring(k).trim();
			}
			else
			{
				whereStr = leftstr.substring(6);
				leftstr= "" ;
			}
		}
		
		if(leftstr.toLowerCase().startsWith("groupby "))
		{
			int k = leftstr.indexOf(" orderby ");
			
			if(k>0)
			{
				groupStr = leftstr.substring(8,k);
				leftstr = leftstr.substring(k).trim();
			}
			else
			{
				groupStr = leftstr.substring(8);
				leftstr= "" ;
			}
		}
		
		if(leftstr.toLowerCase().startsWith("orderby "))
			orderByStr = leftstr.substring(8) ;
		
		if(condInputPath.size()>0||limitIdxPath!=null||limitSizePath!=null)
		{
			inputXSD = new XmlDataStruct();
			if(limitIdxPath!=null)
			{
				inputXSD.setXmlDataMemberByPath(limitIdxPath, XmlVal.VAL_TYPE_INT32);
			}
			if(limitSizePath!=null)
			{
				inputXSD.setXmlDataMemberByPath(limitSizePath, XmlVal.VAL_TYPE_INT32);
			}
			
			for(String tmpp:condInputPath)
			{
				inputXSD.setXmlDataMemberByPath(tmpp, XmlVal.VAL_TYPE_STR);
			}
		}
		outputStruct = DataXItemList.calItemListStruct(getRelatedDataXClass().getDataStruct());
		
		initCond();
	}
	
	private void initCond()
	{
		StringBuffer tmpcols = null;
		if(colsStr!=null)
			tmpcols = new StringBuffer(colsStr) ;
		else
			tmpcols = new StringBuffer();
		
		whereStr = transToDBCol(whereStr,null);
		
		ArrayList<InputParam> inputps = new ArrayList<InputParam>() ;
		whereStr = transToQuestionMark(whereStr,inputps);
		whereInputs = new InputParam[inputps.size()];
		inputps.toArray(whereInputs);
		
		colsStr= transToDBCol(colsStr,null);
		orderByStr = transToDBCol(orderByStr,null);
		groupStr = transToDBCol(groupStr,null);
		
		StringTokenizer tmpst = new StringTokenizer(colsStr,",");
		int c = tmpst.countTokens();
		cols = new String[c];
		for(int k = 0 ; k<c ; k ++)
		{
			cols[k] = tmpst.nextToken();
		}
	}
	
	@Override
	public String getName()
	{
		return "list";
	}

	@Override
	public XmlData doCmd(XmlData inputxd) throws Exception
	{
		int tmpidx = limitIdx ;
		if(limitIdxPath!=null)
		{
			Object lidx = inputxd.getParamValueByPath(limitIdxPath);
			if(lidx!=null)
			{
				if(lidx instanceof Number)
				{
					tmpidx = ((Number)lidx).intValue();
				}
				else if(lidx instanceof String)
				{
					String tmps = (String)lidx;
					if(!tmps.equals(""))
						tmpidx = Integer.parseInt(tmps);
				}
			}
		}
		
		int tmpsize = limitSize ;
		if(limitSizePath!=null)
		{
			Object lidx = inputxd.getParamValueByPath(limitSizePath);
			if(lidx!=null)
			{
				if(lidx instanceof Number)
				{
					tmpsize = ((Number)lidx).intValue();
				}
				else if(lidx instanceof String)
				{
					String tmps = (String)lidx;
					if(!tmps.equals(""))
						tmpsize = Integer.parseInt(tmps);
				}
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
		
		if(relatedIdx!=null)
		{
			XmlDataList xdl = getRelatedDataXClass().queryIdxByIdx(relatedIdx.getName(),
					cols,
					whereStr,where_vals,
					groupStr,
					orderByStr, tmpidx, tmpsize);
			return xdl.toXmlData();
		}
		else
		{
			return null ;
		}
		
		
	}

	@Override
	public XmlDataStruct getOutputStruct()
	{
		return outputStruct;
	}

	

}
