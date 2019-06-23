/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.BallPossessionCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.criteria.global.OpponentApproximateScoringChanceCrit;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff.PositioningOnKickOffThemPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * This Play belongs to the {@link KeeperPlus2DefenderPlay}.
 * It manages the strikers, who transform
 * in defense situations to ManToManMarkers ("Manndecker").
 * For now, they mark/attack the 2 opponents who are moved forward most.
 * 
 * @author Malte
 */
public class ManToManMarkerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long							serialVersionUID	= -8847620142604122537L;
	
	private ManToManMarkerRole							firstMarker;
	private ManToManMarkerRole							secondMarker;
	
	private Vector2										firstMarkerPos;
	
	private TrackedBot									enemy1;
	private TrackedBot									enemy2;
	
	private BallPossessionCrit							ballPosCrit;
	private OpponentApproximateScoringChanceCrit	oppScoChaCrit;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public ManToManMarkerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.MAN_TO_MAN_MARKER, aiFrame);
		
		ballPosCrit = new BallPossessionCrit(EBallPossession.THEY);
		oppScoChaCrit = new OpponentApproximateScoringChanceCrit(true);
		addCriterion(ballPosCrit);
		addCriterion(oppScoChaCrit);
		
		firstMarker = new ManToManMarkerRole(EWAI.FIRST);
		secondMarker = new ManToManMarkerRole(EWAI.SECOND);
		addCreativeRole(firstMarker, new Vector2(0, 250));
		addCreativeRole(secondMarker, new Vector2(0, -250));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		// System.out.println("BallPosCrit: "+ballPosCrit.getPenaltyFactor());
		// System.out.println("OppScoChaCrit: "+oppScoChaCrit.getPenaltyFactor());
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame frame)
	{
		if (frame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.WE)
		{
			changeToSucceeded();
		}
		
		enemy1 = null;
		enemy2 = null;
	}
	

	/**
	 * TODO: optimize this! @see {@link PositioningOnKickOffThemPlay}
	 */
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		int ballCarrierID = frame.tacticalInfo.getBallPossesion().getOpponentsId();
		Map<Integer, TrackedBot> enemies = new HashMap<Integer, TrackedBot>(frame.worldFrame.foeBots);
		
		// There is a ball carrier!
		if(ballCarrierID != -1)
		{
			enemies.remove(ballCarrierID);
		}
		
		if (enemies.size() <= 1)
		{
			log.warn("ManToManMarkerPlay is selected although there are less then 2 enemy" + " bots!");
			firstMarker.updateTarget(new Vector2(-AIConfig.getGeometry().getFieldLength() / 10, AIConfig.getGeometry()
					.getFieldWidth() / 4));
			secondMarker.updateTarget(new Vector2(-AIConfig.getGeometry().getFieldLength() / 10, -AIConfig.getGeometry()
					.getFieldWidth() / 4));
			return;
		}
		
		for (TrackedBot bot : enemies.values())
		{
			if (enemy1 == null)
			{
				enemy1 = bot;
			} else if (enemy2 == null)
			{
				enemy2 = bot;
			} else if (bot.pos.x < enemy1.pos.x)
			{
				if (enemy1.pos.x < enemy2.pos.x)
				{
					enemy2 = bot;
					continue;
				}
				enemy1 = bot;
			} else if (bot.pos.x < enemy2.pos.x)
			{
				enemy2 = bot;
			}
		}
		
		firstMarkerPos = new Vector2(frame.worldFrame.tigerBots.get(firstMarker.getBotID()).pos);
		if (AIMath.distancePP(enemy1, firstMarkerPos) < AIMath.distancePP(enemy2, firstMarkerPos))
		{
			firstMarker.updateTarget(enemy1.pos);
			secondMarker.updateTarget(enemy2.pos);
		} else
		{
			firstMarker.updateTarget(enemy2.pos);
			secondMarker.updateTarget(enemy1.pos);
		}
		
		
	}
}
