package com.dw.web_ui.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dw.comp.AbstractComp;
import com.dw.comp.CompManager;
import com.dw.system.Convert;
import com.dw.user.LoginSession;
import com.dw.user.SessionManager;

/**
 * 用来支持客户端控件直接和WebServer打交道的Servlet
 * 
 * 客户端控件和WebServer之间直接采用Cmd方式进行
 * 这样可以进行指令的扩充
 * 
 * @author Jason Zhu
 */
public class HttpClientCmdServlet extends HttpServlet
{
	static final String ENC = "UTF-16LE" ;
	
	static String CMD = "JasonZhu.WebBrick.Cmd";
    static String CMD_COOKIE = "JasonZhu.WebBrick.CmdCookie";
    static String CMD_RESULT = "JasonZhu.WebBrick.CmdResult";
    static String PARM_PREFIX = "JasonZhu.WebBrick.Param.";
    static int PARM_PREFIX_LEN = PARM_PREFIX.length();
    static String FILE_PREFIX = "JasonZhu.WebBrick.File.";
    static int FILE_PREFIX_LEN = FILE_PREFIX.length();

    public void init() throws ServletException
	{
	}
    
    protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, java.io.IOException
	{
    	try
    	{
    		DoCmd(req, resp);
    	}
    	catch(Exception e)
    	{
    		throw new ServletException(e);
    	}
	}
    
    private void DoCmd(HttpServletRequest req, HttpServletResponse resp)
    throws Exception
    {
        String cmd = req.getHeader(CMD);
        
        String strcookie = req.getHeader(CMD_COOKIE);
        LoginSession ls = SessionManager.StrCookie2LoginSession(strcookie);
        
        HashMap<String, String> cmd_param = new HashMap<String, String>();
        HashMap<String, String> out_ret_parms = new HashMap<String, String>();
        HashMap<String, byte[]> file_param = new HashMap<String, byte[]>();
        HashMap<String, byte[]> out_ret_files = new HashMap<String, byte[]>();
        
        if(Convert.isNullOrEmpty(cmd))
        {//通过ajax方式提交的命令--该命令和控件无关
        	//主要支持没有文件传输的不需要控件支持的一些http请求操作
        	req.setCharacterEncoding("UTF-8");
        	String httpcmd = req.getParameter("cmd");
        	ls = SessionManager.getCurrentLoginSession(req);
        	if(!Convert.isNullOrEmpty(httpcmd))
        	{
        		for(Enumeration en = req.getParameterNames();en.hasMoreElements();)
        		{
        			String pn = (String)en.nextElement();
        			if(pn.startsWith("p_"))
        			{
        				String pv = req.getParameter(pn) ;
        				pv = URLDecoder.decode(pv, "UTF-8");
        				cmd_param.put(pn.substring(2), pv) ;
        			}
        		}
        		doCmd(ls,httpcmd,cmd_param,file_param,out_ret_parms,out_ret_files) ;
        		PrintWriter pw = resp.getWriter() ;
        		for(String rpn:out_ret_parms.keySet())
        		{
        			pw.write(rpn) ;
        			pw.write("=");
        			pw.write(out_ret_parms.get(rpn));
        		}
        		return ;
        	}
        }
        
        
        //通过控件的方式提交内容
        int clen = (int)req.getContentLength();
        byte[] buf = null;
        int ll=0;
        if (clen > 0)
        {
        	buf = new byte[clen];
            ServletInputStream is = req.getInputStream() ;
            do
            {
            	ll+=is.read(buf,ll,clen-ll) ;
            }
            while(ll>0);
        }

        
        
        for (Enumeration en = req.getHeaderNames();en.hasMoreElements();)
        {//由于http头全部被改成小写,所以需要对此进行转换判断
        	String k = (String)en.nextElement() ;
            if (k.toLowerCase().startsWith(PARM_PREFIX.toLowerCase()))
            {
                String strpos_len = k.substring(PARM_PREFIX_LEN);
                String n = req.getHeader(k);
                StringTokenizer st = new StringTokenizer(strpos_len,".");
                //String[] ss = strpos_len.Split('.');
                if (st.countTokens() != 2)
                    continue;

                int pos = Integer.parseInt(st.nextToken());
                int len = Integer.parseInt(st.nextToken());

                String v = new String(buf, pos, len,ENC) ;
                cmd_param.put(n,v);
            }
            else if (k.toLowerCase().startsWith(FILE_PREFIX.toLowerCase()))
            {
                String strf = k.substring(FILE_PREFIX_LEN);
                StringTokenizer st = new StringTokenizer(strf,".");
                //String[] ss = strf.Split('.');
                if (st.countTokens() != 3)
                    continue;

                int pos = Integer.parseInt(st.nextToken());
                int fnlen = Integer.parseInt(st.nextToken());
                int flen = Integer.parseInt(st.nextToken());

                String fn = new String(buf, pos, fnlen,ENC);

                byte[] tmpbs = new byte[flen];
                System.arraycopy(buf, pos + fnlen, tmpbs, 0, flen);

                file_param.put(fn,tmpbs);
            }
        }
        
        boolean res = doCmd(ls,cmd, cmd_param, file_param, out_ret_parms, out_ret_files);

        if(res)
            resp.setHeader(CMD_RESULT, "1");
        else
            resp.setHeader(CMD_RESULT, "0");

        ArrayList<byte[]> conts = new ArrayList<byte[]>();
        int wlen = 0;

        for (String k : out_ret_parms.keySet())
        {
            String v = out_ret_parms.get(k);
            byte[] bs = v.getBytes(ENC);//Encoding.Unicode.GetBytes(v);
            conts.add(bs);
            resp.setHeader(PARM_PREFIX + wlen + "." + bs.length, k);
            wlen += bs.length;
        }

        for (String k : out_ret_files.keySet())
        {
            byte[] fnbs = k.getBytes(ENC);//Encoding.Unicode.GetBytes(k);
            byte[] cont = out_ret_files.get(k);

            resp.setHeader(FILE_PREFIX + wlen + "." + fnbs.length + "." + cont.length, "");

            conts.add(fnbs);
            conts.add(cont);
            wlen += (fnbs.length + cont.length);
        }

        resp.setContentLength(wlen);

        OutputStream os = resp.getOutputStream();
        for (byte[] bs : conts)
        {
            if (bs == null || bs.length == 0)
                continue;

            os.write(bs, 0, bs.length);
        }
    }
    
    private String getExceptionStackTrace(Exception e)
    {
    	StringBuilder sb = new StringBuilder();
    	for(StackTraceElement ste : e.getStackTrace())
    		sb.append(ste.toString()).append("\r\n");
    	
    	return sb.toString();
    }

    private boolean doHttpCmd(LoginSession ls,String cmd,
    		HashMap<String, String> parms,HashMap<String, String> out_ret_parms)
    {
    	return true ;
    }
    
    private boolean doCmd(LoginSession ls,String cmd,
                       HashMap<String, String> parms,
                       HashMap<String, byte[]> files,
                       HashMap<String, String> out_ret_parms,
                       HashMap<String, byte[]> out_ret_files) throws Exception
    {
        if (cmd.startsWith("c:"))
        {
            String cn = cmd.substring(2);
            try
            {
                Class t = Class.forName(cn);
                if (t == null)
                {
                	out_ret_parms.put("Exception","Cannot get Type:"+cn);
                    return false;
                }

                IHttpClientCmdHandler cch = (IHttpClientCmdHandler)t.newInstance();
                if (cch == null)
                {
                	out_ret_parms.put("Exception","Cannot create IClientCmdHandler instance for type=:" + cn);
                    return false;
                }

                StringBuilder failedreson = new StringBuilder(),succmsg = new StringBuilder();
                boolean b = cch.doCmd(ls,parms, files, out_ret_parms, out_ret_files,succmsg, failedreson);
                if (!b)
                {
                    out_ret_parms.put("Exception",failedreson.toString());
                    return false;
                }

                if (succmsg.length()>0)
                {
                    out_ret_parms.put("FeedBack_Msg__",succmsg.toString());
                }

                return b;
            }
            catch(Exception e)
            {
            	out_ret_parms.put("Exception", e.getMessage());
            	out_ret_parms.put("StackTrace", getExceptionStackTrace(e));
                return false;
            }
        }
        
        // 形如 TC:CompName:CompCmd
        //TC 是 Tomato Comp的简称
        if (cmd.startsWith("TC:"))
        {
            String cn = cmd.substring(3);
            int p = cn.indexOf(':');
            if(p<=0)
            {
            	out_ret_parms.put("Exception", "TC: cmd must has comp name!");
            	return false;
            }
            
            try
            {
	            String comp_name = cn.substring(0,p) ;
	            AbstractComp comp = CompManager.getInstance().getComp(comp_name);
	            if(comp==null)
	            {
	            	out_ret_parms.put("Exception", "no comp found with name="+comp_name);
	            	return false;
	            }
	            
	            String cmdn = cn.substring(p+1);
	            IHttpClientCmdHandler cch = comp.getHttpClientCmdHandler(cmdn);
	            if (cch == null)
	            {
	            	out_ret_parms.put("Exception","Cannot create IClientCmdHandler instance for type=:" + cn);
	                return false;
	            }
            
                StringBuilder failedreson = new StringBuilder(),succmsg = new StringBuilder();
                boolean b = cch.doCmd(ls,parms, files, out_ret_parms, out_ret_files,succmsg, failedreson);
                if (!b)
                {
                    out_ret_parms.put("Exception",failedreson.toString());
                    return false;
                }

                if (succmsg.length()>0)
                {
                    out_ret_parms.put("FeedBack_Msg__",succmsg.toString());
                }

                return b;
            }
            catch(Exception e)
            {
            	out_ret_parms.put("Exception", e.getMessage());
            	out_ret_parms.put("StackTrace", getExceptionStackTrace(e));
                return false;
            }
        }

        if ("DocMgr_AddNewServerFile".equals(cmd))
        {
            if (files == null)
                return false;

            for (String k : files.keySet())
            {
            	String path = null ;
            	if(k.startsWith("/"))
            		path = "E:/tmp/docmgr_root" + k;
            	else
            		path = "E:/tmp/docmgr_root/" + k;
                File fi = new File(path);
                fi.getParentFile().mkdirs() ;
                
                FileOutputStream fs = null;
                try
                {
                    byte[] cont = files.get(k);
                    fs = new FileOutputStream(path);
                    fs.write(cont);
                }
                finally
                {
                    fs.close();
                }
            }

            out_ret_parms.put("result","true");
            return true;
        }

        if ("DocMgr_GetServerFile" == cmd)
        {
            String spath = parms.get("server_path");
            if(Convert.isNullOrEmpty(spath))
            	return false ;

            String path = null ;
            if(spath.startsWith("/"))
        		path = "E:/tmp/docmgr_root" + spath;
        	else
        		path = "E:/tmp/docmgr_root/" + spath;
            
            File fi = new File(path);
            if (!fi.exists())
                return false;

            byte[] cont = new byte[(int)fi.length()];
            FileInputStream fs = null;
            try
            {
                fs = new FileInputStream(path);
                fs.read(cont);
            }
            finally
            {
                fs.close();
            }
            
            out_ret_files.put(spath,cont);
            return true;
        }

        return false;
    }
}
