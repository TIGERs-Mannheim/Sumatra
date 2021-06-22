/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Play that handles automated ball placement
 */
public class BallPlacementPlay extends ABallPlacementPlay
{
	public BallPlacementPlay()
	{
		super(EPlay.BALL_PLACEMENT);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();
		assignBallPlacementRoles();
	}


	@Override
	protected IVector2 getBallTargetPos()
	{
		return getAiFrame().getGameState().getBallPlacementPositionForUs();
	}
}
