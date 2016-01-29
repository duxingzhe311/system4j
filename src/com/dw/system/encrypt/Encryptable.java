package com.dw.system.encrypt;

/**
* Encryptable provide a common algorithm interface for encrypt and decrypt.
* All classes implemented Encryptable must provide a encrypt and a decrypt algorithm.
*/
public interface Encryptable
{
	/**
	* Return the algorithm name
	* @return String algorithm 's name 
	*/ 
	String getAlgoName();

	/**
	* Encrypt a String
	* @param String Source String
	* @return String String after encrypt
	*/
	String encrypt(String s);

	/**
	* Decrypt a String
	* @param String Encrypted Source String 
	* @return Decrypted String 
	*/
	String decrypt(String s);

	/**
	* Encrypt a byte array
	* @param byte[] Source byte array
	* @return byte[] byte array after encrypted.
	*/
	byte [] encrypt(byte [] array);

	/**
	* Decrypt a byte array
	* @param byte[] Encrypted byte array
	* @return byte[] Decrypted byte array
	*/
	byte [] decrypt(byte [] array);
}