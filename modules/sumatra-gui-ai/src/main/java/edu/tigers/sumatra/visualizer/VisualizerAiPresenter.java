/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;


import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.autoref.presenter.VisualizerRefPresenter;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;
import edu.tigers.sumatra.guinotifications.visualizer.IVisualizerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.AMoveSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickChillSkill;
import edu.tigers.sumatra.visualizer.view.BotStatus;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisualizerAiPresenter extends VisualizerRefPresenter
		implements IVisualizationFrameObserver, IBotManagerObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(VisualizerAiPresenter.class.getName());
	
	@Configurable(comment = "Enter penalty area when moving bot with point'n click")
	private static boolean moveToPenAreaAllowed = false;
	
	@Configurable(comment = "Use fastPosMove for point'n click", defValue = "false")
	private static boolean useFastPosMove = false;
	
	static
	{
		ConfigRegistration.registerClass("gui", VisualizerAiPresenter.class);
	}
	
	private final Map<EAiTeam, VisualizationFrame> visFrames = new EnumMap<>(EAiTeam.class);
	private MovementCon moveCon = new MovementCon();
	private ASkillSystem skillsystem = null;
	private ABotManager botManager = null;
	private AWorldPredictor wp = null;
	private IVisualizerObserver visObserver = null;
	private boolean mouseMoveUpdateDestinationMode = false;
	
	
	@Override
	public void onRobotClick(final BotID botId)
	{
		super.onRobotClick(botId);
		mouseMoveUpdateDestinationMode = false;
		
		if (visObserver != null)
		{
			visObserver.onRobotClick(botId);
		}
	}
	
	
	@Override
	public void onHideBotFromRcmClicked(final BotID botId, final boolean hide)
	{
		ABot bot = botManager.getBotTable().get(botId);
		if (bot == null)
		{
			log.error("Bot with id " + botId + " does not exist.");
		} else
		{
			bot.setHideFromRcm(hide);
			if (visObserver != null)
			{
				visObserver.onHideFromRcm(botId, hide);
			}
		}
	}
	
	
	@Override
	public void onBotAiAssignmentChanged(final BotID botID, final EAiType aiAssignment)
	{
		if (wp == null)
		{
			return;
		}
		wp.updateBot2AiAssignment(botID, aiAssignment);
	}
	
	
	@Override
	public void onMouseMoved(final IVector2 pos, final MouseEvent e)
	{
		if (mouseMoveUpdateDestinationMode)
		{
			if (TeamConfig.getLeftTeam() != getSelectedRobotId().getTeamColor())
			{
				moveCon.updateDestination(Vector2.fromXY(-pos.x(), -pos.y()));
			} else
			{
				moveCon.updateDestination(pos);
			}
		}
	}
	
	
	@Override
	public void onRobotSelected(final IVector2 posIn, final MouseEvent e)
	{
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		BotID selectedRobotId = getSelectedRobotId();
		WorldFrameWrapper lastWorldFrameWrapper = getLastWorldFrameWrapper();
		
		if (!selectedRobotId.isUninitializedID())
		{
			AMoveSkill skill = AMoveToSkill.createMoveToSkill();
			moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowedOur(moveToPenAreaAllowed);
			moveCon.setPenaltyAreaAllowedTheir(moveToPenAreaAllowed);
			moveCon.setIgnoreGameStateObstacles(true);
			moveCon.setFastPosMode(useFastPosMove);
			
			final IVector2 pos;
			final IVector2 ballPos;
			if (TeamConfig.getLeftTeam() != selectedRobotId.getTeamColor())
			{
				pos = Vector2.fromXY(-posIn.x(), -posIn.y());
				IVector2 b = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
				ballPos = Vector2.fromXY(-b.x(), -b.y());
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
				KickChillSkill kickSkill = new KickChillSkill(new DynamicPosition(pos));
				kickSkill.setKickMode(EKickMode.POINT);
				skillsystem.execute(selectedRobotId, kickSkill);
			} else
			{
				moveCon.updateDestination(pos);
				skillsystem.execute(selectedRobotId, skill);
				
				if (visObserver != null)
				{
					visObserver.onMoveClick(selectedRobotId, pos);
				}
			}
		}
	}
	
	
	private void activate()
	{
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.addVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
		
		try
		{
			botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			botManager.addObserver(this);
			for (ABot bot : botManager.getAllBots().values())
			{
				onBotAdded(bot);
			}
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get BM module", err);
		}
		
		try
		{
			skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
		} catch (final ModuleNotFoundException err)
		{
			log.error("no skillsystem found!!!", err);
		}
		
		try
		{
			visObserver = (IVisualizerObserver) SumatraModel.getInstance().getModule("gui_notifications_controller");
		} catch (ModuleNotFoundException err)
		{
			log.warn(
					"Could not get GuiNotificationController. The visualizer will work as expected but other modules may depend on this controller.",
					err);
		}
		
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get wp module", err);
		}
	}
	
	
	private void deactivate()
	{
		try
		{
			AAgent agent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
			agent.removeVisObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get agent module", err);
		}
		
		try
		{
			ABotManager bm = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			bm.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not get BM module", err);
		}
		if (botManager != null)
		{
			botManager.removeObserver(this);
		}
		
		skillsystem = null;
		botManager = null;
		visObserver = null;
		wp = null;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				activate();
				break;
			case RESOLVED:
				deactivate();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	@Override
	public void updateRobotsPanel()
	{
		if (botManager != null)
		{
			for (ABot bot : botManager.getAllBots().values())
			{
				onBotAdded(bot);
			}
		}
		super.updateRobotsPanel();
	}
	
	
	@Override
	protected void updateVisFrameShapes()
	{
		super.updateVisFrameShapes();
		for (EAiTeam aiTeam : EAiTeam.values())
		{
			VisualizationFrame visFrame = visFrames.get(aiTeam);
			if (visFrame == null)
			{
				getPanel().getFieldPanel().clearField(EShapeLayerSource.forAiTeam(aiTeam));
			} else
			{
				for (IShapeLayer sl : visFrame.getShapes().getAllShapeLayers())
				{
					getPanel().getOptionsMenu().addMenuEntry(sl);
				}
				getPanel().getFieldPanel().setShapeMap(EShapeLayerSource.forAiTeam(aiTeam), visFrame.getShapes(),
						visFrame.isInverted());
			}
		}
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		visFrames.put(frame.getAiTeam(), frame);
	}
	
	
	@Override
	public void onClearVisualizationFrame(final EAiTeam team)
	{
		visFrames.clear();
	}
	
	
	@Override
	public void onBotAdded(final ABot bot)
	{
		onBotAddedInternal(bot);
	}
	
	
	@Override
	public void onBotRemoved(final ABot bot)
	{
		onBotRemovedInternal(bot);
	}
	
	
	private void onBotAddedInternal(final IBot bot)
	{
		BotStatus status = getPanel().getRobotsPanel().getBotStatus(bot.getBotId());
		status.setBatRel(bot.getBatteryRelative());
		status.setConnected(true);
		status.setKickerRel(bot.getKickerLevel() / bot.getKickerLevelMax());
		status.setHideAi(bot.isHideFromAi());
		status.setHideRcm(bot.isHideFromRcm());
		status.setBotFeatures(bot.getBotFeatures());
		status.setRobotMode(bot.getRobotMode());
		if (wp == null)
		{
			status.setAiAssignment(EAiType.NONE);
		} else
		{
			status.setAiAssignment(wp.getBotToAiMap().get(bot.getBotId()));
		}
	}
	
	
	private void onBotRemovedInternal(final IBot bot)
	{
		BotStatus status = getPanel().getRobotsPanel().getBotStatus(bot.getBotId());
		status.setBatRel(0);
		status.setConnected(false);
		status.setKickerRel(0);
	}
}
