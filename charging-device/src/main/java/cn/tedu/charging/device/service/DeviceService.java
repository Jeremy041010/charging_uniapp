package cn.tedu.charging.device.service;

import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.common.pojo.vo.StationDetailVO;
import cn.tedu.charging.common.pojo.vo.StationInfoVO;

import java.util.List;

public interface DeviceService {
    List<StationInfoVO> nearStations(NearStationsQuery query);

    StationDetailVO detailStation(Integer stationId);

    void updateGunStatus(Integer gunId, Integer status);

    Boolean checkGunAvailable(Integer gunId);
}
