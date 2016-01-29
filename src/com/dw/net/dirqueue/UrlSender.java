package com.dw.net.dirqueue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import com.dw.system.encrypt.DES;
import com.dw.system.xmldata.XmlDataWithFile;

public class UrlSender
{
	public static final int SEND_RES_SUCC = 1;

	public static final int SEND_RES_CONN_FAILED = 0;

	public static final int SEND_RES_AUTH_FAILED = -1;

	public static final int SEND_RES_SEND_FAILED = -2;

	public static final int SEND_RES_RECV_RESP_FAILED = -3;

	// / 检测备用url地址，该方法用来支持更加可靠的链接所提供的备用Url计算
	static String checkBkUrl(String cururl)
	{
		String bku = Util.getServerUrlBk();
		if (bku == null || bku.trim().equals(""))
			return null;

		if (!cururl.startsWith(Util.getServerUrl() + "/"))
			return null;

		String r = Util.getServerUrlBk()
				+ cururl.substring(Util.getServerUrl().length());

		return r;
	}

	Socket client = null;

	InputStream inputStream = null;

	OutputStream outputStream = null;

	public UrlSender()
	{
	}

	private byte[] createAuthLine(String msgtype)
	{
		String idkey = Util.getIdKey();
		if (idkey == null || idkey.equals(""))
			return null;

		int p = idkey.indexOf(':');
		if (p <= 0)
			return null;

		String id = idkey.substring(0, p);
		String key = idkey.substring(p + 1);

		// 产生验证串
		String uuid = UUID.randomUUID().toString();
		String secstr = DES.encode(uuid, key);

		// 加密之,并提交给服务器
		if (msgtype == null || msgtype.equals(""))
		{
			return (id + ":" + uuid + ":" + secstr + "\n").getBytes();
		}
		else
		{
			return (id + ":" + uuid + ":" + secstr + ":" + msgtype + "\n")
					.getBytes();
		}
	}

	public int sendXmlDataWithFileNoCB(String url, String msgtype,
			XmlDataWithFile postdata, boolean bdel_localfile)
	{
		try
		{
			byte[] authline = createAuthLine(msgtype);
			if (authline == null)
				return SEND_RES_AUTH_FAILED;

			HttpURLConnection urlc = null;
			try
			{
				URL u = new URL(url);
				urlc = (HttpURLConnection) u.openConnection();

				urlc.setDoOutput(true);

				urlc.setRequestProperty("Content-Length",
						(postdata.calculateSendLen() + authline.length) + "");
				urlc.setRequestProperty("Content-Type",
						"application/octet-stream");

				outputStream = urlc.getOutputStream();
			}
			catch (Exception connexp)
			{
				String bkurl = checkBkUrl(url);
				connexp.printStackTrace();
				if (bkurl != null && !bkurl.equals(""))
				{
					return sendXmlDataWithFileNoCB(bkurl, msgtype, postdata,
							bdel_localfile);
				}
				return SEND_RES_CONN_FAILED;
			}

			try
			{
				outputStream.write(authline, 0, authline.length);
				outputStream.flush();

				postdata.writeToStream(outputStream,
						new XmlDataWithFile.DefaultSendCB());

				outputStream.flush();
				outputStream.close();
				outputStream = null;
			}
			catch (Exception eee)
			{
				return SEND_RES_SEND_FAILED;
			}

			String reps_str = null;
			try
			{
				inputStream = urlc.getInputStream();
				reps_str = readStrFromStreamUtf8(inputStream).toString();
				if (reps_str != null)
					reps_str = reps_str.trim().toLowerCase();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return SEND_RES_RECV_RESP_FAILED;
			}

			if ("reconnect".equals(reps_str))
				return SEND_RES_CONN_FAILED;

			if ((!"succ".equals(reps_str)) && (!"ok".equals(reps_str)))
				return SEND_RES_SEND_FAILED;

			if (bdel_localfile)
			{// del local file
				List<String> lfps = postdata.getLocalFilePaths();
				if (lfps != null)
				{
					for (String lfp : lfps)
					{
						try
						{
							File tmpf = new File(lfp);
							if (tmpf.exists())
								tmpf.delete();
						}
						catch (Exception eee)
						{
						}
					}
				}
			}
			return SEND_RES_SUCC;
		}
		finally
		{
			close();
		}
	}

	public static StringBuilder readStrFromStreamUtf8(InputStream s)
			throws IOException
	{
		StringBuilder strResult = new StringBuilder();

		InputStreamReader sr = new InputStreamReader(s, "UTF-8");
		char[] read = new char[256];

		int count = sr.read(read, 0, 256);
		while (count > 0)
		{
			strResult.append(read, 0, count);
			count = sr.read(read, 0, 256);
		}

		return strResult;
	}

	static void readFromStreamAndWriteToFile(InputStream s, File fi)
			throws IOException
	{
		if (!fi.getParentFile().exists())
		{
			fi.getParentFile().mkdirs();
		}

		FileOutputStream fs = null;
		try
		{
			fs = new FileOutputStream(fi);// , FileMode.Create,
											// FileAccess.Write);
			byte[] read = new byte[256];
			int count = -1;
			do
			{
				count = s.read(read, 0, 256);
				if (count < 0)
					break;

				fs.write(read, 0, count);
			}
			while (count > 0);
		}
		finally
		{
			if (fs != null)
			{
				try
				{
					fs.close();
				}
				catch (Exception ee)
				{
				}
			}
		}
	}

	public void close()
	{
		if (inputStream != null)
		{
			try
			{
				inputStream.close();
				inputStream = null;
			}
			catch (Exception e)
			{

			}
		}

		if (outputStream != null)
		{
			try
			{
				outputStream.close();
				outputStream = null;
			}
			catch (Exception e)
			{

			}
		}

		if (client != null)
		{
			try
			{
				client.close();
				client = null;
			}
			catch (Exception e)
			{

			}
		}
	}

	public void Dispose()
	{
		close();
	}
}
