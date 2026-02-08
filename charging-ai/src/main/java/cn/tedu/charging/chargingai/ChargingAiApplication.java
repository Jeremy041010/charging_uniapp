package cn.tedu.charging.chargingai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ChargingAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargingAiApplication.class, args);
    }
}