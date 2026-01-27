package cn.tedu.charging.device.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@TableName("charging_pile_info")
public class ChargingPileInfoPO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("pile_number")
    private String pileNumber;

    @TableField("pile_name")
    private String pileName;

    @TableField("gun_infos")
    private String gunInfos;

    @TableField("power")
    private BigDecimal power;

    @TableField("pile_type")
    private Integer pileType;

    @TableField("pile_model")
    private Integer pileModel;

    @TableField("protocol")
    private Integer protocol;

    @TableField("network")
    private Integer network;

    @TableField("pile_lng")
    private BigDecimal pileLng;

    @TableField("pile_lat")
    private BigDecimal pileLat;

    @TableField("station_id")
    private Integer stationId;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("deleted")
    private Integer deleted;
}
