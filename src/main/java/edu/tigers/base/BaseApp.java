/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.base;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.ModulesState;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.wp.exporter.VisionTrackerSender;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;

import java.util.function.Consumer;


/**
 * Main class for auto referee.
 */
@Log4j2
public class BaseApp
{
	protected <T> void ifNotNull(T value, Consumer<T> consumer)
	{
		if (value != null)
		{
			consumer.accept(value);
		}
	}


	protected void runIf(boolean condition, Runnable runnable)
	{
		if (condition)
		{
			runnable.run();
		}
	}


	private void registerShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown, "Sumatra-shutdown"));
	}


	private void onShutdown()
	{
		log.debug("Shutting down");
		if (SumatraModel.getInstance().getModulesState().get() == ModulesState.ACTIVE)
		{
			SumatraModel.getInstance().stopModules();
		}
		SumatraModel.getInstance().saveUserProperties();
		log.debug("Shut down");
		// We have disabled the shutdown hook in log4j2.xml, so we have to shut log4j down manually
		LogManager.shutdown();
	}


	protected void loadModules()
	{
		try
		{
			SumatraModel.getInstance().loadModules();
		} catch (Throwable e)
		{
			log.error("Could not start Sumatra.", e);
			System.exit(1);
		}
	}


	protected void start()
	{
		try
		{
			SumatraModel.getInstance().startModules();
			registerShutdownHook();
		} catch (Throwable e)
		{
			log.error("Could not start Sumatra.", e);
			System.exit(1);
		}
	}


	protected void updateVisionAddress(String fullAddress)
	{
		log.info("Setting custom vision address: {}", fullAddress);
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			SSLVisionCam.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			SSLVisionCam.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	protected void updateRefereeAddress(String fullAddress)
	{
		log.info("Setting custom referee address: {}", fullAddress);
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			Referee.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			Referee.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	protected void updateTrackerAddress(String fullAddress)
	{
		log.info("Setting custom tracker address: {}", fullAddress);
		String[] parts = fullAddress.split(":");
		String address = parts[0];
		if (!address.isBlank())
		{
			VisionTrackerSender.setCustomAddress(address);
		}
		if (parts.length > 1)
		{
			VisionTrackerSender.setCustomPort(Integer.parseInt(parts[1]));
		}
	}


	protected void activateAutoRef()
	{
		log.info("Activating autoRef in active mode");
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(a -> a.changeMode(EAutoRefMode.ACTIVE));
	}
}
