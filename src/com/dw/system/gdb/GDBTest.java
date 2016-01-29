package com.dw.system.gdb;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import com.dw.system.gdb.xorm.XORMClass;
import com.dw.system.gdb.xorm.XORMProperty;
import com.dw.system.xmldata.XmlData;

@XORMClass(table_name = "t_xorm_test")
public class GDBTest
{
	/*
	 * XORM Gdb�Ĳ��Դ���,�������Լ����ڵĻ���,�������ݿ��Ľ��� ��ɾ������ ����ɹ�,�����ְѱ�ɾ��
	 * 
	 */
	public static void XORMTest(PrintWriter out)
	{
		try
		{
			GDB g = GDB.getInstance();
			out.println("install db xorm class!\r\n");
			List<String> sqls = g.installByXORMClassTable((String) null,
					GDBTest.class);
			for (String sql : sqls)
				out.println(" [sql]->" + sql);

			out.println(" [install succ]");

			out.println(" [insert data->");
			XmlData extinfo = new XmlData();
			extinfo.setParamValue("xx", "xx����");
			int v = 123;
			extinfo.setParamValue("zz��", v);
			GDBTest tc = new GDBTest("filen����", 100, "����xxx".getBytes(),
					GDBTest.Level.pub, "�鿴Ȩ��123", true, extinfo);
			long newid = (Long) g.addXORMObjWithNewId(tc);
			out.println(" [insert succ with new id=" + newid);

			out.println(" [get data by id->" + newid);
			GDBTest tc1 = (GDBTest) g.getXORMObjByPkId(GDBTest.class, newid);
			if (tc1 != null)
			{
				out.println(" [get data by id succ]");
				XmlData tmpxd = tc1.getExtInfo();
				if (extinfo.toXmlString().equals(tmpxd.toXmlString()))
					out.println(" check xmldata field equal!!");
				else
					out.println(" check xmldata field not equal!!");
				
				out.println(" get add date=="+tc1.addDate);
				out.println(" get last up date=="+tc1.lastUpdateDate+" "+(tc1.lastUpdateDate==null?"<succ>":"<failed>"));
			}

			out.println("drop db xorm class!\r\n");
			g.dropXORMClassTable((String) null, GDBTest.class);

			out.println(" [drop succ]");
		}
		catch (Exception e)
		{
			e.printStackTrace(out);
		}
	}

	public static enum Level
	{
		pub(0), need_login(1);

		final int val;

		Level(int i)
		{
			val = i;
		}

		public int getIntValue()
		{
			return val;
		}

		public static Level valueOfInt(int i)
		{
			switch (i)
			{
			case 0:
				return pub;
			case 1:
				return need_login;
			default:
				throw new IllegalArgumentException("unknow leve val=" + i);
			}
		}
	}

	/**
	 * �ļ�id
	 */
	@XORMProperty(name = "FileId", has_col = true, is_pk = true, is_auto = true)
	long fileId = -1;

	/**
	 * �ļ�����
	 */
	@XORMProperty(name = "FileName", has_col = true, has_idx = true, max_len = 100, order_num = 1)
	String fileName = null;

	/**
	 * ��������id
	 */
	@XORMProperty(name = "SectionId", has_col = true, is_auto = true)
	long sectionId = -1;

	Level rightLevel = Level.pub;

	@XORMProperty(name = "RightLevel", has_col = true)
	private int get_Level()
	{
		return rightLevel.getIntValue();
	}

	private void set_Level(int i)
	{
		rightLevel = Level.valueOfInt(i);
	}

	/**
	 * �ļ��������
	 */
	@XORMProperty(name = "AddDate", has_col = true, order_num = 10)
	Date addDate = new Date();

	/**
	 * �ļ�����������
	 */
	@XORMProperty(name = "LastUpdateDate", has_col = true, order_num = 20)
	Date lastUpdateDate = null;

	/**
	 * �ļ��Ǽ̳����������Ȩ�ޣ�����ӵ���Լ���Ȩ��
	 */
	@XORMProperty(name = "IsSelfRight", has_col = true, order_num = 21)
	boolean bSelfRight = false;

	/**
	 * �ļ�����
	 */
	@XORMProperty(name = "FileContent", has_col = true, order_num = 30)
	byte[] filecontent = null;

	/**
	 * �ļ��鿴Ȩ�ޣ���ֻ�ܿ����ļ��б�
	 */
	@XORMProperty(name = "ViewRight")
	String viewRight = null;

	@XORMProperty(name = "ExtInfo")
	XmlData extInfo = null;

	public GDBTest()
	{
	}

	public GDBTest(String fname, long fsectionId, byte[] fcontent,
			Level frightlevel, String viewright, boolean selfright,
			XmlData extinfo)
	{
		if (fname == null || fname.equals(""))
			throw new IllegalArgumentException("fill cannot be null!");

		this.fileName = fname;
		this.sectionId = fsectionId;
		this.filecontent = fcontent;
		this.rightLevel = frightlevel;
		this.viewRight = viewright;
		this.bSelfRight = selfright;
		this.extInfo = extinfo;

	}

	/**
	 * ����ļ�����
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * ����ļ�Id
	 */
	public long getFileId()
	{
		return fileId;
	}

	/**
	 * ����ļ���������Id
	 */
	public long getSectionId()
	{
		return sectionId;
	}

	/**
	 * ����ļ�����
	 */
	public byte[] getFileContent()
	{
		return filecontent;
	}

	/**
	 * ����ļ�rightlevel
	 */
	public Level getFileRightLevel()
	{
		return rightLevel;
	}

	/**
	 * ����ļ��Ƿ�̳и���Ȩ��
	 */
	public boolean getFileSelfRight()
	{
		return bSelfRight;
	}

	/**
	 * �����ļ��鿴Ȩ��
	 */
	public String getViewRight()
	{
		if (viewRight == null)
			return "";

		return viewRight;
	}

	public XmlData getExtInfo()
	{
		return extInfo;
	}

	public String toString()
	{
		return "id=" + sectionId + " viewr=" + viewRight;
	}

}
