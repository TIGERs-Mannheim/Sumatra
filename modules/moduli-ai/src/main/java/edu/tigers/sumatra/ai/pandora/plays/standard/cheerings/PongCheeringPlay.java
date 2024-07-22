/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;

import java.util.ArrayList;
import java.util.List;


public class PongCheeringPlay implements ICheeringPlay
{
	private static final double MAX_RECTANGLE_WIDTH = 2000;
	private static final double MAX_RECTANGLE_LENGTH = 3000;
	int c = 0;

	private IVector2 ballPos;
	private ILineSegment ballMoveLine = getMoveLineForStage(BallStage.INIT);
	private BallStage stage = BallStage.INIT;
	private CheeringPlay play;


	@Override
	public void initialize(CheeringPlay play)
	{
		this.play = play;
		ballPos = ballMoveLine.getPathStart();
	}


	private IVector2 getRightPadCenter()
	{
		if (ballMoveLine.getPathEnd().distanceTo(Geometry.getGoalTheir().getCenter()) < ballMoveLine.getPathStart()
				.distanceTo(Geometry.getGoalTheir().getCenter()))
		{
			return Vector2.fromXY(ballMoveLine.getPathEnd().x() + Geometry.getBotRadius() * 2.5,
					ballMoveLine.getPathEnd().y());
		} else
		{
			return Vector2.fromXY(ballMoveLine.getPathStart().x() + Geometry.getBotRadius() * 2.5,
					ballMoveLine.getPathStart().y());
		}
	}


	private IVector2 getLeftPadCenter()
	{

		if (ballMoveLine.getPathEnd().distanceTo(Geometry.getGoalOur().getCenter()) < ballMoveLine.getPathStart()
				.distanceTo(Geometry.getGoalOur().getCenter()))
		{
			return Vector2.fromXY(ballMoveLine.getPathEnd().x() - Geometry.getBotRadius() * 2.5,
					ballMoveLine.getPathEnd().y());
		} else
		{
			return Vector2.fromXY(ballMoveLine.getPathStart().x() - Geometry.getBotRadius() * 2.5,
					ballMoveLine.getPathStart().y());
		}
	}


	@Override
	public boolean isDone()
	{
		return c > 4;
	}


	@Override
	public List<IVector2> calcPositions()
	{
		var roles = play.getPermutedRoles();
		var positions = new ArrayList<IVector2>();

		// Ball
		if (ballPos.distanceTo(ballMoveLine.getPathEnd()) > 100)
		{
			ballPos = ballPos.addNew(ballMoveLine.directionVector().scaleToNew(75));
		}
		positions.add(ballPos);

		// Pads
		IVector2 centerOfRightPad = getRightPadCenter();
		IVector2 centerOfLeftPad = getLeftPadCenter();
		int pos = 0;
		for (int botId = 3; botId < roles.size() + 2; botId++)
		{
			IVector2 relativePosition;
			if (botId % 4 >= 2)
				relativePosition = Vector2.fromY(-3 * Geometry.getBotRadius() * pos);
			else
				relativePosition = Vector2.fromY(3 * Geometry.getBotRadius() * pos);

			if (botId % 2 == 0)
			{
				positions.add(centerOfRightPad.addNew(relativePosition));
			} else
			{
				positions.add(centerOfLeftPad.addNew(relativePosition));
			}
			if (botId % 4 == 0)
				pos += 1;
		}
		return positions;
	}


	@Override
	public void doUpdate()
	{
		List<IVector2> positions = calcPositions();

		if (play.getPermutedRoles().get(0).getPos().distanceTo(ballMoveLine.getPathEnd()) < 250)
		{
			stage = stage.getNext();
			ballMoveLine = getMoveLineForStage(stage);
		}

		if (stage == BallStage.BOTTOM_RIGHT_TO_BOTTOM_LEFT)
		{
			c++;
		}

		var roles = play.getPermutedRoles();
		for (int botId = 0; botId < roles.size(); botId++)
		{
			roles.get(botId).getMoveCon().setOurBotsObstacle(false);
			roles.get(botId).updateDestination(positions.get(botId));
		}
	}


	private ILineSegment getMoveLineForStage(final BallStage stage)
	{
		var rectangle = Rectangle.fromCenter(Vector2.zero(), MAX_RECTANGLE_LENGTH, MAX_RECTANGLE_WIDTH);
		var field = Geometry.getField().withMargin(-500);
		var bottomLeft = field.nearestPointInside(rectangle.getCorner(IRectangle.ECorner.BOTTOM_LEFT));
		var topLeft = field.nearestPointInside(rectangle.getCorner(IRectangle.ECorner.TOP_LEFT));
		var topRight = field.nearestPointInside(rectangle.getCorner(IRectangle.ECorner.TOP_RIGHT));
		var bottomRight = field.nearestPointInside(rectangle.getCorner(IRectangle.ECorner.BOTTOM_RIGHT));
		return switch (stage)
		{
			case BOTTOM_LEFT_TO_TOP_RIGHT -> Lines.segmentFromPoints(bottomLeft, topRight);
			case TOP_RIGHT_TO_TOP_LEFT -> Lines.segmentFromPoints(topRight, topLeft);
			case TOP_LEFT_TO_BOTTOM_RIGHT -> Lines.segmentFromPoints(topLeft, bottomRight);
			case BOTTOM_RIGHT_TO_BOTTOM_LEFT -> Lines.segmentFromPoints(bottomRight, bottomLeft);
			case INIT -> getMoveLineForStage(BallStage.BOTTOM_RIGHT_TO_BOTTOM_LEFT);
		};

	}


	@Override
	public ECheeringPlays getType()
	{
		return ECheeringPlays.PONG;
	}


	// TL -> TopLeft
	// BR -> BottomRight
	enum BallStage
	{
		INIT,
		BOTTOM_LEFT_TO_TOP_RIGHT,
		TOP_RIGHT_TO_TOP_LEFT,
		TOP_LEFT_TO_BOTTOM_RIGHT,
		BOTTOM_RIGHT_TO_BOTTOM_LEFT;


		static
		{
			INIT.next = BOTTOM_LEFT_TO_TOP_RIGHT;
			BOTTOM_LEFT_TO_TOP_RIGHT.next = TOP_RIGHT_TO_TOP_LEFT;
			TOP_RIGHT_TO_TOP_LEFT.next = TOP_LEFT_TO_BOTTOM_RIGHT;
			TOP_LEFT_TO_BOTTOM_RIGHT.next = BOTTOM_RIGHT_TO_BOTTOM_LEFT;
			BOTTOM_RIGHT_TO_BOTTOM_LEFT.next = BOTTOM_LEFT_TO_TOP_RIGHT;
		}

		private BallStage next;


		public BallStage getNext()
		{
			return next;
		}
	}
}
