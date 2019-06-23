/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 4, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;


/**
 * Get the ball by directly driving towards it instead of driving around it
 * (according to lookAtTarget)
 * Then, determining the best (or at least a good) potential receiver or the
 * goal to look at and turn around the ball.
 * This play should not be used, if an opponent is at the ball, too!
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class IntelligentBallGetterPlay extends ABallGetterPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final BallGetterRole	ballGetter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public IntelligentBallGetterPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		addCriterion(new BallPossessionCrit(EBallPossession.NO_ONE));
		
		IVector2 ballPos = new Vector2(aiFrame.worldFrame.ball.getPos());
		
		ballGetter = new BallGetterRole(ballPos, EBallContact.DISTANCE);
		addAggressiveRole(ballGetter, ballPos);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		ballGetter.setViewPoint(calcReceiver(frame));
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (ballGetter.isCompleted())
		{
			changeToFinished();
		}
	}
	
	
	private IVector2 calcReceiver(AIInfoFrame currentFrame)
	{
		List<BotID> botsSorted = AiMath.getTigerBotsNearestToPointSorted(currentFrame, ballGetter.getPos());
		
		for (BotID botId : botsSorted)
		{
			if (botId.equals(ballGetter.getBotID()))
			{
				continue;
			}
			IVector2 start;
			start = ballGetter.getPos();
			IVector2 end = currentFrame.worldFrame.tigerBotsVisible.get(botId).getPos();
			float raySize = AIConfig.getGeometry().getBotRadius() * 2;
			if (GeoMath.p2pVisibility(currentFrame.worldFrame, start, end, raySize))
			{
				return end;
			}
		}
		return currentFrame.worldFrame.tigerBotsVisible.get(botsSorted.get(0)).getPos();
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
