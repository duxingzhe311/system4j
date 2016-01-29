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
 * ÿ��ָ���Proxy�����У���Server�˴����֧�ֳ�����
 * 
 * ������Proxy����Ҫ���·�ʽʹ��
 * 	1�����ݲ�ͬ��ָ�����ͬ�Ķ���-�����õĶ�������Ӧ�ð��������͵���Ϣ
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
//	 * ����Server�������֮�󣬲����ܱ�֤�����ظ���Proxy
//	 * ���ԣ�����ڴ�������������ݿ���ʵ��龰��Ҫ�������ظ�Proxy�������������
//	 * �����ύ
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
	 * Proxy�˷�����Ϣǰ����Ҫ���ô˷�������ö�Ӧ��XmlData��Ϣ����
	 * @return
	 */
	protected abstract XmlData Proxy_getMsgToBeSent()  throws Exception;
	
	/**
	 * Proxy���ڻ�÷�������������ʱ��Ҫ���ĺ�������
	 * �ķ��������ܴ����������һ���ɹ�-����ͨ��һ���ɹ�
	 * @param resp_xd
	 */
	protected abstract ProxySentRes Proxy_onResponseSucc(XmlData resp_xd)  throws Exception;
	
	/**
	 * Proxy�����ӳɹ��󣬷������ݼ���ȡ��Ӧʧ��
	 * @param error
	 */
	protected abstract ProxySentRes Proxy_onResponseError(Exception error);
	
	
	/**
	 * Proxy������ʱ�������������޷����ӷ�������֮�������
	 * @param error
	 */
	protected abstract ProxySentRes Proxy_onRequestError(Exception error);
	
	/**
	 * server�˽��յ�Proxy������Ƿ���Ҫ�������ݿ����Ӹ���������ʹ��
	 * ���������Ա�֤��Ӧ��Proxy�˳ɹ�������Server�������ύ���ݸ���
	 * @return
	 */
	protected abstract boolean Server_needDBConn() ;
	
	/**
	 * �������˽��յ���Ӧ��Proxy���͵����ݵĴ���--�����ʹ�ø÷���
	 * �÷����ڷ�������Ҫע�Ⲣ������
	 * 
	 * @param xd Proxy_getMsgToBeSent���ɵ�����
	 * @return �����������ɵķ���ֵ
	 */
	protected abstract XmlData Server_onRequestRecved(GDBConn conn,String proxyid,XmlData xd)  throws Exception ;
	
	/**
	 * �������˴������-�÷�����jspҳ�����
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
			{//�ظ����ͻ��˳ɹ�����ʱ�ſ��Զ�Server�ĸ��½����ύ,��ʱ������ΪProxy
				//���Ѿ��ɹ�������
				//����֮��Proxyû�д���ý��--�����������Ҫ���������˹�ά����
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
		{//�ظ����ͻ��˳ɹ�����ʱ�ſ��Զ�Server�ĸ��½����ύ,��ʱ������ΪProxy
			//���Ѿ��ɹ�������
			//����֮��Proxyû�д���ý��--�����������Ҫ���������˹�ά����
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
	 * ���͵������
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
	        //������Ҫ�ڴ˷���Proxy����Ϣ
	        xdm.setParamValue("proxyid", proxyid);
	        
	        //ָ������
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
			//����Serverʧ��--������һ����˵�ȽϺô���
			//����ҪԤ��
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
			//�������ݣ������ش���ʧ��--
			//�����������ϵͳ�ڲ�bug���⣬�Ƚ�����
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
	        //������Ҫ�ڴ˷���Proxy����Ϣ
	        xdm.setParamValue("proxyid", proxyid);
	        
	        //ָ������
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
			//����Serverʧ��--������һ����˵�ȽϺô���
			//����ҪԤ��
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
			//�������ݣ������ش���ʧ��--
			//�����������ϵͳ�ڲ�bug���⣬�Ƚ�����
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
