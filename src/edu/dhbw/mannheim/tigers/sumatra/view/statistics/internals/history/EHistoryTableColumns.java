package edu.dhbw.mannheim.tigers.sumatra.view.statistics.internals.history;

import java.util.ArrayList;
import java.util.List;


/**
 * the table headers of history table
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public enum EHistoryTableColumns
{
	/**  */
	START(0, "Start"),
	/**  */
	END(1, "End"),
	/**  */
	DURATION(2, "Duration (ms)"),
	/**  */
	OFFENSIVE(3, "Offensive Play"),
	/**  */
	DEFFENSIVE(4, "Deffensive Play"),
	/**  */
	SUPPORT(5, "Support Play"),
	/**  */
	REASON(6, "Reason"),
	/**  */
	RESULT(7, "Result"),
	/**  */
	OTHERS(8, "Others");
	
	private final int		id;
	private final String	label;
	
	
	private EHistoryTableColumns(int id, String label)
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
	public static EHistoryTableColumns getColumnById(int columnId)
	{
		for (EHistoryTableColumns col : EHistoryTableColumns.values())
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
		for (EHistoryTableColumns col : EHistoryTableColumns.values())
		{
			columns.add(col.getLabel());
		}
		return columns;
	}
}
