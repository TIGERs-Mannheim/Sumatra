/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Lukas Magel
 */
public class BotCollisionDetector extends AGameEventDetector
{
	private static final int	priority						= 1;
	
	@Configurable(comment = "[m/s] The velocity threshold above with a bot contact is considered a crash")
	private static double		CRASH_VEL_THRESHOLD		= 2.0;
	
	@Configurable(comment = "[m/s] The contact is only considered a crash if the speed of both bots differ by at least this value")
	private static double		MIN_SPEED_DIFF				= 2.2;
	
	@Configurable(comment = "[ms] Wait time before reporting a crash with a robot again")
	private static double		CRASH_COOLDOWN_TIME_MS	= 1_000;
	
	@Configurable(comment = "Adjust the bot to bot distance that is considered a contact: dist * factor")
	private static double		MIN_DISTANCE_FACTOR		= 0.9;
	
	private Map<BotID, Long>	lastViolators				= new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(BotCollisionDetector.class);
	}
	
	
	/**
	 * 
	 */
	public BotCollisionDetector()
	{
		super(EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		List<ITrackedBot> yellowBots = AutoRefUtil.filterByColor(bots, ETeamColor.YELLOW);
		List<ITrackedBot> blueBots = AutoRefUtil.filterByColor(bots, ETeamColor.BLUE);
		
		long curTS = frame.getTimestamp();
		for (ITrackedBot blueBot : blueBots)
		{
			if (botStillOnCoolDown(blueBot.getBotId(), curTS))
			{
				continue;
			}
			lastViolators.remove(blueBot.getBotId());
			for (ITrackedBot yellowBot : yellowBots)
			{
				if (botStillOnCoolDown(yellowBot.getBotId(), curTS))
				{
					continue;
				}
				lastViolators.remove(yellowBot.getBotId());
				
				if (VectorMath.distancePP(blueBot.getPos(),
						yellowBot.getPos()) <= (2 * Geometry.getBotRadius() * MIN_DISTANCE_FACTOR))
				{
					IVector2 blueVel = blueBot.getVel();
					IVector2 yellowVel = yellowBot.getVel();
					double crashVel = calcCrashVelocity(blueVel, yellowVel);
					double velDiff = blueVel.getLength() - yellowVel.getLength();
					if ((crashVel > CRASH_VEL_THRESHOLD) && (Math.abs(velDiff) > MIN_SPEED_DIFF))
					{
						BotID violatorID;
						double violatorSpeed;
						IVector2 kickPos;
						if (velDiff > 0)
						{
							violatorID = blueBot.getBotId();
							violatorSpeed = blueVel.getLength();
							kickPos = blueBot.getPos();
						} else
						{
							violatorID = yellowBot.getBotId();
							violatorSpeed = yellowVel.getLength();
							kickPos = yellowBot.getPos();
						}
						lastViolators.put(blueBot.getBotId(), curTS);
						lastViolators.put(yellowBot.getBotId(), curTS);
						
						kickPos = AutoRefMath.getClosestFreekickPos(kickPos, violatorID.getTeamColor().opposite());
						
						FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, violatorID.getTeamColor()
								.opposite(), kickPos);
						GameEvent violation = new SpeedViolation(EGameEvent.BOT_COLLISION, frame.getTimestamp(),
								violatorID, followUp, violatorSpeed);
						return Optional.of(violation);
					}
				}
				
			}
		}
		
		return Optional.empty();
	}
	
	
	private boolean botStillOnCoolDown(final BotID bot, final long curTS)
	{
		if (lastViolators.containsKey(bot))
		{
			Long ts = lastViolators.get(bot);
			if ((curTS - ts) < (CRASH_COOLDOWN_TIME_MS * 1_000_000))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * The function splits each vector into its perpendicular parts (x|y). The x values represent the parts of the
	 * vectors that point in the same direction and the y values represent the parts of the vectors that point towards
	 * each other.
	 * The x values are subtracted from each other to calculate the relative velocity with which both robots travel in
	 * the same direction. The y values are added up two calculate the relative velocity with which the robots travel
	 * towards each other:
	 * Result = |ax - bx| + (ay + by)
	 * Result = |cos(alpha/2) * |va| - cos(alpha/2) * |vb|| + (sin(alpha/2) * |va| + sin(alpha/2) * |vb|)
	 * Result = cos(alpha/2) * ||va| - |vb|| + sin(alpha/2) * (|va| + |vb|)
	 * 
	 * <pre>
	 * 
	 *   va----x----vb
	 *    :\   |   /:
	 * ax : \  |  / : bx
	 *    :  \^|^/  :
	 *    :   \|/<--alpha / 2
	 *    :....*....:
	 *     ay    by
	 * </pre>
	 * 
	 * @param va
	 * @param vb
	 * @return
	 */
	private double calcCrashVelocity(final IVector2 va, final IVector2 vb)
	{
		double angle = va.angleToAbs(vb).orElse(0.0);
		double a = va.getLength();
		double b = vb.getLength();
		return (Math.sin(angle / 2) * (a + b)) + (Math.cos(angle / 2) * Math.abs(a - b));
	}
	
	
	@Override
	public void reset()
	{
	}
	
}
