package cn.tedu.charging.device.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MappingTest {
    public static void main(String[] args) {
        //1.将 1 2 3 4 5 做映射计算 元素平方 生成新的集合
        List<Integer> origin=List.of(1,2,3,4,5);
        //2.使用映射计算 得到 1 4 9 16 25
        List<Integer> news = origin.stream().map(number -> {
            //映射算法 对每个元素怎么计算
            return number * number;
        }).collect(Collectors.toList());
        System.out.println(news);
    }
}
