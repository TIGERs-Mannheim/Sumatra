/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.06.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;

/**
 * Move to a position!
 * 
 * @author AndreR
 * 
 */
public class MoveV2 extends ASkill
{
	public class State
	{
		public Vector2f position;
		public Vector2f velocity;
		public float rotation;	// TODO: velocity?
	}
	
	private class Trajectory1DResult
	{
		public float trajAcc;
		public float trajTime;
	}
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final float maxAcc;
	private final float maxVel;
	private final float maxAngAcc;
	private final float maxAngVel;
	
	private IVector2 position;
	private float orientation;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public MoveV2(IVector2 position, float orientation)
	{
		super(ESkillName.MOVE_V2, ESkillGroup.MOVE);
		
		maxAcc = 0.5f;
		maxVel = 3.0f;
		maxAngAcc = (float)(2*Math.PI);
		maxAngVel = (float)(10*Math.PI);
		
		this.position = position;
		this.orientation = orientation;
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		TrackedTigerBot trackedBot = getBot();
		if(trackedBot == null)
		{
			return cmds;
		}
		
		State startState = new State();
		startState.position = new Vector2f(trackedBot.pos.multiplyNew(0.001f));
		startState.velocity = trackedBot.vel;
		startState.rotation = trackedBot.aVel;
		
		State endState = new State();
		endState.position = new Vector2f(position);
		endState.velocity = new Vector2f(0, 0);
//		endState.velocity = new Vector2f(endState.position.subtractNew(startState.position));
		endState.rotation = orientation;

		
		Vector3 cmd = trajectory2D(startState, endState);
		
		Vector2 XY = new Vector2(cmd.x, cmd.y);
		XY.turn(AIMath.PI_HALF - getBot().angle);
		
		cmds.add(new TigerMotorMoveV2(XY, cmd.z));

		return cmds;
	}
	
	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return false;
	}
	
	private Vector3 trajectory2D(State start, State end)
	{
		float u = AIMath.PI/2;
		float du = -u;
		
		float dt = 0.02f;	// TODO: measure and fix
		
		Trajectory1DResult trajX = null;
		Trajectory1DResult trajY = null;

		for(int i = 0; i < 10; i++)
		{
			du *= 0.5f;
			float alpha = u+du;
			
			float axMax = (float) (Math.sin(alpha)*maxAcc);
			float ayMax = (float) (Math.cos(alpha)*maxAcc);
			float vxMax = (float) (Math.sin(alpha)*maxVel);
			float vyMax = (float) (Math.cos(alpha)*maxVel);
			
			float deltaX = end.position.x - start.position.x;
			float deltaY = end.position.y - start.position.y;
			
			trajX = traj1DZeroAware(deltaX, start.velocity.x, end.velocity.x, dt, axMax, vxMax);
			trajY = traj1DZeroAware(deltaY, start.velocity.y, end.velocity.y, dt, ayMax, vyMax);
			
			if(trajX.trajTime - trajY.trajTime <= 0.0f)
			{
				u = alpha;
			}
		}
		
		float trajTime = Math.max(trajX.trajTime, trajY.trajTime);
		float deltaAngle = determineAngleDifference(end.rotation, start.rotation);
		
		Trajectory1DResult trajW = null;
		
		for(float factor = 0.1f; factor < 1.0f; factor += 0.1f)
		{
			trajW = traj1DZeroAware(deltaAngle, start.rotation, determineContinuousAngle(start.rotation, end.rotation), dt, maxAngAcc*factor, maxAngVel);
			if(trajW.trajTime < trajTime)
			{
				break;
			}
		}
		
		return new Vector3(start.velocity.x + trajX.trajAcc*dt, start.velocity.y + trajY.trajAcc*dt, start.rotation + trajW.trajAcc*dt);
	}
	
	protected float normalizeAngle(float angle)
	{
		angle = angle / (2*AIMath.PI);
		angle = angle - (int) angle;
		if (angle < 0)
		{
			angle = 1 + angle;
		}
		if (angle < 0.5)
		{
			angle = 2*angle * AIMath.PI;
		} else
		{
			angle = -2*(1-angle) * AIMath.PI;
		}
		return angle;
	}
	
	protected float determineContinuousAngle(float oldAngle, float newAngle)
	{
		// project new orientation next to old one
		// thus values greater than PI are possible but the handling gets easier
		
		float dAngle = newAngle - normalizeAngle(oldAngle);	//standard: just a small rotation
		if (dAngle > Math.PI)				//rotation clockwise over Pi-border
		{
			dAngle = dAngle - 2*AIMath.PI;
		} 
		else if(dAngle < -Math.PI)			//rotation counter-clockwise over Pi-border
		{
			dAngle = dAngle + 2*AIMath.PI;
		}
		return oldAngle + dAngle;
	}
	
	protected float determineAngleDifference(float angle1, float angle2)
	{
		float angleDif = (angle1-angle2) % (2*AIMath.PI);
		if (angleDif > Math.PI)
		{
			return angleDif-2*AIMath.PI;
		}
		if (angleDif < -Math.PI)
		{
			return angleDif + 2*AIMath.PI;
		}
		return angleDif;
	}
	
	private Trajectory1DResult traj1DZeroAware(float deltaS, float startVel, float endVel, float dt, float accMax, float velMax)
	{
		Trajectory1DResult result = null;
		
		// only acceleration or deceleration
		if (startVel*endVel >= 0.0)
		{
			result = trajectory1DNew(deltaS, startVel, endVel, dt, accMax, velMax);
		}
		// fist deceleration to 0 then acceleration
		else
		{
			// here we only decelerate to 0.0
			//TODO do we need to handle steps passing the target velocity?
			result = new Trajectory1DResult();
			result.trajAcc = Math.signum(endVel)*maxAcc;
			result.trajTime = dt;
		}
		return result;
	}
	
	
	@SuppressWarnings("unused")
	private Trajectory1DResult trajectory1D(float deltaS, float startVel, float endVel, float dt, float accMax, float velMax)
	{
		Trajectory1DResult result = new Trajectory1DResult();
		result.trajAcc = 0;
		result.trajTime = 0;
		
		if(deltaS == 0 && startVel == endVel)
		{
			return result;
		}
		
		float timeToFinalVel = Math.abs(startVel - endVel)/accMax;
		float distanceToFinalVel = (Math.abs(endVel + startVel)/2.0f)*timeToFinalVel;
		
		float timeAcc = 0;
		float timeDec = 0;
		
		if(Math.abs(startVel) > Math.abs(endVel))
		{
			float timeTemp = (float)((Math.sqrt((startVel*startVel + endVel*endVel)/2.0f + Math.abs(deltaS)*accMax) - Math.abs(startVel))/accMax);
			if(timeTemp < 0.0f)
			{
				timeTemp = 0.0f;
			}
			
			timeAcc = timeTemp;
			timeDec = timeTemp + timeToFinalVel;
		}
		else if(distanceToFinalVel > Math.abs(deltaS))
		{
			float timeTemp = (float)((Math.sqrt(startVel*startVel+2*accMax*Math.abs(startVel)) - Math.abs(startVel))/accMax);
			timeAcc = timeTemp;
			timeDec = 0.0f;
		}
		else
		{
			float timeTemp = (float)((Math.sqrt((startVel*startVel + endVel*endVel)/2.0f + Math.abs(deltaS)*accMax) - Math.abs(endVel))/accMax);
			if(timeTemp < 0.0f)
			{
				timeTemp = 0.0f;
			}
			
			timeAcc = timeTemp + timeToFinalVel;
			timeDec = timeTemp;
		}
		
		result.trajTime = timeAcc + timeDec;
		if(timeAcc * maxAcc + Math.abs(startVel) > velMax)
		{
			result.trajTime += Math.pow((velMax - (accMax * timeAcc + Math.abs(startVel))),2)/(accMax*velMax);
		}
		
		if(timeAcc < dt && timeDec == 0)
		{
			result.trajAcc = (endVel-startVel)*dt;
		}
		else if(timeAcc < dt && timeDec > 0)
		{
			result.trajAcc = (accMax*timeAcc) + (-accMax*(dt - timeAcc));
		}
		else
		{
			result.trajAcc = accMax;
		}
		
		return result;
	}
	
	private Trajectory1DResult trajectory1DNew(float deltaS, float startVel, float endVel, float dt, float accMax, float velMax)
	{
		Trajectory1DResult result = new Trajectory1DResult();
		result.trajAcc = 0;
		result.trajTime = 0;
		
		if(deltaS == 0)
		{
			return result;
		}
		
		float timeToFinalVel = Math.abs(startVel - endVel)/accMax;
		float tf = timeToFinalVel;
		float distanceToFinalVel = (Math.abs(endVel + startVel)/2.0f)*timeToFinalVel;
		
		float timeAcc = 0;
		float timeDec = 0;
		
		if(Math.abs(startVel) > Math.abs(endVel))
		{
			// need to brake
			if(distanceToFinalVel > Math.abs(deltaS))
			{
				// cannot reach max
				timeDec = Math.abs((float) ((Math.sqrt(2*accMax*deltaS-startVel*startVel)+startVel)/accMax));
				timeAcc = (float) 0.0;
			}
			else
			{
				timeDec = Math.abs((float)((Math.sqrt(2)*Math.sqrt(accMax*accMax*tf*tf-2*accMax*tf*deltaS+2*accMax*deltaS+2*deltaS*deltaS)+2*accMax*tf-2*deltaS)/(2*accMax)));
				timeAcc = timeDec - timeToFinalVel;
			}
		}
		else
		{
			// accel
			if(distanceToFinalVel > Math.abs(deltaS))
			{
				// cannot reach max
				timeAcc = Math.abs((float) ((Math.sqrt(2*accMax*deltaS-startVel*startVel)+startVel)/accMax));
				timeDec = (float) 0.0;
			}
			else
			{
				timeDec = Math.abs((float)((Math.sqrt(2)*Math.sqrt(accMax*accMax*tf*tf+2*accMax*tf*deltaS+2*accMax*deltaS+2*deltaS*deltaS)-2*accMax*tf-2*deltaS)/(2*accMax)));
				timeAcc = timeDec + timeToFinalVel;
			}
		}
		
		result.trajTime = timeAcc + timeDec;
		if(timeAcc * maxAcc + Math.abs(startVel) > velMax)
		{
			result.trajTime += Math.abs((Math.pow(accMax*timeAcc+Math.abs(startVel), 2) - velMax*velMax)/(accMax*velMax));
		}
		
		if(timeAcc < dt && timeDec == 0)
		{
			result.trajAcc = (endVel-startVel)*dt;
		}
		else if(timeAcc < dt && timeDec > 0)
		{
			result.trajAcc = Math.signum(deltaS)*(accMax*timeAcc) + (-accMax*(dt - timeAcc));
		}
		else
		{
			result.trajAcc = Math.signum(deltaS)*accMax;
		}
		
		return result;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
