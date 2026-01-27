package cn.tedu.charging.cost.service.impl;

import cn.tedu.charging.common.pojo.param.ProgressCostParam;
import cn.tedu.charging.common.pojo.vo.ProgressCostVO;
import cn.tedu.charging.cost.dao.mapper.CostRuleMapper;
import cn.tedu.charging.cost.dao.repository.CostRuleRepository;
import cn.tedu.charging.cost.pojo.po.ChargingCostRulePO;
import cn.tedu.charging.cost.service.CostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class CostServiceImpl implements CostService {
    @Autowired
    private CostRuleRepository costRuleRepository;
    @Override
    public ProgressCostVO calculateCost(ProgressCostParam cost) {
        //1.拿到当前系统小时数 比如 10:58 拿到了10
        Integer hourNum = getCurrentHour();
        ChargingCostRulePO costRule  = getCostRule(cost.getStationId(),cost.getGunId(),hourNum);
        BigDecimal onceCapacity = calculateOnceCapacity(cost);
        BigDecimal totalCost = calculateTotalCost(cost,onceCapacity,costRule);
        ProgressCostVO vo = new ProgressCostVO();
        vo.setTotalCost(totalCost.doubleValue());
        vo.setPowerFee(costRule.getPowerFee().doubleValue());
        vo.setChargingCapacity(onceCapacity.doubleValue());
        return vo;
    }

    private BigDecimal calculateTotalCost(ProgressCostParam cost ,BigDecimal once, ChargingCostRulePO costRule) {
        //这里要利用incrby底层命令做单次金额累加
        ValueOperations<String,Double> valueOps=redisTemplate.opsForValue();
        String totalCostKey="charging:order:total:cost:"+cost.getOrderNo();
        //1.准备电单价 计算单次金额
        BigDecimal powerFee = costRule.getPowerFee();
        BigDecimal onceCost=powerFee.multiply(once);
        //2.使用key值做累加 第一次从0开始加,
        Double totalCost = valueOps.increment(totalCostKey, onceCost.doubleValue());
        log.debug("当前订单:{},充电单价:{},本次金额:{},总金额{}",cost.getOrderNo(),powerFee,onceCost,totalCost);
        return new BigDecimal(totalCost+"");
    }
    @Autowired
    private RedisTemplate redisTemplate;
    private BigDecimal calculateOnceCapacity(ProgressCostParam cost) {
        //要调用的getset 属于String类型 先拿到操作读写的客户端
        ValueOperations<String,Double> valueOps=redisTemplate.opsForValue();
        //1.将本次总度数 存储到redis同时读取上次总度数 需要一个key值和订单编号绑定
        String orderLastTotalKey="charging:order:last:total:"+cost.getOrderNo();
        //getset key currentTotal
        Double lastTotal = valueOps.getAndSet(orderLastTotalKey, cost.getTotalCapacity());
        //判断上次是否存在
        if(lastTotal==null){
            log.debug("上次总度数不存在,说明本次同步计算是第一次");
            lastTotal=0d;
        }
        //2.使用本次总度数 减去上次 double有精度偏差 在业务中所有的计算过程全部转化为BigDecimal
        BigDecimal currentTotalCapacity=new BigDecimal(cost.getTotalCapacity()+"");
        BigDecimal lastTotalCapacity=new BigDecimal(lastTotal+"");
        //BigDeciaml 加减乘除  加plus 减subtract 乘multiply 除divide
        return currentTotalCapacity.subtract(lastTotalCapacity);
}

    private ChargingCostRulePO getCostRule(Integer stationId, Integer gunId, Integer hourNum) {
        //undo 根据枪id查询枪类型
        return costRuleRepository.getCostRule(stationId,hourNum);
    }

    private Integer getCurrentHour() {
        //利用现成api实现 LocalDateTime
        return LocalDateTime.now().getHour();
    }


}
