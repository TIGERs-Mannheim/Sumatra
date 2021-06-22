/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Setter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


/**
 * Pass between n bots around a circle (pass to the next bot clock-wise), stopping the ball each time.
 */
public class PassAroundACirclePlay extends ARedirectPlay
{
	@Setter
	private IVector2 center;
	@Setter
	private double radius;


	public PassAroundACirclePlay()
	{
		super(EPlay.PASS_AROUND_A_CIRCLE);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableCircle(Circle.createCircle(center, radius), Color.green));
	}


	@Override
	protected List<IVector2> getOrigins()
	{
		double angleStep = AngleMath.PI_TWO / getRoles().size();
		double initialAngle = AngleMath.PI_QUART;

		List<IVector2> destinations = new ArrayList<>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			destinations.add(center.addNew(Vector2.fromAngle(initialAngle + (angleStep * i)).scaleTo(radius)));
		}
		return destinations;
	}


	@Override
	protected IVector2 getReceiverTarget(IVector2 origin)
	{
		var currentIdx = origins.indexOf(origin);
		var nextIdx = (currentIdx + 1) % origins.size();
		return origins.get(nextIdx);
	}


	@Override
	protected EReceiveMode getReceiveMode(IVector2 origin)
	{
		return EReceiveMode.RECEIVE;
	}
}
