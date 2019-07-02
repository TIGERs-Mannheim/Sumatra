/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.log.JULLoggingBridge;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SumatraSimulator;
import edu.tigers.sumatra.sim.net.SimNetClient;


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
 *
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 *
 * @author bernhard
 */
@SuppressWarnings("squid:S1147") // calling System.exit() is ok in this entry class
public final class Sumatra
{
	private static final Logger LOG;
	private static CommandLine cmd;


	private Sumatra()
	{
	}


	static
	{
		// Connect java.util.logging (for jinput)
		JULLoggingBridge.install();
		LOG = Logger.getLogger(Sumatra.class);
	}


	/**
	 * Creates the model of the application and redirects to a presenter.
	 *
	 * @param args
	 */
	public static void main(final String[] args)
	{
		Options options = createOptions();
		cmd = parseOptions(args, options, new DefaultParser());

		ifHasOption("h", () -> printHelp(options));
		ifHasOption("m", Sumatra::setModule);
		ifHasOption("w", () -> SumatraSimulator.setWaitForRemoteAis(true));
		ifHasOption("ho", () -> SimNetClient.setStartupHost(cmd.getOptionValue("ho")));
		ifHasOption("hl", () -> SumatraModel.changeLogLevel(Level.INFO));
		ifNotHasOption("hl", MainPresenter::new);
		ifHasOption("s", () -> start(cmd));
		ifHasOption("ms", () -> SimulationHelper.setSimulateWithMaxSpeed(true));
		ifHasOption("ar", Sumatra::activateAutoRef);
		ifHasOption("c", () -> setInitialRefereeCommand(cmd));
		ifHasOption("pt", () -> limitMatchDuration(cmd));
		ifHasOption("to", () -> setTimeout(cmd));
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
		options.addOption("s", "start", false, "start modules of current moduli config immediately");
		options.addOption("ms", "maxSpeed", false, "run simulation with maximum speed (not in real time)");
		options.addOption("hl", "headless", false, "run without a UI");
		options.addOption("ay", "aiYellow", false, "activate yellow AI. Requires -s");
		options.addOption("ab", "aiBlue", false, "activate blue AI. Requires -s");
		options.addOption("ar", "autoRef", false, "activate autoRef in active mode");
		options.addOption("c", "initialCommand", true, "initial referee command to send");
		options.addOption("pt", "playingTime", true, "duration [s] of time to play (match time)");
		options.addOption("ho", "host", true, "the host of the simulator to connect to");
		options.addOption("to", "timeout", true, "the timeout [s] after which the application will be exited");
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
		double duration = Double.parseDouble(cmd.getOptionValue("pt"));
		SumatraModel.getInstance().getModuleOpt(Referee.class)
				.ifPresent(r -> r.addObserver(new MatchDurationLimiter(duration)));
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
			LOG.info(String.format("Timed out after %.1fs.", timeout));
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
			LOG.error("Could not parse command: " + cmd.getOptionValue("c") + ". It should be one of: "
					+ Arrays.toString(Command.values()));
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
			LOG.error("Could not start Sumatra.", e);
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
			LOG.error("Could not parse options.", e);
			printHelp(options);
		}
		return null;
	}

	private static class MatchDurationLimiter implements IRefereeObserver
	{
		final double duration;


		public MatchDurationLimiter(final double duration)
		{
			this.duration = duration;
		}


		@Override
		public void onNewRefereeMsg(final edu.tigers.sumatra.Referee.SSL_Referee refMsg)
		{
			double stageDuration = 5.0 * 60;
			double stageTime = stageDuration - refMsg.getStageTimeLeft() / 1e6;
			if (stageTime > duration)
			{
				LOG.info("Match reached the maximum desired duration: " + stageTime);
				new Thread(Sumatra::tearDown).start();
			}
		}
	}
}
