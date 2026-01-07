package com.merkle.oss.magnolia.usermanager.inmemory;

import info.magnolia.cms.security.User;

import java.time.Instant;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class InMemoryUser implements User {
	private final Map<String, String> properties;
	private final Set<String> groups;
	private final Set<String> allGroups;
	private final Set<String> roles;
	private final String name;
	private final String password;
	private final Set<String> allRoles;
	@Nullable
	private final Instant lastAccessTimestamp;
	@Nullable
	private final Instant previousAccessTimestamp;
	private final boolean disabled;
	private final Map<String, SortedSet<Instant>> failedLoginAttemptsPerClientId;

	public InMemoryUser(
		final String name,
		final String password
	) {
		this(name, password, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptyMap(), null, null, false, Collections.emptyMap());
	}
	public InMemoryUser(
		final String name,
		final String password,
		final Set<String> groups,
		final Set<String> allGroups,
		final Set<String> roles,
		final Set<String> allRoles,
		final Map<String, String> properties,
		@Nullable final Instant lastAccessTimestamp,
		@Nullable final Instant previousAccessTimestamp,
		final boolean disabled,
		final Map<String, SortedSet<Instant>> failedLoginAttemptsPerClientId
	) {
		this.name = name;
		this.password = password;
		this.groups = groups;
		this.allGroups = allGroups;
		this.roles = roles;
		this.allRoles = allRoles;
		this.properties = properties;
		this.lastAccessTimestamp = lastAccessTimestamp;
		this.previousAccessTimestamp = previousAccessTimestamp;
		this.disabled = disabled;
		this.failedLoginAttemptsPerClientId = failedLoginAttemptsPerClientId;
	}

	public InMemoryUser withGroups(final Set<String> groups, final Set<String> allGroups) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withRoles(final Set<String> roles, final Set<String> allRoles) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withProperties(final Map<String, String> properties) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withLastAccessTimestamp(final Instant lastAccessTimestamp) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withPreviousAccessTimestamp(final Instant previousAccessTimestamp) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withDisabled(final boolean disabled) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withFailedLoginAttempts(final Map<String, SortedSet<Instant>> failedLoginAttemptsPerClientId) {
		return new InMemoryUser(name, password, groups, allGroups, roles, allRoles, properties, lastAccessTimestamp, previousAccessTimestamp, disabled, failedLoginAttemptsPerClientId);
	}
	public InMemoryUser withFailedLoginAttempts(final String clientId, final SortedSet<Instant> failedLoginAttempts) {
		return withFailedLoginAttempts(
			Stream.concat(
				failedLoginAttemptsPerClientId.entrySet().stream(),
				Stream.of(Map.entry(clientId, failedLoginAttempts))
			).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2))
		);
	}

	@Override
	public boolean hasRole(final String roleName) {
		return roles.contains(roleName);
	}

	@Override
	public boolean inGroup(final String groupName) {
		return groups.contains(groupName);
	}

	@Override
	public boolean isEnabled() {
		return !disabled;
	}

	@Override
	public String getLanguage() {
		return Locale.ENGLISH.getLanguage();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getProperty(final String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public void setProperty(final String propertyName, final String value) {
		throw new UnsupportedOperationException();
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String getIdentifier() {
		return getName();
	}

	@Override
	public Set<String> getGroups() {
		return groups;
	}

	@Override
	public Set<String> getAllGroups() {
		return allGroups;
	}

	@Override
	public Set<String> getRoles() {
		return roles;
	}

	@Override
	public Set<String> getAllRoles() {
		return allRoles;
	}

	public Map<String, SortedSet<Instant>> getFailedLoginAttempts() {
		return failedLoginAttemptsPerClientId;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof InMemoryUser that)) {return false;}
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return "MagnoliaPropertiesUser{" +
			"properties=" + properties +
			", groups=" + groups +
			", allGroups=" + allGroups +
			", roles=" + roles +
			", name='" + name + '\'' +
			", password='<redacted>'" +
			", allRoles=" + allRoles +
			'}';
	}
}
