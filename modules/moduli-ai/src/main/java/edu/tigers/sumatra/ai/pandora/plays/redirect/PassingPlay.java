/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Pass between bots, stopping the ball each time
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PassingPlay extends ARedirectPlay
{
	@Configurable(comment = "Center of the circle", defValue = "0.0;0.0")
	private static IVector2 center = Vector2.fromXY(0, 0);

	@Configurable(comment = "dist to center (radius) [mm]", defValue = "3000.0")
	private static double distance = 3000;
	
	/**
	 * 
	 */
	public PassingPlay()
	{
		super(EPlay.PASSING);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		double angleStep = AngleMath.PI_TWO / getRoles().size();
		double initialAngle = AngleMath.PI_QUART;
		
		List<IVector2> destinations = new ArrayList<>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			destinations.add(center.addNew(Vector2.fromAngle(initialAngle + (angleStep * i)).scaleTo(distance)));
		}
		return destinations;
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		return (roleIdx + 1) % getRoles().size();
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		for (ARole role : getRoles())
		{
			modes.put(role, EReceiveMode.RECEIVE);
		}
	}
}
