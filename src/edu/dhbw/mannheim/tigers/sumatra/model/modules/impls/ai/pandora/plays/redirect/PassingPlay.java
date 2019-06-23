/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 8, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Pass between bots, stopping the ball each time
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PassingPlay extends ARedirectPlay
{
	@Configurable(comment = "Center of the circle")
	private static IVector2	center	= new Vector2(0, 0);
	
	
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
		float angleStep = AngleMath.PI_TWO / getRoles().size();
		float initialAngle = AngleMath.PI_QUART;
		
		List<IVector2> destinations = new ArrayList<IVector2>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			destinations.add(center.addNew(new Vector2(initialAngle + (angleStep * i)).scaleTo(getDistance())));
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
