# Spirit-go


```shell
# 构建镜像
docker-compose build 

# 保存镜像
docker save -o spirit-go-1.0.0.tar spirit-go:1.0.0

# 加载镜像
docker load -i spirit-go-latest.tar

# 启动镜像
docker compose up -d

# 进入容器
docker exec -it spirit-go sh
```
