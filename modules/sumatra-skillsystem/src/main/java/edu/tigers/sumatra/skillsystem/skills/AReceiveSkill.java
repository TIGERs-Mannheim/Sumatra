/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.botmanager.commands.botskills.data.KickerDribblerCommands;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static edu.tigers.sumatra.skillsystem.skills.util.CatchBallCalc.CatchBallResult;


/**
 * Receive a ball and stop it or redirect it
 * 
 * @author MarkG <Mark.Geiger@dlr.de>
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public abstract class AReceiveSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log							= Logger.getLogger(AReceiveSkill.class.getName());
	
	
	private IVector2					desiredDestination		= null;
	private IVector2					curDesDest					= null;
	private double						lastTargetOrientation	= 0;
	
	private EKickerDevice			kickerDevice				= EKickerDevice.STRAIGHT;
	private double						kickSpeed					= 0;
	private double						dribblerSpeed				= 0;
	
	
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
			getMoveCon().setPenaltyAreaAllowedTheir(false);
			getMoveCon().setMinDistToBall(Geometry.getBallRadius() + Geometry.getBotRadius());
			
			catchBallCalc = new CatchBallCalc(getBotId());
			catchBallCalc.setCalcTargetOrientation(AReceiveSkill.this::calcTargetOrientation);
		}
		
		
		@Override
		public void doUpdate()
		{
			List<IDrawableShape> shapes = new ArrayList<>();
			
			catchBallCalc.setMoveCon(getMoveCon());
			catchBallCalc.update(getWorldFrame());
			catchBallCalc.setDesiredDestination(desiredDestination);
			
			CatchBallResult catchBallResult = catchBallCalc.calculate();
			curDesDest = catchBallResult.getKickerDest();
			
			double dist2Dest = curDesDest.distanceTo(getKickerPos());
			getMoveCon().setTheirBotsObstacle(dist2Dest > 1000);
			
			getMoveCon().updateDestination(catchBallResult.getBotDest());
			getMoveCon().updateTargetAngle(catchBallResult.getTargetOrientation());
			lastTargetOrientation = catchBallResult.getTargetOrientation();
			
			drawShapes(shapes);
			
			super.doUpdate();
		}
		
		
		private IVector2 getKickerPos()
		{
			return getTBot().getPos().addNew(
					Vector2.fromAngle(getAngle()).scaleTo(getTBot().getCenter2DribblerDist() + Geometry.getBallRadius()));
		}
		
		
		private void drawShapes(final List<IDrawableShape> shapes)
		{
			// draw desired dest
			if (desiredDestination != null)
			{
				DrawableCircle dc2 = new DrawableCircle(Circle.createCircle(desiredDestination, 250),
						new Color(0, 255, 255, 100));
				dc2.setFill(true);
				shapes.add(dc2);
			}
			getShapes().get(ESkillShapesLayer.RECEIVER_SKILL).addAll(shapes);
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
	 * @return
	 */
	public IVector2 getDesiredDestination()
	{
		return this.desiredDestination;
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
	
	
	protected IVector2 getCurDesDest()
	{
		return curDesDest;
	}
}
