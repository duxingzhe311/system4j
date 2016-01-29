/*
 * Created on 2005-12-19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.dw.mltag;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;



/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NodeFilterParser extends NodeParser
{
	public static interface IFilter
	{
		public boolean needCreateXmlNode(String tagname,Properties attrs);
	}
	
	HashSet nodeTagSet = new HashSet() ;
	HashSet nodeTagSetIgnoreCase = new HashSet() ;
	
	IFilter filter = null ;

	StringBuffer curBkStr = new StringBuffer() ;
	/**
	 * @param in
	 * @throws IOException
	 */
	public NodeFilterParser(InputStream in) throws IOException
	{
		super(in);
	}

	/**
	 * @param in
	 * @param doc
	 * @throws IOException
	 */
	public NodeFilterParser(InputStream in, TagDocument doc) throws IOException
	{
		super(in, doc);
	}

	/**
	 * @param in
	 * @param doc
	 * @param encoding
	 * @throws IOException
	 */
	public NodeFilterParser(InputStream in, TagDocument doc, String encoding)
			throws IOException
	{
		super(in, doc, encoding);
	}

	/**
	 * @param in
	 * @throws IOException
	 */
	public NodeFilterParser(Reader in) throws IOException
	{
		super(in);
	}

	/**
	 * @param in
	 * @param doc
	 * @throws IOException
	 */
	public NodeFilterParser(Reader in, TagDocument doc) throws IOException
	{
		super(in, doc);
	}

	/**
	 * @param inFile
	 * @throws IOException
	 */
	public NodeFilterParser(String inFile) throws IOException
	{
		super(inFile);
	}
	
	public void setParserFilter(IFilter filter)
	{
		this.filter = filter ;
	}

	public void SetNodeTagSet(HashSet hs)
	{
		for(Iterator en = hs.iterator();en.hasNext();)
		{
			String s = (String)en.next();
			nodeTagSet.add(s);
			nodeTagSetIgnoreCase.add(s.toLowerCase()) ;
		}
	}

	public void SetNodeTagSet(String[] tags)
	{
		for(int i = 0 ; i < tags.length; i ++)
		{
			nodeTagSet.add(tags[i]) ;
			nodeTagSetIgnoreCase.add(tags[i].toLowerCase()) ;
		}
	}

	private boolean IsTagInNodeSet(String tagname)
	{
		if(this.isIgnoreCase())
		{
			return this.nodeTagSetIgnoreCase.contains(tagname.toLowerCase());
		}
		else
		{
			return this.nodeTagSet.contains(tagname) ;
		}
	}
	/**
	 * Do XML file parsing.<br/>
	 * If encounter '&lt', will call {@link #maybeElementDecl()};
	 * otherwise call {@link #maybePCData()}.
	 *
	 */
	public void parse (PushbackReader reader)
	throws IOException
	{
		if (reader != null)
			in = reader ;

		isInELementDecl = false ;
		root = new XmlNode ("root") ;
		currentNode = root ;

//		keyNodeRoot = new KeyNode("key_root") ;
//
//		currentKeyNode = keyNodeRoot ;

		ch = in.read () ;
		if(ch=='<')
		{
			char tmpc = (char)in.read() ;
			if(tmpc=='?')
			{
				// is for xml
				IS_FOR_HTML = false ;
				this.setIgnoreCase(false);
			}
			in.unread(tmpc) ;
		}

		while (ch != -1)
		{
			if (ch == '<')
			{
				maybeElementDecl () ;
			}
			else
			{
				ParserText(""+(char)ch);
			}

			ch = in.read () ;
		}
	}

	protected void ParserText(String startc)
	throws IOException
	{
		XmlText tmpxt = null ;
		if(currentNode instanceof XmlText)
		{
			tmpxt = (XmlText)currentNode ;
		}
		else
		{
			tmpxt = new XmlText() ;
			currentNode.addChild(tmpxt) ;
			currentNode = tmpxt ;
		}

		//Console.WriteLine("parser text=="+startc+"  curbk="+this.curBkStr.ToString()+"\n----->");
		tmpxt.append(startc) ;

		ch = in.read () ;
		while(ch!=-1)
		{
			if(ch=='<')
			{
				maybeElementDecl () ;
				return ;
			}
			else
			{
				tmpxt.append((char)ch) ;
				ch = in.read() ;
			}
		}
	}

	
	protected void maybeElementDecl ()
	throws IOException
	{
		pushBkStrToTxtNode() ;

		curBkStr.append('<');

		ch = in.read() ;

		curBkStr.append((char)ch);

		peekTxt (curBkStr) ;

		if (ch == '/')
		{
			maybeElementEnd () ;
			
			return ;
		}

		String name = "" ;

		// get name
		while (!isDelimiter (ch) && ch != '>' && ch != '/')
		{
			name += (char) ch ;
			ch = in.read () ;
			curBkStr.append((char)ch) ;
		}

		if (ignoreCase)
			name = name.toLowerCase () ;

//		String curs = curBkStr.toString() ;
//		curBkStr.delete(0,curBkStr.length()) ;
//
//		if(!IsTagInNodeSet(name))
//		{
//			this.ParserText(curs) ;
//			return ;
//		}

		ArrayList attrlist = new ArrayList() ;
		Properties tmpht = new Properties();
		//分析一个元素节点名称之后的内容
		while (ch != -1)
		{
			peekTxt(curBkStr) ;

			if (ch == '>' || ch == '/')
				break ;

			Attr attr = maybeAttrTxt (curBkStr) ;

			if (attr != null)
			{
				attrlist.add(attr) ;
				tmpht.put(attr.getName(),attr.getValue());
			}
		}
		
		String curs = curBkStr.toString() ;
		curBkStr.delete(0,curBkStr.length()) ;
		
		if(filter!=null&&!filter.needCreateXmlNode(name,tmpht))
		{
			this.ParserText(curs) ;
			return ;
		}

		XmlNode tmpnode = null ;

		int nodestyle ;
		if (ch == '/')
		{//发现没有子节点的元素
			ch = in.read () ;
			while (ch != '>' && ch != -1)
				ch = in.read () ;

			if(IS_FOR_HTML&&AbstractNode.NEED_CHILD_ELEMENTS.indexOf("|"+name.toLowerCase()+"|")>=0)
			{
				nodestyle = AbstractNode.NODE_STYLE_NEED_CHILD;
			}
			else
			{
				nodestyle = AbstractNode.NODE_STYLE_NO_CHILD ;
			}

			tmpnode = new XmlNode (name);// , currentNode) ;
		}
		else
		{//
			//			if(AbstractNode.IS_FOR_HTML&&!HTML_TAGS.IsHtmlTag(name))
			//			{//忽略该标签，但会继续扫描其子节点
			//				
			//			}


			if (IS_FOR_HTML&&AbstractNode.NO_CHILD_ELEMENTS.indexOf ("|" + name.toLowerCase () + "|")>=0)
			{
				nodestyle = AbstractNode.NODE_STYLE_NO_CHILD ;

				tmpnode = new XmlNode (name);// , currentNode) ;
			}
			else
			{
				nodestyle = AbstractNode.NODE_STYLE_NEED_CHILD;

				tmpnode = new XmlNode (name);// , currentNode) ;
			}
		}

		
		for(int i = 0 ; i < attrlist.size() ; i ++)
		//foreach(Attr att in attrlist)
		{
			Attr att = (Attr)attrlist.get(i);
			tmpnode.setAttr(att) ;
		}

		if (currentNode == null || root == null)
		{
			root = tmpnode ;
			currentNode = tmpnode ;
		}
		else
		{
			if(currentNode instanceof XmlText)
			{
				currentNode = (AbstractNode)currentNode.getParent();
			}

			currentNode.addChild (tmpnode) ;
		}
	

		//Console.WriteLine("Begin Element={0} NodeStyle={1}",name,nodestyle) ;

		if(nodestyle==AbstractNode.NODE_STYLE_NEED_CHILD)
		{
			//有小孩的节点
			currentNode = tmpnode ;
		}
		else if(nodestyle==AbstractNode.NODE_STYLE_NO_CHILD)
		{
		}
	}

	protected void peekTxt (StringBuffer buf1)
	throws IOException
	{
		// ch = pbin.Read () ;
		while (isDelimiter (ch))
		{
			ch = in.read () ;
			if(buf1!=null)
				buf1.append ((char) ch) ;
		}
	}
	
	protected Attr maybeAttrTxt (StringBuffer buf) throws IOException
	{
		// System.out.println ("maybeAttr") ;

		Attr attr = new Attr () ;


		if (ch == -1)
			return null ;
		peekTxt (buf) ;

		//
		while (ch != -1 && (! isNamingChar (ch)))
		{
			ch = in.read () ;
			
			buf.append((char)ch);
		}
		String name = "" ;

		while ((!isDelimiter (ch)) && (ch != '=') && (ch != -1))
		{
			if (isNamingChar (ch))
			{
				name += (char) ch ;
				ch = in.read () ;
				
				buf.append((char)ch);
			}
			else
				break ;
		}

		if (name.length () <= 0)
			return null ;

		if (ignoreCase)
			attr.setName (name.toLowerCase ()) ;
		else
			attr.setName (name) ;

		// System.out.println ("AttributeName: [" + attr.getName () + "]") ;
		peekTxt (buf) ;

		if (ch == -1)
			return attr ;

		if (ch == '=') // value start
		{
			//attr.setValue(maybeAttrValue ()) ;
			String tmpav  = maybeAttrValue () ;
			buf.append("\"").append(tmpav).append("\"") ;
			
			Attr.parseAttrValue(attr, tmpav);
		}
		/*
		else
		{
			return maybeAttr () ;
		}
		*/
		return attr ;
	}
	
	
	/**
	 * A element ended.
	 */
	protected void maybeElementEnd ()
	throws IOException
	{
		ch = in.read() ;
		curBkStr.append((char)ch) ;

		peekTxt (curBkStr) ;

		String name = "" ;

		while (! isDelimiter (ch) && ch != '>' && ch != -1)
		{
			name += (char) ch ;

			ch = in.read () ;
			curBkStr.append((char)ch) ;
		}

		if (ignoreCase)
			name = name.toLowerCase () ;

		String curs = curBkStr.toString() ;
		curBkStr.delete(0,curBkStr.length()) ;

		if(!IsTagInNodeSet(name))
		{
			this.ParserText(curs) ;
			return ;
		}

		System.out.println("Find end ele:"+name) ;

		while (ch != '>' && ch != -1)
			ch = in.read () ;


//		bool iskey = false ;
//		if(IS_FOR_HTML&&AbstractNode.KEY_HTML_ELEMENT.IndexOf("|"+name.ToLower()+"|")>=0)
//		{
//			iskey = true ;
//		}

		if(currentNode instanceof XmlText)
		{
			currentNode = (AbstractNode)currentNode.getParent() ;
		}

		if(name.toLowerCase()!=this.currentNode.getNodeName().toLowerCase())
		{
			

			//当前节点与发现的结束节点不匹配
			//处理如下:
			//1,查找当前节点的父结点,并进行匹配
			//2,建立一个元素标签重要程度表,比较如果发现自己没有
			//Console.WriteLine("**** End Element=</"+name+">   cur node="+currentNode.getNodeName()) ;
			throw new IOException("Node in set is not match:["+name+"]") ;
		}

		//Console.WriteLine("End Element=</"+name+">") ;

		XmlNode curxn = (XmlNode)currentNode;
		curxn.bHasEndTag = true ;

		if(currentNode.getParent()!=null)
			currentNode = (AbstractNode)currentNode.getParent() ;
		
	}


	private void pushBkStrToTxtNode()
	{
		if(this.curBkStr.length()==0)
			return ;

		String s = curBkStr.toString() ;
		curBkStr.delete(0,curBkStr.length()) ;

		if(currentNode instanceof XmlText)
		{
			((XmlText)currentNode).append(s) ;
			return ;
		}

		XmlText tmpxt = new XmlText() ;
		tmpxt.append(s) ;
		currentNode.addChild(tmpxt) ;
		currentNode = tmpxt ;
	}
}
