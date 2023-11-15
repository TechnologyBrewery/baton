#!/bin/bash

#This script requires two input parameter, 1)target release version 2)next development version.
#To run the script, enter a command like this: ./release-baton.sh <target-release-version> <next-development-version>

#After the script is executed, navigate to github to create a PR to merge your release branch
#and validate that the released version of baton-maven-plugin may be found in Maven Central

echo "////////////////////////////////////////////"
echo "//"
echo "// Use this script to run a release for Baton"
echo "//"
echo "////////////////////////////////////////////"

echo "//////////// Check out a release branch ////////////"
git checkout -b $1-release

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to check out release branch!'; exit 1
fi

echo "/////////// Check there are no uncommitted local changes ///////////"
mvn scm:check-local-modification

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Uncommitted local changes detected!'; exit 1
fi

echo "/////////// Update POM versions to the target release version ///////////"
mvn versions:set -DnewVersion=$1 -DgenerateBackupPoms=false

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to update POM versions to the target release version'; exit 1
fi

echo "///////////  Rebuild Baton modules (1) ///////////"
mvn clean install -Pbootstrap -Dmaven.build.cache.enabled=false && mvn clean install -pl :example-migration-configuration -Dmaven.build.cache.enabled=false && mvn clean initialize -Dmaven.build.cache.enabled=false

if [[ "$?" -ne 0 ]] ; then
  echo 'Rebuilding Baton (1) failed!'; exit 1
fi

echo "///////////  Commit all changes that reflect the target release version and create a tag ///////////"
mvn scm:checkin -Dmessage=":memo: Prepare release baton-$1"
mvn scm:tag -Dtag=baton-$1

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to commit changes and create a tag!'; exit 1
fi

echo "/////////// Deploy Baton to Maven Central ///////////"
mvn deploy -P ossrh-release -Dmaven.build.cache.enabled=false

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to deploy Baton to Maven Central!'; exit 1
fi

echo "/////////// Update POM versions to the next development version ///////////"
mvn versions:set -DnewVersion=$2 -DgenerateBackupPoms=false

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to update POM versions to the next development version!'; exit 1
fi

echo "/////////// Rebuild Baton modules (2) ///////////"
mvn clean install -Pbootstrap && mvn clean install -pl :example-migration-configuration && mvn clean initialize

if [[ "$?" -ne 0 ]] ; then
  echo 'Rebuilding Baton (2) failed!'; exit 1
fi

echo "/////////// Commit all changes that reflect the next development iteration///////////"
mvn scm:checkin -Dmessage="Prepare for next development iteration"

if [[ "$?" -ne 0 ]] ; then
  echo 'Process failed! Unable to commit changes for the next development iteration!'; exit 1
fi