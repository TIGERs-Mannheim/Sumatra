/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.vector.IVector2;


/**
 * Factory for creating redirect consultants
 */
public final class RedirectConsultantFactory
{
	@Configurable(spezis = { "", "SUMATRA" }, defValueSpezis = { "POLY_CORRECTION", "SIMPLE" })
	private static ERedirectConsultant defaultRedirectConsultant = ERedirectConsultant.SIMPLE;
	
	static
	{
		ConfigRegistration.registerClass("skills", RedirectConsultantFactory.class);
	}
	
	
	private RedirectConsultantFactory()
	{
	}
	
	private enum ERedirectConsultant
	{
		SIMPLE,
		POLY_CORRECTION
	}
	
	
	public static void init()
	{
		// nothing to do, only for class loading
	}
	
	
	public static ARedirectConsultant createDefault(
			final IVector2 incomingBallVel,
			final IVector2 desiredOutgoingBallVel)
	{
		switch (defaultRedirectConsultant)
		{
			case SIMPLE:
				return new SimpleRedirectConsultant(incomingBallVel, desiredOutgoingBallVel);
			case POLY_CORRECTION:
				return new PolyCorrectionRedirectConsultant(incomingBallVel, desiredOutgoingBallVel);
			default:
				throw new IllegalStateException("unknown type: " + defaultRedirectConsultant);
		}
	}
	
}
