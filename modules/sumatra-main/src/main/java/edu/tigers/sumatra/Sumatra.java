/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra;

import java.awt.EventQueue;

import edu.tigers.sumatra.log.JULLoggingBridge;


/**
 * <pre>
 *          __  _-==-=_,-.
 *         /--`' \_@-@.--<
 *         `--'\ \   <___/.                 The wonderful thing about Tiggers,
 *             \ \\   " /                   is Tiggers are wonderful things.
 *               >=\\_/`<                   Their tops are made out of rubber,
 *   ____       /= |  \_/                   their bottoms are made out of springs.
 * _'    `\   _/=== \__/                    They're bouncy, trouncy, flouncy, pouncy,
 * `___/ //\./=/~\====\                     Fun, fun, fun, fun, fun.
 *     \   // /   | ===:                    But the most wonderful thing about Tiggers is,
 *      |  ._/_,__|_ ==:        __          I'm the only one.
 *       \/    \\ \\`--|       / \\
 *        |    _     \\:      /==:-\
 *        `.__' `-____/       |--|==:
 *           \    \ ===\      :==:`-'
 *           _>    \ ===\    /==/
 *          /==\   |  ===\__/--/
 *         <=== \  /  ====\ \\/
 *         _`--  \/  === \/--'
 *        |       \ ==== |
 *         -`------/`--' /
 *                 \___-'
 * </pre>
 *
 * The starter class of Sumatra.
 * Sumatra uses the MVP-passive view pattern in combination with moduli (a module-system for Java).
 * Make sure that you understand this approach to design an application,
 * before investigating Sumatra.
 * 
 * @author bernhard
 */
public final class Sumatra
{
	private Sumatra()
	{
	}
	
	
	static
	{
		// Connect java.util.logging (for jinput)
		JULLoggingBridge.install();
	}
	
	
	/**
	 * Creates the model of the application and redirects to a presenter.
	 * 
	 * @param args
	 */
	public static void main(final String[] args)
	{
		EventQueue.invokeLater(MainPresenter::new);
	}
}
