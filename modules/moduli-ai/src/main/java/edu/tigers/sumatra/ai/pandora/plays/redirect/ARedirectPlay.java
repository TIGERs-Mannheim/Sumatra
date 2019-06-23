/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Test redirecting with multiple bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class ARedirectPlay extends APlay
{
	@Configurable(defValue = "false")
	private static boolean turn180degOnWait = false;
	
	private boolean needReorder = true;
	
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
	protected ARole onAddRole()
	{
		needReorder = true;
		return new RedirectTestRole(new DynamicPosition(Vector2f.ZERO_VECTOR));
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
		
		assignTargets(frame, destinations);
		
		RedirectTestRole nearest2BallRole = getNearestRoleToBall(frame);
		
		updateRoleStates(frame, nearest2BallRole);
	}
	
	
	private void updateRoleStates(final AthenaAiFrame frame, final RedirectTestRole nearest2BallRole)
	{
		if (frame.getWorldFrame().getBall().getVel().getLength2() < 1.0)
		{
			boolean robotsReady = getRoles().stream().map(r -> (RedirectTestRole) r)
					.noneMatch(RedirectTestRole::isDrivingToDesiredDest);
			
			if (robotsReady && (nearest2BallRole != null))
			{
				nearest2BallRole.changeToPass();
			}
			changeAllExceptNearestToWait(nearest2BallRole);
		} else
		{
			updateRoleStates(nearest2BallRole);
		}
	}
	
	
	private void changeAllExceptNearestToWait(final RedirectTestRole nearest2BallRole)
	{
		for (ARole role : getRoles())
		{
			RedirectTestRole redRole = (RedirectTestRole) role;
			if (redRole != nearest2BallRole)
			{
				redRole.changeToWait();
			}
		}
	}
	
	
	private void assignTargets(final AthenaAiFrame frame, final List<IVector2> destinations)
	{
		for (int i = 0; i < getRoles().size(); i++)
		{
			RedirectTestRole redRole = (RedirectTestRole) getRoles().get(i);
			redRole.setDesiredDestination(destinations.get(i));
			int receiverIdx = getReceiverTarget(i);
			ITrackedBot targetBot = getRoles().get(receiverIdx).getBot();
			redRole.setTarget(new DynamicPosition(targetBot));
			
			if (turn180degOnWait)
			{
				IVector2 ball2Bot = redRole.getPos().subtractNew(frame.getWorldFrame().getBall().getPos());
				redRole.setDesiredOrientation(ball2Bot.getAngle());
			}
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
		boolean ballMoving = ballVel.getLength2() > 1.0;
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
}
