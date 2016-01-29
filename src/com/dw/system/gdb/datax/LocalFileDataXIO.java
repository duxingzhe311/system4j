package com.dw.system.gdb.datax;

import java.io.*;
import java.util.*;

import com.dw.system.gdb.datax.*;

public class LocalFileDataXIO implements IDataXIO
{
	File dirBase = null;

	String dirBasePath = null;

	public LocalFileDataXIO(String dirbase) throws Exception
	{
		dirBase = new File(dirbase);
		dirBase.mkdirs();

		dirBasePath = dirBase.getCanonicalPath();
	}

	public synchronized int getNewId() throws Exception
	{
		Properties p = loadId();
		String strcpid = p.getProperty("max_id");
		int v = 0;
		if (strcpid != null && !strcpid.equals(""))
			v = Integer.parseInt(strcpid);

		v++;
		p.setProperty("max_id", "" + v);
		saveId(p);
		return v;
	}

	private Properties loadId() throws IOException
	{
		File f = new File(dirBasePath + "/_id");
		Properties p = new Properties();
		if (f.exists())
		{
			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(f);
				p.load(fis);
			}
			finally
			{
				if (fis != null)
					fis.close();
			}
		}

		return p;
	}

	private void saveId(Properties p) throws IOException
	{
		File f = new File(dirBasePath + "/_id");

		FileOutputStream fos = null;

		try
		{
			fos = new FileOutputStream(f);
			p.store(fos, "");
		}
		finally
		{
			if (fos != null)
				fos.close();
		}
	}

	private void saveFile(File f, byte[] cont) throws FileNotFoundException,
			IOException
	{
		if (cont == null)
		{
			// remove it
			if (f.exists())
				f.delete();
			return;
		}
		FileOutputStream fos = null;

		try
		{

			fos = new FileOutputStream(f);

			fos.write(cont);

			fos.flush();
		}
		finally
		{
			if (fos != null)
				fos.close();
		}
	}

	private byte[] loadFile(File f) throws FileNotFoundException, IOException
	{
		FileInputStream fis = null;
		try
		{

			if (!f.exists())
				return null;

			byte[] buf = new byte[(int) f.length()];
			fis = new FileInputStream(f);

			fis.read(buf);
			return buf;
		}
		finally
		{
			if (fis != null)
				fis.close();
		}
	}

	// //////////////////////
	public void saveDataXDB(byte[] cont) throws Exception
	{
		File f = new File(dirBasePath + "/_datax_db");
		saveFile(f, cont);
	}

	public byte[] loadDataXDB() throws Exception
	{
		File f = new File(dirBasePath + "/_datax_db");
		return loadFile(f);
	}

	// //////////////////////////

	FilenameFilter baseFnFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			if (name.startsWith("b_") && name.endsWith(".xml"))
				return true;
			return false;
		}
	};

	private String id2BaseFileName(int baseid)
	{
		return "b_" + baseid + ".xml";
	}

	private int BaseFileName2id(String bfn)
	{
		String tmps = bfn.substring(2, bfn.length() - 4);
		return Integer.parseInt(tmps);
	}

	public void saveBaseById(int id, byte[] cont) throws Exception
	{
		String fn = id2BaseFileName(id);
		File f = new File(dirBasePath + "/" + fn);
		saveFile(f, cont);
	}

	public byte[] loadBaseById(int id) throws Exception
	{
		String fp = dirBasePath + "/" + id2BaseFileName(id);
		File f = new File(fp);

		return loadFile(f);
	}

	public int[] loadBaseAllIds() throws Exception
	{
		File[] fs = dirBase.listFiles(baseFnFilter);
		List<Integer> ffs = new ArrayList<Integer>(fs.length);
		for (File f : fs)
		{
			int tmpn = BaseFileName2id(f.getName());
			ffs.add(tmpn);
		}

		int[] rets = new int[ffs.size()];
		for (int i = 0; i < rets.length; i++)
		{
			rets[i] = ffs.get(i);
		}
		return rets;
	}

	// ////////////////////////////////
	FilenameFilter classFnFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			if (name.startsWith("c_") && name.endsWith(".xml"))
				return true;
			return false;
		}
	};

	private String id2ClassFileName(int baseid, int classid)
	{
		return "c_" + baseid + "_" + classid + ".xml";
	}

	private int[] ClassFileName2id(String bfn)
	{
		String tmps = bfn.substring(2, bfn.length() - 4);
		int p = tmps.indexOf('_');
		int[] rets = new int[2];
		rets[0] = Integer.parseInt(tmps.substring(0, p));
		rets[1] = Integer.parseInt(tmps.substring(p + 1));
		return rets;
	}

	public void saveClassById(int baseid, int id, byte[] cont) throws Exception
	{
		String fn = id2ClassFileName(baseid, id);
		File f = new File(dirBasePath + "/" + fn);
		saveFile(f, cont);
	}

	public byte[] loadClassById(int baseid, int id) throws Exception
	{
		String fp = dirBasePath + "/" + id2ClassFileName(baseid, id);
		File f = new File(fp);

		return loadFile(f);
	}

	public int[] loadClassAllIds(int baseid) throws Exception
	{
		File[] fs = dirBase.listFiles(classFnFilter);
		List<Integer> ffs = new ArrayList<Integer>(fs.length);
		for (File f : fs)
		{
			int[] tmpn = ClassFileName2id(f.getName());
			if (tmpn[0] != baseid)
				continue;

			ffs.add(tmpn[1]);
		}

		int[] rets = new int[ffs.size()];
		for (int i = 0; i < rets.length; i++)
		{
			rets[i] = ffs.get(i);
		}
		return rets;
	}

	// ///////////////////////

	FilenameFilter indexFnFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			if (name.startsWith("i_") && name.endsWith(".xml"))
				return true;
			return false;
		}
	};

	private String id2IndexFileName(int baseid, int classid, int idxid)
	{
		return "i_" + baseid + "_" + classid + "_" + idxid + ".xml";
	}

	private int[] IndexFileName2id(String bfn)
	{
		String tmps = bfn.substring(2, bfn.length() - 4);
		String[] ss = tmps.split("_");
		int[] rets = new int[3];
		rets[0] = Integer.parseInt(ss[0]);
		rets[1] = Integer.parseInt(ss[1]);
		rets[2] = Integer.parseInt(ss[2]);
		return rets;
	}

	public void saveIndexById(int baseid, int classid, int id, byte[] cont)
			throws Exception
	{
		String fn = id2IndexFileName(baseid, classid, id);
		File f = new File(dirBasePath + "/" + fn);
		saveFile(f, cont);
	}

	public byte[] loadIndexById(int baseid, int classid, int id)
			throws Exception
	{
		String fp = dirBasePath + "/" + id2IndexFileName(baseid, classid, id);
		File f = new File(fp);

		return loadFile(f);
	}

	public int[] loadIndexAllIds(int baseid, int classid) throws Exception
	{
		File[] fs = dirBase.listFiles(indexFnFilter);
		List<Integer> ffs = new ArrayList<Integer>(fs.length);
		for (File f : fs)
		{
			int[] tmpn = IndexFileName2id(f.getName());
			if (tmpn[0] != baseid)
				continue;
			if (tmpn[1] != classid)
				continue;

			ffs.add(tmpn[2]);
		}

		int[] rets = new int[ffs.size()];
		for (int i = 0; i < rets.length; i++)
		{
			rets[i] = ffs.get(i);
		}
		return rets;
	}

	// ///////////////////////

	FilenameFilter formFnFilter = new FilenameFilter()
	{
		public boolean accept(File dir, String name)
		{
			if (name.startsWith("f_") && name.endsWith(".xml"))
				return true;
			return false;
		}
	};

	private String id2FormFileName(int baseid, int classid, int idxid)
	{
		return "f_" + baseid + "_" + classid + "_" + idxid + ".xml";
	}

	private int[] FormFileName2id(String bfn)
	{
		String tmps = bfn.substring(2, bfn.length() - 4);
		String[] ss = tmps.split("_");
		int[] rets = new int[3];
		rets[0] = Integer.parseInt(ss[0]);
		rets[1] = Integer.parseInt(ss[1]);
		rets[2] = Integer.parseInt(ss[2]);
		return rets;
	}

	public void saveFormById(int baseid, int classid, int id, byte[] cont)
			throws Exception
	{
		String fn = id2FormFileName(baseid, classid, id);
		File f = new File(dirBasePath + "/" + fn);
		saveFile(f, cont);
	}

	public byte[] loadFormById(int baseid, int classid, int id)
			throws Exception
	{
		String fp = dirBasePath + "/" + id2FormFileName(baseid, classid, id);
		File f = new File(fp);

		return loadFile(f);
	}

	public int[] loadFormAllIds(int baseid, int classid) throws Exception
	{
		File[] fs = dirBase.listFiles(formFnFilter);
		List<Integer> ffs = new ArrayList<Integer>(fs.length);
		for (File f : fs)
		{
			int[] tmpn = FormFileName2id(f.getName());
			if (tmpn[0] != baseid)
				continue;
			if (tmpn[1] != classid)
				continue;

			ffs.add(tmpn[2]);
		}

		int[] rets = new int[ffs.size()];
		for (int i = 0; i < rets.length; i++)
		{
			rets[i] = ffs.get(i);
		}
		return rets;
	}

	// //////////////file
	Object fileLock = new Object();

	private String getFileBase(int baseid, int classid)
	{
		return dirBasePath + "/file_" + baseid + "_" + classid;
	}

	private Properties loadFileId(int baseid, int classid) throws IOException
	{
		File f = new File(getFileBase(baseid, classid) + "/_id");
		Properties p = new Properties();
		if (f.exists())
		{
			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(f);
				p.load(fis);
			}
			finally
			{
				if (fis != null)
					fis.close();
			}
		}

		return p;
	}

	private void saveFileId(int baseid, int classid, Properties p)
			throws IOException
	{
		File f = new File(getFileBase(baseid, classid) + "/_id");

		FileOutputStream fos = null;

		try
		{
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();

			fos = new FileOutputStream(f);
			p.store(fos, "");
		}
		finally
		{
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * 获得文件存储新id
	 * 
	 * @param baseid
	 * @param classid
	 * @return
	 * @throws Exception
	 */
	public long getFileNewId(int baseid, int classid) throws Exception
	{
		synchronized (fileLock)
		{
			Properties p = loadFileId(baseid, classid);
			String strcpid = p.getProperty("max_id");
			int v = 0;
			if (strcpid != null && !strcpid.equals(""))
				v = Integer.parseInt(strcpid);

			v++;
			p.setProperty("max_id", "" + v);
			saveFileId(baseid, classid, p);
			return v;
		}
	}

	static final long MAX_DIR_NUM = 1000;

	static final int FILENAME_LEN = 3;

	static final long DIR_LEVEL = 3;

	static long tmplll = 1000;

	static long MAX_ID = tmplll * tmplll * tmplll * tmplll - 1;

	/**
	 * 得到文件路径
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @return 如果返回null表示文件不存在
	 */
	public File getFile(int baseid, int classid, long fid)
	{
		if (fid <= 0 || fid > MAX_ID)
			throw new IllegalArgumentException("invalid autoid=" + fid);

		String tmps = "";
		long tmpl = fid;
		for (int i = 0; i <= DIR_LEVEL; i++)
		{
			String fdname = "" + tmpl % MAX_DIR_NUM;
			// 对名称前面位数不足进行补0
			int c = FILENAME_LEN - fdname.length();
			for (int j = 0; j < c; j++)
			{
				fdname = "0" + fdname;
			}

			tmpl = tmpl / MAX_DIR_NUM;

			tmps = "/" + fdname + tmps;
		}

		return new File(getFileBase(baseid, classid) + tmps);
	}

	/**
	 * 存储文件
	 * 
	 * @param baseid
	 * @param classid
	 * @param fid
	 * @param cont
	 * @throws Exception
	 */
	public String saveFile(int baseid, int classid, long fid, byte[] cont,
			boolean bhis) throws Exception
	{
		File f = getFile(baseid, classid, fid);
		String hisid = null;
		if (!f.exists())
		{
			f.getParentFile().mkdirs();
		}
		else
		{
			if (bhis)
			{// 生成历史文件
				hisid = UUID.randomUUID().toString().replace("-", "");
				File hf = new File(f.getAbsolutePath() + "_" + hisid);
				f.renameTo(hf);
			}
		}

		this.saveFile(f, cont);
		return hisid;
	}

	public byte[] loadFile(int baseid, int classid, long fid) throws Exception
	{
		File f = getFile(baseid, classid, fid);
		if (!f.exists())
			return null;

		return this.loadFile(f);
	}

	static class HisFnFilter implements FilenameFilter
	{
		String prefix = null;

		public HisFnFilter(String p)
		{
			prefix = p;
		}

		public boolean accept(File dir, String name)
		{
			return name.startsWith(prefix);
		}
	}

	public void delFile(int baseid, int classid, long fid)
	{
		File f = getFile(baseid, classid, fid);
		if (!f.exists())
			return;

		HisFnFilter hff = new HisFnFilter(fid + "_");
		File[] hfs = f.getParentFile().listFiles(hff);
		if (hfs != null)
		{
			for (File hf : hfs)
			{
				hf.delete();
			}
		}
		f.delete();
	}

	public void delHisFile(int baseid, int classid, long fid, String hisid)
	{
		File f = getFile(baseid, classid, fid);
		if (!f.exists())
			return;

		File hf = new File(f.getAbsoluteFile() + "_" + hisid);
		if (hf.exists())
			hf.delete();
	}

	public byte[] loadHisFile(int baseid, int classid, long fid, String hisid)
			throws Exception
	{
		File f = getFile(baseid, classid, fid);
		if (!f.exists())
			return null;
		File hf = new File(f.getAbsoluteFile() + "_" + hisid);
		if (!hf.exists())
			return null;

		return this.loadFile(hf);
	}

	public File getHisFile(int baseid, int classid, long fid, String hisid)
	{
		File f = getFile(baseid, classid, fid);
		if (!f.exists())
			return null;
		return new File(f.getAbsoluteFile() + "_" + hisid);
	}
}
