package edu.tigers.sumatra.persistence.log;

import edu.tigers.sumatra.persistence.PersistenceTable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


// For reasoning behind this name ask André in severely sleep-deprived state.
public class PersistenceLogCohort implements PersistenceTable.IEntry<PersistenceLogCohort>
{
	@Getter
	private final List<PersistenceLogEvent> events = new ArrayList<>(1);


	public PersistenceLogCohort(PersistenceLogEvent event)
	{
		events.add(event);
	}


	@Override
	public long getKey()
	{
		return getTimestamp();
	}


	public long getTimestamp()
	{
		return events.getFirst().getTimestamp();
	}


	@Override
	public void merge(PersistenceLogCohort other)
	{
		events.addAll(other.events);
	}
}
