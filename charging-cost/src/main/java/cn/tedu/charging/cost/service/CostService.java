package cn.tedu.charging.cost.service;

import cn.tedu.charging.common.pojo.param.ProgressCostParam;
import cn.tedu.charging.common.pojo.vo.ProgressCostVO;

public interface CostService {
    ProgressCostVO calculateCost(ProgressCostParam cost);
}
