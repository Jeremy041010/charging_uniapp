package cn.tedu.charging.common.enums;
public enum GunStatusEnum {
    AVAILABLE(1,"空闲"),
    CHARGING(2,"充电中"),
    ERROR(3,"故障"),
    UNAVAILABLE(4,"不可用")
    ;
    private Integer status;
    private String desc;

    GunStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    GunStatusEnum() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
