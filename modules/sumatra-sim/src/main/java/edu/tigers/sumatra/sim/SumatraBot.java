/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.bot.EBotType;
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
	private double mass = 2.5f;
	@Configurable(defValue = "false")
	private static boolean sendFeedback = false;
	
	private boolean barrierInterrupted = false;
	
	private transient SimBotDynamics dynamics = new SimBotDynamics();
	private transient long lastFeedbackTime = 0;
	protected transient IBotParams botParams = new BotParams();
	
	
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
	
	
	/**
	 * @param id
	 * @param bs
	 * @param pos
	 * @param vel
	 */
	public SumatraBot(final BotID id, final SumatraBaseStation bs, final IVector3 pos, final IVector3 vel)
	{
		super(EBotType.SUMATRA, id, bs);
		dynamics.setPos(pos);
		dynamics.setVelGlobal(vel);
	}
	
	
	private void init()
	{
		double inv = 1;
		if (getBotId().getTeamColor() == ETeamColor.YELLOW)
		{
			inv *= -1;
		}
		int id = getBotId().getNumber();
		double x = inv * (Geometry.getPenaltyMarkOur().x() + 1000);
		double y = (id % 2 == 0 ? 1 : -1) * 120.0 * (id - id % 2 + 1);
		dynamics.setPos(Vector3.fromXYZ(x, y, 0));
		dynamics.setVelLocal(Vector3.zero());
	}
	
	
	@Override
	public void update(final double dt, final long timestamp)
	{
		BotSkillInput input = new BotSkillInput(getMatchCtrl().getSkill(), dynamics.getPos(), dynamics.getVelLocal(),
				dynamics.getAccLocal(), timestamp);
		
		botSkillSim.execute(input);
		
		SumatraBaseStation sbs = (SumatraBaseStation) getBaseStation();
		
		for (ACommand cmd : botSkillSim.getLastBotSkillOutput().getCommands())
		{
			sbs.notifyCommand(getBotId(), cmd);
		}
		
		double dtFeedback = (input.gettNow() - lastFeedbackTime) * 1e-9;
		if (sendFeedback && (dtFeedback >= (1.0 / getUpdateRate())))
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
			SumatraSimulator simulator = (SumatraSimulator) SumatraModel.getInstance().getModule(AVisionFilter.MODULE_ID);
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
			BotParamsManager botParamsManager = (BotParamsManager) SumatraModel.getInstance()
					.getModule(BotParamsManager.MODULE_ID);
			if (getBotId().getTeamColor() == ETeamColor.YELLOW)
			{
				botParams = botParamsManager.getBotParams(EBotParamLabel.SIMULATION_YELLOW);
			} else
			{
				botParams = botParamsManager.getBotParams(EBotParamLabel.SIMULATION_BLUE);
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
			SumatraSimulator simulator = (SumatraSimulator) SumatraModel.getInstance().getModule(AVisionFilter.MODULE_ID);
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
			BotParamsManager botParamsManager = (BotParamsManager) SumatraModel.getInstance()
					.getModule(BotParamsManager.MODULE_ID);
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
	
	
	/**
	 * @return the pos [x,y,w]
	 */
	@Override
	public Optional<IVector3> getSensoryPos()
	{
		return Optional.of(getPos());
	}
	
	
	/**
	 * @return the vel [x,y,w]
	 */
	@Override
	public Optional<IVector3> getSensoryVel()
	{
		return Optional.of(getVel());
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
