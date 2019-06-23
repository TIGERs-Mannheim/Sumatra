/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ReceiveBallSkill;

import java.awt.Color;


public class ReceiveBallState extends AOffensiveState
{
	@Configurable(defValue = "0.4")
	private static double maxAllowedImpactTimeToSwitchToRedirect = 0.4;
	
	static
	{
		ConfigRegistration.registerClass("roles", ReceiveBallState.class);
	}
	
	private ReceiveBallSkill skill;
	
	
	public ReceiveBallState(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = new ReceiveBallSkill(getReceivingPosition());
		setNewSkill(skill);
	}
	
	
	private IVector2 getReceivingPosition()
	{
		return getAiFrame().getTacticalField().getOffensiveStrategy().getActivePassTarget()
				.filter(pt -> pt.getBotId().equals(getBotID()))
				.map(IPassTarget::getKickerPos)
				.orElse(getPos());
	}
	
	
	@Override
	public void doUpdate()
	{
		OffensiveAction offensiveAction = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
		if (skill.ballHasBeenReceived())
		{
			triggerEvent(EBallHandlingEvent.BALL_RECEIVED);
		} else if (!skill.ballCanBeReceived())
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
	
	
	private double getTimeBallNeedsToReachMe()
	{
		double ballDistToMe = getBot().getBotKickerPos().distanceTo(getBall().getPos());
		return getBall().getTrajectory().getTimeByDist(ballDistToMe);
	}
	
	
	private void drawImpactTime(final double impactTime)
	{
		DrawableAnnotation da = new DrawableAnnotation(getPos(), "redirect impact time: " + impactTime, Color.black);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ATTACKER).add(da);
	}
}
