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
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Arne
 *         this is orientated on PenaltyShootSkill but uses BangBangTrajectoryControl
 *         Move to given Destination and Orientation an shoot.
 *         Constants have to be tested and evaluated!!!
 */

public class PenaltyShootTrajSkill extends MoveToTrajSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private EState						state						= EState.Prepositioning;
	
	/**
	 * This value adjusts the angle to shoot but to low or to high number
	 */
	@Configurable(comment = "This value adjusts the angle to shoot but to low or to high number will cause the skill to fail, so be very careful here.")
	private static long				timeToShoot				= 50;
	
	@Configurable(comment = "dribbleSpeed")
	private static int				dribbleSpeed			= 0;
	
	@Configurable(comment = "correction Distance: distance to ball")
	private static float				correctionDist			= 0;
	
	@Configurable(comment = "interval in which new short trajectories are planned and executed")
	private static long				stepTimeForSlowMove	= 150;
	
	@Configurable(comment = "distance to ball below this threshold will to switch to Turn-State")
	private static float				turnThreshold			= 20;
	
	@Configurable(comment = "Distance to ball to switch to slow move.")
	private static float				slowMoveDist			= 200;
	
	@Configurable(comment = "trajectory length of one single trajectory in slow move.")
	private static float				slowMoveStepSize		= 3;
	
	private float						delta						= 0;
	
	private boolean					firstTurn				= true;
	
	private long						lastTime					= 0;
	
	private long						timeout					= 500;
	
	private ERotateTrajDirection	rotateDirection		= ERotateTrajDirection.LEFT;
	
	private boolean					ready						= false;
	
	
	/**
	 * @author MarkG
	 */
	public enum ERotateTrajDirection
	{
		/**  */
		LEFT,
		/**  */
		RIGHT;
	}
	
	/*
	 * ------------------
	 * | Prepositioning | (Move in predefined distance to ball)
	 * ------------------
	 * | bot close enough to ball & normalStart occured
	 * v
	 * ---------------
	 * | Positioning | (Move directly in front of ball)
	 * ---------------
	 * | ready for kick
	 * v
	 * --------
	 * | Turn | (Turn and Kick)
	 * --------
	 */
	
	private enum EState
	{
		Prepositioning,
		Positioning,
		Turn;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Do not use this constructor, if you extend from this class
	 * 
	 * @param rotate
	 */
	public PenaltyShootTrajSkill(final ERotateTrajDirection rotate)
	{
		super(ESkillName.PENALTY_TRAJ_SHOOT);
		rotateDirection = rotate;
		delta = slowMoveDist;
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		IVector2 dest;
		IVector2 orientVect;
		TrajPathFinderInput input2;
		TrajPath path;
		
		obstacleGen.setUseBall(getMoveCon().isBallObstacle());
		obstacleGen.setUseBots(getMoveCon().isBotsObstacle());
		obstacleGen.setUsePenAreaOur(getMoveCon().isPenaltyAreaAllowedOur());
		obstacleGen.setUseGoalPostsTheir(getMoveCon().isPenaltyAreaAllowedTheir());
		
		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotID());
		finderInput.setTrackedBot(getTBot());
		finderInput.setObstacles(obstacles);
		
		if (GeoMath.distancePP(getPos(), getWorldFrame().getBall().getPos()) < turnThreshold)
		{
			state = EState.Turn;
			lastTime = 0;
		}
		
		switch (state)
		{
			case Prepositioning:
				dest = getWorldFrame().getBall().getPos().subtractNew(new Vector2(slowMoveDist, 0));
				orientVect = getWorldFrame().getBall().getPos().subtractNew(getPos());
				
				finderInput.setDest(dest);
				finderInput.setTargetAngle(orientVect.getAngle());
				input2 = new TrajPathFinderInput(finderInput);
				getBot().getPathFinder().calcPath(input2);
				path = getBot().getPathFinder().getCurPath();
				driver.setPath(path);
				
				if (GeoMath.distancePP(getWorldFrame().getBall().getPos(), getWorldFrame().getBot(getBot().getBotID())
						.getPos()) <= (slowMoveDist + 10))
				{
					state = EState.Positioning;
				}
				break;
			case Positioning:
				if (!ready || ((SumatraClock.currentTimeMillis() - lastTime) < stepTimeForSlowMove))
				{
					break;
				}
				lastTime = SumatraClock.currentTimeMillis();
				delta -= slowMoveStepSize;
				dest = getWorldFrame()
						.getBall()
						.getPos()
						.addNew(
								new Vector2(getBot().getCenter2DribblerDist() - AIConfig.getGeometry().getBallRadius() - delta
										- correctionDist, 0));
				finderInput.setDest(dest);
				input2 = new TrajPathFinderInput(finderInput);
				getBot().getPathFinder().calcPath(input2);
				path = getBot().getPathFinder().getCurPath();
				driver.setPath(path);
				
				
				break;
			case Turn:
				if (!ready)
				{
					break;
				}
				
				if (lastTime == 0)
				{
					lastTime = SumatraClock.currentTimeMillis();
				}
				
				
				if (firstTurn)
				{
					switch (rotateDirection)
					{
						case LEFT:
							orientVect = getWorldFrame().getBall().getPos()
									.addNew(new Vector2(0, -100).subtractNew(new Vector2(getPos())));
							break;
						case RIGHT:
							orientVect = getWorldFrame().getBall().getPos()
									.addNew(new Vector2(0, 100).subtractNew(new Vector2(getPos())));
							
							break;
						default:
							orientVect = new Vector2();
					}
					float angle = orientVect.getAngle();
					dest = getPos();
					finderInput.setDest(dest);
					finderInput.setTargetAngle(angle);
					input2 = new TrajPathFinderInput(finderInput);
					getBot().getPathFinder().calcPath(input2);
					path = getBot().getPathFinder().getCurPath();
					driver.setPath(path);
					firstTurn = false;
					
				}
				
				if ((lastTime - SumatraClock.currentTimeMillis()) > timeout)
				{
					complete();
					return;
				}
				else if ((lastTime - SumatraClock.currentTimeMillis()) > timeToShoot)
				{
					getDevices().kickGeneralSpeed(cmds, EKickerMode.FORCE, EKickerDevice.STRAIGHT, 8, dribbleSpeed);
				}
				break;
		}
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
	}
	
	
	/**
	 * Skill will go on and Shoot.
	 */
	public void normalStartCalled()
	{
		ready = true;
	}
	
	
	/**
	 * change ShootDirection
	 * 
	 * @param rotate
	 */
	public void setShootDirection(final ERotateTrajDirection rotate)
	{
		rotateDirection = rotate;
	}
}
