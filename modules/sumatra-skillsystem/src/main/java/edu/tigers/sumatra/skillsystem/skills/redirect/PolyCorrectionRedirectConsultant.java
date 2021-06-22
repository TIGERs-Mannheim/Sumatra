/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;

import static java.lang.Math.abs;
import static java.lang.Math.signum;


/**
 * This redirect consultant adds a correction to the target angle based on a polynomial model
 */
class PolyCorrectionRedirectConsultant implements IRedirectConsultant
{
	@Configurable(
			comment = "f(angle,vIn,vOut) = p0 + p1*angle + p2*vIn + p3*vOut",
			spezis = { "", "SIMULATOR" },
			defValueSpezis = {"0.0;0.20131;0.0;0.0", "0.0;0.37855;0.0;0.0"}
	)
	private static Double[] angleParams = new Double[] { 0.0 };

	static
	{
		ConfigRegistration.registerClass("skills", PolyCorrectionRedirectConsultant.class);
		String env = SumatraModel.getInstance().getEnvironment();
		ConfigRegistration.applySpezi("skills", env);
		ConfigRegistration.registerConfigurableCallback("skills", new IConfigObserver()
		{
			@Override
			public void afterApply(IConfigClient configClient)
			{
				String env = SumatraModel.getInstance().getEnvironment();
				ConfigRegistration.applySpezi("skills", env);
			}
		});
	}


	@Override
	public double getKickSpeed(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		return desiredOutgoingBallVel.getLength2();
	}


	@Override
	public double getTargetAngle(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		double redirectAngle = getRedirectAngle(incomingBallVel, desiredOutgoingBallVel);
		double x = abs(redirectAngle);
		double y = incomingBallVel.getLength2();
		double z = desiredOutgoingBallVel.getLength2();
		double correction = angleParams[0] + angleParams[1] * x + angleParams[2] * y + angleParams[3] * z;
		return desiredOutgoingBallVel.getAngle() - signum(redirectAngle) * correction;
	}


	private double getRedirectAngle(final IVector2 incomingBallVel, final IVector2 desiredOutgoingBallVel)
	{
		IVector2 invIncomingBallVel = incomingBallVel.multiplyNew(-1);
		return invIncomingBallVel.angleTo(desiredOutgoingBallVel).orElse(0.0);
	}
}
