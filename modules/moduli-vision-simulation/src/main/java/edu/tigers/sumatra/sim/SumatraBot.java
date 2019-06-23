/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.Optional;
import java.util.Random;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.bot.params.IBotParams;
import edu.tigers.sumatra.botmanager.bots.ASimBot;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.BotParamsManager.IBotParamsManagerObserver;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.botshape.IBotShape;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.util.SimBotDynamics;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.wp.data.MotionContext;


/**
 * Bot for internal Sumatra simulation
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SumatraBot extends ASimBot implements ISimulatedObject, IBotParamsManagerObserver
{
	private static final Logger log = Logger.getLogger(SumatraBot.class.getName());
	private static final double NEAR_BARRIER_TOLERANCE = 2;
	
	@Configurable(defValue = "2.5")
	private static double mass = 2.5;
	@Configurable(defValue = "false")
	private static boolean sendFeedback = false;
	@Configurable(defValue = "false")
	private static boolean applyNoise = false;
	@Configurable(defValue = "100.0", comment = "[Hz] desired number of package to receive")
	private static double updateRate = 100;
	
	private boolean barrierInterrupted = false;
	
	private transient SimBotDynamics dynamics = new SimBotDynamics();
	private transient long lastFeedbackTime = 0;
	private final Random rnd = new Random(0);
	private transient IBotParams botParams = new BotParams();
	
	
	static
	{
		ConfigRegistration.registerClass("botmgr", SumatraBot.class);
	}
	
	
	@SuppressWarnings("unused")
	private SumatraBot()
	{
		super();
	}
	
	
	/**
	 * @param id
	 * @param bs
	 */
	public SumatraBot(final BotID id, final SumatraBaseStation bs)
	{
		super(EBotType.SUMATRA, id, bs);
		init();
	}
	
	
	private void init()
	{
		double inv = 1;
		if (getBotId().getTeamColor() == ETeamColor.YELLOW)
		{
			inv *= -1;
		}
		int id = getBotId().getNumber();
		double x = (id % 2 == 0 ? 1 : -1) * 300.0 * (id - id % 2 + 1);
		double y = inv * (Geometry.getFieldWidth() / 2 + Geometry.getBotRadius() * 2);
		dynamics.setPos(Vector3.fromXYZ(x, y, 0));
		dynamics.setVelLocal(Vector3.zero());
	}
	
	
	@Override
	public void update(final double dt, final long timestamp)
	{
		BotSkillInput input = new BotSkillInput(getMatchCtrl().getSkill(), dynamics.getPos(), dynamics.getVelLocal(),
				dynamics.getAccLocal(), timestamp, getMatchCtrl().isStrictVelocityLimit());
		
		botSkillSim.execute(input);
		
		SumatraBaseStation sbs = (SumatraBaseStation) getBaseStation();
		
		for (ACommand cmd : botSkillSim.getLastBotSkillOutput().getCommands())
		{
			sbs.notifyCommand(getBotId(), cmd);
		}
		
		double dtFeedback = (input.gettNow() - lastFeedbackTime) * 1e-9;
		if (sendFeedback && (dtFeedback >= (1.0 / updateRate)))
		{
			lastFeedbackTime = input.gettNow();
			
			TigerSystemMatchFeedback fb = new TigerSystemMatchFeedback();
			fb.setDribblerTemp(25.0);
			fb.setBatteryLevel(16.8);
			fb.setCurPosition(dynamics.getPos().getXYVector(), dynamics.getPos().z());
			fb.setCurVelocity(dynamics.getVelLocal());
			fb.setDribblerSpeed(getDribblerSpeed());
			fb.setHardwareId(42);
			fb.setKickerLevel(getKickerLevel());
			fb.setBarrierInterrupted(barrierInterrupted);
			sbs.notifyMatchFeedback(getBotId(), fb);
		}
	}
	
	
	@Override
	public void step(final double dt, final MotionContext context)
	{
		updateBarrier(context);
		dynamics.step(dt, botSkillSim.getLastBotSkillOutput());
	}
	
	
	private void updateBarrier(final MotionContext context)
	{
		IBotShape botShape = BotShape.fromFullSpecification(getPos().getXYVector(), Geometry.getBotRadius(),
				getCenter2DribblerDist(), getPos().z());
		barrierInterrupted = botShape.isPointInKickerZone(context.getBallInfo().getPos().getXYVector(),
				Geometry.getBallRadius() + NEAR_BARRIER_TOLERANCE);
	}
	
	
	private IVector3 noisyVector(IVector3 v, double noise)
	{
		if (applyNoise)
		{
			return v.addNew(Vector3.fromXY(rnd.nextGaussian() * noise, rnd.nextGaussian() * noise));
		}
		return v;
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return
	 */
	public double getKickSpeed()
	{
		if (botSkillSim.getLastBotSkillOutput().getKickMode() == EKickerMode.DISARM)
		{
			return 0;
		}
		
		if (botSkillSim.getLastBotSkillOutput().getKickDevice() == EKickerDevice.CHIP)
		{
			double maxChipSpeed = getBotParams().getKickerSpecs().getMaxAbsoluteChipVelocity();
			return Math.min(botSkillSim.getLastBotSkillOutput().getKickSpeed(), maxChipSpeed);
		}
		return botSkillSim.getLastBotSkillOutput().getKickSpeed();
	}
	
	
	@Override
	public double getDribblerSpeed()
	{
		// Actually, this would also need to come from a dynamics model of the dribbler motor.
		return botSkillSim.getLastBotSkillOutput().getDribblerRPM();
	}
	
	
	@Override
	public void start()
	{
		try
		{
			SumatraSimulator simulator = (SumatraSimulator) SumatraModel.getInstance().getModule(AVisionFilter.class);
			simulator.registerBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli", err);
		}
		
		try
		{
			BotParamsManager botParamsManager = SumatraModel.getInstance()
					.getModule(BotParamsManager.class);
			if (getBotId().getTeamColor() == ETeamColor.YELLOW)
			{
				botParams = botParamsManager.get(EBotParamLabel.SIMULATION_YELLOW);
			} else
			{
				botParams = botParamsManager.get(EBotParamLabel.SIMULATION_BLUE);
			}
			botParamsManager.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find BotParamsManager module", err);
		}
		
		super.start();
	}
	
	
	@Override
	public void stop()
	{
		super.stop();
		try
		{
			SumatraSimulator simulator = (SumatraSimulator) SumatraModel.getInstance().getModule(AVisionFilter.class);
			simulator.unregisterBot(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find module ACam", err);
		} catch (ClassCastException err)
		{
			log.error("You try to use a SumatraBot with wrong moduli", err);
		}
		
		try
		{
			BotParamsManager botParamsManager = SumatraModel.getInstance()
					.getModule(BotParamsManager.class);
			botParamsManager.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find BotParamsManager module", err);
		}
	}
	
	
	/**
	 * @return the pos [x,y,w]
	 */
	@Override
	public IVector3 getPos()
	{
		return dynamics.getPos();
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	@Override
	public void setPos(final IVector3 pos)
	{
		dynamics.setPos(pos);
	}
	
	
	/**
	 * @return the vel [x,y,w]
	 */
	@Override
	public IVector3 getVel()
	{
		return dynamics.getVelGlobal();
	}
	
	
	/**
	 * @param vel the vel to set
	 */
	@Override
	public void setVel(final IVector3 vel)
	{
		dynamics.setVelGlobal(vel);
	}
	
	
	@Override
	public Optional<BotState> getSensoryState(final long timestamp)
	{
		return Optional.of(BotState.of(getBotId(), State.of(
				Pose.from(noisyVector(getPos(), 2)),
				noisyVector(getVel(), 0.01))));
	}
	
	
	@Override
	public void addVel(final IVector3 vector3)
	{
		dynamics.setVelGlobal(dynamics.getVelGlobal().addNew(vector3));
	}
	
	
	/**
	 * @return
	 */
	public double getMass()
	{
		return mass;
	}
	
	
	@Override
	public boolean isBarrierInterrupted()
	{
		return barrierInterrupted;
	}
	
	
	@Override
	public IBotParams getBotParams()
	{
		return botParams;
	}
	
	
	@Override
	public void onBotParamsUpdated(final EBotParamLabel label, final IBotParams params)
	{
		if (((getBotId().getTeamColor() == ETeamColor.YELLOW) && (label == EBotParamLabel.SIMULATION_YELLOW)) ||
				((getBotId().getTeamColor() == ETeamColor.BLUE) && (label == EBotParamLabel.SIMULATION_BLUE)))
		{
			botParams = params;
		}
	}
}
