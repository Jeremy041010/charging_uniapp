package cn.tedu.charging.device.service.impl;

import cn.tedu.charging.common.pojo.query.NearStationsQuery;
import cn.tedu.charging.common.pojo.vo.GunInfoVO;
import cn.tedu.charging.common.pojo.vo.StationDetailVO;
import cn.tedu.charging.common.pojo.vo.StationInfoVO;
import cn.tedu.charging.device.dao.repository.DeviceRepository;
import cn.tedu.charging.device.pojo.po.ChargingGunInfoPO;
import cn.tedu.charging.device.pojo.po.ChargingStationPO;
import cn.tedu.charging.device.pojo.po.StationGeoPO;
import cn.tedu.charging.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {
    @Autowired
    private DeviceRepository deviceRepository;
    @Override
    public List<StationInfoVO> nearStations(NearStationsQuery query) {
        //最终返回的是vo列表,先准备一个空值,能封装的时候,再初始化
        List<StationInfoVO> vos=null;
        //1.调用仓储层 返回包含了redis和mysql的数据
        List<StationGeoPO> geoPos = deviceRepository.nearStations(query);
        //判断是否为空
        if (geoPos!=null&&geoPos.size()>0){
            log.debug("查询附近充电站,命中:{}条",geoPos.size());
            //对vos做初始化
            vos=new ArrayList<>();
            //2.封装vos 循环遍历pos
            for (StationGeoPO geoPo : geoPos) {
                StationInfoVO vo=new StationInfoVO();
                //能拷贝先拷贝
                BeanUtils.copyProperties(geoPo,vo);
                //单独补充avgPrice
                vo.setAvgPrice(1.5);
                //TODO 距离值调整 要求km 小数点不能超过2位
                log.debug("当前距离的值:{}",vo.getDistance());
                vos.add(vo);
            }
        }else{
            log.debug("本次查询附近场站,中点:X={},Y={},半径:{}米,未命中",query.getLongitude(),query.getLatitude(),query.getRadius());
        }
        return vos;
    }

    @Override
    public StationDetailVO detailStation(Integer stationId) {
        //先定义一个返回的空值
        StationDetailVO vo=null;
        //1.查询这个id对应的场站
        ChargingStationPO stationPO = deviceRepository.getStationById(stationId);
        //判断非空
        if (stationPO!=null){
            log.debug("查询场站详情,命中场站数据:{}",stationPO);
            //2.利用场站id查询对应枪列表
            List<ChargingGunInfoPO> gunPos=deviceRepository.getStationGuns(stationId);
            //查询枪pos是为了封装枪vos
            List<GunInfoVO> gunVos=null;
            //判断枪是否为空 非空的时候封装gunVos
            if (gunPos!=null&&gunPos.size()>0){
                //提升代码性能 list转化list的时候 使用的stream() sort filter
                gunVos=gunPos.stream().map(gunPo->{
                    //把每个原集合po转化成vo
                    GunInfoVO gunVo=new GunInfoVO();
                    BeanUtils.copyProperties(gunPo,gunVo);
                    return gunVo;
                }).collect(Collectors.toList());
                /*for (ChargingGunInfoPO gunPo : gunPos) {
                    GunInfoVO vo=new GunInfoVO();
                    BeanUtils.copyProperties(gunPo,vo);
                }*/
            }
            //3.封装最终的detailVO
            vo=new StationDetailVO();
            vo.setGunInfoVos(gunVos);
            vo.setStationId(stationPO.getId());
            vo.setStationName(stationPO.getStationName());
            vo.setAddress(stationPO.getAddress());
            vo.setStationStatus(stationPO.getStationStatus());
        }else{
            log.debug("场站查询结果为空");
        }
        return vo;
    }


    @Override
    public void updateGunStatus(Integer gunId, Integer status) {
        deviceRepository.updateGunStatus(gunId,status);
    }

    @Override
    public Boolean checkGunAvailable(Integer gunId) {
        return null;
    }
    
    @Override
    public ChargingGunInfoPO getGunById(Integer gunId) {
        return deviceRepository.getGunById(gunId);
    }
}
