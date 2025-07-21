/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 10, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package com.github.g3force.configurable;

import com.github.g3force.configurable.ConfigClass1.ETest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ConfigurableTest
{
	private static final Logger log = LogManager.getLogger(ConfigurableTest.class.getName());

	private static final String CATEGORY = "default";

	private final Path configFilePath = Path.of("config").resolve(CATEGORY + ".xml");


	@AfterEach
	void after()
	{
		deleteConfigFile();
	}


	private void deleteConfigFile()
	{
		try
		{
			Path p = Paths.get("config", CATEGORY + ".xml");
			if (p.toFile().exists())
			{
				Files.delete(p);
			}
		} catch (IOException e)
		{
			log.error("", e);
		}
	}


	@Test
	void testSave()
	{
		// save current config
		ConfigRegistration.save(CATEGORY);
		// the config file will only contain values that differ from their default value
		assertTrue(configFilePath.toFile().exists());
	}


	@Test
	void testChangeValue()
	{
		assertFalse(ConfigClass1.testBool);

		// change a value
		ConfigClass1.testBool = true;
		// reread the value from all classes into internal config
		ConfigRegistration.readClasses(CATEGORY);

		// change value back
		ConfigClass1.testBool = false;
		ConfigClass1.testEnum = ETest.TWO;

		// apply internal config
		ConfigRegistration.applyConfig(CATEGORY);
		// value should be changed back to true
		assertTrue(ConfigClass1.testBool);
		assertSame(ETest.ONE, ConfigClass1.testEnum);

		// save internal config to file
		ConfigRegistration.save(CATEGORY);

		// config file should exist and contain testbool
		assertTrue(configFilePath.toFile().exists());
	}


	@Test
	void testSpezi()
	{
		// default value for fields with spezi is the empty spezi
		assertEquals(1, ConfigClass2.testSpezi, 0.0001);

		ConfigRegistration.applySpezi(CATEGORY, "CONF1");
		assertEquals(2, ConfigClass2.testSpezi, 0.0);
		ConfigRegistration.applySpezi(CATEGORY, "CONF2");
		assertEquals(3, ConfigClass2.testSpezi, 0.0);
		ConfigRegistration.applySpezi(CATEGORY, "");
		assertEquals(1, ConfigClass2.testSpezi, 0.0);
	}


	@Test
	void testInstance()
	{
		ConfigClass3 cc = new ConfigClass3();
		assertEquals(2, cc.testSpezi, 0.0001);
	}


	@Test
	void testCallback()
	{
		ConfigRegistration.registerConfigurableCallback(CATEGORY, new IConfigObserver()
		{
			@Override
			public void afterApply(final IConfigClient configClient)
			{
				ConfigClass1.testDouble = 10;
			}
		});

		ConfigClass1.testDouble = -1;

		ConfigRegistration.applyConfig(CATEGORY);

		assertEquals(10, ConfigClass1.testDouble, 0.0001);
	}


	@Test
	void testOverride()
	{
		ConfigClass1.testDouble = 1;
		ConfigRegistration.overrideConfig(ConfigClass1.class, CATEGORY, "testDouble", "42");
		assertEquals(42.0, ConfigClass1.testDouble, 1e-10);
	}


	@Test
	void testDefValue()
	{
		ConfigAnnotationProcessor cap = new ConfigAnnotationProcessor("read");
		cap.loadClass(ConfigClass4.class, false);
		assertThat(ConfigClass4.testBoolFalse).isFalse();
		assertThat(ConfigClass4.testBoolTrue).isTrue();
		assertThat(ConfigClass4.testEnum.name()).isEqualTo("ONE");
		assertThat(ConfigClass4.testEnumDefValue.name()).isEqualTo("TWO");
		assertThat(ConfigClass4.testDouble).isEqualTo(1.0, within(1e-10));
		assertThat(ConfigClass4.testDoubleWithDefault).isEqualTo(2.0, within(1e-10));
		assertThat(ConfigClass4.testDefaultDifferent).isEqualTo(6.0);
		assertThat(ConfigClass4.testStoredDifferent).isEqualTo(42.0, within(1e-10));
	}
}
