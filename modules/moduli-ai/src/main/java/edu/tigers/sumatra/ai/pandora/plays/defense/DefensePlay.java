/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 24, 2014
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.AngleDefPointCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.StopDefPointCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole.EDefenseStates;
import edu.tigers.sumatra.drawable.DrawableTriangle;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.triangle.Triangle;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class DefensePlay extends APlay
{
	@SuppressWarnings("unused")
	private static final Logger		log					= Logger.getLogger(DefensePlay.class.getName());
	
	private final List<DefenderRole>	listDefender		= new ArrayList<DefenderRole>();
	
	private boolean						isStopSituation	= false;
	
	
	/**
	 * 
	 */
	public DefensePlay()
	{
		super(EPlay.DEFENSIVE);
	}
	
	
	/**
	 * Logic cannot be moved to roles, because I have got to know which bots I got.
	 * 
	 * @param currentFrame
	 */
	@Override
	public void updateBeforeRoles(final AthenaAiFrame currentFrame)
	{
		Map<DefenderRole, DefensePoint> defDistribution = new HashMap<DefenderRole, DefensePoint>();
		// final IVector2 ballPos = currentFrame.getWorldFrame().getBall().getPos();
		
		if (listDefender.size() == 0)
		{
			return;
		}
		
		// defDistribution = getZoneDefenseDistribution(currentFrame);
		defDistribution = getAngleDefenseDistribution(currentFrame);
		
		if (isStopSituation)
		{
			
			StopDefPointCalc.modifyDistributionForStop(
					DefenseAux.getBallPosDefense(currentFrame.getWorldFrame().getBall()), defDistribution);
		}
		
		defDistribution.forEach((defender, defPoint) -> defender.setDefPoint(defPoint));
	}
	
	
	private Map<DefenderRole, DefensePoint> getAngleDefenseDistribution(final AthenaAiFrame currentFrame)
	{
		Map<DefenderRole, DefensePoint> defDistribution = new HashMap<DefenderRole, DefensePoint>();
		final IDefensePointCalc defensePointCalculator = new AngleDefPointCalc();
		
		ITacticalField tacticalField = currentFrame.getTacticalField();
		
		defDistribution = defensePointCalculator.getDefenderDistribution(currentFrame, listDefender,
				tacticalField.getDangerousFoeBots());
		
		return defDistribution;
	}
	
	
	@SuppressWarnings("unused")
	private void setClearing(final AthenaAiFrame currentFrame, final ITacticalField tacticalField,
			final Map<ITrackedBot, DefensePoint> directShotDistr,
			final Map<ITrackedBot, DefenderRole> botsToRoleMapping)
	{
		for (ITrackedBot curDirectShotDefender : directShotDistr.keySet())
		{
			DefenderRole curDefenderRole = botsToRoleMapping.get(curDirectShotDefender);
			
			if (directShotDistr.keySet().stream()
					.noneMatch(bot -> botsToRoleMapping.get(bot).getCurrentState() == EDefenseStates.CLEAR_BALL))
			{
				if (DefenseAux.enableClearingByDefense && switchToClear(tacticalField, currentFrame, curDefenderRole))
				{
					curDefenderRole.triggerEvent(DefenderRole.EDefenderEvent.NEED_TO_CLEAR);
					break;
				}
			}
		}
	}
	
	
	/**
	 * @param tacticalField
	 * @param baseAiFrame
	 * @param defRole
	 * @return
	 */
	public static boolean switchToClear(final ITacticalField tacticalField, final BaseAiFrame baseAiFrame,
			final DefenderRole defRole)
	{
		IVector2 ballPos = DefenseAux.getBallPosDefense(baseAiFrame.getWorldFrame().getBall());
		IVector2 botPos = defRole.getBot().getPos();
		double botAngle = defRole.getBot().getAngle();
		List<ARole> offenderRoles = baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE);
		
		double triangleWidth = DefenseAux.triangleWidthClearing;
		double triangleHeight = DefenseAux.triangleHeightClearing;
		
		IVector2 halfWidthVector = new Vector2(triangleWidth / 2., 0);
		
		IVector2 triangleCornerA = botPos
				.addNew(new Vector2(triangleHeight, 0).turn(botAngle).addNew(halfWidthVector.turnNew(-Math.PI / 2.)));
		IVector2 triangleCornerB = botPos
				.addNew(new Vector2(triangleHeight, 0).turn(botAngle).addNew(halfWidthVector.turnNew(Math.PI / 2.)));
		
		Triangle frontTriangle = new Triangle(botPos, triangleCornerA, triangleCornerB);
		
		if (tacticalField.getGameState() != EGameStateTeam.RUNNING)
		{
			return false;
		}
		
		tacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE)
				.add(new DrawableTriangle(frontTriangle, Color.ORANGE));
		
		if (!frontTriangle.isPointInShape(ballPos))
		{
			return false;
		}
		
		if (botPos.x() > ballPos.x())
		{
			return false;
		}
		
		if ((offenderRoles == null) || offenderRoles.stream()
				.anyMatch(role -> (role.getAiFrame() != null)
						&& (GeoMath.distancePP(role.getBot().getPos(), ballPos) < DefenseAux.noClearingBecauseOffenseDist)))
		{
			return false;
		}
		
		return true;
	}
	
	
	@Override
	protected void onRoleRemoved(final ARole role)
	{
		listDefender.remove(role);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
		switch (gameState)
		{
			case PREPARE_PENALTY_THEY:
				assert false : "Play can not deal with PenaltyKick!";
			case PREPARE_PENALTY_WE:
			case STOPPED:
			case HALTED:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_KICKOFF_WE:
			case DIRECT_KICK_THEY:
			case DIRECT_KICK_WE:
			case CORNER_KICK_THEY:
			case CORNER_KICK_WE:
			case GOAL_KICK_THEY:
			case GOAL_KICK_WE:
			case THROW_IN_THEY:
			case THROW_IN_WE:
			case BALL_PLACEMENT_THEY:
			case BALL_PLACEMENT_WE:
				isStopSituation = true;
				break;
			default:
				isStopSituation = false;
				break;
		}
		
		for (DefenderRole role : listDefender)
		{
			
			role.setGameState(gameState);
		}
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		if (listDefender.isEmpty())
		{
			throw new IllegalStateException("There is no role left to be deleted");
		}
		return listDefender.remove(listDefender.size() - 1);
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		DefenderRole def = new DefenderRole(frame.getTacticalField().getGameState());
		listDefender.add(def);
		return def;
	}
	
	
}
