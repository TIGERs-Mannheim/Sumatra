/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.TrajPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.IObstacle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.obstacles.ObstacleGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.AsyncExecution;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToTrajSkill extends AMoveSkill
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(MoveToTrajSkill.class.getName());
	
	protected AsyncExecution		asyncExecution;
	protected TrajPathFinderInput	finderInput		= new TrajPathFinderInput();
	protected ObstacleGenerator	obstacleGen		= new ObstacleGenerator();
	protected TrajPathDriver		driver			= new TrajPathDriver();
	
	@Configurable
	private static int				maxSubPoints	= 4;
	
	
	/**
	 * 
	 */
	public MoveToTrajSkill()
	{
		super(ESkillName.MOVE_TO_TRAJ);
		finderInput.setMaxSubPoints(maxSubPoints);
		setPathDriver(driver);
	}
	
	
	/**
	 * @param skillname
	 */
	public MoveToTrajSkill(final ESkillName skillname)
	{
		super(skillname);
		finderInput.setMaxSubPoints(maxSubPoints);
		setPathDriver(driver);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		super.update(cmds);
		
		obstacleGen.setUseBall(getMoveCon().isBallObstacle());
		obstacleGen.setUseBots(getMoveCon().isBotsObstacle());
		obstacleGen.setUsePenAreaOur(!getMoveCon().isPenaltyAreaAllowedOur());
		obstacleGen.setUsePenAreaTheir(!getMoveCon().isPenaltyAreaAllowedTheir());
		obstacleGen.setUseGoalPostsOur(getMoveCon().isGoalPostObstacle());
		
		List<IObstacle> obstacles = obstacleGen.generateObstacles(getWorldFrame(), getBot().getBotID());
		finderInput.setObstacles(obstacles);
		finderInput.setTrackedBot(getTBot());
		finderInput.setDest(getMoveCon().getDestCon().getDestination());
		finderInput.setTargetAngle(getMoveCon().getAngleCon().getTargetAngle());
		final TrajPathFinderInput localInput = new TrajPathFinderInput(finderInput);
		localInput.setForcePathAfter(getMoveCon().getForcePathAfterTime());
		
		asyncExecution.executeAsynchronously(() -> getBot().getPathFinder().calcPath(localInput));
		
		
		TrajPath path = getBot().getPathFinder().getCurPath();
		// if ((path != null) && ((driver.getPath() == null) || (!SumatraMath.isEqual(path.getRndId(),
		// driver.getPath().getRndId()))))
		// {
		// log.info("New Path for " + getBot().getBotID() + " :\n" + path);
		// }
		driver.setPath(path);
		// finderInput.getDebugShapes().add(obstacleGen);
		// driver.setShapes(finderInput.getDebugShapes());
	}
	
	
	@Override
	protected void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
		asyncExecution = new AsyncExecution(getBot().getColor());
	}
}
