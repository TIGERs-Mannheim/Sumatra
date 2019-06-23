/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.10.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai;


import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.AttackerSetPiece;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSender;


/**
 * This tests different
 * 
 * @author Gero
 * 
 */
public class RoleTest
{
	private final int		BOT_ID	= 1;
	
	private ARole			attSetPiece;
	private ARole			ballGetter;
	// private ARole getBallAndShootToTarget;
	private PassSender	passSender;
	
	
	// private ARole testRole;
	

	@Before
	public void setUp() throws Exception
	{
		attSetPiece = new AttackerSetPiece();
		attSetPiece.assignBotID(BOT_ID);
		
		ballGetter = new BallGetterRole(EGameSituation.GAME);
		ballGetter.assignBotID(BOT_ID);
		
		passSender = new PassSender(EGameSituation.GAME);
		passSender.assignBotID(BOT_ID);
		
		// testRole = new TestRole();
		// testRole.setBotID(BOT_ID);
	}
	

	@Test
	public void testGetDestination()
	{
		AIInfoFrame aiFrame = AITestHelper.createAIInfoFrame(BOT_ID);
		
		attSetPiece.doUpdate(aiFrame);
		assertNotNull(attSetPiece.getDestination());
		
		ballGetter.doUpdate(aiFrame);
		assertNotNull(ballGetter.getDestination());
		
		// getBallAndShootToTarget.update(aiFrame);
		// assertNotNull(getBallAndShootToTarget.getDestination());
		
		passSender.doUpdate(aiFrame);
		assertNotNull(passSender.getDestination());
		
		// testRole.update(aiFrame);
		// assertNotNull(testRole.getDestination());
	}
}
