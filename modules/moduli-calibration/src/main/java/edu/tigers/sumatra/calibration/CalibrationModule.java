/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.calibration;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.calibration.redirect.RedirectCalibrationStream;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.wp.WorldInfoCollector;


public class CalibrationModule extends AModule
{
	private final CalibrationDataCollector calibrationDataCollector = new CalibrationDataCollector();
	private final RedirectCalibrationStream redirectCalibrationStream = new RedirectCalibrationStream();


	@Override
	public void startModule() throws StartModuleException
	{
		super.startModule();
		SumatraModel.getInstance().getModule(GenericSkillSystem.class).addObserver(calibrationDataCollector);
		SumatraModel.getInstance().getModule(WorldInfoCollector.class).addObserver(calibrationDataCollector);
		calibrationDataCollector.addObserver(redirectCalibrationStream);
	}


	@Override
	public void stopModule()
	{
		super.stopModule();
		SumatraModel.getInstance().getModule(GenericSkillSystem.class).removeObserver(calibrationDataCollector);
		SumatraModel.getInstance().getModule(WorldInfoCollector.class).removeObserver(calibrationDataCollector);
		calibrationDataCollector.onClearCamDetectionFrame();
		calibrationDataCollector.onClearWorldFrame();
		calibrationDataCollector.removeObserver(redirectCalibrationStream);
	}
}
