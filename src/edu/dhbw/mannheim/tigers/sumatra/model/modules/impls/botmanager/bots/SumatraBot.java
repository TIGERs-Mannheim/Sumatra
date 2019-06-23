/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.botskills.BotSkillPositionPid;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.LimitedVelocityCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.other.TigerKickerKickV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.ISimulatedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.ISimulatedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.cam.SumatraCam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.LinearPolicy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ACam;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;
import edu.dhbw.mannheim.tigers.sumatra.util.learning.IPolicyController;


/**
 * Bot for internal Sumatra simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class SumatraBot extends SimBot implements ISimulatedObject
{
	private static final Logger					log							= Logger.getLogger(SumatraBot.class.getName());
	private Vector3									pos							= new Vector3();
	private Vector3									vel							= new Vector3();
	private transient Vector3						action						= new Vector3();
	private transient TigerKickerKickV3			kick							= new TigerKickerKickV3();
	
	@Configurable(comment = "multiplied on position error for PositionController")
	private static float								positionErrorMultiplier	= 0.003f;
	
	@Configurable(comment = "Dist [mm] - Distance between center of bot to dribbling bar")
	private static float								center2DribblerDist		= 75;
	
	private transient final IPolicyController	policy						= new LinearPolicy();
	
	
	@Configurable
	private static boolean							useTrajectory				= true;
	
	
	@SuppressWarnings("unused")
	private SumatraBot()
	{
		
	}
	
	
	/**
	 * @param botConfig
	 */
	public SumatraBot(final SubnodeConfiguration botConfig)
	{
		super(botConfig);
		init();
	}
	
	
	/**
	 * @param id
	 */
	public SumatraBot(final BotID id)
	{
		super(EBotType.SUMATRA, id, -1, id.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
		init();
	}
	
	
	private void init()
	{
		float y = 1000;
		float inv = 1;
		if (getBotID().getTeamColor() == ETeamColor.YELLOW)
		{
			y *= -1;
			inv *= -1;
		}
		
		switch (getBotID().getNumber())
		{
			case 0:
				pos = new Vector3(-3500 * inv, 0, 0);
				break;
			case 1:
				pos = new Vector3(-2500 * inv, -300, 0);
				break;
			case 2:
				pos = new Vector3(-2500 * inv, 300, 0);
				break;
			case 3:
				pos = new Vector3(-500 * inv, -1000, 0);
				break;
			case 4:
				pos = new Vector3(-1000 * inv, 800, 0);
				break;
			case 5:
				pos = new Vector3(-500 * inv, 0, 0);
				break;
			default:
				pos = new Vector3(-1000 + (300 * getBotID().getNumber()), y, 0);
		}
		
		vel = new Vector3();
	}
	
	
	@Override
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.WORKING);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.WORKING);
		return result;
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_MOTOR_MOVE_V2:
			{
				TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				IVector2 velGlob = AiMath.convertLocalBotVector2Global(move.getXY(), pos.z());
				action.set(0, -velGlob.x());
				action.set(1, -velGlob.y());
				action.set(2, -move.getW());
			}
				break;
			case CMD_SKILL_POSITIONING:
			{
				TigerSkillPositioningCommand posCmd = (TigerSkillPositioningCommand) cmd;
				IVector2 dest = posCmd.getDestination();
				float orient = posCmd.getOrientation();
				handlePositioning(dest, orient);
			}
				break;
			case CMD_SYSTEM_BOT_SKILL:
				TigerSystemBotSkill botSkill = (TigerSystemBotSkill) cmd;
				ABotSkill skill = botSkill.getSkill();
				switch (skill.getType())
				{
					case GLOBAL_POSITION:
						BotSkillGlobalPosition posCmd = (BotSkillGlobalPosition) skill;
						IVector3 globVel = handleTrajectoryMove(posCmd.getPos(), posCmd.getOrientation(), posCmd.getT(), pos,
								vel, 0.0f, false);
						action.set(0, globVel.x());
						action.set(1, globVel.y());
						action.set(2, globVel.z());
						break;
					case LOCAL_VELOCITY:
					{
						BotSkillLocalVelocity velCmd = (BotSkillLocalVelocity) skill;
						IVector2 velGlob = AiMath.convertLocalBotVector2Global(new Vector2(velCmd.getX(), velCmd.getY()),
								pos.z());
						action.set(0, -velGlob.x());
						action.set(1, -velGlob.y());
						action.set(2, -velCmd.getW());
					}
						break;
					case MOTORS_OFF:
					{
						action.set(0, 0);
						action.set(1, 0);
						action.set(2, 0);
					}
						break;
					case POSITION_PID:
						BotSkillPositionPid pidPos = (BotSkillPositionPid) skill;
						handlePositioning(pidPos.getPos(), pidPos.getOrientation());
						break;
					case PENALTY_SHOOT:
					case TUNE_PID:
					case GLOBAL_POS_VEL:
					case GLOBAL_VELOCITY:
					case ENC_TRAIN:
					default:
						log.warn("Unhandled bot skill: " + skill.getType());
						break;
				}
				break;
			case CMD_KICKER_KICKV3:
				kick = (TigerKickerKickV3) cmd;
				break;
			case CMD_SYSTEM_LIMITED_VEL:
				LimitedVelocityCommand limVelCmd = (LimitedVelocityCommand) cmd;
				getPerformance().setVelMaxOverride(limVelCmd.getMaxVelocity());
				break;
			default:
		}
	}
	
	
	private void handlePositioning(final IVector2 dest, final float orient)
	{
		IVector2 error = dest.subtractNew(pos.getXYVector()).multiply(positionErrorMultiplier);
		float errorW = -AngleMath.getShortestRotation(orient, pos.z());
		
		double[] stateArr = new double[] { error.x(), error.y(), errorW, vel.x(), vel.y(),
				vel.z() };
		Matrix state = new Matrix(stateArr, 1);
		Matrix u = policy.getControl(state);
		
		action.set(0, (float) u.get(0, 0));
		action.set(1, (float) u.get(0, 1));
		action.set(2, (float) u.get(0, 2));
	}
	
	
	@Override
	public void step(final float dt)
	{
		// float xyFactor = 10.0f;
		// float zFactor = 10.0f;
		// vel.set(0, vel.x() + (dt * xyFactor * (action.x() - vel.x())));
		// vel.set(1, vel.y() + (dt * xyFactor * (action.y() - vel.y())));
		// vel.set(2, vel.z() + (dt * zFactor * (action.z() - vel.z())));
		vel.set(action);
		
		pos.set(0, pos.x() + ((vel.x() * dt) * 1000));
		pos.set(1, pos.y() + ((vel.y() * dt) * 1000));
		pos.set(2, pos.z() + (vel.z() * dt));
		
		if (!AIConfig.getGeometry().getFieldWReferee().isPointInShape(pos.getXYVector()))
		{
			vel.set(AVector3.ZERO_VECTOR);
			pos.set(new Vector3(AIConfig.getGeometry().getFieldWReferee().nearestPointInside(pos.getXYVector()), pos
					.z()));
		}
	}
	
	
	/**
	 * @param ball
	 */
	public void ballInteraction(final ISimulatedBall ball)
	{
		
		IVector2 kickerPos = AiMath.getBotKickerPos(pos.getXYVector(), pos.z(), getCenter2DribblerDist());
		float dist = GeoMath.distancePP(kickerPos, ball.getPos().getXYVector());
		if ((dist < 20))
		{
			if ((kick.getMode() == EKickerMode.ARM)
					|| (((kick.getMode() == EKickerMode.FORCE))))
			{
				
				IVector2 vel = kickerPos.subtractNew(pos.getXYVector()).scaleTo(kick.getKickSpeed());
				ball.addVel(new Vector3(vel, 0));
				ball.setPos(ball.getPos().addNew(ball.getPos().subtractNew(pos)));
				kick = new TigerKickerKickV3();
			}
		} else if (ball
				.getPos()
				.getXYVector()
				.equals(pos.getXYVector(),
						AIConfig.getGeometry().getBallRadius() + 75))
		{
			ball.setVel(ball.getVel().multiplyNew(-1 / 2).getXYVector());
		}
		if (kick.getMode() == EKickerMode.FORCE)
		{
			kick = new TigerKickerKickV3();
		}
	}
	
	
	@Override
	public void start()
	{
		try
		{
			SumatraCam sc = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			sc.registerBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli");
		}
		super.start();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			SumatraCam sc = (SumatraCam) SumatraModel.getInstance().getModule(ACam.MODULE_ID);
			sc.unregisterBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli");
		}
	}
	
	
	@Override
	public float getBatteryLevel()
	{
		return 15.5f;
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return 16;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return 14;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return 200;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 200;
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
	}
	
	
	@Override
	public void setDefaultKickerMaxCap()
	{
	}
	
	
	/**
	 * @return the pos [x,y,w]
	 */
	@Override
	public IVector3 getPos()
	{
		return new Vector3(pos.getXYVector(), AngleMath.normalizeAngle(pos.z()));
	}
	
	
	/**
	 * @return the vel [x,y,w]
	 */
	@Override
	public IVector3 getVel()
	{
		return new Vector3(vel);
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	@Override
	public void setPos(final IVector3 pos)
	{
		this.pos = new Vector3(pos);
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	public void setVel(final IVector3 vel)
	{
		this.vel = new Vector3(vel);
	}
	
	
	@Override
	public void setVel(final IVector2 vel)
	{
		this.vel = new Vector3(vel, 0);
	}
	
	
	@Override
	public void addVel(final IVector3 vector3)
	{
		vel.add(vector3);
	}
	
	
	@Override
	public float getCenter2DribblerDist()
	{
		return center2DribblerDist;
	}
}
