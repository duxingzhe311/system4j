package com.dw.system.loader;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.jar.JarFile;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * ��ĳ��Ӧ����ʵ�ֶ�̬�����ض�Ŀ¼�º��ض�jar�ļ��е���ʱ.����ͨ���÷���.
 * 
 * ����,eclipse�еĲ����j2ee�е�webapp,����һ��ʼ����֪��Ҫװ����Щ����.
 *  ��������ʱ�Ÿ�����Ҫ����صĲ����Դ(��,��Դ�ļ���)����װ��.
 *  
 *  ������������Ӧ�û�����,���п��ܳ���:�ڲ�ͬ�Ĳ����,���ڵİ���������ȫ��ͬ�����.����
 *   �ڲ���ʵ����ȫ��ͬ. ͨ������װ����,����ʹ�������������ɳ�ͻ--��Ϊ���Ǳ����Ե�ClassLoader
 *   װ��,��������.
 *   
 *   ��Ȼ,��ͬ��װ�ؿռ��е���,����ʵ�ֹ����Ľӿ�,����ȫ���ϱ�ʹ��.
 *  
 * ʹ�ø÷���ʱ,Ӧ�ø���ָ����Ŀ¼��Ψһ���,��ö�Ӧ��ClassLoader--��ClassLoader
 * �ڲ��Լ�ʵ���˶�ָ��Ŀ¼��jar�ļ��µ������Դ��װ��.
 * 
 * 
 * ע:��ͬ����װ����(ClassLoader)����,װ����ͬһ�����ļ���,������Class�����ǲ�ͬ��.
 * @author Jason Zhu
 *
 */
public class CompLoader
{
	private static String loaderClass = "com.dw.system.loader.CompClassLoader";

	private static Hashtable<String, CompClassLoader> name2ccl = new Hashtable<String, CompClassLoader>();

	/**
	 * ���ݸ�����webappĿ¼,��ָ�������������һ���µ���װ����
	 * ��װ����װ����ʱ,��webappĿ¼�²���classes lib Ŀ¼�µ��������jar�ļ��е�������
	 * 
	 * ���Ա�����Ϊϵͳ�ṩ��һ����̬װ�� ָ��Ŀ¼��jar�ļ� ����ķ���
	 * 
	 * ����������ʵ������ j2ee webapp��װ�ص�.
	 * @param comp_dirbase
	 * @param comp_name
	 * @return
	 * @throws Exception
	 */
	public static CompClassLoader getCompClassLoader(String comp_dirbase,
			String comp_name) throws Exception
	{
		CompClassLoader ccl = name2ccl.get(comp_name);
		if (ccl != null)
			return ccl;

		File f = new File(comp_dirbase, comp_name);
		if (!f.exists())
			return null;

		CompLoader cl = new CompLoader(f);
		ccl = cl.createClassLoader();
		return ccl;
	}

	private boolean useSystemClassLoaderAsParent = false;

	private File classesDir = null;

	private File libDir = null;

	private CompLoader(File compdirbase)
	{
		File classesdir = new File(compdirbase, "classes/");
		if (classesdir.exists())
		{
			classesDir = classesdir;
		}

		File libd = new File(compdirbase, "libs/");
		if (libd.exists())
		{
			libDir = libd;
		}
	}

	/**
	 * Create associated classLoader.
	 */
	private CompClassLoader createClassLoader() throws Exception
	{
		Class clazz = Class.forName(loaderClass);
		CompClassLoader classLoader = null;

		ClassLoader parentClassLoader = null;

		if (parentClassLoader == null)
		{
			if (useSystemClassLoaderAsParent)
			{
				parentClassLoader = ClassLoader.getSystemClassLoader();
			}
			else
			{
				parentClassLoader = this.getClass().getClassLoader();
			}
		}
		Class[] argTypes = { ClassLoader.class };
		Object[] args = { parentClassLoader };
		Constructor constr = clazz.getConstructor(argTypes);
		classLoader = (CompClassLoader) constr.newInstance(args);

		if (classesDir != null)
			classLoader.addRepository(classesDir.toURL().toString());

		if (libDir != null)
		{
			String libPath = libDir.getCanonicalPath();
			classLoader.setJarPath(libPath);

			if (libDir != null)
			{
				for (File tmpf : libDir.listFiles())
				{
					if (!tmpf.isFile())
						continue;

					String lfn = tmpf.getName().toLowerCase();
					if (!lfn.endsWith(".jar"))
						continue;

					try
					{
						JarFile jarFile = new JarFile(tmpf);
						classLoader.addJar(lfn, jarFile, tmpf);
					}
					catch (Exception ex)
					{
						// Catch the exception if there is an empty jar file
						// Should ignore and continute loading other jar files
						// in the dir
					}
				}

			}

		}

		classLoader.start();

		return classLoader;

	}
}
