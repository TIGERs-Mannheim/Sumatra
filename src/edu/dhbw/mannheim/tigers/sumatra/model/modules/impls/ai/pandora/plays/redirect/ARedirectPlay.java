/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.redirect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.line.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.test.RedirectRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * Test redirecting with multiple bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARedirectPlay extends APlay
{
	@Configurable(comment = "dist to center (radius) [mm]")
	private static float		distance				= 1500;
	@Configurable(comment = "Fixed duration for kick+redirect")
	private static int		duration				= 1500;
	
	private boolean			needReorder			= true;
	
	@Configurable
	private static boolean	predictTargetPos	= false;
	
	@Configurable
	private static boolean	turn180degOnWait	= false;
	
	@Configurable(comment = "Assumed avg ball speed for calculating lookahead")
	private static float		avgBallSpeed		= 2.0f;
	
	
	protected enum EReceiveMode
	{
		REDIRECT,
		RECEIVE
	}
	
	
	protected ARedirectPlay(final EPlay playType)
	{
		super(playType);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		needReorder = true;
		return new RedirectRole(new DynamicPosition(AVector2.ZERO_VECTOR));
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
		
	}
	
	
	@Override
	public void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().size() < 2)
		{
			return;
		}
		
		
		List<IVector2> destinations = getFormation();
		
		if (destinations.size() != getRoles().size())
		{
			return;
		}
		
		if (needReorder)
		{
			reorderRolesToDestinations(destinations);
			needReorder = false;
		}
		
		int i = 0;
		for (ARole role : getRoles())
		{
			RedirectRole redRole = (RedirectRole) role;
			redRole.setDesiredDestination(destinations.get(i));
			// get opposite bot
			int roleIdx = getReceiverTarget(i);
			TrackedTigerBot targetBot = getRoles().get(roleIdx).getBot();
			float dist2RedRole = GeoMath.distancePP(frame.getWorldFrame().getBall().getPos(), redRole.getPos());
			float lookahead = 0;
			if (predictTargetPos)
			{
				lookahead = DistanceUnit.MILLIMETERS.toMeters(dist2RedRole + (2 * distance)) / avgBallSpeed;
			}
			if (turn180degOnWait)
			{
				IVector2 ball2Bot = redRole.getPos().subtractNew(frame.getWorldFrame().getBall().getPos());
				redRole.setDesiredOrientation(ball2Bot.getAngle());
			}
			redRole.setTarget(new DynamicPosition(targetBot, lookahead));
			i++;
			redRole.setDuration(duration);
		}
		
		RedirectRole nearest2BallRole = getNearestRoleToBall(frame);
		
		if (frame.getWorldFrame().getBall().getVel().getLength2() < 1f)
		{
			boolean needInit = false;
			for (ARole role : getRoles())
			{
				if (role == nearest2BallRole)
				{
					continue;
				}
				RedirectRole redRole = (RedirectRole) role;
				if (!redRole.getDesiredDestination().equals(redRole.getPos(), 500))
				{
					needInit = true;
				}
			}
			
			// ball is not moving
			if (!needInit && nobodyPassing() && (nearest2BallRole != null))
			{
				if ((nearest2BallRole.getCurrentSkill().getSkillName() != ESkillName.RECEIVER)
						|| nearest2BallRole.getCurrentSkill().isComplete())
				{
					nearest2BallRole.changeToPass();
				}
			}
			for (ARole role : getRoles())
			{
				RedirectRole redRole = (RedirectRole) role;
				if (needInit || (redRole != nearest2BallRole))
				{
					redRole.changeToWait();
				}
			}
		} else
		{
			for (ARole role : getRoles())
			{
				RedirectRole redRole = (RedirectRole) role;
				if ((role != nearest2BallRole))
				{
					redRole.changeToWait();
				} else
				{
					if (!redRole.isReceivingOrRedirecting())
					{
						Map<ARole, EReceiveMode> modes = new HashMap<>();
						getReceiveModes(modes);
						EReceiveMode mode = modes.get(redRole);
						if ((mode == null) || (mode == EReceiveMode.REDIRECT))
						{
							redRole.changeToRedirect();
						} else
						{
							redRole.changeToReceive();
						}
					}
				}
			}
		}
	}
	
	
	protected abstract List<IVector2> getFormation();
	
	
	protected abstract int getReceiverTarget(final int roleIdx);
	
	
	protected abstract void getReceiveModes(Map<ARole, EReceiveMode> modes);
	
	
	private RedirectRole getNearestRoleToBall(final AthenaAiFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		IVector2 ballVel = frame.getWorldFrame().getBall().getVel();
		ARole nearestRole = null;
		float shortestDist = Float.MAX_VALUE;
		boolean ballMoving = ballVel.getLength2() > 0.5f;
		for (ARole role : getRoles())
		{
			float dist;
			if (ballMoving)
			{
				IVector2 lp = GeoMath.leadPointOnLine(role.getPos(), new Line(ballPos, ballVel));
				IVector2 bot2Lp = lp.subtractNew(ballPos);
				// is ball moving into direction of lp? x * ballVel = bot2Lp => x should be > 0
				if ((bot2Lp.x() / (ballVel.x())) < 0)
				{
					continue;
				}
				dist = GeoMath.distancePP(role.getPos(), lp);
			} else
			{
				dist = GeoMath.distancePP(ballPos, role.getPos());
			}
			if (dist < shortestDist)
			{
				shortestDist = dist;
				nearestRole = role;
			}
		}
		return (RedirectRole) nearestRole;
	}
	
	
	private boolean nobodyPassing()
	{
		// for (ARole role : getRoles())
		// {
		// RedirectRole redRole = (RedirectRole) role;
		// if (redRole.isPassing())
		// {
		// return false;
		// }
		// }
		return true;
	}
	
	
	/**
	 * @return the distance
	 */
	protected static float getDistance()
	{
		return distance;
	}
}
