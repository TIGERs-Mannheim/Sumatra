/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.finisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * A trajectory generated from local velocity commands, sampled at discrete time-stamps.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class CommandListTrajectory
{
	private static final double SAMPLING_INTERVAL = 0.02;
	
	private final List<PoseWithTime> samples = new ArrayList<>();
	
	
	public CommandListTrajectory(final List<SkillCommand> commands, final ITrackedBot bot)
	{
		MoveSegment segment = new MoveSegment(bot);
		
		for (int i = 0; i < (commands.size() - 1); i++)
		{
			SkillCommand cmd = commands.get(i);
			SkillCommand next = commands.get(i + 1);
			
			if (cmd.getXyVel() != null)
			{
				segment.finalVel = cmd.getXyVel();
			}
			
			if (cmd.getaVel() != null)
			{
				segment.finalAngularVel = cmd.getaVel();
			}
			
			if (cmd.getAccMaxXY() != null)
			{
				segment.maxAccXY = cmd.getAccMaxXY();
			}
			
			if (cmd.getAccMaxW() != null)
			{
				segment.maxAccW = cmd.getAccMaxW();
			}
			
			segment.duration = next.getTime() - cmd.getTime();
			
			samples.addAll(segment.generatePath(SAMPLING_INTERVAL));
			
			segment = new MoveSegment(segment);
		}
	}
	
	private static class PoseWithTime
	{
		private Pose pose;
		private double time;
		
		
		private PoseWithTime(final Pose pose, final double time)
		{
			this.pose = pose;
			this.time = time;
		}
	}
	
	private static class MoveSegment
	{
		private IVector2 finalVel;
		private double finalAngularVel;
		private double maxAccXY;
		private double maxAccW;
		
		private IVector2 pos;
		private double orient;
		private IVector2 vel;
		private double angularVel;
		
		private double tStart;
		private double duration;
		
		
		/**
		 * Initialize from a tracked bot (first segment).
		 * 
		 * @param bot
		 */
		private MoveSegment(final ITrackedBot bot)
		{
			pos = bot.getBotKickerPos();
			orient = bot.getOrientation();
			vel = BotMath.convertGlobalBotVector2Local(bot.getVel(), bot.getOrientation());
			angularVel = bot.getAngularVel();
			
			finalVel = Vector2f.ZERO_VECTOR;
			finalAngularVel = 0;
			maxAccXY = bot.getMoveConstraints().getAccMax();
			maxAccW = bot.getMoveConstraints().getAccMaxW();
			
			tStart = 0;
		}
		
		
		/**
		 * Initialize from final pose of previous segment.
		 * 
		 * @param copy
		 */
		private MoveSegment(final MoveSegment copy)
		{
			pos = Vector2.copy(copy.pos);
			orient = copy.orient;
			vel = Vector2.copy(copy.vel);
			angularVel = copy.angularVel;
			
			finalVel = Vector2.copy(copy.finalVel);
			finalAngularVel = copy.finalAngularVel;
			maxAccXY = copy.maxAccXY;
			maxAccW = copy.maxAccW;
			duration = copy.duration;
			tStart = copy.tStart + duration;
		}
		
		
		private List<PoseWithTime> generatePath(final double dt)
		{
			List<PoseWithTime> points = new ArrayList<>();
			
			double nextAngularVel;
			IVector2 nextVel;
			
			for (double t = 0; t < duration; t += dt)
			{
				if (Math.abs(finalAngularVel - angularVel) < (maxAccW * dt))
				{
					nextAngularVel = finalAngularVel;
				} else
				{
					nextAngularVel = angularVel + (Math.signum(finalAngularVel - angularVel) * maxAccW * dt);
				}
				
				if (finalVel.distanceTo(vel) < (maxAccXY * dt))
				{
					nextVel = Vector2.copy(finalVel);
				} else
				{
					nextVel = vel.addNew(finalVel.subtractNew(vel).scaleTo(maxAccXY * dt));
				}
				
				double lastOrient = orient;
				orient += (angularVel + nextAngularVel) * dt * 0.5;
				
				IVector2 velGlobal = BotMath.convertLocalBotVector2Global(nextVel.addNew(vel).multiply(0.5 * dt * 1000.0),
						(lastOrient + orient) * 0.5);
				
				pos = pos.addNew(velGlobal);
				
				vel = nextVel;
				angularVel = nextAngularVel;
				
				points.add(new PoseWithTime(Pose.from(pos, orient), tStart + t));
			}
			
			return points;
		}
	}
	
	
	/**
	 * Get total time of this move.
	 * 
	 * @return time in [s]
	 */
	public double getTotalTime()
	{
		if (samples.isEmpty())
		{
			return 0;
		}
		
		return samples.get(samples.size() - 1).time;
	}
	
	
	/**
	 * Get approx. pose at time.
	 * 
	 * @param t in [s]
	 * @return
	 */
	public Pose getPose(final double t)
	{
		if ((t < 0) || samples.isEmpty())
		{
			return Pose.zero();
		}
		
		int index = (int) ((t + (SAMPLING_INTERVAL * 0.5)) / SAMPLING_INTERVAL);
		if (index > samples.size())
		{
			return samples.get(samples.size() - 1).pose;
		}
		
		return samples.get(index).pose;
	}
	
	
	/**
	 * Get final pose at total time.
	 * 
	 * @return
	 */
	public Pose getFinalPose()
	{
		if (samples.isEmpty())
		{
			return Pose.zero();
		}
		
		return samples.get(samples.size() - 1).pose;
	}
	
	
	/**
	 * Get all positions.
	 * 
	 * @return
	 */
	public List<IVector2> getPath()
	{
		return samples.stream().map(p -> p.pose.getPos()).collect(Collectors.toList());
	}
}
