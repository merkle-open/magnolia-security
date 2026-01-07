package com.merkle.oss.magnolia.usermanager.inmemory;

import info.magnolia.cms.security.ACLImpl;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.security.auth.ACL;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Value;
import javax.security.auth.Subject;

import org.apache.commons.lang3.StringUtils;

import com.machinezoo.noexception.Exceptions;

import javax.inject.Inject;
import javax.inject.Singleton;

public class InMemoryUserManager implements UserManager {
	public static final Realm REALM = new Realm.RealmImpl("in-memory");
	private final SecuritySupport securitySupport;
    private final State state;
    private int lockTimePeriod;
	private int maxFailedLoginAttempts;

	@Inject
	public InMemoryUserManager(
			final SecuritySupport securitySupport,
			final State state
	) {
		this.securitySupport = securitySupport;
        this.state = state;
    }

	@Override
	public InMemoryUser getUser(final String name) {
		return state.users.get(name);
	}

	@Override
	public User getUserById(final String id) {
		return getUser(id);
	}

	@Override
	public User getUser(final Subject subject) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * SystemUserManager does this.
	 */
	@Override
	public User getSystemUser() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * SystemUserManager does this.
	 */
	@Override
	public User getAnonymousUser() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<User> getAllUsers() {
		return Set.copyOf(state.users.values());
	}

	@Override
	public User createUser(final String name, final String password) {
		if (StringUtils.isBlank(name)) {
			throw new IllegalArgumentException(name + " is not a valid username.");
		}
		if (StringUtils.isBlank(password)) {
			throw new IllegalArgumentException("password must not be empty.");
		}
		if(securitySupport.getUserManager().getUser(name) != null) {
			throw new IllegalArgumentException("User with name " + name + " already exists.");
		}
		updateUser(new InMemoryUser(name, password));
		return getUser(name);
	}

	@Override
	public User createUser(final String path, final String name, final String password) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public User changePassword(final User user, final String newPassword) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public User setProperty(final User user, final String propertyName, final Value propertyValue)  throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public User setProperty(final User user, final String propertyName, final String propertyValue) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		return inMemoryUser.withProperties(Stream.concat(
			inMemoryUser.getProperties().entrySet().stream(),
			Stream.of(Map.entry(propertyName, propertyValue))
		).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	private InMemoryUser castOrThrow(final User user) {
		if(user instanceof final InMemoryUser inMemoryUser) {
			return inMemoryUser;
		}
		throw new UnsupportedOperationException("User is not an inMemory user - " + user.getClass());
	}

	@Override
	public void setLockTimePeriod(final int lockTimePeriod) {
		this.lockTimePeriod = lockTimePeriod;
	}

	@Override
	public int getLockTimePeriod() {
		return lockTimePeriod;
	}

	@Override
	public void setMaxFailedLoginAttempts(final int maxFailedLoginAttempts) {
		this.maxFailedLoginAttempts = maxFailedLoginAttempts;
	}

	@Override
	public int getMaxFailedLoginAttempts() {
		return maxFailedLoginAttempts;
	}

	@Override
	public User addRole(final User user, final String roleName) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		final Set<String> newRoles = Stream.concat(inMemoryUser.getRoles().stream(), Stream.of(roleName)).collect(Collectors.toSet());
		updateUser(inMemoryUser.withRoles(newRoles, getAllRoles(inMemoryUser.getAllGroups(), newRoles)));
		return getUser(user.getName());
	}

	@Override
	public User removeRole(final User user, final String roleName) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		final Set<String> newRoles = inMemoryUser.getRoles().stream().filter(Predicate.not(roleName::equals)).collect(Collectors.toSet());
		updateUser(inMemoryUser.withRoles(newRoles, getAllRoles(inMemoryUser.getAllGroups(), newRoles)));
		return getUser(user.getName());
	}

	@Override
	public User addGroup(final User user, final String groupName) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		final Set<String> newGroups = Stream.concat(inMemoryUser.getGroups().stream(), Stream.of(groupName)).collect(Collectors.toSet());
		updateUser(inMemoryUser.withRoles(newGroups, getAllGroups(newGroups)));
		return getUser(user.getName());
	}

	@Override
	public User removeGroup(final User user, final String groupName) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		final Set<String> newGroups = inMemoryUser.getGroups().stream().filter(Predicate.not(groupName::equals)).collect(Collectors.toSet());
		updateUser(inMemoryUser.withRoles(newGroups, getAllGroups(newGroups)));
		return getUser(user.getName());
	}

	private Set<String> getAllRoles(final Set<String> allGroups, final Set<String> roles) {
		return Stream.concat(
			roles.stream(),
			allGroups.stream().flatMap(group ->
				Exceptions.wrap().get(() -> securitySupport.getGroupManager().getGroup(group)).getRoles().stream()
			)
		).collect(Collectors.toSet());
	}

	private Set<String> getAllGroups(final Set<String> groups) {
		return groups.stream().flatMap(group ->Stream.concat(
			Stream.of(group),
			securitySupport.getGroupManager().getAllSubGroups(group).stream()
		)).collect(Collectors.toSet());
	}

	@Override
	public void updateLastAccessTimestamp(final User user) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		updateUser(inMemoryUser.withLastAccessTimestamp(Instant.now()));
	}

	@Override
	public void updatePreviousAccessTimestamp(final User user) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		updateUser(inMemoryUser.withPreviousAccessTimestamp(Instant.now()));
	}

	public void updateUser(final User user) {
		final InMemoryUser inMemoryUser = castOrThrow(user);
		state.users.put(user.getName(), inMemoryUser);
	}

	@Override
	public boolean hasAny(final String principal, final String resourceName, final String resourceType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, ACL> getACLs(final User user) {
		return user.getAllRoles().stream()
			.map(securitySupport.getRoleManager()::getACLs)
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				Map.Entry::getValue,
				(acl1, acl2) -> new ACLImpl(acl1.getName(), Stream.concat(
					acl1.getList().stream(),
					acl2.getList().stream()
				).toList())
			));
	}

	@Override
	public Collection<String> getUsersWithGroup(final String groupName) {
		return getUsersWithGroup(groupName, false);
	}

	@Override
	public Collection<String> getUsersWithGroup(final String groupName, final boolean transitive) {
		return getAllUsers().stream()
			.filter(users -> (transitive ? users.getAllGroups() : users.getGroups()).contains(groupName))
			.map(User::getName)
			.collect(Collectors.toSet());
	}

	@Override
	public Collection<String> getUsersWithRole(final String roleName) {
		return getAllUsers().stream()
			.filter(users -> users.getAllRoles().contains(roleName))
			.map(User::getName)
			.collect(Collectors.toSet());
	}

	@Singleton
	public static class State {
		private final Map<String, InMemoryUser> users = new ConcurrentHashMap<>();
	}
}
