package com.starservice.inventory.inventory_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Configuration
@Getter
public class SecurityProperties {

    private final List<String> permitAll;

    public SecurityProperties(@Value("${security.permit-all}") String permitAllStr) {
        this.permitAll = Arrays.asList(permitAllStr.split(","));
    }
}