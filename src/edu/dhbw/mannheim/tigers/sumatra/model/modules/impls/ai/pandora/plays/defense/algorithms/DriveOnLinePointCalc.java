/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.exceptions.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.EPointOnLineGetter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.IPointOnLine;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers.ZoneDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.util.InstanceableClass.NotCreateableException;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Make sure we drive towards a good point on the line as soon as we reached the line
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DriveOnLinePointCalc extends ShortestPathDefensePointCalc
{
	private static final Logger			log						= Logger.getLogger(DriveOnLinePointCalc.class.getName());
	
	// private final IPointOnLine pointOnLineGetter = new PassIntersectionPointCalc();
	private IPointOnLine						pointOnLineCalc		= new ZoneDefensePointCalc();
	
	@Configurable(comment = "Choose a calculator for the defense point on the shoot vector of the enemy bot.")
	private static EPointOnLineGetter	pointOnLineGetter		= EPointOnLineGetter.PASSINTERSECTION;
	
	@Configurable(comment = "If the defender is closer than this distance to the shoot vector, he will get a new position on the shoot vector.")
	private static float						minDistShootVector	= 3 * AIConfig.getGeometry().getBotRadius();
	
	/**  */
	@Configurable(comment = "Distance around the enemies offensive bot the defender will optimize its position")
	public static float						nearEnemyBotDist		= 4 * AIConfig.getGeometry().getBotRadius();
	
	@Configurable(comment = "Maximum x position of the defendenders")
	private static float						xLimit					= 0;
	
	
	/**
	  * 
	  */
	public DriveOnLinePointCalc()
	{
		Object[] params = new Object[0];
		try
		{
			pointOnLineCalc = (IPointOnLine) pointOnLineGetter.getInstanceableClass().newInstance(params);
		} catch (NotCreateableException err)
		{
			log.error("Failed to create a instance of a point of line getter.");
		}
	}
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		
		Map<DefenderRole, DefensePoint> distribution = super.getDefenderDistribution(frame, defenders, foeBotDataList);
		DefenderRole curDefender = null;
		DefensePoint curDefPoint = null;
		
		for (Entry<DefenderRole, DefensePoint> entry : distribution.entrySet())
		{
			curDefender = entry.getKey();
			curDefPoint = entry.getValue();
			
			switch (pointOnLineGetter)
			{
				case ZONEDEFENSE:
				case RECEIVERBLOCK:
					if ((GeoMath.distancePP(curDefPoint, curDefender.getPos()) < minDistShootVector)
							||
							Circle.getNewCircle(curDefPoint.getFoeBotData().getFoeBot().getPos(), nearEnemyBotDist)
									.isPointInShape(curDefender.getPos()))
					{
						updateDefPoint(distribution, curDefender, curDefPoint, frame);
					}
					break;
				case HEDGEHOG:
				case PASSINTERSECTION:
				case PASSIVEAGRESSIVE:
				default:
					if ((GeoMath.distancePP(curDefPoint, curDefender.getPos()) < minDistShootVector))
					{
						updateDefPoint(distribution, curDefender, curDefPoint, frame);
					}
					break;
			}
		}
		
		distribution.forEach((defender, defPoint) -> MovementCon.assertDestinationValid(frame.getWorldFrame(),
				defender.getBot().getId(), defPoint, false, false));
		
		return distribution;
	}
	
	
	private void updateDefPoint(final Map<DefenderRole, DefensePoint> distribution, final DefenderRole curDefender,
			final DefensePoint curDefPoint, final MetisAiFrame frame)
	{
		DefensePoint defPoint = pointOnLineCalc.getPointOnLine(curDefPoint, frame, curDefender);
		
		defPoint = limitX(curDefPoint, defPoint);
		
		AIConfig.getGeometry().getPenaltyAreaOur()
				.nearestPointOutside(defPoint, Geometry.getPenaltyAreaMargin());
		distribution.put(curDefender, defPoint);
	}
	
	
	private DefensePoint limitX(final DefensePoint curDefPoint, final DefensePoint defPoint)
	{
		if ((defPoint.x() > xLimit) && !defPoint.equals(curDefPoint))
		{
			IVector2 intersectionPoint;
			try
			{
				intersectionPoint = GeoMath.intersectionPoint(curDefPoint, defPoint.subtractNew(curDefPoint),
						new Vector2(xLimit, 0), new Vector2(0, 1));
			} catch (MathException err)
			{
				intersectionPoint = curDefPoint;
				log.error("Could not properly x limit the defense point in " + DriveOnLinePointCalc.class.getName()
						+ ". Using fallback.");
			}
			
			return new DefensePoint(intersectionPoint, curDefPoint.getFoeBotData());
		}
		
		return defPoint;
	}
}
