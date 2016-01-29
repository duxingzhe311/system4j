package com.dw.system.gdb;

import java.io.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.dw.system.AppConfig;
import com.dw.system.gdb.conf.XDataHelper;


public class GFile
{
	private static GFile fileProvider = null;

    public static GFile getInstance()
    {
        if (fileProvider != null)
            return fileProvider;

        try
        {
	        fileProvider = new GFile();
	        return fileProvider ;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	return null ;
        }
    }


    class FileBase
    {
        public String fileBase = null ;
        public IPathGenerator pathGen = null ;
    }

    public enum BaseType
    {
    	file,
    	url,
    	webapp
    }
    /// <summary>
    /// 前缀名称映射到文件系统的根目录
    /// </summary>
    private Hashtable prefix2filebase = new Hashtable() ;
    /// <summary>
    /// 前缀名称映射到URL系统的根目录
    /// </summary>
    private Hashtable prefix2urlbase = new Hashtable() ;
    /// <summary>
    /// 前缀名称映射到Web应用的根目录
    /// </summary>
    private Hashtable prefix2WebAppBase = new Hashtable();

    private GFile() throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
    	Element gfileele = AppConfig.getConfElement("gfile");
		if(gfileele==null)
			return ;
        
        Element[] eles = XDataHelper.getCurChildElement(gfileele, "base");
		if (eles == null || eles.length <= 0)
        {
			for(Element ele:eles)
			{
				BaseType bt = BaseType.file;
				String t = ele.getAttribute("file");
				if(t!=null)
					bt = BaseType.valueOf(t);
				String n = ele.getAttribute("name");
	            String v = ele.getAttribute("path");

	            loadNameValue(bt,n, v);
			}
        }
    }


    private FileBase StrToFileBase(String s) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        int p = s.indexOf('|') ;
        FileBase fb = null ;
        if(p<0)
        {
            fb = new FileBase() ;
            fb.fileBase = s ;
        }
        else
        {
            fb = new FileBase() ;
            fb.fileBase = s.substring(0,p) ;
            String cn = s.substring(p+1).trim() ;
            if(cn!="")
            {
                Class t = Class.forName(cn) ;
                fb.pathGen = (IPathGenerator)t.newInstance();
            }
        }

        //相对于GFile.conf文件目录
//        if(fb.fileBase.startsWith("."))
//        {
//            //confFileDirInfo
//            boolean benddd = fb.fileBase.endsWith("\\") ;
//
//            DirectoryInfo tmpdi = new DirectoryInfo(confFileDirInfo.FullName+"\\"+fb.fileBase) ;
//
//            fb.fileBase = tmpdi.FullName ;
//            if(benddd)
//            {
//                if(!fb.fileBase.endsWith("\\"))
//                    fb.fileBase += "\\" ;
//            }
//            else
//            {
//                if(fb.fileBase.endsWith("\\"))
//                    fb.fileBase = fb.fileBase.substring(0,fb.fileBase.length()-1) ;
//            }
//        }

        return fb ;
    }


    private void loadNameValue(BaseType bt,String n,String v) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        if(bt==BaseType.url)
        	prefix2urlbase.put(n,v);
        else if(bt==BaseType.webapp)
        	prefix2WebAppBase.put(n,v);
        else
        	prefix2filebase.put(n, StrToFileBase(v)) ;
    }

    public String[] GetFileBasePrefix()
    {
        String[] rets = new String[prefix2filebase.size()] ;
        prefix2filebase.keySet().toArray(rets);
        return rets ;
    }

    public String[] GetUrlBasePrefix()
    {
        String[] rets = new String[prefix2urlbase.size()] ;
        prefix2urlbase.keySet().toArray(rets);
        return rets ;
    }


    /// <summary>
    /// 判断一个前缀是否支持文件的操作
    /// </summary>
    /// <param name="prefix">前缀名称</param>
    /// <returns>true表示支持，否则不支持</returns>
    public boolean HasFileOper(String prefix)
    {
        return prefix2filebase.containsKey(prefix);
    }

    /// <summary>
    /// 判断一个前缀是否可以通过url方式访问（也就是是否有对应urlbase）
    /// </summary>
    /// <param name="prefix">前缀名称</param>
    /// <returns>true表示支持，否则不支持</returns>
    public boolean HasUrlAccess(String prefix)
    {
        return prefix2urlbase.containsKey(prefix);
    }

    /// <summary>
    /// 根据格式化路径和具体内容,对文件进行保存操作
    /// </summary>
    /// <param name="formatedpath">格式化路径</param>
    /// <param name="cont">文件内容</param>
    public void SaveToFile(String formatedpath,byte[] cont) throws IOException
    {
        SaveToFile(formatedpath,cont,null);
    }

    public void SaveToFile(String formatedpath,byte[] cont,Date modify_time) throws IOException
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到["+formatedpath+"]对应的文件系统路径，可能是个无效的前缀(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        FileOutputStream fs = null ;
        File fi = new File(fp) ;
        try
        {
        	File df = fi.getParentFile() ;
        	if(!df.exists())
        		df.mkdirs();

            fs = new FileOutputStream(fp);//|FileMode.Truncate) ;
            fs.write(cont) ;
            fs.flush() ;
        }
        finally
        {
            if(fs!=null)
                fs.close() ;
        }

        if(modify_time!=null)
        {
        	fi.setLastModified(modify_time.getTime());
        }
    }

    /// <summary>
    /// 根据格式化路径和输入流,对文件进行保存操作
    /// </summary>
    /// <param name="formatedpath"></param>
    /// <param name="instream"></param>
    public void SaveToFile(String formatedpath,InputStream instream) throws IOException
    {
        SaveToFile(formatedpath,instream,null) ;
    }

    public void SaveToFile(String formatedpath,InputStream instream,Date modify_time) throws IOException
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到["+formatedpath+"]对应的文件系统路径，可能是个无效的前缀(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        FileOutputStream fs = null ;
        File fi = new File(fp) ;
        try
        {
        	File df = fi.getParentFile() ;
        	if(!df.exists())
        		df.mkdirs();

            fs = new FileOutputStream(fp);//|FileMode.Truncate) ;

            byte[] buf = new byte[1024] ;
            int rlen ;
            while((rlen=instream.read(buf,0,1024))>0)
            {
                fs.write(buf,0,rlen) ;
            }

            fs.flush() ;
        }
        finally
        {
            if(fs!=null)
                fs.close() ;
        }

        if(modify_time!=null)
        {
        	fi.setLastModified(modify_time.getTime());
        }
    }

//    public void SaveXmlToFile(String formatedpath,Document xd)
//    {
//        SaveXmlToFile(formatedpath,xd,null) ;
//    }

//    public void SaveXmlToFile(String formatedpath,Document xd,Date modify_time)
//    {
//        String fp = GetFilePath(formatedpath) ;
//        if(fp==null)
//            throw new RuntimeException("不能找到["+formatedpath+"]对应的文件系统路径，可能是个无效的前缀(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;
//
//        xd..Save(fp);
//
//        if(modify_time!=DateTime.MinValue)
//        {
//            File.SetLastAccessTime(fp,modify_time) ;
//        }
//    }
    /// <summary>
    /// 根据前缀名称，自动生成一个新的保存路径
    /// </summary>
    /// <param name="prefix"></param>
    /// <param name="fileext">文件扩展名</param>
    /// <returns></returns>
    private String CreateAutoPath(String prefix,String fileext)
    {
        FileBase fb = (FileBase)this.prefix2filebase.get(prefix) ;
        if(fb==null)
            return null ;
        if(fb.pathGen==null)
            return null ;
        StringBuilder sb= new StringBuilder() ;

        sb.append("[").append(prefix).append("]");

        String s = fb.pathGen.GetOrCreateFilePath();
        if(s.charAt(0)!='\\'&&s.charAt(0)!='/')
            sb.append('\\') ;
        sb.append(s) ;
        if(s.charAt(s.length()-1)!='\\'&&s.charAt(s.length()-1)!='/')
            sb.append('\\') ;
        
        sb.append(fb.pathGen.CreateNewFileName()) ;

        sb.append('.') ;
        sb.append(fileext) ;

        return sb.toString() ;
    }

    /// <summary>
    /// 根据前缀名，文件内容，自动生成文件保存路径，并对
    /// </summary>
    /// <param name="prefix">支持文件路径，并可以自动产生的前缀</param>
    /// <param name="fileext">文件扩展名</param>
    /// <param name="cont">被保存文件内容</param>
    /// <returns>保存后具体文件的格式化路径</returns>
    public String SaveToAutoPath(String prefix,String fileext,byte[] cont) throws IOException
    {
        String formatp = CreateAutoPath(prefix,fileext) ;
        if(formatp==null)
            throw new RuntimeException("前缀["+prefix+"]不能创建自动路径(Cannot create auto path with prefix ["+prefix+"])") ;
        //Console.WriteLine("save to="+formatp) ;
        SaveToFile(formatp,cont) ;
        return formatp ;
    }


//    public String SaveXmlToAutoPath(String prefix,XmlDocument xd)
//    {
//        String formatp = CreateAutoPath(prefix,"xml") ;
//        if(formatp==null)
//            throw new RuntimeException("前缀["+prefix+"]不能创建自动路径(Cannot create auto path with prefix ["+prefix+"])") ;
//        //Console.WriteLine("save to="+formatp) ;
//        SaveXmlToFile(formatp,xd) ;
//        return formatp ;
//    }
    /// <summary>
    /// 拷贝一个文件到另一个文件
    /// </summary>
    /// <param name="sorformatedpath">原始文件格式化路径</param>
    /// <param name="tarformatedpath">目标文件格式化路径</param>
    /// <param name="overwrite">如果目标文件已经存在，该参数决定是否要进行覆盖</param>
    public void CopyToFile(String sorformatedpath,String tarformatedpath,boolean overwrite) throws IOException
    {
        String fp = GetFilePath(sorformatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到源文件系统路径Unable to Get Sor File Path,may be has unknown prefix in:"+sorformatedpath) ;

        String tarfp = GetFilePath(tarformatedpath) ;
        if(tarfp==null)
            throw new RuntimeException("不能找到目标文件系统路径Unable to Get Tar File Path,may be has unknown prefix in:"+tarformatedpath) ;

        File sorfi = new File(fp) ;
        if(!sorfi.exists())
            throw new RuntimeException("源文件不存在source file is not existed:"+sorformatedpath) ;

        File tarfi = new File(tarfp) ;
        if(tarfi.exists()&&!overwrite)
        {
            throw new RuntimeException("目标文件已经存在Target file is existed:"+tarformatedpath) ;
        }

        if(!tarfi.getParentFile().exists())
        {
        	tarfi.getParentFile().mkdirs();
        }

        copyFile(sorfi,tarfp);
    }
    
    private static void copyFile(File sf,String tf) throws IOException
    {
    	FileInputStream fis = null ;
    	FileOutputStream fos = null ;
    	
    	try
    	{
    		fis = new FileInputStream(sf);
    		fos = new FileOutputStream(tf);
    		byte[] buf = new byte[1024];
    		int len ;
    		while((len=fis.read(buf))>=0)
    		{
    			fos.write(buf,0,len);
    		}
    		fos.flush();
    	}
    	finally
    	{
    		if(fis!=null)
    			fis.close();
    		if(fos!=null)
    			fos.close();
    	}
    }

    /// <summary>
    /// 修改一个文件名称到另一个名称。
    /// 如果目标名称已经存在，则抛出错误
    /// </summary>
    /// <param name="sorformatedpath">原始文件格式化路径</param>
    /// <param name="tarformatedpath">目标文件格式化路径</param>
    public void MoveToFile(String sorformatedpath,String tarformatedpath)
    {
        String fp = GetFilePath(sorformatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到源文件系统路径Unable to Get Sor File Path,may be has unknown prefix in:"+sorformatedpath) ;
        String tarfp = GetFilePath(tarformatedpath) ;
        if(tarfp==null)
            throw new RuntimeException("不能找到目标文件系统路径Unable to Get Tar File Path,may be has unknown prefix in:"+tarformatedpath) ;

        File tarfi = new File(tarfp) ;
        if(tarfi.exists())
        {
            throw new RuntimeException("目标文件已经存在The file with target formated path is already existed!") ;
        }

        if(!tarfi.getParentFile().exists())
            tarfi.getParentFile().mkdirs();

        File fi = new File(fp) ;
        
        fi.renameTo(tarfi);
    }


    /// <summary>
    /// 根据格式化信息对相关的文件进行删除
    /// </summary>
    /// <param name="formatedpath"></param>
    public void DeleteFile(String formatedpath)
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到对应的文件系统路径Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        if(fi.exists())
        {
            fi.delete();
        }
    }

    /// <summary>
    /// 判断一个格式化文件是否存在
    /// </summary>
    /// <param name="formatedpath"></param>
    /// <returns></returns>
    public boolean IsExistFile(String formatedpath)
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到对应的文件系统路径，前缀出错！Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        return fi.exists() ;
    }
    /// <summary>
    /// 根据格式化路径获得对应文件的内容
    /// </summary>
    /// <param name="formatedpath">格式化路径</param>
    /// <returns>文件的内容</returns>
    public byte[] ReadFromFile(String formatedpath) throws IOException
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("不能找到对应的文件系统路径Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        if(!fi.exists())
            throw new RuntimeException("文件不存在File is not existed:"+formatedpath) ;

        FileInputStream fs = null ;
        try
        {
            fs = new FileInputStream(fp) ;
            int len = (int)fi.length() ;
            byte[] buf = new byte[(int)fi.length()];
            fs.read(buf,0,len) ;
            return buf ;
        }
        finally
        {
            if(fs!=null)
                fs.close() ;
        }
    }

//    public XmlDocument ReadXmlFromFile(String formatedpath)
//    {
//        String fp = GetFilePath(formatedpath) ;
//        if(fp==null)
//            throw new DataAccessException("不能找到对应的文件系统路径Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;
//        XmlDocument xd = new XmlDocument() ;
//        xd.Load(fp) ;
//        return xd ;
//    }

    /// <summary>
    /// 根据格式化路径获得Mime类型
    /// </summary>
    /// <param name="formatedpath"></param>
    /// <returns></returns>
    public String GetMimeType(String formatedpath)
    {
        return null ;
    }

    private String[] splitFormatPath(String formatpath)
    {
        if(formatpath.charAt(0)!='[')
        {
            return null ;
        }

        int p = formatpath.indexOf(']',1) ;
        if(p<0)
        {
            return null ;
        }

        String[] rets = new String[2];
        rets[0] = formatpath.substring(1,p-1) ;
        rets[1] = formatpath.substring(p+1) ;
        return rets ;
    }

    /// <summary>
    /// 根据格式化路径得到具体的文件系统中的路径（通过该路径可以对文件进行IO操作）
    /// </summary>
    /// <param name="formatpath">格式化的路径</param>
    /// <returns>如果不存在对应的文件系统前缀，则返回null</returns>
    public String GetFilePath(String formatpath)
    {
//        if (formatpath.startsWith("."))
//        {//相对于配置文件路径
//            String ff = confFileDirInfo.FullName + "/" + formatpath;
//            FileInfo f = new FileInfo(ff);
//            return f.FullName;
//        }

        String prefix = null ;
        String relatedpath = null ;
        String[] pr = splitFormatPath(formatpath) ;
        if(pr==null)
            throw new RuntimeException("不合法的格式化路径Illegal Formated Path:"+formatpath) ;

        FileBase fbase = (FileBase)prefix2filebase.get(pr[0]);
        if(fbase==null)
            return null ;

        return fbase.fileBase+pr[1] ;
    }

    /// <summary>
    /// 根据格式化路径得到具体的外交能够访问的URL路径
    /// </summary>
    /// <param name="formatpath">格式化的路径</param>
    /// <returns>如果不存在对应的URL前缀，则返回null</returns>
    public String GetUrlPath(String formatpath)
    {
    	String[] pr = splitFormatPath(formatpath) ;
        if(pr==null)
            throw new RuntimeException("不合法的格式化路径Illegal Formated Path:"+formatpath) ;

        String ubase = (String)prefix2urlbase.get(pr[0]);
        if(ubase==null)
            return null ;

        return ubase+pr[1] ;
    }
    
    public interface IPathGenerator
    {
        public String GetOrCreateFilePath();

        public String CreateNewFileName() ;
    }

    public class YMDPathGen implements IPathGenerator
    {
        
        public String GetOrCreateFilePath()
        {
            Calendar dt = Calendar.getInstance();
            StringBuilder sb = new StringBuilder() ;
            sb.append(dt.get(Calendar.YEAR))
                .append('\\')
                .append(dt.get(Calendar.MONTH)+1)
                .append('\\')
                .append(dt.get(Calendar.DAY_OF_MONTH)) ;
            return sb.toString() ;
        }

        public String CreateNewFileName()
        {
            return UUID.randomUUID().toString().replaceAll("-","");
        }
    }

    public class YMDHPathGen implements IPathGenerator
    {
        
        public String GetOrCreateFilePath()
        {
            Calendar dt = Calendar.getInstance();
            StringBuilder sb = new StringBuilder() ;
            sb.append(dt.get(Calendar.YEAR))
            .append('\\')
            .append(dt.get(Calendar.MONTH)+1)
            .append('\\')
            .append(dt.get(Calendar.DAY_OF_MONTH))
                .append("\\")
                .append(dt.get(Calendar.HOUR));
            return sb.toString() ;
        }

        public String CreateNewFileName()
        {
            return UUID.randomUUID().toString().replaceAll("-","");
        }
    }
}
