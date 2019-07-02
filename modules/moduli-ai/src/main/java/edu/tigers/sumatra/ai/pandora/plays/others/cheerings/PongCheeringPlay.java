/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others.cheerings;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.pandora.plays.others.CheeringPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


public class PongCheeringPlay implements ICheeringPlay
{
	int c = 0;
	private IVector2 topLeft = Geometry.getField().getCorners().get(0)
			.addNew(Vector2.fromAngle(Math.PI / 4).scaleTo(750));
	private IVector2 topRight = Geometry.getField().getCorners().get(1)
			.addNew(Vector2.fromAngle(-Math.PI / 4).scaleTo(750));
	private IVector2 bottomRight = Geometry.getField().getCorners().get(2)
			.subtractNew(Vector2.fromAngle(Math.PI / 4).scaleTo(750));
	private IVector2 bottomLeft = Geometry.getField().getCorners().get(3)
			.subtractNew(Vector2.fromAngle(-Math.PI / 4).scaleTo(750));
	private IVector2 ballPos;
	private ILineSegment ballMoveLine = getMoveLineForStage(BallStage.INIT);
	private BallStage stage = BallStage.INIT;
	private CheeringPlay play;
	
	@Override
	public void initialize(CheeringPlay play)
	{
		this.play = play;
		ballPos = ballMoveLine.getStart();
	}
	
	
	private IVector2 getRightPadCenter()
	{
		if (ballMoveLine.getEnd().distanceTo(Geometry.getGoalTheir().getCenter()) < ballMoveLine.getStart()
				.distanceTo(Geometry.getGoalTheir().getCenter()))
		{
			return Vector2.fromXY(ballMoveLine.getEnd().x() + Geometry.getBotRadius() * 2.5, ballMoveLine.getEnd().y());
		} else
		{
			return Vector2.fromXY(ballMoveLine.getStart().x() + Geometry.getBotRadius() * 2.5,
					ballMoveLine.getStart().y());
		}
	}
	
	
	private IVector2 getLeftPadCenter()
	{
		
		if (ballMoveLine.getEnd().distanceTo(Geometry.getGoalOur().getCenter()) < ballMoveLine.getStart()
				.distanceTo(Geometry.getGoalOur().getCenter()))
		{
			return Vector2.fromXY(ballMoveLine.getEnd().x() - Geometry.getBotRadius() * 2.5, ballMoveLine.getEnd().y());
		} else
		{
			return Vector2.fromXY(ballMoveLine.getStart().x() - Geometry.getBotRadius() * 2.5,
					ballMoveLine.getStart().y());
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
		List<ARole> roles = play.getRoles();
		List<IVector2> positions = new ArrayList<>();
		
		// Ball
		if (ballPos.distanceTo(ballMoveLine.getEnd()) > 100)
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
		
		if (play.getRoles().get(0).getPos().distanceTo(ballMoveLine.getEnd()) < 250)
		{
			stage = stage.getNext();
			ballMoveLine = getMoveLineForStage(stage);
		}
		
		if (stage == BallStage.BOTTOM_LEFT_TO_TOP_LEFT)
		{
			c++;
		}
		
		List<ARole> roles = play.getRoles();
		for (int botId = 0; botId < roles.size(); botId++)
		{
			((MoveRole) roles.get(botId)).getMoveCon().setBotsObstacle(false);
			((MoveRole) roles.get(botId)).getMoveCon().updateDestination(positions.get(botId));
		}
	}
	
	
	private ILineSegment getMoveLineForStage(final BallStage stage)
	{
		switch (stage)
		{
			case TOP_LEFT_TO_BOTTOM_RIGHT:
				return Lines.segmentFromPoints(topLeft, bottomRight);
			
			case BOTTOM_RIGHT_TO_TOP_RIGHT:
				return Lines.segmentFromPoints(bottomRight, topRight);
			
			case TOP_RIGHT_TO_BOTTOM_LEFT:
				return Lines.segmentFromPoints(topRight, bottomLeft);
			
			case BOTTOM_LEFT_TO_TOP_LEFT:
				return Lines.segmentFromPoints(bottomLeft, topLeft);
			
			case INIT:
				return getMoveLineForStage(BallStage.BOTTOM_LEFT_TO_TOP_LEFT);
		}
		
		return null;
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
		TOP_LEFT_TO_BOTTOM_RIGHT,
		BOTTOM_RIGHT_TO_TOP_RIGHT,
		TOP_RIGHT_TO_BOTTOM_LEFT,
		BOTTOM_LEFT_TO_TOP_LEFT;
		
		static
		{
			INIT.next = TOP_LEFT_TO_BOTTOM_RIGHT;
			TOP_LEFT_TO_BOTTOM_RIGHT.next = BOTTOM_RIGHT_TO_TOP_RIGHT;
			BOTTOM_RIGHT_TO_TOP_RIGHT.next = TOP_RIGHT_TO_BOTTOM_LEFT;
			TOP_RIGHT_TO_BOTTOM_LEFT.next = BOTTOM_LEFT_TO_TOP_LEFT;
			BOTTOM_LEFT_TO_TOP_LEFT.next = TOP_LEFT_TO_BOTTOM_RIGHT;
		}
		
		private BallStage next;
		
		
		public BallStage getNext()
		{
			return next;
		}
	}
}
