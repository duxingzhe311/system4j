package com.dw.system.gdb.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import com.dw.system.codedom.BoolExp;

public abstract class IOperItem
{
	/**
	 * ��һ��Oper����ǩ��,�������������set_param="@NewWorkItemId=(0,0)|@xx=(0,1)"������
	 * �����ڲ�������н�������
	 * ��,�ڸ�������н�����,������������������.�������������������Ҫ�Ĳ���.
	 * 
	 * ����,һ��insert��������,����Զ����ɵ�id,������ʱ�����õ�һ��������,��Ϊ��һ�����
	 * ���������
	 * 
	 * ����������������龰����Ϣ
	 * @author Jason Zhu
	 *
	 */
	public static class ParamSetter
	{
		String paramName = null ;
		int resultTableRow = -1 ;
		int resultTableCol = -1 ;
		
		public ParamSetter(String attrv)
		{
			attrv = attrv.trim();
			int p = attrv.indexOf('=');
			if(p<=0)
				throw new IllegalArgumentException("set_param must like @paramn=(0,0) but it is ="+attrv);
			
			paramName = attrv.substring(0,p);
			String tmps = attrv.substring(p+1).trim();
			StringTokenizer st = new StringTokenizer(tmps,"(,)");
			if(st.countTokens()!=2)
				throw new IllegalArgumentException("set_param must like @paramn=(0,0)");
			
			resultTableRow = Integer.parseInt(st.nextToken());
			resultTableCol = Integer.parseInt(st.nextToken());
		}
		
		public String getParamName()
		{
			return paramName ;
		}
		
		public int getResultTableRow()
		{
			return resultTableRow ;
		}
		
		public int getResultTableCol()
		{
			return resultTableCol ;
		}
	}
	
	private List<ParamSetter> paramSetters = null ;
	
	/**
	 * �������,������sqlǰ���ж��Ƿ���������,���������,�����ж�Ӧ��sql
	 */
	private BoolExp ifBoolExp = null ;
	
	protected void parseEle(Element sqlele) throws Exception
	{
		String setp = sqlele.getAttribute(Gdb.ATTR_SET_PARAM);
		if(setp!=null&&!setp.equals(""))
		{
			StringTokenizer tmpst = new StringTokenizer(setp,"|");
			
			paramSetters = new ArrayList<ParamSetter>() ;
			while(tmpst.hasMoreTokens())
			{
				paramSetters.add(new ParamSetter(tmpst.nextToken()));
			}
		}
		
		String ifstr = sqlele.getAttribute(Gdb.ATTR_IF);
		if(ifstr!=null&&!ifstr.equals(""))
		{
			ifBoolExp = new BoolExp(ifstr);
		}
	}
	
	/**
	 * �жϸò������Ƿ��޸�����,�����ݿ�Ĳ������ɾ������
	 * ����Ӱ�쵽����ʱ�Ƿ���Ҫʹ���������
	 * @return
	 */
	public abstract boolean isChangedData() ;
	
	public final List<ParamSetter> getParamSetters()
	{
		return paramSetters ;
	}
	
	public final BoolExp getIfBoolExp()
	{
		return ifBoolExp ;
	}
}
