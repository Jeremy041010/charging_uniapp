package cn.tedu.charging.device.dao.repository;

import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;
import cn.tedu.charging.device.pojo.po.ChargingStationPO;
import cn.tedu.charging.device.pojo.po.StationCanalPO;
import cn.tedu.charging.device.pojo.po.StationGeoPO;

import java.util.List;

public interface DeviceRepository {
    List<StationGeoPO> nearStations(NearStationsQuery query);


    String getStationName(Integer stationId);

    ChargingStationPO getStationById(Integer stationId);

    List<ChargingGunInfoPO> getStationGuns(Integer stationId);

    Boolean updateGunStatus(Integer gunId, Integer status);
    void saveStation(StationCanalPO stationCanalPO);

    void updateStation(StationCanalPO before, StationCanalPO after);

    void deleteStation(StationCanalPO stationCanalPO);

    Long countGunByIdAndStatus(Integer gunId, Integer status);
    
    ChargingGunInfoPO getGunById(Integer gunId);

    List<ChargingStationPO> getAllStations();

    void saveGeos(List<ChargingStationPO> pos);

    List<ChargingStationPO> getStationsPage(Long start, Long batch);

    Long countStations();
}
