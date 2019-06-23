/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 18.11.2010
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Linef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MathException;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.KeeperPlus1DefenderPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;


/**
 * Abstract Role for {@link KeeperPlus1DefenderPlay}.<br>
 * Extending Roles: {@link KeeperK1DRole}, {@link DefenderK1DRole}
 * 

 * - ball shoot trigger
 * 
 * @author Malte
 * 
 */
public abstract class AK1DRole extends ADefenseRole
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long	serialVersionUID	= -2418212059817866645L;
		
	protected EWAI					leftOrRight;
	/** Line from Ball to the goal middle. */
	protected final Line			dangerLine;
	
	/** Our goal-line. */
	protected final Linef		goalLine = AIConfig.getGeometry().getGoalLineOur();
	
	/** Gap orthogonal from the dangerline to the Role's side */
	protected float				gap;
	
	/** Gap from the goal middle */
	protected float				radius;
	
	protected Vector2				destination;
	
	private Goal goal = AIConfig.getGeometry().getGoalOur();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public AK1DRole(ERole type)
	{
		super(type);
		destination = new Vector2(AIConfig.INIT_VECTOR);
		dangerLine = new Line();
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		TrackedBall ball = currentFrame.worldFrame.ball;
		Vector2f interceptPoint;
		if(ball.vel.x() < -0.5)
		{
			Linef ballShootLine = new Linef(ball.pos, ball.vel);

			try
			{
				interceptPoint = new Vector2f(AIMath.intersectionPoint(ballShootLine,
						new Line(goal.getGoalCenter(), AVector2.Y_AXIS)));
			} catch (MathException err)
			{
				interceptPoint = new Vector2f(0, 99999);
			}
			
			//Ball wird auf unser Tor geschossen!
			
			if(interceptPoint.y() <  goal.getGoalPostLeft().y()+50
				&& interceptPoint.y() >  goal.getGoalPostRight().y()-50)
			{
			destination = AIMath.leadPointOnLine(this.getPos(currentFrame), ballShootLine);
			}
		}
		else
		{
		// Make the robots not to drive over the goalline
			float border = AIMath.PI * 6/16 ;
			if (Math.abs(AIMath.angleBetweenXAxisAndLine(dangerLine)) > border)
			{
				dangerLine.setDirectionVector(dangerLine.directionVector().turnToNew(
						AIMath.sign(dangerLine.directionVector().y()) * border));
			}
			
			// Point on the goalline where the danger line intersects;
			Vector2 startPoint;
			try
			{
				startPoint = AIMath.intersectionPoint(dangerLine, goalLine);
			} catch (MathException err)
			{
				startPoint = new Vector2(AIConfig.getGeometry().getGoalOur().getGoalCenter());
			}
			// Point on the bisector with distance = radius to goalline.
			destination = startPoint.addNew(dangerLine.directionVector().scaleToNew(radius));
			
			// orthogonal vector to dangerLine
			Vector2 temp;
			
			if (leftOrRight == EWAI.LEFT)
			{
				temp = dangerLine.directionVector().turnNew(AIMath.PI_HALF);
			} else if (leftOrRight == EWAI.RIGHT)
			{
				temp = dangerLine.directionVector().turnNew(-AIMath.PI_HALF);
			} else
			{
				log.warn("unknown EWAI type!!");
				temp = dangerLine.directionVector().turnNew(AIMath.PI_HALF);
			}
			
			temp.scaleTo(gap);
			destination.add(temp);	
		}
		
		if(AIConfig.getGeometry().getFakeOurPenArea().isPointInShape(destination))
		{
			destination = AIConfig.getGeometry().getFakeOurPenArea().nearestPointOutside(destination);
		}
		
		// update Conditions
		destCon.updateDestination(destination);
		// TODO use dangerline here!
		lookAtCon.updateTarget(currentFrame.worldFrame.ball.pos);
	}
	

	public void updateDangerLine(Line dangerLine)
	{
		this.dangerLine.set(dangerLine);
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setLeftOrRight(EWAI leftOrRight)
	{
		this.leftOrRight = leftOrRight;
	}
}
