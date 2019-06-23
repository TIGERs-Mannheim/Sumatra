/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;


/**
 * The pass receiver role prepares for receiving a pass.
 * It drives to the currently active pass target.
 * It will be overtaken by the attacker as soon as the pass is on its way
 */
public class PassReceiverRole extends ARole
{
	private static final Logger log = Logger.getLogger(PassReceiverRole.class.getName());
	
	
	public PassReceiverRole()
	{
		super(ERole.PASS_RECEIVER);
		
		setInitialState(new DefaultState());
	}
	
	private class DefaultState extends AState
	{
		@Override
		public void doEntryActions()
		{
			setNewSkill(AMoveToSkill.createMoveToSkill());
			getCurrentSkill().getMoveCon().setBallObstacle(false);
		}
		
		
		@Override
		public void doUpdate()
		{
			getAiFrame().getTacticalField().getOffensiveStrategy().getActivePassTarget().ifPresent(this::updateTargetPose);
		}
		
		
		private void updateTargetPose(final IPassTarget passTarget)
		{
			if (passTarget.getBotId() != getBotID())
			{
				log.warn("PassTarget botId does not match pass receiver anymore: " + getBotID() + " changed to "
						+ passTarget.getBotId());
			}
			getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			IVector2 dest = LineMath.stepAlongLine(passTarget.getKickerPos(), getBall().getPos(),
					-getBot().getCenter2DribblerDist() - Geometry.getBallRadius());
			getCurrentSkill().getMoveCon().updateDestination(dest);
		}
	}
}
