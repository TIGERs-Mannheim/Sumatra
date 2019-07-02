/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


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
			
			IVector2 dest = LineMath.stepAlongLine(passTarget.getPos(), getBall().getPos(),
					-getBot().getCenter2DribblerDist() - Geometry.getBallRadius());
			
			// Calculate the correct target angle for receiving the ball
			IVector2 ballVelAtReceive = getBot().getPos().subtractNew(getBall().getPos())
					.scaleTo(OffensiveConstants.getMaxPassEndVelRedirect());
			
			OffensiveAction action = getAiFrame().getTacticalField().getOffensiveActions().get(getBotID());
			IVector2 target = getAiFrame().getTacticalField().getBestGoalKickTargetForBot().get(getBotID())
					.map(IRatedTarget::getTarget)
					.map(DynamicPosition::getPos)
					.orElse(Geometry.getGoalTheir().getCenter());
			double vel = action.getKickTarget().getBallSpeedAtTarget();
			
			IVector2 desiredBallVel = target.subtractNew(getPos()).scaleTo(vel);
			
			double targetAngle = RedirectConsultantFactory.createDefault(ballVelAtReceive, desiredBallVel)
					.getTargetAngle();
			
			IVector2 dir = Vector2.fromAngle(targetAngle);
			DrawableArrow da = new DrawableArrow(getPos(), dir.scaleToNew(200), Color.WHITE);
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING).add(da);
			
			boolean lookAtBall = OffensiveMath.getRedirectAngle(getBall().getPos(), getBot().getBotKickerPos(),
					target) >= OffensiveConstants
							.getMaximumReasonableRedirectAngle();
			
			getCurrentSkill().getMoveCon().updateDestination(dest);
			
			if (lookAtBall)
			{
				getCurrentSkill().getMoveCon().updateLookAtTarget(getBall());
			} else
			{
				getCurrentSkill().getMoveCon().updateTargetAngle(targetAngle);
			}
		}
	}
}
