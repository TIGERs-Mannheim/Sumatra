/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;


public class MessageSignerTest
{
	
	@Test
	public void signingTest() throws IOException
	{
		// Check Signing by singing and verifying some test data
		MessageSigner signer = new MessageSigner(
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.key.pem.pkcs8", Charset.forName("UTF-8")),
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.pub.pem", Charset.forName("UTF-8")));
		byte[] data = "Foo".getBytes();
		
		byte[] sig = signer.sign(data);
		Assert.assertTrue(signer.verify(data, sig));
		Assert.assertFalse(signer.verify("Bar".getBytes(), sig));
	}
	
}