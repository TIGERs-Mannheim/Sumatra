/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import static edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode.MAX;

import java.awt.Color;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.EBallResponsibility;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickNormalSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.util.ExtendedPenaltyArea;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * DefenderPenAreaRole
 *
 * @author Jonas, Stefan
 */
public class DefenderPenAreaRole extends ADefenseRole
{
	
	@Configurable(defValue = "1.0")
	private static double requiredMinimumSlackTime = 1.0;
	
	@Configurable(defValue = "0.6")
	private static double slackTimeHyst = 0.6;
	
	@Configurable(defValue = "500.0")
	private static double moveDirectlyMargin = 500;
	
	/*
	 * use two states, one for finding the way back to the penalty area
	 * and the other one to move smoothly on the penalty area line when being already on it
	 */
	private enum EEvent implements IEvent
	{
		REACHED_PEN_AREA,
		LEFT_PEN_AREA,
		ENOUGH_TIME_TO_KICK,
		SITUATION_IS_DANGEROUS
	}
	
	
	private IVector2 destination = Vector2.fromXY(0, 0);
	private double distanceToPenArea = 300;
	private boolean isRoleTestMode = false;
	private final IPenaltyArea penaltyAreaOur = Geometry.getPenaltyAreaOur()
			.withMargin(Geometry.getBotRadius() * 2 + Geometry.getBallRadius());
	
	
	/**
	 * Default
	 */
	public DefenderPenAreaRole()
	{
		super(ERole.DEFENDER_PEN_AREA);
		
		IState moveDirectly = new MoveDirectlyState();
		IState moveOnPenArea = new MoveOnPenAreaState();
		IState kickBall = new KickBallState();
		// go from state(arg1) into state(arg3) when event(arg2) is triggered
		addTransition(moveDirectly, EEvent.REACHED_PEN_AREA, moveOnPenArea);
		addTransition(moveOnPenArea, EEvent.LEFT_PEN_AREA, moveDirectly);
		addTransition(moveOnPenArea, EEvent.ENOUGH_TIME_TO_KICK, kickBall);
		addTransition(kickBall, EEvent.SITUATION_IS_DANGEROUS, moveOnPenArea);
		// start with going straight to the target position
		setInitialState(moveDirectly);
	}
	
	
	/**
	 * Constructor for TextMode
	 * 
	 * @param isRoleTestMode
	 */
	public DefenderPenAreaRole(final Boolean isRoleTestMode)
	{
		this();
		this.isRoleTestMode = isRoleTestMode;
		this.destination = Geometry.getPenaltyAreaOur().withMargin(distanceToPenArea).stepAlongPenArea(Vector2.zero(), 0);
	}
	
	
	/**
	 * can be called from play to set the destination of this role
	 * 
	 * @param destination
	 */
	public void setTarget(final IVector2 destination)
	{
		this.destination = destination;
		// update distance to penalty area
		final ExtendedPenaltyArea toProjectOn = new ExtendedPenaltyArea(Geometry.getPenaltyAreaOur().getRadius());
		double destinationToPenAreaDist = Vector2
				.fromPoints(toProjectOn.projectPointOnPenaltyAreaLine(destination), destination).getLength();
		distanceToPenArea = destinationToPenAreaDist;
	}
	
	
	public IVector2 getTarget()
	{
		return destination;
	}
	
	
	/**
	 * @param hyst
	 * @return whether there is enough time left to kick the ball
	 */
	public boolean enoughTimeToKickSafely(double hyst)
	{
		IVector2 target = getWFrame().getBall().getPos();
		double minFoeArrivalTime = getWFrame().getFoeBots().values().stream()
				.mapToDouble(bot -> TrajectoryGenerator.generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		double myTime = TrajectoryGenerator.generatePositionTrajectory(getBot(), target).getTotalTime();
		double slackTime = minFoeArrivalTime - myTime - requiredMinimumSlackTime + hyst;
		return slackTime > 0;
	}
	
	
	/**
	 * Tell the role that is is selected to kick the ball.
	 */
	public void setAsActiveKicker()
	{
		if (getCurrentState() instanceof MoveOnPenAreaState)
		{
			triggerEvent(EEvent.ENOUGH_TIME_TO_KICK);
		}
	}
	
	private class MoveDirectlyState implements IState
	{
		private AMoveToSkill moveToPenAreaSkill;
		
		
		@Override
		public void doEntryActions()
		{
			moveToPenAreaSkill = AMoveToSkill.createMoveToSkill();
			setNewSkill(moveToPenAreaSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			ExtendedPenaltyArea extendedPenArea = new ExtendedPenaltyArea(
					Geometry.getPenaltyAreaOur().getRadius() + distanceToPenArea);
			if (extendedPenArea.isPointInShape(getPos(), Geometry.getBotRadius() + moveDirectlyMargin))
			{
				triggerEvent(EEvent.REACHED_PEN_AREA);
			}
			moveToPenAreaSkill.getMoveCon().updateDestination(destination);
		}
	}
	
	private class MoveOnPenAreaState implements IState
	{
		private MoveOnPenaltyAreaSkill moveOnPenAreaSkill;
		
		
		@Override
		public void doEntryActions()
		{
			moveOnPenAreaSkill = new MoveOnPenaltyAreaSkill(distanceToPenArea);
			setNewSkill(moveOnPenAreaSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			if (isRoleTestMode)
			{
				destination = getTestDestination();
			}
			moveOnPenAreaSkill.updateDistanceToPenArea(distanceToPenArea);
			moveOnPenAreaSkill.updateDestination(destination);
			armDefenders(moveOnPenAreaSkill);
			drawShapes();
		}
		
		
		private void drawShapes()
		{
			List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
					.get(EAiShapesLayer.DEFENSE_PENALTY_AREA_ROLE);
			shapes.add(new DrawableCircle(Circle.createCircle(destination, Geometry.getBotRadius() + 20),
					getBotID().getTeamColor().getColor()));
			shapes.add(new DrawableLine(Line.fromPoints(destination, getPos()), Color.GREEN));
		}
		
		
		// for testing purposes, use intersection of ball-to-goal-center-line with penalty area as target point
		private IVector2 getTestDestination()
		{
			IVector2 ballPos = getBall().getPos();
			ExtendedPenaltyArea extendedPenArea = new ExtendedPenaltyArea(
					Geometry.getPenaltyAreaOur().getRadius() + distanceToPenArea);
			destination = extendedPenArea.lineIntersectionsBallGoalLine(ballPos);
			if (ballPos.x() <= (-Geometry.getFieldLength() / 2))
			{
				// invert y coordinate because the wrong penalty area line is intersected by constructing a line from ball
				// via own goal center to penArea
				destination = Vector2.fromXY(destination.x(), destination.y() * -1);
			}
			return destination;
		}
	}
	
	private class KickBallState implements IState
	{
		
		private AKickSkill kickSkill = null;
		
		
		@Override
		public void doEntryActions()
		{
			kickSkill = new KickNormalSkill(new DynamicPosition(Geometry.getGoalTheir().getCenter()), MAX,
					EKickerDevice.CHIP, 8);
			setNewSkill(kickSkill);
		}
		
		
		@Override
		public void doUpdate()
		{
			// if dangerous switch State
			if (penaltyAreaOur.isPointInShape(getWFrame().getBall().getPos())
					|| !enoughTimeToKickSafely(slackTimeHyst)
					|| getAiFrame().getTacticalField().getBallResponsibility() != EBallResponsibility.DEFENSE)
			{
				triggerEvent(EEvent.SITUATION_IS_DANGEROUS);
			}
		}
	}
}
