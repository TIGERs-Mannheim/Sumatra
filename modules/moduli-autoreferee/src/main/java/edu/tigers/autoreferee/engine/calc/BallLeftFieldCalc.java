/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.calc;

import java.util.LinkedList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * Save the moment, the ball left the field
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallLeftFieldCalc implements IRefereeCalc
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int BUFFER_SIZE = 15;
	private static final int CERTAINTY = 3;
	@Configurable(comment = "[mm] Area behind the goal that is still considered to be part of the goal for better goal detection")
	private static double goalMargin = 50;
	
	private final LinkedList<TimedPosition> buffer = new LinkedList<>();
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		updateBuffer(frame.getWorldFrame());
		
		boolean wasInsideField = frame.getPreviousFrame().isBallInsideField();
		
		TimedPosition leftFieldPos = frame.getPreviousFrame().getBallLeftFieldPos();
		boolean isBallInsideField = isBallInsideField(wasInsideField);
		if (!isBallInsideField && wasInsideField)
		{
			leftFieldPos = getBallLeftFieldPosition();
		}
		frame.setBallInsideField(isBallInsideField);
		frame.setBallLeftFieldPos(leftFieldPos);
		if (leftFieldPos != null)
		{
			frame.getShapes().get(EAutoRefShapesLayer.BALL_LEFT_FIELD)
					.add(new DrawableCircle(Circle.createCircle(leftFieldPos.getPos(), 100)));
		}
	}
	
	
	private void updateBuffer(final SimpleWorldFrame frame)
	{
		TimedPosition pos = new TimedPosition(frame.getTimestamp(), frame.getBall().getPos3());
		
		if (buffer.size() >= BUFFER_SIZE)
		{
			buffer.pollLast();
		}
		buffer.offerFirst(pos);
	}
	
	
	private TimedPosition getBallLeftFieldPosition()
	{
		IRectangle field = Geometry.getField();
		
		TimedPosition outsidePos = null;
		TimedPosition insidePos = null;
		for (TimedPosition curPos : buffer)
		{
			if (!field.isPointInShape(curPos.getPos()))
			{
				outsidePos = curPos;
			} else
			{
				insidePos = curPos;
				break;
			}
		}
		
		if ((insidePos != null) && (outsidePos != null))
		{
			ILine line = Line.fromPoints(insidePos.getPos(), outsidePos.getPos());
			List<IVector2> intersections = field.lineIntersections(line);
			if (!intersections.isEmpty())
			{
				return new TimedPosition(outsidePos.getTimestamp(), outsidePos.getPos().nearestTo(intersections));
			}
		}
		return new TimedPosition();
	}
	
	
	private boolean isBallInsideField(final boolean currentlyInside)
	{
		if (buffer.size() < CERTAINTY)
		{
			return currentlyInside;
		}
		
		boolean ballStateChanged = true;
		for (int i = 0; i < CERTAINTY; i++)
		{
			TimedPosition pos = buffer.get(i);
			if (isBallInsideFieldPerimeter(pos.getPos3D(), currentlyInside) == currentlyInside)
			{
				ballStateChanged = false;
				break;
			}
		}
		return ballStateChanged ^ currentlyInside;
	}
	
	
	private boolean isBallInsideFieldPerimeter(final IVector3 ballPos, final boolean currentlyInside)
	{
		return Geometry.getField().isPointInShape(ballPos.getXYVector()) || ballInsideGoal(ballPos, currentlyInside);
	}
	
	
	/**
	 * Check if the ball is located inside the goal.
	 * We use a hysteresis like approach to avoid false positive detections that can occur if the physical goal is not
	 * positioned correctly and the ball enters the virtual goal after having left the field.
	 */
	private boolean ballInsideGoal(final IVector3 ballPos, final boolean currentlyInside)
	{
		return currentlyInside && NGeometry.ballInsideGoal(ballPos, 0, goalMargin);
	}
}
