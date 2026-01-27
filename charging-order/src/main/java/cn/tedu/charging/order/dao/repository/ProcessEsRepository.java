package cn.tedu.charging.order.dao.repository;

import cn.tedu.charging.order.pojo.po.ChargingProgressEsPO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 继承 ElasticsearchRepository
 * 类似 与 plus 里的 baseMapper
 * ElasticsearchRepository<T, ID> <ChargingProgressEsPO, String>
 * T 表示要映射的对象
 * ID 表示主键的类型
 */
public interface ProcessEsRepository
        extends ElasticsearchRepository<ChargingProgressEsPO, String> {
}