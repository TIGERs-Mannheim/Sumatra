/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.util.LinkedList;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * @author "Lukas Magel"
 */
public class PossibleGoalCalc implements IRefereeCalc
{
	@Configurable(comment = "[degree] The angle by which the ball heading needs to change while inside the goal to count as goal")
	private static double goalBallAngleChange = 45;
	@Configurable(comment = "[mm] negative margin for penArea: ball will be tracked in this smaller region of penArea and behind", defValue = "-500")
	private static double marginToDecreasePenArea = -500;
	@Configurable(comment = "[mm] margin added to field -> moves goal line in x to make sure ball is definitely inside goal", defValue = "21.5")
	private static double goalMarginInX = Geometry.getBallRadius();
	
	static
	{
		ConfigRegistration.registerClass("autoreferee", PossibleGoalCalc.class);
	}
	
	private PossibleGoal detectedGoal = null;
	private boolean ballWasInGoal = false;
	private LinkedList<TimedPosition> buffer = new LinkedList<>();
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		ITrackedBall ball = frame.getWorldFrame().getBall();
		IVector3 ballPos = ball.getPos3();
		Optional<IPenaltyArea> penArea = NGeometry.getPenaltyAreas().stream()
				.filter(pa -> pa.withMargin(marginToDecreasePenArea).isPointInShapeOrBehind(ballPos.getXYVector()))
				.findAny();
		
		if (!penArea.isPresent() || ballPos.z() > Geometry.getGoalHeight())
		{
			reset();
		} else if (detectedGoal == null)
		{
			if (NGeometry.ballInsideGoal(ball.getPos3(), goalMarginInX, 0))
			{
				handleBallInsideGoal(frame, ballPos);
			} else if (ballWasInGoal && NGeometry.posInsidePenaltyArea(ballPos.getXYVector(), -100) && buffer.size() > 1)
			{
				ILineSegment ballEntersGoal = Lines.segmentFromPoints(buffer.pollFirst().getPos(),
						buffer.peekFirst().getPos());
				ILineSegment ballLeavesGoal = Lines.segmentFromPoints(buffer.pop().getPos(), ballPos.getXYVector());
				double angle = ballEntersGoal.directionVector().angleToAbs(ballLeavesGoal.directionVector()).orElse(0.0);
				if (angle > AngleMath.deg2rad(goalBallAngleChange))
				{
					detectedGoal = new PossibleGoal(frame.getTimestamp(), getGoalColor(ballPos.getXYVector()));
				}
			}
		}
		frame.setPossibleGoal(detectedGoal);
	}
	
	
	private void handleBallInsideGoal(final AutoRefFrame frame, final IVector3 ballPos)
	{
		if (!ballWasInGoal)
		{
			AutoRefFrame prevFrame = frame.getPreviousFrame();
			IVector3 ballPosOutsideGoal = prevFrame.getWorldFrame().getBall().getPos3();
			if (ballPosOutsideGoal.z() >= Geometry.getGoalHeight())
			{
				buffer = new LinkedList<>();
				detectedGoal = null;
				frame.setBallLeftFieldPos(new TimedPosition(frame.getTimestamp(), ballPosOutsideGoal));
			} else
			{
				buffer.add(new TimedPosition(prevFrame.getTimestamp(), ballPosOutsideGoal));
			}
		}
		ballWasInGoal = true;
		buffer.add(new TimedPosition(frame.getTimestamp(), ballPos));
		if (buffer.size() > 3)
		{
			detectedGoal = new PossibleGoal(frame.getTimestamp(), getGoalColor(ballPos.getXYVector()));
		}
	}
	
	
	private void reset()
	{
		detectedGoal = null;
		ballWasInGoal = false;
		buffer = new LinkedList<>();
	}
	
	
	private ETeamColor getGoalColor(final IVector2 ballPos)
	{
		return NGeometry.getTeamOfClosestGoalLine(ballPos);
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public static final class PossibleGoal
	{
		private final long timestamp;
		private final ETeamColor goalColor;
		
		
		/**
		 * @param timestamp
		 * @param goalColor
		 */
		public PossibleGoal(final long timestamp, final ETeamColor goalColor)
		{
			this.timestamp = timestamp;
			this.goalColor = goalColor;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @return the goalColor
		 */
		public ETeamColor getGoalColor()
		{
			return goalColor;
		}
	}
	
}
