/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsCarrier;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TigersApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;


/**
 * This play shall be selected if we are in ball possession but there is no
 * chance of scoring a goal directly or indirectly. The dribbler will try to
 * improve its position in order to be in position for a direct shot. Another
 * bot will run free to be a pass receiver and a potential shooter at the same
 * time.
 * 
 * @author FlorianS
 * 
 */
public class GameOffensePrepareWithTwoPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID							= -88162997197546124L;
	
	private Goal											goal											= AIConfig.getGeometry()
																														.getGoalTheir();
	private Vector2f										goalPostLeft								= goal.getGoalPostLeft();
	private Vector2f										goalPostRight								= goal.getGoalPostRight();
	
	private final Vector2								distanceToPost								= new Vector2(0, 50);
	
	private PositioningRole								dribbler;
	private PositioningRole								freeRunner;
	
	/**
	 * value a new point has to be better than an old one
	 * the bigger this value is the more rarely a bot will change its position
	 */
	private final float									IMPROVEMENT_VALUE							= AIConfig
																														.getPlays()
																														.getGameOffensePrepareWithTwo()
																														.getImprovementValue();
	
	private BallPossessionCrit							ballPossessionCrit						= null;
	private TigersApproximateScoringChanceCrit	tigersApproximateScoringChanceCrit	= null;
	private TigersPassReceiverCrit					tigersPassReceiverCrit					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param wf
	 */
	public GameOffensePrepareWithTwoPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.GAME_OFFENSE_PREPARE_WITH_TWO, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersApproximateScoringChanceCrit = new TigersApproximateScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersApproximateScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
		
		Vector2f ballPos = aiFrame.worldFrame.ball.pos;
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		
		Vector2 initPosDribbler = new Vector2(ballPos);
		Vector2 initPosFreeRunner = new Vector2(fieldLength / 4, fieldWidth / 4);
		
		dribbler = new PositioningRole(true);
		freeRunner = new PositioningRole(false);
		addAggressiveRole(dribbler, initPosDribbler);
		addAggressiveRole(freeRunner, initPosFreeRunner);
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
		Vector2 destinationDribbler = currentFrame.tacticalInfo.getOffCarrierPoints().get(0);
		Vector2 destinationLeft = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
		Vector2 destinationRight = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
		Vector2 destinationFreeRunner = new Vector2(AIConfig.INIT_VECTOR);
		
		Vector2 destinationDribblerOld = new Vector2(dribbler.getDestination());
		Vector2 destinationLeftOld = new Vector2(freeRunner.getDestination());
		Vector2 destinationRightOld = new Vector2(freeRunner.getDestination());
		
		Vector2 destinationDribblerNew = currentFrame.tacticalInfo.getOffCarrierPoints().get(0);
		Vector2 destinationLeftNew = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
		Vector2 destinationRightNew = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
		
		// compare new values with old ones
		if (OffensePointsCarrier.evaluatePoint(destinationDribblerNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsCarrier
				.evaluatePoint(destinationDribblerOld, currentFrame.worldFrame)))
		{
			destinationDribbler = destinationDribblerNew;
		} else
		{
			destinationDribbler = destinationDribblerOld;
		}
		
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
		
		float valueLeft = -1;
		float valueRight = -1;
		
		// determine the values of both possible positions
		valueLeft = OffensePointsReceiver.evaluatePoint(destinationLeft, currentFrame.worldFrame);
		valueRight = OffensePointsReceiver.evaluatePoint(destinationRight, currentFrame.worldFrame);
		
		// check which destination is better
		if (valueLeft > valueRight)
		{
			destinationFreeRunner = destinationLeft;
		} else
		{
			destinationFreeRunner = destinationRight;
		}
		
		// points which shall be shot on
		Vector2 goalPointDribbler = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 goalPointFreeRunner = new Vector2(AIConfig.INIT_VECTOR);
		
		// check whether the dribbler's destination is on the left or the right side
		if (destinationDribbler.y > 0)
		{
			goalPointDribbler = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPointDribbler = goalPostRight.addNew(distanceToPost);
		}
		
		// check whether the free runner's destination is on the left or the right side
		if (destinationLeft.y > 0)
		{
			goalPointFreeRunner = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPointFreeRunner = goalPostRight.addNew(distanceToPost);
		}
		
		dribbler.setTarget(goalPointDribbler);
		freeRunner.setTarget(goalPointFreeRunner);
		
		dribbler.setDestination(destinationDribbler);
		freeRunner.setDestination(destinationFreeRunner);
	}
	

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
		
		// really least favorite option: turnover
		if (currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.THEY
				|| currentFrame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.NO_ONE)
		{
			changeToFailed();
			return;
		}
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
