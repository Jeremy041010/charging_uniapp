package cn.tedu.charging.common.utils;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法ID生成器工具类
 */
public class SnowflakeIdGenerator {
    // 起始的时间戳（2020-01-01 00:00:00）
    private final static long START_TIMESTAMP = 1577808000000L;

    // 每一部分占用的位数
    private final static long SEQUENCE_BIT = 12;   // 序列号占用的位数
    private final static long MACHINE_BIT = 5;     // 机器标识占用的位数
    private final static long DATACENTER_BIT = 5;  // 数据中心占用的位数

    // 每一部分的最大值
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    // 每一部分向左的位移
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;    // 数据中心ID
    private final long machineId;       // 机器ID
    private long sequence = 0L;        // 序列号
    private long lastTimestamp = -1L;  // 上一次时间戳

    /**
     * 构造函数（自动生成数据中心ID和机器ID）
     */
    public SnowflakeIdGenerator() {
        this.datacenterId = getDatacenterId();
        this.machineId = getMaxMachineId(datacenterId);
    }

    /**
     * 构造函数（手动指定数据中心ID和机器ID）
     * @param datacenterId 数据中心ID (0~31)
     * @param machineId    机器ID (0~31)
     */
    public SnowflakeIdGenerator(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个ID
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，此时应当抛出异常
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " +
                    (lastTimestamp - currentTimestamp) + " milliseconds");
        }

        // 如果是同一时间生成的，则进行序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号已经达到最大值，需要等待下一毫秒
            if (sequence == 0L) {
                currentTimestamp = getNextMillisecond(lastTimestamp);
            }
        } else {
            // 不同毫秒内，序列号重置为随机值，增加离散性
            sequence = ThreadLocalRandom.current().nextLong(0, 2);
        }

        lastTimestamp = currentTimestamp;

        // 通过移位和或运算拼到一起组成64位的ID
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long getNextMillisecond(long lastTimestamp) {
        long currentTimestamp = getCurrentTimestamp();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = getCurrentTimestamp();
        }
        return currentTimestamp;
    }

    /**
     * 获取当前时间戳（毫秒）
     * @return 当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取最大机器ID
     * @param datacenterId 数据中心ID
     * @return 最大机器ID
     */
    protected static long getMaxMachineId(long datacenterId) {
        StringBuilder pid = new StringBuilder();
        pid.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (name != null && !name.isEmpty()) {
            // 获取jvm进程ID
            pid.append(name.split("@")[0]);
        }
        // 机器ID = PID + MAC地址哈希值
        return (pid.toString().hashCode() & 0xFFFF) % (MAX_MACHINE_NUM + 1);
    }

    /**
     * 获取数据中心ID
     * @return 数据中心ID
     */
    protected static long getDatacenterId() {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) |
                            (0x0000FF00 & (((long) mac[mac.length - 2]) << 8)) >> 6);
                    id = id % (MAX_DATACENTER_NUM + 1);
                }
            }
        } catch (Exception e) {
            System.err.println("Get datacenter id error: " + e.getMessage());
            id = 0L;
        }
        return id;
    }

    // 测试
    public static void main(String[] args) {
        SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator();

        for (int i = 0; i < 10; i++) {
            long id = idGenerator.nextId();
            System.out.println(id + " -> " + Long.toBinaryString(id));
        }
    }
}