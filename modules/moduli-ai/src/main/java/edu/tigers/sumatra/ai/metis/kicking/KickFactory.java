/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.Setter;


public class KickFactory
{
	@Configurable(defValue = "0.0", comment = "Default aiming tolerance for goal kicks")
	private static double defaultAimingTolerance = 0.0;

	static
	{
		ConfigRegistration.registerClass("metis", PassFactory.class);
	}

	private WorldFrame worldFrame;

	@Setter
	private double aimingTolerance = defaultAimingTolerance;


	public void update(WorldFrame worldFrame)
	{
		this.worldFrame = worldFrame;
	}


	public Kick goalKick(IVector2 source, IVector2 target)
	{
		return straight(source, target, RuleConstraints.getMaxKickSpeed());
	}


	public Kick straight(IVector2 source, IVector2 target, double speed)
	{
		var kickVel = target.subtractNew(source).scaleTo(speed);
		return Kick.builder()
				.source(source)
				.target(target)
				.kickParams(KickParams.straight(speed))
				.kickVel(kickVel.getXYZVector())
				.aimingTolerance(aimingTolerance)
				.build();
	}


	public Kick chip(IVector2 source, IVector2 target, double speed)
	{
		var kickVel = worldFrame.getBall().getChipConsultant().speedToVel(
				target.subtractNew(source).getAngle(), speed
		);
		return Kick.builder()
				.source(source)
				.target(target)
				.kickParams(KickParams.chip(speed))
				.kickVel(kickVel)
				.aimingTolerance(aimingTolerance)
				.build();
	}
}
