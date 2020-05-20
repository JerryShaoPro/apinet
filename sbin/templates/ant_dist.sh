#!/bin/bash

DIR_NAME=`dirname $0`
cd $DIR_NAME
THIS=`pwd`

DEPLOY_TARGET=$THIS/deploy_dist

#remove the old deploy version
if [ -d $DEPLOY_TARGET ]; then
	echo "remove the old deploy"
	rm -rf $DEPLOY_TARGET
fi

#create the deploy directory
mkdir -p $DEPLOY_TARGET

ant dist

echo "finish!!!"