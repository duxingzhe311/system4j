package com.dw.system.gdb.datax.query;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.datax.*;
import com.dw.system.cache.Cacher;
import com.dw.system.xmldata.*;

/**
 * DataX查询
 *  目的:1,通过定义好的命令格式,对DataX中的数据进行各种操作.其中所有的命令都可以通过文本信息描述
 *      2,查询使用的输入参数和输出结果都基于XmlData
 * 
 * DataX查询思路
 *  关键点:查询的条件必须通过建立的索引信息进行,也就是说,如果一个路径没有被定义到一个索引中,就无法
 *    作为查询条件.
 *    
 * 1,只查询定义索引
 * 	不带翻页
 * 	listidx base1.class1 {/name} as n,{/title} as t
 * listidx base1.class1 {/name} as n,{/title} as t where {/name} like 'aa%'
 * listidx base1.class1 {/name} as n,{/title} as t where {/name} like 'aa%' groupby {/name} orderby {/name}
 *  带翻页,第2页,每一页5个记录
 * 	listidx base1.class1 limit(2,5) {/name},{/title} where {/name} like 'aa%'
 * 
 * 	
 * 2,根据定义好的索引条件查找XmlData内容,返回XmlData列表,其结构和class1定义的相同
 *  list base1.class1 where {/name} like 'aa%'
 *  第2页,每一页5个内容
 *  list base1.class1 limit(2,5) where {/name} like 'aa%'
 *  
 *  list base1.class1 limit(0,0) where {/name} like 'aa%'
 *  
 *  list base1.class1 limit({@/pageidx:int32},{@/pagesize}) where {/name} like '%{@/input_n}'
 *  
 *  //如果要通过class中的数据本身,则只有id,所以没有必要加条件
 *  list base1.class1(101,102,103)
 *  
 * *,带参数的支持,前提一次查询输入的参数都放在一个XmlData中,属于参数的路径前面有@
 *  list base1.class1 limit(2,5) where {/name} like '%@{/input_n}' order by {/date} desc
 *  
 *  特殊情况,翻页的支持
 * 
 * 3,增加数据
 *  add base1.class1 with <?xml ?......  返回新的id(固定的XmlDataStruct)
 *  add base1.class1 with @{/}
 *  add base1.class1 with @{/product[0]}
 * 4,更新数据
 *  update base1.class1(101) with <?xml ?......
 *  update base1.class1 where {/name} like '%{@/input_n}' desc with <?xml ?...... 
 *  
 *   改变数据
 *  change base1.class1(101) 
 * 
 * 5,删除数据
 *  del base1.class1(101,102,103)
 *  
 *  注:select {/name},{/title}  可以用来生成结果XmlDataStruct,这样可以使数据库的访问能够支持
 *  	统一的数据交互接口
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
	 * 把一个字符串指令解析成哑命令
	 * 该方法用来支持客户端程序获取指令的各种信息
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
	 * 运行该方法，会自动保存对应的语句，而不用做解析
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
