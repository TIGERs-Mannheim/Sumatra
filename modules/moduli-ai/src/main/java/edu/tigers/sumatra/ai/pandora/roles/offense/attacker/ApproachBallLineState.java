/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ApproachBallLineSkill;


/**
 * Catch up with the ball, if the bot is in front of the ball.
 * The state is over, when the bot is near the ball travel line
 */
public class ApproachBallLineState extends AOffensiveState
{
	@Configurable(defValue = "300.0", comment = "When distance is below, the ball is considered catched up")
	private static double maxDistanceToBallLine = 300;

	@Configurable(defValue = "50.0", comment = "Margin (excl. bot radius) to penArea")
	private static double extraMarginToApproachPos = 50;

	@Configurable(defValue = "0.4", comment = "Max ball vel angle [rad] deviation from initial angle. Desired approaching pos will be reset above this.")
	private static double maxAllowedBallVelAngleDeviation = 0.4;

	private ApproachBallLineSkill skill;
	private IVector2 initBallVelAngle;

	static
	{
		ConfigRegistration.registerClass("roles", ApproachBallLineState.class);
	}


	public ApproachBallLineState(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		skill = new ApproachBallLineSkill();
		// calculate the desired approach position only once
		// the ball interception is not really stable (and does not really aim for it),
		// so the robot destination would be unstable.
		calcDesiredApproachPos().ifPresent(skill::setDesiredApproachingPos);
		setSkillTarget();
		setNewSkill(skill);
		initBallVelAngle = getBall().getVel();
	}


	private Optional<IVector2> calcDesiredApproachPos()
	{
		final IVector2 target = getAiFrame().getTacticalField().getBallInterceptions().get(getBotID())
				.getBotTarget();

		if (target == null)
		{
			return Optional.empty();
		}

		final ILineSegment travelLine = getBall().getTrajectory().getTravelLineSegment();
		final double margin = Geometry.getBotRadius() + extraMarginToApproachPos;
		final IPenaltyArea penAreaOur = Geometry.getPenaltyAreaOur().withMargin(margin);
		final IPenaltyArea penAreaTheir = Geometry.getPenaltyAreaTheir().withMargin(margin);
		IPenaltyArea[] penaltyAreas = new IPenaltyArea[] { penAreaOur, penAreaTheir };
		for (IPenaltyArea penaltyArea : penaltyAreas)
		{
			if (penaltyArea.isPointInShapeOrBehind(target) ||
					penaltyArea.isIntersectingWithLine(travelLine))
			{
				return Optional.empty();
			}
		}

		return Optional.of(target);
	}


	@Override
	public void doUpdate()
	{
		if (initBallVelAngle != null
				&& getBall().getVel().angleToAbs(initBallVelAngle).orElse(0.0) > maxAllowedBallVelAngleDeviation)
		{
			calcDesiredApproachPos().ifPresent(skill::setDesiredApproachingPos);
			initBallVelAngle = null;
		}

		skill.setMarginToTheirPenArea(getMarginToTheirPenArea());
		setSkillTarget();
		if (ballMovesAwayFromMe() && !opponentInWay(300))
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES_AWAY_FROM_ME);
		} else if (ballMovesTowardsMeAndLineIsApproached())
		{
			triggerEvent(EBallHandlingEvent.BALL_LINE_APPROACHED);
		} else if (getBall().getVel().getLength2() < 0.1)
		{
			triggerEvent(EBallHandlingEvent.BALL_STOPPED_MOVING);
		}
	}


	private boolean ballMovesTowardsMeAndLineIsApproached()
	{
		IVector2 ballStart = getBall().getPos();
		IHalfLine ballTravelLine = Lines.halfLineFromDirection(ballStart,
				getBall().getTrajectory().getTravelLine().directionVector());
		IVector2 ballToMe = getBot().getBotKickerPos().subtractNew(getBall().getPos());
		IVector2 ballVel = getBall().getVel().scaleToNew(200);
		IVector2 desiredApproachingPos = Optional.ofNullable(skill.getDesiredApproachingPos()).orElse(getPos());
		IVector2 closestToApproachingPos = ballTravelLine.closestPointOnLine(desiredApproachingPos);
		return closestToApproachingPos.distanceTo(getBot().getBotKickerPos()) < maxDistanceToBallLine
				&& ballToMe.angleTo(ballVel).orElse(0.0) < AngleMath.PI_HALF;
	}


	private void setSkillTarget()
	{
		OffensiveAction action = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		if (action.isAllowRedirect())
		{
			skill.setTarget(action.getKickTarget().getTarget().getPos());
		} else
		{
			skill.setTarget(getBall().getPos());
		}
	}


	private boolean ballMovesAwayFromMe()
	{
		IVector2 base = getBall().getPos().addNew(getBall().getVel().scaleToNew(200));
		return getPos().subtractNew(base).angleToAbs(getBall().getVel()).map(a -> a > AngleMath.PI_HALF).orElse(false);
	}
}
