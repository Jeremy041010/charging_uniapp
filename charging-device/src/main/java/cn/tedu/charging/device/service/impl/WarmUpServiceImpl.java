package cn.tedu.charging.device.service.impl;

import cn.tedu.charging.device.dao.repository.DeviceRepository;
import cn.tedu.charging.device.pojo.po.ChargingStationPO;
import cn.tedu.charging.device.service.WarmUpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WarmUpServiceImpl implements WarmUpService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public void doWarmUp() {
        //1.定义批量batch,1万
        Long batch =2L;//演示的
        //2.查询一下total
        Long total = deviceRepository.countStations();
        //3.计算得到循环的次数
        Long loopNum = total%batch==0?total/batch:total/batch+1;
        log.debug("本次总数据量:{},每批:{},循环读取次数:{}",total,batch,loopNum);
        //4.循环
        for (int i =0; i<loopNum; i++){
            //5.每次读取起始下标start不一样 rows就是batch
            Long start = i*batch;
            //6.分批分页的读取
            List<ChargingStationPO> pos = deviceRepository.getStationsPage(start,batch);
            //7.这一批次写入到geo
            deviceRepository.saveGeos(pos);
        }
    }
    /*@Override
    public void doWarmUp() {
        //1.读取数据层的场站数据 读取到的是数据库持久化po的列表
        List<ChargingStationPO>  pos=deviceRepository.getAllStations();
        //判断 只有pos不为空 才写入redis
        if (pos!=null&&pos.size()>0){
            //2.写入geo集合
            deviceRepository.saveGeos(pos);
        }
    }
    */
}
