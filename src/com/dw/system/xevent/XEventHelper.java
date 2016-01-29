package com.dw.system.xevent;

import java.util.zip.*;
import java.util.jar.*;
import java.util.*;
import java.text.*;
import java.io.*;
import java.lang.reflect.*;

import org.w3c.dom.*;

import com.dw.system.*;

/**
 * <p>Title: �¼�������</p>
 * <p>Description: �ṩɨ������ϵͳ������ÿ������Ŀ¼�������ļ���Ϣ.
 * Ϊ�¼�ϵͳ�ĳ�ʼ���ṩ������
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Jason Zhu
 * @version 1.0
 */

public class XEventHelper
{
	final static Element[] getCurChildElement(Element ele,
											  String tagname)
	{
		if (ele == null || tagname == null)
		{
			return null;
		}

		boolean isall = false;
		if (tagname.equals("*"))
		{
			isall = true;

		}
		NodeList tmpnl = ele.getChildNodes();

		Node tmpn = null;

		Vector v = new Vector();
		int k;
		if (tmpnl != null)
		{
			for (k = 0; k < tmpnl.getLength(); k++)
			{
				tmpn = tmpnl.item(k);

				if (tmpn == null || tmpn.getNodeType() != Node.ELEMENT_NODE)
				{
					continue;
				}
				Element eee = (Element) tmpn;
				if (isall || tagname.equals(eee.getNodeName()))
				{
					v.add(eee);
				}
			}
		}

		Element[] tmpe = new Element[v.size()];
		v.toArray(tmpe);
		return tmpe;
	}

	public static void sort(Vector v)
	{
		sort(v, false);
	}

	public static void sort(Vector v, boolean inorder)
	{
		sort(v, inorder, 0, v.size() - 1);
	}

	private static void sort(Vector v, boolean inorder, int lo0, int hi0)
	{
		if (v.size() <= 0)
		{
			return;
		}

		int inv = inorder ? -1 : 1;
		int lo = lo0;
		int hi = hi0;
		Comparable mid;

		if (hi0 > lo0)
		{
			mid = (Comparable) v.elementAt( (lo0 + hi0) / 2);

			// loop through the array until indices cross
			while (lo <= hi)
			{
				/* find the first element that is greater than or equal to
				 * the partition element starting from the left Index.
				 */
				Comparable medl = (Comparable) v.elementAt(lo);
				while ( (lo < hi0) && (medl.compareTo(mid) * inv < 0))
				{
					++lo;
				}

				/* find an element that is smaller than or equal to
				 * the partition element starting from the right Index.
				 */
				Comparable medh = (Comparable) v.elementAt(hi);
				while ( (hi > lo0) && (medh.compareTo(mid) * inv > 0))
				{
					--hi;
				}

				// if the indexes have not crossed, swap
				if (lo <= hi)
				{
					swap(v, lo, hi);
					++lo;
					--hi;
				}
			}

			/* If the right index has not reached the left side of array
			 * must now sort the left partition.
			 */
			if (lo0 < hi)
			{
				sort(v, inorder, lo0, hi);

				/* If the left index has not reached the right side of array
				 * must now sort the right partition.
				 */

			}
			if (lo < hi0)
			{
				sort(v, inorder, lo, hi0);
			}
		}
	}

	private static void swap(Vector v, int i, int j)
	{
		Object obj;

		obj = v.elementAt(i);

		v.setElementAt(v.elementAt(j), i);
		v.setElementAt(obj, j);
	}



	static final String INI_FILE = "ini.xml";
	/**
	 * ����classesĿ¼��libĿ¼�е�jar�ļ�
	 * @return
	 * @throws IOException
	 */
	public static Vector readIniFiles()
		throws IOException
	{
		String classdir = Configuration.getProperty("current.location");
		if (classdir == null)
		{
			throw new RuntimeException("Cannot locate class dir!");
		}
		String libdir = classdir + "/../lib";
		File[] dirfs = new File[]
			{
			new File(classdir),
			new File(libdir)};
		return readDirFiles(dirfs, INI_FILE);
	}

	/**
	 * ��ѡ����Ŀ¼������������Ŀ¼����Ŀ¼������jar�ļ��е�ָ���ļ�����
	 * @param fs ָ����Ŀ¼
	 * @param filename �ļ�����
	 * @return ���������ļ�����
	 * @throws IOException
	 */
	public static Vector readDirFiles(File[] fs, String filename)
		throws IOException
	{
		if (fs == null)
		{
			return null;
		}
		Vector v = new Vector();
		for (int i = 0; i < fs.length; i++)
		{
			Vector v0 = readDirFile(fs[i], filename);
			v.addAll(v0);
		}
		return v;
	}

	static class EntryFilter
		implements FileFilter
	{
		String entryN = null;
		public EntryFilter(String fn)
		{
			entryN = fn;
		}

		public boolean accept(File pathname)
		{
			if (pathname.isDirectory())
			{
				return true;
			}

			String n = pathname.getName();
			if (n.equalsIgnoreCase(entryN))
			{
				return true;
			}
			n = n.toLowerCase();
			if (n.endsWith(".jar") || n.endsWith(".zip"))
			{
				return true;
			}
			return false;
		}
	}

	private static Vector readDirFile(File dirfile, String filename)
		throws IOException
	{
		Vector v = new Vector();
		EntryFilter ef = new EntryFilter(filename);
		readDirFile(dirfile, ef, v, filename);
		return v;
	}

	private static void readDirFile(
		File dirfile,
		FileFilter filter, Vector v, String filename)
		throws IOException
	{
		File[] fs = dirfile.listFiles(filter);
		if (fs == null)
		{
			return;
		}
		for (int i = 0; i < fs.length; i++)
		{
			if (fs[i].isDirectory())
			{
				readDirFile(fs[i], filter, v, filename);
			}
			else
			{
				String n = fs[i].getName().toLowerCase();
				if (n.endsWith(".jar") || n.endsWith(".zip"))
				{
					Vector v0 = readJarFile(fs[i], filename);
					v.addAll(v0);
				}
				else
				{
					byte[] buf = new byte[ (int) fs[i].length()];
					FileInputStream fis = null;
					try
					{
						fis = new FileInputStream(fs[i]);
						fis.read(buf);
						v.addElement(new FileDataItem(fs[i].getAbsolutePath(),
							buf));
					}
					finally
					{
						fis.close();
					}
				}
			}
		}
	}

	/**
	 * ��ȡjar�ļ��е�ָ�����ļ����������������ƺ�����
	 * @param jarfile jar�ļ�
	 * @param filename �ļ�����
	 * @return
	 * @throws IOException
	 */
	private static Vector readJarFile(File jarfile, String filename)
		throws IOException
	{
		JarInputStream jis = null;
		Vector v = new Vector();
		try
		{
			jis = new JarInputStream(new FileInputStream(jarfile));
			JarEntry je = null;
			while ( (je = jis.getNextJarEntry()) != null)
			{
				try
				{
					String ename = je.getName();
					if (ename.equals(filename) || ename.endsWith("/" + filename))
					{
						String en = jarfile.getAbsolutePath() + "/" + ename;
						v.addElement(readData(en, jis));
					}
				}
				finally
				{
					jis.closeEntry();
				}
			}
			return v;
		}
		finally
		{
			jis.close();
		}
	}

	private static FileDataItem readData(String filename, InputStream is)
		throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buf = new byte[2048];

		while (true)
		{
			int read = is.read(buf);
			if (read == -1)
			{
				break;
			}
			baos.write(buf, 0, read);
		}
		baos.flush();
		return new FileDataItem(filename, baos.toByteArray());
	}

	/**
	 * �õ��¼�Զ��ͨ�Žӿ�
	 * @return Զ�̽ӿڶ���
	 * @throws ClassNotFoundException û���ҵ�ʵ����
	 * @throws IllegalAccessException ���Ϸ��ķ���
	 * @throws InstantiationException ��ʼ��ʧ��
	 */
	public static RemoteInterface getRemoteInterface(RemoteCallback rcb,
		StringBuffer failedreson)
	{
		try
		{
			Class c = Class.forName("com.dw.net.broadcast.XEventRemoter");
			Constructor cons = c.getConstructor(
				new Class[]
				{RemoteCallback.class});
			return (RemoteInterface) cons.newInstance(new Object[]
				{rcb});
			//return (RemoteInterface) c.newInstance();
		}
		catch (ClassNotFoundException cnfe)
		{
			failedreson.append(
				"Cannot find RemoteInterface Implementation class!");
		}
		catch (IllegalAccessException iae)
		{
			failedreson.append(
				"Cannot access RemoteInterface Implementation class!");
		}
		catch (InstantiationException ie)
		{
			failedreson.append(
				"Cannot instantiate RemoteInterface Implementation class!");
		}
		catch (NoSuchMethodException nsme)
		{
			failedreson.append(
				"Cannot find constructer (" + RemoteCallback.class.getName() +
				")!");
		}
		catch (InvocationTargetException ite)
		{
			failedreson.append(
				"Cannot create instance from constructer!");
		}
		return null;
	}

	public static void main(String[] args)
		throws Throwable
	{
		File[] fs = new File[]
			{
			new File("D:/working/RnD/coding/system/classes"),
			new File("D:/working/RnD/coding/system/test")
		};
		Vector v = readDirFiles(fs, "ini.xml");
		int s = v.size();
		for (int i = 0; i < s; i++)
		{
			System.out.println(v.elementAt(i));
		}
	}
}

class FileDataItem
{
	private String name = null;
	/**
	 * ��Ӧ�ļ���Ϊ��Դ·������Ϣ��
	 */
	private String absResPath = null ;
	private byte[] content = null;

	FileDataItem(String n, byte[] cont)
	{
		name = n;
		content = cont;
	}
	
	FileDataItem(String n,String absresp, byte[] cont)
	{
		name = n;
		absResPath = absresp;
		content = cont;
	}

	public String getName()
	{
		return name;
	}
	
	public String getAbsResPath()
	{
		return absResPath ;
	}

	public byte[] getContent()
	{
		return content;
	}

	public String toString()
	{
		return name + "\n" + new String(content);
	}
}