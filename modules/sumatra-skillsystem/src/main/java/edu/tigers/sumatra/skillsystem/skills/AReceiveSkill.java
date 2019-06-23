/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import static edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc.CatchBallResult;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.CachedBallInterception;
import edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc;


/**
 * Receive a ball and stop it or redirect it
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AReceiveSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AReceiveSkill.class.getName());
	@Configurable(defValue = "false")
	private static boolean debugShapes = false;
	private IVector2 desiredDestination = null;
	private double lastTargetOrientation = 0;
	private EKickerDevice kickerDevice = EKickerDevice.STRAIGHT;
	private double kickSpeed = 0;
	private double dribblerSpeed = 0;
	private ExponentialMovingAverageFilter2D filter = null;
	
	
	/**
	 * @param skillName ESkill name
	 */
	AReceiveSkill(final ESkill skillName)
	{
		super(skillName);
	}
	
	protected class AReceiveState extends MoveToState
	{
		private CatchBallCalc catchBallCalc;
		
		
		protected AReceiveState()
		{
			super(AReceiveSkill.this);
		}
		
		
		@Override
		public void doEntryActions()
		{
			super.doEntryActions();
			getMoveCon().setBallObstacle(false);
			getMoveCon().setOurBotsObstacle(false);
			getMoveCon().setMinDistToBall(Geometry.getBallRadius() + Geometry.getBotRadius());
			
			catchBallCalc = new CatchBallCalc(getBotId());
			catchBallCalc.setCalcTargetOrientation(AReceiveSkill.this::calcTargetOrientation);
		}
		
		
		@Override
		public void doUpdate()
		{
			catchBallCalc.setMoveCon(getMoveCon());
			catchBallCalc.update(getWorldFrame());
			catchBallCalc.setExternallySetDestination(desiredDestination);
			
			CatchBallResult catchBallResult = catchBallCalc.calculate();
			IVector2 kickerDest = catchBallResult.getKickerDest();
			double dist2Dest = kickerDest.distanceTo(getKickerPos());
			getMoveCon().setTheirBotsObstacle(dist2Dest > 1000);
			
			IVector2 botPos = filterPosition(catchBallResult.getBotDest());
			getMoveCon().updateDestination(botPos);
			getMoveCon().updateTargetAngle(catchBallResult.getTargetOrientation());
			lastTargetOrientation = catchBallResult.getTargetOrientation();
			
			drawShapes();
			
			super.doUpdate();
		}
		
		
		private IVector2 filterPosition(IVector2 kickerDest)
		{
			if (filter == null || filter.getState().getXYVector().distanceToSqr(kickerDest) > 200 * 200)
			{
				filter = new ExponentialMovingAverageFilter2D(0.95, kickerDest);
			} else
			{
				kickerDest = filter.update(kickerDest);
			}
			return kickerDest;
		}
		
		
		private IVector2 getKickerPos()
		{
			return getTBot().getPos().addNew(
					Vector2.fromAngle(getAngle()).scaleTo(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius()));
		}
		
		
		private void drawShapes()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			
			// draw desired dest
			if (desiredDestination != null)
			{
				DrawableCircle dc2 = new DrawableCircle(Circle.createCircle(desiredDestination, 250),
						new Color(0, 255, 255, 100));
				dc2.setFill(true);
				shapes.add(dc2);
			}
			
			if (debugShapes)
			{
				drawSlackTime(shapes);
			}
			
			final CachedBallInterception cachedBallInterception = catchBallCalc.getCachedBallInterception();
			shapes.add(createPoint(cachedBallInterception.getOptimalInterceptionTime(), Color.red, 50));
			shapes.add(createPoint(cachedBallInterception.getInterceptionTime(), Color.GREEN, 40));
			shapes.add(createPoint(cachedBallInterception.getInitialInterceptionTime(), Color.BLUE, 30));
			
			double slackTime = cachedBallInterception.getInterceptionSlackTime();
			shapes.add(new DrawableAnnotation(getMoveCon().getDestination(), String.format("%.2f", slackTime)));
			
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL).addAll(shapes);
		}
		
		
		private void drawSlackTime(final List<IDrawableShape> shapes)
		{
			IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();
			if (catchBallCalc.getCachedBallInterception().getOptimalBallInterception() != null)
			{
				for (double t = 0; t < getBall().getTrajectory().getTimeByVel(0.0); t += 0.01)
				{
					double slackTime = catchBallCalc.getCachedBallInterception().getOptimalBallInterception().slackTime(t);
					Color color;
					if (slackTime > 1)
					{
						color = Color.black;
					} else if (slackTime < 0)
					{
						color = Color.WHITE;
					} else
					{
						double relSlackTime = slackTime / 1;
						relSlackTime = Math.max(0, relSlackTime);
						relSlackTime = Math.min(1, relSlackTime);
						color = colorPicker.getColor(1 - relSlackTime);
					}
					IVector2 pos = getBall().getTrajectory().getPosByTime(t).getXYVector();
					DrawablePoint p = new DrawablePoint(pos, color);
					shapes.add(p);
				}
			}
		}
		
		
		private DrawablePoint createPoint(final double t, final Color color, final double size)
		{
			IVector2 p = getWorldFrame().getBall().getTrajectory().getPosByTime(t).getXYVector();
			return new DrawablePoint(p, color).withSize(size);
		}
	}
	
	
	@Override
	protected void updateKickerDribbler(final KickerDribblerCommands kickerDribblerOutput)
	{
		super.updateKickerDribbler(kickerDribblerOutput);
		kickerDribblerOutput.setKick(kickSpeed, kickerDevice, EKickerMode.ARM);
		kickerDribblerOutput.setDribblerSpeed(dribblerSpeed);
	}
	
	
	protected abstract double calcTargetOrientation(final IVector2 kickerPos);
	
	
	/**
	 * @param desiredDestination the desiredDestination to set
	 */
	public void setDesiredDestination(final IVector2 desiredDestination)
	{
		this.desiredDestination = desiredDestination;
	}
	
	
	/**
	 * @param kickSpeed the kickSpeed to set
	 */
	protected final void setKickSpeed(final double kickSpeed)
	{
		this.kickSpeed = kickSpeed;
	}
	
	
	/**
	 * @param dribblerSpeed the dribblerSpeed to set
	 */
	protected final void setDribblerSpeed(final double dribblerSpeed)
	{
		this.dribblerSpeed = dribblerSpeed;
	}
	
	
	protected double getLastTargetOrientation()
	{
		return lastTargetOrientation;
	}
}
