/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.gamelog;

import java.awt.Color;
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
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.engine.log.GameLogFormatter;
import edu.tigers.autoreferee.engine.log.GameTime;


/**
 * {@link TableCellRenderer} implementation that is responsible for rendering the columns in a {@link JTable} which is
 * used in conjunction with the {@link GameLogTableModel}.
 * 
 * @author "Lukas Magel"
 */
public class GameLogCellRenderer extends DefaultTableCellRenderer
{
	/**  */
	private static final long				serialVersionUID	= -6221824311185461448L;
	
	private static final DecimalFormat	msFormat				= new DecimalFormat("000");
	private static final DecimalFormat	sFormat				= new DecimalFormat("00");
	private static final DecimalFormat	minFormat			= new DecimalFormat("00");
	
	
	/**
	 *
	 */
	public GameLogCellRenderer()
	{
	}
	
	
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
		if ((column < (table.getColumnCount() - 1)))
		{
			doResize(table, column);
		}
		
		return this;
	}
	
	
	private void doResize(final JTable table, final int colIndex)
	{
		TableColumn col = table.getColumnModel().getColumn(colIndex);
		
		int preferedWidth = getPreferredSize().width + table.getIntercellSpacing().width + 10;
		int newWidth = Math.max(col.getPreferredWidth(), preferedWidth);
		
		col.setPreferredWidth(newWidth);
		col.setMaxWidth(newWidth);
	}
	
	
	private void styleComponent(final GameLogEntry entry, final int colIndex)
	{
		setText(getCellText(entry, colIndex));
		setToolTipText(getToolTipText(entry));
		setForeground(getForegroundColor(entry));
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
				switch (entry.getType())
				{
					case COMMAND:
						return GameLogFormatter.formatCommand(entry.getCommand());
					case FOLLOW_UP:
						FollowUpAction action = entry.getFollowUpAction();
						if (action == null)
						{
							return "Follow Up reset";
						}
						return GameLogFormatter.formatFollowUp(action);
					case GAME_STATE:
						return entry.getGamestate().toString();
					case REFEREE_MSG:
						return GameLogFormatter.formatRefMsg(entry.getRefereeMsg());
					case GAME_EVENT:
						IGameEvent event = entry.getGameEvent();
						StringBuilder builder = new StringBuilder();
						builder.append(event.toString());
						if (event.getFollowUpAction() != null)
						{
							builder.append(" | Next action: ");
							builder.append(GameLogFormatter.formatFollowUp(event.getFollowUpAction()));
						}
						return builder.toString();
				}
			default:
				throw new IllegalArgumentException("Column index out of range: " + colIndex);
		}
		
	}
	
	
	private String getToolTipText(final GameLogEntry entry)
	{
		StringBuilder builder = new StringBuilder();
		switch (entry.getType())
		{
			case COMMAND:
				builder.append("Sent the command \"" + GameLogFormatter.formatCommand(entry.getCommand())
						+ "\" to the refbox");
				break;
			case FOLLOW_UP:
				FollowUpAction action = entry.getFollowUpAction();
				if (action != null)
				{
					builder.append("Set the next action which is executed when the game reaches the Stopped state to: "
							+ GameLogFormatter.formatFollowUp(action));
					
				} else
				{
					builder.append("Reset the follow up action."
							+ " No actions will be taken when the game reaches the Stopped state");
				}
				break;
			case GAME_STATE:
				builder.append("The game state of the AutoReferee has changed to " + entry.getGamestate());
				break;
			case REFEREE_MSG:
				builder.append("Received a new referee msg from the refbox with command "
						+ entry.getRefereeMsg().getCommand());
				break;
			case GAME_EVENT:
				builder.append("The AutoReferee has registered the following game event");
				builder.append(System.lineSeparator());
				builder.append(entry.getGameEvent());
				break;
		}
		return builder.toString();
	}
	
	
	private Color getForegroundColor(final GameLogEntry entry)
	{
		switch (entry.getType())
		{
			case COMMAND:
				return new Color(150, 40, 0);
			case FOLLOW_UP:
				return new Color(0, 0, 150);
			case GAME_STATE:
				return new Color(0, 180, 0);
			case REFEREE_MSG:
				return new Color(50, 50, 50);
			case GAME_EVENT:
				return new Color(230, 0, 0);
		}
		return Color.BLACK;
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
