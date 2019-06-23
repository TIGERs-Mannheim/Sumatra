/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BcBotPingPanel.IBcBotPingPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.BotConfigOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.ConsolePanel.IConsolePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.KickerFirePanel.IKickerFirePanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.MotorInputPanel.IMotorInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots.SystemMatchFeedbackPanel.ISystemMatchFeedbackPanelObserver;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.BotWatcher;
import edu.tigers.sumatra.botmanager.PingThread;
import edu.tigers.sumatra.botmanager.PingThread.IPingThreadObserver;
import edu.tigers.sumatra.botmanager.basestation.IBaseStationObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.DummyBot;
import edu.tigers.sumatra.botmanager.bots.PingStats;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.AMoveBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.commands.tiger.TigerSystemPong;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerSystemPerformance;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ASkill;
import edu.tigers.sumatra.skillsystem.skills.BotSkillWrapperSkill;
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
		
		botConfigOverviewPanel.getManualControlPanel().getPingPanel().addObserver(new BcBotPingPanelObserver());
		botConfigOverviewPanel.getManualControlPanel().getKickerFirePanel().addObserver(new KickerFirePanelObserver());
		botConfigOverviewPanel.getManualControlPanel().getInputPanel().addObserver(new MotorInputPanelObserver());
		botConfigOverviewPanel.getManualControlPanel().getEnhancedInputPanel().addObserver(new MotorInputPanelObserver());
		botConfigOverviewPanel.getSkillsPanel().addObserver(new SkillsPanelObserver());
		botConfigOverviewPanel.getConsolePanel().addObserver(new ConsolePanelObserver());
		botConfigOverviewPanel.getSystemStatusPanel().addObserver(new SystemMatchFeedbackObserver());
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
	
	
	private void processPong(final TigerSystemPong pong)
	{
		if (pingThread != null)
		{
			pingThread.pongArrived(pong.getId());
			if (!pong.payloadValid())
			{
				log.warn("Invalid payload received: " + Arrays.toString(pong.getPayload()));
			}
		}
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
				processPong((TigerSystemPong) cmd);
				break;
			case CMD_SYSTEM_MATCH_FEEDBACK:
				procCmdSystemMatchFeedback(cmd);
				break;
			case CMD_SYSTEM_PERFORMANCE:
				final TigerSystemPerformance perf = (TigerSystemPerformance) cmd;
				botConfigOverviewPanel.getSystemStatusPanel().addPerformance(perf);
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
	
	
	private void procCmdSystemMatchFeedback(final ACommand cmd)
	{
		final TigerSystemMatchFeedback feedback = (TigerSystemMatchFeedback) cmd;
		botConfigOverviewPanel.getSystemStatusPanel().addTigerSystemMatchFeedback(feedback);
		botConfigOverviewPanel.getManualControlPanel().getEnhancedInputPanel()
				.setLatestVelocity(feedback.getVelocity());
		botConfigOverviewPanel.getManualControlPanel().getEnhancedInputPanel()
				.setLatestAngularVelocity(feedback.getAngularVelocity());
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
			
			pingService.shutdown();
			pingService = null;
			pingThread = null;
		}
	}
	
	private class PingThreadObserver implements IPingThreadObserver
	{
		@Override
		public void onNewPingStats(final PingStats stats)
		{
			botConfigOverviewPanel.getManualControlPanel().getPingPanel().setPingStats(stats);
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
			for (ABot aBot : botManager.getAllBots().values())
			{
				aBot.execute(consoleCmd);
			}
		}
	}
	
	private class MotorInputPanelObserver implements IMotorInputPanelObserver
	{
		@Override
		public void onSetSpeed(final double x, final double y, final double w)
		{
			KickerDribblerCommands basicKickerDribblerOutput = bot.getMatchCtrl().getSkill().getKickerDribbler();
			AMoveBotSkill skill = new BotSkillLocalVelocity(Vector2.fromXY(x, y), w,
					new MoveConstraints(getBot().getBotParams().getMovementLimits()));
			skill.setKickerDribbler(basicKickerDribblerOutput);
			bot.getMatchCtrl().setSkill(skill);
			bot.sendMatchCommand();
		}
	}
	
	private class SkillsPanelObserver implements ISkillsPanelObserver
	{
		
		@Override
		public void onMoveToXY(final double x, final double y)
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(Vector2.fromXY(x, y));
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onRotateAndMoveToXY(final double x, final double y, final double angle)
		{
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			skill.getMoveCon().updateDestination(Vector2.fromXY(x, y));
			skill.getMoveCon().updateTargetAngle(angle);
			skillsystem.execute(bot.getBotId(), skill);
		}
		
		
		@Override
		public void onStraightMove(final int distance, final double angle)
		{
			if (bot.getSensoryPos().isPresent())
			{
				IVector3 pose = bot.getSensoryPos().get();
				IVector2 dest = pose.getXYVector().addNew(Vector2.fromAngle(pose.z() + angle).scaleTo(distance));
				onMoveToXY(dest.x(), dest.y());
			} else
			{
				log.error("Not supported without bot feedback anymore.");
			}
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
			switch (bot.getMatchCtrl().getSkill().getType())
			{
				case GLOBAL_POSITION:
				case GLOBAL_VELOCITY:
				case GLOBAL_VEL_XY_POS_W:
				case LOCAL_VELOCITY:
				case WHEEL_VELOCITY:
					AMoveBotSkill mSkill = (AMoveBotSkill) bot.getMatchCtrl().getSkill();
					mSkill.getKickerDribbler().setDribblerSpeed(rpm);
					break;
				case BOT_SKILL_SINE:
				case MOTORS_OFF:
				default:
					break;
			}
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
				botWatcher = new BotWatcher(bot);
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
			switch (bot.getMatchCtrl().getSkill().getType())
			{
				case GLOBAL_POSITION:
				case GLOBAL_VELOCITY:
				case LOCAL_VELOCITY:
				case GLOBAL_VEL_XY_POS_W:
				case WHEEL_VELOCITY:
					AMoveBotSkill mSkill = (AMoveBotSkill) bot.getMatchCtrl().getSkill();
					mSkill.getKickerDribbler().setKick(kickSpeed, device, mode);
					break;
				case BOT_SKILL_SINE:
				case MOTORS_OFF:
				default:
					break;
			}
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
