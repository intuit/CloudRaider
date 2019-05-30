PID=`ps -ef | grep $1 | awk '{ print $2 }'`
sudo kill -9 $PID