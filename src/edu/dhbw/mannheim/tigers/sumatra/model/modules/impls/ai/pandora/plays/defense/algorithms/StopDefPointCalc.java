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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.interfaces.IDefensePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;


/**
 * Deal with stop situation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * @author FelixB <bayer.fel@gmail.com>
 */
public class StopDefPointCalc implements IDefensePointCalc
{
	private IDefensePointCalc	parent;
	
	private static float			xLimit	= -(AIConfig.getGeometry().getCenterCircleRadius() + (2 * AIConfig
															.getGeometry()
															.getBotRadius()));
	
	
	/**
	 * @param parent
	 */
	public StopDefPointCalc(final IDefensePointCalc parent)
	{
		this.parent = parent;
	}
	
	
	@Override
	public Map<DefenderRole, DefensePoint> getDefenderDistribution(final MetisAiFrame frame,
			final List<DefenderRole> defenders,
			final List<FoeBotData> foeBotDataList)
	{
		Map<DefenderRole, DefensePoint> distribution = parent.getDefenderDistribution(frame, defenders, foeBotDataList);
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		float stopDistance = AIConfig.getGeometry().getBotToBallDistanceStop()
				+ (1.5f * AIConfig.getGeometry().getBotRadius());
		Circle forbiddenCircle = Circle.getNewCircle(ballPos, stopDistance);
		
		for (Entry<DefenderRole, DefensePoint> entry : distribution.entrySet())
		{
			DefensePoint defPoint = entry.getValue();
			DefenderRole defender = entry.getKey();
			
			if ((defPoint.x() > xLimit))
			{
				defPoint.setX(xLimit);
			}
			
			if (forbiddenCircle.isPointInShape(defPoint))
			{
				IVector2 newDefPoint = GeoMath.stepAlongLine(ballPos, defPoint, stopDistance);
				distribution.put(defender, new DefensePoint(newDefPoint));
			}
		}
		
		return distribution;
	}
}
