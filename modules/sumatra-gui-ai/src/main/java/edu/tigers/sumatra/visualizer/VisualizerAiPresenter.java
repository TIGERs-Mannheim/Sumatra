/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;


import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.IVisualizationFrameObserver;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.IBotManagerObserver;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.guinotifications.GuiNotificationsController;
import edu.tigers.sumatra.guinotifications.visualizer.IVisualizerObserver;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.pathfinder.MovementCon;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.SingleTouchKickSkill;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import lombok.extern.log4j.Log4j2;

import java.awt.event.MouseEvent;


/**
 * Extended presenter for the visualizer with AI specific stuff.
 */
@Log4j2
public class VisualizerAiPresenter extends VisualizerPresenter
		implements IVisualizationFrameObserver, IBotManagerObserver
{
	@Configurable(comment = "Enter penalty area when moving bot with point'n click", defValue = "true")
	private static boolean moveToPenAreaAllowed = true;

	@Configurable(comment = "Use fastPosMove for point'n click", defValue = "false")
	private static boolean useFastPosMove = false;

	@Configurable(comment = "Ball is obstacle for point'n click", defValue = "true")
	private static boolean ballObstacle = true;

	static
	{
		ConfigRegistration.registerClass("user", VisualizerAiPresenter.class);
	}

	private ASkillSystem skillsystem = null;
	private ABotManager botManager = null;
	private IVisualizerObserver visObserver = null;
	private boolean mouseMoveUpdateDestinationMode = false;
	private MoveToSkill skill;


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
		ABot bot = botManager.getBots().get(botId);
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
	public void onMouseMoved(final IVector2 pos, final MouseEvent e)
	{
		if (mouseMoveUpdateDestinationMode && skill != null)
		{
			if (Geometry.getNegativeHalfTeam() != getSelectedRobotId().getTeamColor())
			{
				skill.updateDestination(Vector2.fromXY(-pos.x(), -pos.y()));
			} else
			{
				skill.updateDestination(pos);
			}
		}
	}


	@Override
	public void onRobotSelected(final IVector2 posIn, final MouseEvent e)
	{
		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean alt = e.isAltDown();
		BotID selectedRobotId = getSelectedRobotId();

		if (skillsystem != null && !selectedRobotId.isUninitializedID())
		{
			skill = MoveToSkill.createMoveToSkill();
			final MovementCon moveCon = skill.getMoveCon();
			moveCon.setPenaltyAreaOurObstacle(!moveToPenAreaAllowed);
			moveCon.setPenaltyAreaTheirObstacle(!moveToPenAreaAllowed);
			skill.getMoveConstraints().setFastMove(useFastPosMove);
			moveCon.setBallObstacle(ballObstacle);

			final DynamicPosition ballPos = new DynamicPosition(BallID.instance());
			IVector2 pos = posIn;
			if (Geometry.getNegativeHalfTeam() != selectedRobotId.getTeamColor())
			{
				pos = Vector2.fromXY(-posIn.x(), -posIn.y());
			}

			mouseMoveUpdateDestinationMode = false;
			if (getPanel().getRobotsPanel().getBotStatus(selectedRobotId).isVisible())
			{
				handleClickMovement(ctrl, shift, alt, pos, selectedRobotId, ballPos);
			}
		}
	}


	private void handleClickMovement(
			boolean ctrl,
			boolean shift,
			boolean alt,
			IVector2 pos,
			BotID selectedRobotId,
			DynamicPosition ballPos
	)
	{
		if (ctrl && shift)
		{
			mouseMoveUpdateDestinationMode = true;
			skill.updateDestination(pos);
			skillsystem.execute(selectedRobotId, skill);
		} else if (ctrl)
		{
			// move there and look at the ball
			skill.updateDestination(pos);
			skill.updateLookAtTarget(ballPos);
			skillsystem.execute(selectedRobotId, skill);
		} else if (shift)
		{
			KickParams kickParams = alt ? KickParams.maxChip() : KickParams.maxStraight();
			SingleTouchKickSkill kickSkill = new SingleTouchKickSkill(pos, kickParams);
			skillsystem.execute(selectedRobotId, kickSkill);
		} else
		{
			skill.updateDestination(pos);
			skillsystem.execute(selectedRobotId, skill);

			if (visObserver != null)
			{
				visObserver.onMoveClick(selectedRobotId, pos);
			}
		}
	}


	private void activate()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			SumatraModel.getInstance().getModule(AAgent.class)
					.addVisObserver(this);
		}
		if (SumatraModel.getInstance().isModuleLoaded(ABotManager.class))
		{
			botManager = SumatraModel.getInstance().getModule(ABotManager.class);
			botManager.addObserver(this);
			for (ABot bot : botManager.getBots().values())
			{
				onBotAdded(bot);
			}
		}

		if (SumatraModel.getInstance().isModuleLoaded(ASkillSystem.class))
		{
			skillsystem = SumatraModel.getInstance().getModule(ASkillSystem.class);
		}

		if (SumatraModel.getInstance().isModuleLoaded(GuiNotificationsController.class))
		{
			visObserver = SumatraModel.getInstance().getModule(GuiNotificationsController.class);
		}
	}


	private void deactivate()
	{
		if (SumatraModel.getInstance().isModuleLoaded(AAgent.class))
		{
			SumatraModel.getInstance().getModule(AAgent.class)
					.removeVisObserver(this);
		}

		if (botManager != null)
		{
			botManager.removeObserver(this);
		}

		skillsystem = null;
		botManager = null;
		visObserver = null;
	}


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		if (state == ModulesState.ACTIVE)
		{
			activate();
		} else if (state == ModulesState.RESOLVED)
		{
			deactivate();
		}
	}


	@Override
	public void updateRobotsPanel()
	{
		if (botManager != null)
		{
			for (ABot bot : botManager.getBots().values())
			{
				onBotAdded(bot);
			}
		}
		super.updateRobotsPanel();
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
		status.setHideRcm(bot.isHideFromRcm());
		status.setBotFeatures(bot.getBotFeatures());
		status.setRobotMode(bot.getRobotMode());
	}


	private void onBotRemovedInternal(final IBot bot)
	{
		BotStatus status = getPanel().getRobotsPanel().getBotStatus(bot.getBotId());
		status.setBatRel(0);
		status.setConnected(false);
		status.setKickerRel(0);
	}
}
