package com.merkle.oss.magnolia.property.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.merkle.oss.magnolia.property.StringEncryptor;

public class PropertiesEncryptor {
    public void decryptProperties(final StringEncryptor encryptor, final Path propertiesPath) throws IOException {
        final String content = Files.readString(propertiesPath);
        final String decrypted = apply(content, encryptor::decrypt);
        Files.writeString(propertiesPath.resolveSibling(propertiesPath.getFileName().toString().replace(".properties", ".decrypted.properties")), decrypted);
    }

    public void reEncryptProperties(final StringEncryptor encryptor, final StringEncryptor reEncryptor, final Path propertiesPath) throws IOException {
        final String content = Files.readString(propertiesPath);
        final String reencrypted = apply(content, encrypted -> reEncryptor.encrypt(encryptor.decrypt(encrypted)));
        Files.writeString(propertiesPath.resolveSibling(propertiesPath.getFileName().toString().replace(".properties", ".reencrypted.properties")), reencrypted);
    }

    private String apply(final String content, final UnaryOperator<String> operation) {
        final Pattern pattern = Pattern.compile("ENC\\((.*)\\)");
        final Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.replaceAll(matchResult -> operation.apply(matchResult.group()).replaceAll("\\$", "\\\\\\$"));
        }
        return content;
    }
}
