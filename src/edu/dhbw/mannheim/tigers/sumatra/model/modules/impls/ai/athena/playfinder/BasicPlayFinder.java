/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.05.2012
 * Author(s): Paul
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.Pattern;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlayState;


/**
 * Finding plays without the scoring factor (from previous MatchPlayFinder)
 * Quite simple method:
 * * there is one certain play set for each situation
 * * no intelligence
 * * lots of sources for bugs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class BasicPlayFinder extends APlayFinder
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final float	PATTERN_MATCH	= 0.8f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Choose the best plays for the current game situation
	 * 
	 * @param frame current AIInfoFrame
	 * @param preFrame previous AIInfoFrame
	 * @param plays Store chosen plays here
	 */
	private void selectPlays(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		// ### check if we have the ball and goal is visible for bot with ball ###
		boolean isGoalVis = false;
		if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
		{
			final BotID possessionBotID = frame.tacticalInfo.getBallPossession().getTigersId();
			
			for (int i = 1; (i < 10) && !isGoalVis; i++)
			{
				isGoalVis = GeoMath.p2pVisibility(
						frame.worldFrame,
						frame.worldFrame.getTiger(possessionBotID).getPos(),
						AIConfig.getGeometry().getGoalTheir().getGoalPostLeft()
								.subtractNew(new Vector2(0, ((AIConfig.getGeometry().getGoalSize() / 10f) * i) + 10)),
						new ArrayList<BotID>());
			}
		}
		
		// ### if there is an indirect shot running, let it go on and just add some defence and support plays ##
		for (final APlay play : preFrame.playStrategy.getActivePlays())
		{
			if ((play.getType() == EPlay.INDIRECT_SHOTV2) && (play.getPlayState() == EPlayState.RUNNING))
			{
				plays.add(play); // 2
				if (frame.worldFrame.tigerBotsAvailable.size() == 6)
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame)); // 2
				} else if (frame.worldFrame.tigerBotsAvailable.size() == 5)
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				} else if (frame.worldFrame.tigerBotsVisible.size() == 4)
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
				} else if (frame.worldFrame.tigerBotsVisible.size() == 3)
				{
					plays.add(getPlayFactory().createPlay(EPlay.KEEPER_SOLO, frame)); // 1
				}
				return;
			}
		}
		
		// ### depending on number of bots, choose plays. This is the ugly part of this PlayFinder ;) ##
		// 6...
		if (frame.worldFrame.tigerBotsVisible.size() == 6)
		{
			if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
			{
				
				if ((isGoalVis && !((frame.worldFrame.ball.getPos().x() > 2700) && (Math.abs(frame.worldFrame.ball.getPos()
						.y()) > 1500))) || (frame.worldFrame.ball.getPos().x() < 0))
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				} else
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.INDIRECT_SHOTV2, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame)); // 2
				}
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 3
				plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
				
				final List<Pattern> playPatterns = frame.tacticalInfo.getPlayPattern();
				if ((playPatterns.size() != 0) && (playPatterns.get(0).getMatchingScore() > 0.8))
				{
					plays.add(getPlayFactory().createPlay(EPlay.PATTERN_BLOCK_PLAY, frame)); // 1
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				} else
				{
					plays.add(getPlayFactory().createPlay(EPlay.MAN_TO_MAN_MARKER, frame)); // 2
				}
			}
		}
		
		// 5...
		else if (frame.worldFrame.tigerBotsVisible.size() == 5)
		{
			
			if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
			{
				if ((isGoalVis && !((frame.worldFrame.ball.getPos().x() > 2700) && (Math.abs(frame.worldFrame.ball.getPos()
						.y()) > 1500))) || (frame.worldFrame.ball.getPos().x() < 0))
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 3
					plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				} else
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.INDIRECT_SHOTV2, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				}
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 3
				plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
				
				final List<Pattern> playPatterns = frame.tacticalInfo.getPlayPattern();
				if ((playPatterns.size() != 0) && (playPatterns.get(0).getMatchingScore() > PATTERN_MATCH))
				{
					plays.add(getPlayFactory().createPlay(EPlay.PATTERN_BLOCK_PLAY, frame)); // 1
				} else
				{
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				}
			}
		}
		// 4...
		else if (frame.worldFrame.tigerBotsVisible.size() == 4)
		{
			if (frame.tacticalInfo.getBallPossession().getEBallPossession() == EBallPossession.WE)
			{
				if ((isGoalVis && !((frame.worldFrame.ball.getPos().x() > 2700) && (Math.abs(frame.worldFrame.ball.getPos()
						.y()) > 1500))) || (frame.worldFrame.ball.getPos().x() < 0))
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
					plays.add(getPlayFactory().createPlay(EPlay.BREAK_CLEAR, frame)); // 1
				} else
				{
					plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
					plays.add(getPlayFactory().createPlay(EPlay.INDIRECT_SHOTV2, frame)); // 2
				}
			} else
			{
				plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 3
				plays.add(getPlayFactory().createPlay(EPlay.DIRECT_SHOTV2, frame)); // 1
			}
		}
		// 3...
		else if (frame.worldFrame.tigerBotsVisible.size() == 3)
		{
			plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 3
		}
		// 2...
		else if (frame.worldFrame.tigerBotsVisible.size() == 2)
		{
			plays.add(getPlayFactory().createPlay(EPlay.N_DEFENDER_DEFENSPOINTS, frame)); // 2
		}
		// 1...
		else if (frame.worldFrame.tigerBotsVisible.size() == 1)
		{
			plays.add(getPlayFactory().createPlay(EPlay.KEEPER_SOLO, frame)); // 1
		}
		// Risiko!
		else if (frame.worldFrame.tigerBotsVisible.size() == 0)
		{
			return;
		}
	}
	
	
	@Override
	public void onNewDecision(AIInfoFrame frame, AIInfoFrame preFrame, List<APlay> plays)
	{
		selectPlays(frame, preFrame, plays);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
