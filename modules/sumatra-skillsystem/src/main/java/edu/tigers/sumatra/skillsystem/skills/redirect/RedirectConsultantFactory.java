/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.redirect;

import edu.tigers.sumatra.model.SumatraModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Factory for creating redirect consultants
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedirectConsultantFactory
{
	public static IRedirectConsultant createDefault()
	{
		if (SumatraModel.getInstance().isSimulation())
		{
			return new SimpleRedirectConsultant();
		}
		return new ConstantLossRedirectConsultant();
	}
}
