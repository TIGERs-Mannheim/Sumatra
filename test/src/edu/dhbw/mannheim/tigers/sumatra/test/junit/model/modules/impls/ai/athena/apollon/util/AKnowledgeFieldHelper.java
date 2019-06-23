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

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedFoeBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AKnowledgeField;


/**
 * Abstract test class for a knowledgeField
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class AKnowledgeFieldHelper
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Default implementation for creating a tiger bot with default values
	 * 
	 * @param botId
	 * @param pos
	 * @return
	 */
	protected static TrackedTigerBot createTigerBot(BotID botId, IVector2 pos)
	{
		return new TrackedTigerBot(botId, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0f, 0f, 0f, 0, null);
	}
	
	
	/**
	 * Default implementation for creating a foe bot with default values
	 * 
	 * @param botId
	 * @param pos
	 * @return
	 */
	protected static TrackedBot createFoeBot(BotID botId, IVector2 pos)
	{
		return new TrackedFoeBot(botId, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0);
	}
	
	
	/**
	 * Create a new knowledgeField
	 * Must be implemented by the actual KnowledgeField
	 * 
	 * @param pTigers
	 * @param pFoes
	 * @param pBall
	 * @param ballPossession
	 * @return
	 */
	public abstract AKnowledgeField createKnowledgeField(List<IVector2> pTigers, List<IVector2> pFoes, IVector3 pBall,
			BallPossession ballPossession);
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
