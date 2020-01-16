
echo "build push stress-node"
cd nodejs
docker build . -t registry.cn-beijing.aliyuncs.com/lemonbox/stress-node
docker push registry.cn-beijing.aliyuncs.com/lemonbox/stress-node
echo "build push sucess"

cd ..

echo "build push stress-java"
cd java
docker build . -t registry.cn-beijing.aliyuncs.com/lemonbox/stress-java
docker push registry.cn-beijing.aliyuncs.com/lemonbox/stress-java
echo "build push sucess"