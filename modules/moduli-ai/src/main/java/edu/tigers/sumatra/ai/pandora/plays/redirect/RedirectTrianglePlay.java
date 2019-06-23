/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * Yet another redirect test play
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RedirectTrianglePlay extends ARedirectPlay
{
	@Configurable(defValue = "70.0")
	private static double angleDeg = 70;
	
	@Configurable(defValue = "3000.0")
	private static double distance = 3000;
	
	@Configurable(defValue = "2000.0;1000.0")
	private static IVector2 redirectPos = Vector2.fromXY(2000, 1000);
	
	@Configurable(defValue = "-4.0;-1.0")
	private static IVector2 dir = Vector2.fromXY(-4, -1);
	
	private AthenaAiFrame latestFrame = null;
	
	
	/**
	 * Default
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
		IVector2 destSec1 = redirectPos.addNew(dir.turnNew(AngleMath.deg2rad(angleDeg) / 2.0).scaleTo(distance));
		IVector2 destSec2 = redirectPos.addNew(dir.turnNew(AngleMath.deg2rad(-angleDeg) / 2.0).scaleTo(distance));
		List<IVector2> formation = new ArrayList<>(3);
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
				return getRoleIdxFarerFromBall();
			default:
				return 0;
		}
	}
	
	
	private int getRoleIdxFarerFromBall()
	{
		if (getRoles().size() < 2)
		{
			return 0;
		}
		double maxDist = 0;
		int idx = 0;
		for (int i = 1; i < getRoles().size(); i++)
		{
			double dist = VectorMath.distancePP(latestFrame.getWorldFrame().getBall().getPos(),
					getRoles().get(i).getPos());
			if (dist > maxDist)
			{
				maxDist = dist;
				idx = i;
			}
		}
		return idx;
	}
}
