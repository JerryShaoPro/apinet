#!/bin/bash
function enter_current_dir() {
	THIS="$0"
	THIS=`dirname "$THIS"`
	cd $THIS
	THIS="`pwd`"
	echo "enter : `pwd`" 1>&2
}

OLD_DIR="`pwd`"
enter_current_dir

export EXECUTORS_DIR="operation"

function get_status() {
	 STATUS=`bash status.sh simple`
     echo "$STATUS"
}

function show_info() {
	INFO=`bash status.sh detail`
	echo "$INFO"
}

function get_operation_action() {
	echo "start"
	echo "stop"
	echo "update"
	echo "info"
}

function update() {
	do_stop
	svn up
	do_start
}

function do_stop() {
	bash $EXECUTORS_DIR/shutdown.sh
}

function do_start() {
	bash $EXECUTORS_DIR/startup.sh $PARAM2 
}

CMD=$1
export PARAM2=$2

shift

if [ "$CMD" = "status" ]; then
	get_status
elif [ "$CMD" = "actions" ]; then
	get_operation_action
elif [ "$CMD" = "update" ]; then
	update
elif [ "$CMD" = "stop" ]; then
	do_stop
elif [ "$CMD" = "start" ]; then
	do_start
elif [ "$CMD" = "info" ]; then
	show_info
else
	echo "unknown command"
fi

cd $OLD_DIR
echo "return to `pwd`" 1>&2
