/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.ProtectBallSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author MarkG
 */
public class ProtectionKickStateState extends AOffensiveRoleKickStateState
{
	private ProtectBallSkill	protectSkill	= null;
	private long					initTime			= 0;
	
	
	/**
	 * @param role the offensiveRole instance
	 */
	public ProtectionKickStateState(OffensiveRoleKickState role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getDestination()
	{
		return protectSkill.getMoveCon().getDestination();
	}
	
	
	/**
	 * Called once on state entrance
	 */
	@Override
	public void doEntryActions()
	{
		protectSkill = new ProtectBallSkill(calcProtectAgainstTarget());
		setNewSkill(protectSkill);
		initTime = getWFrame().getTimestamp();
	}
	
	
	/**
	 * Called continuously with each new aiFrame
	 */
	@Override
	public void doUpdate()
	{
		double remTime = 2.0 - (getWFrame().getTimestamp() - initTime) / 1e9;
		protectSkill.setProtectionTarget(calcProtectAgainstTarget());
		if (isProtecionNotRequiredAnymore())
		{
			triggerInnerEvent(EKickStateEvent.FOUND_GOOD_STRATEGY);
		} else if (remTime <= 0)
		{
			triggerInnerEvent(EKickStateEvent.TIMED_OUT);
		}
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ROLE_STATUS)
				.add(new DrawableAnnotation(getPos(), String.format("time left: %.2f", remTime))
						.setOffset(Vector2.fromX(200)));
	}
	
	
	/**
	 * @return an optional identifier for this state, defaults to the class name
	 */
	@Override
	public String getIdentifier()
	{
		return EKickStateState.PROTECTION.name();
	}
	
	
	private DynamicPosition calcProtectAgainstTarget()
	{
		BotDistance bot = getAiFrame().getTacticalField().getEnemyClosestToBall();
		return new DynamicPosition(bot.getBot().getId());
	}
}
