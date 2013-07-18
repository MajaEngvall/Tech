package com.sics.sicsthsense.auth.openid;

import com.google.common.base.Optional;

import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;

import com.sics.sicsthsense.core.InMemoryUserCache;
import com.sics.sicsthsense.model.security.OpenIDUser;

/**
 * <p>Authenticator to provide the following to application:</p>
 * <ul>
 * <li>Verifies the provided credentials are valid</li>
 * </ul>
 *
 * @since 0.0.1
 */
public class OpenIDAuthenticator implements Authenticator<OpenIDCredentials, OpenIDUser> {

  @Override
  public Optional<OpenIDUser> authenticate(OpenIDCredentials credentials) throws AuthenticationException {

    // Get the User referred to by the API key
    Optional<OpenIDUser> user = InMemoryUserCache
      .INSTANCE
      .getBySessionToken(credentials.getSessionToken());
    if (!user.isPresent()) {
      return Optional.absent();
    }

    // Check that their authorities match their credentials
    if (!user.get().hasAllAuthorities(credentials.getRequiredAuthorities())) {
      return Optional.absent();
    }

    return user;

  }

}
