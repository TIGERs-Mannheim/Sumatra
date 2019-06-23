/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.11.2012
 * Author(s): jan
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;


/**
 * The good old Rambo is back.
 * 
 * It should be chosen if the enemy has got the ball, it will drive to the ball and try to divorce the enemey and the
 * ball
 * 
 * @author Dirk Klostermann
 * 
 */
public class RamboPlay extends AOffensivePlay

{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger	log	= Logger.getLogger(RamboPlay.class.getName());
	
	private final MoveRole			ramboRole;
	private EState						state	= EState.DIVORCE;
	
	enum EState
	{
		DIVORCE;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public RamboPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		addCriterion(new BallPossessionCrit(EBallPossession.THEY));
		ramboRole = new MoveRole(EMoveBehavior.NORMAL);
		addAggressiveRole(ramboRole, aiFrame.worldFrame.ball.getPos());
		ramboRole.getMoveCon().setBallObstacle(false);
		ramboRole.updateLookAtTarget(Vector2.ZERO_VECTOR);
		if (!ballPossesionThey(aiFrame))
		{
			changeToFailed();
			log.info("Rambo play failed because the opponent has not the ball");
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * TODO to be optimized
	 * 
	 * @param frame
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<IVector2> getIntermediatePoints(AIInfoFrame frame)
	{
		List<IVector2> intermediates = new ArrayList<IVector2>();
		// TrackedBot ballframe.tacticalInfo.getBallPossession().getOpponentsId();
		// if (isSecPointNeeded(destination))
		// {
		// if (ballPossesionThey(frame))
		// {
		// return intermediates;
		// }
		// }
		
		return intermediates;
	}
	
	
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		ramboRole.updateLookAtTarget(ramboRole.getPos());
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		switch (state)
		{
			case DIVORCE:
				IVector2 ball = currentFrame.worldFrame.ball.getPos();
				if (!ball.equals(ramboRole.getDestination(), 0.1f))
				{
					ramboRole.updateDestination(ball);
					ramboRole.updateLookAtTarget(ramboRole.getPos());
				}
				IVector2 rambo = currentFrame.worldFrame.getTiger(ramboRole.getBotID()).getPos();
				float tol = AIConfig.getGeometry().getBotRadius();
				if (ball.equals(rambo, tol))
				{
					changeToSucceeded();
				}
				if (!ballPossesionBoth(currentFrame))
				{
					changeToFinished();
				}
				break;
		}
	}
	
	
	private boolean ballPossesionThey(AIInfoFrame frame)
	{
		return (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.THEY);
	}
	
	
	private boolean ballPossesionBoth(AIInfoFrame frame)
	{
		return (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.BOTH)
				|| ballPossesionThey(frame);
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// idle
	}
	
	
	@Override
	protected void timedOut(AIInfoFrame frame)
	{
		log.warn("Failed timeout");
		changeToFailed();
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
