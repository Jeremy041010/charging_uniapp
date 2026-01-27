package cn.tedu.charging.cost.dao.repository.impl;

import cn.tedu.charging.cost.dao.mapper.CostRuleMapper;
import cn.tedu.charging.cost.dao.repository.CostRuleRepository;
import cn.tedu.charging.cost.pojo.po.ChargingCostRulePO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CostRuleRepositoryImpl implements CostRuleRepository {
    @Autowired
    private CostRuleMapper costRuleMapper;
    @Override
    public ChargingCostRulePO getCostRule(Integer stationId, Integer hour) {
 /*select * from charging_cost_rule
        where station_id=5 and gun_type=1 and start_time<=10 and end_time>10*/
        //拼接where条件
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("station_id",stationId);
        queryWrapper.eq("gun_type",1);
        queryWrapper.le("start_time",hour);
        queryWrapper.gt("end_time",hour);
        return costRuleMapper.selectOne(queryWrapper);
    }
}
