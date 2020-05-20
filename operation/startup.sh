#!/bin/bash

function enter_root_dir() {
	THIS="$0"
	THIS=`dirname "$THIS"`
	cd $THIS/../
	THIS="`pwd`"
	echo "enter : `pwd`" 1>&2
}

OLD_DIR="`pwd`"
enter_root_dir

cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

build_class_path() {
	CLASSPATH="$THIS/conf"
	#add jar in root dir
	for f in $THIS/*.jar; do
		CLASSPATH=$CLASSPATH:$f
	done
	#add jars in lib dir
	for f in $THIS/lib/*.jar; do
		CLASSPATH=$CLASSPATH:$f
	done
	#add develop classes to classpath
	if [ -d "$THIS/build" ]; then
	  CLASSPATH=${CLASSPATH}:$THIS/build
	fi

	if [ -d "$THIS/bin" ]; then
	  CLASSPATH=${CLASSPATH}:$THIS/bin
	fi
	
	# cygwin path translation
	if $cygwin; then
	  CLASSPATH=`cygpath -p -w "$CLASSPATH"`
	fi
}

CLASS=com.jerryshao.apinet.cluster.starter.ApinetMonitor
build_class_path

echo "CLASS: $CLASS"
echo "CLASSPATH: $CLASSPATH"

#parse the server settings
. $EXECUTORS_DIR/parse_server_settings.sh
echo "Startup the apiserver with param1=$PARAM2"
nohup java -cp $CLASSPATH $CLASS $PARAM2 >> $THIS/logs/apiserver.log 2>&1 &
