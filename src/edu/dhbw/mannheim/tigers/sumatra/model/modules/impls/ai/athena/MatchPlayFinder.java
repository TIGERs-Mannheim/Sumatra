/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay.EPlayState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EMatchBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayFactory;


/**
 * <p>
 * This is the first {@link IPlayFinder}-implementation which is meant do deal with a real RoboCup-match. In the first
 * version, it will only implement basic principles which are described here: <a
 * href="http://tigers-mannheim.de/trac/wiki/Protokolle/KI_100211#1.%20Play-Auswahl">Play-Auswahl</a>.
 * </p>
 * 
 * @author Gero, Malte
 */
public class MatchPlayFinder implements IPlayFinder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	protected final Logger				log						= Logger.getLogger(getClass());
	
	private final PlayFactory			factory					= PlayFactory.getInstance();
	private final PlayMap				playMap					= PlayMap.getInstance();
	
	/** A list of {@link EPlay}s which actually can be played during a match */
	private final List<EPlay>			availablePlays			= EPlay.getGamePlays();
	
	/** All referee messages that come in when the ball is out of play are stored here! */
	private final Queue<RefereeMsg>	refereeQueue			= new LinkedList<RefereeMsg>();
	
	// private RefereeMsg lastRefereeMsg = null;
	
	/**  */
	public static final int				MIN_SCORE_THRESHOLD	= 5;
	
	private boolean						firstFrame				= true;
	
	private EMatchBehavior				matchBehaviour;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @see MatchPlayFinder
	 */
	public MatchPlayFinder()
	{
		matchBehaviour = AIConfig.getTactics().getTacticalOrientation();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Decision whether we win or loose is done here.
	 * 
	 * 
	 * @author Malte, Gero
	 */
	@Override
	public void choosePlays(List<APlay> result, AIInfoFrame frame, AIInfoFrame preFrame)
	{
		// Pre-operations on AIInfoFrame are done here:
		frame.playStrategy.setMatchBehavior(this.matchBehaviour);
		
		// -1. New referee message incoming?
		if (frame.refereeMsg != null)
		{
			refereeQueue.add(frame.refereeMsg);
		}
		
		// 0. Handles first Frame.
		if (firstFrame)
		{
			result.add(factory.createPlay(EPlay.INIT, frame));
			firstFrame = false;
			return;
		}
		
		// 1. What if there is no ball or no enemies?
		if (frame.worldFrame.ball.pos.equals(AIConfig.INIT_VECTOR) || frame.worldFrame.foeBots.size() < 2)
		{
			// Init play already in last frame and no new decision?
			final List<APlay> prePlays = preFrame.playStrategy.getActivePlays();
			if (prePlays.size() > 0 && prePlays.get(0).getType() == EPlay.INIT && !frame.playStrategy.isForceNewDecision())
			{
				result.addAll(prePlays);
			} else
			{
				log.warn("Ball out of field or no enemy bots! Init Play started!");
				result.add(factory.createPlay(EPlay.INIT, frame));
			}
			return;
		}
		
		// 2. Referee Message present?
		if (refereeQueue.size() > 0)
		{
			RefereeMsg msg = refereeQueue.poll();
			reactOnRefereeCmd(msg.cmd, frame, preFrame, result);
			return;
		}
		
		// 3. Has the mode changed back to match mode?
		if (frame.playStrategy.isStateChanged())
		{
			result.add(factory.createPlay(EPlay.INIT, frame));
			return;
		}
		
		// 4. Force New Decision wish from the GUI?
		if (frame.playStrategy.isForceNewDecision())
		{
			choosePlaysFree(availablePlays, frame, preFrame, result);
			return;
		}
		
		// 5. Bot Connection state
		// TODO: React more sophisticated
		if (frame.playStrategy.getBotConnection().isSomethingTrue())
		{
			log.warn("BotConnection changed: " + frame.playStrategy.getBotConnection().toString());
			if (frame.worldFrame.tigerBots.size() == 5)
			{
				result.add(factory.createPlay(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_TWO, frame));
				result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER,frame));
			} else if(frame.worldFrame.tigerBots.size() == 4)
			{
				result.add(factory.createPlay(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_TWO, frame));
				result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER,frame));
			}
			else if(frame.worldFrame.tigerBots.size() == 3){
				result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
			}
			else
			{
				result.add(factory.createPlay(EPlay.HALT, frame));
			}
			
			return;
		}
		

		// 6. Everything "normal", check for finishedPlays
		if (preFrame.playStrategy.getFinishedPlays().size() == 0)
		{
			result.addAll(preFrame.playStrategy.getActivePlays());
			return; // Hu, that was easy! =)
		}

		// 7. Okay, one or more plays have ended. For each of them, choose one of their follow-ups!
		// TODO: consider not only if a play has finished but also how it finished!
		else
		{
			for (APlay finishedPlay : preFrame.playStrategy.getFinishedPlays())
			{
				// If at least one play failed, choose free!
				if (finishedPlay.getPlayState() == EPlayState.FAILED)
				{
					choosePlaysFree(availablePlays, frame, preFrame, result);
					return;
				}
				EPlay finishedType = finishedPlay.getType();
				// No Follow Tuples for this play?
				final List<PlayTuple> followUps = playMap.getFollowups(finishedType);
				if (followUps == null || followUps.size() < 1)
				{
					choosePlaysFree(availablePlays, frame, preFrame, result);
					return;
				}
				
				int numberOfBots = finishedPlay.getRoleCount();
				// Check which one of all follow ups has the best score
				PlayTuple bestScored = null;
				for (PlayTuple tuple : followUps)
				{
					// first tuple is the best tuple ;-)
					if (bestScored == null)
					{
						bestScored = tuple;
					}
					// if the current tuple is better then the best tuple for now...switch!
					else if (tuple.calcPlayableScore(frame, numberOfBots) > bestScored
							.calcPlayableScore(frame, numberOfBots))
					{
						bestScored = tuple;
					}
				}
				// if the best tuple's score is lower than the minimal score, choose plays free!
				if (bestScored.calcPlayableScore(frame, numberOfBots) < MIN_SCORE_THRESHOLD)
				{
					choosePlaysFree(availablePlays, frame, preFrame, result);
					return;
				}
				// add the new choosen plays
				else
				{
					for (EPlay play : bestScored.getPlays())
					{
						result.add(factory.createPlay(play, frame));
					}
				}
			}
			// If at least one play has not finished, add it again.
			// "What up, not finishers?"
			List<APlay> notFinishedPlays = new ArrayList<APlay>(preFrame.playStrategy.getActivePlays());
			notFinishedPlays.removeAll(preFrame.playStrategy.getFinishedPlays());
			result.addAll(notFinishedPlays);
			return;
		}
	}
	

	/**
	 * Handle play choice triggerd by an incoming referee message.
	 * All standards-plays can be coded in here hardly!
	 * Make sure your plays added here handle exactly 5 roles!
	 * 
	 * @param cmd
	 * @param frame
	 * @param preFrame
	 * @param result Store chosen plays here
	 * 
	 */
	private void reactOnRefereeCmd(ERefereeCommand cmd, AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> result)
	{
		// 5 Bots available?
		if (frame.worldFrame.tigerBots.size() == 5)
		{
			switch (cmd)
			{
				// Always when the ball gets out the field, there is a foul or a timeout the game is stopped.
				// Since we do not know at first WHY the game stopped, we just group around the ball and wait
				// for the next, specific referee command.
				case Stop:
				{
					//result.add(factory.createPlay(EPlay.STOP_MOVE, frame));
					result.add(factory.createPlay(EPlay.STOP_MARKER, frame));
//					result.add(factory.createPlay(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_TWO, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
					break;
				}
					
				case KickOffEnemies:
				{
					result.add(factory.createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
					break;
				}
					
				case KickOffTigers:
				{
					result.add(factory.createPlay(EPlay.KICK_OF_US_SYMMETRY_POSITION, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
					break;
				}
					
				case PenaltyEnemies:
				{
					result.add(factory.createPlay(EPlay.PENALTY_THEM, frame));
					break;
				}
					
				case PenaltyTigers:
				{
					result.add(factory.createPlay(EPlay.PENALTY_US, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
					break;
				}
					
				case IndirectFreeKickTigers:

				case DirectFreeKickTigers:
				{
//					result.add(factory.createPlay(EPlay.FREEKICK_WITH_TWO, frame));
					result.add(factory.createPlay(EPlay.BALL_GETTING_AND_IMMEDIATE_SHOT, frame));
					result.add(factory.createPlay(EPlay.SUPPORT_WITH_ONE_MARKER , frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
					break;
				}
					
					// Here we need a choice for different Plays depending on the type of the
					// freekick and the position where it is taken.
					// For a first approach we don't need to distinguish between
					// throw-in, corner kick, goal kick...
				case IndirectFreeKickEnemies:

				case DirectFreeKickEnemies:
				{
					result.add(factory.createPlay(EPlay.FREEKICK_MARKER, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
					break;
				}
					
					// Force Start ("Schiedsrichter Ball"). After a stopped play, the ball is set
					// free immediatly. A Ball getter should be used here.
				case Start:
				{
					choosePlaysFree(availablePlays, frame, preFrame, result);
					break;
				}
				
				case Halt:
				{
					result.add(factory.createPlay(EPlay.HALT, frame));
					break;
				}
					

					// Important case. The 'ready' or 'normal start' command is called when the game
					// get restarted because there was a recess. The recess was triggered by sth. like
					// a penalty or kickoff. So these plays or their follow plays have to handle what
					// will happen when the game is started again!
					// If not, the playfinder will in the next frame.
				case Ready:
				{
					result.addAll(preFrame.playStrategy.getActivePlays());
					break;
				}
				
				
					

					// Unknown, maybe irrelevant message. Just go on with the current play.
				default:
				{
					result.addAll(preFrame.playStrategy.getActivePlays());
					break;
				}
			}
		}

		
		
		
		
		
		
		// Exceptional situation: Only 4 Bots are available.
		// Focus is given now to working plays, they dont have to be the best ones!
		else if (frame.worldFrame.tigerBots.size() == 4)
		{
			switch (cmd)
			{
				// Always when the ball gets out the field, there is a foul or a timeout the game is stopped.
				// Since we do not know at first WHY the game stopped, we just group around the ball and wait
				// for the next, specific referee command.
				case Stop:
				{
					result.add(factory.createPlay(EPlay.POSITIONING_ON_STOPPED_PLAY_WITH_TWO, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
					break;
				}
					
				case KickOffEnemies:
				{
					result.add(factory.createPlay(EPlay.POSITIONING_ON_KICK_OFF_THEM, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
					break;
				}
					
				case KickOffTigers:
				{
					result.add(factory.createPlay(EPlay.KICK_OF_US_SYMMETRY_POSITION, frame));
					result.add(factory.createPlay(EPlay.KEEPER_SOLO, frame));
					break;
				}
					
				case PenaltyEnemies:
				{
					// TODO: Create Penalty them play with less then 5 bots!
					result.add(factory.createPlay(EPlay.PENALTY_THEM, frame));
					break;
				}
					
				case PenaltyTigers:
				{
					result.add(factory.createPlay(EPlay.PENALTY_US, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
					break;
				}
					
				case IndirectFreeKickTigers:

				case DirectFreeKickTigers:
				{
					result.add(factory.createPlay(EPlay.FREEKICK_OFFENSE_PREPARE_WITH_THREE, frame));
					result.add(factory.createPlay(EPlay.KEEPER_SOLO, frame));
					break;
				}
					
					// Here we need a choice for different Plays depending on the type of the
					// freekick and the position where it is taken.
					// For a first approach we don't need to distinguish between
					// throw-in, corner kick, goal kick...
				case IndirectFreeKickEnemies:

				case DirectFreeKickEnemies:
				{
					result.add(factory.createPlay(EPlay.FREEKICK_MARKER, frame));
					result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
					break;
				}
					
					// Force Start ("Schiedsrichter Ball"). After a stopped play, the ball is set
					// free immediatly. A Ball getter should be used here.
				case Start:
				{
					choosePlaysFree(availablePlays, frame, preFrame, result);
					break;
				}
					
				case Halt:
				{
					result.add(factory.createPlay(EPlay.HALT, frame));
					break;
				}

					// Important case. The 'ready' or 'normal start' command is called when the game
					// get restarted because there was a recess. The recess was triggered by sth. like
					// a penalty or kickoff. So these plays or their follow plays have to handle what
					// will happen when the game is started again!
					// If not, the playfinder will in the next frame.
				case Ready:
				{
					result.addAll(preFrame.playStrategy.getActivePlays());
					break;
				}
					

					// Unknown, maybe irrelevant message. Just go on with the current play.
				default:
				{
					result.addAll(preFrame.playStrategy.getActivePlays());
					break;
				}
			}
		}
		// 3...
		else if (frame.worldFrame.tigerBots.size() == 3)
		{
			result.add(factory.createPlay(EPlay.KEEPER_PLUS_2_DEFENDER, frame));
		}
		// 2...
		else if (frame.worldFrame.tigerBots.size() == 2)
		{
			result.add(factory.createPlay(EPlay.KEEPER_PLUS_1_DEFENDER, frame));
		}
		// 1...
		else if (frame.worldFrame.tigerBots.size() == 1)
		{
			result.add(factory.createPlay(EPlay.KEEPER_SOLO, frame));
		}
		// Risiko!
		else if (frame.worldFrame.tigerBots.size() == 0)
		{
			return;
		}
	}
	

	/**
	 * Choose the best plays for the current game situation depending on the play's playable score.
	 * 
	 * @param A list of plays within which the new plays can be chosen.
	 * @param current frame
	 * @param previous frame
	 * @param result: Store chosen plays here
	 */
	private void choosePlaysFree(List<EPlay> choices, AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> result)
	{
		PlayTuple bestTuple = playMap.getTuples().get(0);
		int numberOfBots = frame.worldFrame.tigerBots.size();
		for (PlayTuple tuple : playMap.getTuples())
		{
			if (tuple.calcPlayableScore(frame, numberOfBots) > bestTuple.calcPlayableScore(frame, numberOfBots))
			{
				bestTuple = tuple;
			}
		}
		result.clear();
		result.addAll(factory.createPlays(bestTuple.getPlays(), frame));
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
