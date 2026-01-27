package cn.tedu.charging.order.clients;

import cn.tedu.charging.common.pojo.param.ProgressCostParam;
import cn.tedu.charging.common.pojo.vo.ProgressCostVO;
import cn.tedu.charging.common.protocol.JsonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("charging-cost")
public interface CostClient {
    //调用计算价格
    @PostMapping("/cost/bill/calculate")
    JsonResult<ProgressCostVO> calculateCost(@RequestBody ProgressCostParam param);
}