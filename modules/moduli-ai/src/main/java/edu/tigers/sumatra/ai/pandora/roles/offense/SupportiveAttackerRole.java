/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.stream.Collectors;


public class SupportiveAttackerRole extends ARole
{
	private IPenaltyArea area = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + 20);
	
	
	public SupportiveAttackerRole()
	{
		super(ERole.SUPPORTIVE_ATTACKER);
		
		setInitialState(new DefaultState());
	}
	
	private class DefaultState extends AState
	{
		
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 movePos;
			switch (getAiFrame().getTacticalField().getSkirmishInformation().getStrategy())
			{
				case FREE_BALL:
					movePos = calcMovePositionFreeBall();
					break;
				default:
					movePos = getAiFrame().getTacticalField().getSupportiveAttackerMovePos();
			}
			movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
			getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall());
			getCurrentSkill().getMoveCon().updateDestination(movePos);
		}
		
		
		private IVector2 calcMovePositionFreeBall()
		{
			return getAiFrame().getTacticalField().getSkirmishInformation().getSupportiveCircleCatchPos();
		}
	}
	
	
	@Override
	protected void afterUpdate()
	{
		super.afterUpdate();
		
		// determine critical foe bots
		getCurrentSkill().getMoveCon().setCriticalFoeBots(
				getWFrame().getFoeBots().values().stream()
						.filter(b -> OffensiveMath.isBotCritical(b.getPos(), area))
						.map(ITrackedBot::getBotId)
						.collect(Collectors.toSet()));
	}
}
