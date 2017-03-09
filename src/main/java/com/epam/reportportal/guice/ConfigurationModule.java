package com.epam.reportportal.guice;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.utils.properties.ListenerProperty;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

/**
 * Configuration Module
 *
 * @author Andrei Varabyeu
 */
public class ConfigurationModule implements Module {
    @Override
    public void configure(Binder binder) {
        Names.bindProperties(binder, PropertiesLoader.getProperties());
        for (final ListenerProperty listenerProperty : ListenerProperty.values()) {
            binder.bind(Key.get(String.class, ListenerPropertyBinder.named(listenerProperty)))
                    .toProvider(new Provider<String>() {
                        @Override
                        public String get() {
                            return PropertiesLoader.getProperty(listenerProperty.getPropertyName());
                        }
                    });
        }
    }

    /**
     * Provides wrapper for report portal properties
     */
    @Provides
    @Singleton
    public ListenerParameters provideListenerProperties() {
        return new ListenerParameters(PropertiesLoader.getProperties());
    }
}
