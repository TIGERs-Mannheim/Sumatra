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

import javax.swing.JCheckBoxMenuItem;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EKickMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.KickSkill.EMoveMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts;
import edu.dhbw.mannheim.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.dhbw.mannheim.tigers.sumatra.util.ThreadUtil;
import edu.dhbw.mannheim.tigers.sumatra.util.config.UserConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.VisualizerPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.IFieldPanelObserver;
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
		IModuliStateObserver
{
	private static final Logger			log										= Logger.getLogger(VisualizerPresenter.class
																										.getName());
	
	private Updater							updater									= new Updater();
	private WorldFrameWrapper				lastWorldFrameWrapper				= WorldFrameWrapper.createDefault(0);
	private IRecordFrame						lastRecordFrameBlue					= null;
	private IRecordFrame						lastRecordFrameYellow				= null;
	private RefereeMsg						lastRefereeMsg							= null;
	private final VisualizerPanel			panel;
	private BotID								selectedRobotId						= BotID.createBotId();
	private MovementCon						moveCon									= new MovementCon();
	private boolean							mouseMoveUpdateDestinationMode	= false;
	
	private ASkillSystem						skillsystem								= null;
	private ABotManager						botManager								= null;
	
	private final OptionsPanelPresenter	optionsPanelPresenter;
	
	
	/**
	 */
	public VisualizerPresenter()
	{
		panel = new VisualizerPanel();
		optionsPanelPresenter = new OptionsPanelPresenter(panel.getFieldPanel(), panel.getOptionsMenu());
		
		// --- register on robotspanel as observer ---
		panel.getRobotsPanel().addObserver(this);
		
		// --- register on fieldpanel as observer ---
		panel.getFieldPanel().addObserver(this);
		
		// --- register on optionspanel as observer ---
		panel.getOptionsMenu().addObserver(optionsPanelPresenter);
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
		mouseMoveUpdateDestinationMode = false;
		panel.repaint();
	}
	
	
	@Override
	public void onMouseMoved(final IVector2 pos, final MouseEvent e)
	{
		if (mouseMoveUpdateDestinationMode)
		{
			if (TeamConfig.getLeftTeam() != selectedRobotId.getTeamColor())
			{
				moveCon.updateDestination(new Vector2(-pos.x(), -pos.y()));
			} else
			{
				moveCon.updateDestination(pos);
			}
		}
	}
	
	
	@Override
	public void onFieldClick(final IVector2 posIn, final MouseEvent e)
	{
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean rightClick = SwingUtilities.isRightMouseButton(e);
		boolean middleClick = SwingUtilities.isMiddleMouseButton(e);
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
		
		if (middleClick)
		{
			AWorldPredictor wp;
			try
			{
				wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
				wp.setLatestBallPosHint(posIn);
			} catch (ModuleNotFoundException err)
			{
				log.error("Could not find WP module!");
			}
			return;
		}
		
		if ((lastWorldFrameWrapper == null) || (lastWorldFrameWrapper.getSimpleWorldFrame() == null))
		{
			return;
		}
		
		for (TrackedTigerBot tBot : lastWorldFrameWrapper.getSimpleWorldFrame().getBots().values())
		{
			Circle botCircle = new Circle(tBot.getPos(), AIConfig.getGeometry().getBotRadius());
			if (botCircle.isPointInShape(posIn))
			{
				onRobotClick(tBot.getId());
				return;
			}
		}
		
		
		if (!selectedRobotId.isUninitializedID() && robotAvailable(selectedRobotId))
		{
			IMoveToSkill skill = AMoveSkill.createMoveToSkill();
			skill.setDoComplete(true);
			moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowedOur(true);
			moveCon.setPenaltyAreaAllowedTheir(true);
			
			final IVector2 pos;
			final IVector2 ballPos;
			if (TeamConfig.getLeftTeam() != selectedRobotId.getTeamColor())
			{
				pos = new Vector2(-posIn.x(), -posIn.y());
				IVector2 b = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
				ballPos = new Vector2(-b.x(), -b.y());
			} else
			{
				pos = posIn;
				ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
			}
			
			mouseMoveUpdateDestinationMode = false;
			if (ctrl && shift)
			{
				mouseMoveUpdateDestinationMode = true;
				moveCon.updateDestination(pos);
				skillsystem.execute(selectedRobotId, skill);
			} else if (ctrl)
			{
				// move there and look at the ball
				moveCon.updateDestination(pos);
				moveCon.updateLookAtTarget(ballPos);
				skillsystem.execute(selectedRobotId, skill);
			} else if (shift)
			{
				skillsystem.execute(selectedRobotId, new KickSkill(new DynamicPosition(pos), EKickMode.POINT,
						EMoveMode.CHILL));
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
		if (lastWorldFrameWrapper == null)
		{
			return false;
		}
		TrackedTigerBot tBot = lastWorldFrameWrapper.getSimpleWorldFrame().getBot(id);
		if (tBot == null)
		{
			return false;
		}
		ABot bot = tBot.getBot();
		if ((bot != null) && (bot.getNetworkState() == ENetworkState.ONLINE))
		{
			return true;
		}
		return false;
	}
	
	
	/**
	 * Start view
	 */
	public void start()
	{
		panel.getRobotsPanel().clearView();
		panel.getOptionsMenu().setInitialButtonState();
		panel.getOptionsMenu().setButtonsEnabled(true);
		panel.getFieldPanel().setPanelVisible(true);
		
		for (JCheckBoxMenuItem cb : panel.getOptionsMenu().getCheckBoxes().values())
		{
			optionsPanelPresenter.reactOnActionCommand(cb.getActionCommand(), cb.isSelected());
		}
		
		Thread updaterThread = new Thread(updater, "VisualizerUpdater");
		updaterThread.start();
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		updater.running = false;
		panel.getRobotsPanel().clearView();
		panel.getRobotsPanel().repaint();
		panel.getOptionsMenu().setInitialButtonState();
		panel.getFieldPanel().setPanelVisible(false);
		panel.getFieldPanel().clearField();
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				
				
				// --- get worldpredictor ---
				try
				{
					final SumatraModel model = SumatraModel.getInstance();
					
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					
					panel.getRobotsPanel().addObserver(botManager);
					
					Agent agent = (Agent) model.getModule(AAgent.MODULE_ID_YELLOW);
					agent.addVisObserver(this);
					agent = (Agent) model.getModule(AAgent.MODULE_ID_BLUE);
					agent.addVisObserver(this);
					
					AWorldPredictor worldPredictor = (AWorldPredictor) model.getModule(AWorldPredictor.MODULE_ID);
					worldPredictor.addObserver(this);
					
					skillsystem = (ASkillSystem) model.getModule(ASkillSystem.MODULE_ID);
					
					AReferee referee = (AReferee) model.getModule(AReferee.MODULE_ID);
					referee.addObserver(this);
				} catch (final ModuleNotFoundException err)
				{
					log.error("no worldpredictor or botmanager or skillsystem or botManager found!!!", err);
				}
				
				GlobalShortcuts.register(EShortcut.RESET_FIELD,
						(() -> panel.getFieldPanel().onOptionChanged(EVisualizerOptions.RESET_FIELD, true)));
				
				start();
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				GlobalShortcuts.unregisterAll(EShortcut.RESET_FIELD);
				
				stop();
				try
				{
					panel.getRobotsPanel().removeObserver(botManager);
					Agent agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agent.removeVisObserver(this);
					agent = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agent.removeVisObserver(this);
					AWorldPredictor worldPredictor = (AWorldPredictor) SumatraModel.getInstance().getModule(
							AWorldPredictor.MODULE_ID);
					worldPredictor.removeObserver(this);
				} catch (final ModuleNotFoundException err)
				{
					log.error("no worldpredictor or botmanager or skillsystem or botManager found!!!", err);
				}
				break;
			default:
				break;
		
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- inner-class-listener -------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
		if (lastAIInfoframe.getTeamColor() == ETeamColor.YELLOW)
		{
			lastRecordFrameYellow = lastAIInfoframe;
		} else
		{
			lastRecordFrameBlue = lastAIInfoframe;
		}
	}
	
	
	@Override
	public void onAIStopped(final ETeamColor teamColor)
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			lastRecordFrameYellow = null;
		} else
		{
			lastRecordFrameBlue = null;
		}
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
		lastWorldFrameWrapper = wfWrapper;
	}
	
	
	@Override
	public void onNewRefereeMsg(final RefereeMsg refMsg)
	{
		lastRefereeMsg = refMsg;
	}
	
	
	private void updateWorldframe(final WorldFrameWrapper wfWrapper)
	{
		final RobotsPanel robotsPanel = panel.getRobotsPanel();
		robotsPanel.settBots(wfWrapper.getSimpleWorldFrame().getBots());
		panel.getFieldPanel().updateWFrame(wfWrapper.getSimpleWorldFrame());
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer-methods -----------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	
	/**
	 * update new worldframes in a fixed interval.
	 */
	private class Updater implements Runnable
	{
		private boolean	running	= false;
		
		
		@Override
		public void run()
		{
			running = true;
			while (running)
			{
				ThreadUtil.parkNanosSafe((long) (1e9f / UserConfig.getVisualizerWpUpdateRate()));
				updateWorldframe(lastWorldFrameWrapper);
				panel.getFieldPanel().updateAiFrame(null);
				if (lastRecordFrameYellow != null)
				{
					panel.getFieldPanel().updateAiFrame(lastRecordFrameYellow);
				}
				if (lastRecordFrameBlue != null)
				{
					panel.getFieldPanel().updateAiFrame(lastRecordFrameBlue);
				}
				panel.getFieldPanel().updateRefereeMsg(lastRefereeMsg);
				panel.getFieldPanel().repaint();
			}
			panel.getRobotsPanel().clearView();
		}
	}
}
