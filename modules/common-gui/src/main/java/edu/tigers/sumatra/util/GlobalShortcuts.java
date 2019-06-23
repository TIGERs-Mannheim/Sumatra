/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 27, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Manage global shortcuts
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class GlobalShortcuts
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final List<ShortcutWrapper> SHORTCUTS = new CopyOnWriteArrayList<>();
	
	private static class ShortcutWrapper
	{
		private EShortcut				shortcut;
		private KeyEventDispatcher	dispatcher;
	}
	
	
	/**
	 */
	public enum EShortcut
	{
		/**  */
		EMERGENCY_MODE(KeyEvent.VK_ESCAPE, "esc"),
		/**  */
		START_STOP(KeyEvent.VK_F11, "F11"),
		
		/**  */
		MATCH_MODE(KeyEvent.VK_F1, "F1"),
		
		/**  */
		MATCH_LAYOUT(KeyEvent.VK_F2, "F2"),
		/**  */
		TIMEOUT_LAYOUT(KeyEvent.VK_F3, "F3"),
		/**  */
		DEFAULT_LAYOUT(KeyEvent.VK_F4, "F4"),
		
		/**  */
		CHARGE_ALL_BOTS(KeyEvent.VK_F5, "F5"),
		/**  */
		DISCHARGE_ALL_BOTS(KeyEvent.VK_F6, "F6"),
		
		/**  */
		RESET_FIELD(KeyEvent.VK_F7, "F7"),
		
		/**  */
		REFEREE_HALT(KeyEvent.VK_F8, "F8"),
		/**  */
		REFEREE_STOP(KeyEvent.VK_F9, "F9"),
		/**  */
		REFEREE_START(KeyEvent.VK_F10, "F10"),
		
		;
		
		private final int		key;
		private final String	desc;
									
									
		private EShortcut(final int key, final String desc)
		{
			this.key = key;
			this.desc = desc;
		}
		
		
		/**
		 * @return the key
		 */
		public final int getKey()
		{
			return key;
		}
		
		
		/**
		 * @return the desc
		 */
		public final String getDesc()
		{
			return desc;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private GlobalShortcuts()
	{
	
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param shortcut
	 * @param run
	 */
	public static void register(final EShortcut shortcut, final Runnable run)
	{
		KeyEventDispatcher ked = new KeyEventDispatcher()
		{
			@Override
			public boolean dispatchKeyEvent(final KeyEvent e)
			{
				if ((e.getID() == KeyEvent.KEY_PRESSED))
				{
					if ((e.getKeyCode() == shortcut.key))
					{
						run.run();
					}
				}
				return false;
			}
		};
		ShortcutWrapper w = new ShortcutWrapper();
		w.shortcut = shortcut;
		w.dispatcher = ked;
		SHORTCUTS.add(w);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked);
	}
	
	
	/**
	 * @param shortcut
	 */
	public static void unregisterAll(final EShortcut shortcut)
	{
		List<ShortcutWrapper> toBeDeleted = new ArrayList<ShortcutWrapper>();
		for (ShortcutWrapper w : SHORTCUTS)
		{
			if (w.shortcut.equals(shortcut))
			{
				toBeDeleted.add(w);
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(w.dispatcher);
			}
		}
		SHORTCUTS.removeAll(toBeDeleted);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
