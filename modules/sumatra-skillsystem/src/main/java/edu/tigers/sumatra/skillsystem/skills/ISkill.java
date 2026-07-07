package edu.tigers.sumatra.skillsystem.skills;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;


/**
 * Interface to skills from outside of {@link ASkillSystem}.
 */
public interface ISkill
{
	void update(SkillInput input);


	void calcActions();


	/**
	 * Called once upon end of this skill
	 */
	void calcExitActions();


	/**
	 * Called once upon start of this skill
	 */
	void calcEntryActions();


	/**
	 * @return
	 */
	BotID getBotId();


	/**
	 * @return
	 */
	boolean isAssigned();


	/**
	 * @return
	 */
	boolean isInitialized();


	/**
	 * Create bot ai infos based on current state
	 *
	 * @return new bot ai info structure
	 */
	BotAiInformation getBotAiInfo();

	/**
	 * @return the last applied trajectory
	 */
	default TrajectoryWithTime<IVector3> getCurrentTrajectory()
	{
		return null;
	}

	default void setCurrentTrajectory(TrajectoryWithTime<IVector3> trajectory)
	{
	}
}
