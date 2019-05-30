PID=`ps -ef | grep java | awk '{ print $2 }'`
kill -9 $PID