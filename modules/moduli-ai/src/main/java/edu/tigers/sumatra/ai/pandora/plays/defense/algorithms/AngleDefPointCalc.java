/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2016
 * Author(s): Felix Bayer <bayer.fel@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotGroup;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author Felix Bayer <bayer.fel@googlemail.com>
 */
public class AngleDefPointCalc implements IDefensePointCalc
{
	@SuppressWarnings("unused")
	private static final Logger	log						= Logger.getLogger(AngleDefPointCalc.class.getName());
	
	private AngleDefenseData		angleDefenseData		= null;
	
	@SuppressWarnings("unused")
	private List<IDrawableShape>	angleDefenseShapes	= null;
	
	private int							maxDefendersBall		= 2;
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		Map<DefenderRole, DefensePoint> distribution = null;
		
		ITacticalField tacticalField = frame.getTacticalField();
		angleDefenseData = tacticalField.getAngleDefenseData();
		angleDefenseShapes = tacticalField.getDrawableShapes().get(EShapesLayer.ANGLE_DEFENSE);
		
		int nDefenders = defenders.size();
		
		List<FoeBotGroup> foeGroups = angleDefenseData.getFoeBotGroups();
		foeGroups.sort(FoeBotGroup.PRIORITY);
		
		setGroup2nDef(frame.getWorldFrame(), tacticalField.getGameState(), foeGroups, nDefenders);
		splitGroups(foeGroups);
		
		distribution = calculateDefenderDistribution(frame, defenders, foeGroups);
		
		return distribution;
	}
	
	
	/**
	 * @param foeGroups
	 * @brief split the ball possessing group to only defend with two defenders against the ball.
	 *        You need to think through the behaviour when you are sorting the groups again...
	 */
	private void splitGroups(final List<FoeBotGroup> foeGroups)
	{
		if ((foeGroups.size() == 0) || !foeGroups.get(0).possessesBall())
		{
			return;
		}
		
		FoeBotGroup ballPossessingGroup = foeGroups.get(0);
		
		if (ballPossessingGroup.getNAssignedDefender() <= maxDefendersBall)
		{
			return;
		}
		
		FoeBotGroup newGroup = new FoeBotGroup(ballPossessingGroup.invertPossessesBall());
		newGroup.setWasBallPossessing(true);
		
		ballPossessingGroup.getMember().forEach(member -> newGroup.addMember(member));
		
		newGroup.setFirst(ballPossessingGroup.isFirst());
		newGroup.setLast(ballPossessingGroup.isLast());
		
		newGroup.setNAssignedDefender(ballPossessingGroup.getNAssignedDefender() - maxDefendersBall);
		ballPossessingGroup.setNAssignedDefender(maxDefendersBall);
		
		foeGroups.add(1, newGroup);
	}
	
	
	private Map<DefenderRole, DefensePoint> calculateDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders, final List<FoeBotGroup> groups)
	{
		Map<DefenderRole, DefensePoint> defDistr = new HashMap<>();
		
		ITacticalField tacticalField = frame.getTacticalField();
		IVector2 ballPos = DefenseAux.getBallPosDefense(frame.getWorldFrame().getBall());
		
		defDistr = getDefDistrPrioritySorted(groups, defenders, tacticalField, frame.getWorldFrame(), ballPos);
		
		return defDistr;
	}
	
	
	/**
	 * @param groups
	 * @param defenders
	 * @param tacticalField
	 * @param ballPos
	 * @return
	 */
	private Map<DefenderRole, DefensePoint> getDefDistrPrioritySorted(final List<FoeBotGroup> groups,
			final List<DefenderRole> defenders, final ITacticalField tacticalField, final WorldFrame frame,
			final IVector2 ballPos)
	{
		Map<DefenderRole, DefensePoint> defDistr = new HashMap<>();
		
		EGameStateTeam gameState = tacticalField.getGameState();
		
		List<DefenderRole> remainingDefenders = new ArrayList<>();
		remainingDefenders.addAll(defenders);
		
		boolean crucialDefenderSet = false;
		
		Map<DefenderRole, DefensePoint> localDistr = new HashMap<>();
		for (FoeBotGroup group : groups)
		{
			if (group.getNAssignedDefender() > 0)
			{
				if (group.possessesBall())
				{
					localDistr = group.getBallBlockingDefPoints(frame, gameState, ballPos, remainingDefenders);
				} else
				{
					localDistr = group.getDefenseDistribution(remainingDefenders, gameState, tacticalField, frame);
					
				}
			}
			
			for (DefenderRole defRole : localDistr.keySet())
			{
				remainingDefenders.remove(defRole);
				
				if (DefenseAux.crucialDefendersEnabled && !crucialDefenderSet
						&& (group.possessesBall() || group.containsBestRedirector()))
				{
					tacticalField.addCrucialDefender(defRole.getBotID());
					crucialDefenderSet = true;
				}
			}
			
			defDistr.putAll(localDistr);
		}
		
		return defDistr;
	}
	
	
	/**
	 * @param angleDefenseShapes
	 * @param ballPos
	 */
	public static void drawBallShapes(final List<IDrawableShape> angleDefenseShapes, final IVector2 ballPos)
	{
		IVector2 leftGoalPost = Geometry.getGoalOur().getGoalPostLeft();
		IVector2 rightGoalPost = Geometry.getGoalOur().getGoalPostRight();
		IVector2 intersectionBisectorGoal = GeoMath.calculateBisector(ballPos, leftGoalPost, rightGoalPost);
		
		angleDefenseShapes.add(new DrawableCircle(ballPos, 40, Color.BLACK));
		angleDefenseShapes.add(new DrawableLine(new Line(leftGoalPost, ballPos.subtractNew(leftGoalPost)), Color.ORANGE));
		angleDefenseShapes
				.add(new DrawableLine(new Line(rightGoalPost, ballPos.subtractNew(rightGoalPost)), Color.ORANGE));
		angleDefenseShapes.add(new DrawableLine(
				new Line(intersectionBisectorGoal, ballPos.subtractNew(intersectionBisectorGoal)), Color.ORANGE));
	}
	
	
	private void setGroup2nDef(final WorldFrame worldFrame, final EGameStateTeam gameState,
			final List<FoeBotGroup> foeGroups, final int nDefenders)
	{
		int defendersAvailable = nDefenders;
		
		foeGroups.forEach(group -> group.setNAssignedDefender(0));
		
		while ((defendersAvailable > 0) && !allGroupsFull(worldFrame, gameState, foeGroups))
		{
			for (FoeBotGroup group : foeGroups)
			{
				int curValue = group.getNAssignedDefender();
				
				if (defendersAvailable == 0)
				{
					break;
				}
				
				if ((group.nMaxDefenders(gameState, worldFrame) > curValue))
				{
					group.setNAssignedDefender(curValue + 1);
					defendersAvailable -= 1;
				}
			}
		}
	}
	
	
	private boolean allGroupsFull(final WorldFrame worldFrame, final EGameStateTeam gameState,
			final List<FoeBotGroup> groups)
	{
		for (FoeBotGroup group : groups)
		{
			if (group.nMaxDefenders(gameState, worldFrame) > group.getNAssignedDefender())
			{
				return false;
			}
		}
		
		return true;
	}
}
