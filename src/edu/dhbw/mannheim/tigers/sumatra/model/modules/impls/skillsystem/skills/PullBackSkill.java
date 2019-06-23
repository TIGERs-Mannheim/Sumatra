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

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.AroundBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.DoNothingDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.skills.PullBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.skills.TowardsBallDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Move to a given destination and orientation with PositionController
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class PullBackSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	@Configurable
	private static int			dribbleSpeed				= 20000;
	
	@Configurable
	private static float			maxSpeedPosTowardsBall	= 150;
	
	@Configurable
	private static float			pullingDriveSpeed			= 90;
	
	@Configurable
	private static float			targetTolerance			= 50;
	
	private AroundBallDriver	aroundBallDriver;
	
	private PullBallDriver		pullBallDriver;
	
	private TowardsBallDriver	towardsBallDriver;
	
	
	private EPullState			state							= EPullState.TURN;
	
	private IVector2				pullToTarget				= null;
	
	private IVector2				turnAroundTarget			= null;
	
	private enum EPullState
	{
		TURN,
		GET_CLOSE,
		PULL,
		DO_NOTHING
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param target
	 * @param orientation
	 */
	public PullBackSkill(final IVector2 target, final float orientation)
	{
		super(ESkillName.PULL_BACK);
		pullToTarget = target;
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
		// initial orientation here
		IVector2 ballToPullToTarget = pullToTarget.subtractNew(getWorldFrame().getBall().getPos());
		turnAroundTarget = getWorldFrame().getBall().getPos().addNew(ballToPullToTarget.multiplyNew(-1));
		aroundBallDriver = new AroundBallDriver(new DynamicPosition(turnAroundTarget));
		
		pullBallDriver = new PullBallDriver(pullingDriveSpeed, pullToTarget, targetTolerance);
		towardsBallDriver = new TowardsBallDriver(maxSpeedPosTowardsBall);
		setPathDriver(aroundBallDriver);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		switch (state)
		{
			case TURN:
				getDevices().dribble(cmds, false);
				if (aroundBallDriver.isDone())
				{
					state = EPullState.GET_CLOSE;
					towardsBallDriver = new TowardsBallDriver(maxSpeedPosTowardsBall);
					setPathDriver(towardsBallDriver);
				}
				break;
			case GET_CLOSE:
				getDevices().dribble(cmds, dribbleSpeed);
				aroundBallDriver.update(getTBot(), getWorldFrame());
				towardsBallDriver.update(getTBot(), getWorldFrame());
				if (getTBot().hasBallContact())
				{
					state = EPullState.PULL;
					pullBallDriver = new PullBallDriver(pullingDriveSpeed, pullToTarget, targetTolerance);
					setPathDriver(pullBallDriver);
				}
				else if (!aroundBallDriver.isDone())
				{
					state = EPullState.TURN;
					IVector2 ballToPullToTarget = pullToTarget.subtractNew(getWorldFrame().getBall().getPos());
					turnAroundTarget = getWorldFrame().getBall().getPos().addNew(ballToPullToTarget.multiplyNew(-1));
					aroundBallDriver = new AroundBallDriver(new DynamicPosition(turnAroundTarget));
					setPathDriver(aroundBallDriver);
				}
				break;
			case PULL:
				getDevices().dribble(cmds, dribbleSpeed);
				pullBallDriver.update(getTBot(), getWorldFrame());
				if (pullBallDriver.isDone())
				{
					state = EPullState.DO_NOTHING;
					setPathDriver(new DoNothingDriver());
				}
				else if (!getTBot().hasBallContact())
				{
					state = EPullState.TURN;
					IVector2 ballToPullToTarget = pullToTarget.subtractNew(getWorldFrame().getBall().getPos());
					turnAroundTarget = getWorldFrame().getBall().getPos().addNew(ballToPullToTarget.multiplyNew(-1));
					aroundBallDriver = new AroundBallDriver(new DynamicPosition(turnAroundTarget));
					setPathDriver(aroundBallDriver);
				}
				break;
			case DO_NOTHING:
				getDevices().dribble(cmds, false);
				if (GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos()) > (targetTolerance * 1.5))
				{
					state = EPullState.TURN;
					IVector2 ballToPullToTarget = pullToTarget.subtractNew(getWorldFrame().getBall().getPos());
					turnAroundTarget = getWorldFrame().getBall().getPos().addNew(ballToPullToTarget.multiplyNew(-1));
					aroundBallDriver = new AroundBallDriver(new DynamicPosition(turnAroundTarget));
					setPathDriver(aroundBallDriver);
				}
				break;
		}
	}
}
