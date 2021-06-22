/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration.redirect;

import edu.tigers.sumatra.calibration.CalibrationDataSample;

import java.util.Optional;


public interface IRedirectDataCollector
{
	default void start()
	{
	}

	default void stop()
	{
	}

	Optional<RedirectSample> process(CalibrationDataSample sample);
}
