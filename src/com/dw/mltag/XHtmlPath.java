package com.dw.mltag;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * ��ʵ�ֶ�xml���������ݵĶ�λ��
 * ��λ��ʽ�ñ�׼��xpath��ʽ���С�<br/><br/>
 * �Զ�λΪ�������ṩ�����������л�ȡ�����Ӻ��޸ĵĲ�<br/><br/>
 * �ṩ����Ԫ�ص����뻯����
 */
public class XHtmlPath
{
	//////////////////////////
	/*
	 * ��ȡԪ�صĺ���Ԫ��
	  protected static Element [] getChildElement (Element ele)
	  {
	 NodeList tmpnl = ele.getChildNodes() ;
	 Node tmpn = null ;
	 Vector v = new Vector () ;
	 int k;
	 for (k = 0 ; k < tmpnl.getLength() ; k ++)
	 {
	  tmpn = tmpnl.item (k) ;
	  if (tmpn.getNodeType()!=Node.ELEMENT_NODE)
	   continue ;
	  v.add (tmpn) ;
	 }
	 int s = v.size () ;
	 Element [] tmpe = new Element [s] ;
	 for (k = 0 ; k < s ; k ++)
	  tmpe [k] = (Element)v.elementAt (k) ;
	 return tmpe ;
	  }*/
	/*
	 * ��ȡԪ�ص��������Ժ�ֵ
	 *@param ele Ԫ�ض���
	 *@return Hashtable ������Ժ�ֵ��Hash����keyΪ��������Ӧ��ֵΪ����ֵ
	  public static Properties getElementAttributes (Element ele)
	  {
	 Properties ht = new Properties () ;
	 NamedNodeMap nnm = ele.getAttributes () ;
	 int len = nnm.getLength () ;
	 Node tmpn = null ;
	 for (int k = 0 ; k < len ; k ++)
	 {
	  tmpn = nnm.item (k) ;
	  String tmps = tmpn.getNodeValue () ;
	  if (tmps==null)
	 tmps = "" ;
	  ht.put (tmpn.getNodeName(),tmps) ;
	 }
	 return ht ;
	  }*/
	/*
	 * ��ȡ��xpath��ָ��Ԫ�ص�����������
	 *@param xp ָ����������ĳһԪ�ص�xpath��
	 *@return String[] �����������������ַ���
	  public String [] getElementAllAttrNames (String xp)
	 throws Exception
	  {
	 Element tmpe = getElement (xp) ;
	 if (tmpe==null)
	  return new String [0] ;
	 Hashtable ht = getElementAttributes (tmpe) ;
	 int s = ht.size () ;
	 String tmps[] = new String [s] ;
	 Enumeration enum = ht.keys () ;
	 for (int i = 0 ; enum.hasMoreElements () ; i ++)
	  tmps[i] = (String)enum.nextElement () ;
	 return tmps ;
	  }*/
	/*
	 * ��һ��xpath��ȡ��ָ��ڵ�����ݡ�
	  public static String getXPathNodeName (String xp)
	 throws Exception
	  {
	 int i = xp.lastIndexOf ('/') ;
	 int j = xp.lastIndexOf ('@') ;
	 if (i<j&&xp.charAt(j-1)!='[')
	  throw new Exception ("invalid xpath ["+xp+"],it isn't point to nodes.") ;
	 String tmps = xp.substring (i+1) ;
	 int k = tmps.indexOf ('[') ;
	 if (k<0)
	  return tmps ;
	 return tmps.substring (0,k) ;
	  }*/

	/*
	 * ��ȡ���е�����·���������ַ�����˳��0->  ��ʾ����->����
	  public String[] getAllAncestorNodeXPath (String xp,boolean inself)
	  throws Exception
	  {
	  int i = xp.lastIndexOf ('/') ;
	 int j = xp.lastIndexOf ('@') ;
	 if (i<j&&xp.charAt(j-1)!='[')
	  throw new Exception ("invalid xpath ["+xp+"],it isn't point to nodes.") ;
	 //if (i<2)
	  //throw new Exception ("invalid xpath ["+xp+"],it has no fathor node.") ;
	  Element tmpe = getElement (xp) ;
	  if (tmpe==null)
		 throw new Exception ("invalid xpath ["+xp+"],it isn't point to element.") ;
	  Vector v = new Vector () ;
	  String tmppath = xpathNodePath (xp) ;
	  if (inself)
	   v.add (tmppath) ;
	  while (true)
	  {
	   tmpe = (Element)tmpe.getParentNode () ;
	   if (tmpe==superroot||tmpe==null)
	  break ;
	   tmppath = getParentNodeXPath (tmppath) ;
	   System.out.println (">>"+tmppath) ;
	   Element[] eles = parse (superroot,tmppath) ;
	   if (eles==null||eles.length==0)
	  break ;
	   if (eles.length==1)
	  v.add (tmppath) ;
	   if (eles.length>1)
	   {//�Զ��������
	  int k ;
	  for (k = 0 ; k < eles.length ; k ++)
	  {
	   if (eles[k]==tmpe)
	 v.add (tmppath+"["+k+"]") ;
	  }
	  if (k==eles.length)
	   throw new Exception ("Perhaps XPathWorker's bug that can't get proper element.");
	   }
	  }
	  int s = v.size () ;
	  String[] tmps = new String [s] ;
	  for (int p = 0 ; p < s ; p ++)
	   tmps[p] = (String)v.elementAt(s-p-1) ;
	  return tmps ;
	  }*/
	/**
	 * ��xpath��ȡ�������ڵ��xpath����
	 */
	public static String getParentNodeXPath(String xp)
		throws Exception
	{
		/*
		   int k = xp.indexOf (rootName) ;
		   if (k<0)
		   {
		 if (xp.charAt(1)!='/')
		  xp = ("/" + rootName + xp) ;
		   }
		 */
		int i = xp.lastIndexOf('/');
		int j = xp.lastIndexOf('@');

		if (i < j && xp.charAt(j - 1) != '[')
		{
			throw new Exception("invalid xpath [" + xp +
								"],it isn't point to nodes.");
		}

		if (i < 2)
		{
			throw new Exception("invalid xpath [" + xp +
								"],it has no fathor node.");
		}

		String tmps = xp.substring(0, i);

		if (tmps.endsWith("/"))
		{

			//return tmps + "/*" ;
			 throw new Exception("invalid xpath [" + xp +
								 "],it may has multi fathor nodes.");
		}

		return tmps;
	}

	/**
	 * �ж�һ��xpath��ָ�������Ƿ������һ��xpath��ָ�����ݡ�<br/>
	 *    �ж�  sorxp �Ƿ���� tarxp.
	 *@param sorxp ��xpath
	 *@param tarxp ��xpath,�����ж��Ƿ񱻰���<br/>
	 *<b>ע:���е�xpath��ò�����λ����Ϣ.</b>
	 *@return boolean ���sorxp����tarxp=true,����=false.
	 */
	public boolean xpathInclude(String sorxp, String tarxp)
		throws Exception
	{
		String sorxppath = xpathNodePath(sorxp);
		String sorxpattr = xpathAttribute(sorxp);
		String tarxppath = xpathNodePath(tarxp);
		String tarxpattr = xpathAttribute(tarxp);

		if (sorxpattr != null && !sorxpattr.equals(tarxpattr))
		{
			return false;
		}

		return xpathNodeInclude(sorxppath, tarxppath);
	}

	/**
	 * �ж�ָ��ڵ��xpath�Ƿ����
	 * xpath����ָ��ڵ�
	 */
	private boolean xpathNodeInclude(String sorxp, String tarxp)
		throws Exception
	{
		/*
		   int k = sorxp.indexOf (rootName) ;
		   if (k>=0)
		 sorxp = sorxp.substring (k+rootName.length()) ;
		   k = tarxp.indexOf (rootName) ;
		   if (k>=0)
		 tarxp = tarxp.substring (k+rootName.length()) ;
		 */
		int k = tarxp.indexOf("//");
		if (k >= 0)
		{
			return false;
		}

		k = sorxp.indexOf("//");
		if (k >= 0)
		{ //&&sorxp.lastIndexOf("//")!=k)
			throw new Exception("invalid source xpath=[" + sorxp +
								"],it include more than one \"//\"!");
		}

		if (k < 0)
		{ //no '//' case
			return xpathNodeIncludeAbsulte(sorxp, tarxp);
		}
		else
		{
			return false;
		}
	}

	/**
	 * �ж���������xpath·���Ƿ����໥������ϵ��
	 * ��������xpath��ָ��ڵ�
	 * <b>be careful:This function cannot judge xpath that has "//"</b>
	 */
	private boolean xpathNodeIncludeAbsulte(String sorxp, String tarxp)
		throws
		Exception
	{
		StringTokenizer sorst = new StringTokenizer(sorxp, "/", false);
		StringTokenizer tarst = new StringTokenizer(tarxp, "/", false);
		String tmpsor = null, tmptar = null;
		String tagsor = null, tagtar = null;
		int possor, postar;
		Hashtable htsor = null, httar = null;
		while (sorst.hasMoreTokens())
		{
			tmpsor = sorst.nextToken();
			if (!tarst.hasMoreTokens())
			{
				return false;
			}
			tmptar = tarst.nextToken();

			tagsor = getTag(tmpsor);
			tagtar = getTag(tmptar);
			if (!tagsor.equals("*") && !tagsor.equals(tagtar))
			{
				return false;
			}

			possor = getPos(tmpsor);
			postar = getPos(tmptar);
			if (possor > 0 && possor != postar)
			{
				return false;
			}

			htsor = getAtt(tmpsor);
			httar = getAtt(tmptar);

			if (!attInclude(htsor, httar))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * ��ȡxpath��ĳһ���еı�ǩ��
	 */
	private String getTag(String input)
		throws Exception
	{
		int k = input.indexOf('[');
		if (k < 0)
		{
			return input;
		}
		else if (k > 0)
		{
			return input.substring(0, k);
		}
		else
		{
			throw new Exception("invalid xpath=(.." + input +
								"..),it has no tag name!");
		}
	}

	/**
	 * ��ȡxpath��ĳһ���е�λ����Ϣ
	 */
	private int getPos(String input)
		throws Exception
	{
		int k = input.indexOf('[');
		if (k < 0)
		{
			return -1;
		}

		StringTokenizer st = new StringTokenizer(input.substring(k), "[]", false);
		String tmps = null;
		if (st.hasMoreTokens())
		{
			tmps = st.nextToken().trim();
			if (tmps.charAt(0) != '@')
			{
				try
				{
					return Integer.parseInt(tmps);
				}
				catch (NumberFormatException nfe)
				{
					throw new Exception("invalid xpath (.." + input +
										"..),it position is not numbers!");
				}
			}
		}

		if (st.hasMoreTokens())
		{
			tmps = st.nextToken().trim();
			if (tmps.charAt(0) != '@')
			{
				try
				{
					return Integer.parseInt(tmps);
				}
				catch (NumberFormatException nfe)
				{
					throw new Exception("invalid xpath (.." + input +
										"..),it position is not numbers!");
				}
			}
		}
		return -1;
	}

	/**
	 * ��ȡxpath��ĳһ���е���������
	 *@return Hashtable �������Ե�Hash��
	 */
	private Hashtable getAtt(String input)
		throws Exception
	{
		int k = input.indexOf('[');
		if (k < 0)
		{
			return null;
		}

		StringTokenizer st = new StringTokenizer(input.substring(k), "[]", false);
		String tmps = null;
		if (st.hasMoreTokens())
		{
			tmps = st.nextToken().trim();
			if (tmps.charAt(0) == '@')
			{
				return getHashAttribute(tmps);
			}
		}
		if (st.hasMoreTokens())
		{
			tmps = st.nextToken().trim();
			if (tmps.charAt(0) == '@')
			{
				return getHashAttribute(tmps);
			}
		}

		return null;
	}

	/**
	 * �ж�һ������Hash���е����������Ƿ����һ���١�
	 */
	private boolean attInclude(Hashtable sorht, Hashtable tarht)
	{
		if (sorht == null)
		{
			return true;
		}
		if (tarht == null)
		{
			return false;
		}

		String tmps = null;
		String valsor = null, valtar = null;
		for (Enumeration sorenum = sorht.keys(); sorenum.hasMoreElements(); )
		{
			tmps = (String) sorenum.nextElement();
			if (!tarht.containsKey(tmps))
			{
				return false;
			}
			valsor = (String) sorht.get(tmps);
			valtar = (String) tarht.get(tmps);

			if (!valsor.equals("") && !valsor.equals(valtar))
			{
				return false;
			}
		}

		return true;
	}

	///////////////////////////////////////////////////////////////
	static String DIV_CHAR = "/* @[]\"";
	//static String COND_DIV = "[]" ;
	String defaultFileName = null;
	//Document document = null ;
	AbstractNode superroot = null;
	AbstractNode root = null;
	String rootName = null;

	public static XHtmlPath getXPath(String filename)
		throws Exception
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(new File(filename));

			NodeParser parser = new NodeParser(fis);
			parser.setIgnoreCase(true);

			parser.parse();

			AbstractNode root = parser.getRoot();
			XHtmlPath xw = new XHtmlPath(root);

			fis.close();

			xw.setDefaultFileName(filename);

			return xw;
		}
		catch (Exception e)
		{
			throw new Exception(e.toString());
		}
		finally
		{
			if (fis != null)
			{
				fis.close();
			}
		}
	}

	public XHtmlPath()
	{}

	public XHtmlPath(InputStream stream)
		throws Exception
	{
		NodeParser parser = new NodeParser(stream);
		parser.setIgnoreCase(true);

		parser.parse();

		root = parser.getRoot();

		superroot = new XmlNode("superroot");
		superroot.addChild(root);
		rootName = root.getNodeName();
	}
	
	public XHtmlPath(InputStream stream,NodeFilterParser.IFilter filter)
	throws Exception
	{
		this(stream,filter,null);
	}
	
	public XHtmlPath(InputStream stream,NodeFilterParser.IFilter filter,String encoding)
	throws Exception
	{
		//NodeFilterParser parser = new NodeFilterParser(stream);
		
		NodeParser parser = new NodeParser(stream,null,encoding);
		//parser.setParserFilter(filter) ;
		parser.setIgnoreCase(true);
	
		parser.parse();
	
		root = parser.getRoot();
	
		superroot = new XmlNode("superroot");
		superroot.addChild(root);
		rootName = root.getNodeName();
	}
	
	
	public XHtmlPath(URL u,NodeFilterParser.IFilter filter)
	throws Exception
	{
		URLConnection conn = u.openConnection();

		conn.connect();
		
		String enc = null ;
		String contt = conn.getContentType() ;
//		for(Map.Entry<String, List<String>> n2fvs:conn.getHeaderFields().entrySet())
//		{
//			String n0 = n2fvs.getKey() ;
//			List<String> vs = n2fvs.getValue() ;
//			System.out.println(n0+"="+vs.get(0)) ;
//		}
		if(contt!=null)
		{
			//text/html; charset=UTF-8
			enc = getEncFromContentType(contt);
		}
		
		NodeParser parser = null ;
		int len = (int)conn.getContentLength() ;
		int rlen = 0 ;
		if(enc==null||enc.equals(""))
		{//������Ҫ�������ݽ��б����ȡ�������½��������ĵ��������ȶ�ȡ����
			byte[] buf = null ;
			if(len>0)
			{
				buf = new byte[len] ;
				InputStream is = null;
				try
				{
					is = conn.getInputStream();
					while(rlen<len)
					{
						int ll = is.read(buf, rlen, len-rlen) ;
						if(ll<0)
							break ;
						
						rlen += ll ;
					}
				}
				finally
				{
					if(is!=null)
						is.close() ;
				}
			}
			else
			{
				InputStream is = null;
				try
				{
					is = conn.getInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream() ;
					int rll = 0 ;
					byte[] buff = new byte[1024] ;
					do
					{
						rll = is.read(buff);
						if(rll>0)
						{
							baos.write(buff, 0, rll) ;
						}
					}while(rll>=0) ;
					
					buf = baos.toByteArray() ;
				}
				finally
				{
					if(is!=null)
						is.close() ;
				}
				
			}
			
			parser = new NodeParser(new ByteArrayInputStream(buf),null,"UTF-8");
			parser.setIgnoreCase(true);
			parser.parse();
			root = parser.getRoot();
			
			superroot = new XmlNode("superroot");
			superroot.addChild(root);
			rootName = root.getNodeName();
			
			AbstractNode[] ans = this.getAllNode("//meta");
			if(ans==null)
				return ;
			
			for(AbstractNode an:ans)
			{
				if("Content-Type".equalsIgnoreCase(an.getAttribute("http-equiv")))
				{
					String ct = an.getAttribute("content") ;
					enc = getEncFromContentType(ct);
					break ;
				}
			}
			
			if(enc==null||enc.equals("")||"utf-8".equalsIgnoreCase(enc)||"utf8".equalsIgnoreCase(enc))
				return ;//ʹ��ԭ���ı���
			
			parser = new NodeParser(new ByteArrayInputStream(buf),null,enc);
			parser.setIgnoreCase(true);
			parser.parse();
			root = parser.getRoot();
			
			superroot = new XmlNode("superroot");
			superroot.addChild(root);
			rootName = root.getNodeName();
		}
		else
		{
			InputStream is = null ;
			try
			{
				is = conn.getInputStream();
			
				parser = new NodeParser(is,null,enc);
	//			parser.setParserFilter(filter) ;
				parser.setIgnoreCase(true);
			
				parser.parse();
			}
			finally
			{
				if(is!=null)
					is.close() ;
			}
			
			root = parser.getRoot();
		
			superroot = new XmlNode("superroot");
			superroot.addChild(root);
			rootName = root.getNodeName();
		}
	}

	static String getEncFromContentType(String ct)
	{
		StringTokenizer st = new StringTokenizer(ct,";") ;
		while(st.hasMoreTokens())
		{
			String ss = st.nextToken().trim() ;
			int k = ss.indexOf('=') ;
			if(k<=0)
				continue ;
			
			if("charset".equalsIgnoreCase(ss.substring(0,k).trim()))
				return ss.substring(k+1).trim() ;
		}
		
		return null ;
	}
	/**
	 * ���캯��
	 *@param doc Xml parser �е��ĵ�����
	 */
	public XHtmlPath(AbstractNode root)
	{
		//document = doc ;
		superroot = new XmlNode("superroot"); //document.createElement("superroot") ;

		superroot.addChild(root);
		rootName = root.getNodeName();
	}

	public void setDefaultFileName(String dfn)
	{
		defaultFileName = dfn;
	}

	/**
	 * �õ���ǰ�ĵ���������Ӧ�ĸ�Ԫ��
	 */
	public AbstractNode getRootNode()
	{
		return root;
	}

	/**
	 * ��ȡһ��Ԫ�صĵ�һ��Text�ڵ����ݣ�����������ʵ���������ݣ�
	 */
	private String getText(AbstractNode ele)
	{
		/*
		   AbstractNode tmpn = ele.getFirstChild () ;
		   if (tmpn!=null&&tmpn.getNodeType()==AbstractNode.TEXT_NODE)
		 return tmpn.getNodeValue().trim() ;
		   else
		 return null ;
		 */
		int cc = ele.getChildCount();

		for (int i = 0; i < cc; i++)
		{
			Object o = ele.getChildAt(i);
			if (o instanceof XmlText)
			{
				XmlText xt = (XmlText) o;
				return xt.getText();
			}
		}

		return "";
	}

	/**
	 * ��ȡһ��Ԫ���е�ĳһ������ֵ
	 *@param ele Ԫ�ض���
	 *@param attrn ������
	 *@return String ����ֵ
	 */
	private String getAttrValue(AbstractNode ele, String attrn)
	{
		return ele.getAttribute(attrn);
	}

	/**
	 * ��ĳ��Ԫ���������Ժ�ֵ�����ԭ�����Դ��ڣ������
	 *@param ele Ԫ�ض���
	 *@param attrn ������
	 *@param attrv ����ֵ
	 */
	private void setAttrValue(AbstractNode ele, String attrn, String attrv)
	{
		ele.setAttribute(attrn, attrv);
	}

	/**
	 * ɾ��Ԫ�ص�ĳ������
	 *@param ele Ԫ�ض���
	 *@param sttrn ������
	 *@return return �ɵ�ֵ
	 */
	private String removeAttrValue(AbstractNode ele, String attrn)
	{
		String tmps = ele.getAttribute(attrn);
		ele.removeAttribute(attrn);
		return tmps;
	}

	/**
	 * ��ĳ��Ԫ�ص�������Ԫ�ؽ��й��ˣ�ʹ�õ������ǵ�һ����ǩ �� "/xxx[][]" or "//xxx[][]"
	 * @param curxp ָ��Ԫ�ص����xpath·�� (not attribute)
	 */
	private static AbstractNode[] parse(AbstractNode curele, String curxp)
		throws
		Exception
	{ //log ("cruxp="+curxp) ;
		if (curxp.charAt(0) != '/')
		{
			throw new Exception("invalid xpath \"" + curxp +
								"\" [no '/' in header]");
		}
		/*
		   else if (curxp.equals("/"))
		 return new Element []{curele} ;
		 */
		String restxp = null;
		// between /'s element content.
		String curinxp = null;
		int i = curxp.indexOf('/', 2);
		if (i > 0)
		{
			restxp = curxp.substring(i);
			curinxp = curxp.substring(1, i);
		}
		else
		{
			curinxp = curxp.substring(1);
		}
		//log ("curxp="+curinxp+" restxp="+restxp) ;
		AbstractNode[] tmpe = parseInXp(curele, curinxp);
		if (tmpe == null)
		{
			return new AbstractNode[0];
		}

		if (restxp == null)
		{
			return tmpe;
		}

		Vector v = new Vector();
		int s = 0;
		for (int k = 0; k < tmpe.length; k++)
		{
			AbstractNode[] tmpee = parse(tmpe[k], restxp);

			if (tmpee == null)
			{
				continue;
			}

			s += tmpee.length;
			v.add(tmpee);
		}

		AbstractNode[] rete = new AbstractNode[s];
		s = v.size();
		int pos = 0;
		for (int j = 0; j < s; j++)
		{
			AbstractNode[] tmpeee = (AbstractNode[]) v.elementAt(j);
			if (tmpeee == null)
			{
				continue;
			}
			System.arraycopy(tmpeee, 0, rete, pos, tmpeee.length);
			pos += tmpeee.length;
		}

		return rete;
	}

	/**
	 * ����һ����ǩ����������{@link parse(Element,String) parse}����
	 */
	private static AbstractNode[] parseInXp(AbstractNode curele, String curinxp)
		throws
		Exception
	{
		char c = curinxp.charAt(0);
		if (c == '/')
		{ //case '//'
			curinxp = curinxp.substring(1);

		}

		if (c == '@')
		{ //do nothing
			log("Warning [/" + curinxp + "] is attribute!!");
			return new AbstractNode[0];
		}

		//parse current element
		int i = curinxp.indexOf('[');
		String tagname = null;
		String cond = null;
		String attstr = null;
		String posstr = null;
		String sss[] = new String[3];
		parseInXP(curinxp, sss);
		tagname = sss[0];
		attstr = sss[1];
		posstr = sss[2];
		/*
		   if (i<=0)
		   {
		 tagname = curinxp ;
		   }
		   else
		   {
		 tagname = curinxp.substring (0,i) ;
		 cond = curinxp.substring (i) ;
		 StringTokenizer st = new StringTokenizer (cond,"[]",false) ;
		 String tmps = null ;
		 while (st.hasMoreTokens())
		 {
		  tmps = st.nextToken () ;
		  //if (tmps.length()==1 && COND_DIV.indexOf(tmps.charAt(0))>=0 )
		  if (tmps.indexOf('@')>=0)
		   attstr = tmps ; //[@aaa="bbb"]
		  else
		   posstr = tmps ; //[3] [last()]
		 }
		   }
		 */
		if (c == '/')
		{ //case '//'
			AbstractNode[] tmpe = getDesChildElement(curele, tagname);
			return filterByCond(tmpe, attstr, posstr);
		}
		else
		{
			AbstractNode[] tmpe = getCurChildElement(curele, tagname);
			return filterByCond(tmpe, attstr, posstr);
		}
	}

	/**
	 * ��ĳ���ڵ���зֽ⡪���ֽ�Ϊtagname+attrstr+position
	 *@param inxp in xpath content
	 *@param res [0]=tagname [1]=attrstr [2]=position
	 */
	private static void parseInXP(String inxp, String[] res)
	{
		//parse current element
		int i = inxp.indexOf('[');
		String tagname = null;
		String cond = null;
		String attstr = null;
		String posstr = null;

		if (i <= 0)
		{
			tagname = inxp;
		}
		else
		{
			tagname = inxp.substring(0, i);
			cond = inxp.substring(i);
			StringTokenizer st = new StringTokenizer(cond, "[]", false);
			String tmps = null;
			while (st.hasMoreTokens())
			{
				tmps = st.nextToken();
				//if (tmps.length()==1 && COND_DIV.indexOf(tmps.charAt(0))>=0 )
				if (tmps.indexOf('@') >= 0)
				{
					attstr = tmps; //[@aaa="bbb"]
				}
				else
				{
					posstr = tmps; //[3] [last()]
				}
			}
		}
		res[0] = tagname;
		res[1] = attstr;
		res[2] = posstr;
	}

	/**
	 * ��Ԫ�������е�Ԫ�ظ������Ժ�λ����Ϣ���й���
	 * ��{@link parseInXp(Element,String) parseInXp}ʹ��
	 */
	private static AbstractNode[] filterByCond(AbstractNode[] sor,
											   String attrstr,
											   String posstr)
		throws Exception
	{
		AbstractNode[] tmpe = sor;

		if (tmpe == null)
		{
			return null;
		}

		if (posstr != null)
		{
			try
			{
				int pos = Integer.parseInt(posstr);
				if (pos > tmpe.length || pos <= 0)
				{
					return null;
				}
				tmpe = new AbstractNode[]
					{
					tmpe[pos - 1]};
			}
			catch (NumberFormatException nfe)
			{
				throw new Exception("Invalid position [" + posstr + "]!");
			}
		}

		Hashtable htAttr = getHashAttribute(attrstr);
		if (tmpe.length == 1)
		{
			if (isSatisfyAttr(tmpe[0], htAttr))
			{
				return tmpe;
			}
			else
			{
				return null;
			}
		}

		Vector v = new Vector();
		int k;
		for (k = 0; k < tmpe.length; k++)
		{
			if (isSatisfyAttr(tmpe[k], htAttr))
			{
				v.add(tmpe[k]);
			}
		}

		int s = v.size();
		tmpe = new AbstractNode[s];
		for (k = 0; k < s; k++)
		{
			tmpe[k] = (AbstractNode) v.elementAt(k);
		}
		return tmpe;
	}

	/**
	 * �������������ַ���ת��ΪHash��ı�ʾ��ʽ
	 */
	private static Hashtable getHashAttribute(String attr)
		throws Exception
	{
		if (attr == null)
		{
			return null;
		}
		//log ("attr========"+attr);
		Hashtable tmpht = new Hashtable();
		//@aa="bb"  @aa="bb"|@cc="dd"  @aa  @aa|@bb
		StringTokenizer st = new StringTokenizer(attr, "|");
		String tmps = null;
		int i;
		while (st.hasMoreTokens())
		{
			tmps = st.nextToken().trim();
			if (tmps.charAt(0) != '@')
			{
				throw new Exception("invalid attribute [" + attr +
									"] less of '@'!");
			}

			StringTokenizer tmpst = null;
			if (tmps.indexOf('\"') < 0)
			{
				tmpst = new StringTokenizer(tmps, "@=\" ", false);
			}
			else
			{
				tmpst = new StringTokenizer(tmps, "@=\"", false);
			}
			String attrn = "";
			String attrv = "";
			if (tmpst.hasMoreTokens())
			{
				attrn = tmpst.nextToken().trim();
			}
			else
			{
				throw new Exception("invalid attribute [.." + tmps + "..] !");
			}

			if (tmpst.hasMoreTokens())
			{
				attrv = tmpst.nextToken();

			}
			tmpht.put(attrn, attrv);
		}

		return tmpht;
	}

	/**
	 * �ж�ĳһԪ���е����������Ƿ�����һ����������
	 */
	private static boolean isSatisfyAttr(AbstractNode ele, Hashtable attr)
		throws
		Exception
	{
		if (attr == null)
		{
			return true;
		}

		//NamedNodeMap nnm = ele.getAttributes();
//ele.getAttribute()
		String attrn = null, attrv = null;
		for (Enumeration en = attr.keys(); en.hasMoreElements(); )
		{
			attrn = (String) en.nextElement();
			attrv = (String) attr.get(attrn);

//			Node tmpn = nnm.getNamedItem(attrn);
//			if (tmpn == null)
//			{
//				return false;
//			}

			if (!attrv.equals(""))
			{
				if (!attrv.equals(ele.getAttribute(attrn)))
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * �õ���ǰԪ�ص����е���Ԫ�أ�����Ԫ�ض���ָ���ı�ǩ��
	 *@param ele ��ǰԪ��
	 *@param tagname ��ǩ����"*" �������еı�ǩ��
	 *@return Element[] Ԫ������
	 */
	public static AbstractNode[] getCurChildElement(AbstractNode ele,
		String tagname)
	{
		if (ele == null)
		{
			return null;
		}

		boolean isall = false;
		if (tagname.equals("*"))
		{
			isall = true;
		}
		Vector v = new Vector();
		int cc = ele.getChildCount();
		for (int i = 0; i < cc; i++)
		{
			AbstractNode an = (AbstractNode) ele.getChildAt(i);
			if (isall || an.getNodeName().equals(tagname))
			{
				v.addElement(an);
			}
		}
		Object[] obs = v.toArray();
		AbstractNode[] retns = new AbstractNode[obs.length];
		System.arraycopy(obs, 0, retns, 0, obs.length);
		return retns;
	}

	/**
	 * �õ���ǰԪ�ص����еĺ����Ԫ�أ�����Ԫ�ض���ָ���ı�ǩ��
	 *@param ele ��ǰԪ��
	 *@param tagname ��ǩ����"*" �������еı�ǩ��
	 *@return Element[] Ԫ������
	 */
	public static AbstractNode[] getDesChildElement(AbstractNode ele,
		String tagname)
	{
		if (ele == null)
		{
			return null;
		}
		Vector v = new Vector();
		int cc = ele.getChildCount();
		for (int i = 0; i < cc; i++)
		{
			getMeDesChildEles( (AbstractNode) ele.getChildAt(i), tagname, v);
		}
		Object[] obs = v.toArray();
		AbstractNode[] retns = new AbstractNode[obs.length];
		System.arraycopy(obs, 0, retns, 0, obs.length);
		return retns;
	}

	/**
	 * �õ��������Ƶ����к���ӽڵ㣬����������㣬������Լ�
	 * @param ele
	 * @param tagname
	 * @param v
	 */
	private static void getMeDesChildEles(AbstractNode ele, String tagname,
										  Vector v)
	{
		if (ele == null)
		{
			return;
		}
		if (ele.getNodeName().equals(tagname))
		{
			v.addElement(ele);
		}
		int cc = ele.getChildCount();
		for (int i = 0; i < cc; i++)
		{
			getMeDesChildEles( (AbstractNode) ele.getChildAt(i), tagname, v);
		}
	}

	/*
	 * xp point to a node,so it will find its fathor first.
	 * if fathor exist,then add node with two case:<br/>
	 * 	1,node has no position information--add the last<br/>
	 * 	2,node has position information,then insert into specail
	 *    position and if position is bigger than count+1,then insert
	 *    some empty nodes first.
	  private Element addNode(String xp, boolean isreplace)
	 throws Exception
	  { //log ("toadd add="+xp);
	 String fatherxp = xpathNodeFathorPath(xp);
	 int i = xp.lastIndexOf('/');
	 String nodename = xp.substring(i + 1);
	 //log ("tobu add father="+xp);
	 Element[] tmpe = parse(superroot, fatherxp);
	 if (tmpe == null || tmpe.length == 0)
	 {
	  throw new Exception("cannot find (" + xp + ")node's fathor!");
	 }
	 if (tmpe.length != 1)
	 {
	  throw new Exception("(" + xp + ")node's father is not only one!");
	 }
	 String tagname = null;
	 String attstr = null;
	 String posstr = null;
	 String sss[] = new String[3];
	 parseInXP(nodename, sss);
	 tagname = sss[0];
	 attstr = sss[1];
	 posstr = sss[2];
	 //log ("pos========="+posstr);
	 Element newele = document.createElement(tagname);
	 String tmps = null;
	 if (attstr != null)
	 {
	  Hashtable htattr = getHashAttribute(attstr);
	  for (Enumeration enum = htattr.keys(); enum.hasMoreElements(); )
	  {
	   tmps = (String) enum.nextElement();
	   newele.setAttribute(tmps, (String) htattr.get(tmps));
	  }
	 }
	 //add elemnt first
	 if (posstr == null || posstr.equals(""))
	 { //add tail
	  tmpe[0].appendChild(newele);
	  return newele;
	 }
	 //has position information
	 try
	 {
	  int pos = Integer.parseInt(posstr) - 1;
	  String brotherxp = xp.substring(0, i + 1) + "*";
	  Element[] brothereles = parse(superroot, brotherxp);
	  if (pos < brothereles.length)
	  { //insert or replace
	   if (isreplace || brothereles[pos].getNodeName().equals("empty"))
	   {
	 tmpe[0].replaceChild(newele, brothereles[pos]);
	   }
	   else
	   {
	 tmpe[0].insertBefore(newele, brothereles[pos]);
	   }
	  }
	  else if (pos == brothereles.length)
	  {
	   tmpe[0].appendChild(newele);
	  }
	  else
	  { //insert empty first
	   int emptynum = pos - brothereles.length;
	   for (int p = 0; p < emptynum; p++)
	   {
	 tmpe[0].appendChild(document.createElement("empty"));
	   }
	   tmpe[0].appendChild(newele);
	  }
	 }
	 catch (NumberFormatException nfe)
	 {
	  throw new Exception("invalid xpath [" + xp +
	 "],it has invalid position!");
	 }
	 return newele;
	  }
	 */
	/*
	 * ɾ��xpath��ָ��Ԫ��
	 *@param xp xpath ָ��Ψһ���������е�Ԫ��
	 *@return Element ���ر�ɾ����Ԫ�ض���
	  private Element delNode(String xp)
	 throws Exception
	  {
	 //log ("tobu del="+xp);
	 Element[] dn = parse(superroot, xp);
	 if (dn == null || dn.length == 0)
	 {
	  return null;
	 }
	 if (dn.length != 1)
	 {
	  throw new Exception("the node xpath=[" + xp +
	 "] point to is not only one!");
	 }
	 String fatherxp = xpathNodeFathorPath(xp);
	 int i = xp.lastIndexOf('/');
	 String nodename = xp.substring(i + 1);
	 //log ("fatther="+fatherxp);
	 Element[] tmpe = parse(superroot, fatherxp);
	 if (tmpe == null || tmpe.length == 0)
	 {
	  throw new Exception("cannot find node's fathor!");
	 }
	 if (tmpe.length != 1)
	 {
	  throw new Exception("node's father is not only one!");
	 }
	 return (Element) tmpe[0].removeChild(dn[0]);
	  }
	 */
	public static void log(AbstractNode[] ele)
	{
		/*
		  System.out.println ("Element-->");
		  if (ele==null)
		   System.out.print ("null");
		  for (int k = 0 ; k < ele.length ; k ++)
		   System.out.print ("|"+ele[k].getNodeName());
		  System.out.println ("\n____________");*/
	}

	public static void log(String str)
	{
		System.out.println(str);
	}

	private String xpathNodeFathorPath(String xp)
		throws Exception
	{
		/*
		   int k = xp.indexOf (rootName) ;
		   if (k>=0)
		 xp = xp.substring (k+rootName.length()) ;
		 */
		int k = xp.indexOf(rootName);

		if (k < 0)
		{
			if (xp.charAt(1) != '/')
			{
				xp = ("/" + rootName + xp);
			}
		}

		int i = xp.lastIndexOf('/');
		int j = xp.lastIndexOf('@');

		if (i < j && xp.charAt(j - 1) != '[')
		{
			throw new Exception("invalid xpath [" + xp +
								"],it isn't point to nodes.");
		}

		if (i < 2)
		{
			throw new Exception("invalid xpath [" + xp +
								"],it has no fathor node.");
		}

		String tmps = xp.substring(0, i);

		if (tmps.endsWith("/"))
		{
			return tmps + "/*";
		}
		else
		{
			return tmps;
		}
	}

	//change attribute
	public String xpathNodePath(String xp)
		throws Exception
	{
		if (xp.charAt(0) != '/')
		{
			throw new Exception("invalid xpath (" + xp + "),no '/' header!");
		}
		int k = xp.indexOf(rootName);
		/*
		   if (k>=0)
		 xp = xp.substring (k+rootName.length()) ;
		 */
		if (k < 0)
		{
			if (xp.charAt(1) != '/')
			{
				xp = ("/" + rootName + xp);
			}
		}

		int i = xp.lastIndexOf('/');
		int j = xp.lastIndexOf('@');
		if (i < j && xp.charAt(j - 1) != '[')
		{ //do get attribute
			String tmps = xp.substring(0, j);
			if (tmps.endsWith("/"))
			{
				return tmps + "*";
			}
			else
			{
				return tmps;
			}
		}
		else
		{ //do get node text
			return xp;
		}
	}

	public static String xpathAttribute(String xp)
	{
		int i = xp.lastIndexOf('/');
		int j = xp.lastIndexOf('@');
		if (i < j && xp.charAt(j - 1) != '[')
		{ //do get attribute
			String tmps = xp.substring(j + 1);
			if (tmps == null || tmps.equals(""))
			{
				return null;
			}
			else
			{
				return tmps;
			}
		}
		else
		{ //do get node text
			return null;
		}
	}
	
	public static String xpathNode(String xp)
		throws Exception
	{
		if (xp.charAt(0) != '/')
		{
			throw new Exception("invalid xpath (" + xp + "),no '/' header!");
		}
		
		int i = xp.lastIndexOf('/');
		int j = xp.lastIndexOf('@');
		if (i < j && xp.charAt(j - 1) != '[')
		{ //do get attribute
			String tmps = xp.substring(0, j);
			if (tmps.endsWith("/"))
			{
				return tmps + "*";
			}
			else
			{
				return tmps;
			}
		}
		else
		{ //do get node text
			return xp;
		}
	}

	/**
	 * �õ���root��ʼ��ָ���ڵ�Ľڵ�����0��λ�ô���root
	 * @param node
	 * @return
	 */
	private AbstractNode[] getNodeChain(AbstractNode node)
	{
		if (node == null || node == superroot)
		{
			return null;
		}
		if (node == root)
		{
			return new AbstractNode[]
				{
				root};
		}

		Vector v = new Vector();
		AbstractNode tmpan = node;
		v.insertElementAt(tmpan, 0);
		while (true)
		{
			tmpan = (AbstractNode) tmpan.getParent();

			v.insertElementAt(tmpan, 0);
			if (tmpan == root)
			{
				break;
			}
		}

		int s = v.size();
		AbstractNode[] retns = new AbstractNode[s];
		System.arraycopy(v.toArray(), 0, retns, 0, s);
		return retns;
	}

	/**
	 * ���ݽڵ��ö�Ӧ��XPath��
	 * @param node
	 * @return
	 */
	public String getXPathByNode(AbstractNode node)
		throws Exception
	{
		if (node == null)
		{
			return "";
		}
		AbstractNode[] ans = getNodeChain(node);
		if (ans == null)
		{
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("/").append(ans[0].getNodeName());
		for (int i = 1; i < ans.length; i++)
		{
			//AbstractNode pn = ans[i - 1];
			sb.append("/").append(ans[i].getNodeName());
			AbstractNode[] brothers = getAllNode(sb.toString());
			if (brothers == null || brothers.length == 0)
			{
				throw new RuntimeException("Get Path Failed:[" + sb.toString() +
										   "] has no nodes found!");
			}
			if (brothers.length == 1)
			{
				continue;
			}
			int j = 0;
			for (j = 0; j < brothers.length; j++)
			{
				if (brothers[j] == ans[i])
				{
					sb.append("[").append( (j + 1)).append("]");
					break;
				}
			}

			if (j == brothers.length)
			{
				throw new RuntimeException("Get Path Failed:[" + sb.toString() +
										   "] has not be matched nodes!");
			}
		}
		return sb.toString();
	}

	/*
	 * get all attributes or text values that xp point to.
	 * �õ�xpath��ָ�����е�����ֵ��Ԫ�ص�Text�ڵ�ֵ
	 *@param xp xpath ���ָ�����ԣ���ȡ����ֵ������ֵ�����ָ��Ԫ�أ���ȡ��Text�ڵ�ֵ
	 *@return String[] �ַ�������ķ�ʽ����
	  public String[] getAll(String xp)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 AbstractNode[] tmpe = parse(superroot, path);
	 //log (tmpe) ;
	 Vector v = new Vector();
	 if (attn == null)
	 { //get node text
	  for (int k = 0; k < tmpe.length; k++)
	  {
	   String tmps = getText(tmpe[k]);
	   if (tmps != null && !tmps.equals(""))
	   {
	 v.add(tmps);
	   }
	  }
	 }
	 else
	 {
	  for (int k = 0; k < tmpe.length; k++)
	  {
	   String tmps = getAttrValue(tmpe[k], attn);
	   if (tmps != null && !tmps.equals(""))
	   {
	 v.add(tmps);
	   }
	  }
	 }
	 int s = v.size();
	 String[] rets = new String[s];
	 for (int k = 0; k < s; k++)
	 {
	  rets[k] = (String) v.elementAt(k);
	 }
	 return rets;
	  }
	 */

	/**
	 * �õ�xpath��Ψһָ���Ԫ��
	 *@param xp point to only one node
	 */
	public AbstractNode getNode(String xp)
		throws Exception
	{
		String path = xpathNodePath(xp);
		String attn = xpathAttribute(xp);

		if (attn != null)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to node!");
		}

		AbstractNode[] tmpe = parse(superroot, path);
		if (tmpe == null || tmpe.length == 0)
		{
			return null;
		}

		if (tmpe.length != 1)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to only one node[1]!");
		}

		return tmpe[0];
	}

	public AbstractNode[] getAllNode(String xp)
		throws Exception
	{
		String path = xpathNodePath(xp);
		String attn = xpathAttribute(xp);

		if (attn != null)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to node!");
		}

		return parse(superroot, path);
	}

	/**
	 * ��ȡ�Ե�ǰԪ�ؿ�ʼ����������ǰԪ�أ������������Ԫ��
	 */
	public static AbstractNode[] getElementAfter(AbstractNode tmproot,
												 String xp)
		throws
		Exception
	{
		//String path = xpathNodePath (xp) ;
		String attn = xpathAttribute(xp);

		if (attn != null)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to node!");
		}

		AbstractNode[] tmpe = parse(tmproot, xp);
		return tmpe;
	}

	/**
	 * ����һ��ָ���ĸ��ڵ㣬�����·��
	 * ��ö�Ӧ���ı�����
	 * 
	 * ·������ָ��ڵ㣬Ҳ����ָ������
	 * @param tmproot
	 * @param xp
	 * @return
	 * @throws Exception
	 */
	public String getFirstNodeOrAttrTxtAfter(AbstractNode tmproot,String xp) throws Exception
	{
		String attp = xpathNode(xp);
		String attn = xpathAttribute(xp);
		
		AbstractNode[] tmpe = parse(tmproot, attp);
		if(tmpe==null||tmpe.length<=0)
			return null ;
		
		if (attn != null)
		{
			return tmpe[0].getAttribute(attn) ;
		}
		else
		{
			return tmpe[0].toString(false) ;
		}
	}
	/*
	 * get first attribute or text value that xp point to.
	 * �õ���һ��xpath��ָ����ֵ��Ԫ�ص�Text�ڵ�ֵ
	 *@param xp xpath ���ָ�����ԣ���ȡ����ֵ������ֵ�����ָ��Ԫ�أ���ȡ��Text�ڵ�ֵ
	 *@return String �ַ����ķ�ʽ����
	  public String get(String xp)
	 throws Exception
	  {
	 String[] tmps = getAll(xp);
	 if (tmps == null || tmps.length == 0)
	 {
	  return null;
	 }
	 else
	 {
	  return tmps[0];
	 }
	  }
	 */
	/**
	 * �õ�xpath��ָԪ�ص��ӽڵ��ǩ��
	 *@param xp xpath must point only one element.
	 *@return String [] null if father (xp) elemetn is not exist.
	 */
	public String[] getChildTagNames(String xp)
		throws Exception
	{
		String path = xpathNodePath(xp);
		String attn = xpathAttribute(xp);

		if (attn != null)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to node!");
		}

		AbstractNode[] tmpe = parse(superroot, path);
		if (tmpe == null || tmpe.length == 0)
		{
			return null;
		}

		if (tmpe.length != 1)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to only one node[2]!");
		}

		path = path + "/*";
		tmpe = parse(superroot, path);
		if (tmpe == null || tmpe.length == 0)
		{
			return new String[0];
		}

		String[] tmps = new String[tmpe.length];
		for (int k = 0; k < tmpe.length; k++)
		{
			tmps[k] = tmpe[k].getNodeName();
		}
		return tmps;
	}

	/*
	 * �õ�xpath��ָ��Ԫ�ص��ַ�����ʾ
	 * the xpath point to only one node
	 */
	public String getNodeStr(String xp)
		throws Exception
	{
		String path = xpathNodePath(xp);
		String attn = xpathAttribute(xp);
		if (attn != null)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to node!");
		}
		AbstractNode[] tmpe = parse(superroot, path);
		if (tmpe == null || tmpe.length == 0)
		{
			return null;
		}
		if (tmpe.length != 1)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to only one node[3]!");
		}
		return tmpe[0].toString(false);
	}

	/**
	 * ����xpath(ָ��ڵ������)����ö�Ӧ��url
	 * @param xp
	 * @return
	 * @throws Exception
	 */
	public String getSmartStr(String xp) throws Exception
	{
		String path = xpathNodePath(xp);
		String attn = xpathAttribute(xp);
		
		AbstractNode[] tmpe = parse(superroot, path);
		if (tmpe == null || tmpe.length == 0)
		{
			return null;
		}
		if (tmpe.length != 1)
		{
			throw new Exception("invalid xpath=(" + xp +
								"),it isn't point to only one node[3]!");
		}
		
		if (attn != null)
			return tmpe[0].getAttribute(attn) ;
		else
			return tmpe[0].toString(false);
	}
	/*
	 * set attribute that xp point to.
	 * ��xpath��ָ���������������ֵ
	 * <b>Note:set the attribute that xp point to.</b><br/>
	 * <b>Note:the element that the xp point to must only one</b>
	 *@param xp xpath��ָ�����ԣ�
	 *@param attrval ����ֵ
	 *@return return the old value
	  synchronized public String setAttr(String xp, String attrval)
	 throws
	 Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn == null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to attribute!");
	 }
	 Element[] tmpe = parse(superroot, path);
	 if (tmpe == null || tmpe.length == 0)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],cannot find any element!");
	 }
	 if (tmpe.length != 1)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],the element is not only one!");
	 }
	 String tmps = tmpe[0].getAttribute(attn);
	 setAttrValue(tmpe[0], attn, attrval);
	 return tmps;
	  }
	 */
	/*
	 * add attribute that xp point to.
	 * ��xpath��ָ���������������ֵ�����ӣ�
	 * <b>Note:set the first attribute that xp point to.</b><br/>
	 * <b>Note:the element that the xp point to must only one</b>
	 *@param xp xpath��ָ�����ԣ�
	 *@param attrval ����ֵ
	  synchronized public void addAttr(String xp, String attrval)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn == null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to attribute!");
	 }
	 Element[] tmpe = parse(superroot, path);
	 if (tmpe == null || tmpe.length == 0)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],cannot find any element!");
	 }
	 if (tmpe.length != 1)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],the element is not only one!");
	 }
	 String tmps = getAttrValue(tmpe[0], attn);
	 if (tmps != null && !tmps.equals(""))
	 {
	  //tmps += ("|" + attrval) ;
	  throw new Exception("the att xpath=[" + xp +
	 "],has already existed.");
	 }
	 else
	 {
	  //setAttrValue (tmpe[0],attn,attrval) ;
	  setAttrValue(tmpe[0], attn, attrval);
	 }
	  }
	 */
	/*
	 * remove attribute that xp point to.
	 * ɾ������
	 * <b>Note:set the first attribute that xp point to.</b><br/>
	 * <b>Note:the element that the xp point to must only one</b>
	 *@param xp xpath��ָ�����ԣ�
	 *@return return the old value
	  synchronized public String removeAttr(String xp)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn == null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to attribute!");
	 }
	 Element[] tmpe = parse(superroot, path);
	 if (tmpe == null || tmpe.length == 0)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],cannot find any element!");
	 }
	 if (tmpe.length != 1)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],the element is not only one!");
	 }
	 String tmps = getAttrValue(tmpe[0], attn);
	 removeAttrValue(tmpe[0], attn);
	 return tmps;
	  }
	 */
	/*
	 * add element into xml tree.
	 * ������Ԫ��
	 * <b> xp must point to element.</b>
	 *@param xp xpath��ָ��Ԫ�أ�
	  synchronized public Element addElement(String xp)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn != null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to node!");
	 }
	 //Element eee = null ;
	 return addNode(path, false);
	  }
	 */
	/*
	 * add attrbute with create new element
	 * ����һ����Ԫ�ؽڵ㣬������һ������
	 * <b> xp must point to attribute.</b>
	 *@param xp xpathָ�����Բ�����ֵ �磺/webbase[@name=wb1]/adapters/adapter@name=TCP
	 *@return Element �����´�����Ԫ�ض���
	  synchronized public Element addAttrWithNewElement(String xp)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn == null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to attr!");
	 }
	 Element eee = addNode(path, false);
	 int i = attn.indexOf('=');
	 if (i > 0)
	 {
	  //String
	  String attval = attn.substring(i + 1).trim();
	  //log ("--attval="+attval);
	  if (attval.charAt(0) == '\"' &&
	   attval.charAt(attval.length() - 1) == '\"')
	  {
	   //log ("triming \"....");
	   attval = attval.substring(1, attval.length() - 1);
	  }
	  //log ("add attn="+attn+" attval="+attval) ;
	  eee.setAttribute(attn.substring(0, i), attval);
	 }
	 else
	 {
	  throw new Exception(
	   "the xpath must has attribute value!like (/webbase[@name=wb1]/adapters/adapter@name=TCP)"
	   +
	   "\n\tor you can use addAttrWithNewElement(String,String) do it"
	   );
	 }
	 return eee;
	  }
	 */
	/*
	 * add attrbute with create new element
	 * ����һ����Ԫ�ؽڵ㣬������һ������
	 * <b> xp must point to attribute.</b>
	 *@param xp xpathָ������
	 *@return Element �����´�����Ԫ�ض���
		 synchronized public Element addAttrWithNewElement(String xp, String attrv)
	 throws
	 Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn == null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to attr!");
	 }
	 Element eee = addNode(path, false);
	 int i = attn.indexOf('=');
	 if (i > 0)
	 {
	  throw new Exception("the xpath can't have attribute value!");
	 }
	 else
	 {
	  eee.setAttribute(attn, attrv);
	 }
	 return eee;
	  }
	 */
	/*
	 * remove only one node
	 *ɾ��xpath��ָ��Ԫ��
	 *@param xp xpath
	 *@return Element ���ر�ɾ��Ԫ�ض���
	  synchronized public Element removeElement(String xp)
	 throws Exception
	  {
	 String path = xpathNodePath(xp);
	 String attn = xpathAttribute(xp);
	 if (attn != null)
	 {
	  throw new Exception("invalid xpath=[" + xp +
	 "],it isn't point to node!");
	 }
	 return delNode(path);
	  }
	 */
	/*
	 * remove only one node or remove only one attribute
	 *@param xp xpath
	 *@return String if remove node it return null,if remove nothing
	 return "",if remove attribute return attribute value
	 */
	/*
	  synchronized public String remove (String xp)
	   throws Exception
	  {
	   String path = xpathNodePath (xp) ;
	   String attn = xpathAttribute (xp) ;
	   if (attn==null)
	   {//remove node
	 delNode (path) ;
	 return null ;
	   }
	   else
	   {//remove attribute
	 Element [] tmpe = parse (superroot,path) ;
	 if (tmpe==null||tmpe.length==0)
	  return "";
	 if (tmpe.length!=1)
	  throw new Exception ("the node xpath=["+xp+"(pt:attribute)] point to is not only one!") ;
	 String attv = tmpe[0].getAttribute (attn) ;
	 if (attv==null||attv.equals(""))
	  return "" ;
	 tmpe[0].removeAttribute (attn) ;
	 return attv ;
	   }
	  }*/
	/**
	 * no implement yet (perhaps it isn't necessary)
	 */
	synchronized public void removeAll(String xp)
	{

	}

	/*
	 * Element��String��ʽ�����ӽڵ�
		 * @param fatherxp xpath that point to only element that will be added child
	 * @param strElement a string that will to be element.
	 * @return Element return new element that to be created and added.
	  synchronized public Element addElementWithStr(String fatherxp,
	 String strElement)
	 throws
	 Exception
	  {
	 try
	 {
	  DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.
	   newInstance();
	  docBuilderFactory.setNamespaceAware(false);
	  docBuilderFactory.setValidating(false);
	  DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	  StringReader sr = new StringReader("" + strElement);
	  InputSource is = new InputSource(sr);
	  Document doc = docBuilder.parse(is);
	  Element tobeaddele = doc.getDocumentElement();
	  //Element tobeaddeleold = (Element)doc.removeChild(tobeaddele) ;
	  Element tmpe = getElement(fatherxp);
	  return graftElement(tmpe, tobeaddele);
	 }
	 catch (Exception e)
	 {
	  e.printStackTrace();
	  throw new Exception(e.toString());
	 }
	  }
	 */
	/*
	 * Element��String��ʽ���滻�ڵ�
		 * @param fatherxp xpath that point to only element that will be added child
	 * @param xpath xpath that point to element to be replaced.
	 * @param strElement a string that will to be element.
	 * @return Element return new element that to be created and added.
	  synchronized public Element replaceElementWithStr(String fatherxp,
	 String xpath, String strElement)
	 throws Exception
	  {
	 try
	 {
	  DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.
	   newInstance();
	  docBuilderFactory.setNamespaceAware(false);
	  docBuilderFactory.setValidating(false);
	  DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	  StringReader sr = new StringReader("" + strElement);
	  InputSource is = new InputSource(sr);
	  Document doc = docBuilder.parse(is);
	  Element tobeaddele = doc.getDocumentElement();
	  //Element tobeaddeleold = (Element)doc.removeChild(tobeaddele) ;
	  Element tmpe = getElement(fatherxp);
	  Element tmpee = getElement(xpath);
	  if (tmpee != null)
	  {
	   tmpe.removeChild(tmpee);
	  }
	  return graftElement(tmpe, tobeaddele);
	 }
	 catch (Exception e)
	 {
	  e.printStackTrace();
	  throw new Exception(e.toString());
	 }
	  }
	 */
	/*
	 * ��n2��ֲ(����)��n1��һ���ӽڵ�
	 * @return Element �µ��ӽڵ�
	  private Element graftElement(Element n1, Element n2)
	 throws Exception
	  {
	 try
	 {
	  String tag = n2.getTagName();
	  Hashtable attrs = getElementAttributes(n2);
	  Document doc = n1.getOwnerDocument();
	  Element newroot = (Element) doc.createElement(tag);
	  String key = null;
	  for (Enumeration enum = attrs.keys(); enum.hasMoreElements(); )
	  {
	   key = (String) enum.nextElement();
	   newroot.setAttribute(key, (String) attrs.get(key));
	  }
	  n1.appendChild(newroot);
	  NodeList nl = n2.getChildNodes();
	  for (int i = 0; true; i++)
	  {
	   Node node = nl.item(i);
	   if (node == null)
	   {
	 break;
	   }
	   short s = node.getNodeType();
	   switch (s)
	   {
	 case Node.COMMENT_NODE:
	  Comment c = doc.createComment(node.getNodeValue());
	  newroot.appendChild(c);
	  break;
	 case Node.ELEMENT_NODE:
	  graftElement(newroot, (Element) node);
	  break;
	 case Node.CDATA_SECTION_NODE:
	  CDATASection cs = doc.createCDATASection(node.
	   getNodeValue());
	  newroot.appendChild(cs);
	  break;
	 case Node.ENTITY_REFERENCE_NODE:
	  EntityReference er = doc.createEntityReference(node.
	   getNodeName());
	  newroot.appendChild(er);
	  break;
	 case Node.TEXT_NODE:
	  Text t = doc.createTextNode(node.getNodeValue());
	  newroot.appendChild(t);
	  break;
	  //case Node.
	   }
	  }
	  return newroot;
	 }
	 catch (DOMException e)
	 {
	  throw new Exception(e.toString());
	 }
	  }
	 */
	/*
	 * �ѱ�����󱣴浽�ļ��У���ʹ�ñ������е��ļ���
	  public void saveToFile()
	 throws Exception, IOException
	  {
	 if (defaultFileName == null || defaultFileName.equals(""))
	 {
	  throw new Exception("No Default File Name to be set!");
	 }
	 File f = new File(defaultFileName);
	 FileOutputStream fos = new FileOutputStream(f);
	 StringBuffer tmpsb = new StringBuffer(
	  "<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
	 elementToString(getRootElement(), tmpsb);
	 fos.write(tmpsb.toString().getBytes());
	 fos.close();
	  }
	 */
	/*
	 * �ѱ�����󱣴浽�ļ���
	  public void saveToFile(String filename)
	 throws IOException
	  {
	 File f = new File(filename);
	 FileOutputStream fos = new FileOutputStream(f);
	 StringBuffer tmpsb = new StringBuffer(
	  "<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
	 elementToString(getRootElement(), tmpsb);
	 fos.write(tmpsb.toString().getBytes());
	 fos.close();
	  }
	 */
	/*
	 *
	  public static void saveToFile(XHtmlPath xw, String filename)
	 throws
	 IOException
	  {
	 File f = new File(filename);
	 FileOutputStream fos = new FileOutputStream(f);
	 StringBuffer tmpsb = new StringBuffer(
	  "<?xml version=\"1.0\" encoding=\"gb2312\"?>\n");
	 elementToString(xw.getRootElement(), tmpsb);
	 fos.write(tmpsb.toString().getBytes());
	 fos.close();
	  }
	 */
	/**
	 * �ѽ���е��漰ʵ�����õ��ַ�ת��Ϊʵ��
	 */
	private static String xmlEncoding(String input)
	{
		String entitystr = "><&\'\"\r\n";
		StringTokenizer tmpst = new StringTokenizer(input, entitystr, true);
		StringBuffer tmpsb = new StringBuffer(input.length() + 100);
		String tmps = null;
		while (tmpst.hasMoreTokens())
		{
			tmps = tmpst.nextToken();
			if (tmps.length() == 1 && entitystr.indexOf(tmps) >= 0)
			{
				switch (tmps.charAt(0))
				{
					case '<':
						tmpsb.append("&lt;");
						break;
					case '>':
						tmpsb.append("&gt;");
						break;
					case '&':
						tmpsb.append("&amp;");
						break;
					case '\'':
						tmpsb.append("&apos;");
						break;
					case '\"':
						tmpsb.append("&quot;");
						break;
					case '\n':
						tmpsb.append("&#10;");
						break;
					case '\r':
						tmpsb.append("&#13;");
						break;
				}
			}
			else
			{
				tmpsb.append(tmps);
			}
		}

		return tmpsb.toString();
	}

	/*
	 private static void elementToString(Element ele, StringBuffer buf)
	 {
	  String tagname = ele.getTagName();
	  buf.append("<" + tagname);
	  NamedNodeMap nnm = ele.getAttributes();
	  int len = nnm.getLength();
	  Node tmpn = null;
	  for (int k = 0; k < len; k++)
	  {
	   tmpn = nnm.item(k);
	   String tmpnn = tmpn.getNodeName();
	   String tmpnv = tmpn.getNodeValue();
	   if (tmpnv != null)
	   {
	 buf.append(" " + tmpnn + "=\"" + xmlEncoding(tmpnv) + "\"");
	 //else
	 //buf.append (" "+tmpnn+"=\"\"") ;
	   }
	  }
	  Element[] tmpes = getChildElement(ele);
	  if (tmpes == null || tmpes.length == 0)
	  {
	   buf.append("/>");
	   return;
	  }
	  buf.append(">");
	  for (int i = 0; i < tmpes.length; i++)
	  {
	   elementToString(tmpes[i], buf);
	  }
	  buf.append("</" + tagname + ">");
	 }
	 */
	/**
	 * ������ǩ����������������ֵ���ڵ�ֵ��������ؼ��ֵ����нڵ�
	 * @param str
	 * @return
	 */
	public AbstractNode[] searchAll(AbstractNode tmproot, String key)
	{
		Vector v = new Vector();
		searchAll(tmproot, key, v);
		int s = v.size();
		AbstractNode[] ans = new AbstractNode[s];
		Object[] obs = v.toArray();
		System.arraycopy(obs, 0, ans, 0, s);
		return ans;
	}

	private void searchAll(AbstractNode tmproot, String key, Vector buf)
	{
		if (tmproot instanceof XmlNode)
		{
			String key0 = key.toLowerCase();
			if (tmproot.getNodeName().indexOf(key0) >= 0)
			{ //�жϱ������
				buf.addElement(tmproot);
			}
			else
			{
				Enumeration en = tmproot.getAtributeNames();
				if (en != null)
				{
					while (en.hasMoreElements())
					{
						String attrn = (String) en.nextElement();
						if (attrn.indexOf(key0) >= 0)
						{ //�ж���������
							buf.addElement(tmproot);
							break;
						}
						String av = tmproot.getAttribute(attrn);
						if (av.indexOf(key0) >= 0)
						{ //�ж�����ֵ
							buf.addElement(tmproot);
							break;
						}
					}
				}
			}
		}
		else if (tmproot instanceof XmlText)
		{
			XmlText xt = (XmlText) tmproot;
			String xts = xt.getText() ;
			if (xts.indexOf(key) >= 0)
			{
				buf.addElement(tmproot);
			}
		}
		int cc = tmproot.getChildCount();
		for (int i = 0; i < cc; i++)
		{
			AbstractNode an = (AbstractNode) tmproot.getChildAt(i);
			searchAll(an, key, buf);
		}

	}

////////////////////////////////////////////////////////
	public static void main(String args[])
		throws Exception
	{
		InputStream tmpis = null;
		try
		{
			if ("file".equalsIgnoreCase(args[0]))
			{
				tmpis = new FileInputStream(args[1]);
			}
			else if ("url".equalsIgnoreCase(args[0]))
			{
				URL u = new URL(args[1]);
				URLConnection conn = u.openConnection();

				conn.connect();

				tmpis = conn.getInputStream();
			}
			else
			{
				System.out.println("Input the type!");
				return;
			}

			XHtmlPath xw = new XHtmlPath(tmpis);

			String inputLine;
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				System.in));
			while ( (inputLine = in.readLine()) != null)
			{
				try
				{
					int i = inputLine.indexOf(' ');
					String cmd = null;
					String param = null;
					if (i >= 0)
					{
						cmd = inputLine.substring(0, i);
						param = inputLine.substring(i + 1);
					}
					else
					{
						cmd = inputLine.trim();
					}

					if (cmd.equals("getall"))
					{
//						String[] tmps = xw.getAll(param);
//						for (i = 0; i < tmps.length; i++)
//						{
//							System.out.println(tmps[i]);
//						}
					}
					else if (cmd.equals("reload"))
					{
//						tmpis = args[0].getClass().getResourceAsStream("/" +
//							args[0]);
//						doc = docBuilder.parse(tmpis);
//						xw.setDocument(doc);
					}
					else if (cmd.equals("addelement"))
					{
						//xw.addElement(param);
					}
					else if (cmd.equals("addattr"))
					{
						param = param.trim();
						int p = param.indexOf(' ');
						//xw.addAttr(param.substring(0, p), param.substring(p + 1));
					}
					else if (cmd.equals("save"))
					{
						File f = new File(param);
						FileOutputStream fos = new FileOutputStream(f);
						//fos.write(xw.getRootElement().toString().getBytes());
						fos.close();
					}
					else if (cmd.equals("removeele"))
					{
//						Element ele = xw.removeElement(param);
//						System.out.println(ele.toString() + "\nHas removed!!!!");
					}
					else if (cmd.equals("removeattr"))
					{
//						String oldval = xw.removeAttr(param);
//						System.out.println(param + "=" + oldval +
//										   "\nHas removed!!!!");
					}
					else if (cmd.equals("include"))
					{
						param = param.trim();
						int p = param.indexOf(' ');
						System.out.println(xw.xpathInclude(param.substring(0, p),
							param.substring(p + 1)));
					}
					else if (cmd.equals("setatt"))
					{
//						int s = param.indexOf(' ');
//						xw.setAttr(param.substring(0, s), param.substring(s + 1));
					}
					else if (cmd.equals("element"))
					{
						System.out.println(xw.getNodeStr(param));
					}
					else if (cmd.equals("childtags"))
					{
						String tmps[] = xw.getChildTagNames(param);
						if (tmps == null)
						{
							System.out.println("null father element!");
							return;
						}
						for (i = 0; i < tmps.length; i++)
						{
							System.out.println(tmps[i]);
						}
					}
					else if (cmd.equals("eleattrname"))
					{
//						String tmps[] = xw.getElementAllAttrNames(param);
//						for (i = 0; i < tmps.length; i++)
//						{
//							System.out.println(tmps[i]);
//						}
					}
					else if (cmd.equals("addAttrWithNewElement"))
					{
						//xw.addAttrWithNewElement(param);
					}
					else if (cmd.equals("addElementWithStr"))
					{
//						param = param.trim();
//						int p = param.indexOf(' ');
//						xw.addElementWithStr(param.substring(0, p),
//											 param.substring(p + 1));
					}
					else if (cmd.equals("getNodeAfter"))
					{
						param = param.trim();
						int p = param.indexOf(' ');
						AbstractNode ele = xw.getNode(param.substring(0, p));
						AbstractNode[] eles = getElementAfter(ele,
							param.substring(p + 1));
						for (int pp = 0; pp < eles.length; pp++)
						{
							System.out.println("****");
							System.out.println(eles[pp].toString());
						}
					}
					else if (cmd.equals("getAllAncestorNodeXPath"))
					{
//						param = param.trim();
//						String[] tmps = xw.getAllAncestorNodeXPath(param, true);
//						// System.out.println (tmps.length) ;
//						for (int j = 0; j < tmps.length; j++)
//						{
//							System.out.println(tmps[j]);
//						}
					}
					else if (cmd.equals("doc"))
					{
						//System.out.println(xw.getDocument().toString());
					}
				}
				catch (Exception ce)
				{
					ce.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (tmpis != null)
			{
				tmpis.close();
			}
		}
	}
}