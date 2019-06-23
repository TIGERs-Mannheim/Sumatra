/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.05.2011
 * Author(s): Vendetta
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.AOffensePrepareWithThreePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.BallGetterRole;


/**
 * Implementation of {@link AOffensePrepareWithThreePlay} for freekick-purposes.
 * 
 * @author GuntherB
 * 
 */
public class FreekickOffensePrepareWithThreeBots extends AOffensePrepareWithThreePlay
{
	/**  */
	private static final long	serialVersionUID	= 3345191829935119085L;
	private BallGetterRole		ballGetter;
	
	
	/**
	 * @param wf
	 * @param playName
	 */
	public FreekickOffensePrepareWithThreeBots(AIInfoFrame aiFrame)
	{
		super(EPlay.FREEKICK_OFFENSE_PREPARE_WITH_THREE, aiFrame);
	}
	

	@Override
	protected void setCriteria()
	{
		// no criteria, it's a referee-triggered play
	}
	

	@Override
	protected ABaseRole getBallCarrierRole()
	{
		if (ballGetter == null)
		{
			ballGetter = new BallGetterRole(EGameSituation.SET_PIECE);
		}
		return ballGetter;
	}
	

	@Override
	protected IVector2 getBallCarrierDestination(AIInfoFrame currentFrame)
	{
		return currentFrame.worldFrame.ball.pos;
	}
	

	@Override
	protected void setNewPositionDataToBallCarrier(IVector2 goalPointBallCarrier, IVector2 destinationBallCarrier)
	{
		ballGetter.setViewPoint(goalPointBallCarrier);
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
	

	@Override
	public boolean isBallCarrying()
	{
		return true;
	}
}
