/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 20, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.athena.apollon.util;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AKnowledgeField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgeFieldBotAssignment;


/**
 * Test class for the KnowledgeFieldBotAssignment.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class KnowledgeFieldBotAssignmentHelper extends AKnowledgeFieldHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public AKnowledgeField createKnowledgeField(List<IVector2> pTigers, List<IVector2> pFoes, IVector3 pBall,
			BallPossession ballPossession)
	{
		BotIDMap<TrackedBot> foeBots = new BotIDMap<TrackedBot>();
		BotIDMap<TrackedTigerBot> tigerBots = new BotIDMap<TrackedTigerBot>();
		
		int botNum = 0;
		for (IVector2 pos : pTigers)
		{
			TrackedTigerBot bot = createTigerBot(new BotID(botNum), pos);
			tigerBots.put(bot.getId(), bot);
			botNum++;
		}
		
		botNum = 0;
		for (IVector2 pos : pFoes)
		{
			TrackedBot bot = createFoeBot(new BotID(botNum, ETeam.OPPONENTS), pos);
			foeBots.put(bot.getId(), bot);
			botNum++;
		}
		
		
		TrackedBall ball = new TrackedBall(pBall, new Vector3f(), new Vector3f(), 0f, true);
		
		return new KnowledgeFieldBotAssignment(BotIDMapConst.unmodifiableBotIDMap(tigerBots),
				BotIDMapConst.unmodifiableBotIDMap(foeBots), ball, ballPossession);
	}
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
