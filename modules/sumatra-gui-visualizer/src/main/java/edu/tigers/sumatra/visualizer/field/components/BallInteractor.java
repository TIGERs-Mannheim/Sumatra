/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.components;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;


public class BallInteractor implements IWorldFrameObserver
{
	private WorldFrameWrapper lastWorldFrameWrapper = null;


	private IVector3 doChipKick(final IVector2 posIn, final double dist, final int numTouchdown)
	{
		double kickSpeed = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getChipConsultant()
				.getInitVelForDistAtTouchdown(dist, numTouchdown);
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		return Geometry.getBallFactory().createChipConsultant()
				.speedToVel(posIn.subtractNew(ballPos).getAngle(), kickSpeed);
	}


	private IVector3 doStraightKick(final IVector2 posIn, final double dist, final double passEndVel)
	{
		double speed = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getStraightConsultant().getInitVelForDist(
				dist,
				passEndVel);
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		return Vector3.from2d(posIn.subtractNew(ballPos).scaleTo(speed), 0);
	}


	public void onFieldClick(IVector2 pos, MouseEvent e)
	{
		if (SwingUtilities.isRightMouseButton(e))
		{
			placeBall(pos, e);
		} else if (SwingUtilities.isMiddleMouseButton(e))
		{
			resetBall(pos);
		}
	}


	private void placeBall(IVector2 posIn, MouseEvent e)
	{
		IVector2 ballPos = lastWorldFrameWrapper.getSimpleWorldFrame().getBall().getPos();
		double dist = VectorMath.distancePP(ballPos, posIn);

		boolean ctrl = e.isControlDown();
		boolean shift = e.isShiftDown();
		boolean alt = e.isAltDown();

		IVector2 pos;
		IVector3 vel;
		if (ctrl && shift)
		{
			pos = ballPos;
			if (alt)
			{
				vel = doChipKick(posIn, dist, 2);
			} else
			{
				vel = doStraightKick(posIn, dist, 2);
			}
		} else if (ctrl)
		{
			pos = ballPos;
			if (alt)
			{
				vel = Geometry.getBallFactory().createChipConsultant()
						.speedToVel(posIn.subtractNew(ballPos).getAngle(), RuleConstraints.getMaxBallSpeed() - 0.001);
			} else
			{
				vel = Vector3.from2d(posIn.subtractNew(ballPos).scaleTo(RuleConstraints.getMaxBallSpeed() - 0.001), 0);
			}
		} else if (shift)
		{
			pos = ballPos;
			if (alt)
			{
				vel = doChipKick(posIn, dist, 0);
			} else
			{
				vel = doStraightKick(posIn, dist, 0);
			}
		} else
		{
			pos = posIn;
			vel = Vector3f.ZERO_VECTOR;
		}
		SumatraModel.getInstance().getModuleOpt(AVisionFilter.class).ifPresent(
				visionFilter -> visionFilter.placeBall(Vector3.from2d(pos, 0), vel.multiplyNew(1e3))
		);
	}


	private void resetBall(IVector2 pos)
	{
		SumatraModel.getInstance().getModuleOpt(AVisionFilter.class).ifPresent(
				visionFilter -> visionFilter.resetBall(Vector3.from2d(pos, 0), Vector3f.ZERO_VECTOR)
		);
	}


	@Override
	public void onNewWorldFrame(WorldFrameWrapper wFrameWrapper)
	{
		lastWorldFrameWrapper = wFrameWrapper;
	}
}
