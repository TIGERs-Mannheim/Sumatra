/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 27, 2016
 * Author(s): Felix Bayer <bayer.fel@googlemail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.defense.data;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author Felix Bayer <bayer.fel@googlemail.com>
 *         Container class for data calculated by the angle defense before the role assigner is executed.
 */
public class AngleDefenseData
{
	private static double		botRadius		= Geometry.getBotRadius();
	private static double		ballRadius		= Geometry.getBallRadius();
	
	private List<FoeBotGroup>	foeBotGroups	= new ArrayList<>();
	
	
	/**
	 * @return
	 */
	public static double getMinDefRadius()
	{
		return Geometry.getPenaltyAreaOur().getLengthOfPenaltyAreaFrontLineHalf()
				+ Geometry.getPenaltyAreaOur().getRadiusOfPenaltyArea() + Geometry.getPenaltyAreaMargin();
	}
	
	
	/**
	 * @return the botWidthAngle
	 */
	public static double getBotWidthAngle()
	{
		return 2 * Math.asin((botRadius + (ballRadius / 2)) / getMinDefRadius());
	}
	
	
	/**
	 * @param ballWorldFrame
	 * @return the ballAngle
	 */
	public double getBallAngle(final TrackedBall ballWorldFrame)
	{
		IVector2 goalCenter = Geometry.getGoalOur().getGoalCenter();
		IVector2 corner = new Vector2(-Geometry.getFieldLength() / 2, Geometry.getFieldWidth() / 2);
		
		IVector2 goal2Ball = DefenseAux.getBallPosDefense(ballWorldFrame).subtractNew(goalCenter);
		IVector2 refVector = corner.subtractNew(goalCenter);
		
		return GeoMath.angleBetweenVectorAndVector(refVector, goal2Ball);
	}
	
	
	/**
	 * @return the foeBotGroups
	 */
	public List<FoeBotGroup> getFoeBotGroups()
	{
		return foeBotGroups;
	}
	
	
	/**
	 * @param newGroup
	 */
	public void addFoeBotGroup(final FoeBotGroup newGroup)
	{
		foeBotGroups.add(newGroup);
	}
	
	
	/**
	 * @param newGroups
	 */
	public void addFoeBotGroups(final List<FoeBotGroup> newGroups)
	{
		foeBotGroups.addAll(newGroups);
	}
	
	
	/**
	 * @return
	 */
	public int nFoeBotGroups()
	{
		return foeBotGroups.size();
	}
}
