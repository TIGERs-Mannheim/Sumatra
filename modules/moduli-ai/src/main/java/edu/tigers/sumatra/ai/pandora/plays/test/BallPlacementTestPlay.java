/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.ABallPlacementPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Arrays;
import java.util.List;


/**
 * Test Play for testing ball placement
 */
public class BallPlacementTestPlay extends ABallPlacementPlay
{
	private final List<IVector2> placementPositions;
	private int placementPositionIdx;


	public BallPlacementTestPlay(IVector2[] placementPositions)
	{
		super(EPlay.BALL_PLACEMENT_TEST);
		this.placementPositions = Arrays.asList(placementPositions);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();
		if (ballPlacementDone())
		{
			placementPositionIdx = (placementPositionIdx + 1) % placementPositions.size();
		}

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.AI_BALL_PLACEMENT);
		for (var pos : placementPositions)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(pos, 50)));
		}

		assignBallPlacementRoles();
	}


	@Override
	protected boolean useAssistant()
	{
		return getRoles().size() > 1
				&& getBall().getTrajectory().getTravelLineSegment().distanceTo(getBallTargetPos()) > 1000;
	}


	@Override
	protected IVector2 getBallTargetPos()
	{
		return placementPositions.get(placementPositionIdx);
	}
}
