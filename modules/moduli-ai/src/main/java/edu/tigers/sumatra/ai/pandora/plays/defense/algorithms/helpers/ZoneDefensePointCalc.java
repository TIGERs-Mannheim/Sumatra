/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 18, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.helpers;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.metis.defense.ZoneDefenseCalc;
import edu.tigers.sumatra.ai.metis.defense.algorithms.ExtensiveFoeBotCalc;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseAux;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.pandora.plays.defense.algorithms.DriveOnLinePointCalc;
import edu.tigers.sumatra.ai.pandora.roles.defense.DefenderRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author FelixB <bayer.fel@gmail.com>
 */
public class ZoneDefensePointCalc implements IPointOnLine
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(
			ZoneDefensePointCalc.class.getName());
	
	
	@Override
	public DefensePoint getPointOnLine(final DefensePoint defPoint, final MetisAiFrame frame,
			final DefenderRole curDefender)
	{
		double defendDistance = ZoneDefenseCalc.getMinDistanceDef2Foe();
		double zoneDefenseAngleCorrection = ZoneDefenseCalc.getZoneDefenseAngleCorrection();
		
		FoeBotData foeBotData = defPoint.getFoeBotData();
		IVector2 foeBotPos = foeBotData.getFoeBot().getPos();
		
		if (Circle.getNewCircle(defPoint.getFoeBotData().getFoeBot().getPos(), DriveOnLinePointCalc.nearEnemyBotDist)
				.isPointInShape(curDefender.getPos()))
		{
			double yLimitDefenders = ZoneDefenseCalc.getYLimitDefenders();
			
			IVector2 defPos = null;
			
			IVector2 ourGoalCenter = Geometry.getGoalOur().getGoalCenter();
			
			IVector2 foeBot2Ball = foeBotData.getBall2bot().multiplyNew(-1);
			IVector2 foeBot2Goal = foeBotData.getBot2goal();
			
			double angleBallBotGoal = GeoMath.angleBetweenVectorAndVectorWithNegative(foeBot2Goal, foeBot2Ball);
			
			if (Math.abs(angleBallBotGoal) < zoneDefenseAngleCorrection)
			{
				// Calculate a defense point on the bisector in the angle ball->foeBot and foeBot->goal
				IVector2 bisector = GeoMath.calculateBisector(foeBotPos, foeBotPos.addNew(foeBot2Ball),
						foeBotPos.addNew(foeBot2Goal)).subtract(foeBotPos);
				defPos = foeBotPos.addNew(bisector.scaleToNew(defendDistance));
			} else
			{
				// correct the defense position according to the angle
				double pi = Math.PI;
				double angleToCorrect = pi - angleBallBotGoal;
				double correctionAngle = (angleToCorrect * (angleBallBotGoal / angleToCorrect)) - pi;
				
				defPos = foeBotPos.addNew(
						foeBot2Goal.scaleToNew(defendDistance).turn(correctionAngle));
			}
			
			defPos = ExtensiveFoeBotCalc.limitYPointOnVector(defPos, ourGoalCenter.subtractNew(defPos), defPos,
					yLimitDefenders);
			
			defPos = Geometry.getPenaltyAreaOur()
					.nearestPointOutside(defPos, DefenseAux.penAreaMargin());
			
			return new DefensePoint(defPos, foeBotData);
		}
		
		return new DefensePoint(foeBotData.getBot2goalNearestToBot(), foeBotData);
	}
}
