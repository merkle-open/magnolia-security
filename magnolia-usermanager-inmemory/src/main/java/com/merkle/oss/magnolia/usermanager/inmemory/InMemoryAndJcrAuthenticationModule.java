package com.merkle.oss.magnolia.usermanager.inmemory;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.jaas.sp.jcr.JCRAuthenticationModule;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import jakarta.annotation.Nullable;

public class InMemoryAndJcrAuthenticationModule extends JCRAuthenticationModule {
	@Nullable
	private String clientId;

	@Override
	public void initialize(final Subject subject, final CallbackHandler callbackHandler, final Map sharedState, final Map options) {
		super.initialize(
				subject,
				originalCallbacks -> {
					final Callback[] callbacks = Stream.concat(
							Arrays.stream(originalCallbacks),
							Stream.of(new ClientIdCallback())
					).toArray(Callback[]::new);
					final int offset = originalCallbacks.length;
					callbackHandler.handle(callbacks);
					this.clientId = ((ClientIdCallback) callbacks[offset]).getClientId().orElse(null);
				},
				sharedState,
				options
		);
	}

	@Override
	protected void initUser() throws LoginException {
		super.initUser();
		if(getUser() instanceof InMemoryUser) {
			super.realm = InMemoryUserManager.REALM;
		}
	}

	@Override
	protected void matchPassword() throws LoginException {
		if (getUser() instanceof final InMemoryUser inMemoryUser) {
			if (clientId == null) {
				throw new FailedLoginException("clientId not present!");
			}
			final Map<String, SortedSet<Instant>> failedLoginAttemptsPerClientId = getFilteredFailedLoginAttemptsPerClient(inMemoryUser);
			getUserManager().updateUser(inMemoryUser.withFailedLoginAttempts(failedLoginAttemptsPerClientId));

			final SortedSet<Instant> failedLoginAttempts = failedLoginAttemptsPerClientId.getOrDefault(clientId, Collections.emptySortedSet());
			if (!Objects.equals(new String(this.pswd), inMemoryUser.getPassword())) {
				getUserManager().updateUser(inMemoryUser.withFailedLoginAttempts(clientId, Stream.concat(
						failedLoginAttempts.stream(),
						Stream.of(Instant.now())
				).collect(Collectors.toCollection(TreeSet::new))));
				throw new FailedLoginException("Passwords do not match");
			}
			if (failedLoginAttempts.size() >= getMaxAttempts()) {
				throw new FailedLoginException("client was locked due to high number of failed login attempts! id:" + clientId);
			}
			getUserManager().updateUser(inMemoryUser.withFailedLoginAttempts(clientId, Collections.emptySortedSet()));
		} else {
			super.matchPassword();
		}
	}

	private Map<String, SortedSet<Instant>> getFilteredFailedLoginAttemptsPerClient(final InMemoryUser inMemoryUser) {
		return inMemoryUser.getFailedLoginAttempts().entrySet().stream()
				.filter(entry -> !shouldUnlock(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private boolean shouldUnlock(final Set<Instant> failedLoginAttempts) {
		return failedLoginAttempts.stream().max(Comparator.naturalOrder()).map(lastFailedLoginAttempt ->
				Instant.now().isAfter(lastFailedLoginAttempt.plus(Duration.ofMinutes(getTimeLock())))
		).orElse(false);
	}

	@Override
	public long getTimeLock() {
		return getUserManager().getLockTimePeriod();
	}

	@Override
	public int getMaxAttempts() {
		return getUserManager().getMaxFailedLoginAttempts();
	}

	private InMemoryUserManager getUserManager() {
		return (InMemoryUserManager) SecuritySupport.Factory.getInstance().getUserManager(InMemoryUserManager.REALM.getName());
	}
}
