package com.dw.system ;

import java.io.* ;
import java.util.Hashtable ;
import javax.servlet.http.HttpServletResponse ;

/**
 * 该类提供文件下载功能。<br>
 * 下载文件，需要在responase中设定response-header：<br>
 * <b>Content-Disposition=attachment; filename=filename.ext</b><br>
 * 这样可以在IE中显示保存/打开对话框，并提示文件名。
 *
 */
public class FileDownloader
{
	static Hashtable MIME = new Hashtable () ;

	static
	{
		MIME.put ("jpeg" , "image/jpeg") ;
		MIME.put ("jpg" , "image/jpeg") ;
		MIME.put ("jfif" , "image/jpeg") ;
		MIME.put ("jfif-tbnl" , "image/jpeg") ;
		MIME.put ("jpe" , "image/jpeg") ;
		MIME.put ("jfif" , "image/jpeg") ;
		MIME.put ("tiff" , "image/tiff") ;
		MIME.put ("tif" , "image/tiff") ;
		MIME.put ("gif" , "image/gif") ;
		MIME.put ("xls" , "application/x-msexcel") ;
		MIME.put ("doc" , "application/msword") ;
		MIME.put ("ppt" , "application/x-mspowerpoint") ;
		MIME.put ("zip" , "application/x-zip-compressed") ;
	}

	/**
	 * 将指定文件名的文件内容写入response，并提示是否保存文件。
	 * @param down 是否提示下载。
	 */
	public static void write (HttpServletResponse response , String fileName , byte [] bytes , boolean down)
		throws IOException
	{
		int index = 0 ;
		String ext = "" ;
		if ((index = fileName.indexOf ('.')) > 0)
			ext = fileName.substring (index + 1) ;
		String mime = (String) MIME.get (ext) ;
		if (mime == null)
			response.setContentType ("application/x-msdownload") ;
		else
			response.setContentType (mime) ;
		if (down)
			response.setHeader("Content-Disposition" , "attachment; filename=" + fileName) ;

		OutputStream outStream = response.getOutputStream () ;

		outStream.write (bytes , 0 , bytes.length) ;
		outStream.flush () ;
		outStream.close () ;
	}

	/**
	 * 将指定文件名的文件内容从流中读出，并写入response，并提示是否保存文件。
	 * @param down 是否提示下载。
	 */
	public static void write (HttpServletResponse response , String fileName , InputStream in , boolean down)
		throws IOException
	{
		int index = 0 ;
		String ext = "" ;
		if ((index = fileName.indexOf ('.')) > 0)
			ext = fileName.substring (index + 1) ;
		String mime = (String) MIME.get (ext) ;
		if (mime == null)
			response.setContentType ("application/x-msdownload") ;
		else
			response.setContentType (mime) ;
		if (down)
			response.setHeader("Content-Disposition" , "attachment; filename=" + fileName) ;

		OutputStream outStream = response.getOutputStream () ;
		byte [] buf = new byte [512] ;
		int len = -1 ;
		while (true)
		{
			len = in.read (buf) ;
			if (len <= 0)
				break ;

			outStream.write (buf , 0 , len) ;
			outStream.flush() ;
		}

		outStream.close() ;
	}
	/**
	 * 将文件内容写入response。
	 */
	public static void write (HttpServletResponse response , String contentType , byte [] bytes)
		throws IOException
	{
		/*
		int index = 0 ;
		String ext = "" ;
		if ((index = fileName.indexOf ('.')) > 0)
			ext = fileName.substring (index + 1) ;
		String mime = (String) MIME.get (ext) ;
		if (mime == null)
			response.setContentType ("application/x-msdownload") ;
		else
		*/
		response.setContentType (contentType) ;
		// if (down)
		// 	response.setHeader("Content-Disposition" , "attachment; filename=" + fileName) ;

		OutputStream outStream = response.getOutputStream() ;

		outStream.write (bytes , 0 , bytes.length) ;
		outStream.flush () ;
		outStream.close () ;
	}
}