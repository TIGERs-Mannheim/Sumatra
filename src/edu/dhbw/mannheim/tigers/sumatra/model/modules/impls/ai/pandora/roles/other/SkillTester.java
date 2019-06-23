/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.02.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.other;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ABaseRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.SkillFacade;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.moduli.exceptions.ModuleNotFoundException;


/**
 * this is a test role which can be used to test individual skills.
 * you can program a defined sequence of skill that should be executed one after another
 * each skill has to complete before the next gets started
 * 
 * This role has been developed to realize a test procedure for skills. It is recommended to use it
 * in conjunction with the {@link CSVExporter}.
 * 
 * @author DanielW
 * 
 */
public class SkillTester extends ABaseRole implements ISkillSystemObserver
{
	/**  */
	private static final long	serialVersionUID	= 1833424209117756234L;
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * each state represents a skill that should be executed. add as appropriate.
	 */
	private enum State
	{
		TIMEOUT,
		FIRST_MOVE,
		SECOND_MOVE,
		THIRD_MOVE,
		SINE,
		KICK,
		TIMEOUT2,
		FINISHED;
	}
	
	private State			state			= State.TIMEOUT;
	private boolean		completed	= false;
	private boolean		isObserver	= false;
	
	private Long			startTime	= null;
	private final long	timeout		= 2000;				// ms
																		
	private CSVExporter	exporter;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public SkillTester()
	{
		super(ERole.SKILL_TESTER);
		
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
		
		CSVExporter.createInstance("kick", "kick", true);
		exporter = CSVExporter.getInstance("kick");
		exporter.setHeader("time", "bot-ball", "shot", "v");
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void update(AIInfoFrame currentFrame)
	{
		// not used
	}
	

	@Override
	public void calculateSkills(WorldFrame wFrame, SkillFacade skills)
	{
		if (!exporter.isClosed())
		{
			TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
			exporter.addValues(System.nanoTime(), wFrame.ball.pos.subtractNew(bot.pos).getLength2(), completed ? 1000 : 0,
					wFrame.ball.vel.getLength2());
		}
		switch (state)
		{
			case TIMEOUT:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= timeout)
				{
					state = State.KICK;
					startTime = null;
				}
				

				break;
			case FIRST_MOVE:
				if (!completed)
				{
					// move to 0,0; with look at 0
					skills.moveTo(new Vector2f(0, 0), 0);
					completed = false;
				} else
				{
					state = State.SECOND_MOVE;
					completed = false;
				}
				
				break;
			case SECOND_MOVE:
				if (!completed)
				{
					// move to 0,0; with look at 0
					// skills.moveTo(new Vector2f(1000, 1000), 0.0f);
					skills.moveTo(new Vector2f(1000, 1000), 0);
					completed = false;
				} else
				{
					state = State.THIRD_MOVE;
					completed = false;
				}
				
				break;
			case THIRD_MOVE:
				if (!completed)
				{
					// move to 0,0; with look at 0
					skills.moveTo(new Vector2f(1000, -1000), 0);
					completed = false;
				} else
				{
					state = State.FINISHED;
					completed = false;
				}
				
				break;
			case SINE: // not used now
				if (!completed)
				{
					// sine for 60sec
					skills.sinus(60000);
					completed = false;
				} else
				{
					state = State.FINISHED;
					completed = false;
				}
				
				break;
			case KICK:
				if (!completed)
				{
					skills.kickForce();
					completed = false;
				} else
				{
					state = State.TIMEOUT2;
					completed = false;
				}
				break;
			case TIMEOUT2:
				if (startTime == null)
					startTime = System.nanoTime();
				
				if ((System.nanoTime() - startTime) / 1e6 >= timeout)
				{
					state = State.FINISHED;
					startTime = null;
				}
				break;
			case FINISHED:
				// remove observer
				if (isObserver)
					try
					{
						((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).removeObserver(this);
						isObserver = false;
					} catch (ModuleNotFoundException err)
					{
						err.printStackTrace();
					}
				
				exporter.close();
				break;
			
			default:
				break;
		}
	}
	

	@Override
	public void onSkillStarted(ASkill skill, int botID)
	{
		
	}
	

	@Override
	public void onSkillCompleted(ASkill skill, int botID)
	{
		if (botID == getBotID()) // check on skill type is not needed since we only enqueue one skill at a time
			completed = true;
		
		// System.out.println("something completed: " + skill.getSkillName() + " bot id was: " + botID);
		
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
