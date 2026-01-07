# InMemory user manager
In memory user manager (useful for technical users).

## Requirements
* Java 17
* Magnolia = 6.3

## Installation

* Add Maven dependency:
    ```xml
    <dependency>
        <groupId>com.merkle.oss.magnolia</groupId>
        <artifactId>magnolia-usermanager-inmemory</artifactId>
        <version>0.0.1Magnolia6_3-SNAPSHOT</version>
    </dependency>
    ```
* Configure InMemoryAndJcrAuthenticationModule in [jaas config](https://docs.magnolia-cms.com/product-docs/administration/security/jaas-security-setup/) (WEB-INF/config/jaas.config)
    ```
    magnolia {
      com.merkle.oss.magnolia.usermanager.inmemory.InMemoryAndJcrAuthenticationModule requisite;
      info.magnolia.jaas.sp.jcr.JCRAuthorizationModule required;
    };
    ```
* Configure magnolia module dependency
  ```xml
  <dependency>
      <name>magnolia-usermanager-inmemory</name>
      <version>*/*</version>
  </dependency>
  ```

## Configuration
### Automatic lockout
An automatic lockout is a security precaution that prevents users from accessing Magnolia after a number of failed login attempts.

By default, a lockout is triggered after 3 failed login attempts from the same ip, which blocks further requests from that ip for 5 minutes.
The number of failed attempts can be configured. No lockout is triggered when a username that doesn’t exist is entered.

`/server/security/userManagers/in-memory` - default values:
```properties
lockTimePeriod=5
maxFailedLoginAttempts=3
```

### Create user

Can be used in combination with [magnolia-setup-task](https://github.com/merkle-open/magnolia-setup-task) to setup users on module startup. 
```java
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.UserManager;

void main() {
  final UserManager userManager = SecuritySupport.Factory.getInstance().getUserManager(InMemoryUserManager.REALM.getName());
  userManager.createUser("name", "password");
}
```
