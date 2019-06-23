/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.03.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory1D.HermiteSplineTrajectoryPart1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.HermiteSplineTrajectory2D.HermiteSplineTrajectoryPart2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplineTrajectoryGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.HermiteSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.ControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.TigerDevices;
import edu.dhbw.mannheim.tigers.sumatra.util.units.DistanceUnit;


/**
 * The base class for all move-skills.
 * Just follows trajectories.
 * 
 * @author AndreR
 */
public abstract class AMoveSkill extends ASkill
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final Logger			log						= Logger.getLogger(AMoveSkill.class.getName());
	
	private Sisyphus							sisyphus					= null;
	
	private HermiteSplineTrajectory2D	positionTraj			= null;
	private HermiteSplineTrajectory1D	rotateTraj				= null;
	
	private long								startTime;
	private int									lastSentPositionPart	= 0;
	private int									lastSentRotationPart	= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param skillName
	 */
	public AMoveSkill(ESkillName skillName)
	{
		super(skillName);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public final List<ACommand> calcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		return doCalcEntryActions(bot, cmds);
	}
	
	
	protected List<ACommand> doCalcEntryActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		HermiteSpline2D pos = new HermiteSpline2D(bot.getPos().multiplyNew(0.001f), bot.getPos().multiplyNew(0.001f),
				bot.getVel(), AVector2.ZERO_VECTOR);
		HermiteSpline rot = new HermiteSpline(bot.getAngle(), bot.getAngle(), bot.getaVel(), 0);
		
		SplinePair3D pair = new SplinePair3D();
		pair.setPositionTrajectory(new HermiteSplineTrajectory2D(pos));
		pair.setRotationTrajectory(new HermiteSplineTrajectory1D(rot));
		
		setNewTrajectory(pair, System.nanoTime());
		return cmds;
	}
	
	
	@Override
	public final List<ACommand> calcActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		if (isComplete(bot))
		{
			complete();
			
			return cmds;
		}
		
		periodicProcess(bot, cmds);
		
		if ((positionTraj != null) && (rotateTraj != null))
		{
			if (bot.getBotType() == EBotType.TIGER_V2)
			{
				TigerBotV2 botV2 = (TigerBotV2) bot.getBot();
				if (botV2.getControllerType() == ControllerType.FUSION)
				{
					tigerV2Actions(bot, cmds);
				} else if (botV2.getControllerType() == ControllerType.FUSION_VEL)
				{
					legacyActions(bot, cmds);
				}
			} else
			{
				legacyActions(bot, cmds);
			}
		}
		return cmds;
	}
	
	
	@Override
	public final List<ACommand> calcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		getSisyphus().clearPath(bot.getId());
		return doCalcExitActions(bot, cmds);
	}
	
	
	protected List<ACommand> doCalcExitActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	protected abstract void periodicProcess(TrackedTigerBot bot, List<ACommand> cmds);
	
	
	protected final void setNewTrajectory(SplinePair3D traj, long startTime)
	{
		positionTraj = traj.getPositionTrajectory();
		rotateTraj = traj.getRotationTrajectory();
		
		this.startTime = startTime;
		
		lastSentPositionPart = -1;
		lastSentRotationPart = -1;
	}
	
	
	protected HermiteSplineTrajectory2D getPositionTraj()
	{
		return positionTraj;
	}
	
	
	protected HermiteSplineTrajectory1D getRotationTraj()
	{
		return rotateTraj;
	}
	
	
	/**
	 * The legacy version only sends out simple velocity commands.
	 * 
	 * @param bot
	 * @param cmds
	 * @return
	 */
	private List<ACommand> legacyActions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		float t = getTrajectoryTime();
		
		Vector2 globalVel = positionTraj.getVelocity(t);
		Vector2 localVel = AiMath.convertGlobalBotVector2Local(globalVel, bot.getAngle());
		
		float w = rotateTraj.getVelocity(t);
		
		float velComp = -w * getPeriod() * 1e-9f;
		localVel.turn(velComp);
		
		cmds.add(new TigerMotorMoveV2(localVel, w));
		
		return cmds;
	}
	
	
	/**
	 * For the new TigerBotV2 the splines are directly transferred to the bot.
	 * 
	 * @param bot
	 * @param cmds
	 * @return
	 */
	private List<ACommand> tigerV2Actions(TrackedTigerBot bot, List<ACommand> cmds)
	{
		float t = getTrajectoryTime();
		
		if (lastSentPositionPart < 0)
		{
			// TODO DirkK use t
			TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
			splineCmd.setSpline(positionTraj.getSpline(0));
			splineCmd.setOption(TigerCtrlSpline2D.OPTION_APPEND | TigerCtrlSpline2D.OPTION_CLEAR);
			
			cmds.add(splineCmd);
			
			lastSentPositionPart = 0;
		}
		
		if (lastSentPositionPart < (positionTraj.getNumParts() - 1))
		{
			HermiteSplineTrajectoryPart2D lastPart = positionTraj.getPart(lastSentPositionPart);
			HermiteSplineTrajectoryPart2D partToSend = positionTraj.getPart(lastSentPositionPart + 1);
			
			float sendTime = (lastPart.startTime + lastPart.endTime) / 2;
			
			if (t > sendTime)
			{
				TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
				splineCmd.setSpline(partToSend.spline);
				splineCmd.setOption(TigerCtrlSpline2D.OPTION_APPEND);
				
				cmds.add(splineCmd);
				
				lastSentPositionPart++;
			}
		}
		
		if (lastSentRotationPart < 0)
		{
			TigerCtrlSpline1D splineCmd = new TigerCtrlSpline1D();
			splineCmd.setSpline(rotateTraj.getSpline(0));
			splineCmd.setOption(TigerCtrlSpline2D.OPTION_APPEND | TigerCtrlSpline2D.OPTION_CLEAR);
			
			cmds.add(splineCmd);
			
			lastSentRotationPart = 0;
		}
		
		if (lastSentRotationPart < (rotateTraj.getNumParts() - 1))
		{
			HermiteSplineTrajectoryPart1D lastPart = rotateTraj.getPart(lastSentRotationPart);
			HermiteSplineTrajectoryPart1D partToSend = rotateTraj.getPart(lastSentRotationPart + 1);
			
			float sendTime = (lastPart.start + lastPart.end) / 2;
			
			if (t > sendTime)
			{
				TigerCtrlSpline1D splineCmd = new TigerCtrlSpline1D();
				splineCmd.setSpline(partToSend.spline);
				splineCmd.setOption(TigerCtrlSpline2D.OPTION_APPEND);
				
				cmds.add(splineCmd);
				
				lastSentRotationPart++;
			}
		}
		
		return cmds;
	}
	
	
	/**
	 * @param cmds list to put the move command
	 */
	public final void stopMove(List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		if (getBot().getBotType() == EBotType.TIGER_V2)
		{
			TigerBotV2 botV2 = (TigerBotV2) getBot().getBot();
			if (botV2.getControllerType() == ControllerType.FUSION_VEL)
			{
				cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
			}
		} else
		{
			cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
		}
	}
	
	
	/**
	 * Stop immediately
	 * 
	 * @param cmds
	 */
	public final void stopMoveImmediately(List<ACommand> cmds)
	{
		if (getBot().getBotType() == EBotType.TIGER_V2)
		{
			TigerBotV2 botV2 = (TigerBotV2) getBot().getBot();
			if (botV2.getControllerType() == ControllerType.FUSION_VEL)
			{
				cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
			} else
			{
				TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
				splineCmd.setOption(TigerCtrlSpline2D.OPTION_CLEAR);
				cmds.add(splineCmd);
				
				TigerCtrlSpline1D splineCmd2 = new TigerCtrlSpline1D();
				splineCmd2.setOption(TigerCtrlSpline2D.OPTION_CLEAR);
				
				cmds.add(splineCmd2);
			}
		} else
		{
			cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
		}
	}
	
	
	/**
	 * This will draw the path and spline to the visualizer
	 * 
	 * @param botId
	 * @param nodes nodes on the path including start and end [mm]
	 * @param pair spline pair
	 */
	protected final void visualizePath(BotID botId, List<IVector2> nodes, SplinePair3D pair)
	{
		Path path = new Path(botId, nodes, nodes.get(nodes.size() - 1), 0);
		path.setHermiteSpline(pair);
		getSisyphus().newExternalPath(path);
	}
	
	
	/**
	 * Create an empty dummy path to remove current spline and path
	 */
	protected final void removePath()
	{
		List<IVector2> nodesSpline = new ArrayList<IVector2>(2);
		nodesSpline.add(DistanceUnit.MILLIMETERS.toMeters(getBot().getPos()));
		nodesSpline.add(DistanceUnit.MILLIMETERS.toMeters(getBot().getPos()));
		List<IVector2> nodes = new ArrayList<IVector2>(1);
		nodes.add(getBot().getPos());
		
		SplinePair3D pair = new SplineTrajectoryGenerator().create(nodesSpline, Vector2.ZERO_VECTOR, Vector2.ZERO_VECTOR,
				0, 0, 0, 0);
		visualizePath(getBot().getId(), nodes, pair);
	}
	
	
	protected final SplineTrajectoryGenerator createDefaultGenerator(TrackedTigerBot bot)
	{
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxLinearVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxLinearAcceleration());
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		return gen;
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param bot
	 * @param nodes on the path including destination and excluding current position
	 * @param finalOrientation
	 */
	protected final void createSpline(TrackedTigerBot bot, List<IVector2> nodes, float finalOrientation)
	{
		createSpline(bot, nodes, finalOrientation, createDefaultGenerator(bot));
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param bot
	 * @param nodes on the path including destination and excluding current position
	 * @param lookAtTarget final look
	 */
	protected final void createSpline(TrackedTigerBot bot, List<IVector2> nodes, IVector2 lookAtTarget)
	{
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxLinearVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxLinearAcceleration());
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(AIConfig.getSkills(bot.getBotType()).getMaxRotateVelocity(),
				AIConfig.getSkills(bot.getBotType()).getMaxRotateAcceleration());
		
		createSpline(bot, nodes, lookAtTarget, createDefaultGenerator(bot));
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param bot
	 * @param nodes on the path including destination and excluding current position
	 * @param lookAtTarget final look
	 * @param gen custom spline generator
	 * @throws IllegalArgumentException if lookAtTarget and last node are equal
	 */
	protected final void createSpline(TrackedTigerBot bot, List<IVector2> nodes, IVector2 lookAtTarget,
			SplineTrajectoryGenerator gen)
	{
		IVector2 dir = lookAtTarget.subtractNew(nodes.get(nodes.size() - 1));
		final float finalOrientation;
		if (dir.isZeroVector())
		{
			log.warn("lookAtTarget and destination are equal. Can not calculate final orientation. Keep current. "
					+ lookAtTarget + " " + nodes.get(nodes.size() - 1));
			finalOrientation = bot.getAngle();
		} else
		{
			finalOrientation = dir.getAngle();
		}
		createSpline(bot, nodes, finalOrientation, gen);
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param bot
	 * @param nodes on the path including destination and excluding current position
	 * @param finalOrientation
	 * @param gen custom spline generator
	 */
	protected final void createSpline(TrackedTigerBot bot, List<IVector2> nodes, float finalOrientation,
			SplineTrajectoryGenerator gen)
	{
		SplinePair3D pair = createSplineWithoutDrivingIt(bot, nodes, finalOrientation, gen);
		setNewTrajectory(pair, System.nanoTime());
		visualizePath(bot.getId(), nodes, pair);
	}
	
	
	protected final SplinePair3D createSplineWithoutDrivingIt(TrackedTigerBot bot, List<IVector2> nodes,
			float finalOrientation, SplineTrajectoryGenerator gen)
	{
		List<IVector2> nodesMM = new ArrayList<IVector2>(nodes.size() + 1);
		nodesMM.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		for (IVector2 vec : nodes)
		{
			nodesMM.add(DistanceUnit.MILLIMETERS.toMeters(vec));
		}
		
		return gen
				.create(nodesMM, bot.getVel(), Vector2.ZERO_VECTOR, bot.getAngle(), finalOrientation, bot.getaVel(), 0f);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	protected final float getTrajectoryTime()
	{
		return ((float) (System.nanoTime() - startTime) / (1000000000));
	}
	
	
	protected boolean isComplete(TrackedTigerBot bot)
	{
		float t = getTrajectoryTime();
		
		boolean position = (positionTraj == null ? false : ((t > positionTraj.getTotalTime())));
		boolean rotation = (rotateTraj == null ? false : (t > rotateTraj.getTotalTime()));
		
		return position && rotation;
	}
	
	
	/**
	 * @param sisyphus
	 */
	public final void setSisyphus(Sisyphus sisyphus)
	{
		this.sisyphus = sisyphus;
	}
	
	
	protected final Sisyphus getSisyphus()
	{
		return sisyphus;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public final TigerDevices getDevices()
	{
		return getBot().getBot().getDevices();
	}
}
