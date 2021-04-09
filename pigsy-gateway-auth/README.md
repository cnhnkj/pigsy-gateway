# Pigsy-gateway-auth详细说明


## 核心用途

- 提供接口用于登录之后，生成对应的token给前端
- 提供接口用于校验用户端前端传入的token的有效性
- 提供接口用于校验内部系统端的token的有效性


## 工作流程

- 启动的时候注册到`eureka`
- `pigsy-gateway-core`模块对于需要验证token有效性的url先转发到该服务，根据该服务的返回进行后续逻辑处理
- 如果验证成功, 则把`token`转换成`userId`，然后传递到下层服务
- 如果验证失败，`pigsy-gateway-core`直接返回对应的错误码，要求前端重新登录


## 项目依赖

- `eureka`
- `redis`

## 启动命令

```
nohup java -jar target/pigsy-gateway-auth.jar --spring.profiles.active=test > pigsy-gateway-auth.log 2>&1 & 
```

- 访问web页面地址 `http://127.0.0.1:11001/` 可以查看对应的`eureka`的web页面
- 通过点击对应服务的链接，可以跳转到对应的`swagger-ui`页面查看对应的接口说明
- 通过`/internal/auth/token/create`来根据`userId`来获得对应的`token`

#### 请求命令
```
curl -X 'POST' \
  'http://10.10.23.254:12001/internal/auth/token/create?userId=123456&username=cnhnkj' \
  -H 'accept: */*' \
  -d ''
```

#### 返回结果
```
{
  "code": 0,
  "msg": "success",
  "data": "3335febd-63e9-4c36-9f1b-4e83eb59f448"
}
```

- 通过`/internal/auth/userInfo/by/token`来根据`token`来验证用户的登录的有效性，并且获取`userId`

#### 请求命令

```
curl -X 'POST' \
  'http://10.10.23.254:12001/internal/auth/userInfo/by/token?token=3335febd-63e9-4c36-9f1b-4e83eb59f448' \
  -H 'accept: */*' \
  -d ''
```


### 返回结果
```
{
  "code": 0,
  "msg": "success",
  "data": {
    "userId": 123456,
    "username": "cnhnkj"
  }
}
```