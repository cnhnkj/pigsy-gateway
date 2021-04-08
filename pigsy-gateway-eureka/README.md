# Eureka的集群环境部署说明


### 伪集群部署

本地的项目是一个伪集群部署(部署在一台机器，使用两个不同的端口)

两个不同的端口，分别默认写到了对应的两个配置文件中(`appcalition-node-1.yml`和`application-node-2.yml`)


### 启动命令

```
nohup java -jar target/pigsy-gateway-eureka.jar --spring.profiles.active=node-1 > eureka-node-1.log 2>&1 &
nohup java -jar target/pigsy-gateway-eureka.jar --spring.profiles.active=node-2 > eureka-node-2.log 2>&1 & 
```

访问web页面地址 `http://127.0.0.1:11001/` 可以查看对应的`eureka`的web页面


### 高可用集群部署

通过docker启动相关的项目

