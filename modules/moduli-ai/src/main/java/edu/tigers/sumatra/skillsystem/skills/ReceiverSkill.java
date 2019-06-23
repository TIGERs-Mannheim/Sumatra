/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * Receive a ball and stop it
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReceiverSkill extends AReceiveSkill
{
	@Configurable(comment = "Dribbler speed [rpm] for receiving the ball")
	private static int		dribbleSpeed		= 10000;
	
	@Configurable(comment = "Vel [m/s] when ball is considered moving")
	private static double	ballVelMoveBorder	= 0.3;
	
	@Configurable(comment = "max speed [m/s] the bot move back when recieving")
	private static double	maxMoveBackSpeed	= 2;
	
	@Configurable(comment = "max speed [m/s] till the bot can controll the ball without moving")
	private static double	safePassSpeed		= 2;
	
	
	private IVector3			curDest				= null;
	
	/**
	 */
	public enum EReceiverMode
	{
		/**  */
		KEEP_DRIBBLING,
		/**  */
		STOP_DRIBBLER
	}
	
	/**
	 */
	public enum EReceiverState
	{
		/**  */
		RECEIVING,
	}
	
	
	/**
	 * @param mode
	 */
	public ReceiverSkill(final EReceiverMode mode)
	{
		super(ESkill.RECEIVER);
		
		IState receive = new ReceiveState();
		setInitialState(receive);
	}
	
	
	@Override
	protected IVector3 getPose()
	{
		if (curDest == null)
		{
			curDest = new Vector3(getDesiredDestination(), 0);
		}
		Vector3 pose = new Vector3(curDest);
		
		if (getWorldFrame().getBall().getVel().getLength() < ballVelMoveBorder)
		{
			double bot2Ball = getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			pose = new Vector3(getDesiredDestination(), bot2Ball);
			return pose;
		}
		
		TrackedBall ball = getWorldFrame().getBall();
		ILine ballLine = new Line(ball.getPos(), ball.getVel().multiplyNew(1000));
		
		IVector2 dest = GeoMath.leadPointOnLine(getPos(), ballLine);
		if (GeoMath.distancePP(ball.getPos(), getPos()) > 200)
		{
			pose.set(2, ball.getPos().subtractNew(dest).getAngle());
		}
		
		// drive backwards
		// the distance should be calculated basedon the ball speed
		// double ballVel = ball.getVelByPos(getTBot().getBotKickerPos());
		// double ballVelDiff = Math.max(0, ballVel - safePassSpeed);
		// double acc = getMoveCon().getMoveConstraints().getAccMax();
		// double speedUpDistance = (2 / acc) * Math.pow(ballVelDiff, 2) * 1000;
		// if ((GeoMath.distancePP(getPos(), ball.getPos()) < speedUpDistance) && (ballVelDiff > 0))
		// {
		// double dist = (2 * speedUpDistance) + (10 * ballVelDiff); // (ballVelDiff / acc)
		// // double dist = 50;
		// dest = dest.addNew(ball.getVel().scaleToNew(dist));
		// }
		
		pose.set(0, dest.x());
		pose.set(1, dest.y());
		
		List<IDrawableShape> shapes = new ArrayList<>(1);
		shapes.add(new DrawableLine(ballLine, Color.magenta));
		getPathDriver().setShapes(EShapesLayer.RECEIVER_SKILL, shapes);
		
		return pose;
	}
	
	
	private class ReceiveState implements IState
	{
		@Override
		public void doEntryActions()
		{
			getMatchCtrl().setDribblerSpeed(dribbleSpeed);
			if (getDesiredDestination() == null)
			{
				setDesiredDestination(getPos());
			}
			curDest = new Vector3(getDesiredDestination(),
					getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle());
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getWorldFrame().getBall().getVel().getLength() < ballVelMoveBorder)
			{
				getMatchCtrl().setDribblerSpeed(0);
			} else if (!getTBot().hasBallContact())
			{
				getMatchCtrl().setDribblerSpeed(dribbleSpeed);
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return EReceiverState.RECEIVING;
		}
	}
	
	
	/**
	 * @param dest dest
	 */
	public final void setReceiveDestination(final IVector2 dest)
	{
		setDesiredDestination(dest);
	}
}
