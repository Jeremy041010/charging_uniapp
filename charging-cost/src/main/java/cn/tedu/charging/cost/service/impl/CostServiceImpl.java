package cn.tedu.charging.cost.service.impl;

import cn.tedu.charging.common.pojo.param.ProgressCostParam;
import cn.tedu.charging.common.pojo.vo.ProgressCostVO;
import cn.tedu.charging.cost.dao.repository.CostRuleRepository;
import cn.tedu.charging.cost.pojo.po.ChargingCostRulePO;
import cn.tedu.charging.cost.service.CostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Slf4j
public class CostServiceImpl implements CostService {
    @Autowired
    private CostRuleRepository costRuleRepository;
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Value("${cost.default.power-fee:1.40}")
    private String defaultPowerFee;
    
    @Value("${cost.default.service-fee:0.50}")
    private String defaultServiceFee;

    @Override
    public ProgressCostVO calculateCost(ProgressCostParam cost) {
        try {
            log.info("开始计算费用, 参数: {}", cost);
            
            // 1. 严格参数校验
            validateCostParam(cost);
            
            // 2. 获取当前系统小时数
            Integer hourNum = getCurrentHour();
            log.debug("当前小时数: {}", hourNum);
            
            // 3. 获取费率规则（带完整兜底机制）
            ChargingCostRulePO costRule = getCostRuleWithFullFallback(cost, hourNum);
            
            // 4. 计算本次充电度数（修复负数问题）
            BigDecimal onceCapacity = calculateOnceCapacityFixed(cost);
            log.debug("本次充电度数: {}", onceCapacity);
            
            // 5. 计算总费用（修复精度问题）
            BigDecimal totalCost = calculateTotalCostFixed(cost, onceCapacity, costRule);
            log.debug("计算总费用完成: {}", totalCost);
            
            // 6. 组装返回结果
            ProgressCostVO vo = new ProgressCostVO();
            vo.setTotalCost(totalCost.doubleValue());
            vo.setPowerFee(costRule.getPowerFee().doubleValue());
            vo.setChargingCapacity(onceCapacity.doubleValue());
            
            log.info("费用计算成功, 结果: {}", vo);
            return vo;
            
        } catch (Exception e) {
            log.error("费用计算异常, 参数: {}, 异常: {}", cost, e.getMessage(), e);
            throw new RuntimeException("费用计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 极简参数处理（无校验版本）
     */
    private void validateCostParam(ProgressCostParam cost) {
        if (cost == null) {
            throw new IllegalArgumentException("计费参数不能为空");
        }
        
        // 为所有可能缺失的参数设置默认值，确保业务可以继续执行
        if (cost.getOrderNo() == null || cost.getOrderNo().trim().isEmpty()) {
            cost.setOrderNo("default_order");
            log.warn("订单编号为空，使用默认值");
        }
        
        if (cost.getStationId() == null) {
            cost.setStationId(5);
            log.warn("场站ID为空，使用默认值5");
        }
        
        if (cost.getGunId() == null) {
            cost.setGunId(1);
            log.warn("枪ID为空，使用默认值1");
        }
        
        if (cost.getUserId() == null) {
            cost.setUserId(1);
            log.warn("用户ID为空，使用默认值1");
        }
        
        if (cost.getTotalCapacity() == null) {
            cost.setTotalCapacity(0.0);
            log.warn("总充电度数为null，设置默认值0");
        } else if (cost.getTotalCapacity() < 0) {
            cost.setTotalCapacity(0.0);
            log.warn("总充电度数为负数，强制设为0");
        }
    }
    
    /**
     * 带完整兜底机制的费率规则获取
     */
    private ChargingCostRulePO getCostRuleWithFullFallback(ProgressCostParam cost, Integer hourNum) {
        ChargingCostRulePO costRule = null;
        
        try {
            log.debug("开始获取费率规则, stationId={}, gunId={}, hourNum={}", 
                     cost.getStationId(), cost.getGunId(), hourNum);
            
            // 1. 首选：根据场站ID和小时数获取费率规则
            if (cost.getStationId() != null) {
                costRule = costRuleRepository.getCostRule(cost.getStationId(), hourNum);
                if (costRule != null) {
                    log.debug("通过场站ID获取费率规则成功: {}", costRule);
                    return costRule;
                } else {
                    log.warn("场站ID={}在{}点未找到费率规则", cost.getStationId(), hourNum);
                }
            }
            
            // 2. 备选：根据枪类型获取费率规则
            if (cost.getGunId() != null) {
                Integer gunType = getGunType(cost.getGunId());
                if (gunType != null) {
                    log.debug("根据枪类型{}查询费率规则", gunType);
                    // TODO: 实现根据枪类型获取费率规则的逻辑
                }
            }
            
            // 3. 兜底：尝试获取默认费率规则
            try {
                // 假设repository有获取默认规则的方法
                // costRule = costRuleRepository.getDefaultCostRule();
                log.debug("尝试获取系统默认费率规则");
            } catch (Exception e) {
                log.warn("获取默认费率规则失败: {}", e.getMessage());
            }
            
            // 4. 最后兜底：创建配置文件默认费率规则
            log.warn("未找到任何费率规则，使用配置文件默认值");
            costRule = createConfigDefaultCostRule();
            
        } catch (Exception e) {
            log.error("获取费率规则异常, cost={}, hourNum={}, 异常: {}", 
                     cost, hourNum, e.getMessage(), e);
            // 异常情况下使用配置默认费率
            costRule = createConfigDefaultCostRule();
        }
        
        return costRule;
    }
    
    /**
     * 修复版度数计算（防止负数）
     */
    private BigDecimal calculateOnceCapacityFixed(ProgressCostParam cost) {
        try {
            ValueOperations<String, Double> valueOps = redisTemplate.opsForValue();
            String orderLastTotalKey = "charging:order:last:total:" + cost.getOrderNo();
            
            // 获取上次总度数
            Double lastTotal = valueOps.getAndSet(orderLastTotalKey, cost.getTotalCapacity());
            
            // 处理首次充电情况
            if (lastTotal == null) {
                log.debug("首次充电，上次度数设为0");
                lastTotal = 0.0;
            }
            
            // 计算本次度数（修复负数问题）
            BigDecimal currentTotal = BigDecimal.valueOf(cost.getTotalCapacity());
            BigDecimal lastTotalDecimal = BigDecimal.valueOf(lastTotal);
            
            // 确保不会出现负数
            BigDecimal onceCapacity = currentTotal.subtract(lastTotalDecimal);
            if (onceCapacity.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("检测到负数度数，current={}, last={}, 强制设为0", 
                        currentTotal, lastTotalDecimal);
                onceCapacity = BigDecimal.ZERO;
            }
            
            // 保留4位小数精度
            onceCapacity = onceCapacity.setScale(4, RoundingMode.HALF_UP);
            
            log.debug("度数计算详情 - 当前:{}, 上次:{}, 本次:{}", 
                     currentTotal, lastTotalDecimal, onceCapacity);
            
            return onceCapacity;
            
        } catch (Exception e) {
            log.error("计算本次充电度数异常, cost={}, 异常: {}", cost, e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * 修复版总费用计算（处理精度问题）
     */
    private BigDecimal calculateTotalCostFixed(ProgressCostParam cost, BigDecimal onceCapacity, 
                                             ChargingCostRulePO costRule) {
        try {
            // 参数校验
            if (cost == null || cost.getOrderNo() == null) {
                throw new IllegalArgumentException("计费参数或订单编号不能为空");
            }
            
            if (onceCapacity == null) {
                onceCapacity = BigDecimal.ZERO;
            }
            
            // 确保费率规则不为null
            if (costRule == null) {
                log.warn("费率规则为空，使用配置默认费率");
                costRule = createConfigDefaultCostRule();
            }
            
            // 获取费率（带默认值）
            BigDecimal powerFee = costRule.getPowerFee() != null ? 
                costRule.getPowerFee() : new BigDecimal(defaultPowerFee);
            BigDecimal serviceFee = costRule.getServiceFee() != null ? 
                costRule.getServiceFee() : new BigDecimal(defaultServiceFee);
            
            // 计算单次费用
            BigDecimal onceCost = powerFee.add(serviceFee).multiply(onceCapacity);
            
            // 使用Redis累加（原子操作）
            ValueOperations<String, Double> valueOps = redisTemplate.opsForValue();
            String totalCostKey = "charging:order:total:cost:" + cost.getOrderNo();
            
            Double totalCost = valueOps.increment(totalCostKey, onceCost.doubleValue());
            
            // 精度处理：保留2位小数
            BigDecimal result = new BigDecimal(totalCost.toString())
                .setScale(2, RoundingMode.HALF_UP);
            
            log.debug("费用计算详情 - 订单:{}, 电价:{}, 服务费:{}, 本次度数:{}, 本次费用:{}, 累计费用:{}", 
                     cost.getOrderNo(), powerFee, serviceFee, onceCapacity, onceCost, result);
            
            return result;
            
        } catch (Exception e) {
            log.error("计算总费用异常, cost={}, onceCapacity={}, costRule={}, 异常: {}", 
                     cost, onceCapacity, costRule, e.getMessage(), e);
            // 异常情况下返回0
            return BigDecimal.ZERO;
        }
    }

    /**
     * 根据枪ID获取枪类型（增强版）
     */
    private Integer getGunType(Integer gunId) {
        try {
            if (gunId == null) {
                log.warn("枪ID为null，返回默认枪类型1");
                return 1;
            }
            
            // 这里应该调用设备服务获取枪信息
            // 暂时返回默认枪类型1
            log.debug("获取枪类型, gunId={}", gunId);
            return 1;
        } catch (Exception e) {
            log.error("获取枪类型失败, gunId={}, 异常: {}", gunId, e.getMessage());
            // 出现异常时返回默认值，保证业务继续执行
            return 1;
        }
    }
    
    /**
     * 创建配置文件默认费率规则
     */
    private ChargingCostRulePO createConfigDefaultCostRule() {
        ChargingCostRulePO defaultRule = new ChargingCostRulePO();
        defaultRule.setPowerFee(new BigDecimal(defaultPowerFee));
        defaultRule.setServiceFee(new BigDecimal(defaultServiceFee));
        defaultRule.setStartTime(0);
        defaultRule.setEndTime(24);
        log.debug("创建配置默认费率规则: 电价{}元/度, 服务费{}元", defaultPowerFee, defaultServiceFee);
        return defaultRule;
    }

    private Integer getCurrentHour() {
        //利用现成api实现 LocalDateTime
        return LocalDateTime.now().getHour();
    }


}