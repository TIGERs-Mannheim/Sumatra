/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class ClientRESTHandlerTest
{
	
	@Test
	public void testNewUser()
	{
		ClientRESTHandler clientRESTHandler = new ClientRESTHandler();
		long id = clientRESTHandler.createNewUserID();
		long id2 = clientRESTHandler.createNewUserID();
		
		assertThat(id).isNotEqualTo(id2);
		assertThat(id).isLessThan(id2);
	}
	
}
