/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.log4j.Logger;


/**
 * Utility Class for Message Signatures
 * This class provides methods to load RSA Keys and to sign and verify arbitrary byte data.
 * The signatures are based on RSA.
 * NOTE: to generate a new key, use the tool in the SSL-Game-Controller repository
 * and process the private key with the script in this module to get a PKCS8-Key-File
 */
public class MessageSigner
{
	private static final Logger log = Logger.getLogger(MessageSigner.class.getName());
	private static final String SIGNING_ALGORITHM = "SHA256WITHRSA";
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	
	/**
	 * Default constructor without keys
	 */
	public MessageSigner()
	{
		// no key will be loaded by default
	}
	
	
	/**
	 * Constructor, pass a path to a Private and Public Key
	 * 
	 * @param privateKey Path to a private key in PKCS8 Format
	 * @param publicKey Path to a public key for verification
	 */
	public MessageSigner(String privateKey, String publicKey)
	{
		this.privateKey = getPrivateKey(privateKey);
		this.publicKey = getPublicKey(publicKey);
	}
	
	
	/**
	 * read RSA PKCS8 Private key
	 *
	 * @param rawKey the raw read key
	 * @return PrivateKey instance
	 */
	private PrivateKey getPrivateKey(String rawKey)
	{
		try
		{
			// Clean and Decode RSA Key
			rawKey = rawKey.replace("-----BEGIN PRIVATE KEY-----", "");
			rawKey = rawKey.replace("-----END PRIVATE KEY-----", "");
			// Remove Whitespace
			rawKey = rawKey.replaceAll("\\s+", "");
			byte[] dec = Base64.getDecoder().decode(rawKey);
			
			// Get Key
			KeyFactory factory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(dec);
			return factory.generatePrivate(spec);
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e)
		{
			log.warn("Generating private key failed for " + rawKey, e);
		}
		
		return null;
	}
	
	
	/**
	 * load RSA Public Key from File
	 *
	 * @param rawKey the raw read key
	 * @return PublicKey Instance
	 */
	private PublicKey getPublicKey(String rawKey)
	{
		try
		{
			// Clean and Decode RSA Key
			rawKey = rawKey.replace("-----BEGIN PUBLIC KEY-----", "");
			rawKey = rawKey.replace("-----END PUBLIC KEY-----", "");
			// Remove Whitespace
			rawKey = rawKey.replaceAll("\\s+", "");
			byte[] dec = Base64.getDecoder().decode(rawKey);
			
			// Get Key
			KeyFactory factory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(dec);
			return factory.generatePublic(spec);
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NullPointerException e)
		{
			log.warn("Generating public key failed for " + rawKey, e);
		}
		
		return null;
	}
	
	
	/**
	 * Generate Signature
	 *
	 * @param data Data that should be signed
	 * @return The generated signature
	 */
	public byte[] sign(byte[] data)
	{
		if (privateKey == null)
		{
			log.debug("Skipping message signing: no private key");
			return new byte[0];
		}
		
		try
		{
			Signature sig = Signature.getInstance(SIGNING_ALGORITHM);
			sig.initSign(privateKey);
			sig.update(data);
			return sig.sign();
		} catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e)
		{
			log.warn("Message signing failed", e);
		}
		
		return new byte[0];
	}
	
	
	/**
	 * Check if a signature is valid
	 *
	 * @param data Data that was signed
	 * @param sig Signature to check
	 * @return True if Signature matches to data
	 */
	public boolean verify(byte[] data, byte[] sig)
	{
		if (publicKey == null)
		{
			log.debug("Skipping message verification: no public key");
			return true;
		}
		try
		{
			Signature signature = Signature.getInstance(SIGNING_ALGORITHM);
			signature.initVerify(publicKey);
			signature.update(data);
			return signature.verify(sig);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e)
		{
			log.warn("Error while verifying Signature", e);
		}
		return false;
	}
}
