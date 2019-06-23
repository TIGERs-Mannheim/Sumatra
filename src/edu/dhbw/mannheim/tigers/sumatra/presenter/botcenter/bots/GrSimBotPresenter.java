/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.GrSimBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.IGrSimBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeManual;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.FeaturePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ITigerBotMainPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SkillsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.SplinePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.TigerBotMainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.kicker.KickerPanel.IKickerPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorMainPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2.TigerBotV2Summary;


/**
 * Presenter for a grSim bot.
 * 
 * @author AndreR
 * 
 */
public class GrSimBotPresenter extends ABotPresenter implements ITigerBotMainPanelObserver, ILookAndFeelStateObserver,
		IKickerPanelObserver, IWorldPredictorObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log							= Logger.getLogger(TigerBotPresenter.class.getName());
	
	private GrSimBot						bot							= null;
	
	private ABotManager					botmanager					= null;
	private AWorldPredictor				worldPredictor				= null;
	private TigerBotV2Summary			summary						= null;
	private KickerConfig					fastChgPanel				= null;
	private TigerBotMainPanel			mainPanel					= null;
	private SkillsPanel					skills						= null;
	private KickerPanel					kicker						= null;
	private MotorMainPanel				motorMain					= null;
	private FeaturePanel					features						= null;
	private SplinePanel					splinePanel					= null;
	
	// Observer Handler
	private final GrSimBotObserver	tigerBotObserver			= new GrSimBotObserver();
	
	
	private long							startNewWP					= System.nanoTime();
	// in milliseconds
	private static final long			VISUALIZATION_FREQUENCY	= 200;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param bot
	 */
	public GrSimBotPresenter(ABot bot)
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
		
		summary = new TigerBotV2Summary();
		fastChgPanel = new KickerConfig(bot.getBotID());
		mainPanel = new TigerBotMainPanel();
		skills = new SkillsPanel();
		kicker = new KickerPanel();
		motorMain = new MotorMainPanel();
		features = new FeaturePanel();
		splinePanel = new SplinePanel();
		
		this.bot = (GrSimBot) bot;
		
		node = new BotCenterTreeNode(bot.getName() + " (" + bot.getBotID().getNumber() + ")", ETreeIconType.BOT, bot
				.getColor().getColor(), mainPanel, true);
		
		final BotCenterTreeNode motorsNode = new BotCenterTreeNode("Motors", ETreeIconType.MOTOR, motorMain, true);
		node.add(motorsNode);
		node.add(new BotCenterTreeNode("Kicker", ETreeIconType.KICK, kicker, true));
		node.add(new BotCenterTreeNode("Skills", ETreeIconType.LAMP, skills, true));
		node.add(new BotCenterTreeNode("Features", ETreeIconType.GEAR, features, true));
		node.add(new BotCenterTreeNode("Spline", ETreeIconType.SPLINE, splinePanel, true));
		
		summary.setId(bot.getBotID());
		summary.setBotName(bot.getName());
		
		tigerBotObserver.onNameChanged(bot.getName());
		tigerBotObserver.onIdChanged(BotID.createBotId(), bot.getBotID());
		tigerBotObserver.onNetworkStateChanged(this.bot.getNetworkState());
		tigerBotObserver.onBotFeaturesChanged(this.bot.getBotFeatures());
		
		this.bot.addObserver(tigerBotObserver);
		mainPanel.addObserver(this);
		skills.addObserver(this);
		kicker.addObserver(this);
		worldPredictor.addObserver(this);
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		motorMain.getEnhancedInputPanel().addObserver(this);
		features.addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void delete()
	{
		bot.removeObserver(tigerBotObserver);
		mainPanel.removeObserver(this);
		skills.removeObserver(this);
		kicker.removeObserver(this);
		worldPredictor.removeObserver(this);
		LookAndFeelStateAdapter.getInstance().removeObserver(this);
		motorMain.getEnhancedInputPanel().removeObserver(this);
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
				bot.setNetworkState(ENetworkState.ONLINE);
				break;
			case CONNECTING:
			case ONLINE:
				bot.setNetworkState(ENetworkState.OFFLINE);
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
			
			bot.stop();
			bot.start();
		}
		
		catch (final NumberFormatException e)
		{
			log.warn("Invalid value in a general configuration field");
		}
	}
	
	
	@Override
	public void onKickerFire(int level, float duration, EKickerMode mode, int device)
	{
		final TigerKickerKickV2 kick = new TigerKickerKickV2(device, mode, duration, level);
		log.trace("Kick with: " + kick);
		bot.execute(kick);
	}
	
	
	@Override
	public void onKickerChargeManual(int duration, int on, int off)
	{
		if ((on < 0) || (off < 0) || (on > 60000) || (off > 60000) || (duration < 0) || (duration > 60000))
		{
			return;
		}
		
		bot.execute(new TigerKickerChargeManual(on, off, duration));
	}
	
	
	@Override
	public void onKickerChargeAuto(int max)
	{
		bot.setKickerMaxCap(max);
		bot.execute(new TigerKickerChargeAuto(max));
	}
	
	
	@Override
	public void onNewWorldFrame(SimpleWorldFrame wf)
	{
		final TrackedTigerBot tracked = wf.getBots().getWithNull(bot.getBotID());
		if (tracked == null)
		{
			return;
		}
		if ((System.nanoTime() - startNewWP) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
		{
			final Vector2f vel = new Vector2f(tracked.getVel().turnNew(-tracked.getAngle()));
			
			motorMain.getEnhancedInputPanel().setLatestWPData(new Vector2f(-vel.y(), vel.x()), tracked.getaVel());
			startNewWP = System.nanoTime();
		}
	}
	
	
	@Override
	public void onVisionSignalLost(SimpleWorldFrame emptyWf)
	{
		motorMain.getEnhancedInputPanel().setLatestWPData(new Vector2f(0.0f, 0.0f), 0.0f);
	}
	
	
	// -------------------------------------------------------------
	// Sub classes
	// -------------------------------------------------------------
	private class GrSimBotObserver implements IGrSimBotObserver
	{
		@Override
		public void onNameChanged(String name)
		{
			summary.setBotName(name);
			mainPanel.setBotName(name);
		}
		
		
		@Override
		public void onIdChanged(BotID oldId, BotID newId)
		{
			summary.setId(newId);
			mainPanel.setId(newId);
			mainPanel.setColor(newId.getTeamColor());
		}
		
		
		@Override
		public void onNetworkStateChanged(ENetworkState state)
		{
			summary.setNetworkState(state);
			mainPanel.setConnectionState(state);
		}
		
		
		@Override
		public void onBotFeaturesChanged(final Map<EFeature, EFeatureState> newFeatures)
		{
			features.setFeatures(newFeatures);
		}
		
		
		@Override
		public void onBlocked(boolean blocked)
		{
			
		}
		
		
		@Override
		public void onNewSplineData(SplinePair3D spline)
		{
			splinePanel.showSpline(spline);
		}
		
		
		@Override
		public void onIncommingCommand(ACommand cmd)
		{
		}
		
		
		@Override
		public void onOutgoingCommand(ACommand cmd)
		{
		}
		
		
		@Override
		public void onIpChanged(String ip)
		{
		}
		
		
		@Override
		public void onPortChanged(int port)
		{
		}
	}
	
	
	@Override
	public void onSaveLogs()
	{
	}
	
	
	@Override
	public void onUpdateFirmware(String filepath, boolean targetMain)
	{
	}
	
	
	@Override
	public void onNewCamDetectionFrame(CamDetectionFrame frame)
	{
	}
}
