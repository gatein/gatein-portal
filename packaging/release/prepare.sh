#!/bin/bash

# Check if version param was passed
if [[ ! ("$#" == 1)]]; then 
    echo 'Please pass a version number as parameter'
    exit 1
fi

export VERSION=$1
export NAME_PREFIX=GateIn-$VERSION
export NAME_AS5=$NAME_PREFIX-jbossas5
export NAME_AS6=$NAME_PREFIX-jbossas6
export NAME_AS7=$NAME_PREFIX-jbossas7
export NAME_TC6=$NAME_PREFIX-tomcat6
export NAME_TC7=$NAME_PREFIX-tomcat7
export NAME_JETTY=$NAME_PREFIX-jetty


# Cleanup previous stuff
echo 'Removing old stuff...'
rm -rf upload
rm -rf test

# Copy documentation
mkdir ./upload
mkdir ./upload/docs
mkdir ./upload/docs/$VERSION
echo 'Copying documentation into ./upload/docs/'
mkdir ./upload/docs/$VERSION/user-guide
cp -R ../../docs/user-guide/target/docbook/publish/en-US ./upload/docs/$VERSION/user-guide/
mkdir ./upload/docs/$VERSION/reference-guide
cp -R ../../docs/reference-guide/target/docbook/publish/en-US ./upload/docs/$VERSION/reference-guide/

# Copy all 
mkdir ./test
mkdir ./test/server-bundles
echo 'Copying all packaged servers into ./test/server-bundles/'
cp -R ../jboss-as5/pkg/target/jboss ./test/server-bundles/$NAME_AS5
cp -R ../jboss-as6/pkg/target/jboss ./test/server-bundles/$NAME_AS6
cp -R ../jboss-as7/pkg/target/jboss ./test/server-bundles/$NAME_AS7
cp -R ../tomcat/pkg/tc6/target/tomcat6 ./test/server-bundles/$NAME_TC6
cp -R ../tomcat/pkg/tc7/target/tomcat7 ./test/server-bundles/$NAME_TC7
cp -R ../jetty/pkg/target/jetty ./test/server-bundles/$NAME_JETTY

#  Zip all servers
mkdir upload/server-bundles
mkdir upload/server-bundles/$VERSION
echo 'Creating server bundles in ./upload/server-bundles/'
cd ./test/server-bundles
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_AS5.zip $NAME_AS5 -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_AS6.zip $NAME_AS6 -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_AS7.zip $NAME_AS7 -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_TC6.zip $NAME_TC6 -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_TC7.zip $NAME_TC7 -x "*.DS_Store"
zip -r -q ./../../upload/server-bundles/$VERSION/$NAME_JETTY.zip $NAME_JETTY -x "*.DS_Store"
cd ../../

# Generate checksums
echo 'Generating server bundles checksums in ./upload/server-bundles/'
shasum ./upload/server-bundles/$VERSION/$NAME_AS5.zip > ./upload/server-bundles/$VERSION/$NAME_AS5.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_AS5.zip > ./upload/server-bundles/$VERSION/$NAME_AS5.zip.md5
shasum ./upload/server-bundles/$VERSION/$NAME_AS6.zip > ./upload/server-bundles/$VERSION/$NAME_AS6.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_AS6.zip > ./upload/server-bundles/$VERSION/$NAME_AS6.zip.md5
shasum ./upload/server-bundles/$VERSION/$NAME_AS7.zip > ./upload/server-bundles/$VERSION/$NAME_AS7.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_AS7.zip > ./upload/server-bundles/$VERSION/$NAME_AS7.zip.md5
shasum ./upload/server-bundles/$VERSION/$NAME_TC6.zip > ./upload/server-bundles/$VERSION/$NAME_TC6.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_TC6.zip > ./upload/server-bundles/$VERSION/$NAME_TC6.zip.md5
shasum ./upload/server-bundles/$VERSION/$NAME_TC7.zip > ./upload/server-bundles/$VERSION/$NAME_TC7.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_TC7.zip > ./upload/server-bundles/$VERSION/$NAME_TC7.zip.md5
shasum ./upload/server-bundles/$VERSION/$NAME_JETTY.zip > ./upload/server-bundles/$VERSION/$NAME_JETTY.zip.sha
md5 ./upload/server-bundles/$VERSION/$NAME_JETTY.zip > ./upload/server-bundles/$VERSION/$NAME_JETTY.zip.md5




