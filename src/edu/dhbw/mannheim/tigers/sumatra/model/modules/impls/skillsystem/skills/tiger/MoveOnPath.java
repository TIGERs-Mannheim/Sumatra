/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.pathfinding.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.MoveConstraints;


/**
 * this skill moves the bot a long a series of pre-defined pathpoints
 * 
 * @author DanielW
 * 
 */
public class MoveOnPath extends AMoveSkillV2
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<IVector2>	pathpoints;
	private Path						path;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * this skill moves the bot a long a series of pre-defined pathpoints
	 * this is not working quite well because sisyphus acutalize-path function is removing pathpoints way to early
	 * @param pathpoints
	 */
	public MoveOnPath(IVector2... pathpoints)
	{
		super(ESkillName.MOVE_ON_PATH);
		this.pathpoints = new ArrayList<IVector2>();
		for (IVector2 p : pathpoints)
		{
			this.pathpoints.add(p);
		}
		

	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected float calcTargetOrientation(IVector2 move)
	{
		return 0;
	}
	

	@Override
	protected IVector2 getTarget()
	{
		return null; // no target, just move through all pathpoints
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		MoveOnPath move = (MoveOnPath) newSkill;
		return this.pathpoints.equals(move.pathpoints);
	}
	

	@Override
	//TODO restrictedAreas is not used (reported by: Christian)
	protected Path calcPath(WorldFrame wFrame, MoveConstraints moveConstraints)
	{
		if (path == null)
		{
			path = new Path(getBot().id, pathpoints);
		} else
		{
			// chop path if there
			// if (path.size() > 1 && path.path.get(0).equals(getBot().pos, 2 * AIConfig.getTolerances().getPositioning()))
			// {
			// path.path.remove(0);
			// }
			actualizeOldPath(path);
		}
		return path;
	}
	

	/**
	 * bot moves, so it has to be checked, if bot already reached a path point. </br>
	 * if so, removes it
	 * 
	 * for sake of simplicity this is copied from ERRTPlanner_WPC
	 * @return
	 */
	private Path actualizeOldPath(Path oldPath)
	{
		// should run till return statement is reached
		if (oldPath.path.size() > 1)
		{
			IVector2 ppA = oldPath.path.get(0);
			IVector2 ppB = oldPath.path.get(1);
			
			float distanceX = ppB.x() - ppA.x();
			float distanceY = ppB.y() - ppA.y();
			
			// should run till return statement is reached
			float u = ((getBot().pos.x - ppA.x()) * distanceX + (getBot().pos.y - ppA.y()) * distanceY)
					/ (distanceX * distanceX + distanceY * distanceY);
			
			if (u < 0)
			{
				// bot is before ppA, i.e. path only has to be actualized, if distance is below POSITIONING_TOLLERANCE
				if (getBot().pos.equals(ppA, AIConfig.getTolerances().getPositioning() * 2))
				{
					oldPath.path.remove(0);
					oldPath.changed = true;
				}
				return oldPath;
			} else
			{
				// bot has already gone a part of the path
				// delete first Vector2 and check again
				oldPath.path.remove(0);
				oldPath.changed = true;
			}
		} else
		{
			return oldPath;
		}
		
		return oldPath;
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
