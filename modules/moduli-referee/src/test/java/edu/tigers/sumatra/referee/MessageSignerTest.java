/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MessageSignerTest
{
	
	@Test
	void signingTest() throws IOException
	{
		// Check Signing by singing and verifying some test data
		MessageSigner signer = new MessageSigner(
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.key.pem.pkcs8", StandardCharsets.UTF_8),
				IOUtils.resourceToString("/edu/tigers/sumatra/game/test.pub.pem", StandardCharsets.UTF_8));
		byte[] data = "Foo".getBytes();
		
		byte[] sig = signer.sign(data);
		assertTrue(signer.verify(data, sig));
		assertFalse(signer.verify("Bar".getBytes(), sig));
	}
	
}