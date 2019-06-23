/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Redirect in a circle
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectCirclePlay extends ARedirectPlay
{
	@Configurable(comment = "Controls the moving speed. Higher=slower")
	private static int							maxCounter	= 1500;
	private int										counter		= 0;
	@Configurable(comment = "Drive in a circle")
	private static boolean						circle		= false;
	
	@Configurable(comment = "Center of the circle")
	private static IVector2						center		= new Vector2(0, 0);
	
	@Configurable
	private static boolean						receive		= false;
	
	private static Map<Integer, Integer>	map4			= new HashMap<Integer, Integer>();
	private static Map<Integer, Integer>	map6			= new HashMap<Integer, Integer>();
	private static Map<Integer, Integer>	map8			= new HashMap<Integer, Integer>();
	static
	{
		map4.put(0, 2);
		map4.put(2, 1);
		map4.put(1, 3);
		map4.put(3, 0);
		
		map6.put(0, 2);
		map6.put(2, 4);
		map6.put(4, 1);
		map6.put(1, 5);
		map6.put(5, 3);
		map6.put(3, 0);
		
		map8.put(0, 3);
		map8.put(3, 6);
		map8.put(6, 1);
		map8.put(1, 4);
		map8.put(4, 7);
		map8.put(7, 2);
		map8.put(2, 5);
		map8.put(5, 0);
	}
	
	
	/**
	 * 
	 */
	public RedirectCirclePlay()
	{
		super(EPlay.REDIRECT_CIRCLE);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		if (circle)
		{
			counter = (counter + 1) % maxCounter;
		} else
		{
			counter = 0;
		}
		double angleStep = AngleMath.PI_TWO / getRoles().size();
		double initialAngle = AngleMath.PI + 0.3;
		initialAngle += ((double) counter / maxCounter) * (AngleMath.PI_TWO);
		
		List<IVector2> destinations = new ArrayList<IVector2>();
		for (int i = 0; i < getRoles().size(); i++)
		{
			destinations.add(center.addNew(new Vector2(initialAngle + (angleStep * i)).scaleTo(getDistance())));
		}
		return destinations;
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		for (ARole role : getRoles())
		{
			if (receive)
			{
				modes.put(role, EReceiveMode.RECEIVE);
			} else
			{
				modes.put(role, EReceiveMode.REDIRECT);
			}
		}
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		switch (getRoles().size())
		{
			case 1:
				return roleIdx;
			case 2:
				// next bot
				return (roleIdx + 1) % getRoles().size();
			case 4:
				return map4.get(roleIdx);
			case 6:
				return map6.get(roleIdx);
			case 8:
				return map8.get(roleIdx);
			case 3:
			case 5:
			case 7:
			default:
				// opposite bot
				return (roleIdx + (getRoles().size() / 2)) % getRoles().size();
		}
	}
	
}
