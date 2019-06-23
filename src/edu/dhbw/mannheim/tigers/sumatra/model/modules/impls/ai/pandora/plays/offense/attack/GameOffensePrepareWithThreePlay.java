/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.05.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsCarrier;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.TigersApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.local.TigersPassReceiverCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.PositioningRole;


/**
 * Implementation of {@link AOffensePrepareWithThreePlay} for ingame-purposes.
 * 
 */
public class GameOffensePrepareWithThreePlay extends AOffensePrepareWithThreePlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID							= -3521018106922254602L;
	
	private PositioningRole								ballCarrier;
	
	/**
	 * value a new point has to be better than an old one
	 * the bigger this value is the more rarely a bot will change its position
	 */
	private final float									IMPROVEMENT_VALUE							= AIConfig
																														.getPlays()
																														.getGameOffensePrepareWithThree()
																														.getImprovementValue();
	
	private BallPossessionCrit							ballPossessionCrit						= null;
	private TigersApproximateScoringChanceCrit	tigersApproximateScoringChanceCrit	= null;
	private TigersPassReceiverCrit					tigersPassReceiverCrit					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public GameOffensePrepareWithThreePlay(AIInfoFrame aiFrame)
	{
		super(EPlay.GAME_OFFENSE_PREPARE_WITH_THREE, aiFrame);
	}
	

	@Override
	protected void setCriteria()
	{
		ballPossessionCrit = new BallPossessionCrit(EBallPossession.WE);
		tigersApproximateScoringChanceCrit = new TigersApproximateScoringChanceCrit(false);
		tigersPassReceiverCrit = new TigersPassReceiverCrit(false);
		addCriterion(ballPossessionCrit);
		addCriterion(tigersApproximateScoringChanceCrit);
		addCriterion(tigersPassReceiverCrit);
	}
	

	@Override
	protected ABaseRole getBallCarrierRole()
	{
		if (ballCarrier == null)
		{
			ballCarrier = new PositioningRole(true);
		}
		return ballCarrier;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// -------------------------------------------------------------------------
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	

	@Override
	protected IVector2 getBallCarrierDestination(AIInfoFrame currentFrame)
	{
		Vector2 destinationDribblerOld = new Vector2(getBallCarrierRole().getDestination());
		Vector2 destinationDribblerNew = currentFrame.tacticalInfo.getOffCarrierPoints().get(0);
		
		// compare new values with old ones
		if (OffensePointsCarrier.evaluatePoint(destinationDribblerNew, currentFrame.worldFrame) > (IMPROVEMENT_VALUE + OffensePointsCarrier
				.evaluatePoint(destinationDribblerOld, currentFrame.worldFrame)))
		{
			return destinationDribblerNew;
		} else
		{
			return destinationDribblerOld;
		}
	}
	

	@Override
	protected void setNewPositionDataToBallCarrier(IVector2 goalPointBallCarrier, IVector2 destinationBallCarrier)
	{
		ballCarrier.setTarget(goalPointBallCarrier);
		
		ballCarrier.setDestination(destinationBallCarrier);
	}
	

	@Override
	protected void beforeUpdateBallCarrier(AIInfoFrame currentFrame)
	{
		// idle
	}
	

	@Override
	protected void afterUpdateBallCarrier(AIInfoFrame currentFrame)
	{
		// idle
	}
}
