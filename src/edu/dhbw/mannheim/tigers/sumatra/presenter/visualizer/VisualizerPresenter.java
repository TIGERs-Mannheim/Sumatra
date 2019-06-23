/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): Bernhard
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.IAIConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.IBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickAutoSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveBallToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.VisualizerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IReplayOptionsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IRobotsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.RobotsPanel;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * Presenter for the visualizer.
 * <p>
 * NOTE: The fact that the view stores the actual state is not MVP-conform, but that would need greater refactoring and
 * I don't have time for this now
 * </p>
 * 
 * @author Bernhard, (Gero)
 */
public class VisualizerPresenter implements IRobotsPanelObserver, IFieldPanelObserver, IModuliStateObserver,
		IAIConfigObserver, IReplayOptionsPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log							= Logger.getLogger(VisualizerPresenter.class
																								.getName());
	
	private final VisualizerPanel				panel;
	/** in milliseconds */
	private static final long					VISUALIZATION_FREQUENCY	= 1;
	
	private BotID									selectedRobotId			= new BotID();
	
	private ASkillSystem							skillsystem					= null;
	
	private final BotConnectionListener		connectionListener		= new BotConnectionListener();
	private final AgentVisualizerListener	agentListener				= new AgentVisualizerListener();
	private final WPVisualizerListener		wpListener					= new WPVisualizerListener();
	
	private final List<Path>					paths							= new ArrayList<Path>(AIConfig.MAX_NUM_BOTS);
	
	private final OptionsPanelPresenter		optionsPanelPresenter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public VisualizerPresenter()
	{
		panel = new VisualizerPanel();
		optionsPanelPresenter = new OptionsPanelPresenter(panel.getFieldPanel(), panel.getOptionsPanel());
		
		// --- register on robotspanel as observer ---
		panel.getRobotsPanel().addObserver(this);
		
		// --- register on fieldpanel as observer ---
		panel.getFieldPanel().addObserver(this);
		
		AIConfig.getInstance().addObserver(this);
		
		// --- register on optionspanel as observer ---
		panel.getOptionsPanel().addObserver(optionsPanelPresenter);
		
		// --- register on replay options panel
		panel.getReplayOptionsPanel().addObserver(this);
		
		// --- register on moduli ---
		ModuliStateAdapter.getInstance().addObserver(this);
		
		// --- init path-list ---
		for (int i = 0; i < AIConfig.MAX_NUM_BOTS; i++)
		{
			paths.add(null);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @return
	 */
	public ISumatraView getView()
	{
		return panel;
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onRobotClick(BotID botId)
	{
		// --- select/deselect item ---
		if (selectedRobotId.equals(botId))
		{
			selectedRobotId = new BotID();
			panel.getRobotsPanel().deselectRobots();
			
		} else
		{
			selectedRobotId = botId;
			panel.getRobotsPanel().selectRobot(botId.getNumber());
		}
		
		panel.repaint();
		
	}
	
	
	@Override
	public void onFieldClick(final IVector2 pos, boolean ctrl, boolean alt, boolean shift, boolean meta)
	{
		if (!selectedRobotId.isUninitializedID() && robotAvailable(selectedRobotId))
		{
			final MovementCon moveCon = new MovementCon();
			
			if (meta)
			{
				skillsystem.execute(selectedRobotId, new MoveBallToSkill(pos));
			} else if (ctrl)
			{
				// move there and look at the ball
				moveCon.updateDestination(pos);
				moveCon.updateLookAtTarget(wpListener.getLastWorldFrame().ball.getPos());
				skillsystem.execute(selectedRobotId, new MoveToSkill(moveCon));
			} else if (shift)
			{
				final EBotType botType = wpListener.getLastWorldFrame().tigerBotsAvailable.get(selectedRobotId)
						.getBotType();
				float stepSize = AIConfig.getGeneral(botType).getPositioningPreAiming();
				IVector2 dest = GeoMath.stepAlongLine(wpListener.getLastWorldFrame().ball.getPos(), pos, -stepSize);
				moveCon.updateDestination(dest);
				moveCon.updateLookAtTarget(wpListener.getLastWorldFrame().ball.getPos());
				
				skillsystem.addObserver(new ISkillSystemObserver()
				{
					final ISkillSystemObserver	skillSystemObserver	= this;
					
					
					@Override
					public void onSkillStarted(ASkill skill, BotID botID)
					{
					}
					
					
					@Override
					public void onSkillCompleted(ASkill skill, BotID botID)
					{
						float dist = GeoMath.distancePP(pos, wpListener.getLastWorldFrame().ball.getPos());
						skillsystem.execute(selectedRobotId, new KickAutoSkill(dist));
						new Thread(new Runnable()
						{
							
							@Override
							public void run()
							{
								// this must not be called within the same thread in onSkillCompleted,
								// because this results in a ConcurrentModificationException
								skillsystem.removeObserver(skillSystemObserver);
							}
						}).start();
					}
				});
				
				skillsystem.execute(selectedRobotId, new MoveToSkill(moveCon));
			} else
			{
				moveCon.updateDestination(pos);
				skillsystem.execute(selectedRobotId, new MoveToSkill(moveCon));
			}
		}
	}
	
	
	/**
	 * Checks if the robot is available (tracking and connection to bot).
	 * 
	 * @return true;false
	 */
	private boolean robotAvailable(BotID id)
	{
		final RobotsPanel robotsPanel = panel.getRobotsPanel();
		
		// --- check arrays ---
		if (!robotsPanel.isBotConnected(id))
		{
			return false;
		} else if (!robotsPanel.isTigerDetected(id))
		{
			log.warn("no tracking data available to robot " + id);
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		if (state == ModulesState.RESOLVED)
		{
			// --- get worldpredictor ---
			try
			{
				final SumatraModel model = SumatraModel.getInstance();
				
				ABotManager botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
				botmanager.addObserver(connectionListener);
				
				AAgent agent = (AAgent) model.getModule(AAgent.MODULE_ID);
				agent.addObserver(agentListener);
				panel.getRobotsPanel().addObserver(agent);
				
				AWorldPredictor worldPredictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
				worldPredictor.addObserver(wpListener);
				
				
				skillsystem = (ASkillSystem) model.getModule("skillsystem");
			} catch (final ModuleNotFoundException err)
			{
				log.error("no worldpredictor or botmanager or skillsystem found!!!");
			}
			
			// --- clear connection-arrays ---
			panel.getRobotsPanel().clearView();
			panel.getRobotsPanel().repaint();
			panel.getOptionsPanel().setInitialButtonState();
			panel.getFieldPanel().setPanelVisible(false);
			
		} else if (state == ModulesState.ACTIVE)
		{
			panel.getOptionsPanel().setButtonsEnabled(true);
			panel.getFieldPanel().setPanelVisible(true);
			
			for (JCheckBox cb : panel.getOptionsPanel().getCheckBoxes().values())
			{
				optionsPanelPresenter.reactOnActionCommand(cb.getActionCommand(), cb.isSelected());
			}
		}
	}
	
	// --------------------------------------------------------------------------
	// --- inner-class-listener -------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected class AgentVisualizerListener implements IAIObserver
	{
		private long	start	= System.nanoTime();
		
		
		@Override
		public void onNewAIInfoFrame(AIInfoFrame lastAIInfoframe)
		{
			long curTime = System.nanoTime();
			if ((curTime - start) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
			{
				panel.getFieldPanel().drawAIFrame(lastAIInfoframe);
				start = curTime;
			}
		}
		
		
		@Override
		public void onNewPath(Path path)
		{
			// --- path for that robot already in list? ---
			// This is possible because the observable already made a path.lightCopy() for us! =)
			paths.set(path.getBotID().getNumber(), path);
			panel.getFieldPanel().setPaths(paths);
		}
		
		
		@Override
		public void onAIException(Exception ex, AIInfoFrame frame, AIInfoFrame prevFrame)
		{
		}
	}
	
	
	@Override
	public void onNewFieldRaster(FieldRasterConfig newFieldRasterConfig)
	{
		panel.getFieldPanel().setNewFieldRaster(newFieldRasterConfig);
	}
	
	
	protected class WPVisualizerListener implements IWorldPredictorObserver
	{
		private long			start				= System.nanoTime();
		private WorldFrame	lastWorldFrame	= null;
		
		
		@Override
		public void onNewWorldFrame(WorldFrame wf)
		{
			setLastWorldFrame(wf);
			if ((System.nanoTime() - start) > TimeUnit.MILLISECONDS.toNanos(VISUALIZATION_FREQUENCY))
			{
				final RobotsPanel robotsPanel = panel.getRobotsPanel();
				robotsPanel.clearDetections();
				
				for (Map.Entry<BotID, TrackedTigerBot> entry : wf.tigerBotsVisible)
				{
					robotsPanel.setTigerDetected(entry.getKey(), true);
				}
				
				final Iterator<Entry<BotID, TrackedBot>> foeIter = wf.getFoeBotMapIterator();
				
				while (foeIter.hasNext())
				{
					robotsPanel.setFoeDetected(foeIter.next().getKey(), true);
				}
				
				// --- show robots and ball on field ---
				panel.setTigersAreYellow(wf.teamProps.getTigersAreYellow());
				panel.getRobotsPanel().repaint();
				start = System.nanoTime();
			}
		}
		
		
		@Override
		public void onVisionSignalLost(WorldFrame emptyWf)
		{
			setLastWorldFrame(emptyWf);
			final RobotsPanel robotsPanel = panel.getRobotsPanel();
			robotsPanel.clearDetections();
			panel.setTigersAreYellow(getLastWorldFrame().teamProps.getTigersAreYellow());
			panel.getRobotsPanel().repaint();
		}
		
		
		/**
		 * @return the lastWorldFrame
		 */
		public WorldFrame getLastWorldFrame()
		{
			return lastWorldFrame;
		}
		
		
		/**
		 * @param lastWorldFrame the lastWorldFrame to set
		 */
		public void setLastWorldFrame(WorldFrame lastWorldFrame)
		{
			this.lastWorldFrame = lastWorldFrame;
		}
	}
	
	
	/**
	 * Handles BotConnection-infos.
	 * @author bernhard
	 * 
	 */
	protected class BotConnectionListener implements IBotManagerObserver
	{
		private final Map<BotID, BotTransceiverListener>	botTransceivers	= new HashMap<BotID, BotTransceiverListener>();
		
		
		@Override
		public void onBotAdded(ABot bot)
		{
			final BotTransceiverListener l = new BotTransceiverListener(bot.getBotID());
			bot.addObserver(l);
			botTransceivers.put(l.getId(), l);
		}
		
		
		@Override
		public void onBotRemoved(ABot bot)
		{
			final BotTransceiverListener l = botTransceivers.get(bot.getBotID());
			if (l != null)
			{
				bot.removeObserver(l);
			}
			checkPaths();
		}
		
		
		@Override
		public void onBotIdChanged(BotID oldId, BotID newId)
		{
			// Change handled internally in BotTranceiverListener; just change entry in map here!
			final BotTransceiverListener l = botTransceivers.remove(oldId);
			if (l != null)
			{
				botTransceivers.put(newId, l);
			}
			checkPaths();
		}
		
		
		@Override
		public void onBotConnectionChanged(ABot bot)
		{
		}
	}
	
	
	/**
	 * Handles BotTransceiver-infos.
	 * @author bernhard
	 * 
	 */
	protected class BotTransceiverListener implements IBotObserver
	{
		private BotID	id;
		
		
		/**
		 * @param id
		 */
		public BotTransceiverListener(BotID id)
		{
			this.id = id;
		}
		
		
		/**
		 * @return the id
		 */
		public BotID getId()
		{
			return id;
		}
		
		
		@Override
		public void onNameChanged(String name)
		{
		}
		
		
		@Override
		public void onIdChanged(BotID oldId, BotID newId)
		{
			id = newId;
		}
		
		
		@Override
		public void onNetworkStateChanged(ENetworkState state)
		{
			final RobotsPanel robotsPanel = panel.getRobotsPanel();
			robotsPanel.setBotConnected(id, state);
			
			robotsPanel.repaint();
		}
		
		
		@Override
		public void onBlocked(boolean blocked)
		{
			
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Check if there are disconnected bots in the paths and delete them
	 */
	private void checkPaths()
	{
		final List<Path> delete = new LinkedList<Path>();
		for (final Path path : paths)
		{
			if ((path != null) && !robotAvailable(path.getBotID()))
			{
				delete.add(path);
			}
		}
		for (final Path path : delete)
		{
			paths.set(path.getBotID().getNumber(), null);
		}
	}
	
	
	@Override
	public void onRecord(boolean active)
	{
		panel.getFieldPanel().getMultiLayer().setRecording(active, false);
	}
	
	
	@Override
	public void onSave(boolean active)
	{
		panel.getFieldPanel().getMultiLayer().setRecording(active, true);
		
	}
	
	
	@Override
	public void onUpdate()
	{
		panel.getReplayLoadPanel().doUpdate();
	}
	
}
