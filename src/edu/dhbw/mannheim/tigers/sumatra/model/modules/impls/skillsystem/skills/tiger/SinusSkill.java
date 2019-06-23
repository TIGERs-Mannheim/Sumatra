/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.04.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger;

import java.util.ArrayList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter.CSVExporter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillGroup;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;


/**
 * moves the bot back and forth, with a sine applied to velocity
 * 
 * @author DanielW
 * 
 */
public class SinusSkill extends ASkill // pathplanningplay & Skilltester
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private long			startTime	= 0;
	private long			runTime		= 0;		// ms
	private CSVExporter	exporter		= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param runtime in ms
	 */
	public SinusSkill(long runtime)
	{
		super(ESkillName.SINUS, ESkillGroup.MOVE);
		this.runTime = runtime;
		// try
		// {
		CSVExporter.createInstance("sinABC", "sin", true);
		exporter = CSVExporter.getInstance("sinABC");
		exporter.setHeader("time", "set_velocity", "current_velocity");
		//
		// } catch (Exception err)
		// {
		// // err.printStackTrace();
		// } finally
		// {
		//
		// }
		
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public ArrayList<ACommand> calcActions(ArrayList<ACommand> cmds)
	{
		if (startTime == 0)
			startTime = System.nanoTime();
		// System.out.println(System.nanoTime() + "\n");
		final float velo = 2; // ein viertel der maximalgeschwindigkeit
		final int duration = 2000; // PI dauert ... ms
		long currentTime = (long) ((System.nanoTime() - startTime) / 1e6); // ms
		
		// create movement only in y-direction (back-forth);
		// System.out.println(AIMath.sin((float) currentTime / duration * AIMath.PI));
		// System.out.println(AIConfig.getSkills().getMaxVelocity());
		Vector2f move = new Vector2f(0, AIConfig.getSkills().getMaxVelocity() / velo
				* AIMath.sin((float) currentTime / duration * AIMath.PI));
		// System.out.println(move.y());
		cmds.add(new TigerMotorMoveV2(move, 0.0f));
		// if (true)
		exporter.addValues(System.nanoTime(), move.getLength2(), getBot().vel.getLength2());
		
		if (currentTime > runTime)
		{
			// stop movement
			cmds.add(new TigerMotorMoveV2(new Vector2f(0, 0), 0f));
			
			complete();
		}
		
		return cmds;
	}
	

	@Override
	protected boolean compareContent(ASkill newSkill)
	{
		return true;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
