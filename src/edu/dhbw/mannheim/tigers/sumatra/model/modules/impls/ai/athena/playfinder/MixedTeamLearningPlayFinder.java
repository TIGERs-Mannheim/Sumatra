/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2013
 * Author(s): Daniel Andres <andreslopez.daniel@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;


/**
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class MixedTeamLearningPlayFinder extends LearningPlayFinder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private MixedTeamHelper	mixedTeam	= new MixedTeamHelper(true);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onKeeperSelection(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			if (mixedTeam.needsKeeper(frame))
			{
				super.onKeeperSelection(frame, preFrame, plays);
			}
		} else
		{
			super.onKeeperSelection(frame, preFrame, plays);
		}
	}
	
	
	@Override
	protected void reactOnTooFewBots(final AIInfoFrame frame, final AIInfoFrame preFrame, List<APlay> plays)
	{
		// nothing to do because with 0 bots nothing will be selected. case 3,2,1 are different for each method
	}
	
	
	@Override
	public void onRefereeStop(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			if ((botsLeft > 0) && mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			
			if ((botsLeft > 0) && mixedTeam.tigerIsNearThenOtherTeam(frame))
			{
				plays.add(getPlayFactory().createPlay(EPlay.STOP_MOVE, frame, 2));
				botsLeft -= 2;
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 2));
				botsLeft -= 2;
			}
			
			switch (botsLeft)
			{
				case 2:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					break;
				case 1:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				default:
					break;
			}
		} else
		{
			super.onRefereeStop(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeKickoffTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			boolean weKickOff = false;
			if ((botsLeft > 0) && mixedTeam.tigerIsNearThenOtherTeam(frame))
			{
				plays.add(getPlayFactory().createPlay(EPlay.KICK_OFF_CHIP, frame, 1));
				weKickOff = true;
				botsLeft--;
			}
			if ((botsLeft > 0) && mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			
			switch (botsLeft)
			{
				case 4:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 2));
					break;
				case 3:
					
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					if (weKickOff)
					{
						// STOP Move 50 cm circle bigger TODO
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MOVE, frame, 1));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 1));
					}
					break;
				case 2:
					if (weKickOff)
					{
						plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MOVE, frame, 1));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					}
					break;
				case 1:
					if (weKickOff)
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MOVE, frame, 1));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					}
					break;
				default:
					break;
			}
		} else
		{
			super.onRefereeKickoffTigers(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeKickoffEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			if (mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			
			boolean weAreNearerAtBall = mixedTeam.tigerIsNearThenOtherTeam(frame);
			switch (botsLeft)
			{
				case 4:
					if (weAreNearerAtBall)
					{
						plays.add(getPlayFactory().createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 2));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 2));
					}
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					break;
				case 3:
					if (weAreNearerAtBall)
					{
						plays.add(getPlayFactory().createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 2));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 2));
					}
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				case 2:
					if (weAreNearerAtBall)
					{
						plays.add(getPlayFactory().createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 1));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 1));
					}
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				case 1:
					if (weAreNearerAtBall)
					{
						plays.add(getPlayFactory().createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 1));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.STOP_MARKER, frame, 1));
					}
					break;
				default:
					break;
			}
		} else
		{
			super.onRefereeFreeKickEnemies(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereePenaltyTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			if ((getStage() != null))
			{
				if (mixedTeam.tigerIsNearThenOtherTeam(frame))
				{
					if ((getStage() == Stage.PENALTY_SHOOTOUT))
					{
						plays.add(getPlayFactory().createPlay(EPlay.PENALTY_SHOOTOUT_US, frame));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.PENALTY_US, frame));
					}
				} else
				{
					// TODO other should have penalty. position our bots correct and not only maintenance
					plays.add(getPlayFactory().createPlay(EPlay.INIT, frame));
				}
			}
		} else
		{
			super.onRefereePenaltyTigers(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereePenaltyEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			if ((getStage() != null))
			{
				if (mixedTeam.needsKeeper(frame))
				{
					if ((getStage() == Stage.PENALTY_SHOOTOUT))
					{
						plays.add(getPlayFactory().createPlay(EPlay.PENALTY_SHOOTOUT_THEM, frame));
					} else
					{
						plays.add(getPlayFactory().createPlay(EPlay.PENALTY_THEM, frame));
					}
				} else
				{
					// TODO other should defend penalty. position our bots correct and not only maintenance
					plays.add(getPlayFactory().createPlay(EPlay.MAINTENANCE, frame));
				}
			}
		} else
		{
			super.onRefereePenaltyEnemies(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeFreeKickTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			if (mixedTeam.tigerIsNearThenOtherTeam(frame))
			{
				plays.add(getPlayFactory().createPlay(EPlay.PASSER_MIXED, frame, 1));
				botsLeft--;
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.INDIRECT_RECEIVER_MIXED, frame, 1));
				botsLeft--;
			}
			if (mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			
			switch (botsLeft)
			{
				case 4:
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					break;
				case 3:
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				case 2:
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 1));
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				case 1:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				default:
					break;
			}
		} else
		{
			super.onRefereeFreeKickTigers(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeFreeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			freeKickEnemies(frame, preFrame, plays);
		} else
		{
			super.onRefereeFreeKickEnemies(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeIndirectFreeKickTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			if (botsLeft >= 1)
			{
				plays.add(getPlayFactory().createPlay(EPlay.PASSER_MIXED, frame, 1));
				botsLeft--;
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.INDIRECT_RECEIVER_MIXED, frame, 1));
				botsLeft--;
			}
			if (mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			
			switch (botsLeft)
			{
				case 4:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 2));
					break;
				case 3:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 2));
					break;
				case 2:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame, 1));
					break;
				case 1:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				default:
					break;
			}
		} else
		{
			super.onRefereeIndirectFreeKickTigers(frame, preFrame, plays);
		}
	}
	
	
	@Override
	public void onRefereeIndirectFreeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			freeKickEnemies(frame, preFrame, plays);
		} else
		{
			super.onRefereeIndirectFreeKickEnemies(frame, preFrame, plays);
		}
	}
	
	
	@Override
	protected void freeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (mixedTeam.isMixedTeam())
		{
			int botsLeft = frame.worldFrame.tigerBotsAvailable.size();
			if (mixedTeam.needsKeeper(frame))
			{
				onKeeperSelection(frame, preFrame, plays);
				botsLeft--;
			}
			if (mixedTeam.tigerIsNearThenOtherTeam(frame))
			{
				if (botsLeft >= 2)
				{
					plays.add(getPlayFactory().createPlay(EPlay.FREEKICK_MOVE, frame, 2));
					botsLeft -= 2;
				} else if (botsLeft == 1)
				{
					plays.add(getPlayFactory().createPlay(EPlay.FREEKICK_MOVE, frame, 1));
					botsLeft -= 1;
				}
			}
			
			switch (botsLeft)
			{
				case 4:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame, 2));
					break;
				case 3:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame, 1));
					break;
				case 2:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame, 1));
					break;
				case 1:
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
					break;
				default:
					break;
			}
		} else
		{
			super.freeKickEnemies(frame, preFrame, plays);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void setMixedTeam(boolean b)
	{
		mixedTeam.setMixedTeam(b);
	}
}
