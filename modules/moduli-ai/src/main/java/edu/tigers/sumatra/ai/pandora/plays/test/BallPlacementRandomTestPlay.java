/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.test;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.standard.ABallPlacementPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


/**
 * Test Play for testing ball placement
 */
public class BallPlacementRandomTestPlay extends ABallPlacementPlay
{
	private final boolean onlyOurHalf;
	private Queue<IVector2> nextThreePositions = new LinkedList<>();
	private IVector2 currentBallPlacementPosition = Vector2.zero();
	private Random random = new Random();


	public BallPlacementRandomTestPlay(boolean onlyOurHalf)
	{
		super(EPlay.BALL_PLACEMENT_RANDOM_TEST);
		this.onlyOurHalf = onlyOurHalf;
		nextThreePositions.add(randomPosition());
		nextThreePositions.add(randomPosition());
		nextThreePositions.add(randomPosition());
	}


	private IVector2 randomPosition()
	{
		if (onlyOurHalf)
		{
			return Vector2.fromXY(
					random.nextDouble(Geometry.getFieldLength() / 2) - (Geometry.getFieldLength() / 2),
					random.nextDouble(Geometry.getFieldWidth()) - (Geometry.getFieldWidth() / 2)
			);
		}
		return Vector2.fromXY(
				random.nextDouble(Geometry.getFieldLength()) - (Geometry.getFieldLength() / 2),
				random.nextDouble(Geometry.getFieldWidth()) - (Geometry.getFieldWidth() / 2)
		);
	}


	@Override
	protected void doUpdateBeforeRoles()
	{
		super.doUpdateBeforeRoles();

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_BALL_PLACEMENT);
		if (ballPlacementDone())
		{
			nextThreePositions.add(randomPosition());
			currentBallPlacementPosition = nextThreePositions.poll();
		}

		for (var pos : nextThreePositions)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(pos, 50)));
		}

		shapes.add(new DrawableCircle(Circle.createCircle(currentBallPlacementPosition, 70)));

		assignBallPlacementRoles();
	}


	@Override
	protected boolean useAssistant()
	{
		return getRoles().size() > 1
				&& getBall().getTrajectory().distanceTo(getBallTargetPos()) > 1000;
	}


	@Override
	protected IVector2 getBallTargetPos()
	{
		return currentBallPlacementPosition;
	}
}
