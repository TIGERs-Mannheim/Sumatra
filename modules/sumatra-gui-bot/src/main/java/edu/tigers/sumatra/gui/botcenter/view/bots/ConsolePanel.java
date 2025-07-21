/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view.bots;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.Jsoner;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand.ConsoleCommandTarget;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.view.TextPane;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serial;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Command line interface to a bot.
 *
 * @author AndreR
 */
public class ConsolePanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 6126587090532483501L;
	private static final Logger log = LogManager.getLogger(ConsolePanel.class.getName());
	private final TextPane textPane = new TextPane(1100);
	private final JTextField cmdInput = new JTextField();
	private final JRadioButton targetMedia = new JRadioButton("Media");
	private final List<String> history = new ArrayList<>();
	private final List<IConsolePanelObserver> observers = new CopyOnWriteArrayList<>();
	private int histId = 0;
	private long timingStart = 0;


	/** Constructor. */
	@SuppressWarnings("unchecked")
	public ConsolePanel()
	{
		setLayout(new BorderLayout());

		ButtonGroup group = new ButtonGroup();
		final JRadioButton targetMain = new JRadioButton("Main");
		group.add(targetMain);
		group.add(targetMedia);

		targetMain.setSelected(true);

		cmdInput.addActionListener(new SendCommand(false));
		cmdInput.addKeyListener(new InputKeyListener());

		JButton btnSend2All = new JButton("Send to all");
		btnSend2All.addActionListener(new SendCommand(true));

		JPanel targetPanel = new JPanel(new MigLayout("wrap 3, fill", "[fill]"));
		targetPanel.add(targetMain);
		targetPanel.add(targetMedia);
		targetPanel.add(btnSend2All);
		targetPanel.add(cmdInput, "span 3");

		add(targetPanel, BorderLayout.NORTH);
		add(textPane, BorderLayout.CENTER);

		String strHist = SumatraModel.getInstance().getUserProperty(ConsolePanel.class.getCanonicalName());
		if (strHist != null)
		{
			Object obj;
			try
			{
				obj = Jsoner.deserialize(strHist);
				Map<String, Object> jsonMap = (Map<String, Object>) obj;

				List<String> jsonArray = (List<String>) jsonMap.get("history");
				history.addAll(jsonArray);
			} catch (JsonException err)
			{
				log.error("Could not parse history", err);
			}
		}
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IConsolePanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IConsolePanelObserver observer)
	{
		observers.remove(observer);
	}


	/** Console Panel Observer. */
	public interface IConsolePanelObserver
	{
		/**
		 * @param cmd
		 * @param target
		 */
		void onConsoleCommand(String cmd, ConsoleCommandTarget target);


		/**
		 * @param cmd
		 * @param target
		 */
		void onConsoleCommand2All(String cmd, ConsoleCommandTarget target);
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Add a print command to log pane.
	 *
	 * @param print
	 */
	public void addConsolePrint(final TigerSystemConsolePrint print)
	{
		final Color color;
		switch (print.getSource())
		{
			case MEDIA:
				color = new Color(0, 0, 192);
				break;
			case LEFT:
				color = new Color(192, 192, 0);
				break;
			case RIGHT:
				color = new Color(0, 192, 0);
				break;
			case KD:
				color = new Color(192, 0, 0);
				break;
			default:
				color = new Color(0, 0, 0);
				break;
		}

		final StyleContext sc = StyleContext.getDefaultStyleContext();
		final AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

		textPane.append(print.getText() + "\n", aset);

		if (print.getText().startsWith("Time run out:"))
		{
			double time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timingStart) / 1000.0;
			textPane.append("Real time: " + time + "\n", aset);
		}
	}

	private class SendCommand implements ActionListener
	{
		private final boolean send2All;


		/**
		 * @param send2All
		 */
		public SendCommand(final boolean send2All)
		{
			this.send2All = send2All;
		}


		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			ConsoleCommandTarget target = ConsoleCommandTarget.MAIN;
			Color color = new Color(0, 0, 0);
			if (targetMedia.isSelected())
			{
				target = ConsoleCommandTarget.MEDIA;
				color = new Color(0, 0, 255);
			}

			final StyleContext sc = StyleContext.getDefaultStyleContext();
			AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
			aset = sc.addAttribute(aset, StyleConstants.Bold, true);

			String text = cmdInput.getText();

			Calendar cal = Calendar.getInstance();
			Date time = cal.getTime();
			DateFormat formatter = DateFormat.getTimeInstance(DateFormat.MEDIUM);
			textPane.append(formatter.format(time) + "> " + text + "\n", aset);
			if (history.isEmpty() || !history.getLast().equals(text))
			{
				history.add(text);
				Map<String, Object> jsonMap = new LinkedHashMap<>();
				jsonMap.put("history", history);
				SumatraModel.getInstance().setUserProperty(
						ConsolePanel.class.getCanonicalName(), Jsoner.serialize(jsonMap));
			}
			histId = 0;
			cmdInput.setText("");


			if (text.startsWith("timing"))
			{
				timingStart = System.nanoTime();
			}

			if (send2All)
			{
				notifyConsoleCommand2All(text, target);
			} else
			{
				notifyConsoleCommand(text, target);
			}
		}


		private void notifyConsoleCommand(final String cmd, final ConsoleCommandTarget target)
		{
			for (IConsolePanelObserver observer : observers)
			{
				observer.onConsoleCommand(cmd, target);
			}
		}


		private void notifyConsoleCommand2All(final String cmd, final ConsoleCommandTarget target)
		{
			for (IConsolePanelObserver observer : observers)
			{
				observer.onConsoleCommand2All(cmd, target);
			}
		}
	}


	private class InputKeyListener implements KeyListener
	{

		@Override
		public void keyTyped(final KeyEvent e)
		{
			// not used
		}


		@Override
		public void keyPressed(final KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_UP)
			{
				histId++;
				int id = history.size() - histId;
				if (id >= 0)
				{
					cmdInput.setText(history.get(id));
				} else
				{
					histId = history.size();
				}
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				histId--;
				int id = history.size() - histId;
				if ((id >= 0) && (id < history.size()))
				{
					cmdInput.setText(history.get(id));
				} else
				{
					cmdInput.setText("");
					histId = 0;
				}
			}
		}


		@Override
		public void keyReleased(final KeyEvent e)
		{
			// not used
		}
	}
}
