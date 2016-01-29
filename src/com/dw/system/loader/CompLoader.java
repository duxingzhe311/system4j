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
 * 当某个应用想实现动态加载特定目录下和特定jar文件中的类时.可以通过该方法.
 * 
 * 例如,eclipse中的插件和j2ee中的webapp,都是一开始并不知道要装载那些内容.
 *  而在启动时才根据需要对相关的插件资源(类,资源文件等)进行装载.
 *  
 *  在上面描述的应用环境中,很有可能出现:在不同的插件中,存在的包类名称完全相同的情况.当类
 *   内部的实现完全不同. 通过本类装载器,可以使这种情况不会造成冲突--因为他们被各自的ClassLoader
 *   装载,互不干涉.
 *   
 *   当然,不同的装载空间中的类,可以实现公共的接口,并在全局上被使用.
 *  
 * 使用该方法时,应该根据指定的目录和唯一标记,获得对应的ClassLoader--该ClassLoader
 * 内部自己实现了对指定目录和jar文件下的类和资源的装载.
 * 
 * 
 * 注:不同的类装载器(ClassLoader)对象,装载了同一个类文件后,产生的Class对象是不同的.
 * @author Jason Zhu
 *
 */
public class CompLoader
{
	private static String loaderClass = "com.dw.system.loader.CompClassLoader";

	private static Hashtable<String, CompClassLoader> name2ccl = new Hashtable<String, CompClassLoader>();

	/**
	 * 根据给定的webapp目录,和指定的名称做获得一个新的类装载器
	 * 该装载器装载类时,从webapp目录下查找classes lib 目录下的所有类和jar文件中的所有类
	 * 
	 * 所以本方法为系统提供了一个动态装载 指定目录和jar文件 中类的方法
	 * 
	 * 它可以用来实现诸如 j2ee webapp的装载等.
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
