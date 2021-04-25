# Pigsy-gateway-auth详细说明


## 核心用途

- 提供接口用于登录之后，生成对应的token给前端
- 提供接口用于校验用户端前端传入的token的有效性
- 提供接口用于校验内部系统端的jwt的有效性，jwt的签名算法采用`RSA256`的算法，并且相关私钥和公钥配置在配置文件中


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


- 通过`/internal/auth/jwt/sign/create` 来根据`dingId`来生成对应的jwt的`sign`

#### 请求命令

```
curl -X 'POST' \
  'http://127.0.0.1:12001/internal/auth/jwt/sign/create?dingId=12345' \
  -H 'accept: */*' \
  -d ''
```

#### 返回结果

```
{
  "code": 0,
  "msg": "success",
  "data": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJkaW5nSWQiOiIxMjM0NSIsImlzcyI6InBpZ3N5LWF1dGgiLCJleHAiOjE2MTkyMzUxNDUsImlhdCI6MTYxOTE0ODc0NX0.ipe8DxoqttuAQ5D2NngCfXVw4PHO0aV1flLmT9afcxSEsmh0vaGUzTwKLWrbb3p8CPqbPC8K4JoKGeWlr6dD5iIR-DUnAp7fIaWHtIiAvPIIWS7kiv527F0Jrerv9phhVkDcZ2h58nxBg4Ta7r61XZ3ndlDJk9cbZiXwjP8xKbRvN-va-VUqaQKYuFFb0cDWc9QSVEf6CGH_y2vMJQLv2xwdFp4GO9obdpiH8mcpQFeNEOzJFGDDkPSEdrMJoXQRMhGJgMm05RmYJJWX9wq0EHJ5-IGkJQsgu-ruIbdZU8V6rJtg1Twf0Culg4u8GF2PI-oUlXidYxhOkKUJpYN8uA"
}
```

- 通过`/internal/auth/jwt/check` 来校验`sign`，并根据`dingId`查得用户信息进行返回

#### 请求数据

```
curl -X 'POST' \
  'http://127.0.0.1:12001/internal/auth/jwt/check?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJkaW5nSWQiOiIxMjM0NSIsImlzcyI6InBpZ3N5LWF1dGgiLCJleHAiOjE2MTkyMzUxNDUsImlhdCI6MTYxOTE0ODc0NX0.ipe8DxoqttuAQ5D2NngCfXVw4PHO0aV1flLmT9afcxSEsmh0vaGUzTwKLWrbb3p8CPqbPC8K4JoKGeWlr6dD5iIR-DUnAp7fIaWHtIiAvPIIWS7kiv527F0Jrerv9phhVkDcZ2h58nxBg4Ta7r61XZ3ndlDJk9cbZiXwjP8xKbRvN-va-VUqaQKYuFFb0cDWc9QSVEf6CGH_y2vMJQLv2xwdFp4GO9obdpiH8mcpQFeNEOzJFGDDkPSEdrMJoXQRMhGJgMm05RmYJJWX9wq0EHJ5-IGkJQsgu-ruIbdZU8V6rJtg1Twf0Culg4u8GF2PI-oUlXidYxhOkKUJpYN8uA' \
  -H 'accept: */*' \
  -d ''
```

#### 返回结果

```
{
  "code": 0,
  "msg": "success",
  "data": {
    "dingId": "12345",
    "name": "oaGeFi",
    "email": "oaGeFi@cnhnkj.com",
    "phone": "5138085889738",
    "role": "admin",
    "department": "tech"
  }
}
```