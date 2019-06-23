/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Redirect based on a desired angle. Only works with 4 roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectDualPlay extends ARedirectPlay
{
	@Configurable(defValue = "3000.0")
	private static double	distance		= 3000;
	
	@Configurable(defValue = "true")
	private static boolean	receiveOnly	= true;
	
	
	/**
	 * 
	 */
	public RedirectDualPlay()
	{
		super(EPlay.REDIRECT_DUAL);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		List<IVector2> dests = new ArrayList<IVector2>(2);
		dests.add(Vector2.fromXY(distance / 2, 1000));
		dests.add(Vector2.fromXY(-distance / 2, 1000));
		return dests;
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
		}
		return 0;
	}
}
