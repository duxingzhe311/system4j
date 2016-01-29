package com.dw.system ;

import java.io.* ;
import java.util.Hashtable ;
import javax.servlet.http.HttpServletResponse ;

/**
 * @deprecated 因为这个类并不适合放在system中，因此，该类将被废弃。其中的MIME在未来将单独使用配置文件管理。
 * @see FileDownloader
 */

public class ImageWriter
{
	static Hashtable MIME = new Hashtable () ;

	static
	{
		MIME.put ("jpeg" , "jpeg") ;
		MIME.put ("jpg" , "jpeg") ;
		MIME.put ("jfif" , "jpeg") ;
		MIME.put ("jfif-tbnl" , "jpeg") ;
		MIME.put ("jpe" , "jpeg") ;
		MIME.put ("jfif" , "jpeg") ;
		MIME.put ("tiff" , "tiff") ;
		MIME.put ("tif" , "tiff") ;
		MIME.put ("gif" , "gif") ;
	}

	public static void write (HttpServletResponse response , String fileName , byte [] bytes) throws IOException
	{
		int index = 0 ;
		String ext = "gif" ;
		if ((index = fileName.indexOf ('.')) > 0)
			ext = fileName.substring (index + 1) ;

		response.setContentType ("image/" + MIME.get (ext)) ;

		OutputStream outStream = response.getOutputStream () ;

		outStream.write (bytes , 0 , bytes.length) ;
		outStream.flush () ;
		outStream.close () ;
	}
}