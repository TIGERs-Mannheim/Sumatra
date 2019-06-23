/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.SpecialMoveCommand;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.ValuePoint;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.util.redirect.ARedirectBallConsultant;
import edu.tigers.sumatra.skillsystem.skills.util.redirect.RedirectBallConsultantFactory;
import edu.tigers.sumatra.wp.data.ITrackedBall;

import java.awt.Color;


/**
 * The Offensive role is always ball oriented.
 *
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleSpecialMovementState extends AOffensiveRoleState
{
	private AMoveToSkill move = null;
	private int idx = 0;
	private IVector2 initialDest;
	
	
	/**
	 * @param role parent role
	 */
	public OffensiveRoleSpecialMovementState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getMoveDest()
	{
		// dont publish SpecialMovePose.
		return null;
	}
	
	
	@Override
	public String getIdentifier()
	{
		return OffensiveStrategy.EOffensiveStrategy.SPECIAL_MOVE.name();
	}
	
	
	@Override
	public void doExitActions()
	{
		getAiFrame().getAICom().setResponded(false);
	}
	
	
	@Override
	public void doEntryActions()
	{
		move = AMoveToSkill.createMoveToSkill();
		idx = getAiFrame().getPrevFrame().getAICom().getSpecialMoveCounter();
		initialDest = getPos();
		
		setNewSkill(move);
		getAiFrame().getPrevFrame().getAICom().setSpecialMoveCounter(idx + 1);
	}
	
	
	@Override
	public void doUpdate()
	{
		if (idx < getAiFrame().getTacticalField().getOffensiveStrategy().getSpecialMoveCommands().size())
		{
			final SpecialMoveCommand command = getAiFrame().getTacticalField().getOffensiveStrategy()
					.getSpecialMoveCommands()
					.get(idx);
			IVector2 movePos = command.getMovePosition().get(0);
			
			if (Geometry.getPenaltyAreaTheir()
					.isPointInShape(movePos, Geometry.getBotRadius() * 1.2))
			{
				movePos = new ValuePoint(Geometry.getPenaltyAreaTheir()
						.withMargin(Geometry.getBotRadius() * 1.2)
						.nearestPointOutside(movePos));
			} else if (Geometry.getPenaltyAreaOur()
					.isPointInShape(movePos, OffensiveConstants.getDistanceToPenaltyArea()))
			{
				movePos = new ValuePoint(Geometry.getPenaltyAreaOur()
						.withMargin(OffensiveConstants.getDistanceToPenaltyArea())
						.nearestPointOutside(movePos));
			}
			
			IVector2 otarget = getAiFrame().getTacticalField().getBestDirectShotTarget();
			if (otarget == null)
			{
				otarget = Geometry.getGoalTheir().getCenter();
			}
			IVector2 target = getAiFrame().getTacticalField()
					.getBestDirectShotTarget();
			double orientation;
			if (OffensiveMath.isBallRedirectReasonable(getWFrame(), getPos(), otarget))
			{
				if (target == null)
				{
					target = Geometry.getGoalTheir().getCenter();
				}
				
				ITrackedBall ball = getWFrame().getBall();
				IVector2 movePosToTarget = target.subtractNew(movePos);
				IVector2 ballVelAtCollision = movePos.subtractNew(ball.getPos()).scaleTo(4.0);
				double ballRedirectAngle = movePosToTarget.angleTo(ballVelAtCollision).orElse(0.);
				
				RedirectBallConsultantFactory builder = new RedirectBallConsultantFactory();
				builder.setBallRedirectAngle(ballRedirectAngle)
						.setBallVelAtCollision(ballVelAtCollision)
						.setDesiredVelocity(6.0);
				
				ARedirectBallConsultant consultant = builder.create();
				orientation = consultant.getBotTargetAngle();
			} else
			{
				orientation = getWFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			
			double botTime = TrajectoryGenerator.generatePositionTrajectory(getBot(), movePos).getTotalTime()
					+ OffensiveConstants.getNeededTimeForPassReceivingBotOffset();
			
			double ballArrivalTime = command.getTimeUntilPassArrives();
			DrawableAnnotation dt = new DrawableAnnotation(movePos.addNew(Vector2.fromXY(400, 100)),
					"botTime: " + botTime, Color.red);
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING_DEBUG).add(dt);
			
			if ((ballArrivalTime > botTime) && OffensiveConstants.isEnableRedirectorStopMove())
			{
				movePos = initialDest;
			}
			
			move.getMoveCon().updateTargetAngle(orientation);
			move.getMoveCon().updateDestination(
					BotShape.getCenterFromKickerPos(movePos, orientation, getBot().getCenter2DribblerDist()));
		}
	}
}
