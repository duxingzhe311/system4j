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
	 * XORM Gdb的测试代码,它根据自己所在的环境,进行数据库表的建立 增删改数据 如果成功,最终又把表删除
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
			extinfo.setParamValue("xx", "xx中文");
			int v = 123;
			extinfo.setParamValue("zz问", v);
			GDBTest tc = new GDBTest("filen中文", 100, "内容xxx".getBytes(),
					GDBTest.Level.pub, "查看权限123", true, extinfo);
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
	 * 文件id
	 */
	@XORMProperty(name = "FileId", has_col = true, is_pk = true, is_auto = true)
	long fileId = -1;

	/**
	 * 文件名称
	 */
	@XORMProperty(name = "FileName", has_col = true, has_idx = true, max_len = 100, order_num = 1)
	String fileName = null;

	/**
	 * 所属分类id
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
	 * 文件添加日期
	 */
	@XORMProperty(name = "AddDate", has_col = true, order_num = 10)
	Date addDate = new Date();

	/**
	 * 文件最后更新日期
	 */
	@XORMProperty(name = "LastUpdateDate", has_col = true, order_num = 20)
	Date lastUpdateDate = null;

	/**
	 * 文件是继承其所在类的权限，还是拥有自己的权限
	 */
	@XORMProperty(name = "IsSelfRight", has_col = true, order_num = 21)
	boolean bSelfRight = false;

	/**
	 * 文件内容
	 */
	@XORMProperty(name = "FileContent", has_col = true, order_num = 30)
	byte[] filecontent = null;

	/**
	 * 文件查看权限－－只能看到文件列表
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
	 * 获得文件名称
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * 获得文件Id
	 */
	public long getFileId()
	{
		return fileId;
	}

	/**
	 * 获得文件所属分类Id
	 */
	public long getSectionId()
	{
		return sectionId;
	}

	/**
	 * 获得文件内容
	 */
	public byte[] getFileContent()
	{
		return filecontent;
	}

	/**
	 * 获得文件rightlevel
	 */
	public Level getFileRightLevel()
	{
		return rightLevel;
	}

	/**
	 * 获得文件是否继承父类权限
	 */
	public boolean getFileSelfRight()
	{
		return bSelfRight;
	}

	/**
	 * 返回文件查看权限
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
