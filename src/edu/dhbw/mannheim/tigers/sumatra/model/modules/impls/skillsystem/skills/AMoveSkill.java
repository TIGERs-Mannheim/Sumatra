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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
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
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECtrlMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlResetCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline1D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSpline2D;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
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
	private static final Logger	log								= Logger.getLogger(AMoveSkill.class.getName());
	
	@Configurable(comment = "Dist [mm] - if using Spline/Pos movement controller, this is the distance to destination, when pos ctrl will be activated")
	private static float				changeToPosCtrlThreshold	= 100;
	
	@Configurable
	private static float				positionMoveLookAhead		= 0.5f;
	
	private SplinePair3D				splinePair						= new SplinePair3D();
	private SplinePair3D				splinePairMirrored			= new SplinePair3D();
	
	private int							lastSentPositionPart			= 0;
	private int							lastSentRotationPart			= 0;
	
	private float						maxLinearVelocity				= 10;
	
	
	private boolean					overridePP						= false;
	private IVector2					destination						= null;
	private float						targetOrientation				= 0;
	
	@Configurable(comment = "The default MoveToSkill. SPLINE or POS")
	private static EMoveToSkill	moveToSkill						= EMoveToSkill.SPLINE;
	
	private enum EMoveToSkill
	{
		SPLINE,
		POS
	}
	
	/**
	 */
	public enum EMoveToMode
	{
		/**  */
		DO_COMPLETE,
		/**  */
		STAY;
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param skillName
	 */
	public AMoveSkill(final ESkillName skillName)
	{
		super(skillName);
	}
	
	
	/**
	 * Create the configured default MoveToSkill
	 * 
	 * @param moveToMode
	 * @return
	 */
	public static IMoveToSkill createMoveToSkill(final EMoveToMode moveToMode)
	{
		switch (moveToSkill)
		{
			case POS:
				return new MoveToV2Skill(moveToMode);
			case SPLINE:
				switch (moveToMode)
				{
					case DO_COMPLETE:
						return new MoveToSkill();
					case STAY:
						return new MoveAndStaySkill();
				}
		}
		throw new IllegalStateException();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public final List<ACommand> calcEntryActions(final List<ACommand> cmds)
	{
		maxLinearVelocity = Math.min(maxLinearVelocity, Sisyphus.maxLinearVelocity);
		return doCalcEntryActions(cmds);
	}
	
	
	protected List<ACommand> doCalcEntryActions(final List<ACommand> cmds)
	{
		HermiteSpline2D pos = new HermiteSpline2D(getPos().multiplyNew(0.001f), getPos().multiplyNew(0.001f), getVel(),
				AVector2.ZERO_VECTOR);
		HermiteSpline rot = new HermiteSpline(getAngle(), getAngle(), getaVel(), 0);
		
		SplinePair3D pair = new SplinePair3D();
		pair.setPositionTrajectory(new HermiteSplineTrajectory2D(pos));
		pair.setRotationTrajectory(new HermiteSplineTrajectory1D(rot));
		
		setNewTrajectory(pair, Arrays.asList(new IVector2[] { getPos() }));
		return cmds;
	}
	
	
	@Override
	public final List<ACommand> calcActions(final List<ACommand> cmds)
	{
		if (isMoveComplete())
		{
			complete();
			
			return cmds;
		}
		
		periodicProcess(cmds);
		
		if ((splinePair.getPositionTrajectory() != null) && (splinePair.getRotationTrajectory() != null))
		{
			if ((getBotType() == EBotType.GRSIM) || (getBotType() == EBotType.TIGER))
			{
				sendVelOrPos(cmds);
				return cmds;
			}
			TigerBotV2 botV2 = (TigerBotV2) getBot();
			if (botV2.getControllerType() == EControllerType.FUSION)
			{
				switch (botV2.getCtrlMoveType())
				{
					case POS:
						sendPositions(cmds);
						break;
					case SPLINE:
						sendSplines(cmds);
						break;
					case SPLINE_POS:
						sendSplinesOrPos(cmds);
						break;
					case VEL:
						sendVelocities(cmds);
						break;
					default:
						break;
				
				}
			} else if (botV2.getControllerType() == EControllerType.FUSION_VEL)
			{
				sendVelocities(cmds);
			}
		}
		return cmds;
	}
	
	
	@Override
	public final List<ACommand> calcExitActions(final List<ACommand> cmds)
	{
		if (getSisyphus() != null)
		{
			getSisyphus().clearPath(getBot().getBotID());
		}
		TigerCtrlResetCommand cmd = new TigerCtrlResetCommand();
		cmds.add(cmd);
		return doCalcExitActions(cmds);
	}
	
	
	protected List<ACommand> doCalcExitActions(final List<ACommand> cmds)
	{
		return cmds;
	}
	
	
	protected abstract void periodicProcess(List<ACommand> cmds);
	
	
	protected final void setNewTrajectory(final SplinePair3D traj, final List<IVector2> nodes)
	{
		splinePair = traj;
		
		splinePair.setStartTime(System.nanoTime());
		
		if (getWorldFrame().isInverted())
		{
			splinePairMirrored = new SplinePair3D(traj);
			splinePairMirrored.mirror();
		}
		
		lastSentPositionPart = -1;
		lastSentRotationPart = -1;
		
		destination = splinePair.getPositionTrajectory().getPosition(splinePair.getTotalTime());
		targetOrientation = splinePair.getRotationTrajectory().getPosition(splinePair.getTotalTime());
		
		getBot().newSpline(traj);
	}
	
	
	protected HermiteSplineTrajectory2D getPositionTraj()
	{
		return splinePair.getPositionTrajectory();
	}
	
	
	protected HermiteSplineTrajectory1D getRotationTraj()
	{
		return splinePair.getRotationTrajectory();
	}
	
	
	/**
	 * Send splines unless we are near our destination, then
	 * send positions
	 * 
	 * @param cmds
	 */
	private void sendSplinesOrPos(final List<ACommand> cmds)
	{
		float tCur = splinePair.getTrajectoryTime();
		float tEnd = splinePair.getPositionTrajectory().getTotalTime();
		IVector2 curDest = splinePair.getPositionTrajectory().getValueByTime(tCur);
		IVector2 targetDest = splinePair.getPositionTrajectory().getValueByTime(tEnd);
		float dist = GeoMath.distancePP(targetDest, curDest);
		
		if (dist < (changeToPosCtrlThreshold / 1000.0f))
		{
			sendPositions(cmds);
		} else
		{
			sendSplines(cmds);
		}
	}
	
	
	/**
	 * Send velocities unless we are near our destination, then
	 * send positions
	 * 
	 * @param cmds
	 */
	private void sendVelOrPos(final List<ACommand> cmds)
	{
		float tCur = splinePair.getTrajectoryTime();
		float tEnd = splinePair.getPositionTrajectory().getTotalTime();
		IVector2 curDest = splinePair.getPositionTrajectory().getValueByTime(tCur);
		IVector2 targetDest = splinePair.getPositionTrajectory().getValueByTime(tEnd);
		float dist = GeoMath.distancePP(targetDest, curDest);
		
		if (dist < (changeToPosCtrlThreshold / 1000.0f))
		{
			sendPositions(cmds);
		} else
		{
			sendVelocities(cmds);
		}
	}
	
	
	/**
	 * The legacy version only sends out simple velocity commands.
	 * 
	 * @param cmds
	 */
	private void sendVelocities(final List<ACommand> cmds)
	{
		float t = splinePair.getTrajectoryTime();
		
		Vector2 globalVel = splinePair.getPositionTrajectory().getVelocity(t);
		float angle = splinePair.getRotationTrajectory().getPosition(t);
		Vector2 localVel = AiMath.convertGlobalBotVector2Local(globalVel, angle);
		
		float w = splinePair.getRotationTrajectory().getVelocity(t);
		
		float velComp = -w * getDt();
		localVel.turn(velComp);
		
		cmds.add(new TigerMotorMoveV2(localVel, w));
	}
	
	
	/**
	 * Send positions of the spline to the bot,
	 * using a small lookahead
	 * 
	 * @param cmds
	 */
	private void sendPositions(final List<ACommand> cmds)
	{
		if (overridePP)
		{
			IVector2 locDest = destination;
			float locTargetOrient = targetOrientation;
			if (getWorldFrame().isInverted())
			{
				locDest = destination.multiplyNew(-1);
				locTargetOrient = AngleMath.normalizeAngle(targetOrientation + AngleMath.PI);
			}
			cmds.add(new TigerSkillPositioningCommand(locDest, locTargetOrient));
			return;
		}
		SplinePair3D locSplinePair;
		if (getWorldFrame().isInverted())
		{
			locSplinePair = splinePairMirrored;
		} else
		{
			locSplinePair = splinePair;
		}
		
		float ct = locSplinePair.getTrajectoryTime();
		float t = Math.min(ct + positionMoveLookAhead, locSplinePair.getTotalTime());
		
		IVector2 dest = locSplinePair.getPositionTrajectory().getValueByTime(t).multiplyNew(1000.0f);
		float orient = locSplinePair.getRotationTrajectory().getPosition(t);
		
		cmds.add(new TigerSkillPositioningCommand(dest, orient));
	}
	
	
	/**
	 * For the new TigerBotV2 the splines are directly transferred to the bot.
	 * 
	 * @param cmds
	 */
	private void sendSplines(final List<ACommand> cmds)
	{
		SplinePair3D locSplinePair;
		if (getWorldFrame().isInverted())
		{
			locSplinePair = splinePairMirrored;
		} else
		{
			locSplinePair = splinePair;
		}
		
		float t = locSplinePair.getTrajectoryTime();
		
		if (lastSentPositionPart < 0)
		{
			TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
			splineCmd.setSpline(locSplinePair.getPositionTrajectory().getSpline(0));
			splineCmd.setOption(1);
			
			cmds.add(splineCmd);
			
			lastSentPositionPart = 0;
			// System.out.println("Spline time: " + getPositionTraj().getTotalTime() + " parts="
			// + getPositionTraj().getNumParts());
			// System.out.println("MaxVel: " + getPositionTraj().getPart(0).spline.getMaxFirstDerivative());
			// System.out.println("MaxAcc: " + getPositionTraj().getPart(0).spline.getMaxSecondDerivative());
			// System.out.println("spline: " + getPositionTraj().getPart(0).spline.toString());
		}
		
		if (lastSentPositionPart < (locSplinePair.getPositionTrajectory().getNumParts() - 1))
		{
			HermiteSplineTrajectoryPart2D lastPart = locSplinePair.getPositionTrajectory().getPart(lastSentPositionPart);
			HermiteSplineTrajectoryPart2D partToSend = locSplinePair.getPositionTrajectory().getPart(
					lastSentPositionPart + 1);
			
			float sendTime = (lastPart.startTime + lastPart.endTime) / 2;
			
			if (t > sendTime)
			{
				TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
				splineCmd.setSpline(partToSend.spline);
				splineCmd.setOption(lastSentPositionPart + 2);
				
				cmds.add(splineCmd);
				
				// System.out.println(lastSentPositionPart + " MaxVel: "
				// + getPositionTraj().getPart(lastSentPositionPart).spline.getMaxFirstDerivative());
				// System.out.println(lastSentPositionPart + " MaxAcc: "
				// + getPositionTraj().getPart(lastSentPositionPart).spline.getMaxSecondDerivative());
				// System.out.println(lastSentPositionPart + " spline: "
				// + getPositionTraj().getPart(lastSentPositionPart).spline.toString());
				lastSentPositionPart++;
			}
		}
		
		if (lastSentRotationPart < 0)
		{
			TigerCtrlSpline1D splineCmd = new TigerCtrlSpline1D();
			splineCmd.setSpline(locSplinePair.getRotationTrajectory().getSpline(0));
			splineCmd.setOption(1);
			
			cmds.add(splineCmd);
			
			lastSentRotationPart = 0;
		}
		
		if (lastSentRotationPart < (locSplinePair.getRotationTrajectory().getNumParts() - 1))
		{
			HermiteSplineTrajectoryPart1D lastPart = locSplinePair.getRotationTrajectory().getPart(lastSentRotationPart);
			HermiteSplineTrajectoryPart1D partToSend = locSplinePair.getRotationTrajectory().getPart(
					lastSentRotationPart + 1);
			
			float sendTime = (lastPart.start + lastPart.end) / 2;
			
			if (t > sendTime)
			{
				TigerCtrlSpline1D splineCmd = new TigerCtrlSpline1D();
				splineCmd.setSpline(partToSend.spline);
				splineCmd.setOption(lastSentRotationPart + 2);
				
				cmds.add(splineCmd);
				
				lastSentRotationPart++;
			}
		}
	}
	
	
	/**
	 * @param cmds list to put the move command
	 */
	public final void stopMove(final List<ACommand> cmds)
	{
		getDevices().dribble(cmds, false);
		if (getBot().getType() == EBotType.TIGER_V2)
		{
			TigerBotV2 botV2 = (TigerBotV2) getBot();
			if (botV2.getControllerType() == EControllerType.FUSION_VEL)
			{
				cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
			} else if ((botV2.getControllerType() == EControllerType.FUSION)
					&& (botV2.getCtrlMoveType() == ECtrlMoveType.VEL))
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
	public final void stopMoveImmediately(final List<ACommand> cmds)
	{
		if (getBot().getType() == EBotType.TIGER_V2)
		{
			TigerBotV2 botV2 = (TigerBotV2) getBot();
			if (botV2.getControllerType() == EControllerType.FUSION_VEL)
			{
				cmds.add(new TigerMotorMoveV2(new Vector2(0, 0), 0f));
			} else
			{
				TigerCtrlSpline2D splineCmd = new TigerCtrlSpline2D();
				splineCmd.setOption(0);
				
				cmds.add(splineCmd);
				
				TigerCtrlSpline1D splineCmd2 = new TigerCtrlSpline1D();
				splineCmd2.setOption(0);
				
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
	protected final void visualizePath(final BotID botId, final List<IVector2> nodes, final SplinePair3D pair)
	{
		Path path = new Path(botId, nodes, nodes.get(nodes.size() - 1), 0);
		path.setHermiteSpline(pair);
		getSisyphus().onNewPath(path);
	}
	
	
	/**
	 * Create an empty dummy path to remove current spline and path
	 */
	protected final void removePath()
	{
		// List<IVector2> nodesSpline = new ArrayList<IVector2>(2);
		// nodesSpline.add(DistanceUnit.MILLIMETERS.toMeters(getPos()));
		// nodesSpline.add(DistanceUnit.MILLIMETERS.toMeters(getPos()));
		// List<IVector2> nodes = new ArrayList<IVector2>(1);
		// nodes.add(getPos());
		//
		// SplinePair3D pair = new SplineTrajectoryGenerator().create(nodesSpline, Vector2.ZERO_VECTOR,
		// Vector2.ZERO_VECTOR,
		// 0, 0, 0, 0);
		// visualizePath(getBot().getBotID(), nodes, pair);
		getSisyphus().clearPath(getBot().getBotID());
	}
	
	
	protected final SplineTrajectoryGenerator createDefaultGenerator(final EBotType botType)
	{
		SplineTrajectoryGenerator gen = new SplineTrajectoryGenerator();
		gen.setPositionTrajParams(maxLinearVelocity, Sisyphus.maxLinearAcceleration);
		gen.setReducePathScore(0.0f);
		gen.setRotationTrajParams(Sisyphus.maxRotateVelocity, Sisyphus.maxRotateAcceleration);
		return gen;
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param nodes on the path including destination and excluding current position
	 * @param finalOrientation
	 */
	protected final void createSpline(final List<IVector2> nodes, final float finalOrientation)
	{
		createSpline(nodes, finalOrientation, createDefaultGenerator(getBotType()));
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param nodes on the path including destination and excluding current position
	 * @param lookAtTarget final look
	 */
	protected final void createSpline(final List<IVector2> nodes, final IVector2 lookAtTarget)
	{
		createSpline(nodes, lookAtTarget, createDefaultGenerator(getBotType()));
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param nodes on the path including destination and excluding current position
	 * @param lookAtTarget final look
	 * @param gen custom spline generator
	 * @throws IllegalArgumentException if lookAtTarget and last node are equal
	 */
	protected final void createSpline(final List<IVector2> nodes, final IVector2 lookAtTarget,
			final SplineTrajectoryGenerator gen)
	{
		IVector2 dir = lookAtTarget.subtractNew(nodes.get(nodes.size() - 1));
		final float finalOrientation;
		if (dir.isZeroVector())
		{
			log.warn("lookAtTarget and destination are equal. Can not calculate final orientation. Keep current. "
					+ lookAtTarget + " " + nodes.get(nodes.size() - 1));
			finalOrientation = getAngle();
		} else
		{
			finalOrientation = dir.getAngle();
		}
		createSpline(nodes, finalOrientation, gen);
	}
	
	
	/**
	 * Create a new spline with the given path nodes and the final lookAtTarget.
	 * This will also set the spline for execution and sent it to visualizer
	 * 
	 * @param nodes on the path including destination and excluding current position
	 * @param finalOrientation
	 * @param gen custom spline generator
	 */
	protected final void createSpline(final List<IVector2> nodes, final float finalOrientation,
			final SplineTrajectoryGenerator gen)
	{
		SplinePair3D pair = createSplineWithoutDrivingIt(nodes, finalOrientation, gen);
		setNewTrajectory(pair, nodes);
		visualizePath(getBot().getBotID(), nodes, pair);
	}
	
	
	protected final SplinePair3D createSplineWithoutDrivingIt(final List<IVector2> nodes, final float finalOrientation,
			final SplineTrajectoryGenerator gen)
	{
		List<IVector2> nodesMM = new ArrayList<IVector2>(nodes.size() + 1);
		nodesMM.add(convertAIVector2SplineNode(getPos()));
		
		// use position on spline instead of current position (which may be wrong due to delay)
		// if ((getPositionTraj() != null) && (getTrajectoryTime() < getPositionTraj().getTotalTime()))
		// {
		// nodesMM.add(getPositionTraj().getValueByTime(getTrajectoryTime()));
		// } else
		// {
		// nodesMM.add(DistanceUnit.MILLIMETERS.toMeters(bot.getPos()));
		// }
		
		for (IVector2 vec : nodes)
		{
			nodesMM.add(convertAIVector2SplineNode(vec));
		}
		
		return gen.create(nodesMM, getVel(), AVector2.ZERO_VECTOR, convertAIAngle2SplineOrientation(getAngle()),
				convertAIAngle2SplineOrientation(finalOrientation), getaVel(), 0f);
	}
	
	
	private IVector2 convertAIVector2SplineNode(final IVector2 vec)
	{
		IVector2 mVec = DistanceUnit.MILLIMETERS.toMeters(vec);
		return mVec;
	}
	
	
	private float convertAIAngle2SplineOrientation(final float angle)
	{
		return angle;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	protected boolean isMoveComplete()
	{
		float t = splinePair.getTrajectoryTime();
		
		boolean position = (splinePair.getPositionTrajectory() == null ? false : ((t > splinePair.getPositionTrajectory()
				.getTotalTime())));
		boolean rotation = (splinePair.getRotationTrajectory() == null ? false : (t > splinePair.getRotationTrajectory()
				.getTotalTime()));
		
		return position && rotation;
	}
	
	
	/**
	 * @return the maxLinearVelocity
	 */
	public final float getMaxLinearVelocity()
	{
		return maxLinearVelocity;
	}
	
	
	/**
	 * @param maxLinearVelocity the maxLinearVelocity to set
	 */
	public final void setMaxLinearVelocity(final float maxLinearVelocity)
	{
		this.maxLinearVelocity = maxLinearVelocity;
	}
	
	
	/**
	 * @param overridePP the overridePP to set
	 */
	public final void setOverridePP(final boolean overridePP)
	{
		this.overridePP = overridePP;
	}
	
	
	/**
	 * @param destination the destination to set [mm]
	 */
	protected final void setDestination(final IVector2 destination)
	{
		this.destination = destination;
	}
	
	
	/**
	 * @param targetOrientation the targetOrientation to set
	 */
	public final void setTargetOrientation(final float targetOrientation)
	{
		this.targetOrientation = targetOrientation;
	}
}
