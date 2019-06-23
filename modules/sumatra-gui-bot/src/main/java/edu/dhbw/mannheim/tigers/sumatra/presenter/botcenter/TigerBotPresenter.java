/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 2, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.IInstanceableObserver;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BcBotPingPanel.IBcBotPingPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotConfigOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.ConsolePanel.IConsolePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.KickerFirePanel.IKickerFirePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.MotorInputPanel.IMotorInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.SelectControllerPanel.ISelectControllerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.SystemMatchFeedbackPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.SystemMatchFeedbackPanel.ISystemMatchFeedbackPanelObserver;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.PingThread;
import edu.tigers.sumatra.botmanager.PingThread.IPingThreadObserver;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.botmanager.bots.PingStats;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.botmanager.bots.communication.Statistics;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPong;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerCtrlSetControllerType;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemQuery;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemQuery.EQueryType;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
import edu.tigers.sumatra.skillsystem.skills.StraightMoveSkill;
import edu.tigers.sumatra.thread.NamedThreadFactory;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class TigerBotPresenter implements IBaseStationObserver
{
	private static final Logger				log			= Logger.getLogger(TigerBotPresenter.class.getName());
	private PingThread							pingThread	= null;
	private ABot									bot			= new DummyBot();
	private final BotConfigOverviewPanel	botConfigOverviewPanel;
	private GenericSkillSystem					skillsystem;
	private ABotManager							botManager;
	private final ConfigPresenter				configPresenter;
	
	
	/**
	 * @param botConfigOverviewPanel
	 */
	public TigerBotPresenter(final BotConfigOverviewPanel botConfigOverviewPanel)
	{
		this.botConfigOverviewPanel = botConfigOverviewPanel;
		
		configPresenter = new ConfigPresenter(botConfigOverviewPanel.getConfigPanel(), bot);
		
		botConfigOverviewPanel.getBcBotPingPanel().addObserver(new BcBotPingPanelObserver());
		botConfigOverviewPanel.getBcBotKickerPanel().getKickerFirePanel().addObserver(new KickerFirePanelObserver());
		botConfigOverviewPanel.getBcBotControllerCfgPanel().getSelectControllerPanel()
				.addObserver(new SelectControllerPanelObserver());
		botConfigOverviewPanel.getMovePanel().getInputPanel().addObserver(new MotorInputPanelObserver());
		botConfigOverviewPanel.getMovePanel().getEnhancedInputPanel().addObserver(new MotorInputPanelObserver());
		botConfigOverviewPanel.getSkillsPanel().addObserver(new SkillsPanelObserver());
		botConfigOverviewPanel.getConsolePanel().addObserver(new ConsolePanelObserver());
		botConfigOverviewPanel.getCommandPanel().addObserver(new NewCommandObserver());
		botConfigOverviewPanel.getSystemStatusPanel().addObserver(new SystemMatchFeedbackObserver());
		
		// schedule GUI updates every 1s
		Timer timer = new Timer(1000, new SwingUpdateTimer());
		timer.start();
	}
	
	
	/**
	 * @return the bot
	 */
	public IBot getBot()
	{
		return bot;
	}
	
	
	/**
	 * @param bot the selectedBotId to set
	 */
	public void updateSelectedBotId(final ABot bot)
	{
		this.bot.getBaseStation().removeObserver(this);
		this.bot = bot;
		this.bot.getBaseStation().addObserver(this);
		configPresenter.setBot(bot);
	}
	
	
	/**
	 * @param skillsystem the skillsystem to set
	 */
	public void setSkillsystem(final GenericSkillSystem skillsystem)
	{
		this.skillsystem = skillsystem;
	}
	
	
	/**
	 * @param botManager the botManager to set
	 */
	public void setBotManager(final ABotManager botManager)
	{
		this.botManager = botManager;
		configPresenter.setBotManager(botManager);
	}
	
	
	/**
	 * @param cmd
	 */
	private void processCommand(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_SYSTEM_CONSOLE_PRINT:
				TigerSystemConsolePrint print = (TigerSystemConsolePrint) cmd;
				botConfigOverviewPanel.getConsolePanel().addConsolePrint(print);
				break;
			case CMD_SYSTEM_PONG:
			{
				final TigerSystemPong pong = (TigerSystemPong) cmd;
				
				if (pingThread != null)
				{
					pingThread.pongArrived(pong.getId());
					if (!pong.payloadValid())
					{
						log.warn("Invalid payload received: " + Arrays.toString(pong.getPayload()));
					}
				}
			}
				break;
			case CMD_SYSTEM_MATCH_FEEDBACK:
			{
				final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) cmd;
				SystemMatchFeedbackPanel statusPanel = botConfigOverviewPanel.getSystemStatusPanel();
				statusPanel.addTigerSystemMatchFeedback(feedback);
			}
				break;
			case CMD_SYSTEM_PERFORMANCE:
			{
				final TigerSystemPerformance perf = (TigerSystemPerformance) cmd;
				SystemMatchFeedbackPanel statusPanel = botConfigOverviewPanel.getSystemStatusPanel();
				statusPanel.addPerformance(perf);
			}
				break;
			case CMD_CTRL_SET_CONTROLLER_TYPE:
				final TigerCtrlSetControllerType ctrlTypeCmd = (TigerCtrlSetControllerType) cmd;
				botConfigOverviewPanel.getBcBotControllerCfgPanel().getSelectControllerPanel()
						.setControllerType(ctrlTypeCmd.getControllerType());
				break;
			case CMD_CONFIG_FILE_STRUCTURE:
			case CMD_CONFIG_ITEM_DESC:
			case CMD_CONFIG_READ:
				configPresenter.onNewCommand(cmd);
				break;
			default:
				break;
		}
	}
	
	private class BcBotPingPanelObserver implements IBcBotPingPanelObserver
	{
		private ScheduledExecutorService pingService = null;
		
		
		@Override
		public void onStartPing(final int numPings, final int payloadSize)
		{
			onStopPing();
			
			pingThread = new PingThread(payloadSize, bot);
			pingThread.addObserver(new PingThreadObserver());
			pingService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Ping Executor"));
			pingService.scheduleAtFixedRate(pingThread, 0, 1000000000 / numPings, TimeUnit.NANOSECONDS);
		}
		
		
		@Override
		public void onStopPing()
		{
			if (pingService == null)
			{
				return;
			}
			
			if (pingThread != null)
			{
				pingThread.clearObservers();
			}
			
			pingService.shutdownNow();
			pingService = null;
			pingThread = null;
		}
	}
	
	private class PingThreadObserver implements IPingThreadObserver
	{
		@Override
		public void onNewPingStats(final PingStats stats)
		{
			botConfigOverviewPanel.getBcBotPingPanel().setPingStats(stats);
		}
	}
	
	
	private class ConsolePanelObserver implements IConsolePanelObserver
	{
		@Override
		public void onConsoleCommand(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			bot.execute(consoleCmd);
		}
		
		
		@Override
		public void onConsoleCommand2All(final String cmd, final ConsoleCommandTarget target)
		{
			TigerSystemConsoleCommand consoleCmd = new TigerSystemConsoleCommand();
			consoleCmd.setTarget(target);
			consoleCmd.setText(cmd);
			for (ABot bot : botManager.getAllBots().values())
			{
				bot.execute(consoleCmd);
			}
		}
	}
	
	private class SelectControllerPanelObserver implements ISelectControllerPanelObserver
	{
		@Override
		public void onNewControllerSelected(final EControllerType controllerType)
		{
			bot.execute(new TigerCtrlSetControllerType(controllerType));
		}
		
		
		@Override
		public void onQuery(final EQueryType queryType)
		{
			bot.execute(new TigerSystemQuery(queryType));
		}
	}
	
	/**
	 * This is called regularly and directly on the Swing event dispatch thread (no new thread created, no invokelater
	 * needed)
	 */
	private class SwingUpdateTimer implements ActionListener
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (bot == null)
			{
				return;
			}
			final Statistics txStats = bot.getTxStats();
			botConfigOverviewPanel.getBcBotNetStatsPanel().setTxStat(txStats.substract(lastTxStats));
			lastTxStats = new Statistics(txStats);
			
			final Statistics rxStats = bot.getRxStats();
			botConfigOverviewPanel.getBcBotNetStatsPanel().setRxStat(rxStats.substract(lastRxStats));
			lastRxStats = new Statistics(rxStats);
		}
	}
	
	private class MotorInputPanelObserver implements IMotorInputPanelObserver
	{
		@Override
		public void onSetSpeed(final double x, final double y, final double w)
		{
			bot.getMatchCtrl().setSkill(new BotSkillLocalVelocity(new Vector2(x, y), w, getBot().getMoveConstraints()));
			bot.sendMatchCommand();
		}
	}
	
	private class SkillsPanelObserver implements ISkillsPanelObserver
	{
		
		@Override
		public void onMoveToXY(final double x, final double y)
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(new Vector2(x, y));
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onRotateAndMoveToXY(final double x, final double y, final double angle)
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(new Vector2(x, y));
			skill.getMoveCon().updateTargetAngle(angle);
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onStraightMove(final int distance, final double angle)
		{
			skillsystem.execute(bot.getBotId(), new StraightMoveSkill(distance, angle));
		}
		
		
		@Override
		public void onLookAt(final Vector2 lookAtTarget)
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateLookAtTarget(lookAtTarget);
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onDribble(final int rpm)
		{
			bot.getMatchCtrl().setDribblerSpeed(rpm);
		}
		
		
		@Override
		public void onSkill(final ASkill skill)
		{
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onBotSkill(final ABotSkill skill)
		{
			BotSkillWrapperSkill wrapperSkill = new BotSkillWrapperSkill(skill);
			skillsystem.execute(bot.getBotId(), wrapperSkill);
		}
	}
	
	private class NewCommandObserver implements IInstanceableObserver
	{
		@Override
		public void onNewInstance(final Object object)
		{
			bot.execute((ACommand) object);
		}
	}
	
	private class SystemMatchFeedbackObserver implements ISystemMatchFeedbackPanelObserver
	{
		BotWatcher botWatcher = null;
		
		
		@Override
		public void onCapture(final boolean enable)
		{
			if (enable)
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
				}
				botWatcher = new BotWatcher((TigerBotV3) bot);
				botWatcher.start();
			} else
			{
				if (botWatcher != null)
				{
					botWatcher.stop();
					botWatcher = null;
				}
			}
			
		}
	}
	
	private class KickerFirePanelObserver implements IKickerFirePanelObserver
	{
		
		@Override
		public void onKickerFire(final double kickSpeed, final EKickerMode mode, final EKickerDevice device)
		{
			bot.getMatchCtrl().setKick(kickSpeed, device, mode);
		}
	}
	
	
	@Override
	public void onIncommingBotCommand(final BotID id, final ACommand command)
	{
		if (bot.getBotId().equals(id))
		{
			processCommand(command);
		}
	}
}
