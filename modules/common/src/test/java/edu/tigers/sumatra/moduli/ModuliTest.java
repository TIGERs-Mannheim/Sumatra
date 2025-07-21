/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.moduli;

import edu.tigers.sumatra.moduli.exceptions.DependencyException;
import edu.tigers.sumatra.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.moduli.modules.ConcreteTestModule;
import edu.tigers.sumatra.moduli.modules.ConfiguredTestModule;
import edu.tigers.sumatra.moduli.modules.TestModule;
import edu.tigers.sumatra.moduli.modules.UnusedConcreteTestModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ModuliTest
{
	private static final String MODULE_CONFIG_PATH = "src/test/resources/";
	private static final String TEST_CONFIG_XML = "test_config.xml";
	private static final String EMPTY_CONFIG_XML = "empty_config.xml";
	private static final String CYCLIC_CONFIG_XML = "cyclic_config.xml";
	private static final String UNRESOLVED_DEPENDENCY_CONFIG_XML = "unresolved_dependency_config.xml";

	private Moduli moduli;


	@BeforeEach
	void setUp()
	{
		moduli = new Moduli();
	}


	@AfterEach
	void tearDown()
	{
		moduli = null;
	}


	@Test
	void testModuliCycle()
	{
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.NOT_LOADED);

		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);

		assertThat(moduli.isModuleLoaded(TestModule.class)).isTrue();
		assertThat(moduli.isModuleLoaded(ConcreteTestModule.class)).isTrue();
		assertThat(moduli.isModuleLoaded(UnusedConcreteTestModule.class)).isFalse();

		assertThat(moduli.getModule(TestModule.class).isConstructed()).isTrue();
		assertThat(moduli.getModule(ConcreteTestModule.class).isConstructed()).isTrue();

		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);

		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}


	@Test
	void testModuleCycle()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		TestModule module = moduli.getModule(TestModule.class);
		assertThat(module.isConstructed()).isTrue();

		moduli.startModules();
		assertThat(module.isInitialized()).isTrue();
		assertThat(module.isStarted()).isTrue();

		moduli.stopModules();
		assertThat(module.isStopped()).isTrue();
		assertThat(module.isDeinitialized()).isTrue();
	}


	@Test
	void testEmptyConfig()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + EMPTY_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
		assertThat(moduli.isModuleLoaded(TestModule.class)).isFalse();

		moduli.startModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.ACTIVE);

		moduli.stopModules();
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);
	}


	@Test
	void testGlobalConfiguration()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + EMPTY_CONFIG_XML);
		String env = moduli.getGlobalConfiguration().getString("environment");
		assertThat(env).isEqualTo("MODULI");
	}


	@Test
	void testModuleConfiguration()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		moduli.startModules();
		ConfiguredTestModule module = moduli.getModule(ConfiguredTestModule.class);
		assertThat(module.getConfigProperty()).isEqualTo("exists");
	}


	@Test
	void testCyclicConfiguration()
	{
		assertThatThrownBy(() -> moduli.loadModules(MODULE_CONFIG_PATH + CYCLIC_CONFIG_XML))
				.isInstanceOf(DependencyException.class);
	}


	@Test
	void testUnresolvedDependencyConfiguration()
	{
		assertThatThrownBy(() -> moduli.loadModules(MODULE_CONFIG_PATH + UNRESOLVED_DEPENDENCY_CONFIG_XML))
				.isInstanceOf(DependencyException.class);
	}


	@Test
	void testModuleNotFound()
	{
		moduli.loadModulesSafe(MODULE_CONFIG_PATH + TEST_CONFIG_XML);
		assertThat(moduli.getModulesState().get()).isEqualTo(ModulesState.RESOLVED);

		assertThat(moduli.isModuleLoaded(TestModule.class)).isTrue();

		assertThatThrownBy(() -> moduli.getModule(UnusedConcreteTestModule.class))
				.isInstanceOf(ModuleNotFoundException.class);
	}
}
