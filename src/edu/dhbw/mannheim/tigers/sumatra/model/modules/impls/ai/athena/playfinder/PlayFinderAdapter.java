/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 16, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Stage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.ApollonControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.IApollonControlHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.MatchStatistics;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.IStatisticsObserver;


/**
 * This class handles the different kinds of reactions,
 * that need to be handled by a PlayFinder.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PlayFinderAdapter implements IApollonControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger					log							= Logger.getLogger(PlayFinderAdapter.class
																									.getName());
	private static final String					FULL_STATS					= "fullStats";
	private static final String					MATCH_STATS					= "singleMatchStats";
	
	
	private final EMatchBehavior					matchBehaviour;
	
	/** All referee messages that come in when the ball is out of play are stored here! */
	private final Queue<RefereeMsg>				refereeQueue				= new LinkedList<RefereeMsg>();
	
	private boolean									firstFrame					= true;
	
	private final APlayFinder						playFinder;
	
	private boolean									roleNumberOK				= true;
	
	private boolean									possibleGoalLastFrame	= false;
	
	private Stage										currentStage;
	
	private final MatchStatistics					matchStats;
	private final MatchStatistics					fullStats					= new MatchStatistics(FULL_STATS);
	
	private final List<IStatisticsObserver>	statisticsObservers		= new ArrayList<IStatisticsObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PlayFinderAdapter()
	{
		matchBehaviour = AIConfig.getTactics().getTacticalOrientation();
		playFinder = new MixedTeamLearningPlayFinder();
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		matchStats = new MatchStatistics(MATCH_STATS + "_" + dt.format(new Date()));
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Choose {@link APlay}s according to the current situation
	 * 
	 * @param frame
	 * @param preFrame (never null, Athena cares for this by dropping the very first frame)
	 * @param plays The list the chosen plays should be added to; may very well be the plays-List of currentFrame
	 */
	public void choosePlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		// Pre-operations on AIInfoFrame are done here:
		frame.playStrategy.setMatchBehavior(matchBehaviour);
		
		// -1. New referee message incoming?
		if (frame.refereeMsg != null)
		{
			refereeQueue.add(frame.refereeMsg);
		}
		
		List<APlay> oldPlays = new ArrayList<APlay>();
		oldPlays.addAll(preFrame.playStrategy.getActivePlays());
		
		reactOnSituations(frame, preFrame, plays);
		
		for (APlay oldPlay : oldPlays)
		{
			boolean playChanged = true;
			for (APlay newPlay : plays)
			{
				if (newPlay == oldPlay)
				{
					playChanged = false;
				}
			}
			if (playChanged)
			{
				matchStats.onNewPlays(plays);
				fullStats.onNewPlays(plays);
				break;
			}
		}
		
		// 8. Ok, now there should be at least one play in the result
		if (plays.isEmpty())
		{
			if (!frame.worldFrame.tigerBotsAvailable.isEmpty())
			{
				log.error("The playFinder has chosen no Play! This should not happen!!");
			}
		}
		
		// lets check for the role count, but only if we do not have the HALT play (which has no roles)
		else if ((plays.get(0) != null) && (plays.get(0).getType() != EPlay.HALT))
		{
			int numRoles = calcRoles(plays);
			int desiredRoles = frame.worldFrame.tigerBotsAvailable.size();
			
			if (numRoles > desiredRoles)
			{
				log.fatal("The PlayFinder choose too much roles!! This must not happen. (" + numRoles + ").");
				reportWrongNumberOfRoles(plays);
				do
				{
					plays.remove(plays.size() - 1);
				} while (calcRoles(plays) > desiredRoles);
			} else if (numRoles < desiredRoles)
			{
				// role number was ok last time, so now its an error
				if (roleNumberOK)
				{
					log.warn("The PlayFinder choose too less roles (" + numRoles + ").");
					reportWrongNumberOfRoles(plays);
				}
				roleNumberOK = false;
				// at this point, its clear that we will have too less roles.
				// the play finder should be fixed, if you encounter this
			}
		} else
		{
			roleNumberOK = true;
		}
	}
	
	
	private void reportWrongNumberOfRoles(List<APlay> plays)
	{
		List<ARole> roles = new ArrayList<ARole>();
		for (final APlay play : plays)
		{
			roles.addAll(play.getRoles());
		}
		log.warn("This plays were chosen: " + plays);
		log.warn("This roles were chosen: " + roles);
	}
	
	
	private int calcRoles(List<APlay> plays)
	{
		int numRoles = 0;
		for (final APlay play : plays)
		{
			numRoles += play.getRoleCount();
		}
		return numRoles;
	}
	
	
	private void reactOnSituations(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		// 0. Handles first Frame.
		if (firstFrame)
		{
			playFinder.onFirstFrame(frame, preFrame, plays);
			firstFrame = false;
			return;
		}
		
		// 1. What if there is no ball?
		if (frame.worldFrame.ball.getPos().equals(GeoMath.INIT_VECTOR))
		{
			log.warn("There is no ball!");
			playFinder.onNoBall(frame, preFrame, plays);
			return;
		}
		
		// 2. Referee Message present?
		if (refereeQueue.size() > 0)
		{
			final RefereeMsg msg = refereeQueue.poll();
			reactOnRefereeCmd(msg, frame, preFrame, plays);
			return;
		}
		
		// 3. Has the mode changed back to match mode?
		if (frame.playStrategy.isStateChanged())
		{
			playFinder.onChangedToMatchMode(frame, preFrame, plays);
			return;
		}
		
		
		// 4. Bot Connection state
		if (frame.playStrategy.getBotConnection().isSomethingTrue())
		{
			// this will appear to a playFinder as there would be a new referee cmd
			reactOnRefereeCmd(frame.refereeMsgCached, frame, preFrame, plays);
			return;
		}
		
		
		// 5. Force New Decision wish from the GUI?
		if (checkForceNewDecision(frame, preFrame, plays))
		{
			return;
		}
		
		// 6. Check for finishedPlays
		if (checkForFinishedPlays(frame, preFrame, plays))
		{
			return;
		}
		
		// 7. Everything "normal"
		playFinder.onEverythingIsNormal(frame, preFrame, plays);
	}
	
	
	private boolean checkForceNewDecision(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (frame.playStrategy.isForceNewDecision())
		{
			if (frame.refereeMsgCached.getCommand() == Command.NORMAL_START)
			{
				// ready signal is a special case, because it will not change active plays, so we do this here.
				playFinder.onNewDecision(frame, preFrame, plays);
			} else
			{
				reactOnRefereeCmd(frame.refereeMsgCached, frame, preFrame, plays);
			}
			return true;
		}
		return false;
	}
	
	
	private boolean checkForFinishedPlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		if (!preFrame.playStrategy.getFinishedPlays().isEmpty())
		{
			// Okay, one or more plays have ended.
			// let playFinder know, so he can do something with the plays
			
			matchStats.onFinishedPlays(preFrame.playStrategy.getActivePlays());
			fullStats.onFinishedPlays(preFrame.playStrategy.getActivePlays());
			
			notifyStatistic(matchStats);
			
			playFinder.onFinishedPlays(frame, preFrame, preFrame.playStrategy.getFinishedPlays());
			playFinder.onNewDecision(frame, preFrame, plays);
			
			return true;
		}
		
		if (frame.tacticalInfo.getPossibleGoal() != EPossibleGoal.NO_ONE)
		{
			if (!possibleGoalLastFrame)
			{
				playFinder.onPossibleGoalScored(frame, preFrame, preFrame.tacticalInfo.getPossibleGoal());
			}
			possibleGoalLastFrame = true;
		} else
		{
			possibleGoalLastFrame = false;
		}
		return false;
	}
	
	
	/**
	 * Handle play choice triggerd by an incoming referee message.
	 * All standards-plays can be coded in here hardly!
	 * Make sure your plays added here handle exactly 5 roles!
	 * 
	 * @param msg
	 * @param frame
	 * @param preFrame
	 * @param plays resulting plays
	 * 
	 */
	private void reactOnRefereeCmd(RefereeMsg msg, AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		// if there is no referee cmd, just choose freely
		if (msg == null)
		{
			playFinder.onNoRefereeCmd(frame, preFrame, plays);
			return;
		}
		if (!msg.getStage().equals(currentStage))
		{
			currentStage = msg.getStage();
			log.info("Stage changed to " + currentStage);
			boolean playsAdded = playFinder.onGameStageChanged(frame, preFrame, plays, msg.getStage());
			if (playsAdded)
			{
				return;
			}
		}
		
		switch (msg.getCommand())
		{
		// Force Start ("Schiedsrichter Ball"). After a stopped play, the ball is set
		// free immediatly. A Ball getter should be used here.
			case FORCE_START:
			{
				playFinder.onNewDecision(frame, preFrame, plays);
				break;
			}
			
			// Always when the ball gets out the field, there is a foul or a timeout the game is stopped.
			// Since we do not know at first WHY the game stopped, we just group around the ball and wait
			// for the next, specific referee command.
			case STOP:
			{
				playFinder.onRefereeStop(frame, preFrame, plays);
				break;
			}
			
			case HALT:
			{
				playFinder.onRefereeHalt(frame, preFrame, plays);
				break;
			}
			
			
			// Important case. The 'ready' or 'normal start' command is called when the game
			// get restarted because there was a recess. The recess was triggered by sth. like
			// a penalty or kickoff. So these plays or their follow plays have to handle what
			// will happen when the game is started again!
			// If not, the playfinder will in the next frame.
			case NORMAL_START:
			{
				plays.addAll(preFrame.playStrategy.getActivePlays());
				break;
			}
			case GOAL_BLUE:
				playFinder.onRefereeGoalBlue(frame, preFrame, plays);
				break;
			case GOAL_YELLOW:
				playFinder.onRefereeGoalYellow(frame, preFrame, plays);
				break;
			case DIRECT_FREE_BLUE:
			case DIRECT_FREE_YELLOW:
			case INDIRECT_FREE_BLUE:
			case INDIRECT_FREE_YELLOW:
			case PREPARE_KICKOFF_BLUE:
			case PREPARE_KICKOFF_YELLOW:
			case PREPARE_PENALTY_BLUE:
			case PREPARE_PENALTY_YELLOW:
			case TIMEOUT_BLUE:
			case TIMEOUT_YELLOW:
				reactOnTeamSpecificRefereeCmd(frame, preFrame, plays, msg);
				break;
			default:
				log.error("Referee command not recognized");
				plays.addAll(preFrame.playStrategy.getActivePlays());
				break;
		}
	}
	
	
	private void reactOnTeamSpecificRefereeCmd(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays, RefereeMsg msg)
	{
		
		switch (msg.getTeamSpecRefCmd())
		{
			case DirectFreeKickEnemies:
				playFinder.onRefereeFreeKickEnemies(frame, preFrame, plays);
				break;
			case DirectFreeKickTigers:
				// tigers may kick the ball (without waiting for a READY signal)
				// tigers can directly make a goal!
				playFinder.onRefereeFreeKickTigers(frame, preFrame, plays);
				break;
			case IndirectFreeKickEnemies:
				// Here we need a choice for different Plays depending on the type of the
				// freekick and the position where it is taken.
				// For a first approach we don't need to distinguish between
				// throw-in, corner kick, goal kick...
				playFinder.onRefereeIndirectFreeKickEnemies(frame, preFrame, plays);
				break;
			case IndirectFreeKickTigers:
				// tigers may kick the ball (without waiting for a READY signal)
				// the ball has to touch another player before scoring
				playFinder.onRefereeIndirectFreeKickTigers(frame, preFrame, plays);
				break;
			case KickOffEnemies:
				playFinder.onRefereeKickoffEnemies(frame, preFrame, plays);
				break;
			case KickOffTigers:
				playFinder.onRefereeKickoffTigers(frame, preFrame, plays);
				break;
			case NoCommand:
				break;
			case PenaltyEnemies:
				playFinder.onRefereePenaltyEnemies(frame, preFrame, plays);
				break;
			case PenaltyTigers:
				playFinder.onRefereePenaltyTigers(frame, preFrame, plays);
				break;
			case TimeoutEnemies:
			case TimeoutTigers:
				playFinder.onTimeout(frame, preFrame, plays);
				break;
			default:
				break;
		
		}
	}
	
	
	@Override
	public void onNewApollonControl(ApollonControl newControl)
	{
		playFinder.onNewApollonControl(newControl);
	}
	
	
	@Override
	public void onSaveKnowledgeBase()
	{
		playFinder.onSaveKnowledgeBase();
	}
	
	
	/**
	 * 
	 */
	public void onStop()
	{
		matchStats.save();
		fullStats.save();
	}
	
	
	/**
	 * @param observer
	 */
	public void addStatisticsObserver(IStatisticsObserver observer)
	{
		synchronized (statisticsObservers)
		{
			statisticsObservers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeStatisticsObserver(IStatisticsObserver observer)
	{
		synchronized (statisticsObservers)
		{
			statisticsObservers.remove(observer);
		}
	}
	
	
	private void notifyStatistic(MatchStatistics event)
	{
		synchronized (statisticsObservers)
		{
			for (IStatisticsObserver observer : statisticsObservers)
			{
				observer.onNewStatistics(event);
			}
		}
	}
	
	
	/**
	 * @param b
	 */
	public void setMixedTeam(boolean b)
	{
		playFinder.setMixedTeam(b);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
