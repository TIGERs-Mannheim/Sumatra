/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.botmanager.commands.other.ESong;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author MarkG
 */
public class MultimediaCalc extends ACalculator
{
	private static final long TOGGLE_TIME = 550_000_000L;
	
	@Configurable(comment = "time in seconds", defValue = "3.0")
	private static double cheeringStopTimer = 3.0;
	
	
	private long toggleTimer = 0;
	
	private int timeoutLedStatus = 0;
	private boolean toggler = false;
	
	private long cheeringTimer = 0;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (toggleTimer == 0)
		{
			toggleTimer = baseAiFrame.getWorldFrame().getTimestamp();
		}
		Map<BotID, MultimediaControl> multimediaControls = new HashMap<>();
		
		GameState gameState = newTacticalField.getGameState();
		
		if (gameState.getState() == EGameState.TIMEOUT)
		{
			setTimeoutLeds(newTacticalField, baseAiFrame, multimediaControls);
		} else
		{
			setLedsByPlay(baseAiFrame, multimediaControls);
		}
		
		playSongWhenInsane(baseAiFrame, multimediaControls);
		
		cheerWhenWeShootAGoal(multimediaControls);
		newTacticalField.setMultimediaControl(multimediaControls);
	}
	
	
	private void playSongWhenInsane(
			final BaseAiFrame baseAiFrame,
			final Map<BotID, MultimediaControl> multimediaControls)
	{
		boolean insane = OffensiveMath.isKeeperInsane(getBall(), getAiFrame().getGamestate());
		for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
		{
			final MultimediaControl control = multimediaControls.getOrDefault(key,
					new MultimediaControl());
			if (insane)
			{
				control.setSong(ESong.FINAL_COUNTDOWN);
			} else if (control.getSong() == ESong.FINAL_COUNTDOWN)
			{
				control.setSong(ESong.NONE);
			}
			multimediaControls.put(key, control);
		}
	}
	
	
	private void setLedsByPlay(final BaseAiFrame baseAiFrame, final Map<BotID, MultimediaControl> multimediaControls)
	{
		List<APlay> plays = new ArrayList<>(baseAiFrame.getPrevFrame().getPlayStrategy().getActivePlays());
		for (APlay play : plays)
		{
			for (ARole role : play.getRoles())
			{
				final MultimediaControl control = multimediaControls.getOrDefault(role.getBotID(),
						new MultimediaControl());
				setLedByPlay(play, control);
				multimediaControls.put(role.getBotID(), control);
			}
		}
	}
	
	
	private void setTimeoutLeds(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			final Map<BotID, MultimediaControl> multimediaControls)
	{
		List<ARole> roles = new ArrayList<>(baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles().values());
		roles.sort(Comparator.comparing(ARole::getBotID));
		int i = 0;
		for (ARole role : roles)
		{
			final MultimediaControl control = multimediaControls.getOrDefault(role.getBotID(),
					new MultimediaControl());
			
			switch (newTacticalField.getGameState().getState())
			{
				case TIMEOUT:
				case HALT:
					handleTimeout(baseAiFrame, i++, control);
					break;
				default:
					break;
			}
			
			multimediaControls.put(role.getBotID(), control);
		}
	}
	
	
	private void setLedByPlay(final APlay play, final MultimediaControl control)
	{
		if (play.getType() == EPlay.OFFENSIVE)
		{
			control.setLeftGreen(false);
			control.setLeftRed(true);
			control.setRightGreen(false);
			control.setRightRed(true);
		} else if (play.getType() == EPlay.SUPPORT)
		{
			control.setLeftGreen(true);
			control.setLeftRed(true);
			control.setRightGreen(true);
			control.setRightRed(true);
		} else if (play.getType() == EPlay.DEFENSIVE)
		{
			control.setLeftGreen(true);
			control.setLeftRed(false);
			control.setRightGreen(true);
			control.setRightRed(false);
		} else
		{
			control.setLeftGreen(false);
			control.setLeftRed(false);
			control.setRightGreen(false);
			control.setRightRed(false);
		}
	}
	
	
	private void handleTimeout(final BaseAiFrame baseAiFrame, final int i, final MultimediaControl control)
	{
		if (timeoutLedStatus == i)
		{
			if (!toggler)
			{
				toggler = true;
				control.setLeftGreen(false);
				control.setLeftRed(false);
				control.setRightGreen(false);
				control.setRightRed(true);
			} else
			{
				toggler = false;
				control.setLeftGreen(false);
				control.setLeftRed(true);
				control.setRightGreen(false);
				control.setRightRed(false);
			}
		} else
		{
			control.setLeftGreen(false);
			control.setLeftRed(false);
			control.setRightGreen(false);
			control.setRightRed(false);
		}
		if (((baseAiFrame.getWorldFrame().getTimestamp() - toggleTimer) > TOGGLE_TIME) && (!toggler))
		{
			timeoutLedStatus++;
			if (timeoutLedStatus == baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles().size())
			{
				timeoutLedStatus = 0;
			}
			toggleTimer = baseAiFrame.getWorldFrame().getTimestamp();
		}
	}
	
	
	private void cheerWhenWeShootAGoal(final Map<BotID, MultimediaControl> multimediaControls)
	{
		if (getAiFrame().isNewRefereeMsg())
		{
			ETeamColor color = getWFrame().getTeamColor();
			if ((color == ETeamColor.YELLOW && getAiFrame().getRefereeMsg().getCommand() == Command.GOAL_YELLOW) ||
					(color == ETeamColor.BLUE && getAiFrame().getRefereeMsg().getCommand() == Command.GOAL_BLUE))
			{
				cheeringTimer = getWFrame().getTimestamp();
				for (BotID key : getWFrame().tigerBotsAvailable.keySet())
				{
					multimediaControls.putIfAbsent(key, new MultimediaControl());
					multimediaControls.get(key).setSong(ESong.CHEERING);
				}
			}
		}
		if ((((getWFrame().getTimestamp() - cheeringTimer) * 1e-9) > cheeringStopTimer)
				&& (cheeringTimer != 0))
		{
			for (BotID key : getWFrame().tigerBotsAvailable.keySet())
			{
				multimediaControls.putIfAbsent(key, new MultimediaControl());
				multimediaControls.get(key).setSong(ESong.NONE);
			}
			cheeringTimer = 0;
		}
	}
}
