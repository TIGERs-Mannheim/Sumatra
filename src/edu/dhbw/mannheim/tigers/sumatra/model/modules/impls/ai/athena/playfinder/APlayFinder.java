/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 8, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.IApollonControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This APlayFinder is an abstract Playfinder, which implements some common features, that all PlayFinders can use
 * 
 * @author andres
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class APlayFinder implements IModuliStateObserver, IApollonControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log			= Logger.getLogger(APlayFinder.class.getName());
	
	private final PlayFactory		playFactory	= PlayFactory.getInstance();
	
	/** remember that there is no ball */
	private boolean					noBall		= false;
	
	private Stage						stage;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onFirstFrame(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onNoBall(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
		if (frame.refereeMsg != null)
		{
			noBall = false;
		}
		if (!noBall)
		{
			noBall = true;
			log.warn("There is no ball. Put the ball pack to the field.");
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onChangedToMatchMode(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onEverythingIsNormal(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.addAll(preFrame.playStrategy.getActivePlays());
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onNoRefereeCmd(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * Selects a Keeper play.
	 * @param frame
	 * @param preFrame
	 * @param plays
	 */
	public void onKeeperSelection(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		APlay keeper = playFactory.createPlay(EPlay.KEEPER_SOLO, frame, 1);
		keeper.setSelectionReason(ESelectionReason.MANUEL);
		plays.add(keeper);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	protected void reactOnTooFewBots(final AIInfoFrame frame, final AIInfoFrame preFrame, List<APlay> plays)
	{
		// delete previous selected plays to ensure that there are not more roles assigned than available
		plays.clear();
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 0:
				// Risiko!
				return;
			case 1:
				onKeeperSelection(frame, preFrame, plays);
				break;
			case 2:
				onKeeperSelection(frame, preFrame, plays);
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 3:
				onKeeperSelection(frame, preFrame, plays);
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			default:
				log.error("This method should not be called for more than 3 bots!");
		}
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeHalt(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * Implement this method to process any finished plays (successful or failed).
	 * This is just a notification, you do not have to find new plays here!
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param finishedPlays list of finished plays. Do not add plays!
	 */
	public void onFinishedPlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> finishedPlays)
	{
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeStop(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if ((stage != null))
		{
			if ((stage == Stage.PENALTY_SHOOTOUT))
			{
				plays.addAll(preFrame.playStrategy.getActivePlays());
				applyPlaySelectedByRefereeCmd(plays);
				return;
			}
		}
		
		onKeeperSelection(frame, preFrame, plays);
		plays.add(playFactory.createPlay(EPlay.STOP_MOVE, frame, 2));
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				plays.add(playFactory.createPlay(EPlay.STOP_MARKER, frame, 1));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			case 4:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeKickoffTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		onKeeperSelection(frame, preFrame, plays);
		plays.add(playFactory.createPlay(EPlay.KICK_OFF_CHIP, frame, 3));
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 4:
				plays.clear();
				onKeeperSelection(frame, preFrame, plays);
				plays.add(playFactory.createPlay(EPlay.KICK_OFF_CHIP, frame, 2));
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 3:
				plays.clear();
				onKeeperSelection(frame, preFrame, plays);
				plays.add(playFactory.createPlay(EPlay.KICK_OFF_CHIP, frame, 1));
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 2:
				plays.clear();
				onKeeperSelection(frame, preFrame, plays);
				plays.add(playFactory.createPlay(EPlay.KICK_OFF_CHIP, frame, 1));
				break;
			case 1:
				// only one bot, so forget other decisions and add the last bot as kicker
				plays.clear();
				plays.add(playFactory.createPlay(EPlay.KICK_OFF_CHIP, frame, 1));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeKickoffEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		onKeeperSelection(frame, preFrame, plays);
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 3));
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 2));
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			case 4:
				plays.add(playFactory.createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame, 2));
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereePenaltyTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if ((stage != null))
		{
			if ((stage == Stage.PENALTY_SHOOTOUT))
			{
				plays.add(playFactory.createPlay(EPlay.PENALTY_SHOOTOUT_US, frame));
			} else
			{
				plays.add(playFactory.createPlay(EPlay.PENALTY_US, frame));
			}
		} else
		{
			plays.add(playFactory.createPlay(EPlay.PENALTY_THEM, frame));
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereePenaltyEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if ((stage != null))
		{
			if ((stage == Stage.PENALTY_SHOOTOUT))
			{
				plays.add(playFactory.createPlay(EPlay.PENALTY_SHOOTOUT_THEM, frame));
			} else
			{
				plays.add(playFactory.createPlay(EPlay.PENALTY_THEM, frame));
			}
		} else
		{
			plays.add(playFactory.createPlay(EPlay.PENALTY_THEM, frame));
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeFreeKickTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		// onRefereeIndirectFreeKickTigers(frame, preFrame, plays);
		// onKeeperSelection(frame, preFrame, plays);
		// plays.add(playFactory.createPlay(EPlay.DIRECT_SHOTV2, frame, 1));
		//
		// switch (frame.worldFrame.tigerBotsAvailable.size())
		// {
		// case 6:
		// plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
		// plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 2));
		// break;
		// case 5:
		// plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
		// plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 2));
		// break;
		// case 4:
		// plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
		// plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
		// break;
		// case 3:
		// plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
		// break;
		// case 2:
		// break;
		// case 1:
		// // only one bot, so forget other decisions and add the last bot as kicker
		// plays.clear();
		// plays.add(playFactory.createPlay(EPlay.INDIRECT_SHOTV2, frame, 1));
		// break;
		// default:
		// reactOnTooFewBots(frame, preFrame, plays);
		// }
		// applyPlaySelectedByRefereeCmd(plays);
		
		// TODO DanielAl
		onKeeperSelection(frame, preFrame, plays);
		plays.add(playFactory.createPlay(EPlay.FLYING_TIGER, frame, 2));
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
				break;
			case 4:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 3:
				break;
			case 2:
				plays.clear();
				plays.add(playFactory.createPlay(EPlay.INDIRECT_SHOTV2, frame, 2));
				break;
			case 1:
				plays.clear();
				plays.add(playFactory.createPlay(EPlay.KEEPER_SOLO, frame, 2));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeFreeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		freeKickEnemies(frame, preFrame, plays);
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeIndirectFreeKickTigers(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		onKeeperSelection(frame, preFrame, plays);
		plays.add(playFactory.createPlay(EPlay.INDIRECT_SHOTV2, frame, 2));
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
				break;
			case 4:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			case 3:
				break;
			case 2:
				plays.clear();
				plays.add(playFactory.createPlay(EPlay.INDIRECT_SHOTV2, frame, 2));
				break;
			case 1:
				plays.clear();
				plays.add(playFactory.createPlay(EPlay.KEEPER_SOLO, frame, 2));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onRefereeIndirectFreeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		freeKickEnemies(frame, preFrame, plays);
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	protected void freeKickEnemies(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		onKeeperSelection(frame, preFrame, plays);
		plays.add(playFactory.createPlay(EPlay.FREEKICK_MOVE, frame, 2));
		
		switch (frame.worldFrame.tigerBotsAvailable.size())
		{
			case 6:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				plays.add(playFactory.createPlay(EPlay.BREAK_CLEAR, frame, 1));
				break;
			case 5:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 2));
				break;
			case 4:
				plays.add(playFactory.createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame, 1));
				break;
			default:
				reactOnTooFewBots(frame, preFrame, plays);
		}
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public abstract void onNewDecision(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays);
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 * @param stage
	 * @return reacted on stage (plays were added)
	 */
	public boolean onGameStageChanged(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays, SSL_Referee.Stage stage)
	{
		this.stage = stage;
		switch (stage)
		{
			case NORMAL_FIRST_HALF:
			case NORMAL_SECOND_HALF:
			case EXTRA_FIRST_HALF:
			case EXTRA_SECOND_HALF:
			case PENALTY_SHOOTOUT:
				break;
			case NORMAL_FIRST_HALF_PRE:
			case NORMAL_SECOND_HALF_PRE:
			case EXTRA_FIRST_HALF_PRE:
			case EXTRA_SECOND_HALF_PRE:
				plays.add(playFactory.createPlay(EPlay.INIT, frame));
				applyPlaySelectedByRefereeCmd(plays);
				return true;
			case POST_GAME:
			case NORMAL_HALF_TIME:
			case EXTRA_TIME_BREAK:
			case EXTRA_HALF_TIME:
			case PENALTY_SHOOTOUT_BREAK:
				plays.add(playFactory.createPlay(EPlay.MAINTENANCE, frame));
				applyPlaySelectedByRefereeCmd(plays);
				return true;
			default:
				break;
		}
		return false;
	}
	
	
	/**
	 * Goal for Yellow
	 * @param frame
	 * @param preFrame
	 * @param plays
	 */
	public void onRefereeGoalYellow(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.INIT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * Goal scored for blue
	 * @param frame
	 * @param preFrame
	 * @param plays
	 */
	public void onRefereeGoalBlue(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.INIT, frame));
		applyPlaySelectedByRefereeCmd(plays);
	}
	
	
	/**
	 * 
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of frame
	 */
	public void onTimeout(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		plays.add(playFactory.createPlay(EPlay.HALT, frame));
	}
	
	
	/**
	 * The playFinder will be notified when the state of moduli changes.
	 */
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		
	}
	
	
	/**
	 * override this to react on a possible scored goal
	 * 
	 * @param frame
	 * @param preFrame
	 * @param possibleGoal
	 */
	public void onPossibleGoalScored(AIInfoFrame frame, AIInfoFrame preFrame, EPossibleGoal possibleGoal)
	{
		// do nothing on default
	}
	
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		// do nothing on default
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		// do nothing on default
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	protected PlayFactory getPlayFactory()
	{
		return playFactory;
	}
	
	
	/**
	 * @return the stage
	 */
	public Stage getStage()
	{
		return stage;
	}
	
	
	/**
	 * used for mixed team playfinder
	 * @param b
	 */
	public void setMixedTeam(boolean b)
	{
	}
	
	
	protected void applyPlaySelectedByRefereeCmd(List<APlay> plays)
	{
		for (APlay play : plays)
		{
			if ((play != null) && (play.getType() != null) && !play.getType().equals(EPlay.KEEPER_SOLO))
			{
				play.setSelectionReason(ESelectionReason.REFEREE);
			}
		}
	}
}
