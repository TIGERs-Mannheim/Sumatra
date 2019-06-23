/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 9, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Move to a given destination and orientation with PositionController
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PullBackSkill extends PositionSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2		initPos					= null;
	
	@Configurable(comment = "distance to pull the ball backwards")
	private static float	pullDistance			= 500;
	
	@Configurable(comment = "basic / initial moveSpeed")
	private static float	moveSpeedBasic			= 150;
	@Configurable(comment = "speed increase over time")
	private static float	moveSpeedRange			= 200;
	
	@Configurable(comment = "basic dribble speed")
	private static float	dribbleSpeedBasic		= 750;
	@Configurable(comment = "dribble speed at start, then slowly decreases to the end")
	private static float	dribbleSpeedRange		= 12000;
	
	@Configurable(comment = "distance when ball is considered as grabbed")
	private static float	grabBallDist			= (AIConfig.getGeometry().getBotRadius() + (AIConfig.getGeometry()
																	.getBallRadius() * 2) + 10);
	
	@Configurable(comment = "distance to ball to move to initially")
	private static float	distancToBall			= 40;
	
	
	@Configurable(comment = "timeout after x ms")
	private static float	timeout					= 2000;
	private static float	time						= 0;
	
	@Configurable(comment = "Vel [rad/s] to turn on circle (1st phase)")
	private static float	turnCircleVel			= 50f;
	
	// private long timeStart = System.nanoTime();
	
	private long			timeSinceBallContact	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 */
	public PullBackSkill()
	{
		super(ESkillName.PULL_BACK);
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		
		if ((System.currentTimeMillis() - time) > timeout)
		{
			complete();
		}
		IVector2 ballPos = getWorldFrame().getBall().getPos();
		float distanceToInit = GeoMath.distancePP(initPos, getPos());
		
		// float dt = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timeStart) / 1000.0f;
		// timeStart = System.nanoTime();
		
		if (distanceToInit > pullDistance)
		{
			complete();
			return;
		}
		
		
		/**
		 * 1 when pulling is done completely.
		 * 0 when pulling just started.
		 */
		float donePulling = (distanceToInit / pullDistance);
		
		IVector2 rvBallBot = getPos().subtractNew(ballPos).normalizeNew();
		IVector2 destination = ballPos.addNew(rvBallBot.multiplyNew(moveSpeedBasic + (moveSpeedRange * donePulling)));
		
		int rpm = (int) (dribbleSpeedBasic + (int) (dribbleSpeedRange * (1 - donePulling)));
		getDevices().dribble(cmds, rpm);
		
		float orientation = ballPos.subtractNew(getPos()).getAngle();
		if (!hasBallContact())
		{
			destination = ballPos.addNew(rvBallBot.multiplyNew(distancToBall));
			timeSinceBallContact = System.currentTimeMillis();
		} else if ((System.currentTimeMillis() - timeSinceBallContact) < 1500)
		{
			float test = (System.currentTimeMillis() - timeSinceBallContact) / 1500f;
			destination = ballPos.addNew(rvBallBot.multiplyNew(distancToBall + (120 * test)));
		}
		else if (((System.currentTimeMillis() - timeSinceBallContact) > 2000)
				&& ((System.currentTimeMillis() - timeSinceBallContact) < 5000))
		{
			destination = ballPos.addNew(rvBallBot.multiplyNew(distancToBall + (120)));
			orientation = orientation + 0.2f;
		}
		
		setOrientation(orientation);
		setDestination(destination);
		super.doCalcActions(cmds);
	}
	
	
	@Override
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		initPos = getWorldFrame().getBall().getPos();
		time = System.currentTimeMillis();
		super.calcEntryActions(cmds);
		return cmds;
	}
	
	
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		getDevices().allOff(cmds);
		return cmds;
	}
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
