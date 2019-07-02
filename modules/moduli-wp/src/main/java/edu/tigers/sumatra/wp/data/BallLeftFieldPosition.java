package edu.tigers.sumatra.wp.data;

public class BallLeftFieldPosition
{
	private final TimedPosition position;
	private final EBallLeftFieldType type;
	
	
	public BallLeftFieldPosition(final TimedPosition position, final EBallLeftFieldType type)
	{
		this.position = position;
		this.type = type;
	}
	
	
	public TimedPosition getPosition()
	{
		return position;
	}
	
	
	public EBallLeftFieldType getType()
	{
		return type;
	}
	
	public enum EBallLeftFieldType
	{
		TOUCH_LINE,
		GOAL_LINE,
		GOAL,
		GOAL_OVER,
	}
}
