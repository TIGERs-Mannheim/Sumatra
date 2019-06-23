/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.IPathConsumer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This skills asks sysiphus to create a path, makes a spline out of it and then follows the spline.
 * 
 * @author AndreR
 */
public class MoveToSkill extends AMoveSkill implements IPathConsumer, IMoveToSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log					= Logger.getLogger(MoveToSkill.class.getName());
	private MovementCon				moveCon;
	
	private boolean					kickerArmed			= false;
	
	@Configurable(comment = "Dist [mm] - If bot is nearer than this to destination, pathPlanning will not be used and changes directly applied")
	private static float				circumventPPTol	= 100;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Move to a target with an orientation as specified in the moveCon.
	 */
	public MoveToSkill()
	{
		this(ESkillName.MOVE_TO);
	}
	
	
	protected MoveToSkill(final ESkillName skillName)
	{
		super(skillName);
		moveCon = new MovementCon();
		moveCon.setPenaltyAreaAllowed(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected boolean isMoveComplete()
	{
		if (checkIsComplete())
		{
			log.trace("completed due to move and rotate conditions fulfilled");
			return true;
		}
		return false;
	}
	
	
	private boolean checkIsComplete()
	{
		// Check conditions
		boolean moveComplete = true;
		boolean rotateComplete = true;
		
		if (moveCon.getDestCon().isActive())
		{
			moveComplete = moveCon.checkCondition(getWorldFrame(), getBot().getBotID()) == EConditionState.FULFILLED;
		}
		
		if (moveCon.getAngleCon().isActive())
		{
			rotateComplete = moveCon.getAngleCon().checkCondition(getWorldFrame(), getBot().getBotID()) == EConditionState.FULFILLED;
		}
		if (moveComplete && rotateComplete)
		{
			return true;
		}
		
		return false;
	}
	
	
	@Override
	public List<ACommand> doCalcEntryActions(final List<ACommand> cmds)
	{
		moveCon.setSpeed(Math.min(getMaxLinearVelocity(), moveCon.getSpeed()));
		moveCon.update(getWorldFrame(), getBot().getBotID());
		getSisyphus().addObserver(getBot().getBotID(), this);
		getSisyphus().startPathPlanning(getBot().getBotID(), getMoveCon());
		return cmds;
	}
	
	
	@Override
	protected void periodicProcess(final List<ACommand> cmds)
	{
		if (moveCon.isKickerArmed() && (getBot().getBotFeatures().get(EFeature.BARRIER) != EFeatureState.KAPUT)
				&& (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.KAPUT))
		{
			if (!kickerArmed || (getBot().getKickerLevel() < (getBot().getKickerMaxCap() - 50)))
			{
				kickerArmed = true;
				getDevices().kickMax(cmds);
			}
		} else if (kickerArmed)
		{
			kickerArmed = false;
			getDevices().disarm(cmds);
		}
		moveCon.update(getWorldFrame(), getBot().getBotID());
		
		IVector2 dest = moveCon.getDestCon().getDestination();
		if (GeoMath.distancePP(dest, getPos()) < circumventPPTol)
		{
			float orient = moveCon.getAngleCon().getTargetAngle();
			setDestination(dest);
			setTargetOrientation(orient);
			setOverridePP(true);
		} else
		{
			setOverridePP(false);
		}
	}
	
	
	@Override
	protected List<ACommand> doCalcExitActions(final List<ACommand> cmds)
	{
		getSisyphus().stopPathPlanning(getBot().getBotID());
		getSisyphus().removeObserver(getBot().getBotID());
		getDevices().disarm(cmds);
		getDevices().dribble(cmds, false);
		if (moveCon.getVelAtDestination().equals(AVector2.ZERO_VECTOR, 0.1f))
		{
			stopMove(cmds);
		}
		return super.doCalcExitActions(cmds);
	}
	
	
	@Override
	public void onNewPath(final Path path)
	{
		setNewTrajectory(path.getHermiteSpline(), path.getPath());
	}
	
	
	@Override
	public void onPotentialNewPath(final Path path)
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the moveCon
	 */
	@Override
	public MovementCon getMoveCon()
	{
		return moveCon;
	}
}
