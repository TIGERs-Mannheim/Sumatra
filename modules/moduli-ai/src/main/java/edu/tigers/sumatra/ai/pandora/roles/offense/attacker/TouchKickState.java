/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.awt.Color;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.action.moves.EOffensiveActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class TouchKickState extends AKickState
{
	@Configurable(defValue = "1.0")
	private static double maxBallSpeed = 1.0;

	@Configurable(defValue = "true")
	private static boolean allowContinuousActionUpdate = true;

	@Configurable(defValue = "true")
	private static boolean actionUpdateDuringSkirmish = true;

	@Configurable(defValue = "false")
	private static boolean disableActionUpdateIfEnemyIsClose = false;

	@Configurable(defValue = "400.0")
	private static double distOfFoeToBallToNotUpdateAction = 400;

	@Configurable(defValue = "400.0")
	private static double switchToApproachDist = 400;

	static
	{
		ConfigRegistration.registerClass("roles", TouchKickState.class);
	}

	private TouchKickSkill skill;

	private KickTarget kickTarget;

	private DynamicPosition skillTarget;
	private KickParams kickParams;
	private IPassTarget passTarget;

	private OffensiveAction currentOffensiveAction;


	public TouchKickState(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		skillTarget = new DynamicPosition(Vector2.zero());
		kickParams = KickParams.straight(0.0);
		updateAction();
		skill = new TouchKickSkill(skillTarget, kickParams);
		setNewSkill(skill);
	}


	@Override
	public void doUpdate()
	{
		DrawableAnnotation da;
		boolean notEnoughTimeToChangeAction = isEnemyThreateningMe();
		boolean updateDuringSkirmish = actionUpdateDuringSkirmish
				&& getAiFrame().getTacticalField().getSkirmishInformation().isSkirmishDetected();
		boolean changingActionAllowed = (!notEnoughTimeToChangeAction || !disableActionUpdateIfEnemyIsClose);
		if ((allowContinuousActionUpdate || updateDuringSkirmish)
				&& changingActionAllowed)
		{
			updateAction();
			da = new DrawableAnnotation(getPos(), "Updating Action");
		} else
		{
			da = new DrawableAnnotation(getPos(), "Not Updating Action");
		}
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(da);

		final EKickerDevice device = detectDevice(kickTarget);
		kickParams.setDevice(device);
		kickParams.setKickSpeed(getKickSpeed(device, kickTarget));
		skill.setMarginToTheirPenArea(getMarginToTheirPenArea());

		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);

		// check conditions to change State
		handleStateTransitions();

		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(
				new DrawableLine(Line.fromPoints(getBall().getPos(), skillTarget.getPos()), Color.RED));
	}

	private void handleStateTransitions() {
		if (getPos().distanceTo(getBall().getPos()) > switchToApproachDist)
		{
			triggerEvent(EBallHandlingEvent.BALL_LOST);
		} else if (getBall().getVel().getLength2() > maxBallSpeed)
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES);
		} else if (getAiFrame().getGamestate().isStandardSituation())
		{
			triggerEvent(EBallHandlingEvent.FREE_KICK);
		} else if (getAiFrame().getTacticalField().getOffensiveActions().get(getBotID())
				.getAction() == EOffensiveAction.FINISHER_KICK)
		{
			triggerEvent(EBallHandlingEvent.START_FINISHER_MOVE);
		} else if (getAiFrame().getTacticalField().getOffensiveActions().get(getBotID())
				.getAction() == EOffensiveAction.PROTECT)
		{
			triggerEvent(EBallHandlingEvent.BALL_POSSESSION_THREATENED);
		}
	}


	private boolean isEnemyThreateningMe()
	{
		double distOfFoeToBall = getWFrame().getFoeBots().values().stream()
				.mapToDouble(bot -> bot.getBotKickerPos().distanceTo(getBall().getPos()))
				.min()
				.orElse(Double.MAX_VALUE);
		DrawableAnnotation da = new DrawableAnnotation(getPos(), "dist of foe: " + distOfFoeToBall,
				Vector2.fromXY(-200, 0));
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(da);
		return distOfFoeToBall < distOfFoeToBallToNotUpdateAction;
	}


	private void updateAction()
	{
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		currentOffensiveAction = offensiveAction;
		kickTarget = offensiveAction.getKickTarget();
		skillTarget.update(kickTarget.getTarget());
		passTarget = offensiveAction.getRatedPassTarget().orElse(null);
	}


	@Override
	public Optional<EOffensiveActionMove> getCurrentOffensiveActionMove()
	{
		return Optional.of(currentOffensiveAction.getMove());
	}
}
