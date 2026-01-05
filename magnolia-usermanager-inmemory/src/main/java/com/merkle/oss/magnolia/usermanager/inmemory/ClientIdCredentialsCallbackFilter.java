package com.merkle.oss.magnolia.usermanager.inmemory;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;

import java.io.IOException;
import java.util.Arrays;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class ClientIdCredentialsCallbackFilter extends CredentialsCallbackHandler {
	private final CredentialsCallbackHandler wrapped;
	private final String clientId;

	public ClientIdCredentialsCallbackFilter(final CredentialsCallbackHandler wrapped, final String clientId) {
		this.wrapped = wrapped;
		this.clientId = clientId;
	}

	@Override
	public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		Arrays.stream(callbacks).forEach(callback -> {
			if (callback instanceof final ClientIdCallback clientIdCallback) {
				clientIdCallback.setClientId(clientId);
			}
		});
		wrapped.handle(callbacks);
	}

	@Override
	public String getName() {
		return wrapped.getName();
	}

	@Override
	public boolean passwordIsBlank() {
		return wrapped.passwordIsBlank();
	}
}
