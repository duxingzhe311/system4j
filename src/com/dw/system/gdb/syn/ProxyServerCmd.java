package com.dw.system.gdb.syn;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.*;

import com.dw.system.gdb.ConnPoolMgr;
import com.dw.system.gdb.connpool.GDBConn;
import com.dw.system.gdb.connpool.IConnPool;
import com.dw.system.xmldata.*;
import com.sun.corba.se.pept.transport.Connection;

/**
 * 每天指令的Proxy端运行，和Server端处理的支持抽象类
 * 
 * 该类在Proxy端需要如下方式使用
 * 	1，根据不同的指令构建不同的对象-构建好的对象里面应该包含被发送的信息
 * @author Jason Zhu
 *
 */
public abstract class ProxyServerCmd
{
	public static enum ProxySentRes
	{
		succ_normal,
		succ_repeat,
		error_normal,
		error_tryagain
	}
//	/**
//	 * 由于Server处理结束之后，并不能保证正常回复给Proxy
//	 * 所以，如果在处理过程中有数据库访问的情景，要求正常回复Proxy结束后才真正做
//	 * 数据提交
//	 * 
//	 * 
//	 * @author Jason Zhu
//	 */
//	public static class ServerResultCommit
//	{
//		XmlData resultXD = null ;
//		GDBConn dbConn = null ;
//		boolean oldDBCommit = true;
//		
//		public ServerResultCommit(XmlData res,GDBConn conn,boolean old_autoc)
//		{
//			resultXD = res ;
//			dbConn = conn ;
//			oldDBCommit = old_autoc ;
//		}
//		
//		public void commit() throws SQLException
//		{
//			if(dbConn==null)
//				return ;
//			
//			try
//			{
//				dbConn.commit() ;
//			}
//			finally
//			{
//				dbConn.setAutoCommit(oldDBCommit) ;
//				dbConn.freeToPool() ;
//			}
//		}
//		
//		public void rollback() throws SQLException
//		{
//			if(dbConn==null)
//				return ;
//			
//			try
//			{
//				dbConn.rollback() ;
//			}
//			finally
//			{
//				dbConn.setAutoCommit(oldDBCommit) ;
//				dbConn.freeToPool() ;
//			}
//		}
//	}
	
	static HashMap<String,ProxyServerCmd> Server_cmdName2PSC = null;
	
	static void initCmds()
	{
		if(Server_cmdName2PSC!=null)
			return ;
		
		HashMap<String,ProxyServerCmd> n2psc = new HashMap<String,ProxyServerCmd>();
		ProxyServerCmd psc = new PSCSynMode1() ;
		n2psc.put(psc.getCmdName(), psc) ;
		psc = new PSCSynMode1Init() ;
		n2psc.put(psc.getCmdName(), psc) ;
		
		psc = new PSCSynMode2Check() ;
		n2psc.put(psc.getCmdName(), psc) ;
		psc = new PSCSynMode2() ;
		n2psc.put(psc.getCmdName(), psc) ;
		
		Server_cmdName2PSC = n2psc ;
	}
	
	public ProxyServerCmd()
	{
		
	}
	
	public abstract String getCmdName();
	
	/**
	 * Proxy端发送信息前，需要调用此方法，获得对应的XmlData信息内容
	 * @return
	 */
	protected abstract XmlData Proxy_getMsgToBeSent()  throws Exception;
	
	/**
	 * Proxy端在获得服务器返回内容时需要做的后续处理
	 * 改方法并不能代表服务器端一定成功-而是通信一定成功
	 * @param resp_xd
	 */
	protected abstract ProxySentRes Proxy_onResponseSucc(XmlData resp_xd)  throws Exception;
	
	/**
	 * Proxy端链接成功后，发送数据及获取响应失败
	 * @param error
	 */
	protected abstract ProxySentRes Proxy_onResponseError(Exception error);
	
	
	/**
	 * Proxy端请求时，发生错误，如无法连接服务器等之类的问题
	 * @param error
	 */
	protected abstract ProxySentRes Proxy_onRequestError(Exception error);
	
	/**
	 * server端接收到Proxy请求后，是否需要建立数据库链接给后续处理使用
	 * 这样，可以保证响应给Proxy端成功结束后，Server才真正提交数据更新
	 * @return
	 */
	protected abstract boolean Server_needDBConn() ;
	
	/**
	 * 服务器端接收到对应的Proxy发送的数据的处理--服务端使用该方法
	 * 该方法在服务器端要注意并发访问
	 * 
	 * @param xd Proxy_getMsgToBeSent生成的内容
	 * @return 服务器端生成的返回值
	 */
	protected abstract XmlData Server_onRequestRecved(GDBConn conn,String proxyid,XmlData xd)  throws Exception ;
	
	/**
	 * 服务器端处理过程-该方法被jsp页面调用
	 * @param req
	 * @param resp
	 */
	public static final void Server_processRequest(HttpServletRequest req,HttpServletResponse resp)
		throws Exception
	{
		if(Server_cmdName2PSC==null)
			initCmds();
		
		int datalen = req.getContentLength() ;
		byte[] databuf = new byte[datalen] ;
		
		InputStream is = req.getInputStream() ;
		
		
		XmlData xd = new XmlData();
		xd.readCompactNotXmlFromStream(is) ;
		
		String cmd = xd.getParamValueStr("cmd") ;
		ProxyServerCmd psc = Server_cmdName2PSC.get(cmd) ;
		if(psc==null)
		{
			throw new Exception("no server cmd found:"+cmd) ;
		}
		
		String proxyid = xd.getParamValueStr("proxyid") ;
		System.out.println("process proxyid="+proxyid+" cmd=="+cmd) ;
		GDBConn conn = null ;
		boolean b_old_auc = true ;
		//ServerResultCommit src = null ;
		try
		{
			if(psc.Server_needDBConn())
			{
				IConnPool cp = ConnPoolMgr.getDefaultConnPool() ;
				conn = (GDBConn)cp.getConnection();
				b_old_auc = conn.getAutoCommit() ;
				conn.setAutoCommit(false);
			}
			
			XmlData rxd = psc.Server_onRequestRecved(conn,proxyid,xd.getSubDataSingle("req")) ;
			XmlData retxd = new XmlData() ;
			retxd.setParamValue("cmd", cmd) ;
			if(rxd!=null)
				retxd.setSubDataSingle("resp", rxd) ;
			
			int retlen = retxd.calCompactWriteNotXmlStreamLen() ;
			//byte[] rbs = retxd.toBytesWithUTF8() ;
			resp.setContentLength(retlen) ;
			OutputStream os = resp.getOutputStream() ;
			//os.write(rbs) ;
			retxd.writeCompactNotXmlToStream(os) ;
			os.flush();
		
			if(conn!=null)
			{//回复给客户端成功，此时才可以对Server的更新进行提交,此时可以认为Proxy
				//端已经成功处理结果
				//就算之后Proxy没有处理好结果--这种情况就需要报警，并人工维护了
				conn.commit() ;
				conn.setAutoCommit(b_old_auc) ;
				conn.freeToPool() ;
				conn = null ;
			}
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
		}
		finally
		{
			if(conn!=null)
			{
				conn.rollback() ;
				conn.setAutoCommit(b_old_auc) ;
				conn.freeToPool() ;
				conn = null ;
			}
		}
	}
	
	
	private static final void Server_processRequest0(HttpServletRequest req,HttpServletResponse resp)
	throws Exception
{
	if(Server_cmdName2PSC==null)
		initCmds();
	
	int datalen = req.getContentLength() ;
	byte[] databuf = new byte[datalen] ;
	
	InputStream is = req.getInputStream() ;
	
	int len, readlen = 0;

	while ((len = is.read(databuf, readlen, datalen - readlen)) >= 0)
	{
		readlen += len;
		if (readlen == datalen)
			break;
	}
	
	if (readlen < datalen)
	{
		throw new Exception("invalid data format,data read len[" + readlen
				+ "] is less to data len[" + datalen + "]");
	}
	
	XmlData xd = XmlData.parseFromByteArrayUTF8(databuf) ;
	
	String cmd = xd.getParamValueStr("cmd") ;
	ProxyServerCmd psc = Server_cmdName2PSC.get(cmd) ;
	if(psc==null)
	{
		throw new Exception("no server cmd found:"+cmd) ;
	}
	
	String proxyid = xd.getParamValueStr("proxyid") ;
	System.out.println("process proxyid="+proxyid+" cmd=="+cmd) ;
	GDBConn conn = null ;
	boolean b_old_auc = true ;
	//ServerResultCommit src = null ;
	try
	{
		if(psc.Server_needDBConn())
		{
			IConnPool cp = ConnPoolMgr.getDefaultConnPool() ;
			conn = (GDBConn)cp.getConnection();
			b_old_auc = conn.getAutoCommit() ;
			conn.setAutoCommit(false);
		}
		
		XmlData rxd = psc.Server_onRequestRecved(conn,proxyid,xd.getSubDataSingle("req")) ;
		XmlData retxd = new XmlData() ;
		retxd.setParamValue("cmd", cmd) ;
		if(rxd!=null)
			retxd.setSubDataSingle("resp", rxd) ;
		
		byte[] rbs = retxd.toBytesWithUTF8() ;
		resp.setContentLength(rbs.length) ;
		OutputStream os = resp.getOutputStream() ;
		os.write(rbs) ;
	
		if(conn!=null)
		{//回复给客户端成功，此时才可以对Server的更新进行提交,此时可以认为Proxy
			//端已经成功处理结果
			//就算之后Proxy没有处理好结果--这种情况就需要报警，并人工维护了
			conn.commit() ;
			conn.setAutoCommit(b_old_auc) ;
			conn.freeToPool() ;
			conn = null ;
		}
	}
	catch(Exception ee)
	{
		ee.printStackTrace();
	}
	finally
	{
		if(conn!=null)
		{
			conn.rollback() ;
			conn.setAutoCommit(b_old_auc) ;
			conn.freeToPool() ;
			conn = null ;
		}
	}
}
	
	/**
	 * 发送到服务端
	 * 
	 * @param server_url
	 */
	final ProxySentRes Proxy_sendToServer(String proxyid,String server_url)
	{
		HttpURLConnection con = null;
		OutputStream       out = null;
	    OutputStreamWriter osw = null;
	    InputStream        in  = null;
	    
	    
	    URL u = null ;
	    XmlData xdm = new XmlData() ;
		try
	    {
		    u = new URL(server_url);
		    
			
	        xdm.setParamValue("cmd", getCmdName());
	        //可能需要在此放入Proxy端信息
	        xdm.setParamValue("proxyid", proxyid);
	        
	        //指令内容
	        XmlData xd = Proxy_getMsgToBeSent();
	        xdm.setSubDataSingle("req", xd) ;
	        
	        int contlen = xdm.calCompactWriteNotXmlStreamLen() ;
	        
		    con = (HttpURLConnection)u.openConnection();
		    con.setConnectTimeout(3000) ;
		    con.setRequestMethod("POST");
		    //con.setRequestProperty("Content-type", "text/xml");
		    con.setFixedLengthStreamingMode(contlen) ;
		    con.setDoOutput(true);
		    con.setDoInput(true);
		    con.connect();
	    }
		catch(Exception ee)
		{
			//连接Server失败--此问题一般来说比较好处理
			//但需要预警
			return Proxy_onRequestError(ee) ;
		}
		
		try
		{
	        out = con.getOutputStream();
	        xdm.writeCompactNotXmlToStream(out) ;
	        out.flush();
	        
	        //get resp
	        in = con.getInputStream();
	        int rcontlen = con.getContentLength() ;
	        
			
			XmlData rxd = new XmlData() ;
			rxd.readCompactNotXmlFromStream(in) ;
			return Proxy_onResponseSucc(rxd.getSubDataSingle("resp")) ;
	    }
		catch(Exception ee)
		{
			ee.printStackTrace();
			//发送数据，及返回处理失败--
			//此问题可能是系统内部bug问题，比较严重
			return Proxy_onResponseError(ee) ;
		}
	    finally
	    {
	        try { in.close();       } catch(Exception ex) {}
	        try { osw.close();      } catch(Exception ex) {}
	        try { out.close();      } catch(Exception ex) {}
	        try { con.disconnect(); } catch(Exception ex) {}
	    }
	}
	
	
	final ProxySentRes Proxy_sendToServer0(String proxyid,String server_url)
	{
		HttpURLConnection con = null;
		OutputStream       out = null;
	    OutputStreamWriter osw = null;
	    InputStream        in  = null;
	    
	    
	    URL u = null ;
	    byte[] cont = null ;
		try
	    {
		    u = new URL(server_url);
		    
			XmlData xdm = new XmlData() ;
	        xdm.setParamValue("cmd", getCmdName());
	        //可能需要在此放入Proxy端信息
	        xdm.setParamValue("proxyid", proxyid);
	        
	        //指令内容
	        XmlData xd = Proxy_getMsgToBeSent();
	        xdm.setSubDataSingle("req", xd) ;
	        
	        cont = xdm.toBytesWithUTF8() ;
	        
		    con = (HttpURLConnection)u.openConnection();
		    con.setConnectTimeout(3000) ;
		    con.setRequestMethod("POST");
		    //con.setRequestProperty("Content-type", "text/xml");
		    con.setFixedLengthStreamingMode(cont.length) ;
		    con.setDoOutput(true);
		    con.setDoInput(true);
		    con.connect();
	    }
		catch(Exception ee)
		{
			//连接Server失败--此问题一般来说比较好处理
			//但需要预警
			return Proxy_onRequestError(ee) ;
		}
		
		try
		{
	        out = con.getOutputStream();
	        out.write(cont) ;
	        out.flush();
	        
	        //get resp
	        in = con.getInputStream();
	        int rcontlen = con.getContentLength() ;
	        byte[] rcont = new byte[rcontlen] ;
	        
	        int len, readlen = 0;

			while ((len = in.read(rcont, readlen, rcontlen - readlen)) >= 0)
			{
				readlen += len;
				if (readlen == rcontlen)
					break;
			}
			
			if (readlen < rcontlen)
			{
				throw new Exception("invalid data format,data read len[" + readlen
						+ "] is less to data len[" + rcontlen + "]");
			}
			
			XmlData rxd = XmlData.parseFromByteArrayUTF8(rcont) ;
			return Proxy_onResponseSucc(rxd.getSubDataSingle("resp")) ;
	    }
		catch(Exception ee)
		{
			ee.printStackTrace();
			//发送数据，及返回处理失败--
			//此问题可能是系统内部bug问题，比较严重
			return Proxy_onResponseError(ee) ;
		}
	    finally
	    {
	        try { in.close();       } catch(Exception ex) {}
	        try { osw.close();      } catch(Exception ex) {}
	        try { out.close();      } catch(Exception ex) {}
	        try { con.disconnect(); } catch(Exception ex) {}
	    }
	}
}
