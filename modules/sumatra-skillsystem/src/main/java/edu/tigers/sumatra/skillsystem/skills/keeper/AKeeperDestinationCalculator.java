/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.keeper;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.Getter;


public abstract class AKeeperDestinationCalculator implements IKeeperDestinationCalculator
{
	@Getter(AccessLevel.PROTECTED)
	private WorldFrame worldFrame;
	@Getter(AccessLevel.PROTECTED)
	private ShapeMap shapeMap;
	@Getter(AccessLevel.PROTECTED)
	private ITrackedBot tBot;
	@Getter(AccessLevel.PROTECTED)
	private IVector2 posToCover;


	@Override
	public KeeperDestination calcDestination(WorldFrame worldFrame, ShapeMap shapeMap, ITrackedBot tBot,
			IVector2 posToCover)
	{
		this.worldFrame = worldFrame;
		this.shapeMap = shapeMap;
		this.tBot = tBot;
		if (posToCover != null)
		{
			this.posToCover = Geometry.getField().withMargin(-1).nearestPointInside(posToCover);
		} else
		{
			this.posToCover = null;
		}

		return calcDestination();
	}


	abstract KeeperDestination calcDestination();


	protected ITrackedBall getBall()
	{
		return worldFrame.getBall();
	}


	protected IVector2 getPos()
	{
		return tBot.getPos();
	}


	protected final ShapeMap getShapes()
	{
		return shapeMap;
	}


}
