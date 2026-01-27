package cn.tedu.charging.common.enums;

/**
 * 用户和车辆的绑定状态枚举
 */
public enum VehicleUserBindEnum {
    UNBIND("未绑定",0),
    BINDED("已绑定",1);
    private String desc;//对状态的描述
    private Integer state;//状态码 1绑定 0 未绑定

    VehicleUserBindEnum(String desc, Integer state) {
        this.desc = desc;
        this.state = state;
    }

    VehicleUserBindEnum() {
    }



    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
