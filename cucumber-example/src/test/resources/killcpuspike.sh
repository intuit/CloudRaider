PID=`ps -ef | grep 'dd if'  | awk '{ print $2 }'`
echo $PID
sudo kill -9 $PID