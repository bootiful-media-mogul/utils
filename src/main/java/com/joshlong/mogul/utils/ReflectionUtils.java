package com.joshlong.mogul.utils;

import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;

public abstract class ReflectionUtils {

	public static Class<?> classForName(String name) {
		var log = LoggerFactory.getLogger(ReflectionUtils.class);
		try {
			Assert.hasText(name, "you must provide a non-empty class name");
			// the context classloader, not this class's own: under spring boot devtools
			// the application's classes live in a restart classloader and this library
			// doesn't, so Class.forName(name) from here loads a second copy of, say,
			// Segment that equals nothing the application has -- and every lookup keyed
			// on it fails. ClassUtils falls back sensibly when there's no context loader.
			return ClassUtils.forName(name, ClassUtils.getDefaultClassLoader());
		}
		catch (ClassNotFoundException e) {
			log.warn("classNotFoundException when trying to do Class.forName({}) to resolve the class for a {} ", name,
					(name), e);
		}
		return null;
	}

	public static Set<Class<?>> genericsFor(Class<?> clzz) {
		var classes = new HashSet<Class<?>>();
		genericsFor(clzz, classes);
		return classes;
	}

	private static void genericsFor(Class<?> clzz, Set<Class<?>> classes) {
		var resolvableType = ResolvableType.forClass(clzz);

		// Get direct generics of this class
		for (var generic : resolvableType.getGenerics()) {
			if (generic.resolve() != null) {
				classes.add(generic.resolve());
				// Recurse into the generic's type parameters
				genericsFor(generic.resolve(), classes);
			}
		}

		// Get generics from all implemented interfaces
		for (var iface : resolvableType.getInterfaces()) {
			for (var generic : iface.getGenerics()) {
				if (generic.resolve() != null) {
					classes.add(generic.resolve());
					genericsFor(generic.resolve(), classes);
				}
			}
		}

		// Get generics from superclass
		var superType = resolvableType.getSuperType();
		if (!superType.equals(ResolvableType.NONE)) {
			for (var generic : superType.getGenerics()) {
				if (generic.resolve() != null) {
					classes.add(generic.resolve());
					genericsFor(generic.resolve(), classes);
				}
			}
		}
	}

}
