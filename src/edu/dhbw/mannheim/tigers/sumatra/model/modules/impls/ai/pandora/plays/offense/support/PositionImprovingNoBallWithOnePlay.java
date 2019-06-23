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
 * bots in our defense play and our offense play does not sum up five. Only one
 * free runner will run free to be a pass receiver and a potential shooter at the
 * same time.
 * 
 * @author FlorianS
 * 
 */
public class PositionImprovingNoBallWithOnePlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long			serialVersionUID			= 1053979142091713844L;
	
	private Goal							goal							= AIConfig.getGeometry().getGoalTheir();
	private Vector2f						goalPostLeft				= goal.getGoalPostLeft();
	private Vector2f						goalPostRight				= goal.getGoalPostRight();
	
	private final Vector2				distanceToPost				= new Vector2(0, 50);
	
	private PositioningRole				freeRunner;
	
	/**
	 * percentage a new point has to be better than an old one
	 * the bigger this value is the more rarely a bot will change its position
	 */
	private final float					IMPROVEMENT_VALUE			= AIConfig.getPlays().getPositionImprovingNoBallWithOne()
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
	public PositionImprovingNoBallWithOnePlay(AIInfoFrame aiFrame)
	{
		super(EPlay.POSITION_IMPROVING_NO_BALL_WITH_ONE, aiFrame);
		// System.out.println(IMPROVEMENT_VALUE);
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersScoringChanceCrit = new TigersScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
		
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		
		Vector2 initPosFreeRunner = new Vector2(fieldLength / 4, fieldWidth / 4);
		
		freeRunner = new PositioningRole(false);
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
		// destinations from Metis
		Vector2 destinationLeftNew = currentFrame.tacticalInfo.getOffLeftReceiverPoints().get(0);
		Vector2 destinationRightNew = currentFrame.tacticalInfo.getOffRightReceiverPoints().get(0);
		
		// new and old destination
		Vector2 destinationNew = new Vector2(AIConfig.INIT_VECTOR);
		Vector2 destinationOld = new Vector2(freeRunner.getDestination());
		
		// points which shall be shot on
		Vector2 goalPoint = new Vector2(AIConfig.INIT_VECTOR);
		
		// check whether bot is on the left or on the right side
		if (freeRunner.getPos(currentFrame).x() > 0)
		{
			// check whether the new point is better
			if (OffensePointsReceiver.evaluatePoint(destinationLeftNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsReceiver
					.evaluatePoint(destinationOld, currentFrame.worldFrame)))
			{
				destinationNew = destinationLeftNew;
			} else
			{
				destinationNew = destinationOld;
			}
		} else
		{
			// check whether the new point is better
			if (OffensePointsReceiver.evaluatePoint(destinationRightNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsReceiver
					.evaluatePoint(destinationOld, currentFrame.worldFrame)))
			{
				destinationNew = destinationRightNew;
			} else
			{
				destinationNew = destinationOld;
			}
		}
		
		// check whether the free runner's destination is on the left or the right side
		if (destinationNew.y > 0)
		{
			goalPoint = goalPostLeft.subtractNew(distanceToPost);
		} else
		{
			goalPoint = goalPostRight.addNew(distanceToPost);
		}
		
		freeRunner.setTarget(goalPoint);
		freeRunner.setDestination(destinationNew);
	}
	
	
	@Override
	public void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
