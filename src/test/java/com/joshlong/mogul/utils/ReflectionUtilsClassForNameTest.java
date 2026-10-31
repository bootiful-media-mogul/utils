package com.joshlong.mogul.utils;

import org.junit.jupiter.api.Test;

import java.net.URLClassLoader;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * spring boot devtools loads the application's own classes in a restart classloader,
 * while libraries like this one stay in the base one. a class name read back out of the
 * database has to become the application's class, the one everything else compares
 * against -- not a second copy of it from the base classloader, which equals nothing.
 * that second copy is what made every transcription fail with "no repository found for
 * ...Segment" under devtools.
 */
class ReflectionUtilsClassForNameTest {

	/** loaded twice: once normally, once more by a classloader of its own */
	public static class Payload {

	}

	@Test
	void resolvesThroughTheContextClassLoaderLikeTheRestOfTheApplication() throws Exception {
		var classes = Path.of(Payload.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		var current = Thread.currentThread().getContextClassLoader();
		// no parent: everything it loads, it loads itself, like devtools' restart loader
		try (var restart = new URLClassLoader(new java.net.URL[] { classes.toUri().toURL() }, null)) {
			var applications = restart.loadClass(Payload.class.getName());
			assertThat(applications).isNotEqualTo(Payload.class);
			Thread.currentThread().setContextClassLoader(restart);
			try {
				assertThat(ReflectionUtils.classForName(Payload.class.getName())).isSameAs(applications);
			} //
			finally {
				Thread.currentThread().setContextClassLoader(current);
			}
		}
	}

	@Test
	void resolvesNormallyOtherwise() {
		assertThat(ReflectionUtils.classForName(Payload.class.getName())).isSameAs(Payload.class);
		assertThat(ReflectionUtils.classForName("no.such.Thing")).isNull();
	}

}
