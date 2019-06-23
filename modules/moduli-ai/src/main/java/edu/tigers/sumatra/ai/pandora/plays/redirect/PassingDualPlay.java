/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Passing between two robots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PassingDualPlay extends ARedirectPlay
{
	@Configurable(defValue = "-1200.0;600.0")
	private static IVector2 p1 = Vector2.fromXY(-1200, 600);
	@Configurable(defValue = "1200.0;-600.0")
	private static IVector2 p2 = Vector2.fromXY(1200, -600);
	
	@Configurable(defValue = "true")
	private static boolean receiveOnly = true;
	
	
	/**
	 * 
	 */
	public PassingDualPlay()
	{
		super(EPlay.REDIRECT_DUAL);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		return Arrays.asList(p1, p2);
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		if (getRoles().size() == 2)
		{
			modes.put(getRoles().get(0), EReceiveMode.RECEIVE);
			if (receiveOnly)
			{
				modes.put(getRoles().get(1), EReceiveMode.RECEIVE);
			} else
			{
				modes.put(getRoles().get(1), EReceiveMode.REDIRECT);
			}
		}
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		switch (roleIdx)
		{
			case 0:
				return 1;
			case 1:
				return 0;
			default:
				return 0;
		}
	}
}
