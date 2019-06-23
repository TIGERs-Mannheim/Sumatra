package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.summary;

import java.util.ArrayList;
import java.util.List;


/**
 * the table headers of history table
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public enum ESummaryTableColumns
{
	/**  */
	TYPE(0, "Type"),
	/**  */
	PLAY(1, "Play"),
	/**  */
	AMOUNT(2, "Amount"),
	/**  */
	DURATION(3, "Duration"),
	/**  */
	BOTS(4, "Bots"),
	/**  */
	RANDOM(5, "Random"),
	/**  */
	SUCCESSFUL_FIRST_TRY(6, "Successful, first try"),
	/**  */
	SUCCESSFUL_MULTIPLE_TRIES(7, "Successful, multiple tries"),
	/**  */
	SUCCESSFUL_EQUAL_MATCH(8, "Successful, equal match"),
	/**  */
	REFEREE(9, "Referee"),
	/** */
	SUCCESSFUL_UNKNOWN(10, "Successful, unknown"),
	/** */
	SUCCEEDED(11, "Succeeded"),
	/** */
	FAILED(12, "Failed"),
	/** neutral finished state */
	FINISHED(13, "Finished");
	
	
	private final int		id;
	private final String	label;
	
	
	private ESummaryTableColumns(int id, String label)
	{
		this.id = id;
		this.label = label;
	}
	
	
	/**
	 * @return column number starting with 0
	 */
	public int getColumnId()
	{
		return id;
	}
	
	
	/**
	 * @return the label which will be shown in the header of the table
	 */
	public String getLabel()
	{
		return label;
	}
	
	
	/**
	 * returns the according enum to a column id
	 * 
	 * @param columnId
	 * @return
	 */
	public static ESummaryTableColumns getColumnById(int columnId)
	{
		for (ESummaryTableColumns col : ESummaryTableColumns.values())
		{
			if (col.id == columnId)
			{
				return col;
			}
		}
		throw new IllegalArgumentException("No Asset column for ColumnId found");
	}
	
	
	/**
	 * get a list of all column labels
	 * 
	 * @return
	 */
	public static List<String> getColumnLabels()
	{
		List<String> columns = new ArrayList<String>();
		for (ESummaryTableColumns col : ESummaryTableColumns.values())
		{
			columns.add(col.getLabel());
		}
		return columns;
	}
}
