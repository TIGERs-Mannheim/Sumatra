/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 05.07.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.moveskill;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.AMoveSkillV2.AccelerationFacade;


/**
 * Panel for configuration of parameters in {@link AMoveSkillV2}
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class AccelerationSkillPanel extends JPanel
{
	/**
	 * Interface for {@link AccelerationSkillPanel}
	 */
	public interface IAccelerationSkillPanelObserver
	{
		public void onNewAccelerationFacade(AccelerationFacade accFacade);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long										serialVersionUID	= 2240645041913163381L;
	
	private final List<IAccelerationSkillPanelObserver>	observers			= new ArrayList<IAccelerationSkillPanelObserver>();
	
	private final JTextField										maxXAccelBeg;
	private final JTextField										maxXAccelMid;
	private final JTextField										maxXAccelEnd;
	

	private final JTextField										maxYAccelBeg;
	private final JTextField										maxYAccelMid;
	private final JTextField										maxYAccelEnd;
	
	private final JTextField										maxXDeccelBeg;
	private final JTextField										maxXDeccelMid;
	private final JTextField										maxXDeccelEnd;
	
	private final JTextField										maxYDeccelBeg;
	private final JTextField										maxYDeccelMid;
	private final JTextField										maxYDeccelEnd;
	
	private final JButton											saveButton;
	
	private final Logger log = Logger.getLogger(getClass());
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	

	public AccelerationSkillPanel()
	{
		setLayout(new MigLayout("fillx", "[]push"));
		setBorder(BorderFactory.createTitledBorder("Acceleration"));
		
		JPanel headLine = new JPanel(new MigLayout("fill", "[100!, fill]"));
		headLine.add(new JLabel());
		headLine.add(new JLabel("begin"));
		headLine.add(new JLabel("mid"));
		headLine.add(new JLabel("end"));
		

		JPanel maxXAcc = new JPanel(new MigLayout("fill", "[100!, fill]"));
		maxXAcc.add(new JLabel("maxXAcc"));
		maxXAccelBeg = new JTextField();
		maxXAcc.add(maxXAccelBeg);
		maxXAccelMid = new JTextField();
		maxXAcc.add(maxXAccelMid);
		maxXAccelEnd = new JTextField();
		maxXAcc.add(maxXAccelEnd);
		

		JPanel maxYAcc = new JPanel(new MigLayout("fill", "[100!, fill]"));
		maxYAcc.add(new JLabel("maxYAcc"));
		maxYAccelBeg = new JTextField();
		maxYAcc.add(maxYAccelBeg);
		maxYAccelMid = new JTextField();
		maxYAcc.add(maxYAccelMid);
		maxYAccelEnd = new JTextField();
		maxYAcc.add(maxYAccelEnd);
		

		JPanel maxXDec = new JPanel(new MigLayout("fill", "[100!, fill]"));
		maxXDec.add(new JLabel("maxXDecc"));
		maxXDeccelBeg = new JTextField();
		maxXDec.add(maxXDeccelBeg);
		maxXDeccelMid = new JTextField();
		maxXDec.add(maxXDeccelMid);
		maxXDeccelEnd = new JTextField();
		maxXDec.add(maxXDeccelEnd);
		
		JPanel maxYDec = new JPanel(new MigLayout("fill", "[100!, fill]"));
		maxYDec.add(new JLabel("maxYDecc"));
		maxYDeccelBeg = new JTextField();
		maxYDec.add(maxYDeccelBeg);
		maxYDeccelMid = new JTextField();
		maxYDec.add(maxYDeccelMid);
		maxYDeccelEnd = new JTextField();
		maxYDec.add(maxYDeccelEnd);
		
		// -----------------------------------
		
		this.add(headLine, "wrap");
		
		this.add(maxXAcc, "wrap");
		this.add(maxYAcc, "wrap");
		
		this.add(maxXDec, "wrap");
		this.add(maxYDec, "wrap");
		
		saveButton = new JButton("save");
		saveButton.addActionListener(new AccFacadeSaveAction());
		
		this.add(saveButton, "wrap");
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void addObserver(IAccelerationSkillPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	

	public void removeObserver(IAccelerationSkillPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	

	public AccelerationFacade getAccFacade()
	{
		AccelerationFacade facade = new AccelerationFacade();
		
		try
		{
			facade.maxXAccelBeg.setSavedString(maxXAccelBeg.getText());
			facade.maxXAccelMid.setSavedString(maxXAccelMid.getText());
			facade.maxXAccelEnd.setSavedString(maxXAccelEnd.getText());
			
			facade.maxYAccelBeg.setSavedString(maxYAccelBeg.getText());
			facade.maxYAccelMid.setSavedString(maxYAccelMid.getText());
			facade.maxYAccelEnd.setSavedString(maxYAccelEnd.getText());
			
			facade.maxXDeccelBeg.setSavedString(maxXDeccelBeg.getText());
			facade.maxXDeccelMid.setSavedString(maxXDeccelMid.getText());
			facade.maxXDeccelEnd.setSavedString(maxXDeccelEnd.getText());
			
			facade.maxYDeccelBeg.setSavedString(maxYDeccelBeg.getText());
			facade.maxYDeccelMid.setSavedString(maxYDeccelMid.getText());
			facade.maxYDeccelEnd.setSavedString(maxYDeccelEnd.getText());
		}
		
		catch(IllegalArgumentException ex)
		{
			log.warn("Invalid input for acceleration facade");
		}
		
		return facade;
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public void setAccelerationFacade(final AccelerationFacade fac)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				maxXAccelBeg.setText(fac.maxXAccelBeg.getSaveableString());
				maxXAccelMid.setText(fac.maxXAccelMid.getSaveableString());
				maxXAccelEnd.setText(fac.maxXAccelEnd.getSaveableString());
				maxYAccelBeg.setText(fac.maxYAccelBeg.getSaveableString());
				maxYAccelMid.setText(fac.maxYAccelMid.getSaveableString());
				maxYAccelEnd.setText(fac.maxYAccelEnd.getSaveableString());
				maxXDeccelBeg.setText(fac.maxXDeccelBeg.getSaveableString());
				maxXDeccelMid.setText(fac.maxXDeccelMid.getSaveableString());
				maxXDeccelEnd.setText(fac.maxXDeccelEnd.getSaveableString());
				maxYDeccelBeg.setText(fac.maxYDeccelBeg.getSaveableString());
				maxYDeccelMid.setText(fac.maxYDeccelMid.getSaveableString());
				maxYDeccelEnd.setText(fac.maxYDeccelEnd.getSaveableString());
			}
		});
	}
	
	public void notifyNewAccFacade(AccelerationFacade accFacade)
	{
		synchronized (observers)
		{
			for (IAccelerationSkillPanelObserver observer : observers)
			{
				observer.onNewAccelerationFacade(accFacade);
			}
		}
	}
	

	public void setMaxXAccelBeg(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXAccelBeg.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxXAccelMid(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXAccelMid.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxXAccelEnd(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXAccelEnd.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYAccelBeg(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYAccelBeg.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYAccelMid(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYAccelMid.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYAccelEnd(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYAccelEnd.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxXDecccelBeg(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXDeccelBeg.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxXDecccelMid(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXDeccelMid.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxXDecccelEnd(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxXDeccelEnd.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYDecccelBeg(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYDeccelBeg.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYDecccelMid(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYDeccelMid.setText(maxAcc.getSaveableString());
			}
		});
	}
	

	public void setMaxYDecccelEnd(final IVector2 maxAcc)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				AccelerationSkillPanel.this.maxYDeccelEnd.setText(maxAcc.getSaveableString());
			}
		});
	}
	
	// --------------------------------------------------------------------------
	// --- classes --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public class AccFacadeSaveAction implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			notifyNewAccFacade(getAccFacade());
		}
		
	}
	

}
