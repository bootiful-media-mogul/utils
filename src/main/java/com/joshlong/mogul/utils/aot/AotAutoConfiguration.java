package com.joshlong.mogul.utils.aot;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.util.ClassUtils;

import javax.net.ssl.SSLParameters;
import java.util.EventObject;
import java.util.List;

/**
 * GraalVM hints that aren't specific to any one mogul service.
 */
@AutoConfiguration
@ImportRuntimeHints(AotAutoConfiguration.Hints.class)
class AotAutoConfiguration {

	static class Hints implements RuntimeHintsRegistrar {

		private static final String PROTOBUF_EXTENSION_REGISTRY = "com.google.protobuf.ExtensionRegistry";

		@Override
		public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
			var mcs = MemberCategory.values();

			for (var c : new Class<?>[] { ApplicationEvent.class, EventObject.class })
				hints.reflection().registerType(c, mcs);

			// apache httpcore5 configures ALPN on SSLParameters reflectively
			hints.reflection().registerType(TypeReference.of(SSLParameters.class), mcs);

			if (ClassUtils.isPresent(PROTOBUF_EXTENSION_REGISTRY, classLoader))
				hints.reflection()
					.registerType(TypeReference.of(PROTOBUF_EXTENSION_REGISTRY),
							type -> type.withMethod("getEmptyRegistry", List.of(), ExecutableMode.INVOKE));
		}

	}

}
