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
    /// ǰ׺����ӳ�䵽�ļ�ϵͳ�ĸ�Ŀ¼
    /// </summary>
    private Hashtable prefix2filebase = new Hashtable() ;
    /// <summary>
    /// ǰ׺����ӳ�䵽URLϵͳ�ĸ�Ŀ¼
    /// </summary>
    private Hashtable prefix2urlbase = new Hashtable() ;
    /// <summary>
    /// ǰ׺����ӳ�䵽WebӦ�õĸ�Ŀ¼
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

        //�����GFile.conf�ļ�Ŀ¼
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
    /// �ж�һ��ǰ׺�Ƿ�֧���ļ��Ĳ���
    /// </summary>
    /// <param name="prefix">ǰ׺����</param>
    /// <returns>true��ʾ֧�֣�����֧��</returns>
    public boolean HasFileOper(String prefix)
    {
        return prefix2filebase.containsKey(prefix);
    }

    /// <summary>
    /// �ж�һ��ǰ׺�Ƿ����ͨ��url��ʽ���ʣ�Ҳ�����Ƿ��ж�Ӧurlbase��
    /// </summary>
    /// <param name="prefix">ǰ׺����</param>
    /// <returns>true��ʾ֧�֣�����֧��</returns>
    public boolean HasUrlAccess(String prefix)
    {
        return prefix2urlbase.containsKey(prefix);
    }

    /// <summary>
    /// ���ݸ�ʽ��·���;�������,���ļ����б������
    /// </summary>
    /// <param name="formatedpath">��ʽ��·��</param>
    /// <param name="cont">�ļ�����</param>
    public void SaveToFile(String formatedpath,byte[] cont) throws IOException
    {
        SaveToFile(formatedpath,cont,null);
    }

    public void SaveToFile(String formatedpath,byte[] cont,Date modify_time) throws IOException
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ�["+formatedpath+"]��Ӧ���ļ�ϵͳ·���������Ǹ���Ч��ǰ׺(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

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
    /// ���ݸ�ʽ��·����������,���ļ����б������
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
            throw new RuntimeException("�����ҵ�["+formatedpath+"]��Ӧ���ļ�ϵͳ·���������Ǹ���Ч��ǰ׺(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

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
//            throw new RuntimeException("�����ҵ�["+formatedpath+"]��Ӧ���ļ�ϵͳ·���������Ǹ���Ч��ǰ׺(Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;
//
//        xd..Save(fp);
//
//        if(modify_time!=DateTime.MinValue)
//        {
//            File.SetLastAccessTime(fp,modify_time) ;
//        }
//    }
    /// <summary>
    /// ����ǰ׺���ƣ��Զ�����һ���µı���·��
    /// </summary>
    /// <param name="prefix"></param>
    /// <param name="fileext">�ļ���չ��</param>
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
    /// ����ǰ׺�����ļ����ݣ��Զ������ļ�����·��������
    /// </summary>
    /// <param name="prefix">֧���ļ�·�����������Զ�������ǰ׺</param>
    /// <param name="fileext">�ļ���չ��</param>
    /// <param name="cont">�������ļ�����</param>
    /// <returns>���������ļ��ĸ�ʽ��·��</returns>
    public String SaveToAutoPath(String prefix,String fileext,byte[] cont) throws IOException
    {
        String formatp = CreateAutoPath(prefix,fileext) ;
        if(formatp==null)
            throw new RuntimeException("ǰ׺["+prefix+"]���ܴ����Զ�·��(Cannot create auto path with prefix ["+prefix+"])") ;
        //Console.WriteLine("save to="+formatp) ;
        SaveToFile(formatp,cont) ;
        return formatp ;
    }


//    public String SaveXmlToAutoPath(String prefix,XmlDocument xd)
//    {
//        String formatp = CreateAutoPath(prefix,"xml") ;
//        if(formatp==null)
//            throw new RuntimeException("ǰ׺["+prefix+"]���ܴ����Զ�·��(Cannot create auto path with prefix ["+prefix+"])") ;
//        //Console.WriteLine("save to="+formatp) ;
//        SaveXmlToFile(formatp,xd) ;
//        return formatp ;
//    }
    /// <summary>
    /// ����һ���ļ�����һ���ļ�
    /// </summary>
    /// <param name="sorformatedpath">ԭʼ�ļ���ʽ��·��</param>
    /// <param name="tarformatedpath">Ŀ���ļ���ʽ��·��</param>
    /// <param name="overwrite">���Ŀ���ļ��Ѿ����ڣ��ò��������Ƿ�Ҫ���и���</param>
    public void CopyToFile(String sorformatedpath,String tarformatedpath,boolean overwrite) throws IOException
    {
        String fp = GetFilePath(sorformatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ�Դ�ļ�ϵͳ·��Unable to Get Sor File Path,may be has unknown prefix in:"+sorformatedpath) ;

        String tarfp = GetFilePath(tarformatedpath) ;
        if(tarfp==null)
            throw new RuntimeException("�����ҵ�Ŀ���ļ�ϵͳ·��Unable to Get Tar File Path,may be has unknown prefix in:"+tarformatedpath) ;

        File sorfi = new File(fp) ;
        if(!sorfi.exists())
            throw new RuntimeException("Դ�ļ�������source file is not existed:"+sorformatedpath) ;

        File tarfi = new File(tarfp) ;
        if(tarfi.exists()&&!overwrite)
        {
            throw new RuntimeException("Ŀ���ļ��Ѿ�����Target file is existed:"+tarformatedpath) ;
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
    /// �޸�һ���ļ����Ƶ���һ�����ơ�
    /// ���Ŀ�������Ѿ����ڣ����׳�����
    /// </summary>
    /// <param name="sorformatedpath">ԭʼ�ļ���ʽ��·��</param>
    /// <param name="tarformatedpath">Ŀ���ļ���ʽ��·��</param>
    public void MoveToFile(String sorformatedpath,String tarformatedpath)
    {
        String fp = GetFilePath(sorformatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ�Դ�ļ�ϵͳ·��Unable to Get Sor File Path,may be has unknown prefix in:"+sorformatedpath) ;
        String tarfp = GetFilePath(tarformatedpath) ;
        if(tarfp==null)
            throw new RuntimeException("�����ҵ�Ŀ���ļ�ϵͳ·��Unable to Get Tar File Path,may be has unknown prefix in:"+tarformatedpath) ;

        File tarfi = new File(tarfp) ;
        if(tarfi.exists())
        {
            throw new RuntimeException("Ŀ���ļ��Ѿ�����The file with target formated path is already existed!") ;
        }

        if(!tarfi.getParentFile().exists())
            tarfi.getParentFile().mkdirs();

        File fi = new File(fp) ;
        
        fi.renameTo(tarfi);
    }


    /// <summary>
    /// ���ݸ�ʽ����Ϣ����ص��ļ�����ɾ��
    /// </summary>
    /// <param name="formatedpath"></param>
    public void DeleteFile(String formatedpath)
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ���Ӧ���ļ�ϵͳ·��Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        if(fi.exists())
        {
            fi.delete();
        }
    }

    /// <summary>
    /// �ж�һ����ʽ���ļ��Ƿ����
    /// </summary>
    /// <param name="formatedpath"></param>
    /// <returns></returns>
    public boolean IsExistFile(String formatedpath)
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ���Ӧ���ļ�ϵͳ·����ǰ׺����Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        return fi.exists() ;
    }
    /// <summary>
    /// ���ݸ�ʽ��·����ö�Ӧ�ļ�������
    /// </summary>
    /// <param name="formatedpath">��ʽ��·��</param>
    /// <returns>�ļ�������</returns>
    public byte[] ReadFromFile(String formatedpath) throws IOException
    {
        String fp = GetFilePath(formatedpath) ;
        if(fp==null)
            throw new RuntimeException("�����ҵ���Ӧ���ļ�ϵͳ·��Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;

        File fi = new File(fp) ;
        if(!fi.exists())
            throw new RuntimeException("�ļ�������File is not existed:"+formatedpath) ;

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
//            throw new DataAccessException("�����ҵ���Ӧ���ļ�ϵͳ·��Unable to Get File Path,may be has unknown prefix in:"+formatedpath) ;
//        XmlDocument xd = new XmlDocument() ;
//        xd.Load(fp) ;
//        return xd ;
//    }

    /// <summary>
    /// ���ݸ�ʽ��·�����Mime����
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
    /// ���ݸ�ʽ��·���õ�������ļ�ϵͳ�е�·����ͨ����·�����Զ��ļ�����IO������
    /// </summary>
    /// <param name="formatpath">��ʽ����·��</param>
    /// <returns>��������ڶ�Ӧ���ļ�ϵͳǰ׺���򷵻�null</returns>
    public String GetFilePath(String formatpath)
    {
//        if (formatpath.startsWith("."))
//        {//����������ļ�·��
//            String ff = confFileDirInfo.FullName + "/" + formatpath;
//            FileInfo f = new FileInfo(ff);
//            return f.FullName;
//        }

        String prefix = null ;
        String relatedpath = null ;
        String[] pr = splitFormatPath(formatpath) ;
        if(pr==null)
            throw new RuntimeException("���Ϸ��ĸ�ʽ��·��Illegal Formated Path:"+formatpath) ;

        FileBase fbase = (FileBase)prefix2filebase.get(pr[0]);
        if(fbase==null)
            return null ;

        return fbase.fileBase+pr[1] ;
    }

    /// <summary>
    /// ���ݸ�ʽ��·���õ�������⽻�ܹ����ʵ�URL·��
    /// </summary>
    /// <param name="formatpath">��ʽ����·��</param>
    /// <returns>��������ڶ�Ӧ��URLǰ׺���򷵻�null</returns>
    public String GetUrlPath(String formatpath)
    {
    	String[] pr = splitFormatPath(formatpath) ;
        if(pr==null)
            throw new RuntimeException("���Ϸ��ĸ�ʽ��·��Illegal Formated Path:"+formatpath) ;

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
