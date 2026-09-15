# Encrypted magnolia properties
Allows to encrypt secrets in magnolia properties with a master password.

## Requirements
* Java 17
* Magnolia >= 6.4

## Installation

* Add Maven dependency:
  ```xml
  <dependency>
      <groupId>com.merkle.oss.magnolia</groupId>
      <artifactId>magnolia-encrypted-properties</artifactId>
      <version>0.0.1-SNAPSHOT</version>
  </dependency>
  ```
* Bind EncryptedMagnoliaConfigurationProperties
  ```xml
  <components>
      <id>system</id>
      <component>
          <type>info.magnolia.init.MagnoliaConfigurationProperties</type>
          <implementation>com.merkle.oss.magnolia.property.EncryptedMagnoliaConfigurationProperties</implementation>
      </component>
  </components>
  ```
* Pass `master.password` as CATALINA_OPTS
  ```
  -Dmaster.password='SomeMasterPassword'
  ```

## Usage
To encrypt new properties implement something like this:
```java
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.merkle.oss.magnolia.property.StringEncryptor;
import com.merkle.oss.magnolia.property.util.PropertiesEncryptor;

class PropertiesEncryptorTest {
    private final StringEncryptor encryptor = new StringEncryptor("someMasterPassword");
    private final StringEncryptor reEncryptor = new StringEncryptor("newMasterPassword");
    private final Path propertiesPath = Paths.get("src/main/webapp/WEB-INF/config/dev/magnolia.properties");

    @Disabled
    @Test
    void encryptProperty() {
        final String valueToEncrypt = "secretValue";
        System.out.println("value: " + encryptor.encrypt(valueToEncrypt));
    }

    @Disabled
    @Test
    void decryptProperty() {
        final String valueToDecrypt = "ENC(TbwtZXSXP2E0rwB3LSQa3CnJl7iEjf5ODfTD5ADHtt4=)";
        System.out.println("value: " + encryptor.decrypt(valueToDecrypt));
    }

    @Disabled
    @Test
    void decryptProperties() throws IOException {
        new PropertiesEncryptor().decryptProperties(encryptor, propertiesPath);
    }

    @Disabled
    @Test
    void reEncryptProperties() throws IOException {
        new PropertiesEncryptor().reEncryptProperties(encryptor, reEncryptor, propertiesPath);
    }
}
```
