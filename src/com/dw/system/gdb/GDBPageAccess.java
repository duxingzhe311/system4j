package com.dw.system.gdb;

import java.util.*;
import javax.servlet.http.*;
import com.dw.system.*;
import com.dw.system.xmldata.XmlData;

/**
 * 很多情况下,页面需要翻页显示内容
 * 为了方便页面的翻页支持. 可以在jsp页面中定义一个类重载该抽象类
 * 
 * 然后使用支持翻页的标签进行列表显示. 支持翻页的标签可以根据本类自动做
 * 翻页. 加快开发速度
 * 
 * @author Jason Zhu
 */
public abstract class GDBPageAccess
{
	//public static final String PN_PAGE_SIZE = "__ps" ;
	public static final String PN_PAGE_CUR = "__p_" ;
	
	HttpServletRequest request = null ;
	
	int pageSize = 20 ;
	int pageCur = 0 ;
	List listObjs = null ;
	int total = -1 ;
	
	public GDBPageAccess()
	{
		
		
	}
	
	public void setHttpRequest(int pagesize,HttpServletRequest req)
		throws Exception
	{
		request = req ;
		//从request中提取页面信息
		//pageSize = Convert.parseToInt32(req.getParameter(PN_PAGE_SIZE), 20) ;
		pageSize = pagesize ;
		if(pageSize<=0)
			pageSize = 20 ;
		pageCur = Convert.parseToInt32(req.getParameter(PN_PAGE_CUR), 0) ;
		
		DataOut dout = new DataOut() ;
		XmlData xd = Convert.getXmlDataFromRequest(req) ;
		listObjs = getPageObjList(xd,pageCur,pageSize,dout) ;
		total = dout.totalCount ;
	}
	
	public int getPageSize()
	{
		return pageSize ;
	}
	
	public int getPageCur()
	{
		return pageCur ;
	}
	
	public List getListObjs()
	{
		return listObjs ;
	}
	
	public int getListObjTotalNum()
	{
		return total ;
	}
	
	public int getPageTotalNum()
	{
		if(total<=0)
			return 0 ;
		
		return total / pageSize + (total % pageSize)>0?1:0 ;
	}
	
	/**
	 * 
	 * @param searchxd 搜索条件
	 * @param pageidx
	 * @param pagesize
	 * @param dout
	 * @return
	 * @throws Exception
	 */
	public abstract List getPageObjList(XmlData searchxd,int pageidx,int pagesize,DataOut dout)
		throws Exception;
}
