/*
 * Copyright (c) 2013, Swedish Institute of Computer Science
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of The Swedish Institute of Computer Science nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SWEDISH INSTITUTE OF COMPUTER SCIENCE BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

/* Description:
 * TODO:
 * */
package se.sics.sicsthsense.auth;

import com.google.common.base.Optional;

import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.sicsthsense.core.*;
import se.sics.sicsthsense.jdbi.*;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {
		private final StorageDAO storage;
		private final Logger logger = LoggerFactory.getLogger(SimpleAuthenticator.class);

		public SimpleAuthenticator(StorageDAO storage) {
			this.storage = storage;
		}	

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
			logger.info("Authenticating user "+credentials.getUsername());
			User user = storage.findUserByUsername(credentials.getUsername());
			if (user != null) {
				//if ("secret".equals(credentials.getPassword())) {
				if (user.hasPassword(credentials.getPassword())) {
					return Optional.of(user);
				} else {
					logger.error("User "+credentials.getUsername()+" wrong password!");
				}
			} else {
				logger.error("User "+credentials.getUsername()+" doesnt exist!");
			}
			return Optional.absent();
    }
}
