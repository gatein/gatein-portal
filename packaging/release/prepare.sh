#!/bin/bash

# Check if version param was passed
if [[ ! ("$#" == 1)]]; then
    echo 'Please pass a version number as parameter'
    exit 1
fi

export VERSION=$1
export NAME_PREFIX=GateIn-$VERSION
export NAME_EAP=$NAME_PREFIX-jboss-eap
export NAME_TC6=$NAME_PREFIX-tomcat6
export NAME_TC7=$NAME_PREFIX-tomcat7
export NAME_JETTY=$NAME_PREFIX-jetty


# Cleanup previous stuff
echo 'Removing old stuff...'
rm -rf upload
rm -rf test

# Copy all
mkdir ./test
mkdir ./test/server-bundles
echo 'Copying all packaged servers into ./test/server-bundles/'
cp -R ../jboss/pkg/target/jboss-eap ./test/server-bundles/$NAME_EAP
cp -R ../tomcat/tomcat7/target/tomcat ./test/server-bundles/$NAME_TC7
#cp -R ../jetty/pkg/target/jetty ./test/server-bundles/$NAME_JETTY

#  Zip all servers
mkdir upload
mkdir upload/server-bundles
mkdir upload/server-bundles/$VERSION
echo 'Creating server bundles in ./upload/server-bundles/'
cd ./test/server-bundles
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_EAP.zip $NAME_EAP -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_TC7.zip $NAME_TC7 -x "*.DS_Store"
#zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_JETTY.zip $NAME_JETTY -x "*.DS_Store"
cd ../../

# Generate checksums
# We don't need those anymore, they are calculated and added to the download component.
#echo 'Generating server bundles checksums in ./upload/server-bundles/'
#shasum ./upload/server-bundles/$VERSION/$NAME_EAP.zip > ./upload/server-bundles/$VERSION/$NAME_EAP.zip.sha
#md5 ./upload/server-bundles/$VERSION/$NAME_EAP.zip > ./upload/server-bundles/$VERSION/$NAME_EAP.zip.md5
#shasum ./upload/server-bundles/$VERSION/$NAME_TC7.zip > ./upload/server-bundles/$VERSION/$NAME_TC7.zip.sha
#md5 ./upload/server-bundles/$VERSION/$NAME_TC7.zip > ./upload/server-bundles/$VERSION/$NAME_TC7.zip.md5
#shasum ./upload/server-bundles/$VERSION/$NAME_JETTY.zip > ./upload/server-bundles/$VERSION/$NAME_JETTY.zip.sha
#md5 ./upload/server-bundles/$VERSION/$NAME_JETTY.zip > ./upload/server-bundles/$VERSION/$NAME_JETTY.zip.md5

