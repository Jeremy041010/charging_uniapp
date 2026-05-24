package cn.tedu.charging.order.dao.repository;

import cn.tedu.charging.order.pojo.po.ChargingBillEndPO;
import cn.tedu.charging.order.pojo.po.ChargingBillExceptionPO;
import cn.tedu.charging.order.pojo.po.ChargingBillFailPO;
import cn.tedu.charging.order.pojo.po.ChargingBillSuccessPO;

public interface BillRepository {
    long countSuccess(String orderNo);

    long countFail(String orderNo);

    void saveFail(ChargingBillFailPO fail);

    void saveSuccess(ChargingBillSuccessPO success);

    void updateSuccessStatus(Integer id, Integer status);
    void updateSuccessStatus(String billId, Integer status);
    void saveException(ChargingBillExceptionPO exceptionPO);

    ChargingBillSuccessPO getSuccessByBillId(String billId);
    
    // 新增：结束订单相关方法
    void saveEnd(ChargingBillEndPO end);

    
    // 查询结束订单信息
    ChargingBillEndPO getEndByBillId(String billId);


}