package com.bank.itarch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bank.itarch.mapper")
public class BankItArchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankItArchApplication.class, args);
    }
}
