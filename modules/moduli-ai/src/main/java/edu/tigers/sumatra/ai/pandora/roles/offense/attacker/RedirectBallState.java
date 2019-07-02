/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.awt.Color;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.RedirectBallSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class RedirectBallState extends AKickState
{

	@Configurable(defValue = "0.2")
	private static double maxAllowedImpactTimeToUpdateAction = 0.2;

	private final KickParams kickParams;

	private RedirectBallSkill skill;
	private IPassTarget passTarget;
	private KickTarget kickTarget;
	private OffensiveAction currentOffensiveAction;
	private DynamicPosition skillTarget;

	static
	{
		ConfigRegistration.registerClass("roles", RedirectBallState.class);
	}


	public RedirectBallState(final ARole role)
	{
		super(role);
		kickParams = KickParams.maxStraight();
	}


	@Override
	public void doEntryActions()
	{
		skillTarget = new DynamicPosition(Vector2.zero());
		updateActionFields();
		skill = new RedirectBallSkill(getReceivingPosition(), skillTarget, kickParams);
		setNewSkill(skill);
	}


	private void updateActionFields()
	{
		final OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());

		if (offensiveAction.getMove() == EOffensiveActionMove.GOAL_KICK)
		{
			updateFields(offensiveAction);
			return;
		}

		// it is a redirect pass here, so we should check if the redirect is still possible with the new action
		IVector2 origin = getBall().getPos();
		IVector2 reflector = getBot().getBotKickerPos();
		IVector2 target = offensiveAction.getKickTarget().getTarget().getPos();

		if (currentOffensiveAction == null
				|| OffensiveMath.getRedirectAngle(origin, reflector, target) < OffensiveConstants
						.getMaximumReasonableRedirectAngle())
		{
			// only update action if new action can still redirect the ball !
			updateFields(offensiveAction);
		}
	}


	private void updateFields(final OffensiveAction offensiveAction)
	{
		currentOffensiveAction = offensiveAction;
		kickTarget = offensiveAction.getKickTarget();
		skillTarget.update(kickTarget.getTarget());
		EKickerDevice device = detectDevice(kickTarget);
		kickParams.setKickSpeed(getKickSpeed(device, kickTarget));
		kickParams.setDevice(device);
		passTarget = offensiveAction.getRatedPassTarget().orElse(null);
	}


	@Override
	public void doUpdate()
	{
		if (getTimeBallNeedsToReachMe() > maxAllowedImpactTimeToUpdateAction)
		{
			updateActionFields();
		}
		final EKickerDevice device = detectDevice(kickTarget);
		kickParams.setDevice(device);
		kickParams.setKickSpeed(getKickSpeed(device, kickTarget));
		skill.setMarginToTheirPenArea(getMarginToTheirPenArea());

		if (!currentOffensiveAction.isAllowRedirect())
		{
			triggerEvent(EBallHandlingEvent.BALL_NOT_REDIRECTABLE);
		} else if (!skill.ballCanBeRedirected())
		{
			triggerEvent(EBallHandlingEvent.BALL_NOT_REDIRECTED);
		}
		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(
				new DrawableLine(Line.fromPoints(getBall().getPos(), kickTarget.getTarget().getPos()), Color.RED));
	}


	private IVector2 getReceivingPosition()
	{
		return getAiFrame().getTacticalField().getOffensiveStrategy().getActivePassTarget()
				.filter(pt -> pt.getBotId().equals(getBotID()))
				.map(IPassTarget::getPos)
				.orElseGet(this::getReceivingPosWithoutPassTarget);
	}


	private IVector2 getReceivingPosWithoutPassTarget()
	{
		if (getBall().getVel().getLength2() > 0.5
				&& getBot().getVel().getLength2() > 0.5)
		{
			return getBall().getTrajectory().getTravelLineRolling()
					.intersectLine(Lines.lineFromDirection(getBot().getBotKickerPos(), getBot().getVel()))
					.orElse(getBot().getBotKickerPos());
		}
		return getBot().getBotKickerPos();
	}


	@Override
	public Optional<EOffensiveActionMove> getCurrentOffensiveActionMove()
	{
		return Optional.of(currentOffensiveAction.getMove());
	}
}
