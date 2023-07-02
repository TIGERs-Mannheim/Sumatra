/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Kick;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.redirect.RedirectConsultantFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;


/**
 * The pass receiver role prepares for receiving a pass.
 * It drives to the currently active pass target.
 * It will be overtaken by the attacker as soon as the pass is on its way
 */
@Log4j2
public class PassReceiverRole extends ARole
{
	@Getter
	private Pass incomingPass;
	@Getter
	@Setter
	private Kick outgoingKick;
	@Setter
	@Getter
	private boolean physicalObstaclesOnly;

	@Getter
	private double receivingPositionReachedIn;


	public PassReceiverRole()
	{
		super(ERole.PASS_RECEIVER);

		setInitialState(new DefaultState());
	}


	public void setIncomingPass(Pass incomingPass)
	{
		if (incomingPass == null || incomingPass.getReceiver().equals(getBotID()))
		{
			this.incomingPass = incomingPass;
		} else
		{
			log.warn("Bot {} got a pass for another receiver: {}", getBotID(), incomingPass.getReceiver(),
					new Exception(""));
		}
	}


	private class DefaultState extends RoleState<MoveToSkill>
	{
		private DefaultState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			if (physicalObstaclesOnly)
			{
				skill.getMoveCon().physicalObstaclesOnly();
			}
		}


		@Override
		protected void onUpdate()
		{
			if (incomingPass == null)
			{
				skill.updateLookAtTarget(getBall());
				getShapes(EAiShapesLayer.OFFENSIVE_PASSING).add(
						new DrawableAnnotation(getPos(), "No incoming pass!")
								.setColor(Color.magenta));
				return;
			}

			var receivingPos = incomingPass.getKick().getTarget();
			double targetAngle = getTargetAngle();
			skill.updateTargetAngle(targetAngle);

			var dest = receivingPos.subtractNew(
					Vector2.fromAngleLength(targetAngle, getBot().getCenter2DribblerDist() + Geometry.getBallRadius()));
			skill.updateDestination(dest);

			skill.getMoveCon().setBallObstacle(dest.distanceTo(getPos()) > 500);

			receivingPositionReachedIn = skill.getDestinationReachedIn();
			draw();
		}


		private double getTargetAngle()
		{
			if (outgoingKick != null)
			{
				var incomingBallVel = incomingPass.getKick().getKickVel().getXYVector()
						.scaleToNew(incomingPass.getReceivingSpeed());
				var desiredBallVel = outgoingKick.getKickVel().getXYVector();
				return RedirectConsultantFactory.createDefault().getTargetAngle(incomingBallVel, desiredBallVel);
			} else
			{
				return incomingPass.getKick().getKickVel().getXYVector().multiplyNew(-1).getAngle();
			}
		}


		private void draw()
		{
			var incomingPassLine = Lines
					.segmentFromPoints(incomingPass.getKick().getSource(), incomingPass.getKick().getTarget());
			getAiFrame().getShapeMap().get(EAiShapesLayer.OFFENSIVE_PASSING)
					.add(new DrawableArrow(incomingPassLine.getPathStart(), incomingPassLine.directionVector(), Color.WHITE));

			if (outgoingKick != null)
			{
				var outgoingKickLine = Lines.segmentFromPoints(
						outgoingKick.getSource(),
						outgoingKick.getTarget());
				getAiFrame().getShapeMap().get(EAiShapesLayer.OFFENSIVE_PASSING)
						.add(new DrawableArrow(outgoingKickLine.getPathStart(), outgoingKickLine.directionVector(), Color.WHITE));
			}
		}
	}
}
