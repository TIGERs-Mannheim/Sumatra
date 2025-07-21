/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra;

import edu.tigers.base.BaseApp;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ai.metis.statistics.MatchStatisticsCalc;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.presenter.MainPresenter;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.sim.net.SimNetClient;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.runAsync;


/**
 * <pre>
 *          __  _-==-=_,-.
 *         /--`' \_@-@.--<
 *         `--'\ \   <___/.                 The wonderful thing about Tiggers,
 *             \ \\   " /                   is Tiggers are wonderful things.
 *               >=\\_/`<                   Their tops are made out of rubber,
 *   ____       /= |  \_/                   their bottoms are made out of springs.
 * _'    `\   _/=== \__/                    They're bouncy, trouncy, flouncy, pouncy,
 * `___/ //\./=/~\====\                     Fun, fun, fun, fun, fun.
 *     \   // /   | ===:                    But the most wonderful thing about Tiggers is,
 *      |  ._/_,__|_ ==:        __          I'm the only one.
 *       \/    \\ \\`--|       / \\
 *        |    _     \\:      /==:-\
 *        `.__' `-____/       |--|==:
 *           \    \ ===\      :==:`-'
 *           _>    \ ===\    /==/
 *          /==\   |  ===\__/--/
 *         <=== \  /  ====\ \\/
 *         _`--  \/  === \/--'
 *        |       \ ==== |
 *         -`------/`--' /
 *                 \___-'
 * </pre>
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 *
 * @author bernhard
 */
@SuppressWarnings("squid:S1147") // calling System.exit() is ok in this entry class
@Log4j2
public final class Sumatra extends BaseApp implements Runnable
{
	@Setter(onMethod_ = @Option(
			names = { "-hl", "--headless" },
			defaultValue = "${env:SUMATRA_HEADLESS:-false}",
			description = "run without a UI"
	))
	private boolean headless = false;

	@Setter(onMethod_ = @Option(
			names = { "-m", "--moduli" },
			defaultValue = "${env:SUMATRA_MODULI}",
			description = "moduli config to load by default")
	)
	private String moduli;

	@Setter(onMethod_ = @Option(
			names = { "-w", "--waitForAis" },
			defaultValue = "${env:SUMATRA_WAIT_FOR_AIS}",
			description = "wait for all AIs to connect before simulation is started")
	)
	private Boolean waitForAis;

	@Setter(onMethod_ = @Option(
			names = { "-ms", "--maxSpeed" },
			defaultValue = "${env:SUMATRA_MAX_SPEED}",
			description = "run simulation with maximum speed (not in real time)")
	)
	private Boolean maxSpeed;

	@Setter(onMethod_ = @Option(
			names = { "-ay", "--aiYellow" },
			defaultValue = "${env:SUMATRA_AI_YELLOW}",
			description = "activate yellow AI")
	)
	private Boolean aiYellow;

	@Setter(onMethod_ = @Option(
			names = { "-ab", "--aiBlue" },
			defaultValue = "${env:SUMATRA_AI_BLUE}",
			description = "activate blue AI")
	)
	private Boolean aiBlue;

	@Setter(onMethod_ = @Option(
			names = { "-ar", "--autoRef" },
			defaultValue = "${env:SUMATRA_AUTOREF:-false}",
			description = "activate autoRef in active mode")
	)
	private boolean autoRef;

	@Setter(onMethod_ = @Option(
			names = { "-c", "--initialCommand" },
			defaultValue = "${env:SUMATRA_INITIAL_COMMAND}",
			description = "initial referee command to send")
	)
	private Command initialCommand;

	@Setter(onMethod_ = @Option(
			names = { "--stopAtStageTime" },
			defaultValue = "${env:SUMATRA_STOP_AT_STAGE_TIME}",
			description = "Stop the game when the stage time [s] is reached (can be negative, zero is one half time)")
	)
	private Integer stopAtStageTime;

	@Setter(onMethod_ = @Option(
			names = { "--stopAtStage" },
			defaultValue = "${env:SUMATRA_STOP_AT_STAGE}",
			description = "Stop the game when this stage is reached")
	)
	private SslGcRefereeMessage.Referee.Stage stopAtStage;

	@Setter(onMethod_ = @Option(
			names = { "-ho", "--host" },
			defaultValue = "${env:SUMATRA_HOST}",
			description = "the host of the simulator to connect to")
	)
	private String host;

	@Setter(onMethod_ = @Option(
			names = { "-to", "--timeout" },
			defaultValue = "${env:SUMATRA_TIMEOUT}",
			description = "the timeout [s] after which the application will be exited")
	)
	private Double timeout;

	@Setter(onMethod_ = @Option(
			names = { "-tm", "--tournamentMode" },
			defaultValue = "${env:SUMATRA_TOURNAMENT_MODE}",
			description = "run in tournament mode")
	)
	private Boolean tournamentMode;

	@Setter(onMethod_ = @Option(
			names = { "-va", "--visionAddress" },
			defaultValue = "${env:SUMATRA_VISION_ADDRESS}",
			description = "address:port for vision")
	)
	private String visionAddress;

	@Setter(onMethod_ = @Option(
			names = { "-ra", "--refereeAddress" },
			defaultValue = "${env:SUMATRA_REFEREE_ADDRESS}",
			description = "address:port for GC")
	)
	private String refereeAddress;

	@Setter(onMethod_ = @Option(
			names = { "-ta", "--trackerAddress" },
			defaultValue = "${env:SUMATRA_TRACKER_ADDRESS}",
			description = "address:port for tracker")
	)
	private String trackerAddress;

	@Setter(onMethod_ = @Option(
			names = { "--matchStats" },
			defaultValue = "${env:SUMATRA_MATCH_STATS}",
			description = "enable match stats calc")
	)
	private Boolean matchStats;


	public static void main(final String[] args)
	{
		new picocli.CommandLine(new Sumatra()).execute(args);
	}


	@Override
	public void run()
	{
		log.info("Starting Sumatra {}", SumatraModel.getVersion());

		// Start the UI in a separate thread first
		runIf(!headless, this::startUi);

		// Set static parameters that can and must be set before any module is initialized
		ifNotNull(moduli, this::updateModuliConfig);
		ifNotNull(waitForAis, SumatraSimulator::setWaitForRemoteAis);
		ifNotNull(tournamentMode, this::enableTournamentMode);
		ifNotNull(host, SimNetClient::setStartupHost);
		ifNotNull(visionAddress, this::updateVisionAddress);
		ifNotNull(refereeAddress, this::updateRefereeAddress);
		ifNotNull(trackerAddress, this::updateTrackerAddress);
		ifNotNull(matchStats, MatchStatisticsCalc::setEnabled);
		SimNetClient.setStartupTeamColors(aiColors());

		loadModules();

		ifNotNull(timeout, this::enableTimeout);
		ifNotNull(maxSpeed, SimulationHelper::setSimulateWithMaxSpeed);
		ifNotNull(stopAtStage, this::enableStopAtStage);
		ifNotNull(stopAtStageTime, this::enableStopAtStageTime);

		start();

		ifNotNull(initialCommand, this::setInitialRefereeCommand);
		runIf(autoRef, this::activateAutoRef);
		activateAis();

		log.trace("Started Sumatra");
	}


	private void startUi()
	{
		CompletableFuture.runAsync(MainPresenter::new).exceptionally(e -> {
			log.error("Failed to start UI", e);
			return null;
		});
	}


	private void enableStopAtStage(SslGcRefereeMessage.Referee.Stage stage)
	{
		log.info("Stopping match at stage {}", stage);
		SumatraModel.getInstance().getModuleOpt(Referee.class)
				.ifPresent(r -> r.addObserver(new MatchStageWatcher(stage)));
	}


	private void enableStopAtStageTime(int stageTime)
	{
		log.info("Stopping match at stage time {}", stageTime);
		SumatraModel.getInstance().getModuleOpt(Referee.class)
				.ifPresent(r -> r.addObserver(new MatchStageTimeWatcher(stageTime)));
	}


	private void enableTimeout(double t)
	{
		log.info("Starting timeout of {} s", t);
		new Thread(() -> timeout(t)).start();
	}


	private void enableTournamentMode(boolean tournamentMode)
	{
		log.info("Setting tournament mode to {}", tournamentMode);
		SumatraModel.getInstance().setTournamentMode(tournamentMode);
	}


	private void timeout(double timeout)
	{
		Thread.currentThread().setName("TimeoutHandler");
		try
		{
			Thread.sleep((long) (timeout * 1000));
			log.info("Timed out after {} s", () -> String.format("%.1f", timeout));
			runAsync(() -> System.exit(0));
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}


	private void setInitialRefereeCommand(Command command)
	{
		log.info("Sending initial referee command: {}", command);
		SumatraModel.getInstance().getModuleOpt(Referee.class)
				.ifPresent(r -> r.sendGameControllerEvent(GcEventFactory.command(command)));
	}


	private void updateModuliConfig(String moduli)
	{
		log.info("Setting moduli config to {}", moduli);
		SumatraModel.getInstance().setCurrentModuliConfig(getModuliConfig(moduli));
	}


	private static String getModuliConfig(final String name)
	{
		if (!name.endsWith(".xml"))
		{
			return name + ".xml";
		}
		return name;
	}


	private List<ETeamColor> aiColors()
	{
		List<ETeamColor> teamColors = new ArrayList<>();
		if (aiYellow != null && aiYellow)
		{
			teamColors.add(ETeamColor.YELLOW);
		}
		if (aiBlue != null && aiBlue)
		{
			teamColors.add(ETeamColor.BLUE);
		}
		return teamColors;
	}


	private void activateAis()
	{
		if (aiBlue == null && aiYellow == null)
		{
			return;
		}

		if (aiYellow != null && aiYellow)
		{
			log.info("Activating yellow AI");
			SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		} else
		{
			SumatraModel.getInstance().getModuleOpt(AAgent.class)
					.ifPresent(a -> a.changeMode(EAiTeam.YELLOW, EAIControlState.OFF));
		}
		if (aiBlue != null && aiBlue)
		{
			log.info("Activating blue AI");
			SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
		} else
		{
			SumatraModel.getInstance().getModuleOpt(AAgent.class)
					.ifPresent(a -> a.changeMode(EAiTeam.BLUE, EAIControlState.OFF));
		}
	}


	@RequiredArgsConstructor
	private static class MatchStageTimeWatcher implements IRefereeObserver
	{
		private final int stageTime;


		@Override
		public void onNewRefereeMsg(final edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee refMsg)
		{
			if (refMsg.getStageTimeLeft() / 1e6 < stageTime)
			{
				log.info("Match reached stage time {}. Shutting down", stageTime);
				SumatraModel.getInstance().getModule(Referee.class).removeObserver(this);
				runAsync(() -> System.exit(0));
			}
		}
	}


	@RequiredArgsConstructor
	private static class MatchStageWatcher implements IRefereeObserver
	{
		private final SslGcRefereeMessage.Referee.Stage stage;


		@Override
		public void onNewRefereeMsg(final edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee refMsg)
		{
			if (refMsg.getStage() == stage)
			{
				log.info("Match reached stage {}. Shutting down", stage);
				SumatraModel.getInstance().getModule(Referee.class).removeObserver(this);
				runAsync(() -> System.exit(0));
			}
		}
	}
}
