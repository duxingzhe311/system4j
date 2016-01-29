package com.dw.system.encrypt;

import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import com.dw.system.Convert;

public class DesJava
{
	private final static String DES = "DES";

	/**
	 * ����
	 * 
	 * @param src
	 *            ����Դ
	 * @param key
	 *            ��Կ�����ȱ�����8�ı���
	 * @return ���ؼ��ܺ������
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] src, byte[] key) throws Exception
	{
		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom sr = new SecureRandom();
		// ��ԭʼ�ܳ����ݴ���DESKeySpec����
		DESKeySpec dks = new DESKeySpec(key);
		// ����һ���ܳ׹�����Ȼ��������DESKeySpecת����
		// һ��SecretKey����
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher����ʵ����ɼ��ܲ���
		Cipher cipher = Cipher.getInstance(DES);
		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		// ���ڣ���ȡ���ݲ�����
		// ��ʽִ�м��ܲ���
		return cipher.doFinal(src);
	}

	/**
	 * ����
	 * 
	 * @param src
	 *            ����Դ
	 * @param key
	 *            ��Կ�����ȱ�����8�ı���
	 * @return ���ؽ��ܺ��ԭʼ����
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] src, byte[] key) throws Exception
	{
		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom sr = new SecureRandom();
		// ��ԭʼ�ܳ����ݴ���һ��DESKeySpec����
		DESKeySpec dks = new DESKeySpec(key);
		// ����һ���ܳ׹�����Ȼ��������DESKeySpec����ת����
		// һ��SecretKey����
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher����ʵ����ɽ��ܲ���
		Cipher cipher = Cipher.getInstance(DES);
		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		// ���ڣ���ȡ���ݲ�����
		// ��ʽִ�н��ܲ���
		return cipher.doFinal(src);
	}
	
	
//	 DES�㷨Ҫ����һ�������ε������Դ
	SecureRandom sr = new SecureRandom();
	// ��ԭʼ�ܳ����ݴ���һ��DESKeySpec����
	DESKeySpec dks = null ;
	// ����һ���ܳ׹�����Ȼ��������DESKeySpec����ת����
	// һ��SecretKey����
	SecretKeyFactory keyFactory = null;//SecretKeyFactory.getInstance(DES);
	SecretKey securekey = null;//keyFactory.generateSecret(dks);
	// Cipher����ʵ����ɽ��ܲ���
	Cipher enCipher = null,deCipher=null;//Cipher.getInstance(DES);
	
	public DesJava(byte[] key)
		throws Exception
	{
		//System.out.println("key=="+new String(key)+" len="+key.length);
		byte[] kk = new byte[8] ;
		for(int i = 0 ;i<8;i++)
			kk[i] = 0 ;
		for(int i = 0 ; i < 8 && i<key.length ;i++)
		{
			kk[i] = key[i] ;
		}
		dks = new DESKeySpec(kk);
		
		keyFactory = SecretKeyFactory.getInstance(DES);
		
		securekey = keyFactory.generateSecret(dks);
		// Cipher����ʵ����ɽ��ܲ���
		deCipher = Cipher.getInstance(DES);
		// ���ܳ׳�ʼ��Cipher����
		deCipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		
		enCipher = Cipher.getInstance(DES);
		// ���ܳ׳�ʼ��Cipher����
		enCipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
	}
	
	public byte[] encrypt(byte[] src,int offset,int len) throws IllegalBlockSizeException, BadPaddingException
	{
		return enCipher.doFinal(src,offset,len) ;
	}
	
	public byte[] encrypt(byte[] src) throws IllegalBlockSizeException, BadPaddingException
	{
		return enCipher.doFinal(src) ;
	}
	
	public byte[] decrypt(byte[] src,int offset,int len) throws IllegalBlockSizeException, BadPaddingException
	{
		return deCipher.doFinal(src,offset,len) ;
	}

	
	public byte[] decrypt(byte[] src) throws IllegalBlockSizeException, BadPaddingException
	{
		return deCipher.doFinal(src) ;
	}
	
	public static void main(String[] args)
		throws Throwable
	{
		String data = "12345678";//>?:jklmn" ;
		String key = "Th345678" ;
		byte[] dd = encrypt(data.getBytes(), key.getBytes()) ;
		String encdata = Convert.byteArray2HexStr(dd).toUpperCase();
		String decdata = new String(decrypt(dd,key.getBytes()));
		
		System.out.println("key="+key) ;
		System.out.println("enc="+encdata+" ddlen="+dd.length) ;
		System.out.println("dec="+decdata) ;
		
		key="1234567890" ;
		DesJava dj = new DesJava(key.getBytes()) ;
		
		data="123456789012345" ;
		
		dd = dj.encrypt(data.getBytes()) ;
		encdata = Convert.byteArray2HexStr(dd).toUpperCase();
		decdata = new String(dj.decrypt(dd));
		
		System.out.println("\ndlen="+data.getBytes().length+"  data="+data);
		System.out.println("key="+key) ;
		System.out.println("enc="+encdata+" ddlen="+dd.length) ;
		System.out.println("dec ="+decdata) ;
	}
}