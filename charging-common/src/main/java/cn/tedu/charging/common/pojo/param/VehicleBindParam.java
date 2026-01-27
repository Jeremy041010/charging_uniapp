package cn.tedu.charging.common.pojo.param;

import lombok.Data;

@Data
public class VehicleBindParam {

    private Integer userId;

    private String brand;

    private String license;

    private String model;

    private String vin;
}
