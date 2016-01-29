package com.dw.system.gdb.datax.query;

import java.util.*;

import com.dw.system.gdb.datax.*;
import com.dw.system.gdb.datax.query.DXQCmd.InputParam;
import com.dw.system.xmldata.XmlData;
import com.dw.system.xmldata.XmlDataPath;
import com.dw.system.xmldata.XmlDataStruct;
import com.dw.system.xmldata.XmlVal;

public class DXQCmdList extends DXQCmd
{
	int limitIdx = 0 ;
	int limitSize = 20 ;
	
	String limitIdxPath = null ;
	String limitSizePath = null ;
	
	
	
	String whereStr = null ;
	InputParam[] whereInputs = null ;
	String orderByStr = null ;
	
//	ArrayList<String> condIdxPath = new ArrayList<String>();
//	ArrayList<String> condInputPath = new ArrayList<String>();
	
	
	private XmlDataStruct outputStruct = null ;
	private DataXIndex relatedIdx = null ;
	
	public DXQCmdList(DataXManager dxm,String strcmd)
	{
		super(dxm,strcmd);
	}
	
	/**
	 *  limit(2,5) where {/name} like 'aa%' orderby {/name}
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
				if(strlimitidx.charAt(0)=='{')
					limitIdxPath = extractFirstPath(strlimitidx);
				else
					limitIdx = Integer.parseInt(strlimitidx);
			}
			
			String strlimitsize = limstr.substring(p+1).trim();
			if(strlimitsize.length()>0)
			{
				if(strlimitsize.charAt(0)=='{')
					limitSizePath = extractFirstPath(strlimitsize);
				else
					limitSize = Integer.parseInt(strlimitsize);
			}
			
			leftstr = leftstr.substring(rightc+1).trim();
		}
		
		//extractIdxAndInputPath(leftstr,condIdxPath,condInputPath) ;
		
		if(leftstr.toLowerCase().startsWith("where "))
		{
			int k = leftstr.indexOf(" orderby ") ;
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
		
		if(leftstr.toLowerCase().startsWith("orderby "))
			orderByStr = leftstr.substring(8) ;
		
		outputStruct = DataXItemList.calItemListStruct(getRelatedDataXClass().getDataStruct());
		
		initCond();
	}
	
	private void initCond()
	{
		whereStr = transToDBCol(whereStr,null);
		
		ArrayList<InputParam> inputps = new ArrayList<InputParam>() ;
		whereStr = transToQuestionMark(whereStr,inputps);
		whereInputs = new InputParam[inputps.size()];
		inputps.toArray(whereInputs);
		
		orderByStr = transToDBCol(orderByStr,null);
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
		if(inputxd!=null&&limitIdxPath!=null)
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
		if(inputxd!=null&&limitSizePath!=null)
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
		
		DataXItemList dxil = getRelatedDataXClass().findBySeparateStore(
				whereStr,
				where_vals,
				orderByStr, tmpidx, tmpsize);
		return dxil.toXmlData();
	}

	@Override
	public XmlDataStruct getOutputStruct()
	{
		return outputStruct;
	}

	

}
