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

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;

/**
 * This class is meant to help programmers with creating common unit tests and needed test-drivers.
 * 
 * @author Gero
 * @TODO: Malte asks: Not better put these methods in the related Classes?
 * 		Like: WorldFrame.createWorldFrame()
 */
public class AITestHelper
{
	/**
	 * @param botId ID of the bot that is needed for testing
	 * @return
	 */
	public static WorldFrame createWorldFrame(int botId)
	{
		Map<Integer, TrackedBot> enemies = new HashMap<Integer, TrackedBot>();
		

		Map<Integer, TrackedTigerBot> tigers = new HashMap<Integer, TrackedTigerBot>();
		TrackedTigerBot bot = new TrackedTigerBot(botId, new Vector2f(),new Vector2f(),new Vector2f(),0, 0f,0f,0f,0, 0,0, false);
		tigers.put(bot.id, bot);
		
		TrackedBall ball = new TrackedBall(-1, 0,0,0,0,0,0,0,0,0,0,true);
		
		
		return new WorldFrame(enemies, tigers, ball, 0, 0, 0);
	}
	
	
	/**
	 * @see #createWorldFrame(int)
	 * 
	 * @param botId
	 * @return
	 */
	public static AIInfoFrame createAIInfoFrame(int botId)
	{
		return new AIInfoFrame(createWorldFrame(botId), createRefereeMsg());
	}
	
	
	public static RefereeMsg createRefereeMsg()
	{
		return new RefereeMsg(Integer.MIN_VALUE, null, Integer.MIN_VALUE, Integer.MIN_VALUE, Short.MIN_VALUE);
	}
}
