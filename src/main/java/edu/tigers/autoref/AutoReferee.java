/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import edu.tigers.autoref.presenter.AutoRefMainPresenter;
import edu.tigers.base.BaseApp;
import edu.tigers.sumatra.model.SumatraModel;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;

import java.util.concurrent.CompletableFuture;


/**
 * Main class for auto referee.
 */
@Log4j2
public final class AutoReferee extends BaseApp implements Runnable
{
	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-hl", "--headless" },
			defaultValue = "${env:AUTOREF_HEADLESS:-false}",
			description = "run without a UI"
	))
	private boolean headless = false;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-va", "--visionAddress" },
			defaultValue = "${env:AUTOREF_VISION_ADDRESS}",
			description = "address:port for vision")
	)
	private String visionAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-ra", "--refereeAddress" },
			defaultValue = "${env:AUTOREF_REFEREE_ADDRESS}",
			description = "address:port for GC")
	)
	private String refereeAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-ta", "--trackerAddress" },
			defaultValue = "${env:AUTOREF_TRACKER_ADDRESS}",
			description = "address:port for tracker")
	)
	private String trackerAddress;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-a", "--active" },
			defaultValue = "${env:AUTOREF_ACTIVE:-false}",
			description = "activate autoRef in active mode")
	)
	private boolean autoRef;

	@Setter(onMethod_ = @CommandLine.Option(
			names = { "-c", "--ci" },
			defaultValue = "${env:AUTOREF_CI:-false}",
			description = "use CI mode")
	)
	private boolean ciMode;


	public static void main(final String[] args)
	{
		new CommandLine(new AutoReferee()).execute(args);
	}


	@Override
	public void run()
	{
		log.info("Starting AutoReferee {}", SumatraModel.getVersion());

		// Start the UI in a separate thread first
		runIf(!headless, this::startUi);

		ifNotNull(visionAddress, this::updateVisionAddress);
		ifNotNull(refereeAddress, this::updateRefereeAddress);
		ifNotNull(trackerAddress, this::updateTrackerAddress);

		loadModules();
		start();

		runIf(autoRef, this::activateAutoRef);

		log.trace("Started AutoReferee");
	}


	private void startUi()
	{
		CompletableFuture.runAsync(AutoRefMainPresenter::new);
	}


	@Override
	protected void loadModules()
	{
		String config = ciMode ? "autoreferee-ci.xml" : "autoreferee.xml";
		SumatraModel.getInstance().setCurrentModuliConfig(config);
		super.loadModules();
	}
}
