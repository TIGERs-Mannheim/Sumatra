/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.05.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.skilltester;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
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
public class WPTester extends ABaseRole implements ISkillSystemObserver
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
		START,
		MOVE,
		TIMEOUT,
		MOVE_STOP,
		TIMEOUT2,
		FINISHED,

	}
	
	protected float waypoints[][] = {
			{2200.0f,    1500.0f, 0.0f},
			{-2200.0f,   1500.0f, 0.0f},
			{-2200.0f,  -1500.0f, 0.0f},
			{2200.0f,   -1500.0f, 0.0f},
			{2200.0f,    1100.0f, 0.0f},
			{-1500.0f,   1100.0f, 0.0f},
			{-1500.0f,  -1100.0f, 0.0f},
			{1500.0f,   -1100.0f, 0.0f},
			{0.0f,        500.0f, 0.0f},
			{-500.0f,    -500.0f, 0.0f},
			{0f,          -500.0f, 0.0f},
			{0f,            0.0f, 0.0f},
			
	};
	
	private int currentWayPoint = 0;
	
	private State					state					= State.MOVE;
	private boolean				completed			= false;
	private boolean				isObserver			= false;
	
	private Long					startTime			= null;
	private final long			timeout				= 1000;											// ms
																													
	private IVector2				initialPos			= null;
	@SuppressWarnings("unused")
	private IVector2				currentSetpoint	= Vector2.ZERO_VECTOR;
	private long timer;
	
//	private Vector3				tolerance        = new Vector3(100, 100, (float) Math.PI/2);
	
	
//	private CSVExporter			exporter;
	
//	private final Rectanglef	FIELD					= AIConfig.getGeometry().getField();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public WPTester()
	{
		super(ERole.WP_TESTER);
		
		// register as a skill-system observer to observe skill-complete events
		try
		{
			((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).addObserver(this);
			isObserver = true;
		} catch (ModuleNotFoundException err)
		{
			err.printStackTrace();
		}
		timer = System.currentTimeMillis();
		
		
		
//		CSVExporter.createInstance("peter", "peter", true);
//		exporter = CSVExporter.getInstance("peter");
//		exporter.setHeader("time", "setx", "sety", "setv", "actx", "acty", "actv");
		
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
		if (initialPos == null)
		{
			initialPos = wFrame.tigerBots.get(getBotID()).pos;
		}
		// TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
		
		// IVector2 actVel = bot.vel.turnNew(AIMath.PI_HALF - bot.angle);
		// IVector2 actPos = bot.pos;
		// Header("time", "setx", "sety", "setv", "actx","acty","actv");
		// if (!exporter.isClosed())
		// {
		// exporter.addValues(System.nanoTime(), currentSetpoint.x(), currentSetpoint.y(), 0,
		// actPos.x() - initialPos.x(), actPos.y() - initialPos.y(), bot.angle);
		// }
		switch (state)
		{
			case MOVE:
				if (!completed)
				{
					destCon.updateDestination(new Vector2(waypoints[currentWayPoint][0],
							waypoints[currentWayPoint][1]));
					skills.moveTo(destCon.getDestination(),
										waypoints[currentWayPoint][2]);
					completed = false;
//					TrackedTigerBot bot = wFrame.tigerBots.get(getBotID());
//					if (Math.abs(bot.pos.x()-waypoints[currentWayPoint][0]) > tolerance.x() ||
//						 Math.abs(bot.pos.y()-waypoints[currentWayPoint][1]) > tolerance.y() ||
//						 Math.abs((bot.angle-waypoints[currentWayPoint][2])%(2*Math.PI))   > tolerance.z())
//					{
//						destCon.updateDestination(new Vector2(waypoints[currentWayPoint][0],
//								waypoints[currentWayPoint][1]));
//						skills.moveTo(destCon.getDestination(),
//								waypoints[currentWayPoint][2]);
//						completed = false;
//					}
//					else
//					{
//						completed = true;
//					}
				} else
				{
					System.out.println("this was waypoint " + currentWayPoint);
					currentWayPoint++;
					if (currentWayPoint > waypoints.length-1)
					{
						state = State.TIMEOUT;
					}
					completed = false;
				}
				
				break;
			
			case TIMEOUT:
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
						long timeDif = System.currentTimeMillis()-timer;
						System.out.println("finished in " + timeDif/1000 + "," + timeDif%1000 + " s");
						((ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID)).removeObserver(this);
						isObserver = false;
					} catch (ModuleNotFoundException err)
					{
						err.printStackTrace();
					}
				
//				exporter.close();
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
	}
}
