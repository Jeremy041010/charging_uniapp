package cn.tedu.charging.common.pojo.query;

import lombok.Data;

@Data
public class NearStationsQuery {
    //手机定位中心点 写死的 天安门地理位置
    private Double longitude;
    private Double latitude;
    //半径 当前写死的 5000 km
    private Double radius;
}
