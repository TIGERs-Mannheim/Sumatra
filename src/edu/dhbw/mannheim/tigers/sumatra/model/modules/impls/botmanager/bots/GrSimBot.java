/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 23, 2013
 * Author(s): TilmanS
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AngleMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.trajectory.SplinePair3D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.ENetworkState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimConnection;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.grsim.GrSimNetworkCfg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerChargeAuto;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerKickerKickV2.EKickerMode;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSkillPositioningCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AWorldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IWorldFrameConsumer;


/**
 * Bot used when playing in grSim simulator
 * Generates grSim protobuf commands
 * 
 * @author TilmanS
 */
public class GrSimBot extends ABot implements IWorldFrameConsumer
{
	// Logger
	private static final Logger	log							= Logger.getLogger(GrSimBot.class.getName());
	
	private GrSimConnection			con;
	private GrSimNetworkCfg			grSimCfg;
	
	private Vector2					lastDirection				= new Vector2();
	private float						lastAngularVelocity		= 0.0f;
	private float						lastCompensatedVelocity	= 0.0f;
	
	private Matrix						xEpsilon						= null;
	private Matrix						yEpsilon						= null;
	
	private int							maxCapacitorVoltage		= 0;
	private int							currentCapacitorVoltage	= 0;
	
	private float						velXBuffer					= 0.0f;
	private float						velYBuffer					= 0.0f;
	private float						velZBuffer					= 0.0f;
	
	private boolean					kickerDeadtimeActive		= false;
	private static final int		KICKER_DEADTIME_MS		= 1000;
	private static final int		KICKER_DRIBBLE_ARM_MS	= 3000;
	
	private SimpleWorldFrame		latestWorldFrame			= null;
	
	private ENetworkState			networkState				= ENetworkState.ONLINE;
	
	private class KickerDeadTimeTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			kickerDeadtimeActive = false;
			currentCapacitorVoltage = maxCapacitorVoltage;
		}
	}
	
	
	/**
	 * @param botConfig
	 * @throws BotInitException
	 */
	public GrSimBot(final SubnodeConfiguration botConfig) throws BotInitException
	{
		super(botConfig);
		networkState = ENetworkState.valueOf(botConfig.getString("networkState", "OFFLINE"));
	}
	
	
	/**
	 * @param id
	 */
	public GrSimBot(final BotID id)
	{
		super(EBotType.GRSIM, id, -1, id.getTeamColor() == ETeamColor.YELLOW ? 0 : 1);
	}
	
	
	@Override
	public HierarchicalConfiguration getConfiguration()
	{
		HierarchicalConfiguration config = super.getConfiguration();
		config.addProperty("bot.networkState", networkState);
		return config;
	}
	
	
	@Override
	protected Map<EFeature, EFeatureState> getDefaultFeatureStates()
	{
		Map<EFeature, EFeatureState> result = EFeature.createFeatureList();
		result.put(EFeature.DRIBBLER, EFeatureState.WORKING);
		result.put(EFeature.CHIP_KICKER, EFeatureState.WORKING);
		result.put(EFeature.STRAIGHT_KICKER, EFeatureState.WORKING);
		result.put(EFeature.MOVE, EFeatureState.WORKING);
		result.put(EFeature.BARRIER, EFeatureState.LIMITED);
		return result;
	}
	
	
	@Override
	public void execute(final ACommand cmd)
	{
		switch (cmd.getType())
		{
			case CMD_KICKER_CHARGE_AUTO:
			{
				chargeKicker((TigerKickerChargeAuto) cmd);
				break;
			}
			default:
			{
				sendGrSimCommand(cmd);
			}
		}
		
	}
	
	
	@Override
	public void newSpline(final SplinePair3D spline)
	{
		notifyNewSplineData(spline);
	}
	
	
	private void sendGrSimCommand(final ACommand cmd)
	{
		con.setId(botId.getNumber());
		
		switch (cmd.getType())
		{
			case CMD_MOTOR_MOVE_V2:
			{
				final TigerMotorMoveV2 move = (TigerMotorMoveV2) cmd;
				
				move.setUnusedComponents(lastDirection, lastAngularVelocity, lastCompensatedVelocity);
				
				lastDirection = move.getXY();
				lastAngularVelocity = move.getW();
				lastCompensatedVelocity = move.getV();
				
				if ((xEpsilon != null) && (yEpsilon != null))
				{
					final Matrix origCmd = new Matrix(3, 1);
					origCmd.set(0, 0, move.getXY().x);
					origCmd.set(1, 0, move.getXY().y);
					origCmd.set(2, 0, move.getW());
					
					final Matrix sendCmd = TigerBot.getMotorToCmdMatrix()
							.minus(xEpsilon.times(move.getXY().x).plus(yEpsilon.times(move.getXY().y)))
							.times(TigerBot.getCmdToMotorMatrix().times(origCmd));
					
					move.setX((float) sendCmd.get(0, 0));
					move.setY((float) sendCmd.get(1, 0));
					move.setW((float) sendCmd.get(2, 0));
				}
				
				handleMove(move, con);
				
				break;
			}
			case CMD_SKILL_POSITIONING:
				handlePositionMove((TigerSkillPositioningCommand) cmd, con);
				break;
			case CMD_KICKER_KICKV2:
				handleKick((TigerKickerKickV2) cmd, con);
				break;
			case CMD_MOTOR_DRIBBLE:
				handleDribble((TigerDribble) cmd, con);
				break;
			case CMD_CTRL_RESET:
				// ignore
				break;
			default:
				log.debug("Unhandled Command!" + cmd.getType().toString());
		}
		
		con.send();
	}
	
	
	/**
	 * @param move
	 * @param con
	 */
	private void handleMove(final TigerMotorMoveV2 move, final GrSimConnection con)
	{
		con.setWheelSpeed(false);
		if (move != null)
		{
			velXBuffer = move.getY();
			velYBuffer = -move.getX();
			velZBuffer = move.getW();
		}
		con.setVelX(velXBuffer);
		con.setVelY(velYBuffer);
		con.setVelZ(velZBuffer);
	}
	
	
	private void handlePositionMove(final TigerSkillPositioningCommand cmd, final GrSimConnection con)
	{
		IVector2 dest = cmd.getDestination();
		float orient = cmd.getOrientation();
		
		con.setWheelSpeed(false);
		
		if (latestWorldFrame != null)
		{
			TrackedTigerBot bot = latestWorldFrame.getBot(getBotID());
			if (bot == null)
			{
				log.warn("No bot with id " + getBotID());
				return;
			}
			IVector2 error = dest.subtractNew(bot.getPos()).multiply(0.002f);
			IVector2 localVel = AiMath.convertGlobalBotVector2Local(error, bot.getAngle());
			velXBuffer = localVel.y();
			velYBuffer = -localVel.x();
			
			float errorW = orient - bot.getAngle();
			velZBuffer = AngleMath.normalizeAngle(errorW) * 4;
		} else
		{
			velXBuffer = 0;
			velYBuffer = 0;
			velZBuffer = 0;
		}
		con.setVelX(velXBuffer);
		con.setVelY(velYBuffer);
		con.setVelZ(velZBuffer);
	}
	
	
	/**
	 * @param kick
	 * @param con
	 */
	private void handleKick(final TigerKickerKickV2 kick, final GrSimConnection con)
	{
		int kickmode = kick.getMode();
		if (kickmode == 3) // dribble-arm
		{
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				
				@Override
				public void run()
				{
					kick.setMode(EKickerMode.ARM);
					handleKick(kick, con);
				}
			}, KICKER_DRIBBLE_ARM_MS);
			// kickmode = 1;
			return;
		}
		if (kickmode == 0) // Force
		{
			con.setKickmode(0);
		} else if ((kickmode == 1) || (kickmode == 2)) // Arm || Disarm
		{
			con.setKickmode(1);
		}
		
		switch (kick.getDevice())
		{
			case TigerKickerKickV2.Device.STRAIGHT:
			{
				boolean disarm = false;
				if ((kickmode == 1) || (kickmode == 0)) // Force || Arm
				{
					if (kickerDeadtimeActive)
					{
						con.setKickspeedX(0.0f);
						con.setKickspeedZ(0.0f);
					} else
					{
						kickerDeadtimeActive = true;
						float firingDuration = kick.getFiringDuration();
						if (firingDuration > 10000.0f)
						{
							firingDuration = 10000.0f;
						}
						// TODO something smarter would be great...
						float kickspeed = ((maxCapacitorVoltage * 0.02f) * (firingDuration * 0.0001f))
								+ (kick.getLevel() * 0);
						con.setKickspeedX(kickspeed);
						con.setKickspeedZ(0.0f);
						
						// currentCapacitorVoltage = 0;
						Timer kickerDeadtime = new Timer();
						kickerDeadtime.schedule(new KickerDeadTimeTimerTask(), KICKER_DEADTIME_MS);
					}
				} else if (kick.getMode() == 2)
				{
					disarm = true;
				}
				con.setKickerDisarm(disarm);
				break;
			}
			case TigerKickerKickV2.Device.CHIP:
			{
				boolean disarm = false;
				if ((kickmode == 1) || (kickmode == 0)) // Force || Arm
				{
					if (kickerDeadtimeActive)
					{
						con.setKickspeedX(0.0f);
						con.setKickspeedZ(0.0f);
					} else
					{
						kickerDeadtimeActive = true;
						float firingDuration = kick.getFiringDuration();
						if (firingDuration > 10000.0f)
						{
							firingDuration = 10000.0f;
						}
						float kickspeed = (((maxCapacitorVoltage * 0.03f) * (firingDuration * 0.0001f)) / 2)
								+ (kick.getLevel() * 0); // TODO something smarter would be great...
						con.setKickspeedZ(kickspeed);
						con.setKickspeedX(kickspeed);
						
						// currentCapacitorVoltage = 0;
						Timer kickerDeadtime = new Timer();
						kickerDeadtime.schedule(new KickerDeadTimeTimerTask(), KICKER_DEADTIME_MS);
					}
				} else if (kick.getMode() == 2)
				{
					disarm = true;
				}
				con.setKickerDisarm(disarm);
				break;
			}
			default:
				break;
		}
	}
	
	
	/**
	 * @param dribble
	 * @param con
	 */
	private void handleDribble(final TigerDribble dribble, final GrSimConnection con)
	{
		if (Math.abs(dribble.getSpeed()) > 0.0)
		{
			con.setSpinner(true);
			con.setSpinnerSpeed(dribble.getSpeed());
		} else
		{
			con.setSpinner(false);
		}
	}
	
	
	private void chargeKicker(final TigerKickerChargeAuto cmd)
	{
		maxCapacitorVoltage = cmd.getMax();
		currentCapacitorVoltage = maxCapacitorVoltage;
	}
	
	
	@Override
	public void start()
	{
		con.open();
		AWorldPredictor wp;
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
	}
	
	
	/**
	 * @param networkState the networkState to set
	 */
	public final void setNetworkState(final ENetworkState networkState)
	{
		this.networkState = networkState;
		notifyNetworkStateChanged(networkState);
	}
	
	
	@Override
	public void stop()
	{
		AWorldPredictor wp;
		try
		{
			wp = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			wp.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find WP module", err);
		}
		con.close();
	}
	
	
	@Override
	public float getBatteryLevel()
	{
		return 15.5f;
	}
	
	
	@Override
	public float getKickerLevel()
	{
		return currentCapacitorVoltage;
	}
	
	
	@Override
	public ENetworkState getNetworkState()
	{
		return networkState;
	}
	
	
	@Override
	public float getBatteryLevelMax()
	{
		return 16f;
	}
	
	
	@Override
	public float getBatteryLevelMin()
	{
		return 14f;
	}
	
	
	/**
	 * @param grSimCfg
	 */
	public void setGrSimCfg(final GrSimNetworkCfg grSimCfg)
	{
		this.grSimCfg = grSimCfg;
		con = new GrSimConnection(grSimCfg);
	}
	
	
	/**
	 * @return the grSimCfg
	 */
	public final GrSimNetworkCfg getGrSimCfg()
	{
		return grSimCfg;
	}
	
	
	@Override
	public void onNewSimpleWorldFrame(final SimpleWorldFrame worldFrame)
	{
		latestWorldFrame = worldFrame;
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrame wFrame)
	{
	}
	
	
	@Override
	public void onStop()
	{
	}
	
	
	@Override
	public void onVisionSignalLost(final SimpleWorldFrame emptyWf)
	{
		latestWorldFrame = null;
	}
	
	
	@Override
	public float getKickerLevelMax()
	{
		return 200;
	}
	
}
