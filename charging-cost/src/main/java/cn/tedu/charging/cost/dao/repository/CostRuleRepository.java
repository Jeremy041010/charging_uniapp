package cn.tedu.charging.cost.dao.repository;

import cn.tedu.charging.cost.pojo.po.ChargingCostRulePO;

public interface CostRuleRepository {
    ChargingCostRulePO getCostRule(Integer stationId, Integer hour);
}
