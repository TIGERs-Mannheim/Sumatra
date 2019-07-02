package edu.tigers.sumatra.referee.control;

/**
 * Data structure for modification from game controller
 */
public class EventModifyValue
{
	private EventTeam forTeam;
	private Integer goals;
	private Integer goalkeeper;
	private String teamName;
	
	
	public static EventModifyValue goals(final EventTeam forTeam, final int goals)
	{
		EventModifyValue e = new EventModifyValue();
		e.setForTeam(forTeam);
		e.setGoals(goals);
		return e;
	}
	
	
	public static EventModifyValue goalkeeper(final EventTeam forTeam, final int goalkeeper)
	{
		EventModifyValue e = new EventModifyValue();
		e.setForTeam(forTeam);
		e.setGoalkeeper(goalkeeper);
		return e;
	}
	
	
	public static EventModifyValue teamName(final EventTeam forTeam, final String name)
	{
		EventModifyValue e = new EventModifyValue();
		e.setForTeam(forTeam);
		e.setTeamName(name);
		return e;
	}
	
	
	public EventTeam getForTeam()
	{
		return forTeam;
	}
	
	
	public void setForTeam(final EventTeam forTeam)
	{
		this.forTeam = forTeam;
	}
	
	
	public Integer getGoals()
	{
		return goals;
	}
	
	
	public void setGoals(final Integer goals)
	{
		this.goals = goals;
	}
	
	
	public Integer getGoalkeeper()
	{
		return goalkeeper;
	}
	
	
	public void setGoalkeeper(final Integer goalkeeper)
	{
		this.goalkeeper = goalkeeper;
	}
	
	
	public String getTeamName()
	{
		return teamName;
	}
	
	
	public void setTeamName(final String teamName)
	{
		this.teamName = teamName;
	}
}
