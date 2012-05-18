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

#Upload Docs
echo 'Uploading docs...'
rsync -rv --protocol=28 ./upload/docs/ gatein@filemgmt.jboss.org:/docs_htdocs/gatein/portal/
echo 
echo
echo 'Validate following URLs for documentation:'
echo
echo "http://docs.jboss.com/gatein/portal/$VERSION/user-guide/en-US/html/index.html"
echo "http://docs.jboss.com/gatein/portal/$VERSION/user-guide/en-US/html_single/index.html"
echo "http://docs.jboss.com/gatein/portal/$VERSION/user-guide/en-US/pdf/GateIn%20User%20Guide%20en.pdf"
echo "http://docs.jboss.com/gatein/portal/$VERSION/reference-guide/en-US/html/index.html"
echo "http://docs.jboss.com/gatein/portal/$VERSION/reference-guide/en-US/html_single/index.html"
echo "http://docs.jboss.com/gatein/portal/$VERSION/reference-guide/en-US/pdf/GateIn%20Reference%20Guide%20en.pdf"
echo
echo 

echo 'Uploading server bundles...'
rsync -rv --protocol=28 ./upload/server-bundles/ gatein@filemgmt.jboss.org:/downloads_htdocs/gatein/Releases/

echo
echo
echo 'Validate following URLs: for server bundles'
echo
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_AS5.zip"
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_AS6.zip"
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_AS7.zip"
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_TC6.zip"
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_TC7.zip"
echo "http://downloads.jboss.org/gatein/Releases/$VERSION/$NAME_JETTY.zip"



