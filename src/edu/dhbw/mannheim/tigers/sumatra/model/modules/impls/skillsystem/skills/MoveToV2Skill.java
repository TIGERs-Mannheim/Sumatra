/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.IPathConsumer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill.EMoveToMode;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * With pathpoints only
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveToV2Skill extends PositionSkill implements IPathConsumer, IMoveToSkill
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float	VEL_MAX			= 3.0f;
	
	private MovementCon			moveCon;
	private List<IVector2>		pathPoints		= new ArrayList<IVector2>(0);
	private final EMoveToMode	moveToMode;
	
	@Configurable(comment = "If near than this dist [mm] to next point, go on to next point.")
	private static float			distTolerance	= 30;
	
	@Configurable(comment = "Min Raysize [mm] for p2pVisibility check (when standing)")
	private static float			raySizeMin		= 30;
	
	@Configurable(comment = "Max Raysize [mm] for p2pVisibility check (when vel=" + VEL_MAX + ")")
	private static float			raySizeMax		= 150;
	
	private boolean				kickerArmed		= false;
	
	private final IFunction1D	raySizeFn;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param moveToMode
	 */
	public MoveToV2Skill(final EMoveToMode moveToMode)
	{
		super(ESkillName.MOVE_TO_V2);
		moveCon = new MovementCon();
		moveCon.setPenaltyAreaAllowed(false);
		this.moveToMode = moveToMode;
		raySizeFn = new Function1dPoly(new float[] { raySizeMin, (raySizeMax - raySizeMin) / VEL_MAX });
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	public List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		if ((moveCon.getSpeed() > 1e-6f) && (moveCon.getSpeed() < Sisyphus.maxLinearVelocity))
		{
			slow();
		}
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
		getSisyphus().addObserver(getBot().getBotID(), this);
		getSisyphus().startPathPlanning(getBot().getBotID(), moveCon);
		return cmds;
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		moveCon.update(getWorldFrame(), getBot().getBotID());
		if (pathPoints.size() > 1)
		{
			IVector2 curDest = pathPoints.get(0);
			IVector2 nextDest = pathPoints.get(1);
			if ((GeoMath.distancePP(curDest, getPos()) < distTolerance) ||
					GeoMath.p2pVisibilityBotBall(getWorldFrame(), getPos(), nextDest, raySizeFn.eval(getVel().getLength2()),
							getBot()
									.getBotID()))
			{
				pathPoints.remove(0);
			}
		}
		if (!pathPoints.isEmpty())
		{
			setDestination(pathPoints.get(0));
		}
		setOrientation(moveCon.getAngleCon().getTargetAngle());
		
		if (moveToMode == EMoveToMode.DO_COMPLETE)
		{
			if (!isComplete() && checkIsComplete())
			{
				complete();
			}
		}
	}
	
	
	@Override
	public List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		getSisyphus().stopPathPlanning(getBot().getBotID());
		getSisyphus().removeObserver(getBot().getBotID());
		getDevices().disarm(cmds);
		getDevices().dribble(cmds, false);
		return super.calcExitActions(cmds);
	}
	
	
	@Override
	public void onNewPath(final Path path)
	{
		pathPoints = path.getPath();
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
