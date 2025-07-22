/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.botcenter.view;

import edu.tigers.sumatra.botmanager.ping.PingStats;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192")
public class BcBotPingPanel extends JPanel
{
	/**  */
	private static final long							serialVersionUID	= 4531411392390831333L;

	private final JTextField							minDelay				= new JTextField();
	private final JTextField							avgDelay				= new JTextField();
	private final JTextField							maxDelay				= new JTextField();
	private final JTextField							lostPings			= new JTextField();
	private final JTextField							numPings				= new JTextField("10");
	private final JTextField							pingPayload			= new JTextField("0");
	private final JButton								startStopPing		= new JButton("Start");

	private final List<IBcBotPingPanelObserver>	observers			= new CopyOnWriteArrayList<>();


	/**
	 * Constructor.
	 */
	public BcBotPingPanel()
	{
		setLayout(new MigLayout("wrap 2", "[50][100,fill]"));

		startStopPing.addActionListener(new StartStopPing());

		add(new JLabel("Num Pings:"));
		add(numPings);
		add(new JLabel("Payload:"));
		add(pingPayload);
		add(startStopPing, "span 2");
		add(new JLabel("Min Delay:"));
		add(minDelay);
		add(new JLabel("Avg Delay:"));
		add(avgDelay);
		add(new JLabel("Max Delay:"));
		add(maxDelay);
		add(new JLabel("Lost Pings:"));
		add(lostPings);
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IBcBotPingPanelObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IBcBotPingPanelObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * @param stats
	 */
	public void setPingStats(final PingStats stats)
	{
		minDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.minDelay));
		avgDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.avgDelay));
		maxDelay.setText(String.format(Locale.ENGLISH, "%1.3f", stats.maxDelay));
		lostPings.setText(Integer.toString(stats.lostPings));
	}


	private class StartStopPing implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			if ("Start".equals(startStopPing.getText()))
			{
				int num;
				int payload;

				try
				{
					num = Integer.valueOf(numPings.getText());
					payload = Integer.valueOf(pingPayload.getText());
				} catch (final NumberFormatException err)
				{
					return;
				}

				notifyStartPing(num, payload);

				startStopPing.setText("Stop");
			} else
			{
				notifyStopPing();

				startStopPing.setText("Start");
			}
		}


		private void notifyStartPing(final int numPings, final int payloadSize)
		{
			for (IBcBotPingPanelObserver observer : observers)
			{
				observer.onStartPing(numPings, payloadSize);
			}
		}


		private void notifyStopPing()
		{
			for (IBcBotPingPanelObserver observer : observers)
			{
				observer.onStopPing();
			}
		}
	}


	/**
	 * Observer interface.
	 */
	public interface IBcBotPingPanelObserver
	{
		/**
		 * @param numPings
		 * @param payloadSize
		 */
		void onStartPing(final int numPings, final int payloadSize);


		/**
		 * stop ping.
		 */
		void onStopPing();
	}
}
