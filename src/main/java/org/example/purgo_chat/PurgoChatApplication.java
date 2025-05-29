package org.example.purgo_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class PurgoChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurgoChatApplication.class, args);
    }

}
