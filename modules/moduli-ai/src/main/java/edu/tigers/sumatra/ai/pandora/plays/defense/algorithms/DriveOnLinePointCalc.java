/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers.IPointOnLine;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers.PassiveAgressivePointCalc;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers.ZoneDefensePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * Make sure we drive towards a good point on the line as soon as we reached the line
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DriveOnLinePointCalc extends ShortestPathDefensePointCalc
{
	
	
	@SuppressWarnings("unused")
	private static final Logger	log							= Logger.getLogger(DriveOnLinePointCalc.class.getName());
	
	@Configurable(comment = "If the defender is closer than this distance to the shoot vector, he will get a new position on the shoot vector.")
	private static double			minDistShootVector		= 3 * Geometry.getBotRadius();
	
	/**  */
	@Configurable(comment = "Distance around the enemies offensive bot the defender will optimize its position")
	public static double				nearEnemyBotDist			= 4 * Geometry.getBotRadius();
	
	
	private static double			penAreaMarginRefining	= DefenseAux.penAreaMargin();
	
	
	static
	{
		ConfigRegistration.registerClass("defensive", DriveOnLinePointCalc.class);
	}
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		
		Map<DefenderRole, DefensePoint> distribution = super.getDefenderDistribution(frame, defenders, foeBotDataList);
		
		for (Entry<DefenderRole, DefensePoint> entry : distribution.entrySet())
		{
			DefenderRole curDefender = entry.getKey();
			DefensePoint curDefPoint = entry.getValue();
			
			RoleFinderInfo currentRoleFinderInfo = frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE);
			
			if (defenders.size() < currentRoleFinderInfo.getDesiredRoles())
			{
				if ((GeoMath.distancePP(curDefPoint, curDefender.getPos()) < minDistShootVector)
						||
						Circle.getNewCircle(curDefPoint.getFoeBotData().getFoeBot().getPos(), nearEnemyBotDist)
								.isPointInShape(curDefender.getPos()))
				{
					updateDefPoint(distribution, curDefender, curDefPoint, frame, new ZoneDefensePointCalc());
				}
				
			} else
			{
				if ((GeoMath.distancePP(curDefPoint, curDefender.getPos()) < minDistShootVector))
				{
					
					updateDefPoint(distribution, curDefender, curDefPoint, frame, new PassiveAgressivePointCalc());
				}
			}
		}
		
		refineDefensePoints(distribution.values());
		
		return distribution;
	}
	
	
	private void refineDefensePoints(final Collection<DefensePoint> defensePoints)
	{
		PenaltyArea penAreaOur = Geometry.getPenaltyAreaOur();
		
		if (defensePoints.size() > 1)
		{
			List<DefensePoint> listDefensePoints = new ArrayList<DefensePoint>();
			List<DefensePoint> pointsMoved = new ArrayList<DefensePoint>();
			
			double botDiameter = 2 * Geometry.getBotRadius();
			
			defensePoints.forEach(defPoint -> listDefensePoints.add(defPoint));
			
			for (DefensePoint curDefPointOuter : listDefensePoints)
			{
				int index = listDefensePoints.indexOf(curDefPointOuter) + 1;
				List<DefensePoint> sublist = listDefensePoints.subList(index, listDefensePoints.size());
				
				for (DefensePoint curDefPointInner : sublist)
				{
					IVector2 outer2inner = curDefPointInner.subtractNew(curDefPointOuter);
					
					// refining needed?
					if (outer2inner.getLength2() < botDiameter)
					{
						double botRadius = Geometry.getBotRadius();
						
						// special handling near the penalty area; we need to make sure no point ends inside the penalty area
						if (penAreaOur.isPointInShape(curDefPointOuter, penAreaMarginRefining))
						{
							IVector2 center = curDefPointOuter.addNew(outer2inner.scaleToNew(outer2inner.getLength2() / 2.));
							IVector2 goalCenterOur = Geometry.getGoalOur().getGoalCenter();
							IVector2 goal2center = center.subtractNew(goalCenterOur);
							
							// we move each point away from each other if no point was moved before
							if (!pointsMoved.contains(curDefPointOuter))
							{
								curDefPointOuter.set(center.addNew(goal2center.scaleToNew(botRadius).turn(-Math.PI / 2.0)));
								curDefPointInner.set(center.addNew(goal2center.scaleToNew(botRadius).turn(Math.PI / 2.0)));
								
								pointsMoved.add(curDefPointOuter);
								pointsMoved.add(curDefPointInner);
							}
							// if the first point was already moved we only move the second point
							else
							{
								curDefPointInner
										.set(curDefPointOuter.addNew(goal2center).scaleToNew(botDiameter).turn(Math.PI / 2.0));
								
								pointsMoved.add(curDefPointInner);
							}
							
						}
						// just move the points away from each other
						else
						{
							// we move each point away from each other if no point was moved before
							if (!pointsMoved.contains(curDefPointOuter))
							{
								IVector2 center = curDefPointOuter
										.addNew(outer2inner.scaleToNew(outer2inner.getLength2() / 2.));
								
								curDefPointOuter.set(center.addNew(outer2inner.scaleToNew(-botRadius)));
								curDefPointInner.set(center.addNew(outer2inner.scaleToNew(botRadius)));
								
								pointsMoved.add(curDefPointOuter);
								pointsMoved.add(curDefPointInner);
							}
							// if the first point was already moved we only move the second point
							else
							{
								curDefPointInner.set(curDefPointOuter.addNew(outer2inner.scaleToNew(botDiameter)));
								
								pointsMoved.add(curDefPointInner);
							}
						}
					}
				}
			}
		}
		
		// Move points inside of the field and out of the penalty area
		for (DefensePoint curDefPoint : defensePoints)
		{
			
			curDefPoint.set(Geometry.getField().nearestPointInside(curDefPoint, Geometry.getBotRadius()));
			curDefPoint.set(penAreaOur.nearestPointOutside(curDefPoint, penAreaMarginRefining));
		}
	}
	
	
	private void updateDefPoint(final Map<DefenderRole, DefensePoint> distribution, final DefenderRole curDefender,
			final DefensePoint curDefPoint, final MetisAiFrame frame, final IPointOnLine pointOnLineCalc)
	{
		DefensePoint defPoint = pointOnLineCalc.getPointOnLine(curDefPoint, frame, curDefender);
		distribution.put(curDefender, defPoint);
	}
}
