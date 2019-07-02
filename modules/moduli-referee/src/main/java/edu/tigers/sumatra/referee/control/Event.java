package edu.tigers.sumatra.referee.control;

/**
 * Wrapper data structure for events sent to the game controller.
 * Only one field should be set at a time.
 */
public class Event
{
	private EventCard card;
	private EventCommand command;
	private EventStage stage;
	private EventTrigger trigger;
	private EventModifyValue modify;
	
	
	public Event(EventCard card)
	{
		this.card = card;
	}
	
	
	public Event(EventCommand command)
	{
		this.command = command;
	}
	
	
	public Event(EventTrigger trigger)
	{
		this.trigger = trigger;
	}
	
	
	public Event(EventStage stage)
	{
		this.stage = stage;
	}
	
	
	public Event(final EventModifyValue modify)
	{
		this.modify = modify;
	}
	
	
	public EventCard getCard()
	{
		return card;
	}
	
	
	public void setCard(final EventCard card)
	{
		this.card = card;
	}
	
	
	public EventCommand getCommand()
	{
		return command;
	}
	
	
	public void setCommand(final EventCommand command)
	{
		this.command = command;
	}
	
	
	public EventStage getStage()
	{
		return stage;
	}
	
	
	public void setStage(final EventStage stage)
	{
		this.stage = stage;
	}
	
	
	public EventTrigger getTrigger()
	{
		return trigger;
	}
	
	
	public void setTrigger(final EventTrigger trigger)
	{
		this.trigger = trigger;
	}
	
	
	public EventModifyValue getModify()
	{
		return modify;
	}
	
	
	public void setModify(final EventModifyValue modify)
	{
		this.modify = modify;
	}
}
