package com.dw.system.gdb.datax.query;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.datax.*;
import com.dw.system.cache.Cacher;
import com.dw.system.xmldata.*;

/**
 * DataX��ѯ
 *  Ŀ��:1,ͨ������õ������ʽ,��DataX�е����ݽ��и��ֲ���.�������е��������ͨ���ı���Ϣ����
 *      2,��ѯʹ�õ����������������������XmlData
 * 
 * DataX��ѯ˼·
 *  �ؼ���:��ѯ����������ͨ��������������Ϣ����,Ҳ����˵,���һ��·��û�б����嵽һ��������,���޷�
 *    ��Ϊ��ѯ����.
 *    
 * 1,ֻ��ѯ��������
 * 	������ҳ
 * 	listidx base1.class1 {/name} as n,{/title} as t
 * listidx base1.class1 {/name} as n,{/title} as t where {/name} like 'aa%'
 * listidx base1.class1 {/name} as n,{/title} as t where {/name} like 'aa%' groupby {/name} orderby {/name}
 *  ����ҳ,��2ҳ,ÿһҳ5����¼
 * 	listidx base1.class1 limit(2,5) {/name},{/title} where {/name} like 'aa%'
 * 
 * 	
 * 2,���ݶ���õ�������������XmlData����,����XmlData�б�,��ṹ��class1�������ͬ
 *  list base1.class1 where {/name} like 'aa%'
 *  ��2ҳ,ÿһҳ5������
 *  list base1.class1 limit(2,5) where {/name} like 'aa%'
 *  
 *  list base1.class1 limit(0,0) where {/name} like 'aa%'
 *  
 *  list base1.class1 limit({@/pageidx:int32},{@/pagesize}) where {/name} like '%{@/input_n}'
 *  
 *  //���Ҫͨ��class�е����ݱ���,��ֻ��id,����û�б�Ҫ������
 *  list base1.class1(101,102,103)
 *  
 * *,��������֧��,ǰ��һ�β�ѯ����Ĳ���������һ��XmlData��,���ڲ�����·��ǰ����@
 *  list base1.class1 limit(2,5) where {/name} like '%@{/input_n}' order by {/date} desc
 *  
 *  �������,��ҳ��֧��
 * 
 * 3,��������
 *  add base1.class1 with <?xml ?......  �����µ�id(�̶���XmlDataStruct)
 *  add base1.class1 with @{/}
 *  add base1.class1 with @{/product[0]}
 * 4,��������
 *  update base1.class1(101) with <?xml ?......
 *  update base1.class1 where {/name} like '%{@/input_n}' desc with <?xml ?...... 
 *  
 *   �ı�����
 *  change base1.class1(101) 
 * 
 * 5,ɾ������
 *  del base1.class1(101,102,103)
 *  
 *  ע:select {/name},{/title}  �����������ɽ��XmlDataStruct,��������ʹ���ݿ�ķ����ܹ�֧��
 *  	ͳһ�����ݽ����ӿ�
 *  
 * @author Jason Zhu
 */
public class DataXQuery
{
	public static final String PN_UPDATED_NUM = "updated_num" ;
	
	DataXManager dataxMgr = null ;
	
	Cacher cachedStr2Cmd  = Cacher.getCacher("com.dw.system.gdb.datax.query.DataXQuery");
	
	public DataXQuery(DataXManager dxm)
	{
		dataxMgr = dxm ;
		cachedStr2Cmd.setMaxBufferLength(5000);
	}
	
	/**
	 * ��һ���ַ���ָ�������������
	 * �÷�������֧�ֿͻ��˳����ȡָ��ĸ�����Ϣ
	 * @param s
	 * @return
	 */
	public DXQCmdDummy parseQueryStrToDummy(String s)
	{
		DXQCmd dxqc = parseQueryStr(s);
		if(dxqc==null)
			return null ;
		
		return new DXQCmdDummy(dxqc);
	}
	
	DXQCmd parseQueryStr(String s)
	{
		if(s==null)
			return null ;
		
		int[] eps = new int[1];
		StringBuffer fr = new StringBuffer();
		String[] cmdbc = DXQCmd.parseCmdAndDataXClass(s,0,eps,fr);
		if(cmdbc==null)
			throw new IllegalArgumentException(fr.toString());
		
		String leftstr = s.substring(eps[0]+1);
		
		DXQCmd cmd = null ;
		String tmps = cmdbc[0].toLowerCase();
		if(tmps.equals("list"))
		{
			cmd= new DXQCmdList(dataxMgr,s);
		}
		else if(tmps.equals("list_sep"))
		{
			cmd= new DXQCmdListSep(dataxMgr,s);
		}
		else if(tmps.equals("update_sep"))
		{
			cmd= new DXQCmdUpdateSep(dataxMgr,s);
		}
		else if(tmps.equals("listidx"))
		{
			cmd= new DXQCmdListIdx(dataxMgr,s);
		}
		else if(tmps.equals("add"))
		{
			cmd= new DXQCmdAdd(dataxMgr,s);
		}
		else if(tmps.equals("update"))
		{
			cmd= new DXQCmdUpdate(dataxMgr,s);
		}
		else if(tmps.equals("del"))
		{
			cmd= new DXQCmdDel(dataxMgr,s);
		}
		
		if(cmd==null)
			return null ;
		
		if(leftstr!=null)
			leftstr = leftstr.trim();
		
		cmd.init(cmdbc[1],cmdbc[2],leftstr);
		return cmd ;
	}
	
	public XmlData runCmd(String querystr,XmlData inputxd)
		throws Exception
	{
		DXQCmd cmd = parseQueryStr(querystr) ;
		if(cmd==null)
			throw new IllegalArgumentException("cannot parse query str to cmd");
		
//		StringBuffer fr = new StringBuffer();
//		if(!cmd.getInputStruct().checkMatchStruct(inputxd, fr))
//			throw new IllegalArgumentException("input xmldata is not match cmd input String:"+fr.toString());
		
		System.out.println("query str="+querystr);
		return cmd.doCmd(inputxd);
	}
	
	
	
	public DXQCmd parseCachedCmd(String querystr)
	{
		DXQCmd cmd = (DXQCmd)cachedStr2Cmd.get(querystr);
		if(cmd==null)
		{
			cmd = parseQueryStr(querystr) ;
			if(cmd==null)
				throw new IllegalArgumentException("cannot parse query str to cmd");
			
			cachedStr2Cmd.cache(querystr, cmd);
		}
		
		return cmd ;
	}
	/**
	 * ���и÷��������Զ������Ӧ����䣬������������
	 * @param querystr
	 * @param inputxd
	 * @return
	 * @throws Exception
	 */
	public XmlData runCachedCmd(String querystr,XmlData inputxd)
		throws Exception
	{
		DXQCmd cmd = parseCachedCmd(querystr) ;
		return cmd.doCmd(inputxd);
	}
}
