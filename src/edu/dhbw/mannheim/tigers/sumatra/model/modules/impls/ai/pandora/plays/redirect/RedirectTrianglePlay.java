/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Yet another redirect test play
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectTrianglePlay extends ARedirectPlay
{
	@Configurable
	private static float		angleDeg		= 45;
	
	@Configurable
	private static float		distance		= 3000;
	
	@Configurable
	private static IVector2	redirectPos	= new Vector2(2000, 1000);
	
	@Configurable
	private static IVector2	dir			= new Vector2(-4, -1);
	
	private AthenaAiFrame	latestFrame	= null;
	
	
	/**
	 * 
	 */
	public RedirectTrianglePlay()
	{
		super(EPlay.REDIRECT_TRIANGLE);
	}
	
	
	@Override
	public void updateBeforeRoles(final AthenaAiFrame frame)
	{
		latestFrame = frame;
		super.updateBeforeRoles(frame);
	}
	
	
	@Override
	protected List<IVector2> getFormation()
	{
		IVector2 destSec1 = redirectPos.addNew(dir.turnNew((angleDeg * AngleMath.DEG_TO_RAD) / 2).scaleTo(distance));
		IVector2 destSec2 = redirectPos.addNew(dir.turnNew((-angleDeg * AngleMath.DEG_TO_RAD) / 2).scaleTo(distance));
		List<IVector2> formation = new ArrayList<IVector2>(3);
		formation.add(redirectPos);
		formation.add(destSec1);
		formation.add(destSec2);
		return formation;
	}
	
	
	@Override
	protected void getReceiveModes(final Map<ARole, EReceiveMode> modes)
	{
		if (getRoles().size() == 3)
		{
			modes.put(getRoles().get(0), EReceiveMode.REDIRECT);
			modes.put(getRoles().get(1), EReceiveMode.RECEIVE);
			modes.put(getRoles().get(2), EReceiveMode.RECEIVE);
		}
	}
	
	
	@Override
	protected int getReceiverTarget(final int roleIdx)
	{
		switch (roleIdx)
		{
			case 0:
				return getRoleIdxNearerToBall();
			case 1:
				return 0;
			case 2:
				return 0;
		}
		return 0;
	}
	
	
	private int getRoleIdxNearerToBall()
	{
		if (getRoles().size() < 2)
		{
			return 0;
		}
		float maxDist = 0;
		int idx = 0;
		for (int i = 1; i < getRoles().size(); i++)
		{
			float dist = GeoMath.distancePP(latestFrame.getWorldFrame().getBall().getPos(), getRoles().get(i).getPos());
			if (dist > maxDist)
			{
				maxDist = dist;
				idx = i;
			}
		}
		return idx;
	}
}
