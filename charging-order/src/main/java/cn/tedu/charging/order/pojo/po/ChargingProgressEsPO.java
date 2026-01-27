package cn.tedu.charging.order.pojo.po;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "charging-progress")
@Setting(shards = 1,replicas = 0)
public class ChargingProgressEsPO {
    //给属性定义mapping映射 188361212375217312
    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    @Field(type = FieldType.Keyword)
    private String orderNo;
    @Field(type = FieldType.Integer)
    private Integer userId;
    @Field(type = FieldType.Integer)
    private Integer pileId;
    @Field(type = FieldType.Integer)
    private Integer gunId;
    @Field(type = FieldType.Integer)
    private Integer stationId;
    @Field(type = FieldType.Double)
    private Double chargingCapacity;
    @Field(type = FieldType.Double)
    private Double temperature;
    @Field(type = FieldType.Double)
    private Double totalCost;
    @Field(type = FieldType.Boolean)
    private Boolean isFull;
    /**
     * "properties":{
     *         id: String 主键,
     *         billId: keyword,
     *         userId: integer,
     *         stationId: integer,
     *         gunId: integer,
     *         chargingCapacity: double,
     *         totalCost: double,
     *         isFull: boolean
     *}
     */
}
