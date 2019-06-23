/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.BSplinePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.ACondition.EConditionState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.IPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.DoNothingDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.EPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.HermiteSplinePathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.IPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.LongPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.MixedPathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PathPointDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.SplinePathDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.finder.traj.TrajPath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.EBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPositionPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillTrajCtrl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.LimitedVelocityCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlResetCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchPosVel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.LinearPolicy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IConfigObserver;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.ConfigRegistration;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.config.EConfigurableCat;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.GaussianPolicy;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.IPolicyController;


/**
 * The base class for all move-skills.
 * Just follows trajectories.
 * 
 * @author AndreR
 */
public abstract class AMoveSkill extends ASkill implements IMoveToSkill
{
	@SuppressWarnings("unused")
	private static final Logger		log							= Logger.getLogger(AMoveSkill.class.getName());
	
	private final MovementCon			moveCon						= new MovementCon();
	private IPathDriver					pathDriver					= new DoNothingDriver();
	private boolean						doComplete					= false;
	private boolean						isInitialized				= false;
	/** number of new paths between start/stop (usually one Skill lifetime) */
	private int								newPathCounter				= 0;
	
	private boolean						kickerArmed					= false;
	private int								lastDribbleDuration		= 0;
	
	// @Configurable(comment = "Vel [m/s] - Threshold below which a velocity is considered as slow")
	// private static float driveSlowVelocityThreshold = 2.0f;
	
	@Configurable(comment = "Which type of command should be send to the bots? POS or VEL or POS_LEARNED")
	private static ECommandType		defaultCommandType		= ECommandType.POS;
	private ECommandType					commandType					= defaultCommandType;
	
	private static IPolicyController	policyController			= new LinearPolicy();
	
	protected float						forcedTimeToDestination	= 0.0f;
	private long							initTime						= SumatraClock.nanoTime();
	
	@Configurable
	private static EBotSkill			positionSkill				= EBotSkill.POSITION_PID;
	// private float lastCurPathTime = 0;
	
	@Configurable
	private static EMoveToType			moveToType					= EMoveToType.DEFAULT;
	
	@Configurable
	private static String				policyCtrlFile				= "/home/geforce/workspace/policysearchtoolbox/+Experiments/data/SSLRobotReal/SSLRobotTask_RBFStates_RBFProdKernelActions_GPPolicy_RKHSREPS_SSLRobot_Simple/SSLRobotReal_201501301543_01/eval001/trial001/controller_020.txt";
	
	
	private enum EMoveToType
	{
		DEFAULT,
		TRAJECTORY
	}
	
	static
	{
		ConfigRegistration.registerConfigurableCallback(EConfigurableCat.SKILLS, new IConfigObserver()
		{
			@Override
			public void onReload(final HierarchicalConfiguration freshConfig)
			{
				if (((defaultCommandType == ECommandType.POS_LEARNED) && !policyCtrlFile.isEmpty())
				)
				{
					if (policyController.getClass().equals(GaussianPolicy.class))
					{
						GaussianPolicy gp = (GaussianPolicy) policyController;
						if (!gp.getCtrlFile().equals(policyCtrlFile))
						{
							policyController = GaussianPolicy.createGp(policyCtrlFile, "");
						}
					} else
					{
						policyController = GaussianPolicy.createGp(policyCtrlFile, "");
					}
				} else
				{
					policyController = new LinearPolicy();
				}
			}
			
			
			@Override
			public void onLoad(final HierarchicalConfiguration newConfig)
			{
			}
		});
	}
	
	/**
	 * Which type of command should be send to the bots
	 */
	public enum ECommandType
	{
		/**  */
		TRAJ_PATH,
		/**  */
		POS,
		/**  */
		VEL,
		/**  */
		POS_VEL,
		/** Position controller as learned during 'Robot Learning: IP'(NicolaiO) */
		POS_LEARNED;
	}
	
	
	@Configurable(comment = "The default pathDriver: MIXED_SPLINE_POS, PATH_POINT, HERMITE_SPLINE")
	private static EPathDriver	defaultPathDriver	= EPathDriver.MIXED_SPLINE_POS;
	
	
	protected AMoveSkill(final ESkillName skillName)
	{
		super(skillName);
		moveCon.setPenaltyAreaAllowedOur(false);
	}
	
	
	protected AMoveSkill(final ESkillName skillName, final float forcedTimeToDestination)
	{
		super(skillName);
		moveCon.setPenaltyAreaAllowedOur(false);
		this.forcedTimeToDestination = forcedTimeToDestination;
	}
	
	
	/**
	 * Create the configured default MoveToSkill
	 * 
	 * @return
	 */
	public static IMoveToSkill createMoveToSkill()
	{
		switch (moveToType)
		{
			case DEFAULT:
				return new MoveToSkill();
			case TRAJECTORY:
				return new MoveToTrajSkill();
			default:
				throw new IllegalStateException();
		}
	}
	
	
	/**
	 * Create the configured default MoveToSkill
	 * 
	 * @param forcedTimeToDestination
	 * @return
	 */
	public static IMoveToSkill createMoveToSkill(final float forcedTimeToDestination)
	{
		return createMoveToSkill();
	}
	
	
	private void processMoveCon(final List<ACommand> cmds)
	{
		if (getMoveCon().isArmKicker() && (getBot().getBotFeatures().get(EFeature.BARRIER) != EFeatureState.KAPUT)
				&& (getBot().getBotFeatures().get(EFeature.STRAIGHT_KICKER) != EFeatureState.KAPUT))
		{
			if (!kickerArmed || (getBot().getKickerLevel() < (getBot().getKickerMaxCap() - 50)))
			{
				kickerArmed = true;
				getDevices().kickGeneralSpeed(cmds, EKickerMode.ARM, EKickerDevice.STRAIGHT, 8, 0);
			}
		} else if (kickerArmed)
		{
			kickerArmed = false;
			getDevices().disarm(cmds);
		}
		
		if (lastDribbleDuration != moveCon.getDribbleDuration())
		{
			getDevices().dribble(cmds, moveCon.getDribbleDuration());
			lastDribbleDuration = moveCon.getDribbleDuration();
		}
		cmds.add(new LimitedVelocityCommand(moveCon.getSpeed()));
		
		pathDriver.setMovingSpeed(moveCon.getMovingSpeed());
	}
	
	
	private IVector3 getNextDestination()
	{
		IVector3 poss = pathDriver.getNextDestination(getTBot(), getWorldFrame());
		IVector2 dest = new Vector2(poss.getXYVector());
		dest = AIConfig.getGeometry().getFieldWReferee().nearestPointInside(dest);
		float orient = poss.z();
		if (getWorldFrame().isInverted())
		{
			dest = dest.multiplyNew(-1);
			orient = AngleMath.normalizeAngle(orient + AngleMath.PI);
		}
		return new Vector3(dest, orient);
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		if (getWorldFrame().getBots().containsKey(getBot().getBotID()))
		{
			moveCon.update(getWorldFrame(), getBot().getBotID());
			if (doComplete && isDestinationReached())
			{
				complete();
				return;
			}
			
			processMoveCon(cmds);
		}
		update(cmds);
		isInitialized = true;
		
		if (pathDriver.getSupportedCommands().isEmpty())
		{
			return;
		}
		
		ECommandType cmdType = commandType;
		if (!pathDriver.getSupportedCommands().contains(commandType))
		{
			cmdType = pathDriver.getSupportedCommands().get(0);
		}
		pathDriver.update(getTBot(), getWorldFrame());
		
		switch (cmdType)
		{
			case TRAJ_PATH:
				TrajPath path = pathDriver.getPath();
				if (path != null)
				{
					cmds.add(getBot().getPathFinder().getPositioningCommand(path, getWorldFrame().isInverted()));
				} else
				{
					cmds.add(new TigerCtrlResetCommand());
				}
				break;
			case POS:
				IVector3 poss = getNextDestination();
				switch (positionSkill)
				{
					case POSITION_PID:
						cmds.add(new TigerSystemBotSkill(new BotSkillPositionPid(poss.getXYVector(), poss.z())));
						break;
					case TRAJ_CTRL:
						cmds.add(new TigerSystemBotSkill(new BotSkillTrajCtrl(poss.getXYVector(), poss.z())));
						break;
					default:
						throw new IllegalArgumentException("Invalid position skill: " + positionSkill);
				}
				break;
			case VEL:
			{
				final TigerMotorMoveV2 move = new TigerMotorMoveV2();
				IVector3 vel = pathDriver.getNextLocalVelocity(getTBot(), getWorldFrame(), getDt());
				if ((getMoveCon().getSpeed() > 0) && (vel.getXYVector().getLength2() > getMoveCon().getSpeed()))
				{
					vel = new Vector3(vel.getXYVector().scaleTo(getMoveCon().getSpeed()), vel.z());
				}
				move.setX(vel.x());
				move.setY(vel.y());
				move.setW(vel.z());
				cmds.add(move);
			}
				break;
			case POS_VEL:
				IVector3 destination = getNextDestination();
				IVector3 vel = pathDriver.getNextVelocity(getTBot(), getWorldFrame());
				final TigerSystemMatchPosVel posVel = new TigerSystemMatchPosVel(destination, vel);
				cmds.add(posVel);
				break;
			case POS_LEARNED:
			{
				// dest vel only used for non controlled states
				final IVector3 destVel = pathDriver.getNextVelocity(getTBot(), getWorldFrame());
				Vector2 destVelXY = destVel.getXYVector();
				float botW = getTBot().getAngle();
				
				final IVector3 destState = pathDriver.getNextDestination(getTBot(), getWorldFrame());
				
				Vector2 destXY = destState.getXYVector().multiplyNew(1e-3f);
				float destW = destState.z();
				
				float botWVel = getTBot().getaVel();
				
				Vector2 botPos = getTBot().getPos().multiplyNew(1e-3f);
				IVector2 botVel = getTBot().getVel();
				IVector2 botVelLocal = AiMath.convertGlobalBotVector2Local(botVel, botW);
				
				// if worldframe is inverted, turn positions
				if (getWorldFrame().isInverted())
				{
					destXY.multiply(-1);
					destW = AngleMath.normalizeAngle(destW + AngleMath.PI);
					botPos.multiply(-1);
					botW = AngleMath.normalizeAngle(botW + AngleMath.PI);
					destVelXY.multiply(-1);
				}
				
				float diffW = AngleMath.normalizeAngle(destW - botW);
				IVector2 dV = botVelLocal;
				
				// System.out.println(diffW + " " + botW + " " + destW);
				
				IVector2 diffPosGlobal = destXY.subtractNew(botPos);
				// angle between global pos2Dest and global bot orientation
				float alpha = GeoMath.angleBetweenVectorAndVectorWithNegative(new Vector2(botW), diffPosGlobal);
				float diffXLocal = AngleMath.cos(alpha) * diffPosGlobal.getLength2();
				float diffYLocal = AngleMath.sin(alpha) * diffPosGlobal.getLength2();
				IVector2 dP = new Vector2(diffXLocal, diffYLocal);
				
				
				int stateDim = policyController.getStateDimension();
				Matrix state = new Matrix(1, stateDim);
				switch (stateDim)
				{
					case 2:
						state.set(0, 0, dP.x());
						state.set(0, 1, dV.y());
						break;
					case 4:
						state.set(0, 0, dP.x());
						state.set(0, 1, dP.y());
						state.set(0, 2, dV.y());
						state.set(0, 3, dV.x());
						break;
					case 6:
						state.set(0, 0, dP.x());
						state.set(0, 1, dP.y());
						state.set(0, 2, diffW);
						state.set(0, 3, dV.y());
						state.set(0, 4, dV.x());
						state.set(0, 5, botWVel);
						break;
					default:
						throw new IllegalArgumentException("Invalid state dim: " + stateDim);
				}
				
				
				Matrix u = policyController.getControl(state);
				
				
				final TigerMotorMoveV2 momove = new TigerMotorMoveV2();
				// y on bot is x here and vice versa
				// set default values
				Vector2 destVelLocal = AiMath.convertGlobalBotVector2Local(destVelXY, botW);
				momove.setX(destVelLocal.x());
				momove.setY(destVelLocal.y());
				momove.setW(destVel.z());
				
				switch (u.getColumnDimension())
				{
					case 3:
						momove.setW((float) u.get(0, 2));
					case 2:
						momove.setX((float) u.get(0, 1));
					case 1:
						momove.setY((float) u.get(0, 0));
						break;
					default:
				}
				cmds.add(momove);
			}
				break;
			default:
				throw new IllegalStateException();
		}
	}
	
	
	protected void update(final List<ACommand> cmds)
	{
		
	}
	
	
	/**
	 * Check if destination reached through MoveCon
	 * 
	 * @return
	 */
	protected final boolean isDestinationReached()
	{
		boolean moveConOk = moveCon.checkCondition(getWorldFrame(), getBot().getBotID()) != EConditionState.PENDING;
		boolean pathDriverDone = pathDriver.isDone();
		return moveConOk && pathDriverDone;
	}
	
	
	/**
	 * @param commandType the commandType to set
	 */
	public final void setCommandType(final ECommandType commandType)
	{
		this.commandType = commandType;
	}
	
	
	/**
	 * @return the pathDriver
	 */
	protected final IPathDriver getPathDriver()
	{
		return pathDriver;
	}
	
	
	/**
	 * @param pathDriver the pathDriver to set
	 */
	public final void setPathDriver(final IPathDriver pathDriver)
	{
		this.pathDriver = pathDriver;
	}
	
	
	/**
	 * @return the moveCon
	 */
	@Override
	public final MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @param doComplete the doComplete to set
	 */
	@Override
	public final void setDoComplete(final boolean doComplete)
	{
		this.doComplete = doComplete;
	}
	
	
	@Override
	public DrawablePath getDrawablePath()
	{
		DrawablePath dp = new DrawablePath();
		return decoratePath(dp);
	}
	
	
	@Override
	public DrawablePath getLatestDrawablePath()
	{
		return decoratePath(new DrawablePath());
	}
	
	
	private DrawablePath decoratePath(final DrawablePath dp)
	{
		if (isInitialized)
		{
			pathDriver.decoratePath(getTBot(), dp.getPathShapes());
			pathDriver.decoratePathDebug(getTBot(), dp.getPathDebugShapes());
		}
		return dp;
	}
	
	
	@Override
	public final int getNewPathCounter()
	{
		return newPathCounter;
	}
	
	
	/**
	 * @param newPathCounter the newPathCounter to set
	 */
	protected final void setNewPathCounter(final int newPathCounter)
	{
		this.newPathCounter = newPathCounter;
	}
	
	
	protected final IPathDriver getDefaultPathDriver(final IPath path)
	{
		switch (defaultPathDriver)
		{
			case HERMITE_SPLINE:
				return (new HermiteSplinePathDriver(getTBot(), path, getMoveCon().getSpeed(), getSplineForcedEndTime()));
			case MIXED_SPLINE_POS:
				return (new MixedPathDriver(new HermiteSplinePathDriver(getTBot(), path, getMoveCon().getSpeed(),
						getSplineForcedEndTime()),
						new PathPointDriver(path, getMoveCon()), getMoveCon().getDestCon().getDestination()));
			case MIXED_LONG_POS:
				return (new MixedPathDriver(new LongPathDriver(getTBot(), path, getMoveCon().getSpeed(),
						getSplineForcedEndTime()),
						new PathPointDriver(path, getMoveCon()), getMoveCon().getDestCon().getDestination()));
			case PATH_POINT:
				return (new PathPointDriver(path, getMoveCon()));
			case LONG:
				return (new LongPathDriver(getTBot(), path, getMoveCon().getSpeed(),
						getSplineForcedEndTime()));
			case BSPLINE:
				List<IVector2> cps = new ArrayList<IVector2>();
				if (getVel().getLength2() > 0.4)
				{
					IVector2 pos1 = getPos().addNew(getVel().multiplyNew(600f));
					cps.add(pos1);
				} else
				{
					cps.add(getPos());
				}
				for (IVector2 key : path.getPathPoints())
				{
					if (!isVectorInList(cps, key))
					{
						cps.add(key);
					}
				}
				BSplinePath bpath = new BSplinePath(cps, new Vector2(0, 0), new Vector2(0, 0), true);
				return (new MixedPathDriver(new SplinePathDriver(bpath), new PathPointDriver(path, getMoveCon()),
						getMoveCon().getDestCon().getDestination()));
			default:
				throw new IllegalStateException();
		}
	}
	
	
	/**
	 * @return the gpController
	 */
	public final IPolicyController getPolicyController()
	{
		return policyController;
	}
	
	
	/**
	 * @param gpController the gpController to set
	 */
	public final void setGpController(final GaussianPolicy gpController)
	{
		policyController = gpController;
	}
	
	
	private float getSplineForcedEndTime()
	{
		return forcedTimeToDestination - ((SumatraClock.nanoTime() - initTime) * 1e-12f);
	}
	
	
	private boolean isVectorInList(final List<IVector2> list, final IVector2 vec)
	{
		for (IVector2 key : list)
		{
			if (key.equals(vec, 50f))
			{
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * @return the isInitialized
	 */
	public final boolean isInitialized()
	{
		return isInitialized;
	}
	
	
	/**
	 * @return the commandType
	 */
	public final ECommandType getCommandType()
	{
		return commandType;
	}
}
