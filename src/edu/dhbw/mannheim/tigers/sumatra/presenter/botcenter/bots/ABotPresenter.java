/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots;

import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ABotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerDribble;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv3.TigerSystemBotSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.StraightMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.IFeatureChangedObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.ISkillsPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.IMotorEnhancedInputPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.motor.MotorInputPanel.IMotorInputPanelObserver;


/**
 * Presenter base for all bots. Every presenter is responsible for one bot.
 * 
 * @author AndreR
 */
public abstract class ABotPresenter implements IMotorInputPanelObserver, ISkillsPanelObserver,
		IMotorEnhancedInputPanelObserver, IFeatureChangedObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log			= Logger.getLogger(ABotPresenter.class.getName());
	protected BotCenterTreeNode	node			= null;
	private ASkillSystem				skillsystem	= null;
	private ABot						bot;
	private ABotManager				botmanager	= null;
	
	private boolean					statsActive	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 */
	public ABotPresenter(final ABot bot)
	{
		this.bot = bot;
		
		try
		{
			skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule("skillsystem");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Skillsystem not found", err);
			
			return;
		}
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
		try
		{
			botmanager = (ABotManager) SumatraModel.getInstance().getModule("botmanager");
		} catch (final ModuleNotFoundException err)
		{
			log.error("Botmanager not found", err);
			
			return;
		}
	}
	
	
	@Override
	public void onMoveToXY(final float x, final float y)
	{
		IMoveToSkill skill = AMoveSkill.createMoveToSkill();
		skill.setDoComplete(true);
		skill.getMoveCon().updateDestination(new Vector2(x, y));
		skillsystem.execute(bot.getBotID(), skill);
	}
	
	
	@Override
	public void onRotateAndMoveToXY(final float x, final float y, final float angle)
	{
		IMoveToSkill skill = AMoveSkill.createMoveToSkill();
		skill.setDoComplete(true);
		skill.getMoveCon().updateDestination(new Vector2(x, y));
		skill.getMoveCon().updateTargetAngle(angle);
		skillsystem.execute(bot.getBotID(), skill);
	}
	
	
	@Override
	public void onStraightMove(final int distance, final float angle)
	{
		skillsystem.execute(bot.getBotID(), new StraightMoveSkill(distance, angle));
	}
	
	
	@Override
	public void onLookAt(final Vector2 lookAtTarget)
	{
		IMoveToSkill skill = AMoveSkill.createMoveToSkill();
		skill.setDoComplete(true);
		skill.getMoveCon().updateLookAtTarget(lookAtTarget);
		skillsystem.execute(bot.getBotID(), skill);
	}
	
	
	@Override
	public void onDribble(final int rpm)
	{
		bot.execute(new TigerDribble(rpm));
	}
	
	
	@Override
	public void onSkill(final ASkill skill)
	{
		skillsystem.execute(bot.getBotID(), skill);
	}
	
	
	@Override
	public void onBotSkill(final ABotSkill skill)
	{
		bot.execute(new TigerSystemBotSkill(skill));
	}
	
	
	@Override
	public void onSetSpeed(final float x, final float y, final float w, final float v)
	{
	}
	
	
	@Override
	public void onSetSpeed(final float x, final float y, final float w)
	{
		bot.execute(new TigerMotorMoveV2(new Vector2(x, y), w));
	}
	
	
	@Override
	public void onNewVelocity(final Vector2 xy)
	{
		bot.execute(new TigerMotorMoveV2(xy));
	}
	
	
	@Override
	public void onNewAngularVelocity(final float w)
	{
		final TigerMotorMoveV2 move = new TigerMotorMoveV2(w);
		move.setV(0);
		bot.execute(move);
	}
	
	
	@Override
	public void onFeatureChanged(final EFeature feature, final EFeatureState state)
	{
		bot.getBotFeatures().put(feature, state);
	}
	
	
	@Override
	public void onApplyFeaturesToAll(final Map<EFeature, EFeatureState> features)
	{
		for (ABot aBot : botmanager.getAllBots().values())
		{
			aBot.getBotFeatures().putAll(features);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return
	 */
	public BotCenterTreeNode getTreeNode()
	{
		return node;
	}
	
	
	/**
	 * @return
	 */
	public abstract ABot getBot();
	
	
	/**
	 * @return
	 */
	public abstract JPanel getSummaryPanel();
	
	
	/**
	 * @return
	 */
	public abstract JPanel getFastChgPanel();
	
	
	/**
	 * Do some preparation for deleting this presenter.
	 */
	public void delete()
	{
	}
	
	
	/**
	 * @return the statsActive
	 */
	public final boolean isStatsActive()
	{
		return statsActive;
	}
	
	
	/**
	 * @param statsActive the statsActive to set
	 */
	public final void setStatsActive(final boolean statsActive)
	{
		this.statsActive = statsActive;
	}
	
	
	/**
	 * @return the botmanager
	 */
	public final ABotManager getBotmanager()
	{
		return botmanager;
	}
}
