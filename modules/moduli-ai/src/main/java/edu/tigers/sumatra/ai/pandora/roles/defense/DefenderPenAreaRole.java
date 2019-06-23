/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.defense;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveOnPenaltyAreaSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.IDefensePenArea;
import edu.tigers.sumatra.skillsystem.skills.util.penarea.PenAreaFactory;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * DefenderPenAreaRole
 *
 * @author Jonas, Stefan
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class DefenderPenAreaRole extends ADefenseRole
{
	private static final Logger log = Logger.getLogger(DefenderPenAreaRole.class.getName());
	
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
	 * can be called from play to set the destination of this role
	 * 
	 * @param destination
	 */
	public void setTarget(final IVector2 destination)
	{
		this.destination = destination;
		// update distance to penalty area
		final IDefensePenArea toProjectOn = PenAreaFactory.buildWithMargin(0);
		double destinationToPenAreaDist = toProjectOn.projectPointOnPenaltyAreaLine(destination).distanceTo(destination);
		distanceToPenArea = destinationToPenAreaDist;
		
		if (Geometry.getPenaltyAreaOur().isPointInShape(destination, Geometry.getBotRadius() - 5))
		{
			log.warn("Dest inside penArea " + destination, new Exception());
		}
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
	
	private class MoveDirectlyState extends AState
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
			Set<BotID> companions = getAiFrame().getPlayStrategy().getActiveRoles(ERole.DEFENDER_PEN_AREA).stream()
					.map(ARole::getBotID).collect(Collectors.toSet());
			Set<BotID> closeOpponents = getWFrame().getFoeBots().values().stream()
					.filter(b -> b.getPos().distanceTo(destination) < Geometry.getBotRadius() * 3)
					.map(ITrackedBot::getBotId).collect(Collectors.toSet());
			Set<BotID> ignoredBots = new HashSet<>();
			ignoredBots.addAll(companions);
			ignoredBots.addAll(closeOpponents);
			moveToPenAreaSkill.getMoveCon().setIgnoredBots(ignoredBots);
			
			IDefensePenArea extendedPenArea = PenAreaFactory.buildWithMargin(distanceToPenArea);
			if (extendedPenArea.isPointInShape(getPos(), Geometry.getBotRadius() + moveDirectlyMargin))
			{
				triggerEvent(EEvent.REACHED_PEN_AREA);
			}
			moveToPenAreaSkill.getMoveCon().updateDestination(destination);
			
			double targetAngle = getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle();
			moveToPenAreaSkill.getMoveCon().updateTargetAngle(targetAngle);
		}
	}
	
	private class MoveOnPenAreaState extends AState
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
			boolean distanceRequired = getAiFrame().getGamestate().isDistanceToBallRequired();
			moveOnPenAreaSkill.getMoveCon().setIgnoreGameStateObstacles(!distanceRequired);
			moveOnPenAreaSkill.getMoveCon().setBallObstacle(distanceRequired);
			
			moveOnPenAreaSkill.updateDistanceToPenArea(distanceToPenArea);
			moveOnPenAreaSkill.updateDestination(destination);
			armDefenders(moveOnPenAreaSkill);
			drawShapes();
		}
		
		
		private void drawShapes()
		{
			List<IDrawableShape> shapes = getAiFrame().getTacticalField().getDrawableShapes()
					.get(EAiShapesLayer.DEFENSE_PENALTY_AREA_ROLE);
			shapes.add(new DrawableLine(Line.fromPoints(destination, getPos()), Color.GREEN));
		}
	}
	
	private class KickBallState extends AState
	{
		@Override
		public void doEntryActions()
		{
			IVector2 targetDest = Vector2.fromXY(Geometry.getCenter().x(),
					Math.signum(getPos().y()) * Geometry.getFieldWidth() / 2);
			setNewSkill(
					new TouchKickSkill(new DynamicPosition(targetDest, 0.6), KickParams.maxChip()));
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
