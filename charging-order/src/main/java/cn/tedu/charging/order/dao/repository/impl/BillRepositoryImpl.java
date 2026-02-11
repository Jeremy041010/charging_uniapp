package cn.tedu.charging.order.dao.repository.impl;

import cn.tedu.charging.common.exception.ServiceException;
import cn.tedu.charging.order.dao.mapper.BillExceptionMapper;
import cn.tedu.charging.order.dao.mapper.BillFailMapper;
import cn.tedu.charging.order.dao.mapper.BillSuccessMapper;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillFailPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
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
            updateWrapper.eq("bill_status", status);
            ChargingBillSuccessPO po = new ChargingBillSuccessPO();
            po.setBillStatus(status);
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
        queryWrapper.eq("bill_id",billId);
        return successMapper.selectOne(queryWrapper);
    }
}