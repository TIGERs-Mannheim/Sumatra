/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.util.List;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;


/**
 * Ball placement is active, the keeper should move away from the ball
 *
 * @author UlrikeL
 */
public class BallPlacementState extends AKeeperState
{
	private MoveToTrajSkill posSkill;
	
	
	/**
	 * @param parent the parent keeper role
	 */
	public BallPlacementState(KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		posSkill = new MoveToTrajSkill();
		posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		posSkill.getMoveCon().setBallObstacle(true);
		posSkill.getMoveCon().setGoalPostObstacle(true);
		setNewSkill(posSkill);
	}
	
	
	@Override
	public void doExitActions()
	{
		// nothing to do here
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 ballPos = getWFrame().getBall().getTrajectory().getPosByTime(0.1);
		IVector2 destination = calcNewDestinationOnGoalLine(ballPos);
		
		posSkill.getMoveCon().updateDestination(destination);
		posSkill.getMoveCon().updateTargetAngle(calcDefendingOrientation());
	}
	
	
	private IVector2 calcNewDestinationOnGoalLine(IVector2 ballPos)
	{
		ICircle forbiddenArea = Circle.createCircle(ballPos,
				Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius());
		
		GameState gameState = getAiFrame().getGamestate();
		if (gameState.getState() == EGameState.BALL_PLACEMENT)
		{
			return getDestination(ballPos, gameState);
		}
		
		List<IVector2> intersections = forbiddenArea.lineIntersections(Geometry.getGoalOur().getLine());
		if (intersections.size() == 2)
		{
			IVector2 closestIntersection = Geometry.getGoalOur().getCenter().nearestTo(intersections);
			if (isPointBetweenGoalposts(closestIntersection))
			{
				return closestIntersection;
			} else
			{
				IVector2 destination = ballPos
						.addNew(Vector2.fromX(Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius()));
				return Geometry.getPenaltyAreaOur().isPointInShape(destination)
						? destination : Geometry.getGoalOur().getCenter();
			}
		}
		return Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(3 * Geometry.getBotRadius()));
	}
	
	
	private IVector2 getDestination(IVector2 ballPos, GameState gameState)
	{
		IVector2 placementPos = gameState.getBallPlacementPositionForUs();
		double margin = 500 + 2 * Geometry.getBotRadius();
		ITube forbiddenZone = Tube.create(placementPos, ballPos, margin);
		
		IVector2 possibleDest = Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(3 * Geometry.getBotRadius()));
		if (!forbiddenZone.isPointInShape(possibleDest))
		{
			return possibleDest;
		} else
		{
			ILine line = Line.fromPoints(ballPos, placementPos);
			IVector2 newPos = line.nearestPointOnLine(getPos())
					.addNew(line.directionVector().getNormalVector().scaleToNew(margin));
			
			return Geometry.getField().isPointInShape(newPos) ? newPos
					: line.nearestPointOnLine(getPos()).addNew(line.directionVector().getNormalVector().scaleToNew(-margin));
		}
		
	}
	
	
	private boolean isPointBetweenGoalposts(IVector2 destination)
	{
		return Math.abs(destination.y()) < Math.abs(Geometry.getGoalOur().getLeftPost().y()
				- Geometry.getBotRadius() - Geometry.getBallRadius());
	}
}