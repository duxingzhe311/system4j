// Decompiled by Decafe PRO - Java Decompiler
// Classes: 1   Methods: 5   Fields: 2

package com.jspsmart.upload;

import java.util.*;

import javax.servlet.http.* ;
import javax.servlet.* ;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Request implements javax.servlet.http.HttpServletRequest , javax.servlet.ServletRequest
{

    private Hashtable m_parameters;
    private int m_counter;
    
    transient javax.servlet.http.HttpServletRequest sorReq = null ;

    Request()
    {
        m_parameters = new Hashtable();
        m_counter = 0;
    }

    protected void putParameter(String name, String value)
    {
        if(name == null)
            throw new IllegalArgumentException("The name of an element cannot be null.");
        if(m_parameters.containsKey(name))
        {
            Hashtable values = (Hashtable)m_parameters.get(name);
            values.put(new Integer(values.size()), value);
        }
        else
        {
            Hashtable values = new Hashtable();
            values.put(new Integer(0), value);
            m_parameters.put(name, values);
            m_counter++;
        }
    }

    public String getParameter(String name)
    {
        if(name == null)
            throw new IllegalArgumentException("Form's name is invalid or does not exist (1305).");
        Hashtable values = (Hashtable)m_parameters.get(name);
        if(values == null)
            return null;
        else
            return (String)values.get(new Integer(0));
    }

    public Enumeration getParameterNames()
    {
        return m_parameters.keys();
    }

    public String[] getParameterValues(String name)
    {
        if(name == null)
            throw new IllegalArgumentException("Form's name is invalid or does not exist (1305).");
        Hashtable values = (Hashtable)m_parameters.get(name);
        if(values == null)
            return null;
        String strValues[] = new String[values.size()];
        for(int i = 0; i < values.size(); i++)
            strValues[i] = (String)values.get(new Integer(i));

        return strValues;
    }

   	// implements for javax.servlet.ServletRequest
 	public Object getAttribute (String key)
 	{
 		return sorReq.getAttribute(key) ;
 	}
 	public Enumeration getAttributeNames()
 	{
 		return sorReq.getAttributeNames() ;
 	}

 	public String getCharacterEncoding ()
 	{
 		return sorReq.getCharacterEncoding() ;
 	}
 	public int getContentLength()
 	{
 		return sorReq.getContentLength() ;
 	}

 	public String getContentType()
 	{
 		return sorReq.getContentType() ;
 	}
 	public ServletInputStream getInputStream() throws IOException
 	{
 		throw new IOException ("Abstract method.") ;
 	}
 	public String getProtocol()
 	{
 		return sorReq.getProtocol() ;
 	}
 	public String getScheme()
 	{
 		return sorReq.getScheme() ;
 	}

 	public String getServerName()
 	{
 		return sorReq.getServerName() ;
 	}
 	
 	
 	
 	
 	public int getServerPort()
 	{
 		return sorReq.getServerPort() ;
 	}

 	public BufferedReader getReader () throws IOException
 	{
 		throw new IOException ("Abstract method.") ;
	}
	public String getRemoteAddr()
	{
		return sorReq.getRemoteAddr() ;
	}

	public String getRemoteHost()
	{
		return sorReq.getRemoteHost() ;
	}

	public void setAttribute(String key, Object o)
	{
		sorReq.setAttribute(key, o);
	}

	public String getRealPath(String path)
	{
		return sorReq.getRealPath(path) ;
	}

	public String getAuthType()
	{
		return sorReq.getAuthType() ;
	}

	public Cookie[] getCookies()
	{
		return sorReq.getCookies() ;
	}

	public long getDateHeader(String name)
	{
		return sorReq.getDateHeader(name) ;
	}
	public String getHeader(String name)
	{
		return sorReq.getHeader(name) ;
	}

	public Enumeration getHeaderNames()
	{
		return sorReq.getAttributeNames() ;
	}
	public int getIntHeader(String name)
	{
		return sorReq.getIntHeader(name) ;
	}
	public String getMethod()
	{
		return sorReq.getMethod() ;
	}

	public String getPathInfo()
	{
		return sorReq.getPathInfo() ;
	}

	public String getPathTranslated()
	{
		return sorReq.getPathTranslated() ;
	}

	public String getQueryString()
	{
		return sorReq.getQueryString() ;
	}
	public String getRemoteUser()
	{
		return sorReq.getRemoteUser();
	}

	public String getRequestedSessionId ()
	{
		return sorReq.getRequestedSessionId() ;
	}

	public String getRequestURI()
	{
		return sorReq.getRequestURI() ;
	}

	public String getServletPath()
	{
		return sorReq.getServletPath() ;
	}

	public HttpSession getSession (boolean create)
	{
		return sorReq.getSession() ;
	}
	public HttpSession getSession()
	{
		return sorReq.getSession() ;
	}

	public boolean isRequestedSessionIdValid ()
	{
		return sorReq.isRequestedSessionIdValid() ;
	}

	public boolean isRequestedSessionIdFromCookie ()
	{
		return sorReq.isRequestedSessionIdFromCookie() ;
	}

	public boolean isRequestedSessionIdFromURL()
	{
		return sorReq.isRequestedSessionIdFromURL() ;
	}

	public boolean isRequestedSessionIdFromUrl ()
	{
		return sorReq.isRequestedSessionIdFromUrl() ;
	}

	// For IAS
	public void removeAttribute (String key)
	{
		sorReq.removeAttribute(key);
	}

	public boolean isSecure ()
	{
		return sorReq.isSecure() ;
	}

	public java.util.Locale getLocale ()
	{
		return sorReq.getLocale() ;
	}
	public javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String url)
	{
		return sorReq.getRequestDispatcher(url) ;
	}
	public java.util.Enumeration getHeaders(java.lang.String header)
	{
		return sorReq.getHeaders(header) ;
	}

	public java.util.Enumeration getLocales ()
	{
		return sorReq.getLocales() ;
	}

	public java.security.Principal getUserPrincipal ()
	{
		return sorReq.getUserPrincipal() ;
	}

	public boolean isUserInRole(java.lang.String name)
	{
		return sorReq.isUserInRole(name) ;
	}
	public java.lang.String getContextPath ()
	{
		return sorReq.getContextPath() ;
	}

	public void setCharacterEncoding(String encode)
	{
		try
		{
			sorReq.setCharacterEncoding(encode);
		}
		catch(Exception e)
		{}
    }

    public Map getParameterMap()
    {
        //return sor;
    	return null ;
    }

    public StringBuffer getRequestURL()
    {
        return sorReq.getRequestURL();
    }

	public int getRemotePort()
	{
		return sorReq.getRemotePort();
	}

	public String getLocalName()
	{
		return sorReq.getLocalName();
	}

	public String getLocalAddr()
	{
		return sorReq.getLocalAddr();
	}

	public int getLocalPort()
	{
		return sorReq.getLocalPort();
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext startAsync() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse arg0) throws IOException,
			ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Part getPart(String arg0) throws IOException, IllegalStateException,
			ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Part> getParts() throws IOException,
			IllegalStateException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void login(String arg0, String arg1) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub
		
	}
}