/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.02.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.Athena;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus1DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus2DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperSoloPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.ManToManMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.IndirectShotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallCapturingWithDoublingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallCapturingWithOnePassBlockerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallGettingAndImmediateShotPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallGettingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallWinningWithOneBlockerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.BallWinningWithOnePassBlockerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.ChipForwardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.DirectShotPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.GameOffensePrepareWithThreePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.GameOffensePrepareWithTwoPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.IndirectShotPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.PassForwardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.PassToKeeperPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.PassTwoBotsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.attack.PullBackPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support.PositionImprovingNoBallWithOnePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support.PositionImprovingNoBallWithTwoPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support.SupportWithOneBlockerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support.SupportWithOneMarker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense.support.SupportWithOnePassBlockerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.AroundTheBallPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.GuiTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.HaltPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.Init4Play;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.InitPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.MaintenancePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.OneBotTestPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.PassTrainingPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.others.PathPlanningPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreeKickWithOneV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreeKickWithTwo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreekickMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick.FreekickOffensePrepareWithThreeBots;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.KickOffSymmetryPositioningPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.PositioningOnKickOffThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty.PenaltyUsPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.PositioningOnStoppedPlayWithThreePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.PositioningOnStoppedPlayWithTwoPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.StopMarkerPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.stop.StopMovePlay;


/**
 * Simple factory class which helps especially {@link Athena} for in-game play creation and decision
 * 
 * @author Gero
 */
public class PlayFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static PlayFactory	instance	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public static synchronized PlayFactory getInstance()
	{
		if (instance == null)
		{
			instance = new PlayFactory();
		}
		return instance;
	}
	

	private PlayFactory()
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Creates a new Play that has the same type as the given play.
	 * 
	 * @param play
	 * @param wf
	 * @return newPlay
	 * @author MalteM
	 */
	public APlay createPlay(APlay play, AIInfoFrame currentFrame)
	{
		return createPlay(play.getType(), currentFrame);
	}
	

	/**
	 * Creates new Plays to the given EPlays.
	 * 
	 * @param playy
	 * @param wf
	 * @return newPlay
	 * @author MalteM
	 */
	public List<APlay> createPlays(List<EPlay> plays, AIInfoFrame currentFrame)
	{
		List<APlay> resultList = new ArrayList<APlay>();
		for (EPlay play : plays)
		{
			resultList.add(createPlay(play, currentFrame));
		}
		return resultList;
	}
	

	/**
	 * Factory method for {@link APlay}s
	 * <p>
	 * <b>Important:</b> Don't forget to add your play here if you wrote one!!!
	 * </p>
	 * 
	 * @param play The {@link EPlay} associated with the {@link APlay} to return
	 * @param curFrame The {@link AIInfoFrame} the created play should be initialized with
	 * @return The generated {@link APlay}
	 * @throws IllegalArgumentException If play or wf == null or the given {@link EPlay} is not associated with any
	 *            {@link APlay}
	 */
	public APlay createPlay(EPlay play, AIInfoFrame curFrame)
	{
		if (play == null || curFrame == null)
		{
			throw new IllegalArgumentException("Play or AIFrame == null, unable to generate APlay!");
		}
		APlay result = null;
		switch (play)
		{
			case ONE_BOT_TEST:
				result = new OneBotTestPlay(curFrame);
				break;
			
			case HALT:
				result = new HaltPlay(curFrame);
				break;
			
			case SUPPORT_WITH_ONE_MARKER:
				result = new SupportWithOneMarker(curFrame);
				break;
			
			case AROUND_THE_BALL:
				result = new AroundTheBallPlay(curFrame);
				break;
			
			case KEEPER_PLUS_1_DEFENDER:
				result = new KeeperPlus1DefenderPlay(curFrame);
				break;
			
			case KEEPER_PLUS_2_DEFENDER:
				result = new KeeperPlus2DefenderPlay(curFrame);
				break;
			
			case KEEPER_SOLO:
				result = new KeeperSoloPlay(curFrame);
				break;
			
			case PASS_TWO_BOTS:
				result = new PassTwoBotsPlay(curFrame);
				break;
			
			case MAN_TO_MAN_MARKER:
				result = new ManToManMarkerPlay(curFrame);
				break;
			
			case PP_PLAY:
				result = new PathPlanningPlay(curFrame);
				break;
			
			case POSITIONING_ON_STOPPED_PLAY_WITH_TWO:
				result = new PositioningOnStoppedPlayWithTwoPlay(curFrame);
				break;
			
			case POSITIONING_ON_STOPPED_PLAY_WITH_THREE:
				result = new PositioningOnStoppedPlayWithThreePlay(curFrame);
				break;
			
			case MAINTENANCE:
				result = new MaintenancePlay(curFrame);
				break;
			
			case STOP_MARKER:
				result = new StopMarkerPlay(curFrame);
				break;
			
			case STOP_MOVE:
				result = new StopMovePlay(curFrame);
				break;
			
			case PENALTY_THEM:
				result = new PenaltyThemPlay(curFrame);
				break;
			
			case PENALTY_US:
				result = new PenaltyUsPlay(curFrame);
				break;
			
			case BALL_GETTING:
				result = new BallGettingPlay(curFrame);
				break;
			
			case POSITIONING_ON_KICK_OFF_THEM:
				result = new PositioningOnKickOffThemPlay(curFrame);
				break;
			
			case BALLWINNING_WITH_ONE_BLOCKER:
				result = new BallWinningWithOneBlockerPlay(curFrame);
				break;
			
			case BALLWINNING_WITH_ONE_PASS_BLOCKER:
				result = new BallWinningWithOnePassBlockerPlay(curFrame);
				break;
			
			case BALLCAPTURING_WITH_DOUBLING:
				result = new BallCapturingWithDoublingPlay(curFrame);
				break;
			
			case BALLCAPTURING_WITH_ONE_PASS_BLOCKER:
				result = new BallCapturingWithOnePassBlockerPlay(curFrame);
				break;
			
			case FREEKICK_OFFENSE_PREPARE_WITH_THREE:
				result = new FreekickOffensePrepareWithThreeBots(curFrame);
				break;
			
			case FREEKICK_WITH_TWO:
				result = new FreeKickWithTwo(curFrame);
				break;
			
			case FREEKICK_V2:
				result = new FreeKickWithOneV2(curFrame);
				break;
			
			case FREEKICK_MARKER:
				result = new FreekickMarkerPlay(curFrame);
				break;
			
			case GAME_OFFENSE_PREPARE_WITH_TWO:
				result = new GameOffensePrepareWithTwoPlay(curFrame);
				break;
			
			case GAME_OFFENSE_PREPARE_WITH_THREE:
				result = new GameOffensePrepareWithThreePlay(curFrame);
				break;
			
			case POSITION_IMPROVING_NO_BALL_WITH_ONE:
				result = new PositionImprovingNoBallWithOnePlay(curFrame);
				break;
			
			case POSITION_IMPROVING_NO_BALL_WITH_TWO:
				result = new PositionImprovingNoBallWithTwoPlay(curFrame);
				break;
			
			case SUPPORT_WITH_ONE_BLOCKER:
				result = new SupportWithOneBlockerPlay(curFrame);
				break;
			
			case SUPPORT_WITH_ONE_PASS_BLOCKER:
				result = new SupportWithOnePassBlockerPlay(curFrame);
				break;
			
			case DIRECT_SHOT:
				result = new DirectShotPlay(curFrame);
				break;
			
			case GUI_TEST_PLAY:
				result = new GuiTestPlay(curFrame);
				break;
			
			case PASS_TRAINING:
				result = new PassTrainingPlay(curFrame);
				break;
			
			case KICK_OF_US_SYMMETRY_POSITION:
				result = new KickOffSymmetryPositioningPlay(curFrame);
				break;
			
			case INDIRECT_SHOT:
				result = new IndirectShotPlay(curFrame);
				break;
			
			case PASS_FORWARD:
				result = new PassForwardPlay(curFrame);
				break;
			
			case PASS_TO_KEEPER:
				result = new PassToKeeperPlay(curFrame);
				break;
			
			case INIT:
				// TODO Add number of bots condition here
				result = new InitPlay(curFrame);
				break;
			
			case CHIP_FORWARD:
				result = new ChipForwardPlay(curFrame);
				break;
			
			case PULL_BACK:
				result = new PullBackPlay(curFrame);
				break;
			
			case INIT4:
				result = new Init4Play(curFrame);
				break;
			
			case BALL_GETTING_AND_IMMEDIATE_SHOT:
				result = new BallGettingAndImmediateShotPlay(curFrame);
				break;
			
			case INDIRECT_SHOTV2:
				result = new IndirectShotV2(curFrame);
				break;
			
			default:
				throw new IllegalArgumentException("Play type could not be handled by play factory! Play = "
						+ play.toString());
		}
		
		result.loadPenaltyFactors();
		return result;
	}
	

	/**
	 * Calls {@link #doSelfCheck(List)} with {@link EPlay#getGamePlays()}
	 */
	public List<EPlay> selfCheckPlays()
	{
		List<EPlay> result = doSelfCheck(EPlay.getGamePlays());
		result.addAll(doSelfCheck(EPlay.getStandardPlays()));
		return result;
	}
	

	/**
	 * Simply tries to create an instance of {@link APlay} for every given {@link EPlay} using
	 * {@link #createPlay(EPlay, WorldFrame)}.
	 * 
	 * @param plays
	 * @return A list of {@link EPlay} which failed
	 */
	private List<EPlay> doSelfCheck(List<EPlay> plays)
	{
		List<EPlay> failedPlays = new ArrayList<EPlay>();
		for (EPlay testPlay : plays)
		{
			try
			{
				createPlay(testPlay, createFakeFrame());
			} catch (IllegalArgumentException err)
			{
				failedPlays.add(testPlay);
			}
		}
		
		return failedPlays;
	}
	

	/**
	 * <b>WARNING:</b> Only for debugging/test purpose!!!
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createFakeFrame()
	{
		TrackedBall ball = new TrackedBall(-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, true);
		WorldFrame wf = new WorldFrame(new HashMap<Integer, TrackedBot>(), new HashMap<Integer, TrackedTigerBot>(), ball,
				-1, -1, -1);
		
		AIInfoFrame frame = new AIInfoFrame(wf, null);
		return frame;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
