package com.merkle.oss.magnolia.property;

import info.magnolia.init.DefaultMagnoliaConfigurationProperties;
import info.magnolia.init.MagnoliaInitPaths;
import info.magnolia.init.MagnoliaPropertiesResolver;
import info.magnolia.init.PropertySource;
import info.magnolia.init.properties.EnvironmentPropertySource;
import info.magnolia.init.properties.SystemPropertySource;
import info.magnolia.module.ModuleRegistry;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.merkle.oss.magnolia.property.util.Lazy;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
public class EncryptedMagnoliaConfigurationProperties extends DefaultMagnoliaConfigurationProperties {
    public static final String ENV_PASSWORD = "master.password";

    private final Provider<StringEncryptor> stringEncryptor;

    @Inject
    public EncryptedMagnoliaConfigurationProperties(
            final MagnoliaInitPaths initPaths,
            final ModuleRegistry moduleRegistry,
            final MagnoliaPropertiesResolver resolver,
            final SystemPropertySource systemPropertySource,
            final EnvironmentPropertySource environmentPropertySource) {
        super(initPaths, moduleRegistry, resolver, systemPropertySource, environmentPropertySource);
        stringEncryptor = Lazy.of(() -> new StringEncryptor(super.getProperty(EncryptedMagnoliaConfigurationProperties.ENV_PASSWORD)))::get;
    }

    @Override
    public PropertySource getPropertySource(final String key) {
        return Optional
                .ofNullable(super.getPropertySource(key))
                .map(propertySource ->
                        new EncryptedPropertySource(stringEncryptor, propertySource)
                )
                .orElse(null);
    }

    public static class EncryptedPropertySource implements PropertySource {
        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        private final Provider<StringEncryptor> stringEncryptor;
        private final PropertySource propertySource;

        public EncryptedPropertySource(
                final Provider<StringEncryptor> stringEncryptor,
                final PropertySource propertySource
        ) {
            this.stringEncryptor = stringEncryptor;
            this.propertySource = propertySource;
        }

        @Override
        public Set<String> getKeys() {
            return propertySource.getKeys();
        }

        @Override
        public String getProperty(final String key) {
            final String property = propertySource.getProperty(key);
            try {
                if (StringEncryptor.isEncryptedValue(property)) {
                    return stringEncryptor.get().decrypt(property);
                }
            } catch (Exception e) {
                LOG.error("Failed to decrypt magnolia property {}! Returning encrypted property...", key, e);
            }
            return property;
        }

        @Override
        public boolean getBooleanProperty(final String key) {
            return Boolean.parseBoolean(getProperty(key));
        }

        @Override
        public boolean hasProperty(final String key) {
            return propertySource.hasProperty(key);
        }

        @Override
        public String describe() {
            return propertySource.describe();
        }
    }
}
