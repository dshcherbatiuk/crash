/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.crsh.plugin;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import org.crsh.vfs.Resource;
import org.slf4j.Logger;

/**
 * The base class for managing the CRaSH life cycle.
 */
public abstract class PluginLifeCycle {

  private final Logger LOGGER = getLogger(getClass().getName());

  private PluginContext context;

  private Properties config;

  public Properties getConfig() {
    return config;
  }

  public void setConfig(Properties config) {
    this.config = config;
  }

  public PluginContext getContext() {
    return context;
  }

  protected final void start(PluginContext context) throws IllegalStateException {
    if (this.context != null) {
      throw new IllegalStateException("Already started");
    }

    final Properties config = loadProperties(context);

    // Override default properties from plugin defined properties.
    for (final CRaSHPlugin<?> plugin : context.manager.getPlugins()) {
      Iterable<PropertyDescriptor<?>> capabilities = plugin.getConfigurationCapabilities();
      Iterator<PropertyDescriptor<?>> i = capabilities.iterator();
      if (i.hasNext()) {
        while (i.hasNext()) {
          PropertyDescriptor<?> descriptor = i.next();
          LOGGER.debug("Adding plugin {} property {}", plugin, descriptor.getName());
          configureProperty(context, config, descriptor);
        }
      } else {
        LOGGER.debug("Plugin {} does not declare any configuration property", plugin);
      }
    }

    context.start();

    this.context = context;
  }

  private Properties loadProperties(PluginContext context) {
    // Get properties from system properties
    Properties config = new Properties();

    // Load properties from configuration file
    Resource res = context.loadResource("crash.properties", ResourceKind.CONFIG);
    if (res != null) {
      try {
        config.load(new ByteArrayInputStream(res.getContent()));
        LOGGER.debug("Loaded properties from {}", config);
      } catch (IOException e) {
        LOGGER.warn("Could not configure from crash.properties", e);
      }
    } else {
      LOGGER.debug("Could not find crash.properties file");
    }

    // Override default properties from external config
    if (this.config != null) {
      config.putAll(this.config);
    }

    // Override default properties from command line
    for (PropertyDescriptor<?> desc : PropertyDescriptor.ALL.values()) {
      configureProperty(context, config, desc);
    }
    return config;
  }

  public final void stop() throws IllegalStateException {
    if (context == null) {
      throw new IllegalStateException("Not started");
    }
    PluginContext context = this.context;
    this.context = null;
    context.stop();
  }

  private void configureProperty(
      PluginContext context, Properties props, PropertyDescriptor<?> desc) {
    String key = "crash." + desc.name;
    String value = props.getProperty(key);
    if (value != null) {
      try {
        if (context.getProperty(desc) == null) {
          LOGGER.info("Configuring property {}={} from properties", desc.name, value);
          context.setProperty(desc, value);
        }
      } catch (IllegalArgumentException e) {
        LOGGER.error("Could not configure property", e);
      }
    }
  }
}
