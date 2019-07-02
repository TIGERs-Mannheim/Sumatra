/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense.finisher;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.drawable.DrawablePath;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Drive in a S-curve, preferable around an opponent.
 * Ball must be in front of robot.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public abstract class AFinisherMoveSierraOne extends AFinisherMove
{
	private CommandListTrajectory trajectory;
	
	
	public AFinisherMoveSierraOne(final EFinisherMove move, final boolean left)
	{
		super(move);
		
		double dir = left ? 1.0 : -1.0;
		
		SkillCommand turn1 = SkillCommand.command(0.0)
				.withXyVel(Vector2.fromXY(0.27 * dir, 0.5))
				.withAVel(2.5 * dir)
				.withAccMaxXY(3.0)
				.withAccMaxW(35.0)
				.withDribbleSpeed(5000);
		
		SkillCommand turn2 = SkillCommand.command(0.5)
				.withXyVel(Vector2.fromXY(-0.27 * dir, 0.5))
				.withAVel(-2.5 * dir);
		
		SkillCommand forward = SkillCommand.command(1.2)
				.withXyVel(Vector2.fromY(0.5))
				.withAVel(0.0);
		
		SkillCommand noop = SkillCommand.command(1.5);
		
		skillCommands.add(turn1);
		skillCommands.add(turn2);
		skillCommands.add(forward);
		skillCommands.add(noop);
	}
	
	
	@Override
	public boolean isApplicable(final BaseAiFrame aiFrame, final BotID botID)
	{
		ITrackedBot executor = aiFrame.getWorldFrame().getBot(botID);
		if (executor == null)
		{
			return false;
		}
		
		ITrackedBall ball = aiFrame.getWorldFrame().getBall();
		
		if (executor.getBotKickerPos().distanceTo(ball.getPos()) > 30.0)
		{
			return false;
		}
		
		if ((Math.abs(executor.getAngularVel()) > 3.0) || (executor.getVel().getLength2() > 0.5))
		{
			return false;
		}
		
		trajectory = new CommandListTrajectory(skillCommands, executor);
		
		return isValidTrajectory(trajectory, aiFrame);
	}
	
	
	@Override
	public void generateTrajectory(final BaseAiFrame aiFrame, final BotID botID)
	{
		ITrackedBot executor = aiFrame.getWorldFrame().getBot(botID);
		trajectory = new CommandListTrajectory(skillCommands, executor);
	}
	
	
	@Override
	public Pose getKickLocation(final BaseAiFrame aiFrame)
	{
		return trajectory.getFinalPose();
	}
	
	
	@Override
	public List<IDrawableShape> generateShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();
		
		if (trajectory != null)
		{
			shapes.add(new DrawablePath(trajectory.getPath(), Color.GRAY));
		}
		
		return shapes;
	}
	
	public static class FinisherMoveSierraOneLeft extends AFinisherMoveSierraOne
	{
		public FinisherMoveSierraOneLeft()
		{
			super(EFinisherMove.SIERRA_ONE_LEFT, true);
		}
	}
	
	public static class FinisherMoveSierraOneRight extends AFinisherMoveSierraOne
	{
		public FinisherMoveSierraOneRight()
		{
			super(EFinisherMove.SIERRA_ONE_RIGHT, false);
		}
	}
}
