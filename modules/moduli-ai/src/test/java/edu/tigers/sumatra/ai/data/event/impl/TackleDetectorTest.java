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
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
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
public class TackleDetectorTest
{
	/**
	 * This will test the Tackle detector to see if there is a tackle active for our bots
	 */
	@Test
	public void thereShouldBeATackleForTwoBotsIfTheBallPossessionIsBoth()
	{
		BotID tackleBotIDTiger = BotID.createBotId(0, ETeamColor.BLUE);
		BotID tackleBotIDOpponent = BotID.createBotId(0, ETeamColor.YELLOW);
		
		GameEventFrame expectedFrame = new GameEventFrame(tackleBotIDTiger, tackleBotIDOpponent);
		
		IGameEventDetector detector = new TackleDetector();
		
		TacticalField tacticalField = TacticalFieldTestUtils
				.initializeTacticalFieldJustWithBallPossessionForSide(EBallPossession.BOTH);
		
		BaseAiFrame mockFrame = BaseAIFrameTestUtils.mockBaseAiFrameForWorldFrameID(5);
		
		GameEventFrame actualFrame = detector.getActiveParticipant(tacticalField, mockFrame);
		
		Assert.assertEquals(expectedFrame, actualFrame);
	}
}
