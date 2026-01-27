package cn.tedu.charging.device.dao.repository.impl;

import cn.tedu.charging.common.constant.CacheKeyConst;
import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.device.dao.mapper.GunMapper;
import cn.tedu.charging.device.dao.mapper.StationMapper;
import cn.tedu.charging.device.dao.repository.DeviceRepository;
import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;
import cn.tedu.charging.device.pojo.po.ChargingStationPO;
import cn.tedu.charging.device.pojo.po.StationCanalPO;
import cn.tedu.charging.device.pojo.po.StationGeoPO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DeviceRepositoryImpl implements DeviceRepository {
    @Autowired
    private StationMapper stationMapper;
    @Autowired
    private GunMapper gunMapper;
    @Autowired
    private GeoOperations<String,Integer> geoOperations;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<StationGeoPO> nearStations(NearStationsQuery query) {
        List<StationGeoPO> geoPos =null;
        //1.入参是圆参数 中心点和半径 先查询一下geo元素集合
        //georadius lng lat radius m withcoord withdist
        //1.1 封装入参 Point center Distance radius Circle circle
        Point center = new Point(query.getLongitude(),query.getLatitude());
        Distance radius = new Distance(query.getRadius(), RedisGeoCommands.DistanceUnit.METERS);
        Circle circle = new Circle(center,radius);
        //1.2 选项 RedisGeoCommands.args.includeCood includeDist
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs();
        args.includeCoordinates().includeDistance();
        //2.调用api radius查询 解析这个结果List<GeoResult> distance point name
        List<GeoResult<RedisGeoCommands.GeoLocation<Integer>>> results =geoOperations.radius(CacheKeyConst.GEO_STATIONS, circle, args).getContent();
        //3.循环遍历 每次循环使用stationId元素值 查询数据库 以获取name和status
        if (results != null && results.size() >0){
            //命中元素
            geoPos = new ArrayList<>();
            //对geoResult做循环 每次封装一个po
            for (GeoResult<RedisGeoCommands.GeoLocation<Integer>> result : results){
                StationGeoPO po = new StationGeoPO();
                //redis提供的数据 有了 id lng lat distance
                po.setStationId(result.getContent().getName());
                po.setStationLng(result.getContent().getPoint().getX());
                po.setStationLat(result.getContent().getPoint().getY());
                po.setDistance(new BigDecimal(result.getDistance().getValue()+""));
                //mysql提供的数据 单独查询stationName stationStatus
                ChargingStationPO stationPO =getStationById(result.getContent().getName());
                po.setStationStatus(stationPO.getStationStatus());
                po.setStationName(stationPO.getStationName());
                geoPos.add(po);
            }
        }
        //4.封装返回
        return geoPos;
    }

    @Override
    public String getStationName(Integer stationId) {
        return "";
    }

    @Override
    public ChargingStationPO getStationById(Integer stationId) {
        return stationMapper.selectById(stationId);
    }

    @Override
    public List<ChargingGunInfoPO> getStationGuns(Integer stationId) {
        return gunMapper.selectByStationId(stationId);
    }

    @Override
    public Boolean updateGunStatus(Integer gunId, Integer status) {
        return null;
    }

    @Override
    public void saveStation(StationCanalPO stationCanalPO) {

    }

    @Override
    public void updateStation(StationCanalPO before, StationCanalPO after) {

    }

    @Override
    public void deleteStation(StationCanalPO stationCanalPO) {

    }

    @Override
    public Long countGunByIdAndStatus(Integer gunId, Integer status) {
        return 0L;
    }

    @Override
    public List<ChargingStationPO> getAllStations() {
        return stationMapper.selectList(null);
    }

    @Override
    public void saveGeos(List<ChargingStationPO> pos) {
        //1.方法入参是包含了geo元素id以及每个元素的坐标的数组集合 将pos转化成List<GeoLocation>
        List<RedisGeoCommands.GeoLocation<Integer>> members = new ArrayList<>();
        //遍历循环pos
        for (ChargingStationPO po : pos) {
            RedisGeoCommands.GeoLocation member=
                    new RedisGeoCommands.GeoLocation(
                            po.getId(),
                            new Point(po.getStationLng().doubleValue(),po.getStationLat().doubleValue()));
            //将geo对象元素放到members集合
            members.add(member);

            //2.调用geoOps的api
            geoOperations.add(CacheKeyConst.GEO_STATIONS,members);
        }
    }

    @Override
    public List<ChargingStationPO> getStationsPage(Long start, Long batch) {
        QueryWrapper<ChargingStationPO> queryWrapper =new QueryWrapper<>();
        queryWrapper.last("limit " + start + ", "+batch);
        return stationMapper.selectList(queryWrapper);
    }
    @Override
    public Long countStations() {
        return stationMapper.selectCount(null);
    }

}
