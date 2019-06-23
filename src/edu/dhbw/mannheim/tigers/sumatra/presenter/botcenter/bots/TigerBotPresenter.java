/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ITigerBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerIrLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerStatusV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorPidLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetParams.MotorMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorSetPidSp;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPing;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPong;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemPowerLog;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemSetLogs;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerSystemStatusMovement;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.GeneralPurposeTimer;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.FeaturePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.INetworkPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.IRPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ITigerBotMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.NetworkPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.PowerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SplinePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotMainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotSummary;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotSummary.ITigerBotSummaryObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel.IKickerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorConfigurationPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorControlPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorMainPanel;


/**
 * Presenter for a tiger bot.
 *
 * @author AndreR
 */
public class TigerBotPresenter extends ABotPresenter implements ITigerBotMainPanelObserver, ILookAndFeelStateObserver,
		IMotorMainPanelObserver, INetworkPanelObserver, IWorldPredictorObserver, IKickerPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log							= Logger.getLogger(TigerBotPresenter.class.getName());
	
	private TigerBot								bot							= null;
	
	private ABotManager							botmanager					= null;
	private AWorldPredictor						worldPredictor				= null;
	private TigerBotSummary						summary						= null;
	private KickerConfig							fastChgPanel				= null;
	private TigerBotMainPanel					mainPanel					= null;
	private SkillsPanel							skills						= null;
	private KickerPanel							kicker						= null;
	private NetworkPanel							network						= null;
	private MotorMainPanel						motorMain					= null;
	private PowerPanel							power							= null;
	private IRPanel								ir								= null;
	private FeaturePanel							features						= null;
	private SplinePanel							splinePanel					= null;
	private final MotorControlPanel			motorControls[]			= new MotorControlPanel[5];
	private final MotorConfigPresenter		motorPresenters[]			= new MotorConfigPresenter[5];
	private PingThread							pingThread					= null;
	
	// Observer Handler
	private final TigerBotObserver			tigerBotObserver			= new TigerBotObserver();
	private final TigerBotSummaryObserver	tigerBotSummaryObserver	= new TigerBotSummaryObserver();
	
	
	private long									startNewWP					= SumatraClock.nanoTime();
	private long									startMotor					= SumatraClock.nanoTime();
	// in milliseconds
	private static final long					VISUALIZATION_FREQUENCY	= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public TigerBotPresenter(final ABot bot)
	{
		super(bot);
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
			worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule("worldpredictor");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
		
		summary = new TigerBotSummary();
		fastChgPanel = new KickerConfig(bot.getBotID());
		mainPanel = new TigerBotMainPanel();
		skills = new SkillsPanel();
		kicker = new KickerPanel();
		network = new NetworkPanel();
		motorMain = new MotorMainPanel();
		power = new PowerPanel();
		features = new FeaturePanel();
		splinePanel = new SplinePanel();
		ir = new IRPanel();
		
		for (int i = 0; i < 5; i++)
		{
			motorControls[i] = new MotorControlPanel();
			motorPresenters[i] = new MotorConfigPresenter(i);
		}
		
		this.bot = (TigerBot) bot;
		
		node = new BotCenterTreeNode(bot.getName() + " (" + bot.getBotID().getNumber() + ")", ETreeIconType.BOT, bot
				.getColor().getColor(), mainPanel, true);
		
		node.add(new BotCenterTreeNode("Power", ETreeIconType.LIGHTNING, power, true));
		node.add(new BotCenterTreeNode("Network", ETreeIconType.AP, network, true));
		final BotCenterTreeNode motorsNode = new BotCenterTreeNode("Motors", ETreeIconType.MOTOR, motorMain, true);
		motorsNode.add(new BotCenterTreeNode("Front Right", ETreeIconType.GRAPH, motorControls[0], true));
		motorsNode.add(new BotCenterTreeNode("Front Left", ETreeIconType.GRAPH, motorControls[1], true));
		motorsNode.add(new BotCenterTreeNode("Rear Left ", ETreeIconType.GRAPH, motorControls[2], true));
		motorsNode.add(new BotCenterTreeNode("Rear Right", ETreeIconType.GRAPH, motorControls[3], true));
		motorsNode.add(new BotCenterTreeNode("Dribbler", ETreeIconType.GRAPH, motorControls[4], true));
		node.add(motorsNode);
		node.add(new BotCenterTreeNode("Kicker", ETreeIconType.KICK, kicker, true));
		node.add(new BotCenterTreeNode("Skills", ETreeIconType.LAMP, skills, true));
		node.add(new BotCenterTreeNode("IR", ETreeIconType.GRAPH, ir, true));
		node.add(new BotCenterTreeNode("Features", ETreeIconType.GEAR, features, true));
		node.add(new BotCenterTreeNode("Spline", ETreeIconType.SPLINE, splinePanel, true));
		
		summary.setId(bot.getBotID());
		summary.setBotName(bot.getName());
		
		tigerBotObserver.onNameChanged(bot.getName());
		tigerBotObserver.onIdChanged(BotID.createBotId(), bot.getBotID());
		tigerBotObserver.onIpChanged(this.bot.getIp());
		tigerBotObserver.onPortChanged(this.bot.getPort());
		tigerBotObserver.onNetworkStateChanged(this.bot.getNetworkState());
		tigerBotObserver.onServerPortChanged(this.bot.getServerPort());
		tigerBotObserver.onMacChanged(this.bot.getMac());
		tigerBotObserver.onCpuIdChanged(this.bot.getCpuId());
		tigerBotObserver.onUseUpdateAllChanged(this.bot.getUseUpdateAll());
		tigerBotObserver.onLogsChanged(this.bot.getLogs());
		tigerBotObserver.onMotorParamsChanged(this.bot.getMotorParams());
		tigerBotObserver.onBotFeaturesChanged(this.bot.getBotFeatures());
		
		tigerBotSummaryObserver.onConnectionTypeChange(this.bot.getUseUpdateAll());
		tigerBotSummaryObserver.onOOFCheckChange(this.bot.getOofCheck());
		
		this.bot.addObserver(tigerBotObserver);
		summary.addObserver(tigerBotSummaryObserver);
		mainPanel.addObserver(this);
		skills.addObserver(this);
		kicker.addObserver(this);
		network.addObserver(this);
		worldPredictor.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		motorMain.addObserver(this);
		motorMain.getEnhancedInputPanel().addObserver(this);
		features.addObserver(this);
		for (int i = 0; i < 5; i++)
		{
			motorControls[i].getConfigPanel().addObserver(motorPresenters[i]);
		}
		
		GeneralPurposeTimer.getInstance().scheduleAtFixedRate(new NetworkStatisticsUpdater(), 0, 1000);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void delete()
	{
		bot.removeObserver(tigerBotObserver);
		summary.removeObserver(tigerBotSummaryObserver);
		mainPanel.removeObserver(this);
		skills.removeObserver(this);
		kicker.removeObserver(this);
		network.removeObserver(this);
		worldPredictor.removeObserver(this);
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		motorMain.removeObserver(this);
		motorMain.getEnhancedInputPanel().removeObserver(this);
		
		for (int i = 0; i < 5; i++)
		{
			motorControls[i].getConfigPanel().removeObserver(motorPresenters[i]);
		}
	}
	
	
	@Override
	public ABot getBot()
	{
		return bot;
	}
	
	
	@Override
	public JPanel getSummaryPanel()
	{
		return summary;
	}
	
	
	@Override
	public JPanel getFastChgPanel()
	{
		return fastChgPanel;
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(summary);
				SwingUtilities.updateComponentTreeUI(mainPanel);
				SwingUtilities.updateComponentTreeUI(skills);
				SwingUtilities.updateComponentTreeUI(kicker);
				SwingUtilities.updateComponentTreeUI(network);
				SwingUtilities.updateComponentTreeUI(power);
				SwingUtilities.updateComponentTreeUI(motorMain);
			}
		});
	}
	
	
	@Override
	public void onConnectionChange()
	{
		connectionChange();
		botmanager.botConnectionChanged(bot);
	}
	
	
	private void connectionChange()
	{
		switch (bot.getNetworkState())
		{
			case OFFLINE:
				bot.setActive(true);
				bot.start();
				break;
			case CONNECTING:
			case ONLINE:
				bot.stop();
				bot.setActive(false);
				break;
		}
	}
	
	
	@Override
	public void onSaveGeneral()
	{
		try
		{
			if ((mainPanel.getId() != bot.getBotID().getNumber())
					|| (mainPanel.getColor() != bot.getBotID().getTeamColor()))
			{
				botmanager.changeBotId(bot.getBotID(), BotID.createBotId(mainPanel.getId(), mainPanel.getColor()));
			}
			
			bot.setName(mainPanel.getBotName());
			bot.setIP(mainPanel.getIp());
			bot.setPort(mainPanel.getPort());
			bot.setCpuId(mainPanel.getCpuId());
			bot.setMac(mainPanel.getMac());
			bot.setServerPort(mainPanel.getServerPort());
			bot.setUseUpdateAll(mainPanel.getUseUpdateAll());
			
			bot.stop();
			bot.start();
		}
		
		catch (final NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	
	
	@Override
	public void onKickerFire(final int level, final float duration, final EKickerMode mode, final int device)
	{
		final TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration, level);
		
		bot.execute(kick);
	}
	
	
	@Override
	public void onKickerChargeManual(final int duration, final int on, final int off)
	{
		if ((on < 0) || (off < 0) || (on > 60000) || (off > 60000) || (duration < 0) || (duration > 60000))
		{
			return;
		}
		
		bot.execute(new TigerKickerChargeManual(on, off, duration));
	}
	
	
	@Override
	public void onKickerChargeAuto(final int max)
	{
		bot.setKickerMaxCap(max);
		bot.execute(new TigerKickerChargeAuto(max));
	}
	
	
	@Override
	public void onStartPing(final int numPings)
	{
		onStopPing();
		
		pingThread = new PingThread(numPings);
		pingThread.start();
	}
	
	
	@Override
	public void onStopPing()
	{
		if (pingThread == null)
		{
			return;
		}
		
		pingThread.interrupt();
		pingThread = null;
	}
	
	
	@Override
	public void onSaveLogs()
	{
		final boolean moveLog = mainPanel.getLogMovement();
		final boolean kickerLog = mainPanel.getLogKicker();
		final boolean powerLog = mainPanel.getLogPower();
		final boolean irLog = mainPanel.getLogIr();
		
		bot.setLogMovement(moveLog);
		bot.setLogKicker(kickerLog);
		bot.setLogKicker(powerLog);
		bot.setLogIr(irLog);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		final TrackedTigerBot tracked = wfWrapper.getSimpleWorldFrame().getBots().getWithNull(bot.getBotID());
		if (tracked == null)
		{
			return;
		}
		if ((SumatraClock.nanoTime() - startNewWP) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			final Vector2f vel = new Vector2f(tracked.getVel().turnNew(-tracked.getAngle()));
			
			motorMain.getEnhancedInputPanel().setLatestWPData(new Vector2f(-vel.y(), vel.x()), tracked.getaVel());
			startNewWP = SumatraClock.nanoTime();
		}
	}
	
	
	@Override
	public void onSetMotorMode(final MotorMode mode)
	{
		bot.setMode(mode);
	}
	
	
	@Override
	public void onUpdateFirmware(final String filepath, final boolean targetMain)
	{
	}
	
	// -------------------------------------------------------------
	// Sub classes
	// -------------------------------------------------------------
	private class TigerBotObserver implements ITigerBotObserver
	{
		@Override
		public void onNameChanged(final String name)
		{
			summary.setBotName(name);
			mainPanel.setBotName(name);
		}
		
		
		@Override
		public void onIdChanged(final BotID oldId, final BotID newId)
		{
			summary.setId(newId);
			mainPanel.setId(newId);
			mainPanel.setColor(newId.getTeamColor());
		}
		
		
		@Override
		public void onIpChanged(final String ip)
		{
			mainPanel.setIp(ip);
		}
		
		
		@Override
		public void onPortChanged(final int port)
		{
			mainPanel.setPort(port);
		}
		
		
		@Override
		public void onUseUpdateAllChanged(final boolean useUpdateAll)
		{
			mainPanel.setUseUpdateAll(useUpdateAll);
			summary.setMulticast(useUpdateAll);
		}
		
		
		@Override
		public void onOofCheckChanged(final boolean enable)
		{
			summary.setOofCheck(enable);
		}
		
		
		@Override
		public void onIncommingCommand(final ACommand cmd)
		{
		}
		
		
		@Override
		public void onOutgoingCommand(final ACommand cmd)
		{
		}
		
		
		@Override
		public void onNetworkStateChanged(final ENetworkState state)
		{
			summary.setNetworkState(state);
			mainPanel.setConnectionState(state);
		}
		
		
		@Override
		public void onNewKickerStatusV2(final TigerKickerStatusV2 status)
		{
			kicker.getStatusPanel().setChg(status.getChargeCurrent());
			kicker.getStatusPanel().setCap(status.getCapLevel());
			kicker.getStatusPanel().setTDiode(status.getTDiode());
			kicker.getStatusPanel().setTIGBT(status.getTIGBT());
			
			kicker.getPlotPanel().addCapLevel(status.getCapLevel());
			kicker.getPlotPanel().addChargeCurrent(status.getChargeCurrent());
			
			fastChgPanel.setChargeLvL(status.getCapLevel());
		}
		
		
		@Override
		public void onNewSystemStatusMovement(final TigerSystemStatusMovement status)
		{
			if ((SumatraClock.nanoTime() - startMotor) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
			{
				motorMain.getEnhancedInputPanel().setLatestVelocity(status.getVelocity());
				motorMain.getEnhancedInputPanel().setLatestAngularVelocity(status.getAngularVelocity());
				startMotor = SumatraClock.nanoTime();
			}
		}
		
		
		@Override
		public void onNewSystemPowerLog(final TigerSystemPowerLog log)
		{
			power.addPowerLog(log);
			summary.setBatteryLevel(log.getBatLevel());
		}
		
		
		@Override
		public void onNewKickerIrLog(final TigerKickerIrLog log)
		{
			ir.addIrLog(log);
		}
		
		
		@Override
		public void onServerPortChanged(final int port)
		{
			mainPanel.setServerPort(port);
		}
		
		
		@Override
		public void onCpuIdChanged(final String cpuId)
		{
			mainPanel.setCpuId(cpuId);
		}
		
		
		@Override
		public void onMacChanged(final String mac)
		{
			mainPanel.setMac(mac);
		}
		
		
		@Override
		public void onLogsChanged(final TigerSystemSetLogs logs)
		{
			mainPanel.setLogMovement(logs.getMovement());
			mainPanel.setLogKicker(logs.getKicker());
			mainPanel.setLogPower(logs.getPower());
			mainPanel.setLogIr(logs.getIr());
		}
		
		
		@Override
		public void onBotFeaturesChanged(final Map<EFeature, EFeatureState> newFeatures)
		{
			features.setFeatures(newFeatures);
		}
		
		
		@Override
		public void onMotorParamsChanged(final TigerMotorSetParams params)
		{
			motorMain.setMode(params.getMode());
			
			for (int i = 0; i < 5; i++)
			{
				motorControls[i].getConfigPanel().setLogging(bot.getPidLogging(i));
				motorControls[i].getConfigPanel().setPidParams(bot.getKp(i), bot.getKi(i), bot.getKd(i), bot.getSlewMax(i));
			}
		}
		
		
		@Override
		public void onNewSystemPong(final TigerSystemPong pong)
		{
			if (pingThread == null)
			{
				return;
			}
			
			pingThread.pongArrived(pong.getId());
		}
		
		
		@Override
		public void onNewMotorPidLog(final TigerMotorPidLog log)
		{
			motorControls[log.getId()].getPidPanel().setLog(log);
			
			motorControls[log.getId()].getConfigPanel().setLatest(log.getLatest());
			motorControls[log.getId()].getConfigPanel().setOverload(log.getOverload());
			motorControls[log.getId()].getConfigPanel().setECurrent(log.getECurrent());
		}
		
		
		@Override
		public void onBlocked(final boolean blocked)
		{
			
		}
		
		
		@Override
		public void onNewSplineData(final SplinePair3D spline)
		{
			splinePanel.showSpline(spline);
		}
	}
	
	private class TigerBotSummaryObserver implements ITigerBotSummaryObserver
	{
		@Override
		public void onConnectionTypeChange(final boolean multicast)
		{
			bot.setUseUpdateAll(multicast);
		}
		
		
		@Override
		public void onOOFCheckChange(final boolean oofCheck)
		{
			bot.setOofCheck(oofCheck);
		}
		
		
		@Override
		public void onConnectionChange()
		{
			TigerBotPresenter.this.onConnectionChange();
		}
	}
	
	private class NetworkStatisticsUpdater extends TimerTask
	{
		private Statistics	lastTxStats	= new Statistics();
		private Statistics	lastRxStats	= new Statistics();
		
		
		@Override
		public void run()
		{
			if (!isStatsActive())
			{
				return;
			}
			final Statistics txStats = bot.getTransceiver().getTransmitterStats();
			network.setTxStat(txStats.substract(lastTxStats));
			lastTxStats = new Statistics(txStats);
			
			final Statistics rxStats = bot.getTransceiver().getReceiverStats();
			network.setRxStat(rxStats.substract(lastRxStats));
			lastRxStats = new Statistics(rxStats);
			
			network.setTxAllStat(lastTxStats);
			network.setRxAllStat(lastRxStats);
		}
	}
	
	private class MotorConfigPresenter implements IMotorConfigurationPanelObserver
	{
		private final int	id;
		
		
		/**
		 * @param motorId
		 */
		public MotorConfigPresenter(final int motorId)
		{
			id = motorId;
		}
		
		
		@Override
		public void onSetLog(final boolean logging)
		{
			bot.setPidLogging(id, logging);
		}
		
		
		@Override
		public void onSetPidParams(final float kp, final float ki, final float kd, final int slew)
		{
			bot.setPid(id, kp, ki, kd, slew);
		}
		
		
		@Override
		public void onSetManual(final int power)
		{
			bot.execute(new TigerMotorSetManual(id, power));
		}
		
		
		@Override
		public void onSetPidSetpoint(final int setpoint)
		{
			bot.execute(new TigerMotorSetPidSp(id, setpoint));
		}
	}
	
	private class PingThread extends Thread
	{
		private long							delay			= 100000000;
		
		private int								id				= 0;
		
		private final Map<Integer, Long>	activePings	= new HashMap<Integer, Long>();
		
		
		/**
		 * @param numPings
		 */
		public PingThread(final int numPings)
		{
			delay = 1000000000 / numPings;
		}
		
		
		@Override
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				synchronized (activePings)
				{
					activePings.put(id, SumatraClock.nanoTime());
				}
				
				bot.execute(new TigerSystemPing(id));
				id++;
				
				ThreadUtil.parkNanosSafe(delay);
			}
			
			activePings.clear();
		}
		
		
		/**
		 * @param id
		 */
		public void pongArrived(final int id)
		{
			Long startTime = null;
			
			synchronized (activePings)
			{
				startTime = activePings.remove(id);
			}
			
			if (startTime == null)
			{
				return;
			}
			
			final float delayPongArrive = (SumatraClock.nanoTime() - startTime) / 1000000.0f;
			
			network.setDelay(delayPongArrive);
		}
	}
}
