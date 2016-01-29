/*
 * Created on 2005-12-13
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.dw.mltag;

import java.io.InputStream;
import java.io.StringReader;
/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TagDocument
{
	XmlNode rootNode = null ;
	
	public final XmlNode getRootNode()
	{
		return rootNode ;
	}
	
	public void loadFromStr(String tagstr)
	throws Throwable
	{
		StringReader sr = new StringReader(tagstr);
		NodeParser np = new NodeParser(sr,this);
		np.parse();
		rootNode = np.getRoot() ;
		
		OnDocumentLoaded() ;
	}
	
	public void load(InputStream is,String encoding)
	throws Throwable
	{
		NodeParser np = new NodeParser(is,this,encoding);
		np.parse();
		rootNode = np.getRoot() ;
		
		OnDocumentLoaded() ;
	}
	
	public boolean isIgnoreCase()
	{
		return true ;
	}
	
	public XmlNode createXmlNode(String name,Attr[] attrs)
		throws Throwable
	{
		XmlNode xn = new XmlNode (name) ;
		xn.setAttributes (attrs) ;
		return xn ;
	}

    public XmlText createXmlText(String text)
    {
    	return XmlText.parseToXmlText(text) ;
        //return new XmlText(text) ;
    }

    public XmlComment createXmlComment(String cmt)
    {
        return new XmlComment(cmt) ;
    }
    
    public ScriptComment createScriptComment(String cmt)
    {
        return new ScriptComment(cmt) ;
    }
    
    
    public XmlDeclNode createXmlDeclNode(String text)
    {
    	return new XmlDeclNode(text);
    }
    
    public JspTag createJspTagNode(String text)
    {
    	return new JspTag(text);
    }
    
    public JspDeclaration createJspDeclaration(String text)
    {
    	return new JspDeclaration(text);
    }
    
    public JspExpression createJspExpression(String text)
    {
    	return new JspExpression(text);
    }
    
    public JspDirective createJspPageTagNode(String jspdir_t)
    {
    	return new JspDirective(jspdir_t);
    }
    
    public JspComment createJspCommentNode(String cmt)
    {
    	return new JspComment(cmt);
    }
    
    /**
     * 当标签文档被转载结束后，会被调用，继承类可以重载该方法，对装载后的文档
     * 进行更多的后续处理
     */
    protected void OnDocumentLoaded()
    	throws Throwable
    {}
}
