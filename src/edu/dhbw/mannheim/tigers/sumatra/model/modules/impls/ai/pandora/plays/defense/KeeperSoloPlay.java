/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.10.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.KeeperSoloV2Role;


/**
 * A OneBot Play to realize a solo keeper behavior.<br>
 * Requires: 1 {@link KeeperSoloRole}
 * 
 * @author Malte
 * 
 */
public class KeeperSoloPlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private KeeperSoloV2Role	keeper		= null;
	// private ChipKickRole chipKick = null;
	private final float			INIT_X		= AIConfig.getPlays().getKeeperSoloPlay().getInitX();
	private final float			INIT_Y		= AIConfig.getPlays().getKeeperSoloPlay().getInitY();
	
	private IVector2				initVector	= new Vector2(INIT_X, INIT_Y);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public KeeperSoloPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		keeper = new KeeperSoloV2Role();
		addDefensiveRole(keeper, AIConfig.getGeometry().getGoalOur().getGoalCenter());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		IVector2 viewPoint = searchSaveViewPoint(currentFrame, initVector);
		List<BotID> listNearestBots = AiMath.getTigerBotsNearestToPointSorted(currentFrame, viewPoint);
		if (listNearestBots.size() > 0)
		{
			TrackedTigerBot bot = currentFrame.worldFrame.getTiger(listNearestBots.get(0));
			if (!AIConfig.getGeometry().getPenaltyAreaOur().isPointInShape(bot.getPos()))
			{
				viewPoint = AiMath.getBotKickerPos(bot);
				if (!GeoMath.p2pVisibility(currentFrame.worldFrame, viewPoint, bot.getPos(), bot.getId()))
				{
					viewPoint = searchSaveViewPoint(currentFrame, initVector);
				}
			}
		}
		// TODO PhilippP seiten wahl
		// EnhancedFieldAnalyser analyser = currentFrame.tacticalInfo.getEnhancedFieldAnalyser();
		// viewPoint = analyser.getBestPositionInNearOfPoint(ESituation.FREE, ESearchAlgorithm.HILLCLIMBING,
		// viewPoint);
		
		// if (keeper != null)
		// {
		// if ((((isBallInPenaltyArea(currentFrame) && (state == EPlaySituation.KEEPER)) && (currentFrame.worldFrame.ball
		// .getVel().getLength2() < 0.1)) || chipKick.isCompleted()))
		// {
		// chipKick = new ChipKickRole(viewPoint, 0);
		// chipKick.setPenaltyAreaAllowed(true);
		// switchRoles(keeper, chipKick, currentFrame);
		// state = EPlaySituation.SHOOT;
		// } else if (!isBallInPenaltyArea(currentFrame) && (state != EPlaySituation.KEEPER))
		// {
		// keeper = new KeeperSoloRole();
		// switchRoles(chipKick, keeper, currentFrame);
		// state = EPlaySituation.KEEPER;
		// }
		//
		// }
	}
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	/**
	 * First implementation for searching a good aiming Point
	 * 
	 * @param aiFrame
	 * @return
	 */
	private IVector2 searchSaveViewPoint(AIInfoFrame aiFrame, IVector2 viewPoint)
	{
		// TODO Intellegentere Seiten wahl, momentan wird nur eine ausprobiert und wenn die nicht frei ist nimmt er eine
		// andere
		if ((aiFrame.worldFrame.ball.getPos().y() <= 0))
		{
			viewPoint = new Vector2(viewPoint.x(), -viewPoint.y());
		}
		
		if (GeoMath.p2pVisibility(aiFrame.worldFrame, viewPoint, keeper.getPos(), new ArrayList<BotID>()))
		{
			return viewPoint;
		}
		
		return viewPoint;
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
