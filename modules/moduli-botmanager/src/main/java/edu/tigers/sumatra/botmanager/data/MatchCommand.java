package edu.tigers.sumatra.botmanager.data;

import edu.tigers.sumatra.botmanager.botskills.ABotSkill;
import edu.tigers.sumatra.botmanager.botskills.BotSkillMotorsOff;
import lombok.Data;


/**
 * A match command includes all information, a robot needs during a match
 */
@Data
public class MatchCommand
{
	ABotSkill skill = new BotSkillMotorsOff();
	MultimediaControl multimediaControl = new MultimediaControl();
}
