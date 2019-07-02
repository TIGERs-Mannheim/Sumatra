/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import java.util.List;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.defense.KeepDistanceToBall;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;


/**
 * Handles Stopped State and Ball Placement
 */
public class KeeperStoppedState extends AKeeperState
{
	private AMoveToSkill posSkill;
	private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall();
	
	
	/**
	 * @param parent the parent keeper role
	 */
	public KeeperStoppedState(KeeperRole parent)
	{
		super(parent, EKeeperState.STOPPED);
	}
	
	
	@Override
	public void doEntryActions()
	{
		posSkill = AMoveToSkill.createMoveToSkill();
		posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		posSkill.getMoveCon().setBallObstacle(true);
		posSkill.getMoveCon().setGoalPostObstacle(true);
		setNewSkill(posSkill);
	}
	
	
	@Override
	public void doUpdate()
	{
		posSkill.getMoveCon().updateDestination(dest());
		posSkill.getMoveCon().updateTargetAngle(calcDefendingOrientation());
	}
	
	
	private IVector2 dest()
	{
		IVector2 dest = calcNewDestinationOnGoalLine();
		GameState gameState = getAiFrame().getGamestate();
		if (gameState.isBallPlacement() && posSkill.isInitialized())
		{
			keepDistanceToBall.update(getAiFrame(), posSkill.getBotId(), dest);
			return keepDistanceToBall.freeDestination();
		}
		return dest;
	}
	
	
	private IVector2 calcNewDestinationOnGoalLine()
	{
		IVector2 ballPos = getWFrame().getBall().getTrajectory().getPosByTime(0.1).getXYVector();
		ICircle forbiddenArea = Circle.createCircle(ballPos,
				RuleConstraints.getStopRadius() + Geometry.getBotRadius() * 4);
		
		
		final Vector2 destination = Geometry.getGoalOur().getCenter()
				.addNew(Vector2.fromX(Geometry.getPenaltyAreaDepth() / 2));
		
		if (forbiddenArea.isPointInShape(destination) && posSkill.isInitialized())
		{
			// return the closest intersection to the actual position of the bot
			List<IVector2> intersections = forbiddenArea
					.lineIntersections(Line.fromDirection(destination, Vector2f.Y_AXIS));
			return getWFrame().getBot(posSkill.getBotId()).getPos().nearestTo(intersections);
		}
		
		return destination;
	}
}
