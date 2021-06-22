/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TimedPosition;

import java.util.LinkedList;


public class BallLeftFieldCalculator
{
	@Configurable(comment = "Time [s] to wait before using ball positions, invaliding all positions just before a chip kick", defValue = "0.3")
	private static double maxTimeToDetectChipKick = 0.3;

	@Configurable(comment = "Time [s] between two ball positions to pass before comparing them in order to check if the ball left the field", defValue = "0.05")
	private static double minComparisonTimeSpan = 0.05;

	static
	{
		ConfigRegistration.registerClass("wp", BallLeftFieldCalculator.class);
	}

	private final LinkedList<TimedPosition> ballPosBuffer = new LinkedList<>();
	private TimedPosition lastBallLeftFieldPosition = null;
	private boolean ballInsideField = true;
	private long chipStartTime;


	public BallLeftFieldPosition process(final SimpleWorldFrame wFrame)
	{
		reduceBallPosBuffer(wFrame.getTimestamp());
		addToBallPosBuffer(wFrame);
		removeFirstChippedBallPositions(wFrame);
		updateDetection(wFrame);

		return lastBallLeftFieldPosition == null ? null
				: new BallLeftFieldPosition(lastBallLeftFieldPosition, currentType());
	}


	private BallLeftFieldPosition.EBallLeftFieldType currentType()
	{
		if (Math.abs(lastBallLeftFieldPosition.getPos().x()) < Geometry.getFieldLength() / 2)
		{
			return BallLeftFieldPosition.EBallLeftFieldType.TOUCH_LINE;
		}
		boolean overGoal = lastBallLeftFieldPosition.getPos3().z() > Geometry.getGoalHeight();

		for (Goal goal : Geometry.getGoals())
		{
			if (goal.getLineSegment().isPointOnLine(lastBallLeftFieldPosition.getPos()))
			{
				return overGoal ? BallLeftFieldPosition.EBallLeftFieldType.GOAL_OVER
						: BallLeftFieldPosition.EBallLeftFieldType.GOAL;
			}
		}
		return BallLeftFieldPosition.EBallLeftFieldType.GOAL_LINE;
	}


	private void updateDetection(final SimpleWorldFrame wFrame)
	{
		TimedPosition prePos = ballPosBuffer.peekLast();
		TimedPosition postPos = firstValidBallPos(wFrame.getTimestamp());

		if (lastBallLeftFieldPosition != null
				&& Math.abs(wFrame.getTimestamp() - lastBallLeftFieldPosition.getTimestamp()) / 1e9 > 2)
		{
			lastBallLeftFieldPosition = null;
		}

		if (prePos != null && postPos != null
				&& (postPos.getTimestamp() - prePos.getTimestamp()) / 1e9 >= minComparisonTimeSpan)
		{
			boolean postBallPosInsideField = Geometry.getField()
					.withMargin(Geometry.getLineWidth() + Geometry.getBallRadius())
					.isPointInShape(postPos.getPos());
			boolean stateChanged = postBallPosInsideField != ballInsideField;
			ballInsideField = postBallPosInsideField;
			if (!postBallPosInsideField && stateChanged)
			{
				ILine line = Line.fromPoints(postPos.getPos(), prePos.getPos());
				IVector2 pos = postPos.getPos().nearestToOpt(Geometry.getField().lineIntersections(line))
						.orElse(postPos.getPos());
				double height = (postPos.getPos3().z() + prePos.getPos3().z()) / 2.0;
				lastBallLeftFieldPosition = new TimedPosition(postPos.getTimestamp(), Vector3.from2d(pos, height));
			}
		}
	}


	private TimedPosition firstValidBallPos(final long timestamp)
	{
		for (TimedPosition timedPosition : ballPosBuffer)
		{
			if (timedPosition.getAge(timestamp) > maxTimeToDetectChipKick)
			{
				return timedPosition;
			}
		}
		return null;
	}


	private void removeFirstChippedBallPositions(final SimpleWorldFrame wFrame)
	{
		if (wFrame.getBall().isChipped())
		{
			if (chipStartTime == 0)
			{
				chipStartTime = wFrame.getTimestamp();
			}
			double age = (wFrame.getTimestamp() - chipStartTime) / 1e9;
			if (age < maxTimeToDetectChipKick)
			{
				ballPosBuffer.clear();
			}
		} else
		{
			chipStartTime = 0;
		}
	}


	private void addToBallPosBuffer(final SimpleWorldFrame frame)
	{
		TimedPosition pos = new TimedPosition(frame.getTimestamp(), frame.getBall().getPos3());
		ballPosBuffer.offerFirst(pos);
	}


	private void reduceBallPosBuffer(final long currentTimestamp)
	{
		ballPosBuffer.removeIf(t -> t.getTimestamp() >= currentTimestamp);
		while (!ballPosBuffer.isEmpty())
		{
			double age = (currentTimestamp - ballPosBuffer.get(ballPosBuffer.size() - 1).getTimestamp()) / 1e9;
			if (age > maxTimeToDetectChipKick + minComparisonTimeSpan + 0.2)
			{
				ballPosBuffer.remove(ballPosBuffer.size() - 1);
			} else
			{
				break;
			}
		}
	}


	public void reset()
	{
		lastBallLeftFieldPosition = null;
	}
}
