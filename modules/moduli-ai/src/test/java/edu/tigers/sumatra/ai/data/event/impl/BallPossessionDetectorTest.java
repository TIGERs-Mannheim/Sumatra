/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 29, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event.impl;

import junit.framework.Assert;

import org.junit.Test;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.event.GameEventFrame;
import edu.tigers.sumatra.ai.data.event.IGameEventDetector;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.metis.testutils.BaseAIFrameTestUtils;
import edu.tigers.sumatra.ai.data.metis.testutils.TacticalFieldTestUtils;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class BallPossessionDetectorTest
{
	
	/**
	 * This tests the basic funtionality of the ball possession detector
	 */
	@Test
	public void shouldDetectBallPossessionForBot1BlueIfThisBotGotTheBall()
	{
		BotID touchingBot = BotID.createBotId(1, ETeamColor.BLUE);
		
		GameEventFrame expectedEvent = new GameEventFrame(touchingBot);
		TacticalField stubTacticalField = TacticalFieldTestUtils
				.initializeTacticalFieldJustWithPossessingBot(touchingBot);
		
		BaseAiFrame mockedFrame = BaseAIFrameTestUtils.mockBaseAiFrameForWorldFrameID(5);
		
		IGameEventDetector detector = new BallPossessionDetector();
		
		GameEventFrame actualEvent = detector.getActiveParticipant(stubTacticalField, mockedFrame);
		
		Assert.assertEquals(expectedEvent, actualEvent);
	}
}
