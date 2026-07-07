package edu.tigers.autoref.model.gamelog;

import edu.tigers.autoreferee.engine.log.ELogEntryType;
import edu.tigers.autoreferee.engine.log.GameLogEntry;

import javax.swing.RowFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class GameLogRowFilter extends RowFilter<GameLogTableModel, Integer>
{
	private Set<ELogEntryType> includedTypes;


	public GameLogRowFilter()
	{
		includedTypes = new HashSet<>(Arrays.asList(ELogEntryType.values()));
	}


	public void setIncludedTypes(final Set<ELogEntryType> types)
	{
		includedTypes = types;
	}


	@Override
	public boolean include(final Entry<? extends GameLogTableModel, ? extends Integer> entry)
	{
		GameLogEntry logEntry = (GameLogEntry) entry.getValue(0);
		return includedTypes.contains(logEntry.getType());
	}
}
