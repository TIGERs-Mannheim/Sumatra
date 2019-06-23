/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.awt.Color;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
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
	
	@Configurable(defValue = "400.0")
	private static double switchToApproachDist = 400;
	
	static
	{
		ConfigRegistration.registerClass("roles", TouchKickState.class);
	}
	
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
		setNewSkill(new TouchKickSkill(skillTarget, kickParams));
	}
	
	
	@Override
	public void doUpdate()
	{
		if (allowContinuousActionUpdate || (actionUpdateDuringSkirmish
				&& getAiFrame().getTacticalField().getSkirmishInformation().isSkirmishDetected()))
		{
			updateAction();
		}
		
		final EKickerDevice device = detectDevice(kickTarget);
		kickParams.setDevice(device);
		kickParams.setKickSpeed(getKickSpeed(device, kickTarget));
		
		getAiFrame().getTacticalField().getAiInfoForNextFrame().announcePassingTo(passTarget);
		
		if (getPos().distanceTo(getBall().getPos()) > switchToApproachDist)
		{
			triggerEvent(EBallHandlingEvent.BALL_LOST);
		} else if (getBall().getVel().getLength2() > maxBallSpeed)
		{
			triggerEvent(EBallHandlingEvent.BALL_MOVES);
		} else if (getAiFrame().getGamestate().isStandardSituation())
		{
			triggerEvent(EBallHandlingEvent.FREE_KICK);
		}
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(
				new DrawableLine(Line.fromPoints(getBall().getPos(), skillTarget), Color.RED));
	}
	
	
	private void updateAction()
	{
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		currentOffensiveAction = offensiveAction;
		kickTarget = offensiveAction.getKickTarget();
		skillTarget.update(kickTarget.getTarget());
		passTarget = offensiveAction.getPassTarget().orElse(null);
	}
	
	
	@Override
	public Optional<OffensiveAction> getCurrentOffensiveAction()
	{
		return Optional.of(currentOffensiveAction);
	}
}
