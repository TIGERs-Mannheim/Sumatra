/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.view.referee;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.Referee;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.SystemUtils;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.URI;


@Log4j2
public class ControlGameControllerPanel extends JPanel
{
	private static final String SPAN_2 = "span 2";

	private final JButton openControllerButton;
	private final JButton toggleGameController;


	public ControlGameControllerPanel()
	{
		setLayout(new MigLayout("wrap 2", "[fill]10[fill]"));

		openControllerButton = new JButton("Open SSL Game Controller UI");
		openControllerButton.addActionListener(a -> open());
		add(openControllerButton, SPAN_2);

		toggleGameController = new JButton("Start Game Controller");
		toggleGameController.addActionListener(a -> toggleGameController());
		add(toggleGameController, SPAN_2);
	}


	private void open()
	{
		String gameControllerAddress = "http://localhost:"
				+ SumatraModel.getInstance().getModule(Referee.class).getGameControllerUiPort();
		try
		{
			if (SystemUtils.IS_OS_UNIX
					&& Runtime.getRuntime().exec(new String[] { "which", "xdg-open" }).getInputStream().read() != -1)
			{
				// Desktop#browse is not well supported with Linux, so try xdg-open first
				Runtime.getRuntime().exec(new String[] { "xdg-open", gameControllerAddress });
				return;
			}
			if (Desktop.isDesktopSupported())
			{
				Desktop.getDesktop().browse(URI.create(gameControllerAddress));
			} else
			{
				log.warn("Opening web browser is not supported.");
			}
		} catch (IOException e)
		{
			log.warn("Could not execute command to open browser", e);
		}
	}


	private void toggleGameController()
	{
		final Referee referee = SumatraModel.getInstance().getModule(Referee.class);
		if (referee.isInternalGameControllerUsed())
		{
			referee.stopGameController();
		} else
		{
			referee.startGameController();
		}
		updateButtonStates();
	}


	@Override
	public void setEnabled(final boolean enable)
	{
		super.setEnabled(enable);

		EventQueue.invokeLater(this::updateButtonStates);
	}


	private void updateButtonStates()
	{
		final Referee referee = SumatraModel.getInstance().getModule(Referee.class);
		if (referee.isInternalGameControllerUsed())
		{
			openControllerButton.setEnabled(true);
			toggleGameController.setText("Stop Game Controller");
		} else
		{
			openControllerButton.setEnabled(false);
			toggleGameController.setText("Start Game Controller");
		}
	}
}
