/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 10.08.2010
 * Author(s): Bernhard
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.cam.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.VisualizerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IReplayOptionsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IRobotsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.RobotsPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * Presenter for the visualizer.
 * <p>
 * NOTE: The fact that the view stores the actual state is not MVP-conform, but that would need greater refactoring and
 * I don't have time for this now
 * </p>
 * 
 * @author Bernhard, (Gero)
 */
public class VisualizerPresenter implements ISumatraViewPresenter, IRobotsPanelObserver, IFieldPanelObserver,
		IModuliStateObserver, IReplayOptionsPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger				log					= Logger.getLogger(VisualizerPresenter.class
																						.getName());
	
	private final VisualizerPanel				panel;
	private BotID									selectedRobotId	= BotID.createBotId();
	
	private ASkillSystem							skillsystem			= null;
	private ABotManager							botManager			= null;
	
	private final AgentVisualizerListener	agentListener		= new AgentVisualizerListener();
	private final WPVisualizerListener		wpListener			= new WPVisualizerListener();
	
	private final IBotIDMap<Path>				paths					= new BotIDMap<Path>();
	
	private final OptionsPanelPresenter		optionsPanelPresenter;
	
	private ScheduledExecutorService			execService;
	
	
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
		
		// --- register on optionspanel as observer ---
		panel.getOptionsPanel().addObserver(optionsPanelPresenter);
		
		// --- register on replay options panel
		panel.getReplayOptionsPanel().addObserver(this);
		
		// --- register on moduli ---
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- observer-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onRobotClick(final BotID botId)
	{
		// --- select/deselect item ---
		if (selectedRobotId.equals(botId))
		{
			selectedRobotId = BotID.createBotId();
			panel.getRobotsPanel().deselectRobots();
			
		} else
		{
			selectedRobotId = botId;
			panel.getRobotsPanel().selectRobot(botId);
		}
		
		panel.repaint();
		
	}
	
	
	@Override
	public void onFieldClick(final IVector2 posIn, final MouseEvent e)
	{
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean rightClick = SwingUtilities.isRightMouseButton(e);
		
		if (rightClick)
		{
			AReferee referee;
			try
			{
				referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				referee.replaceBall(posIn);
			} catch (ModuleNotFoundException err)
			{
				log.error("Referee module not found.", err);
			}
			return;
		}
		
		if (!selectedRobotId.isUninitializedID() && robotAvailable(selectedRobotId))
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill(EMoveToMode.DO_COMPLETE);
			final MovementCon moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowed(true);
			
			final IVector2 pos;
			final IVector2 ballPos;
			if (TeamConfig.getInstance().getTeamProps().getLeftTeam() != selectedRobotId.getTeamColor())
			{
				pos = new Vector2(-posIn.x(), -posIn.y());
				IVector2 b = wpListener.getLastWorldFrame().getBall().getPos();
				ballPos = new Vector2(-b.x(), -b.y());
			} else
			{
				pos = posIn;
				ballPos = wpListener.getLastWorldFrame().getBall().getPos();
			}
			
			if (ctrl)
			{
				// move there and look at the ball
				moveCon.updateDestination(pos);
				moveCon.updateLookAtTarget(ballPos);
				skillsystem.execute(selectedRobotId, skill);
			} else if (shift)
			{
				IVector2 dest = GeoMath.stepAlongLine(ballPos, pos, -150);
				moveCon.updateDestination(dest);
				moveCon.updateLookAtTarget(ballPos);
				
				skillsystem.addObserver(new ISkillSystemObserver()
				{
					final ISkillSystemObserver	skillSystemObserver	= this;
					
					
					@Override
					public void onSkillStarted(final ISkill skill, final BotID botID)
					{
					}
					
					
					@Override
					public void onSkillCompleted(final ISkill skill, final BotID botID)
					{
						skillsystem.execute(selectedRobotId, new KickSkill(new DynamicPosition(pos), EKickMode.POINT));
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
				
				skillsystem.execute(selectedRobotId, skill);
			} else
			{
				moveCon.updateDestination(pos);
				skillsystem.execute(selectedRobotId, skill);
			}
		}
	}
	
	
	/**
	 * Checks if the robot is available (tracking and connection to bot).
	 * 
	 * @return true;false
	 */
	private boolean robotAvailable(final BotID id)
	{
		ABot bot = botManager.getAllBots().get(id);
		if ((bot != null) && (bot.getNetworkState() == ENetworkState.ONLINE))
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				
				panel.getRobotsPanel().clearView();
				panel.getOptionsPanel().setInitialButtonState();
				panel.getOptionsPanel().setButtonsEnabled(true);
				panel.getFieldPanel().setPanelVisible(true);
				
				// --- get worldpredictor ---
				try
				{
					final SumatraModel model = SumatraModel.getInstance();
					
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					
					panel.getRobotsPanel().addObserver(botManager);
					
					Agent agent = (Agent) model.getModule(AAgent.MODULE_ID_YELLOW);
					agent.addObserver(agentListener);
					agent = (Agent) model.getModule(AAgent.MODULE_ID_BLUE);
					agent.addObserver(agentListener);
					
					AWorldPredictor worldPredictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
					worldPredictor.addObserver(wpListener);
					
					skillsystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
				} catch (final ModuleNotFoundException err)
				{
					log.error("no worldpredictor or botmanager or skillsystem or botManager found!!!", err);
				}
				
				execService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("UpdateRobotsPanel"));
				execService.scheduleAtFixedRate(new Runnable()
				{
					@Override
					public void run()
					{
						panel.getRobotsPanel().setBots(botManager.getAllBots());
						checkPaths();
						panel.getRobotsPanel().repaint();
					}
				}, 100, 100, TimeUnit.MILLISECONDS);
				
				for (JCheckBox cb : panel.getOptionsPanel().getCheckBoxes().values())
				{
					optionsPanelPresenter.reactOnActionCommand(cb.getActionCommand(), cb.isSelected());
				}
				
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				// --- clear connection-arrays ---
				if (execService != null)
				{
					execService.shutdown();
					try
					{
						execService.awaitTermination(2, TimeUnit.SECONDS);
					} catch (InterruptedException err)
					{
						log.error("Error waiting for termination.", err);
					}
				}
				panel.getRobotsPanel().clearView();
				panel.getRobotsPanel().repaint();
				panel.getOptionsPanel().setInitialButtonState();
				panel.getFieldPanel().setPanelVisible(false);
				panel.getFieldPanel().clearField();
				break;
			default:
				break;
		
		}
	}
	
	// --------------------------------------------------------------------------
	// --- inner-class-listener -------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected class AgentVisualizerListener implements IAIObserver
	{
		
		
		@Override
		public void onNewAIInfoFrame(final AIInfoFrame lastAIInfoframe)
		{
			panel.getFieldPanel().updateAiFrame(lastAIInfoframe);
		}
		
		
		@Override
		public void onAIException(final Exception ex, final IRecordFrame frame, final IRecordFrame prevFrame)
		{
		}
	}
	
	
	protected class WPVisualizerListener implements IWorldPredictorObserver
	{
		private SimpleWorldFrame	lastWorldFrame	= null;
		
		
		@Override
		public void onNewWorldFrame(final SimpleWorldFrame wf)
		{
			setLastWorldFrame(wf);
			final RobotsPanel robotsPanel = panel.getRobotsPanel();
			robotsPanel.settBots(wf.getBots());
			panel.getFieldPanel().updateWFrame(wf);
		}
		
		
		@Override
		public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
		{
			setLastWorldFrame(emptyWf);
			panel.getRobotsPanel().settBots(new BotIDMap<TrackedTigerBot>());
			panel.getFieldPanel().updateWFrame(emptyWf);
		}
		
		
		@Override
		public void onNewCamDetectionFrame(final CamDetectionFrame frame)
		{
		}
		
		
		/**
		 * @return the lastWorldFrame
		 */
		public SimpleWorldFrame getLastWorldFrame()
		{
			return lastWorldFrame;
		}
		
		
		/**
		 * @param lastWorldFrame the lastWorldFrame to set
		 */
		public void setLastWorldFrame(final SimpleWorldFrame lastWorldFrame)
		{
			this.lastWorldFrame = lastWorldFrame;
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
		for (final Path path : paths.values())
		{
			if (!robotAvailable(path.getBotID()))
			{
				delete.add(path);
			}
		}
		for (final Path path : delete)
		{
			paths.remove(path.getBotID());
		}
	}
	
	
	@Override
	public void onRecord(final boolean active)
	{
		panel.getFieldPanel().getMultiLayer().setRecording(active, false);
		panel.getReplayLoadPanel().doUpdate();
	}
	
	
	@Override
	public void onSave(final boolean active)
	{
		panel.getFieldPanel().getMultiLayer().setRecording(active, true);
		panel.getReplayLoadPanel().doUpdate();
		
	}
	
	
	@Override
	public Component getComponent()
	{
		return panel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
}
