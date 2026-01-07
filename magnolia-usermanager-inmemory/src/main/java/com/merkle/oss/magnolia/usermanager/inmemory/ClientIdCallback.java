package com.merkle.oss.magnolia.usermanager.inmemory;


import java.io.Serializable;
import java.util.Optional;

import javax.security.auth.callback.Callback;

import javax.annotation.Nullable;

public class ClientIdCallback implements Callback, Serializable {
	@Nullable
	private String clientId;

	public void setClientId(final String clientId) {
		this.clientId = clientId;
	}

	public Optional<String> getClientId() {
		return Optional.ofNullable(clientId);
	}
}
