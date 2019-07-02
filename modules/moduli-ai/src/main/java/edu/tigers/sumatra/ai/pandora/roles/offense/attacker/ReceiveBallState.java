/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.awt.Color;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReceiveBallState extends AOffensiveState
{
	@Configurable(defValue = "0.3")
	private static double maxAllowedImpactTimeToSwitchToRedirect = 0.3;

	@Configurable(defValue = "100.0")
	private static double receiveStepTowardsBall = 100;

	static
	{
		ConfigRegistration.registerClass("roles", ReceiveBallState.class);
	}

	private ReceiveBallSkill skill;

	private final DynamicPosition receivingPosWithoutPassTarget = new DynamicPosition(Vector2f.zero());


	public ReceiveBallState(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		skill = new ReceiveBallSkill(getReceivingPosition());
		skill.getMoveCon().setPenaltyAreaAllowedTheir(true);
		setNewSkill(skill);
	}


	private DynamicPosition getReceivingPosition()
	{
		return getAiFrame().getTacticalField().getOffensiveStrategy().getActivePassTarget()
				.filter(pt -> pt.getBotId().equals(getBotID()))
				.map(IPassTarget::getPos)
				.map(DynamicPosition::new)
				.orElse(receivingPosWithoutPassTarget);
	}


	private IVector2 getReceivingPosWithoutPassTarget()
	{
		IVector2 closestPoint = getBall().getTrajectory().getTravelLineRolling()
				.closestPointOnLine(getBot().getBotKickerPos());
		return LineMath.stepAlongLine(closestPoint, getBall().getPos(), receiveStepTowardsBall);
	}


	@Override
	public void doUpdate()
	{
		receivingPosWithoutPassTarget.setPos(getReceivingPosWithoutPassTarget());
		skill.setMarginToTheirPenArea(getMarginToTheirPenArea());

		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		if (skill.ballHasBeenReceived())
		{
			triggerEvent(EBallHandlingEvent.BALL_RECEIVED);
			return;
		} else if (skill.receivingBall())
		{
			return;
		}

		if (!skill.ballCanBeReceivedAtReceivingPosition())
		{
			triggerEvent(EBallHandlingEvent.BALL_NOT_RECEIVED);
		} else if (offensiveAction.isAllowRedirect())
		{
			double impactTime = getTimeBallNeedsToReachMe();
			drawImpactTime(impactTime);
			if (impactTime > maxAllowedImpactTimeToSwitchToRedirect)
			{
				triggerEvent(EBallHandlingEvent.SWITCH_TO_REDIRECT);
			}
		}
	}


	private void drawImpactTime(final double impactTime)
	{
		DrawableAnnotation da = new DrawableAnnotation(getPos(), "redirect impact time: " + impactTime, Color.black);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(da);
	}


	@Override
	public Optional<EOffensiveActionMove> getCurrentOffensiveActionMove()
	{
		return Optional.of(EOffensiveActionMove.RECEIVE_BALL);
	}
}
