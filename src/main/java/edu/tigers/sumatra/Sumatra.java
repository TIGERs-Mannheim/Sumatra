/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.sim.net.SimNetClient;
import edu.tigers.sumatra.wp.exporter.VisionTrackerSender;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
public final class Sumatra
{
	private static CommandLine cmd;


	/**
	 * Creates the model of the application and redirects to a presenter.
	 *
	 * @param args
	 */
	public static void main(final String[] args)
	{
		log.info("Starting Sumatra {}", SumatraModel.getVersion());
		Options options = createOptions();
		cmd = parseOptions(args, options, new DefaultParser());

		ifHasOption("h", () -> printHelp(options));
		ifNotHasOption("hl", () -> SwingUtilities.invokeLater(MainPresenter::new));

		ifHasOption("m", Sumatra::setModule);
		ifHasOption("w", () -> SumatraSimulator.setWaitForRemoteAis(true));
		ifHasOption("ho", () -> SimNetClient.setStartupHost(cmd.getOptionValue("ho")));
		ifHasOption("p", () -> SumatraModel.getInstance().setTournamentMode(true));
		ifHasOption("va", () -> setVisionAddress(cmd.getOptionValue("va")));
		ifHasOption("ra", () -> setRefereeAddress(cmd.getOptionValue("ra")));
		ifHasOption("ta", () -> setTrackerAddress(cmd.getOptionValue("ta")));

		start(cmd);
		ifHasOption("to", () -> setTimeout(cmd));
		ifHasOption("ms", () -> SimulationHelper.setSimulateWithMaxSpeed(true));
		ifHasOption("pt", () -> limitMatchDuration(cmd));
		ifHasOption("c", () -> setInitialRefereeCommand(cmd));
		ifHasOption("ar", Sumatra::activateAutoRef);
		log.trace("Started Sumatra");
	}


	private static void setVisionAddress(String fullAddress)
	{
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


	private static void setRefereeAddress(String fullAddress)
	{
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


	private static void setTrackerAddress(String fullAddress)
	{
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


	private static void activateAutoRef()
	{
		SumatraModel.getInstance().getModuleOpt(AutoRefModule.class)
				.ifPresent(a -> a.changeMode(EAutoRefMode.ACTIVE));
	}


	private static void setModule()
	{
		SumatraModel.getInstance().setCurrentModuliConfig(getModuliConfig(cmd.getOptionValue("m")));
	}


	private static Options createOptions()
	{
		Options options = new Options();
		options.addOption("h", "help", false, "Print this help message");
		options.addOption("m", "moduli", true, "moduli config to load by default");
		options.addOption("w", "waitForAis", false, "wait for all AIs to connect before simulation is started");
		options.addOption("ms", "maxSpeed", false, "run simulation with maximum speed (not in real time)");
		options.addOption("hl", "headless", false, "run without a UI");
		options.addOption("ay", "aiYellow", false, "activate yellow AI");
		options.addOption("ab", "aiBlue", false, "activate blue AI");
		options.addOption("ar", "autoRef", false, "activate autoRef in active mode");
		options.addOption("c", "initialCommand", true, "initial referee command to send");
		options.addOption("pt", "playingTime", true, "duration [s] of time to play (match time)");
		options.addOption("ho", "host", true, "the host of the simulator to connect to");
		options.addOption("to", "timeout", true, "the timeout [s] after which the application will be exited");
		options.addOption("p", "productive", false, "run in tournament mode (aka productive mode)");
		options.addOption("va", "visionAddress", true, "address:port for vision");
		options.addOption("ra", "refereeAddress", true, "address:port for GC");
		options.addOption("ta", "trackerAddress", true, "address:port for tracker");
		return options;
	}


	private static void ifHasOption(String shortOptions, Runnable r)
	{
		if (cmd.hasOption(shortOptions))
		{
			r.run();
		}
	}


	private static void ifNotHasOption(String shortOptions, Runnable r)
	{
		if (!cmd.hasOption(shortOptions))
		{
			r.run();
		}
	}


	private static void limitMatchDuration(final CommandLine cmd)
	{
		double stageTimeLeft = Double.parseDouble(cmd.getOptionValue("pt"));
		SumatraModel.getInstance().getModuleOpt(Referee.class)
				.ifPresent(r -> r.addObserver(new MatchDurationLimiter(stageTimeLeft)));
	}


	private static void setTimeout(final CommandLine cmd)
	{
		double timeout = Double.parseDouble(cmd.getOptionValue("to"));
		new Thread(() -> timeout(timeout)).start();
	}


	private static void timeout(double timeout)
	{
		Thread.currentThread().setName("TimeoutHandler");
		try
		{
			Thread.sleep((long) (timeout * 1000));
			log.info("Timed out after {} s", () -> String.format("%.1f", timeout));
			tearDown();
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}


	private static void tearDown()
	{
		Thread.currentThread().setName("TearDown");
		SumatraModel.getInstance().stopModules();
		System.exit(0);
	}


	private static void setInitialRefereeCommand(final CommandLine cmd)
	{
		try
		{
			Command command = Command.valueOf(cmd.getOptionValue("c"));
			SumatraModel.getInstance().getModuleOpt(Referee.class)
					.ifPresent(r -> r.sendGameControllerEvent(GcEventFactory.command(command)));
		} catch (IllegalArgumentException err)
		{
			log.error("Could not parse command: {}. It should be one of: {}",
					cmd.getOptionValue("c"),
					Arrays.toString(Command.values()), err);
			System.exit(1);
		}
	}


	private static void printHelp(final Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Sumatra", options);
		System.exit(0);
	}


	private static String getModuliConfig(final String name)
	{
		if (!name.endsWith(".xml"))
		{
			return name + ".xml";
		}
		return name;
	}


	private static List<ETeamColor> aiColors(final CommandLine cmd)
	{
		List<ETeamColor> teamColors = new ArrayList<>();
		if (cmd.hasOption("ay"))
		{
			teamColors.add(ETeamColor.YELLOW);
		}
		if (cmd.hasOption("ab"))
		{
			teamColors.add(ETeamColor.BLUE);
		}
		return teamColors;
	}


	private static void start(final CommandLine cmd)
	{
		SimNetClient.setStartupTeamColors(aiColors(cmd));
		try
		{
			SumatraModel.getInstance().loadModulesOfConfig(SumatraModel.getInstance().getCurrentModuliConfig());
			SumatraModel.getInstance().startModules();
		} catch (Throwable e)
		{
			log.error("Could not start Sumatra. Setting moduli config to default. Please try again.", e);
			SumatraModel.getInstance().setCurrentModuliConfig(SumatraModel.MODULI_CONFIG_FILE_DEFAULT);
			System.exit(1);
		}
		if (cmd.hasOption("ay") || cmd.hasOption("ab"))
		{
			activateAis(cmd);
		}
	}


	private static void activateAis(final CommandLine cmd)
	{
		if (cmd.hasOption("ay"))
		{
			SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		} else
		{
			SumatraModel.getInstance().getModuleOpt(AAgent.class)
					.ifPresent(a -> a.changeMode(EAiTeam.YELLOW, EAIControlState.OFF));
		}
		if (cmd.hasOption("ab"))
		{
			SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
		} else
		{
			SumatraModel.getInstance().getModuleOpt(AAgent.class)
					.ifPresent(a -> a.changeMode(EAiTeam.BLUE, EAIControlState.OFF));
		}
	}


	private static CommandLine parseOptions(final String[] args, final Options options, final CommandLineParser parser)
	{
		try
		{
			return parser.parse(options, args);
		} catch (ParseException e)
		{
			log.error("Could not parse options.", e);
			printHelp(options);
		}
		return null;
	}


	private record MatchDurationLimiter(double desiredStageTimeLeft) implements IRefereeObserver
	{
		@Override
		public void onNewRefereeMsg(final edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee refMsg)
		{
			if (refMsg.getStage() != SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF)
			{
				return;
			}
			double stageTimeLeft = refMsg.getStageTimeLeft() / 1e6;
			if (stageTimeLeft < desiredStageTimeLeft)
			{
				log.info("Match reached the desired stageTime: {}", stageTimeLeft);
				SumatraModel.getInstance().getModule(Referee.class).removeObserver(this);
				new Thread(Sumatra::tearDown).start();
			}
		}
	}
}
