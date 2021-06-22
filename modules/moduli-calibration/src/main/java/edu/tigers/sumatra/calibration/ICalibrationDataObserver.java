/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration;


public interface ICalibrationDataObserver
{
	void onNewCalibrationData(CalibrationDataSample data);

	default void onNoData()
	{
	}
}
