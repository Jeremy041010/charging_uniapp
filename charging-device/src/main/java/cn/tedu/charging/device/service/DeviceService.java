package cn.tedu.charging.device.service;

import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.common.pojo.vo.StationDetailVO;
import cn.tedu.charging.common.pojo.vo.StationInfoVO;
import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;

import java.util.List;

public interface DeviceService {
    List<StationInfoVO> nearStations(NearStationsQuery query);

    StationDetailVO detailStation(Integer stationId);

    void updateGunStatus(Integer gunId, Integer status);

    Boolean checkGunAvailable(Integer gunId);
    
    ChargingGunInfoPO getGunById(Integer gunId);
}
