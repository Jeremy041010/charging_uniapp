package cn.tedu.charging.order.server.points;


import com.alibaba.fastjson2.JSON;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前这个类,表示在web服务中,一个对外暴露的ws连接终端
 * 他负责所有客户端连接这个端点之后的通信过程
 * 1. 建立连接
 * 2. 连接断开
 * 3. 客户端发送什么消息
 * 4. 以上过程是否有任何异常
 * 5. 给客户端推送什么消息
 * 所以想要让这个类生效,需要明确连接终端地址,以及每一个通信过程的处理逻辑
 */
@Component
@Slf4j
@ServerEndpoint("/ws/server/{userId}")
public class WebsocketServerPoint {
    private static final Map<Integer,Session> SESSIONS= new ConcurrentHashMap<>();
    /*
    客户端连接陈工时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId")Integer userId) throws IOException {
        log.info("连接成功,连接id:{},用户id:{}",session.getId(),userId);
        //用户建立连接,传递userId,程序创建session,映射关系存储到SESSIONS中
        //并不是每次连接建立都只是在SESSIONS.put
        //判断某个用户建立的连接,传递的userId 在SESSIONS存在不存在
        boolean contain = SESSIONS.containsKey(userId);
        if (contain){
            log.info("当前用户已经建立连接和映射,userId:{}",userId);
            //将旧的连接和映射删除
            Session oldSession = SESSIONS.get(userId);
            oldSession.close();
            //从map中删除
            SESSIONS.remove(userId);
        }
        //将新的连接放到SESSIONS中
        SESSIONS.put(userId,session);
        //打印SESSIONS的个数,就是在线人数
        log.info("当前在线人数:{}",SESSIONS.size());

       // session.getBasicRemote().sendText("欢迎连接");
    }
    /*
    客户端断开连接时调用的方法
     */
    @OnClose
    public void onClose(Session session,@PathParam("userId") Integer userId){
        log.info("连接断开,连接id:{},用户Id:{}",session.getId(),userId);
    }
    /*
    客户端发送消息时调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session,@PathParam("userId") Integer userId) throws IOException {
        log.info("收到客户端发来的消息:{},连接id:{},用户Id:{}",message,session.getId(),userId);
        session.getBasicRemote().sendText("收到");
    }
    /*
    如果上述3个方法,在执行代码功能的过程中,出现异常,可以调用异常处理
     */
    @OnError
    public void onError(Throwable error, Session session,@PathParam("userId") Integer userId){
        log.error("通信过程异常:{},连接id:{},用户Id:{}",error ,session.getId(),userId);
    }

    //给用户推送消息

    /**
     * @param userId 目标用户id
     * @param msg 发送消息数据 如果是测试功能发送String没问题,如果项目推送的数据不一定是字符串,可能是Object数据
     *            封装类型,考虑扩展的话
     */
    public void pushMsg(Integer userId, Object msg) {
        //解析入参消息的过程
        String textMsg="";
        if(msg instanceof String){
            textMsg= (String) msg;
        }else{
            textMsg= JSON.toJSONString(msg);
        }
        //1.从SESSIONS拿到用户连接对象,不一定能拿到 有可能已经下线了
        Session session = SESSIONS.get(userId);
        //判断有没有
        if (session!=null){
            try {
                //2.将字符串消息推送
                session.getBasicRemote().sendText(textMsg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            log.error("用户已下线,无法推送消息,用户id:{}",userId);
        }
    }
}