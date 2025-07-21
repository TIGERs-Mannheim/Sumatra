/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker.states;


import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.RotationSkill;

import java.awt.Color;


public class FreeSkirmishState extends AAttackerRoleState<RotationSkill>
{
	@Configurable(defValue = "300.0", comment = "[mm] this value is absolutely arbitrary and unimportant.")
	private static double helperTargetOffset = 300.0;

	static
	{
		ConfigRegistration.registerClass("roles", FreeSkirmishState.class);
	}

	public FreeSkirmishState(AttackerRole role)
	{
		super(RotationSkill::new, role, EAttackerState.SKIRMISH);
	}


	@Override
	protected void doStandardUpdate()
	{
		var normalOrientation = getRole().getBall().getPos().subtractNew(getRole().getPos()).getNormalVector();
		final double scale = Math.signum(normalOrientation.y()) == Math.signum(getRole().getPos().y())
						? -helperTargetOffset
						: helperTargetOffset;
		var helperPoint = getRole().getPos().addNew(normalOrientation.scaleToNew(scale));
		getRole().getShapes(EAiShapesLayer.OFFENSE_ATTACKER).add(new DrawableCircle(Circle.createCircle(helperPoint, 100)).setColor(
				Color.magenta));
		getRole().getShapes(EAiShapesLayer.OFFENSE_ATTACKER).add(new DrawableCircle(Circle.createCircle(helperPoint, 120)).setColor(
				Color.magenta));

		IVector2 ballToTarget = helperPoint.subtractNew(getRole().getWFrame().getBall().getPos());
		IVector2 meToBall = getRole().getWFrame().getBall().getPos().subtractNew(getRole().getPos());

		double angle = meToBall.angleTo(ballToTarget).orElse(0.0);
		if (angle > 0)
		{
			// right
			skill.setRotation(AngleMath.deg2rad(170));
		} else
		{
			// left
			skill.setRotation(AngleMath.deg2rad(-170));
		}
	}
}

