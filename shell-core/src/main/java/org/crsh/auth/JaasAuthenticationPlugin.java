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

package org.crsh.auth;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.auto.service.AutoService;
import java.util.Collections;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import org.crsh.plugin.CRaSHPlugin;
import org.crsh.plugin.PropertyDescriptor;
import org.slf4j.Logger;

@AutoService(CRaSHPlugin.class)
public class JaasAuthenticationPlugin extends CRaSHPlugin<AuthenticationPlugin>
    implements AuthenticationPlugin<String> {

  private static final Logger LOGGER = getLogger(JaasAuthenticationPlugin.class.getName());

  static final PropertyDescriptor<String> JAAS_DOMAIN =
      PropertyDescriptor.create(
          "auth.jaas.domain", (String) null, "The JAAS domain name used for authentication");

  public String getName() {
    return "jaas";
  }

  @Override
  protected Iterable<PropertyDescriptor<?>> createConfigurationCapabilities() {
    return Collections.<PropertyDescriptor<?>>singletonList(JAAS_DOMAIN);
  }

  public Class<String> getCredentialType() {
    return String.class;
  }

  public AuthInfo authenticate(final String username, final String password) throws Exception {
    String domain = getContext().getProperty(JAAS_DOMAIN);
    if (domain != null) {
      LOGGER.debug("Will use the JAAS domain '{}' for authenticating user {}", domain, username);
      LoginContext loginContext =
          new LoginContext(
              domain,
              new Subject(),
              callbacks -> {
                for (Callback c : callbacks) {
                  if (c instanceof NameCallback) {
                    ((NameCallback) c).setName(username);
                  } else if (c instanceof PasswordCallback) {
                    ((PasswordCallback) c).setPassword(password.toCharArray());
                  } else {
                    throw new UnsupportedCallbackException(c);
                  }
                }
              });

      try {
        loginContext.login();
        loginContext.logout();
        LOGGER.debug("Authenticated user {} against the JAAS domain '{}'", username, domain);
        return AuthInfo.SUCCESSFUL;
      } catch (Exception e) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("Exception when authenticating user {} to JAAS domain '{}'",
              username, domain, e);
        }
        return AuthInfo.UNSUCCESSFUL;
      }
    } else {
      LOGGER.warn("The JAAS domain property '{}' was not found", JAAS_DOMAIN.name);
      return AuthInfo.UNSUCCESSFUL;
    }
  }

  @Override
  public AuthenticationPlugin getImplementation() {
    return this;
  }
}
