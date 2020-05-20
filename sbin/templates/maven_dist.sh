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

#1. 首先准备所依赖的jar包
LIBRARY_DIR=$DEPLOY_TARGET/lib
mkdir -p $LIBRARY_DIR
mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:copy-dependencies -DoutputDirectory=./lib
cp ./lib/*.jar $LIBRARY_DIR
rm -rf ./lib

#2. package
mvn package
cp target/*.jar $DEPLOY_TARGET

#3. 准备配置文件
cp -rf target/conf $DEPLOY_TARGET

#4. copy运维脚本
operation_scripts="operation.sh status.sh"
for operation_script in $operation_scripts; do
	cp $operation_script $DEPLOY_TARGET
done
cp -rf operation $DEPLOY_TARGET
cp -rf logs $DEPLOY_TARGET

echo "finish!!!"
