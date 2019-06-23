/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 18, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.helpers;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ZoneDefensePointCalc implements IPointOnLine
{
	@Configurable(comment = "Distance of the defense point to the enemies bot")
	private static float	defendDistance	= 1.5f * AIConfig.getGeometry().getBotRadius();
	
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame,
			final DefenderRole curDefender)
	{
		FoeBotData foeBotData = defPoint.getFoeBotData();
		IVector2 foeBotPos = foeBotData.getFoeBot().getPos();
		
		if (Circle.getNewCircle(defPoint.getFoeBotData().getFoeBot().getPos(), DriveOnLinePointCalc.nearEnemyBotDist)
				.isPointInShape(curDefender.getPos()))
		{
			// Calculate a defense point on the bisector in the angle ball->foeBot and foeBot->goal
			IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
			IVector2 foeBot2Ball = ballPos.subtractNew(foeBotPos);
			IVector2 foeBot2Goal = foeBotData.getBot2goal();
			IVector2 bisector = GeoMath.calculateBisector(foeBotPos, foeBotPos.addNew(foeBot2Ball),
					foeBotPos.addNew(foeBot2Goal)).subtract(foeBotPos);
			IVector2 defPos = foeBotPos.addNew(bisector.scaleToNew(defendDistance));
			
			defPos = AIConfig.getGeometry().getPenaltyAreaOur()
					.nearestPointOutside(defPos, Geometry.getPenaltyAreaMargin());
			
			return new DefensePoint(defPos, foeBotData);
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToBot(), foeBotData);
	}
}
