package com.jewelry.workshop.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.security.password")
public class PasswordPolicyProperties {
    private int minLength = 6;
    private int clientChangeMinLength = 8;
}
