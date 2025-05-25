package com.buzznote.buzznote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BuzznoteApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        String dbUrl = dotenv.get("DB_URL");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");
        String secretKey = dotenv.get("SECRET_KEY");

        System.setProperty("DB_URL", dbUrl);
        System.setProperty("DB_USER", dbUser);
        System.setProperty("DB_PASSWORD", dbPassword);
        System.setProperty("SECRET_KEY", secretKey);

        SpringApplication.run(BuzznoteApplication.class, args);
    }

}
