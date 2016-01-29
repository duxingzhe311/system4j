package com.dw.system.dict;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.*;

import javax.servlet.jsp.PageContext;

import org.w3c.dom.Element;

import com.dw.system.AppConfig;
import com.dw.system.Convert;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.util.Sorter;
import com.dw.system.xmldata.XmlHelper;

/**
 * �����ֵ�Ľڵ�
 * 
 * @author Jason Zhu
 */
public class DataNode implements Comparable<DataNode>
{
	/**
	 * �ڵ�Ψһid
	 */
	@XORMProperty(name="DnId")
	int id = -1;

	/**
	 * Ψһ����
	 */
	@XORMProperty(name="DnName")
	String name = null ;
	
	/**
	 * ���ڵ�
	 */
	@XORMProperty(name="DnParentId")
	int parentNodeId = -1;

	transient int belongToClassId = -1 ;

	transient String belongToModule = null ;
	
	
	transient Element relatedEle = null ;
	/**
	 * ��������
	 */
	@XORMProperty(name="DnNameCn")
	String nameCn = null;

	/**
	 * Ӣ������
	 */
	@XORMProperty(name="DnNameEn")
	String nameEn = null;

	/**
	 * �����ַ�������Ӧ���Ե�����,��cn-�ͻ����� en=Custom Mgr
	 */
	HashMap<String,String> lang2name = new HashMap<String,String>() ;

	/**
	 * �����
	 */
	@XORMProperty(name="DnOrderNo")
	int orderNo = -1;

	/**
	 * �Ƿ�ɼ�
	 */
	boolean bVisiable = true;

	/**
	 * �Ƿ��ֹ
	 */
	boolean bForbidden = false;

	/**
	 * �Ƿ���ȱʡ�Ľڵ�--���ѡ��ʱ,�����ֵû������ֵ����ƥ��.��ʹ�øýڵ�
	 */
	boolean bDefault = false;

	/**
	 * ����ʱ��
	 */
	Date createTime = new Date();

	/**
	 * �ϴθ���ʱ��
	 */
	Date lastUpdateTime = new Date();

	/**
	 * 
	 */
	ArrayList<DataNode> childNodes = new ArrayList<DataNode>();

	DataNode parentNode = null;

	DataNode[] validChildNodes = null;

	DataNode[] validOffspringNodes = null;

	HashMap<String,String> extendAttrMap = new HashMap<String,String>();
	
	@XORMProperty(name="DnExtAttrMapStr")
	private void set_ExtAttrMapStr(String s) throws IOException
	{
		if(Convert.isNullOrTrimEmpty(s))
			return ;
		
		s = s.trim() ;
		StringReader sr = new StringReader(s);
		BufferedReader br = new BufferedReader(sr);
		String l = null ;
		while((l=br.readLine())!=null)
		{
			l = l.trim() ;
			if("".equals(l))
				continue ;
			
			int p = l.indexOf('=') ;
			if(p<0)
			{
				extendAttrMap.put(l, "") ;
			}
			else
			{
				extendAttrMap.put(l.substring(0,p).trim(), l.substring(p+1).trim()) ;
			}
		}
	}
	private String get_ExtAttrMapStr() throws IOException
	{
		if(extendAttrMap==null)
			return "" ;
		
		StringBuilder sb = new StringBuilder();
		for(Map.Entry<String, String> n2v:extendAttrMap.entrySet())
		{
			sb.append(n2v.getKey()).append('=').append(n2v.getValue()).append("\r\n") ;
		}
		return sb.toString() ;
		
	}
	
	transient long autoId = -1 ;
	
	public DataNode()
	{}
	
	public DataNode(long autoid,int id,int pid,String name,
			String namecn,String nameen,
			int ordern,boolean bvis,boolean bforbid,
			Date createt,Date lastup_date,
			HashMap<String,String> extinfo,boolean bdefault)
	{
		this.autoId = autoid ;
		this.id = id ;
		this.parentNodeId = pid ;
		this.name = name ;
		
		this.nameCn = namecn;
		if(namecn!=null)
			lang2name.put("cn", namecn) ;
		
		this.nameEn = nameen;
		if(nameen!=null)
			lang2name.put("en", nameen);
		
		this.orderNo = ordern;
		bVisiable = bvis;
		bForbidden = bforbid;
		createTime = createt;
		lastUpdateTime = lastup_date;
		if(extinfo!=null)
		{
			for(Map.Entry<String,String> n2v:extinfo.entrySet())
			{
				String n = n2v.getKey() ;
				String v = n2v.getValue() ;
				
				extendAttrMap.put(n, v) ;
				
				if(n.startsWith("name_"))
				{
					String langn = n.substring(5);
					lang2name.put(langn, v);
				}
			}
		}
		
		bDefault = bdefault ;
	}
	
	public DataNode(int id,int pid,String name,
			String namecn,String nameen,
			int ordern,
			HashMap<String,String> extinfo)
	{
		this(-1,id,pid,name,namecn,nameen,ordern,true,false,new Date(),new Date(),extinfo,false) ;
	}
	
	public DataNode(int id,int pid,String name,
			String namecn,String nameen,
			int ordern,boolean bvis,boolean bforbid,
			Date createt,Date lastup_date,
			HashMap<String,String> extinfo)
	{
		this(-1,id,pid,name,namecn,nameen,ordern,bvis,bforbid,new Date(),new Date(),extinfo,false) ;
	}

	public DataNode(Element ele,DataNode pn)
	{
		relatedEle = ele ;
		
		String strid = ele.getAttribute("id") ;
		if(strid!=null&&!strid.equals(""))
			id = Integer.parseInt(strid);
		
		name = ele.getAttribute("name");
		
		nameCn = ele.getAttribute("name_cn");
		nameEn = ele.getAttribute("name_en");
		
		Properties ps = XmlHelper.getElementAttributes(ele) ;
		for(Enumeration tmpen =ps.propertyNames();tmpen.hasMoreElements();)
		{
			String n0 = (String)tmpen.nextElement() ;
			String v0 = ps.getProperty(n0);
			if(n0.startsWith("name_"))
			{
				String langn = n0.substring(5);
				lang2name.put(langn, v0);
			}
			
			extendAttrMap.put(n0, v0);
		}
		
		bVisiable = !("0".equals(ele.getAttribute("visible"))||"false".equals(ele.getAttribute("visible")));
		bForbidden = "1".equals(ele.getAttribute("forbidden"))||"true".equals(ele.getAttribute("forbidden"));
		parentNode = pn ;
		if(parentNode!=null)
			parentNodeId = pn.id;
		
		
		Element[] eles = XmlHelper.getSubChildElement(ele,"dd_node");
		if(eles==null)
			return ;
		
		for(Element tmpe:eles)
		{
			childNodes.add(new DataNode(tmpe,this));
		}
		
		bDefault = "true".equalsIgnoreCase(ele.getAttribute("default")) ;
	}
	
	//public DataNode copy
	
	public Element getRelatedEle()
	{
		return relatedEle ;
	}
	
	public long getAutoId()
	{
		return autoId ;
	}

	void appendChildNode(DataNode dn)
	{
		if (dn == this)
			throw new IllegalArgumentException();

		if (dn.parentNode == this)
			return;

		dn.RemoveFromParent();

		dn.parentNode = this;
		dn.parentNodeId = this.id;

		childNodes.add(dn);

		Sorter.sort(childNodes);
	}

	void RemoveChildNode(DataNode dn)
	{
		if (dn == this)
			throw new IllegalArgumentException();

		if (dn.parentNode != this)
			throw new IllegalArgumentException();

		this.childNodes.remove(dn);

		dn.parentNode = null;
		dn.parentNodeId = -1;
	}

	void RemoveFromParent()
	{
		if (this.parentNode != null)
			parentNode.RemoveChildNode(this);
	}

	public int getId()
	{
		return id;
	}
	
	

	/**
	 * �õ��ڱ����е�Ψһ����
	 * @return
	 */
	public String getName()
	{
		return name ;
	}
	// / <summary>
	// / ��������
	// / </summary>
	public String getNameCN()
	{
		return nameCn;
	}

	// / <summary>
	// / Ӣ������
	// / </summary>
	public String getNameEn()
	{
		return nameEn;
	}

	// / <summary>
	// / �����������ͻ�ö�Ӧ������
	// / </summary>
	// / <param name="lang"></param>
	// / <returns></returns>
	public String getNameByLang(String lang)
	{
		if(Convert.isNullOrEmpty(lang))
			return getName() ;
		
		return lang2name.get(lang);
	}
	
	/**
	 * ����Jsp PageContext�������Ļ�ö�Ӧ���Ե�����
	 * 
	 * �÷�����Ҫ����֧���ڶ����Ի����е�jspҳ����,�����չʾ�Ķ�����֧��
	 * @param pc
	 * @return
	 */
	public String getNameByJspPageContext(PageContext pc)
	{
		String lan = AppConfig.getAppLang(pc);
		return getNameByLang(lan);
	}
	
	public String getPathNameCn(String path_sep)
	{
		return getPathNameByLang("cn",path_sep) ;
	}
	
	public String getPathNameEn(String path_sep)
	{
		return getPathNameByLang("en",path_sep) ;
	}
	/**
	 * �������Ի�ñ��ڵ�Ӹ���ʼ��·���ַ���
	 * @param lang
	 * @param path_sep ·���ָ,�� / - ��
	 * @return
	 */
	public String getPathNameByLang(String lang,String path_sep)
	{
		StringBuilder sb = new StringBuilder();
		getPathNameByLang(sb,lang,path_sep) ;
		return sb.toString();
	}
	
	private void getPathNameByLang(StringBuilder sb,String lang,String path_sep)
	{
		if(this.parentNode==null)
		{
			sb.append(getNameByLang(lang));
			return ;
		}
		
		parentNode.getPathNameByLang(sb, lang, path_sep);
		
		sb.append(path_sep).append(getNameByLang(lang));
	}

	// / <summary>
	// / �����
	// / </summary>
	public int getOrderNo()
	{
		return orderNo;
	}

	// / <summary>
	// / ���ֵ����еĲ�Σ������Ϊ1������ֱ�Ӻ���Ϊ2
	// / </summary>
	public int getLevel()
	{
		if (this.parentNode == null)
			return 1;

		return parentNode.getLevel() + 1;
	}

	// / <summary>
	// / �Ƿ�ɼ�
	// / </summary>
	public boolean isVisiable()
	{
		return bVisiable;
	}

	// / <summary>
	// / �Ƿ��ֹ
	// / </summary>
	public boolean isForbidden()
	{
		return bForbidden;
	}

	// / <summary>
	// / ����ʱ��
	// / </summary>
	public Date getCreateTime()
	{
		return createTime;
	}

	// / <summary>
	// / �ϴθ���ʱ��
	// / </summary>
	public Date getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public boolean isDefaultNode()
	{
		return bDefault ;
	}

	public DataNode getRootNode()
	{
		if (parentNode == null)
			return this;

		return parentNode.getRootNode();
	}
	
	public int getBelongToClassId()
	{
		return getRootNode().belongToClassId ;
	}
	
	public DataClass getBelongToClass()
	{
		if(Convert.isNotNullEmpty(belongToModule))
			return DictManager.getInstance().getDataClass(belongToModule,this.belongToClassId) ;
		else
			return DictManager.getInstance().getDataClass(this.belongToClassId) ;		
	}

	public DataNode getParentNode()
	{
		return parentNode;
	}
	
	
	public boolean checkIsAncestor(DataNode dn)
	{
		if(belongToClassId!=dn.belongToClassId)
			return false;
		
		if(this.id==dn.id)
			return false;
		
		DataNode pdn = this ;
		while((pdn = pdn.getParentNode())!=null)
		{
			if(pdn.id==dn.id)
				return true ;
		}
		
		return false;
	}
	
	public int getParentNodeId()
	{
		return parentNodeId ;
	}

	public boolean hasChild()
	{
		return childNodes.size() > 0;
	}

	public boolean isLeaf()
	{
		return !hasChild();
	}
	// / <summary>
	// / ����ڵ�
	// / </summary>
	public DataNode[] getChildNodes()
	{
		DataNode[] rets = new DataNode[childNodes.size()];
		return childNodes.toArray(rets);
	}

	// / <summary>
	// / �����Ч���ӽڵ�
	// / </summary>
	public DataNode[] getValidChildNodes()
	{
		if (validChildNodes != null)
			return validChildNodes;

		ArrayList<DataNode> tmpal = new ArrayList<DataNode>();
		for (DataNode dn : childNodes)
		{
			if (dn.isForbidden())
				continue;

			if (dn.isVisiable())
				tmpal.add(dn);
		}

		// tmpal.sort() ;

		DataNode[] rets = new DataNode[tmpal.size()];
		return tmpal.toArray(rets);
	}

	// / <summary>
	// / �������ڵ�
	// / </summary>
	private DataNode[] getOffspringNodes()
	{
		throw new RuntimeException("not impl");
	}

	public DataNode[] getValidOffspringNodes()
	{
		if (validOffspringNodes != null)
			return validOffspringNodes;

		ArrayList<DataNode> tmpal = new ArrayList<DataNode>();
		getValidOffspringNodes(this, tmpal);

		DataNode[] rets = new DataNode[tmpal.size()];
		return tmpal.toArray(rets);
	}

	private void getValidOffspringNodes(DataNode dn, ArrayList<DataNode> al)
	{
		for (DataNode tmpdn : dn.childNodes)
		{
			if (tmpdn.isForbidden())
				continue;

			if (tmpdn.isVisiable())
				al.add(tmpdn);

			getValidOffspringNodes(tmpdn, al);
		}
	}

	// / <summary>
	// / �õ��������id�����м���,�ָ�
	// / </summary>
	// / <returns></returns>
	public String getSearchIdsStr()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(this.id);
		DataNode[] dns = getValidOffspringNodes();
		for (int i = 0; i < dns.length; i++)
		{
			sb.append(',').append(dns[i].id);
		}

		return sb.toString();
	}

	/**
	 * �õ�����ֵ
	 * @param attrn
	 * @return
	 */
	public String getAttrValue(String attrn)
	{
		if("id".equals(attrn))
			return ""+this.id ;
		
		return getExtendedAttr(attrn);
	}
	// / <summary>
	// / �����������ƻ����չ����ֵ�����÷������Ի��xml�ֵ��ļ��еĽڵ���������
	// / �����ֵ�����ṩ�������չ����
	// / </summary>
	// / <param name="attrname"></param>
	// / <returns></returns>
	public String getExtendedAttr(String attrname)
	{
		return extendAttrMap.get(attrname);
	}
	
	public Set<String> getExtendedAttrNames()
	{
		return extendAttrMap.keySet() ;
	}
	
	public void setExtendedAttr(String attrn,String attrv)
	{
		extendAttrMap.put(attrn, attrv);
	}
	/**
	 * �õ�δ�������չ�����ı�--������̨�༭����֮��
	 * @return
	 * @throws IOException
	 */
	public String getUndefinedExtAttrMapStr() throws IOException
	{
		if(extendAttrMap==null)
			return "" ;
		
		StringBuilder sb = new StringBuilder();
		DataClass dc = getBelongToClass() ;
		for(Map.Entry<String, String> n2v:extendAttrMap.entrySet())
		{
			String n = n2v.getKey() ;
			if(dc.isContainDefExtName(n))
				continue ;//�ų��������չ����
			sb.append(n).append('=').append(n2v.getValue()).append("\r\n") ;
		}
		return sb.toString() ;
		
	}

	public void writeTo(Writer tw) throws IOException
	{
		int lv = this.getLevel();
		for (int i = 0; i < lv; i++)
		{
			tw.write("  ");
		}

		tw.write("(" + lv + ") ");
		tw.write("[" + id + "]");
		if (nameCn != null)
			tw.write(nameCn);
		if (nameEn != null)
			tw.write(nameEn);
		tw.write(getSearchIdsStr());
		tw.write("\r\n");

		tw.flush();
		
		DataNode[] cdns = this.getChildNodes();
		for (int i = 0; i < cdns.length; i++)
		{
			cdns[i].writeTo(tw);
		}
	}

	public String toLvlString(String onelvl_prefix)
	{
		StringBuilder tmpsb = new StringBuilder();

		int lvl = this.getLevel();
		for (int i = 1; i < lvl; i++)
		{
			tmpsb.append(onelvl_prefix);
		}

		tmpsb.append(toString());

		return tmpsb.toString();
	}

	public String toString()
	{
		return "(" + this.getOrderNo() + ")[id=" + this.getId() + "][namecn="
				+ this.getNameCN() + "][nameen=" + this.getNameEn() + "]";
	}

	public int compareTo(DataNode o)
	{
		return this.orderNo - o.orderNo;
	}

}