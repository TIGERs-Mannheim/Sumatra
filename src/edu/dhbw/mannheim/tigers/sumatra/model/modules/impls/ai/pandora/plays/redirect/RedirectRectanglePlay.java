/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 12, 2014
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
 * Redirect based on a desired angle. Only works with 4 roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectRectanglePlay extends ARedirectPlay
{
	@Configurable(comment = "Desired redirect angle in degree")
	private static float	angleDeg	= 45;
	
	
	/**
	 * 
	 */
	public RedirectRectanglePlay()
	{
		super(EPlay.REDIRECT_ANGLE);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		if (getRoles().size() == 4)
		{
			float y = getDistance();
			float angle = AngleMath.DEG_TO_RAD * angleDeg;
			double x = y / Math.tan(angle);
			
			List<IVector2> dests = new ArrayList<IVector2>(4);
			if (angleDeg > 45)
			{
				dests.add(new Vector2(-x, -y));
				dests.add(new Vector2(-x, y));
				dests.add(new Vector2(x, -y));
				dests.add(new Vector2(x, y));
			} else
			{
				dests.add(new Vector2(x, -y));
				dests.add(new Vector2(x, y));
				dests.add(new Vector2(-x, -y));
				dests.add(new Vector2(-x, y));
			}
			return dests;
		}
		return new ArrayList<IVector2>();
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		switch (roleIdx)
		{
			case 0:
				return 2;
			case 1:
				return 3;
			case 2:
				return 1;
			case 3:
				return 0;
		}
		return 0;
	}
}
