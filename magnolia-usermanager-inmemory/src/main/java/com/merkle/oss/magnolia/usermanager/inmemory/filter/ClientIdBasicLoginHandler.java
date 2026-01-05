package com.merkle.oss.magnolia.usermanager.inmemory.filter;

import info.magnolia.cms.security.auth.callback.CredentialsCallbackHandler;
import info.magnolia.cms.security.auth.login.BasicLogin;
import info.magnolia.cms.security.auth.login.LoginResult;

import com.merkle.oss.magnolia.usermanager.inmemory.ClientIdCredentialsCallbackFilter;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ClientIdBasicLoginHandler extends BasicLogin {
	@Nullable
	private String clientId;

	@Override
	public LoginResult handle(final HttpServletRequest request, final HttpServletResponse response) {
		clientId = request.getRemoteAddr();
		return super.handle(request, response);
	}

	@Override
	protected LoginResult authenticate(final CredentialsCallbackHandler callbackHandler, final String jaasModuleName) {
		return super.authenticate(new ClientIdCredentialsCallbackFilter(callbackHandler, clientId), jaasModuleName);
	}
}
