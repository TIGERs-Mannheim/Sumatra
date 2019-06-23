/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.05.2011
 * Author(s): GuntherB
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;


/**
 * This play shall be selected if we are in ball possession but there is no
 * chance of scoring a goal directly or indirectly. The ballCarrier will try to
 * improve its position in order to be in position for a direct shot. Two other
 * bots will run free to be a pass receiver and a potential shooter at the same
 * time.
 * It can either be implemented as GameOffensePrepareWithThreePlay or FreekickOffensePreparePlay
 * 
 * @author FlorianS, GuntherB
 * 
 */
public abstract class AOffensePrepareWithThreePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 4741969159032682774L;
	
	private Goal					goal					= AIConfig.getGeometry().getGoalTheir();
	private Vector2f				goalPostLeft		= goal.getGoalPostLeft();
	private Vector2f				goalPostRight		= goal.getGoalPostRight();
	
	private final Vector2		distanceToPost		= new Vector2(0, 50);
	
	/** ballCarrier WILL be implemented in the specific class, and always be gotten by getBallCarrier() in this class */
	// protected ABaseRole ballCarrier;
	private PositioningRole		freeRunnerRight;
	private PositioningRole		freeRunnerLeft;
	
	/**
	 * percentage a new point has to be better than an old one
	 * the bigger this value is the more rarely a bot will change its position
	 */
	private final float			IMPROVEMENT_VALUE	= 75f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param wf
	 */
	public AOffensePrepareWithThreePlay(EPlay playName, AIInfoFrame aiFrame)
	{
		super(playName, aiFrame);
		
		setCriteria();
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		
		Vector2 initPosBallCarrier = new Vector2(ballPos);
		Vector2 initPosFreeRunnerLeft = new Vector2(fieldLength / 4, fieldWidth / 4);
		Vector2 initPosFreeRunnerRight = new Vector2(fieldLength / 4, -fieldWidth / 4);
		
		addAggressiveRole(getBallCarrierRole(), initPosBallCarrier);
		
		freeRunnerLeft = new PositioningRole(false);
		freeRunnerRight = new PositioningRole(false);
		addAggressiveRole(freeRunnerLeft, initPosFreeRunnerLeft);
		addAggressiveRole(freeRunnerRight, initPosFreeRunnerRight);
	}
	

	protected abstract void setCriteria();
	

	protected abstract ABaseRole getBallCarrierRole();
	

	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void beforeFirstUpdate(AIInfoFrame currentFrame)
	{
		// Any initial changes?
	}
	

	@Override
	public void beforeUpdate(AIInfoFrame currentFrame)
	{
		IVector2 destinationBallCarrier = getBallCarrierDestination(currentFrame);
		Vector2 destinationLeft;
		Vector2 destinationRight;
		
		Vector2 destinationLeftOld = new Vector2(freeRunnerLeft.getDestination());
		Vector2 destinationRightOld = new Vector2(freeRunnerRight.getDestination());
		
		Vector2 destinationLeftNew = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
		Vector2 destinationRightNew = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
		
		// compare new values with old ones
		

		if (OffensePointsReceiver.evaluatePoint(destinationLeftNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsReceiver
				.evaluatePoint(destinationLeftOld, currentFrame.worldFrame)))
		{
			destinationLeft = destinationLeftNew;
		} else
		{
			destinationLeft = destinationLeftOld;
		}
		
		if (OffensePointsReceiver.evaluatePoint(destinationRightNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsReceiver
				.evaluatePoint(destinationRightOld, currentFrame.worldFrame)))
		{
			destinationRight = destinationRightNew;
		} else
		{
			destinationRight = destinationRightOld;
		}
		
		// points which shall be shot on
		Vector2 goalPointBallCarrier = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 goalPointLeft = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 goalPointRight = new Vector2(AIConfig.INIT_VECTOR);
		
		// check whether the ballCarrier's destination is on the left or the right side
		if (destinationBallCarrier.y() > 0)
		{
			goalPointBallCarrier = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPointBallCarrier = goalPostRight.addNew(distanceToPost);
		}
		
		// check whether the first destination is on the left or the right side
		if (destinationLeft.y > 0)
		{
			goalPointLeft = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPointLeft = goalPostRight.addNew(distanceToPost);
		}
		
		// check whether the second destination is on the left or the right side
		if (destinationRight.y > 0)
		{
			goalPointRight = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPointRight = goalPostRight.addNew(distanceToPost);
		}
		
		freeRunnerLeft.setTarget(goalPointLeft);
		freeRunnerRight.setTarget(goalPointRight);
		
		freeRunnerLeft.setDestination(destinationLeft);
		freeRunnerRight.setDestination(destinationRight);
		
		setNewPositionDataToBallCarrier(goalPointBallCarrier, goalPointRight);
		
		beforeUpdateBallCarrier(currentFrame);
		
	}
	

	protected abstract IVector2 getBallCarrierDestination(AIInfoFrame currentFrame);
	

	protected abstract void setNewPositionDataToBallCarrier(IVector2 goalPointBallCarrier,
			IVector2 destinationBallCarrier);
	

	protected abstract void beforeUpdateBallCarrier(AIInfoFrame currentFrame);
	

	protected abstract void afterUpdateBallCarrier(AIInfoFrame currentFrame);
	

	@Override
	public void afterUpdate(AIInfoFrame currentFrame)
	{
		// force stuff by the play (probably not needed here)
		
		// favorite option (do it, if you can)
		// check, whether direct shot is available => playstate.succeeded +return
		if (currentFrame.tacticalInfo.getTigersApproximateScoringChance() == true)
		{
			changeToSucceeded();
			return;
		}
		
		// less favorite option (timer?)
		// check, whether pass2DirectShot is available => playstate.succeeded +return
		if (TigersPassReceiverCrit.checkPassReceiver(currentFrame.worldFrame, -1) == true)
		{
			changeToSucceeded();
			return;
		}
		
		// least favorite option (timer?)
		// check, whether safetyPass is available => playstate.succeeded +return
		
		// fail condition: turnover
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY
				|| currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.NO_ONE)
		{
			changeToFailed();
			return;
		}
		
		afterUpdateBallCarrier(currentFrame);
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
