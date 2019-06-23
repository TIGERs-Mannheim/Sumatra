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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.IBotObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveDynamicTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFast;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedCurrentOrientation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IAIObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IBotManagerObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IWorldPredictorObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.VisualizerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IOptionsPanelObserver;
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
public class VisualizerPresenter implements IRobotsPanelObserver, IFieldPanelObserver, IOptionsPanelObserver,
		IModuliStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private VisualizerPanel						panel						= null;
	private Logger									log						= Logger.getLogger(getClass());
	private int										selectedRobotId		= -1;
	
	private ASkillSystem							skillsystem				= null;
	private ABotManager							botmanager				= null;
	private AAgent									agent						= null;
	private AWorldPredictor						worldPredictor			= null;
	
	private final BotConnectionListener		connectionListener	= new BotConnectionListener();
	private final AgentVisualizerListener	agentListener			= new AgentVisualizerListener();
	private final WPVisualizerListener		wpListener				= new WPVisualizerListener();
	// private int counter = 60;
	
	List<Path>										paths						= new Vector<Path>(12);
	
	
	// private ArrayBlockingQueue<WorldFrame> worldpredictorQueue = new ArrayBlockingQueue<WorldFrame>(5);
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public VisualizerPresenter()
	{
		panel = new VisualizerPanel();
		
		// --- register on robotspanel as observer ---
		panel.getRobotsPanel().addObserver(this);
		
		// --- register on fieldpanel as observer ---
		panel.getFieldPanel().addObserver(this);
		
		// --- register on optionspanel as observer ---
		panel.getOptionsPanel().addObserver(this);
		
		// --- register on moduli ---
		ModuliStateAdapter.getInstance().addObserver(this);
		
		// --- init path-list ---
		for (int i = 0; i < 12; i++)
		{
			paths.add(null);
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public ISumatraView getView()
	{
		return panel;
	}
	

	// --------------------------------------------------------------------------
	// --- observer-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onRobotClick(int botId)
	{
		// --- select/deselect item ---
		if (selectedRobotId == botId)
		{
			selectedRobotId = -1;
			panel.getRobotsPanel().deselectRobots();
			
		} else
		{
			selectedRobotId = botId;
			panel.getRobotsPanel().selectRobot(botId);
		}
		
		panel.repaint();
		
	}
	

	@Override
	public void onFieldClick(int x, int y, boolean ctrl, boolean alt)
	{
		if (selectedRobotId != -1 && robotAvailable(selectedRobotId))
		{
			if (ctrl)
			{
				// move there and look at the ball
				skillsystem.execute(selectedRobotId, new MoveDynamicTarget(new Vector2f(x, y), -1));
			} else if (alt)
			{
				skillsystem.execute(selectedRobotId, new MoveFast(new Vector2f(x, y)));
			} else
			{
				skillsystem.execute(selectedRobotId, new MoveFixedCurrentOrientation(new Vector2f(x, y)));
			}
			

		}
	}
	

	/**
	 * Checks if the robot is available (tracking and connection to bot).
	 * 
	 * @return true;false
	 */
	private boolean robotAvailable(int id)
	{
		RobotsPanel robotsPanel = panel.getRobotsPanel();
		
		// --- check arrays ---
		if (!robotsPanel.isBotConnected(id))
		{
			log.warn("no connection available to robot " + id);
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
				SumatraModel model = SumatraModel.getInstance();
				
				botmanager = (ABotManager) model.getModule(ABotManager.MODULE_ID);
				botmanager.addObserver(connectionListener);
				
				agent = (AAgent) model.getModule(AAgent.MODULE_ID);
				agent.addObserver(agentListener);
				
				worldPredictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
				worldPredictor.addObserver(wpListener);
				

				skillsystem = (ASkillSystem) model.getModule("skillsystem");
				
				boolean tigersAreYellow = model.getGlobalConfiguration().getString("ourColor").equals("yellow");
				panel.setTigersAreYellow(tigersAreYellow);
			} catch (ModuleNotFoundException err)
			{
				log.error("no worldpredictor or botmanager or skillsystem found!!!");
			}
			
			// --- clear connection-arrays ---
			panel.getRobotsPanel().clearView();
			panel.getRobotsPanel().repaint();
			
			panel.getFieldPanel().clearField();
		} else if (state == ModulesState.ACTIVE)
		{
			// --- set vis-field-size ---
			panel.getFieldPanel().setFieldSize((int) AIConfig.getGeometry().getFieldWidth() / 10,
					(int) AIConfig.getGeometry().getFieldLength() / 10);
		}
	}
	

	/**
	 * Options checkboxes-handling
	 */
	@Override
	public void onCheckboxClick(String actionCommand, boolean isSelected)
	{
		FieldPanel fieldPanel = panel.getFieldPanel();
		
		// --- worldpredictor-options ---
		if (actionCommand.equals("velocity"))
		{
			fieldPanel.setShowVelocity(isSelected);
		} else if (actionCommand.equals("acceleration"))
		{
			fieldPanel.setShowAcceleration(isSelected);
		}
		// --- ai-options ---
		else if (actionCommand.equals("posGrid"))
		{
			fieldPanel.setShowPositiongGrid(isSelected);
		} else if (actionCommand.equals("analyseGrid"))
		{
			fieldPanel.setShowAnalysingGrid(isSelected);
		} else if (actionCommand.equals("paths"))
		{
			fieldPanel.setShowPaths(isSelected);
		} else if (actionCommand.equals("splines"))
		{
			fieldPanel.setShowSplines(isSelected);
		} else if (actionCommand.equals("debugPoints"))
		{
			fieldPanel.setShowDebugPoints(isSelected);
		} else if (actionCommand.equals("DefenseGoalPoints"))
		{
			fieldPanel.setShowDefenseGoalPoints(isSelected);
		}
		
		// --- repaint panel ---
		fieldPanel.repaint();
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- inner-class-listener -------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected class AgentVisualizerListener implements IAIObserver
	{
		
		@Override
		public void onNewFieldRaster(int columnSize, int rowSize, int columnSizeAnalysing, int rowSizeAnalysing)
		{
			// --- set view-data ---
			panel.getFieldPanel().setColumnGridSize(columnSize);
			panel.getFieldPanel().setRowGridSize(rowSize);
			panel.getFieldPanel().setColumnAnalysingGridSize(columnSizeAnalysing);
			panel.getFieldPanel().setRowAnalysingGridSize(rowSizeAnalysing);
		}
		

		@Override
		public void onNewAIInfoFrame(AIInfoFrame lastAIInfoframe)
		{
			panel.getFieldPanel().setDefensePoints(lastAIInfoframe.tacticalInfo.getDefGoalPoints());
			panel.getFieldPanel().setDebugPoints(lastAIInfoframe.tacticalInfo.getDebugPoints());
		}
		

		@Override
		public void onNewPath(Path path)
		{
			// --- path for that robot already in list? ---
			paths.set(path.botID, path); // This is possible because the observable already made a path.lightCopy() for us!
			// =)
			panel.getFieldPanel().setPaths(paths);
		}
		
	}
	
	
	protected class WPVisualizerListener implements IWorldPredictorObserver
	{
		@Override
		public void onNewWorldFrame(WorldFrame wf)
		{
			RobotsPanel robotsPanel = panel.getRobotsPanel();
			robotsPanel.clearDetections();
			for (Map.Entry<Integer, TrackedTigerBot> map : wf.tigerBots.entrySet())
			{
				robotsPanel.setTigerDetected(map.getKey(), true);
			}
			
			for (Map.Entry<Integer, TrackedBot> map : wf.foeBots.entrySet())
			{
				robotsPanel.setFoeDetected(map.getKey(), true);
			}
			

			// --- show robots and ball on field ---
			panel.getFieldPanel().drawWorldFrame(wf);
			panel.getRobotsPanel().repaint();
		}
	}
	
	
	/**
	 * Handles BotConnection-infos.
	 * @author bernhard
	 * 
	 */
	protected class BotConnectionListener implements IBotManagerObserver
	{
		private final Map<Integer, BotTransceiverListener>	botTransceivers	= new HashMap<Integer, BotTransceiverListener>();
		
		
		@Override
		public void onBotAdded(ABot bot)
		{
			BotTransceiverListener l = new BotTransceiverListener(bot.getBotId());
			bot.addObserver(l);
			botTransceivers.put(l.getId(), l);
		}
		

		@Override
		public void onBotRemoved(ABot bot)
		{
			final BotTransceiverListener l = botTransceivers.get(bot.getBotId());
			if (l != null)
			{
				bot.removeObserver(l);
			}
		}
		

		@Override
		public void onBotIdChanged(int oldId, int newId)
		{
			// Change handled internally in BotTranceiverListener; just change entry in map here!
			final BotTransceiverListener l = botTransceivers.remove(oldId);
			if (l != null)
			{
				botTransceivers.put(newId, l);
			}
		}
	}
	
	
	/**
	 * Handles BotTransceiver-infos.
	 * @author bernhard
	 * 
	 */
	protected class BotTransceiverListener implements IBotObserver
	{
		private int	id;
		
		
		public BotTransceiverListener(int id)
		{
			this.id = id;
		}
		

		/**
		 * @return the id
		 */
		public int getId()
		{
			return id;
		}
		

		@Override
		public void onNameChanged(String name)
		{
		}
		

		@Override
		public void onIdChanged(int oldId, int newId)
		{
			id = newId;
		}
		

		@Override
		public void onIpChanged(String ip)
		{
		}
		

		@Override
		public void onPortChanged(int port)
		{
		}
		

		@Override
		public void onNetworkStateChanged(ENetworkState state)
		{
			
			RobotsPanel robotsPanel = panel.getRobotsPanel();
			robotsPanel.setBotConnected(id, state);
			
			robotsPanel.repaint();
		}
	}
	
}
