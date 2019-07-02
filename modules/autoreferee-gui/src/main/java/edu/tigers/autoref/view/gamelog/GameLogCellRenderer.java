/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.gamelog;

import java.awt.Component;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.tigers.autoref.model.gamelog.GameLogTableModel;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.GameTime;


/**
 * {@link TableCellRenderer} implementation that is responsible for rendering the columns in a {@link JTable} which is
 * used in conjunction with the {@link GameLogTableModel}.
 */
public class GameLogCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = -6221824311185461448L;
	
	private static final DecimalFormat msFormat = new DecimalFormat("000");
	private static final DecimalFormat sFormat = new DecimalFormat("00");
	private static final DecimalFormat minFormat = new DecimalFormat("00");
	
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object objEntry, final boolean isSelected,
			final boolean hasFocus, final int row, final int column)
	{
		super.getTableCellRendererComponent(table, objEntry, isSelected, hasFocus, row, column);
		
		GameLogEntry entry = (GameLogEntry) objEntry;
		
		styleComponent(entry, column);
		
		/*
		 * Resize all but the last column to minimum size
		 */
		if (column < (table.getColumnCount() - 1))
		{
			doResize(table, column);
		}
		
		return this;
	}
	
	
	private void doResize(final JTable table, final int colIndex)
	{
		TableColumn col = table.getColumnModel().getColumn(colIndex);
		
		int preferredWidth = getPreferredSize().width + table.getIntercellSpacing().width + 10;
		int newWidth = Math.max(col.getPreferredWidth(), preferredWidth);
		
		col.setPreferredWidth(newWidth);
		col.setMaxWidth(newWidth);
	}
	
	
	private void styleComponent(final GameLogEntry entry, final int colIndex)
	{
		setText(getCellText(entry, colIndex));
		setToolTipText(entry.getToolTipText());
		setForeground(entry.getForegroundColor());
	}
	
	
	private String getCellText(final GameLogEntry entry, final int colIndex)
	{
		switch (colIndex)
		{
			case 0:
				return formatInstant(entry.getInstant());
			case 1:
				return formatGameTime(entry.getGameTime());
			case 2:
				return entry.getType().toString();
			case 3:
				return entry.workGameLogEntry();
			default:
				throw new IllegalArgumentException("Column index out of range: " + colIndex);
		}
	}
	
	
	private String formatInstant(final Instant instant)
	{
		StringBuilder builder = new StringBuilder();
		LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		
		builder.append(minFormat.format(date.getHour()));
		builder.append(":");
		builder.append(minFormat.format(date.getMinute()));
		builder.append(":");
		builder.append(sFormat.format(date.getSecond()));
		builder.append(":");
		builder.append(msFormat.format(date.getNano() / 1_000_000));
		
		return builder.toString();
	}
	
	
	private String formatGameTime(final GameTime gameTime)
	{
		StringBuilder builder = new StringBuilder();
		
		long micros = gameTime.getMicrosLeft();
		if (micros < 0)
		{
			builder.append("-");
			micros = Math.abs(micros);
		}
		
		int minutes = (int) TimeUnit.MICROSECONDS.toMinutes(micros);
		int seconds = (int) TimeUnit.MICROSECONDS.toSeconds(micros) % 60;
		int ms = (int) TimeUnit.MICROSECONDS.toMillis(micros) % 1_000;
		
		
		builder.append(minFormat.format(minutes));
		builder.append(":");
		builder.append(sFormat.format(seconds));
		builder.append(":");
		builder.append(msFormat.format(ms));
		
		return builder.toString();
	}
}
