package cn.tedu.charging.order.dao.repository.impl;

import cn.tedu.charging.common.exception.ServiceException;
import cn.tedu.charging.order.dao.mapper.BillExceptionMapper;
import cn.tedu.charging.order.dao.mapper.BillFailMapper;
import cn.tedu.charging.order.dao.mapper.BillSuccessMapper;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillFailPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Repository
public class BillRepositoryImpl implements BillRepository {
    @Autowired
    private BillSuccessMapper successMapper;
    @Autowired
    private BillFailMapper failMapper;
    @Autowired
    private BillExceptionMapper billExceptionMapper;

    @Override
    public long countSuccess(String orderNo) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("bill_id", orderNo);
        return successMapper.selectCount(queryWrapper);
    }

    @Override
    public long countFail(String orderNo) {
        try {
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("bill_id", orderNo);
            return successMapper.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("统计失败订单数失败，orderNo={}", orderNo, e);
            throw new ServiceException(5010, "统计失败订单数异常");
        }
    }

    @Override
    public void saveFail(ChargingBillFailPO fail) {
        try {
            failMapper.insert(fail);
        } catch (Exception e){
            log.error("保存失败订单失败，fail={}", fail, e);
            throw new ServiceException(5011, "保存失败订单异常");
        }
    }

    @Override
    public void saveSuccess(ChargingBillSuccessPO success) {
        successMapper.insert(success);
    }

    @Override
    public void updateSuccessStatus(Integer id, Integer status) {
        ChargingBillSuccessPO po = new ChargingBillSuccessPO();
        po.setId(id);
        po.setBillStatus(status);
        successMapper.updateById(po);
    }

    @Override
    public void updateSuccessStatus(String billId, Integer status) {
        try {
            UpdateWrapper updateWrapper = new UpdateWrapper();
            updateWrapper.eq("bill_id", billId);
            // 移除了 bill_status 条件，确保无论当前状态如何都能更新
            ChargingBillSuccessPO po = new ChargingBillSuccessPO();
            po.setBillStatus(status);
            po.setUpdateTime(new Date());
            successMapper.update(po, updateWrapper);
        } catch (Exception e){
            log.error("更新订单状态失败，billId={}, status={}", billId, status, e);
            throw new ServiceException(5015, "更新订单状态异常：" + e.getMessage());
        }
    }

    @Override
    public void saveException(ChargingBillExceptionPO exceptionPO) {
        billExceptionMapper.insert(exceptionPO);
    }

    @Override
    public ChargingBillSuccessPO getSuccessByBillId(String billId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("bill_id", billId);
        return successMapper.selectOne(queryWrapper);
    }

    @Override
    public void saveEnd(ChargingBillEndPO end) {
        try {
            // 修改为更新操作，因为映射的是charging_bill_success表
            UpdateWrapper<ChargingBillSuccessPO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("bill_id", end.getBillId());
            
            // 设置要更新的字段
            ChargingBillSuccessPO updatePo = new ChargingBillSuccessPO();
            if (end.getChargingDuration() != null) {
                updatePo.setChargingDuration(end.getChargingDuration());
            }
            if (end.getConsumeAmount() != null) {
                updatePo.setPayAmount(end.getConsumeAmount());
            }
            if (end.getEndReason() != null) {
                updatePo.setBillStatus(end.getEndReason());
            } else {
                // 如果没有指定结束原因，默认设置为正常结束(状态2)
                updatePo.setBillStatus(2);
            }
            updatePo.setUpdateTime(new Date());
            
            successMapper.update(updatePo, updateWrapper);
        } catch (Exception e) {
            log.error("更新结束订单失败，end={}", end, e);
            throw new ServiceException(5016, "更新结束订单异常：" + e.getMessage());
        }
    }

    @Override
    public ChargingBillEndPO getEndByBillId(String billId) {
        try {
            QueryWrapper<ChargingBillSuccessPO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("bill_id", billId);
            ChargingBillSuccessPO successPO = successMapper.selectOne(queryWrapper);
            
            // 转换为ChargingBillEndPO
            if (successPO != null) {
                ChargingBillEndPO endPO = new ChargingBillEndPO();
                endPO.setBillId(successPO.getBillId());
                endPO.setChargingDuration(successPO.getChargingDuration());
                endPO.setConsumeAmount(successPO.getPayAmount());
                endPO.setEndReason(successPO.getBillStatus());
                endPO.setUpdateTime(successPO.getUpdateTime());
                endPO.setDeleted(successPO.getDeleted());
                return endPO;
            }
            return null;
        } catch (Exception e) {
            log.error("查询结束订单失败，billId={}", billId, e);
            throw new ServiceException(5017, "查询结束订单异常：" + e.getMessage());
        }
    }
}