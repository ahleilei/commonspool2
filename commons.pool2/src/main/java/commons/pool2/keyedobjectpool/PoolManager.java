package commons.pool2.keyedobjectpool;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import commons.pool2.SocketClient;
/**
 * 
 * 项目名称 : commons.pool2
 * 创建日期 : 2018年1月22日
 * 类  描  述 : 池管理类
 * 修改历史 : 
 *     1. [2018年1月22日]创建文件 by ziqiang.zhang
 */
public class PoolManager {
    
    Logger log = LoggerFactory.getLogger(PoolManager.class);
    
    GenericKeyedObjectPool<String, SocketClient> pool;
    
    public void init(){
        
        KeyedPooledObjectFactory<String, SocketClient> factory = new SokcetKeyedPoolFactory();
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        // 池对象放入对象池的方式 true 队头 false 队尾
        config.setLifo(true);
        // 是否执行空闲链接检测 默认false
        config.setTestWhileIdle(true);
        // 空闲链接检测周期（毫秒）
        config.setTimeBetweenEvictionRunsMillis(10000);
        // 每次检测的空闲链接个数
        config.setNumTestsPerEvictionRun(1);
        // 最大连接数
        config.setMaxTotal(100);
        // 每个key对应的最大连接数
        config.setMaxTotalPerKey(1);
        // 每个key对应的最小空闲连接
        config.setMinIdlePerKey(0);
        // 每个key对应的最大空闲连接
        config.setMaxIdlePerKey(1);
        // 最大等待时间（毫秒） 默认－1 一直等待
        config.setMaxWaitMillis(-1);
        // 是否阻塞等待
        config.setBlockWhenExhausted(true);
        // 是否在创建池对象时执行validate  默认false
        config.setTestOnCreate(false);
        // 是否在获取池对象时执行validate 默认false
        config.setTestOnBorrow(false);
        // 是否在归还池对象时执行validate 默认false
        config.setTestOnReturn(false);

        // 空闲最小时间，超时清除，不清除
        config.setMinEvictableIdleTimeMillis(3000000);
        // 空闲最小时间，保留最小空闲
        config.setSoftMinEvictableIdleTimeMillis(-1);

        pool = new GenericKeyedObjectPool<>(factory, config);
    }
    /**
     * 
     * 发送消息
     * @param message
     * @return
     * 2018年1月22日 by ziqiang.zhang
     */
    public String send(String message){
        
        if(pool == null){
            // 初始化连接池
            init();
        }
        
        String resp = null;
        
        SocketClient client = null;
        
        String key = "1234";
        try {
            
            client = pool.borrowObject(key);
            
            if(client.getSecretKey()==null||"".equals(client.getSecretKey())){
                
                client.send("103");
                
                String secretKey = client.receive();
                
                client.setSecretKey(secretKey);
            }
            
            if(client.getSecretKey()!=null){
                
                client.send(message);
                
                resp = client.receive();
            }
            
        } catch (Exception e) {
            
            log.error("PoolManager.send",e);
        }finally{
            
            if(client != null){
                pool.returnObject(key,client);
            }
        }
        
        return resp;
    }
    
    public static void main(String[] args) {
        
        PoolManager manager = new PoolManager();        
        
        String resp = manager.send("200");
        
        System.out.println("main " + resp);
        
    }

}
