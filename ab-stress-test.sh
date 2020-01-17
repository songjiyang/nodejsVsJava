now=`date +%Y%m%d%H%M%S`
c=2000
n=10000
warmC=10
warmN=100
dir=stress_record/$c_$n_$now
mkdir -p $dir
echo "###########################warm up"

ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/hello
ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/compute
ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/listQuestionaire

ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/hello
ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/compute
ab -c $warmC -n $warmN ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/listQuestionaire




echo "###########################stress test"
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/hello > hello_node.txt
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/hello > hello_java.txt
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/compute > compute_node.txt
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/compute > compute_java.txt
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/node/api/v1/listQuestionaire > listQuestionaire_node.txt
ab -c $c -n $n ec2-52-83-66-128.cn-northwest-1.compute.amazonaws.com.cn/java/api/v1/listQuestionaire > listQuestionaire_java.txt

grep -r  "Requests per second" *.txt
mv *.txt $dir