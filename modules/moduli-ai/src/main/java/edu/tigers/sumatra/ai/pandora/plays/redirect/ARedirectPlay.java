/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.units.DistanceUnit;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Test redirecting with multiple bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARedirectPlay extends APlay
{
	@Configurable(comment = "dist to center (radius) [mm]", defValue = "3000.0")
	private static double distance = 3000;
	
	private boolean needReorder = true;
	
	@Configurable(defValue = "false")
	private static boolean predictTargetPos = false;
	
	@Configurable(defValue = "false")
	private static boolean turn180degOnWait = false;
	
	@Configurable(comment = "Assumed avg ball speed for calculating lookahead", defValue = "2.0")
	private static double avgBallSpeed = 2.0;
	
	
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
		return new RedirectTestRole(new DynamicPosition(AVector2.ZERO_VECTOR));
	}
	
	
	@Override
	protected void onGameStateChanged(final GameState gameState)
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
			RedirectTestRole redRole = (RedirectTestRole) role;
			redRole.setDesiredDestination(destinations.get(i));
			// get opposite bot
			int roleIdx = getReceiverTarget(i);
			ITrackedBot targetBot = getRoles().get(roleIdx).getBot();
			double dist2RedRole = VectorMath.distancePP(frame.getWorldFrame().getBall().getPos(), redRole.getPos());
			double lookahead = 0;
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
		}
		
		RedirectTestRole nearest2BallRole = getNearestRoleToBall(frame);
		
		if (frame.getWorldFrame().getBall().getVel().getLength2() < 1)
		{
			boolean needInit = false;
			for (ARole role : getRoles())
			{
				if (role == nearest2BallRole)
				{
					continue;
				}
				RedirectTestRole redRole = (RedirectTestRole) role;
				if (!redRole.getDesiredDestination().isCloseTo(redRole.getPos(), 500))
				{
					needInit = true;
				}
			}
			
			// ball is not moving
			if (!needInit && (nearest2BallRole != null))
			{
				nearest2BallRole.changeToPass();
			}
			for (ARole role : getRoles())
			{
				RedirectTestRole redRole = (RedirectTestRole) role;
				if (needInit || (redRole != nearest2BallRole))
				{
					redRole.changeToWait();
				}
			}
		} else
		{
			updateRoleStates(nearest2BallRole);
		}
	}
	
	
	private void updateRoleStates(final RedirectTestRole nearest2BallRole)
	{
		for (ARole role : getRoles())
		{
			RedirectTestRole redRole = (RedirectTestRole) role;
			if (role != nearest2BallRole)
			{
				redRole.changeToWait();
			} else if (!redRole.isReceivingOrRedirecting())
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
	
	
	protected abstract List<IVector2> getFormation();
	
	
	protected abstract int getReceiverTarget(final int roleIdx);
	
	
	protected abstract void getReceiveModes(Map<ARole, EReceiveMode> modes);
	
	
	private RedirectTestRole getNearestRoleToBall(final AthenaAiFrame frame)
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		IVector2 ballVel = frame.getWorldFrame().getBall().getVel();
		ARole nearestRole = null;
		double shortestDist = Double.MAX_VALUE;
		boolean ballMoving = ballVel.getLength2() > 0.5;
		for (ARole role : getRoles())
		{
			double dist;
			if (ballMoving)
			{
				IVector2 lp = LineMath.leadPointOnLine(Line.fromDirection(ballPos, ballVel), role.getPos());
				IVector2 bot2Lp = lp.subtractNew(ballPos);
				// is ball moving into direction of lp? x * ballVel = bot2Lp => x should be > 0
				if ((bot2Lp.x() / (ballVel.x())) < 0)
				{
					continue;
				}
				dist = VectorMath.distancePP(role.getPos(), lp);
			} else
			{
				dist = VectorMath.distancePP(ballPos, role.getPos());
			}
			if (dist < shortestDist)
			{
				shortestDist = dist;
				nearestRole = role;
			}
		}
		return (RedirectTestRole) nearestRole;
	}
	
	
	/**
	 * @return the distance
	 */
	protected static double getDistance()
	{
		return distance;
	}
}
