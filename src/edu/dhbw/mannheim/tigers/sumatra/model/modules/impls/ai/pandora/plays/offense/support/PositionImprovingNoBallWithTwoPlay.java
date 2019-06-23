/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.05.2011
 * Author(s):
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TigersScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;


/**
 * This play could be selected if we are in ball possession and the number of
 * bots in our defense play and our offense play do not sum up five. Two free
 * runners will run free to be a pass receiver and a potential shooter at the
 * same time.
 * 
 * @author FlorianS
 * 
 */
public class PositionImprovingNoBallWithTwoPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID			= 4607996583488346409L;
	
	private Goal							goal							= AIConfig.getGeometry().getGoalTheir();
	private Vector2f						goalPostLeft				= goal.getGoalPostLeft();
	private Vector2f						goalPostRight				= goal.getGoalPostRight();
	
	private final Vector2				distanceToPost				= new Vector2(0, 50);
	
	private PositioningRole				freeRunnerLeft;
	private PositioningRole				freeRunnerRight;
	
	/**
	 * percentage a new point has to be better than an old one
	 * the bigger this value is the more rarely a bot will change its position
	 */
	private final float					IMPROVEMENT_VALUE			= AIConfig.getPlays().getPositionImprovingNoBallWithTwo()
																						.getImprovementValue();
	
	private BallPossessionCrit			ballPossessionCrit		= null;
	private TigersScoringChanceCrit	tigersScoringChanceCrit	= null;
	private TigersPassReceiverCrit	tigersPassReceiverCrit	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param wf
	 */
	public PositionImprovingNoBallWithTwoPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.POSITION_IMPROVING_NO_BALL_WITH_TWO, aiFrame);
		
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersScoringChanceCrit = new TigersScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
		
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		
		Vector2 initPosFreeRunnerLeft = new Vector2(fieldLength / 4, fieldWidth / 4);
		Vector2 initPosFreeRunnerRight = new Vector2(fieldLength / 4, -fieldWidth / 4);
		
		freeRunnerLeft = new PositioningRole(false);
		freeRunnerRight = new PositioningRole(false);
		addAggressiveRole(freeRunnerLeft, initPosFreeRunnerLeft);
		addAggressiveRole(freeRunnerRight, initPosFreeRunnerRight);
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
		Vector2 destinationLeft = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 destinationRight = new Vector2(AIConfig.INIT_VECTOR);
		
		Vector2 destinationLeftOld = new Vector2(freeRunnerLeft.getDestination());
		Vector2 destinationRightOld = new Vector2(freeRunnerRight.getDestination());
		
		Vector2 destinationLeftNew = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
		Vector2 destinationRightNew = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
		
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
		Vector2 goalPointLeft = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 goalPointRight = new Vector2(AIConfig.INIT_VECTOR);
		
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
	}
	

	@Override
	public void afterUpdate(AIInfoFrame currentFrame)
	{
		// // force stuff by the play (probably not needed here)
		//
		// // favorite option (do it, if you can)
		// // check, whether direct shot is available => playstate.succeeded +return
		// if (currentFrame.tacticalFieldInfo.getTigersApproximateScoringChance() == true)
		// {
		// changeToSucceeded();
		// return;
		// }
		//
		// // less favorite option (timer?)
		// // check, whether pass2DirectShot is available => playstate.succeeded +return
		// if (TigersPassReceiverCrit.checkPassReceiver(currentFrame.worldFrame, -1) == true)
		// {
		// changeToSucceeded();
		// return;
		// }
		//
		// // least favorite option (timer?)
		// // check, whether safetyPass is available => playstate.succeeded +return
		//
		// // fail condition: turnover
		// if (currentFrame.tacticalFieldInfo.getBallPossesion() == EBallPossession.THEY
		// || currentFrame.tacticalFieldInfo.getBallPossesion() == EBallPossession.NO_ONE)
		// {
		// changeToFailed();
		// return;
		// }
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
