/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 20, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.driver;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.trajectory.DribblePath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 */
public class PullBallPathDriver extends PositionDriver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(PullBallPathDriver.class.getName());
	
	private DribblePath				path					= null;
	
	private double						t						= 0;
	
	private long						startTime			= 0;
	
	@Configurable
	private static double			speed					= 1.0;
	
	@Configurable
	private static double			offsetDist			= 70.0;
	
	@Configurable
	private static double			getControlTime		= 1.0;
	
	@Configurable
	private static double			corTime				= 0.4;
	
	private IVector2					staticOffset		= null;
	
	private IVector2					dStaticOffset		= null;
	
	private double						initOrient			= 0.0;
	
	@SuppressWarnings("unused")
	private IVector2					initBallPos			= null;
	
	private long						ballContactTimer	= 0;
	
	
	static
	{
		ConfigRegistration.registerClass("skills", PullBallPathDriver.class);
	}
	
	
	/**
	 * @param path
	 */
	public PullBallPathDriver(final DribblePath path)
	{
		this.path = path;
	}
	
	
	@Override
	public EPathDriver getType()
	{
		return EPathDriver.PULL_BALL_PATH;
	}
	
	
	@Override
	public void update(final ITrackedBot bot, final ABot aBot, final WorldFrame wFrame)
	{
		final List<IDrawableShape> shapes = new ArrayList<>();
		if (startTime == 0)
		{
			// some initial stuff
			startTime = wFrame.getTimestamp();
			IVector2 offset = wFrame.getBall().getPos().subtractNew(path.getPosition(0));
			staticOffset = offset;
			path.setOffset(offset);
			initBallPos = wFrame.getBall().getPos();
			initOrient = path.getPosition(0.1).subtractNew(path.getPosition(0)).getAngle();
			// log.warn(offset);
		} else if (((wFrame.getTimestamp() - startTime) * 1e-9) < getControlTime)
		{
			// get initial ball contact, move slowly towards and inflict rotation.
			double dif = (wFrame.getTimestamp() - startTime) * 1e-9; // timeDif in s
			IVector2 offset = wFrame.getBall().getPos().subtractNew(bot.getPos())
					.scaleToNew((dif / getControlTime) * offsetDist);
			path.setOffset(offset.addNew(staticOffset));
			dStaticOffset = path.getOffset();
			ballContactTimer = wFrame.getTimestamp();
			IVector2 dest = path.getPosition(0);
			setDestination(dest);
			setOrientation(initOrient);
			shapes.add(path.getDrawablePath(10, Color.green));
		} else
		{
			// follow dribble path (with ball)
			// if (!bot.hasBallContact())
			// {
			// // // no more ball contact. Bot tries to get Control back !
			// // double errorTimeDif = ((wFrame.getTimestamp() - ballContactTimer) * 1e-9);
			// // double corProgress = errorTimeDif / corTime;
			// // if (errorTimeDif > corTime && !bot.hasBallContact())
			// // {
			// // ballContactTimer = wFrame.getTimestamp();
			// // dStaticOffset = path.getOffset();
			// // }
			// //
			// // IVector2 kickerPos = bot.getBotKickerPos();
			// // IVector2 kickerToBall = wFrame.getBall().getPos().subtractNew(kickerPos);
			// // path.setOffset(dStaticOffset.addNew(kickerToBall.scaleToNew(corProgress * kickerToBall.getLength())));
			// //
			// // double dif = (wFrame.getTimestamp() - startTime) * 1e-9; // timeDif in s
			// // t = (dif - getControlTime) * speed * (1 - corProgress);
			// // shapes.add(path.getDrawablePath(10, Color.yellow));
			// } else
			// {
			double dif = (wFrame.getTimestamp() - startTime) * 1e-9; // timeDif in s
			double errorTimeDif = ((wFrame.getTimestamp() - ballContactTimer) * 1e-9);
			double corProgress = errorTimeDif / corTime;
			if ((errorTimeDif > corTime) && !bot.hasBallContact())
			{
				ballContactTimer = wFrame.getTimestamp();
				dStaticOffset = path.getOffset();
				t = ((dif - getControlTime) * speed);
			} else if (!bot.hasBallContact())
			{
				IVector2 kickerPos = bot.getBotKickerPos();
				IVector2 kickerToBall = wFrame.getBall().getPos().subtractNew(kickerPos);
				path.setOffset(dStaticOffset.addNew(kickerToBall.scaleToNew(corProgress * kickerToBall.getLength())));
				shapes.add(path.getDrawablePath(10, Color.yellow));
				t = (dif - getControlTime) * speed; // times corProgress to reduce speed while cor
			} else
			{
				ballContactTimer = wFrame.getTimestamp();
				t = ((dif - getControlTime) * speed);
				shapes.add(path.getDrawablePath(10, Color.CYAN));
			}
			// }
			IVector2 dest = path.getPosition(t);
			setDestination(dest);
			double targetOrient = path.getTarget().subtractNew(path.getPosition(0)).getAngle();
			double difRot = AngleMath.getShortestRotation(initOrient, targetOrient);
			setOrientation(initOrient + (difRot * t));
		}
		setShapes(EShapesLayer.OFFENSIVE, shapes);
	}
	
	
	/**
	 * @return t > 1.0
	 */
	public boolean isFinished()
	{
		return t >= 1.0;
	}
	
	
	/**
	 * @return progress 0 = started , 1 = finished, ]0,1[ in progress.
	 */
	public double getProgress()
	{
		return t;
	}
}
