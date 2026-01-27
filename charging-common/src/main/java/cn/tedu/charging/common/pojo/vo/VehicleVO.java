package cn.tedu.charging.common.pojo.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Date;

@Data
public class VehicleVO {

    private Integer id;

    private String brand;

    private String license;

    private String model;

    private String vin;
}
