#!/bin/bash

# Check if version param was passed
if [[ ! ("$#" == 1)]]; then
    echo 'Please pass a version number as parameter'
    exit 1
fi

export VERSION=$1
export NAME_PREFIX=GateIn-$VERSION
export NAME_EAP=$NAME_PREFIX-jboss-eap
export NAME_TC7=$NAME_PREFIX-tomcat7
export NAME_JETTY=$NAME_PREFIX-jetty

echo 'Uploading server bundles...'
rsync -rv --protocol=28 ./upload/server-bundles/ gatein@filemgmt.jboss.org:/downloads_htdocs/gatein/Releases/Portal/

echo
echo
echo 'Validate following URLs: for server bundles'
echo
echo "http://downloads.jboss.org/gatein/Releases/Portal/$VERSION/$NAME_EAP.zip"
echo "http://downloads.jboss.org/gatein/Releases/Portal/$VERSION/$NAME_TC7.zip"
#echo "http://downloads.jboss.org/gatein/Releases/Portal/$VERSION/$NAME_JETTY.zip"



