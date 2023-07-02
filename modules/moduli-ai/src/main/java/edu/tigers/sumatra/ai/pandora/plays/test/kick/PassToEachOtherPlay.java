/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.test.kick;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Setter;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;


/**
 * Passing between two robots.
 */
public class PassToEachOtherPlay extends ARedirectPlay
{
	@Setter
	private IVector2 p1;
	@Setter
	private IVector2 p2;
	@Setter
	private EReceiveMode receiveMode1;
	@Setter
	private EReceiveMode receiveMode2;
	@Setter
	private double receiveBallSpeed;


	public PassToEachOtherPlay()
	{
		super(EPlay.PASS_TO_EACH_OTHER);
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();

		var shapes = getAiFrame().getShapeMap().get(EAiShapesLayer.TEST_KICK);
		shapes.add(new DrawableLine(p1, p2, Color.green));
	}


	@Override
	protected boolean ready()
	{
		return getRoles().size() == 2;
	}


	@Override
	protected List<IVector2> getOrigins()
	{
		return Arrays.asList(p1, p2);
	}


	@Override
	protected IVector2 getReceiverTarget(IVector2 origin)
	{
		if (origin == p1)
		{
			return p2;
		}
		if (origin == p2)
		{
			return p1;
		}
		throw new IllegalArgumentException("Unmapped origin: " + origin);
	}


	@Override
	protected EReceiveMode getReceiveMode(IVector2 origin)
	{
		if (origin == p1)
		{
			return receiveMode2;
		}
		if (origin == p2)
		{
			return receiveMode1;
		}
		throw new IllegalArgumentException("Unmapped origin: " + origin);
	}


	@Override
	protected double getMaxReceivingBallSpeed(IVector2 origin)
	{
		return receiveBallSpeed;
	}
}
