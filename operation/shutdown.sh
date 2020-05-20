#!/bin/bash

echo "stoping the apiserver..."

function enter_root_dir() {
	THIS="$0"
	THIS=`dirname "$THIS"`
	cd $THIS/../
	THIS="`pwd`"
	echo "enter : `pwd`" 1>&2
}

OLD_DIR="`pwd`"
enter_root_dir

#parse the server settings
. $EXECUTORS_DIR/parse_server_settings.sh

(
sleep 2
echo "stop"
echo "exit"
sleep 2
) | telnet 127.0.0.1 $new_server_monitor_port

echo "the apiserver has been shut down"
