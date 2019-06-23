/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.06.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole.EBallContact;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PassSenderRole;


/**
 * This play should try to break the ball clear.<br>
 * For this, the ball tries to get the ball and shoot it into the middle of the field
 * without looking for a target.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BallBreakingPlay extends ABallGetterPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float		MIN_DISTANCE_FOR_TURN	= 250;
	
	private static final int		SHOOT_AWAY_DISTANCE		= 500;
	
	private final BallGetterRole	ballGetter;
	private PassSenderRole			shooter;
	private EState						state							= EState.GET;
	
	private enum EState
	{
		GET,
		SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public BallBreakingPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		addCriterion(new BallPossessionCrit(EBallPossession.THEY, EBallPossession.BOTH));
		
		final IVector2 initPosGetter = new Vector2(aiFrame.worldFrame.ball.getPos());
		
		ballGetter = new BallGetterRole(aiFrame.worldFrame.ball.getPos(), EBallContact.DRIBBLE);
		addAggressiveRole(ballGetter, initPosGetter);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		ballGetter.setViewPoint(GeoMath.stepAlongLine(frame.worldFrame.ball.getPos(), ballGetter.getPos(), -AIConfig
				.getGeometry().getBotRadius()));
		
		if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
		{
			changeToFinished();
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		switch (state)
		{
			case GET:
				if (ballGetter.isCompleted())
				{
					if (currentFrame.tacticalInfo.getEnemyClosestToBall().getDist() < MIN_DISTANCE_FOR_TURN)
					{
						switchToShoot(currentFrame);
					} else
					{
						changeToFinished();
					}
				}
				break;
			case SHOOT:
				if (shooter.isCompleted())
				{
					changeToFinished();
				}
				break;
		}
	}
	
	
	private void switchToShoot(AIInfoFrame currentFrame)
	{
		IVector2 shootTarget;
		shootTarget = new Vector2(ballGetter.getPos().x() - (2 * SHOOT_AWAY_DISTANCE), 0);
		shooter = new PassSenderRole(shootTarget, true);
		switchRoles(ballGetter, shooter, currentFrame);
		state = EState.SHOOT;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}
