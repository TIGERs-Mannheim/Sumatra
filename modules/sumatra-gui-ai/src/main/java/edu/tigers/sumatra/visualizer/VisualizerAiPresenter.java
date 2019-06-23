/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 18, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.visualizer;


import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.autoref.presenter.VisualizerRefPresenter;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.MovementCon;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EKickMode;
import edu.tigers.sumatra.skillsystem.skills.KickSkill.EMoveMode;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisualizerAiPresenter extends VisualizerRefPresenter
		implements IVisualizationFrameObserver, IBotManagerObserver
{
	@SuppressWarnings("unused")
	private static final Logger								log										= Logger
			.getLogger(
					VisualizerAiPresenter.class
							.getName());
	
	private final Map<ETeamColor, VisualizationFrame>	visFrames								= new HashMap<>(2);
	private MovementCon											moveCon									= new MovementCon();
	private ASkillSystem											skillsystem								= null;
	private ABotManager											botManager								= null;
	
	private boolean												mouseMoveUpdateDestinationMode	= false;
	
	
	@Override
	public void onRobotClick(final BotID botId)
	{
		super.onRobotClick(botId);
		mouseMoveUpdateDestinationMode = false;
	}
	
	
	@Override
	public void onHideBotFromAiClicked(final BotID botId, final boolean hide)
	{
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			ABot bot = botManager.getBotTable().get(botId);
			if (bot == null)
			{
				log.error("Bot with id " + botId + " does not exist.");
			} else
			{
				bot.setHideFromAi(hide);
			}
		} catch (ModuleNotFoundException err)
		{
			// ignore
		}
	}
	
	
	@Override
	public void onHideBotFromRcmClicked(final BotID botId, final boolean hide)
	{
		try
		{
			ABotManager botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
			ABot bot = botManager.getBotTable().get(botId);
			if (bot == null)
			{
				log.error("Bot with id " + botId + " does not exist.");
			} else
			{
				bot.setHideFromRcm(hide);
			}
		} catch (ModuleNotFoundException err)
		{
			// ignore
		}
	}
	
	
	@Override
	public void onMouseMoved(final IVector2 pos, final MouseEvent e)
	{
		if (mouseMoveUpdateDestinationMode)
		{
			if (TeamConfig.getLeftTeam() != getSelectedRobotId().getTeamColor())
			{
				moveCon.updateDestination(new Vector2(-pos.x(), -pos.y()));
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
			AMoveToSkill skill = AMoveToSkill.createMoveToSkill();
			moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaAllowedOur(false);
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
				KickSkill kickSkill = new KickSkill(new DynamicPosition(pos));
				kickSkill.setKickMode(EKickMode.POINT);
				kickSkill.setMoveMode(EMoveMode.CHILL);
				skillsystem.execute(selectedRobotId, kickSkill);
			} else
			{
				moveCon.updateDestination(pos);
				skillsystem.execute(selectedRobotId, skill);
			}
		}
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.addVisObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.addVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
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
					log.error("Could not get BM module");
				}
				
				try
				{
					skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
				} catch (final ModuleNotFoundException err)
				{
					log.error("no skillsystem found!!!", err);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				try
				{
					Agent agentYellow = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_YELLOW);
					agentYellow.removeVisObserver(this);
					Agent agentBlue = (Agent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID_BLUE);
					agentBlue.removeVisObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get agent module");
				}
				
				try
				{
					ABotManager bm = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					bm.removeObserver(this);
				} catch (ModuleNotFoundException err)
				{
					log.error("Could not get BM module");
				}
				if (botManager != null)
				{
					botManager.removeObserver(this);
				}
				
				skillsystem = null;
				botManager = null;
				break;
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
		} else if (getLastWorldFrameWrapper() != null)
		{
			IBotIDMap<ITrackedBot> tBotsMap = getLastWorldFrameWrapper().getSimpleWorldFrame().getBots();
			for (ITrackedBot tBot : tBotsMap.values())
			{
				onBotAddedInternal(tBot.getBot());
			}
		}
		super.updateRobotsPanel();
	}
	
	
	@Override
	protected void updateVisFrameShapes()
	{
		super.updateVisFrameShapes();
		for (ETeamColor teamColor : ETeamColor.yellowBlueValues())
		{
			VisualizationFrame visFrame = visFrames.get(teamColor);
			if (visFrame == null)
			{
				getPanel().getFieldPanel().clearField(EShapeLayerSource.forTeamColor(teamColor));
			} else
			{
				for (IShapeLayer sl : visFrame.getShapes().getAllShapeLayers())
				{
					getPanel().getOptionsMenu().addMenuEntry(sl);
				}
				getPanel().getFieldPanel().setShapeMap(EShapeLayerSource.forTeamColor(teamColor), visFrame.getShapes(),
						visFrame.isInverted());
			}
		}
	}
	
	
	@Override
	public void onNewVisualizationFrame(final VisualizationFrame frame)
	{
		visFrames.put(frame.getTeamColor(), frame);
	}
	
	
	@Override
	public void onClearVisualizationFrame(final ETeamColor teamColor)
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
		getPanel().getRobotsPanel().repaint();
	}
	
	
	private void onBotRemovedInternal(final IBot bot)
	{
		BotStatus status = getPanel().getRobotsPanel().getBotStatus(bot.getBotId());
		status.setBatRel(0);
		status.setConnected(false);
		status.setKickerRel(0);
		getPanel().getRobotsPanel().repaint();
	}
}
