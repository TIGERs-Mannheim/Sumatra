package edu.tigers.sumatra.gui.botparams.view;

import edu.tigers.sumatra.treetable.ITreeTableModel;
import edu.tigers.sumatra.treetable.JTreeTable;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.io.Serial;


/**
 * JTreeTable extension for JSON. Hides root node and always uses a String editor.
 *
 * @author AndreR <andre@ryll.cc>
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class JTreeTableJson extends JTreeTable
{
	/**  */
	@Serial
	private static final long serialVersionUID = -3162116773973599163L;


	/**
	 * @param treeTableModel
	 */
	public JTreeTableJson(final ITreeTableModel treeTableModel)
	{
		super(treeTableModel);

		tree.expandRow(0);
		tree.setRootVisible(false);
	}


	@Override
	public TableCellEditor getCellEditor(final int row, final int column)
	{
		if (column == 1)
		{
			return getDefaultEditor(String.class);
		}

		return super.getCellEditor(row, column);
	}


	@Override
	public TableCellRenderer getCellRenderer(final int row, final int column)
	{
		if (column == 1)
		{
			return getDefaultRenderer(String.class);
		}

		return super.getCellRenderer(row, column);
	}

}
