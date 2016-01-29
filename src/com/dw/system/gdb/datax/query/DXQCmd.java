package com.dw.system.gdb.datax.query;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.datax.*;
import com.dw.system.xmldata.*;

/**
 * DataX Query�����ܳ�����
 * 
 * �ó�����ȿ���֧��DataX���������,Ҳ���Կ���֧����ͨ�Ĺ�ϵ���ݿ����
 * 
 * ÿ�������е��漰Xml���ݿ��еĽṹ����ʱ,ʹ��{/path}��ʽ ���� {/name} {/finance_info/bank_account}
 * 
 * ÿ�������е�����������£� {@/path:type=default_strval} ���������������{@��ͷ,��}����,@���������·�� ��{@/name},���typeû��ָ��,���Զ���Ϊ��string
 * �������û��=����,����Ϊ���������û��ȱʡֵ,����ͨ�������XmlData�л�ȡ
 * 
 * ���������=����,��û��ָ��default_strval,����Ϊ��ָ��ȱʡֵnull.
 * ���ָ����default_strval��,����ʹ�õ������Ƿ���type�޶�������.��typeΪint32ʱ,�� default_strvalӦ��Ϊ����
 * 
 * �ر��typeΪstringʱ(ȱʡ),default_strvalӦ�������� 'xxx'�ķ�ʽ����,����ַ�������',��д��'' ����,�ַ�����ֵ
 * abc'kk' Ӧ��д�� 'abc''kk'''
 * 
 * @author Jason Zhu
 */
public abstract class DXQCmd implements IDXQCmd
{
	protected static String WHITE_SPACE = " \r\n\t";

	protected DataXManager dataxMgr = null;

	protected String strCmd = null;

	/**
	 * ֮���Խ�����������Ϣ������Ϊ���Ա��������������ʱ����ʵʱ��� ׼ȷ�Ķ����������������Ļ��屣�棬��������
	 */
	private String dataxBaseName = null;

	private String dataxClassName = null;

	// protected DataXBase dataxBase = null ;
	// protected DataXClass dataxClass = null ;

	protected XmlDataStruct inputXSD = new XmlDataStruct();// XmlDataStruct.EMPTY_STRUCT;

	protected DXQCmd(String strcmd)
	{
		strCmd = strcmd;
	}

	/**
	 * �����ַ���������й���
	 * 
	 * @param strcmd
	 */
	public DXQCmd(DataXManager dataxmgr, String strcmd)
	{
		dataxMgr = dataxmgr;
		strCmd = strcmd;
	}

	void init(String datax_base,String datax_class,String leftstr)
	{
		dataxBaseName = datax_base;
		dataxClassName = datax_class;
		
		
		// ��ȡ���������
//		ArrayList<String> inputpaths = new ArrayList<String>();
//		extractIdxAndInputPath(leftstr, null, inputpaths);
		List<InputParam> ips = extractAllInputParam(leftstr);
		if(ips!=null)
		{
			for (InputParam ip : ips)
			{
				boolean bnullable = ip.hasDefaultVal();
				
				inputXSD.setXmlDataMemberByPath(ip.getPath(), ip.getType(), false, bnullable,
					Integer.MAX_VALUE);
			}
		}

		

		parseSelf(leftstr);
	}

	protected DataXClass getRelatedDataXClass()
	{
		return dataxMgr.getDataXClass(dataxBaseName, dataxClassName);
	}

	protected abstract void parseSelf(String leftstr);

	
	protected static String[] parseCmdAndDataXClass(String inputstr,int startp,
			int[] end_pos,StringBuffer failedreson)
	{
		if (inputstr == null)
			return null;

		int len = inputstr.length();
		if (startp < 0)
		{
			failedreson.append("start pos < 0 ");
			return null;// throw new IllegalArgumentException("p is <0");
		}
		if (startp>= len)
		{
			failedreson.append("start pos is out of input str len ");
			return null;// throw new IllegalArgumentException("p is out of input
						// str len");
		}

		final int BEFORE_CMD = 1 ;
		final int IN_CMD = 2 ;
		final int AFTER_CMD = 3 ;
		final int IN_DATAX_BASE = 4 ;
		final int AFTER_DATAX_BASE = 5 ;
		final int BEFORE_DATAX_CLASS = 6;
		final int IN_DATAX_CLASS = 7 ;
		
		int p = startp ;
		
		String cmd = "";
		String datax_base = "";
		String datax_class = "";
		int st = BEFORE_CMD ;
		while(p<len)
		{
			char c = inputstr.charAt(p);
			switch(st)
			{
			case BEFORE_CMD:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				cmd = ""+c ;
				st = IN_CMD;
				p ++ ;
				break;
			case IN_CMD:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					st = AFTER_CMD;
					break;
				}
			
				cmd += c ;
				p ++ ;
				break;
			case AFTER_CMD:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				datax_base = ""+c ;
				st = IN_DATAX_BASE;
				p ++ ;
				break;
			case IN_DATAX_BASE:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					st = AFTER_DATAX_BASE;
					break;
				}
				
				if(c=='.')
				{
					p ++ ;
					st = BEFORE_DATAX_CLASS;
					break;
				}
				
				datax_base += c ;
				p ++;
				break;
			case AFTER_DATAX_BASE:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				if(c=='.')
				{
					p ++ ;
					st = BEFORE_DATAX_CLASS;
					break;
				}
				
				failedreson.append("Illegal dataxbase.dataxclass");
				return null ;
			case BEFORE_DATAX_CLASS:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				datax_class = ""+c ;
				st = IN_DATAX_CLASS;
				end_pos[0] = p ;//������ʱ������
				p ++ ;
				break;
			case IN_DATAX_CLASS:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					end_pos[0] = p ;
					return new String[]{cmd,datax_base,datax_class};
				}
				
				datax_class += c ;
				end_pos[0] = p ;//������ʱ������
				p ++ ;
				break;
			default:
				throw new IllegalArgumentException("Unkown parse state");
			}
		}
		
		if(datax_class.equals(""))
		{
			failedreson.append("Illegal cmd dataxbase.dataxclass");
			return null ;
		}
		
		return new String[]{cmd,datax_base,datax_class};
	}
	
	/**
	 * ��������'xxxxx'  "xxxxxx"���ַ������ʽ
	 * @param inputstr
	 * @param startp
	 * @param end_pos
	 * @param failedreson
	 * @return
	 */
	protected static String parseStrExpression(String inputstr,int startp,
			int[] end_pos,StringBuffer failedreson)
	{
		if (inputstr == null)
			return null;

		int len = inputstr.length();
		if (startp < 0)
		{
			failedreson.append("start pos < 0 ");
			return null;// throw new IllegalArgumentException("p is <0");
		}
		if (startp>= len)
		{
			failedreson.append("start pos is out of input str len ");
			return null;// throw new IllegalArgumentException("p is out of input
						// str len");
		}

		int p = startp ;
		char contain_c = inputstr.charAt(p);
		if(contain_c!='\''&&contain_c!='\"')
		{
			failedreson.append("str express must start with \' or \"");
			return null ;
		}
		
		StringBuffer tmpsb = new StringBuffer();
		p ++ ;
		while(p<len)
		{
			char c = inputstr.charAt(p);
			if(c=='\\'&&p+1<len)
			{
				char truec = inputstr.charAt(p+1) ;
				switch(truec)
				{
				case 'r':
					truec = '\r';
					break;
				case 'n':
					truec = '\n';
					break;
				case 't':
					truec = '\t';
					break;
				case '\\':
					truec  ='\\';
					break;
				case '\'':
					truec = '\'';
					break;
				case '\"':
					truec = '\"';
					break;
				default:
					break;
				}
				
				p += 2 ;
				tmpsb.append(truec);
			}
			
			if(c=='\''||c=='\"')
			{
				if(c==contain_c)
				{
					end_pos[0] = p ;
					return tmpsb.toString();
				}
			}
			
			tmpsb.append(c);
			p ++ ;
		}
		
		failedreson.append("cannot find end of string char="+contain_c);
		return null ;
	}
	/**
	 * �������� {/xxx/xxx }�Ľṹ·����Ϣ
	 * @param inputstr
	 * @param startp
	 * @param end_pos
	 * @param failedreson
	 * @return
	 */
	protected static String parseStructPath(String inputstr, int startp,
			int[] end_pos, StringBuffer failedreson)
	{
		if (inputstr == null)
			return null;

		int len = inputstr.length();
		if (startp < 0)
		{
			failedreson.append("start pos < 0 ");
			return null;// throw new IllegalArgumentException("p is <0");
		}
		if (startp>= len)
		{
			failedreson.append("start pos is out of input str len ");
			return null;// throw new IllegalArgumentException("p is out of input
						// str len");
		}

		int p = startp ;
		if(inputstr.indexOf("{/",p)!=p)
		{
			failedreson.append("struct path string must start with {/");
			return null;
		}
		
		p += 2 ;
		StringBuffer tmpsb = new StringBuffer();
		tmpsb.append('/');
		boolean inpath = true ;
		while(p<len)
		{
			char c = inputstr.charAt(p);
			if(WHITE_SPACE.indexOf(c)>=0)
			{
				inpath = false;
				p ++ ;
			}
			
			if(c=='}')
			{
				end_pos[0] = p ;
				return tmpsb.toString();
			}
			
			tmpsb.append(c);
			p++ ;
		}
		
		failedreson.append("cannot find end of struct path char }");
		return null ;
	}
	
	/**
	 * �����ַ�����ָ���Ŀ�ʼλ��,����InputParam
	 *  {@ /path : type = default_strval }
	 * 
	 * {@ /name:string="sadfs\r\n\'a"} {@ /name : string = 'sadfs\r\n\'a' }
	 * @param inputstr
	 * @param p
	 * @param end_pos
	 *            ����ɹ�,��end_pos[0]�е�ֵΪ��������Ľ���λ�� }
	 * @return ����ɹ�,�򷵻�InputParam����,���򷵻�null
	 */
	protected static InputParam parseInputParam(String inputstr, int startp,
			int[] end_pos, StringBuffer failedreson)
	{
		//final int IN_START = 0;
		final int IN_BEFORE_PATH = 1;
		final int IN_PATH = 2;
		final int IN_AFTER_PATH = 3;
		final int IN_BEFORE_TYPE = 4;
		final int IN_TYPE = 5;
		final int IN_AFTER_TYPE = 6;
		final int IN_BEFORE_VAL = 7;
		final int IN_VAL = 8;
		final int IN_AFTER_VAL = 9;

		if (inputstr == null)
			return null;

		int len = inputstr.length();
		if (startp < 0)
		{
			failedreson.append("start pos < 0 ");
			return null;// throw new IllegalArgumentException("p is <0");
		}
		if (startp>= len)
		{
			failedreson.append("start pos is out of input str len ");
			return null;// throw new IllegalArgumentException("p is out of input
						// str len");
		}

		int p = startp;
		if (inputstr.indexOf("{@",p) != p)
		{
			failedreson.append("input param string must start with {@");
			return null;
		}
		
		

		p += 2;
		int st = IN_BEFORE_PATH;
		
		String path = "" ;
		String type = "" ;
		boolean hasdef = false ;
		String def_val = "" ;
		
		//{@ /name : string="sadfs\r\n\'a"} {@ /name : string = 'sadfs\r\n\'a' }
		while (p < len)
		{
			char c = inputstr.charAt(p);
			switch (st)
			{
			case IN_BEFORE_PATH:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				path = ""+c ;
				st = IN_PATH;
				p ++ ;
				break;
			case IN_PATH:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					st = IN_AFTER_PATH ;
					break;
				}
				
				if(c==':')
				{
					p ++ ;
					st = IN_BEFORE_TYPE;
					break;
				}
				if(c=='=')
				{
					p ++ ;
					hasdef = true;
					st = IN_BEFORE_VAL;
					break;
				}
				if(c=='}')
				{
					end_pos[0] = p ;
					return new InputParam(path,type,hasdef,def_val);
				}
				
				path += c ;
				p ++ ;
				break;
			case IN_AFTER_PATH:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				if(c==':')
				{
					p ++ ;
					st = IN_BEFORE_TYPE;
					break;
				}
				if(c=='=')
				{
					p ++ ;
					hasdef = true;
					st = IN_BEFORE_VAL;
					break;
				}
				if(c=='}')
				{
					end_pos[0] = p ;
					return new InputParam(path,type,hasdef,def_val);
				}
				
				failedreson.append("unknow char="+c+" after path");
				return null ;
			case IN_BEFORE_TYPE:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				type = ""+c;
				p ++ ;
				st = IN_TYPE ;
				break;
			case IN_TYPE:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					st = IN_AFTER_TYPE;
					break;
				}
				
				if(c=='=')
				{
					p ++ ;
					hasdef = true;
					st = IN_BEFORE_VAL;
					break;
				}
				
				if(c=='}')
				{
					end_pos[0] = p ;
					return new InputParam(path,type,hasdef,def_val);
				}
				
				type += c ;
				p ++ ;
				break;
			case IN_AFTER_TYPE:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				if(c=='=')
				{
					p ++ ;
					hasdef = true;
					st = IN_BEFORE_VAL;
					break;
				}
				
				if(c=='}')
				{
					end_pos[0] = p ;
					return new InputParam(path,type,hasdef,def_val);
				}
				
				failedreson.append("unknow char="+c+" after type!");
				return null ;
			case IN_BEFORE_VAL:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				if(c=='\''||c=='\"')
				{
					int[] strep = new int[1];
					def_val = parseStrExpression(inputstr,p,
							strep,failedreson) ;
					if(def_val==null)
						return null ;
					
					p = strep[0];
					p ++ ;
					st = IN_AFTER_VAL;
					break;
				}
				
				def_val = ""+c ;
				st = IN_VAL;
				p ++ ;
				break;
			case IN_VAL:
				if(c=='}')
				{
					end_pos[0] = p ;
					def_val = def_val.trim();
					return new InputParam(path,type,hasdef,def_val);
				}
				
				def_val+=c;
				p ++ ;
				break;
			case IN_AFTER_VAL:
				if(WHITE_SPACE.indexOf(c)>=0)
				{
					p ++ ;
					break;
				}
				
				if(c=='}')
				{
					end_pos[0] = p ;
					def_val = def_val.trim();
					return new InputParam(path,type,hasdef,def_val);
				}
				break;
			default:
				failedreson.append("unknow parse state");
				return null;
			}
		}
		// return new InputParam();
		failedreson.append("not finished input param str");
		return null;
	}

	protected static List<InputParam> extractAllInputParam(String inputstr)
	{
		if(inputstr==null)
			return null ;
		
		ArrayList<InputParam> ips = new ArrayList<InputParam>() ;
		int p = 0 ;
		StringBuffer fr = new StringBuffer() ;
		int[] ep = new int[1];
		while((p=inputstr.indexOf("{@",p))>=0)
		{
			InputParam ip = parseInputParam(inputstr, p,ep, fr);
			if(ip==null)
				break ;
			
			p = ep[0] + 1 ;
			ips.add(ip);
		}
		return ips ;
	}
	
	/**
	 * ��������ַ����е�·����Ϣ��ȡ��,���滻Ϊ�����ݿ��б�ʾ�ķ�ʽ
	 * �÷���һ��������where orderby groupby���ַ���ת��Ϊ���ݿ��������ķ�ʽ
	 * @param inputstr
	 * @param paths
	 * @return
	 */
	protected static String transToDBCol(String inputstr,List<String> paths)
	{
		if(inputstr==null)
			return null ;
		
		StringBuffer tmpsb = new StringBuffer();
		int sp = 0,fp = 0 ;
		StringBuffer fr = new StringBuffer() ;
		int[] eps = new int[1];
		while((fp=inputstr.indexOf("{/",sp))>=0)
		{
			String ip = parseStructPath(inputstr, fp,eps, fr);
			if(ip==null)
			{
				//tmpsb.append(inputstr.substring(sp));
				break ;
			}
			
			XmlDataPath xdp = new XmlDataPath(ip);
			String coln = xdp.toColumnName() ;
			
			tmpsb.append(inputstr.substring(sp, fp))
				.append(coln);
			
			sp = eps[0] + 1 ;
			if(paths!=null)
				paths.add(ip);
		}
		
		tmpsb.append(inputstr.substring(sp));
		
		return tmpsb.toString() ;
	}
	/**
	 * ��������ַ����е�����·����ȡ��,����ת��Ϊ��?�滻�ķ�ʽ
	 * �÷���һ��������where֮�������������׼����ת������
	 * @param inputstr
	 * @param ips
	 * @return
	 */
	protected static String transToQuestionMark(String inputstr,List<InputParam> ips)
	{
		if(inputstr==null)
			return null ;
		
		StringBuffer tmpsb = new StringBuffer();
		int sp = 0,fp = 0 ;
		StringBuffer fr = new StringBuffer() ;
		int[] eps = new int[1];
		while((fp=inputstr.indexOf("{@",sp))>=0)
		{
			InputParam ip = parseInputParam(inputstr, fp,eps, fr);
			if(ip==null)
			{
				//tmpsb.append(inputstr.substring(sp));
				break ;
			}
			
			tmpsb.append(inputstr.substring(sp, fp))
				.append('?');
			
			sp = eps[0] + 1 ;
			ips.add(ip);
		}
		
		tmpsb.append(inputstr.substring(sp));
		
		return tmpsb.toString() ;
	}
	/**
	 * ��һ��������ַ�������ȡ���������йص����� �� limit(@{/pageidx},@{/pagesize}) where {/name}
	 * like '%@{/input_n}' ������ȡidx·�� /name ������ȡ����·�� /pageidx /pagesize /input_n
	 * 
	 * @param inputstr
	 * @return
	 */
	protected static void extractIdxAndInputPath(String inputstr,
			List<String> idxpaths, List<String> inputpaths)
	{
		int p = 0;
		while ((p = inputstr.indexOf('{', p)) >= 0)
		{
			if (p < inputstr.length() - 2 && inputstr.charAt(p + 1) == '@'
					&& inputstr.charAt(p + 2) == '/')
			{// input
				int q = inputstr.indexOf('}', p + 1);
				if (q < 0)
					return;

				String tmps = inputstr.substring(p + 2, q).trim();
				if (inputpaths != null)
				{
					if (!inputpaths.contains(tmps))
						inputpaths.add(tmps);
				}
				p = q + 1;
			}
			else if (p < inputstr.length() - 1 && inputstr.charAt(p + 1) == '/')
			{
				int q = inputstr.indexOf('}', p + 1);
				if (q < 0)
					return;

				String tmps = inputstr.substring(p + 1, q).trim();
				if (idxpaths != null)
				{
					if (!idxpaths.contains(tmps))
						idxpaths.add(tmps);
				}
				p = q + 1;
			}
			else
			{
				p++;
			}
		}
	}

	/**
	 * ��ȡ�ַ����еĵ�һ�������������·��
	 * 
	 * @param inputstr
	 * @return
	 */
	protected static String extractFirstPath(String inputstr)
	{
		if (inputstr == null)
			return null;

		int p = inputstr.indexOf("{/");
		if(p>=0)
		{
			int[] ep = new int[1];
			StringBuffer fr = new StringBuffer();
			return parseStructPath(inputstr, p,ep, fr);
		}
		
		p = inputstr.indexOf("{@");
		if (p>=0)
		{
			int[] ep = new int[1];
			StringBuffer fr = new StringBuffer();
			InputParam ip = parseInputParam(inputstr, p,ep, fr);
			if(ip==null)
				return null ;
			
			return ip.getPath();
		}
		
		return null ;
	}

	/**
	 * ��������·���ж�ʹ�õ����ĸ�����
	 * 
	 * @param idxpaths
	 * @return
	 */
	protected DataXIndex judgeUsingIdx(List<String> idxpaths)
	{
		if (idxpaths == null || idxpaths.size() <= 0)
			return null;

		DataXIndex[] dxis = getRelatedDataXClass().getDataXIndexes();
		if (dxis == null)
			return null;

		for (DataXIndex dix : dxis)
		{
			if (!(dix instanceof DataXIndexSVMC))
				continue;

			ArrayList<String> curidxpaths = new ArrayList<String>();
			for (DataXIndex.JavaColumnAStructItem jcsi : dix.getColumnInfos())
			{
				String strp = jcsi.getMemberPath().toString();
				curidxpaths.add(strp);
			}

			boolean b = true;
			for (String tmpp : idxpaths)
			{
				if (!curidxpaths.contains(tmpp))
				{
					b = false;
					break;
				}
			}
			if (b)
				return dix;
		}

		return null;
	}

	/**
	 * ����ַ�����ʾ������,���ƴ�������sql
	 * 
	 * @return
	 */
	public String getCmdStr()
	{
		return strCmd;
	}

	/**
	 * ��������
	 */
	public abstract String getName();

	// public DataXBase getDataXBase()
	// {
	// return dataxBase;
	// }
	//	
	// public DataXClass getDataXClass()
	// {
	// return dataxClass ;
	// }
	/**
	 * ִ������
	 * 
	 * @param inputxd
	 * @return
	 */
	public abstract XmlData doCmd(XmlData inputxd) throws Exception;

	/**
	 * ��������ṹ,���п��ܸ��ݾ�����ı���Ϣ������ȡ
	 * 
	 * @return
	 */
	public XmlDataStruct getInputStruct()
	{
		return inputXSD;
	}

	/**
	 * �������ṹ,��Щ����������н�����ж���,��Щ�ǹ̶��ĸ�ʽ(����ʱ���������idֵ)
	 * 
	 * @return
	 */
	public abstract XmlDataStruct getOutputStruct();

	/**
	 * ���������Ľ��{@/path:type=default_strval}
	 * 
	 * @author Jason Zhu
	 */
	public static class InputParam
	{
		String path = null;

		String type = XmlVal.VAL_TYPE_STR;

		boolean hasDefaultVal = false;

		Object defaultVal = null;

		public InputParam(String path, String t, boolean hasdef_val,
				String default_val_str)
		{
			this.path = path;
			type = t;
			if(type==null||type.equals(""))
				type = XmlVal.VAL_TYPE_STR;
			
			if (!XmlVal.isXmlValType(type))
				throw new IllegalArgumentException("the type=[" + type
						+ "] is not xml val type!");
			this.hasDefaultVal = hasdef_val;

			if (hasDefaultVal)
				defaultVal = strValToObjVal(default_val_str);
		}

		private Object strValToObjVal(String strval)
		{
			if (strval == null)
				return null;

			XmlVal xv = XmlVal.createSingleValByStr(type, strval);
			return xv.getObjectVal();
		}

		public String getPath()
		{
			return path;
		}

		public String getType()
		{
			return type;
		}

		public boolean hasDefaultVal()
		{
			return hasDefaultVal;
		}

		public Object getDefaultVal()
		{
			return defaultVal;
		}
		
		public Object getValueFromXmlData(XmlData xd)
		{
			Object ov = null ;
			if(xd!=null)
				ov = xd.getSingleParamValueByPath(path);
			
			if(ov==null)
			{
				if(hasDefaultVal)
					return defaultVal;
				else
					throw new IllegalArgumentException("cannot get value in input xmldata with path="+path);
			}
			
			return ov ;
		}
	}
}
