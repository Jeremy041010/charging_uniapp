package cn.tedu.charging.order.dao.repository.impl;

import cn.tedu.charging.order.dao.mapper.BillExceptionMapper;
import cn.tedu.charging.order.dao.mapper.BillFailMapper;
import cn.tedu.charging.order.dao.mapper.BillSuccessMapper;
import cn.tedu.charging.order.dao.repository.BillRepository;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillFailPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
        queryWrapper.eq("bill_id",orderNo);
        return successMapper.selectCount(queryWrapper);
    }


    @Override
    public long countFail(String orderNo) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("bill_id",orderNo);
        return successMapper.selectCount(queryWrapper);
    }

    @Override
    public void saveFail(ChargingBillFailPO fail) {
        failMapper.insert(fail);

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
        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("bill_id",billId);
        updateWrapper.eq("bill_status",status);
        ChargingBillSuccessPO po = new ChargingBillSuccessPO();
        po.setBillStatus(status);
        successMapper.update(po,updateWrapper);
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