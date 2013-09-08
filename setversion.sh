#!/bin/bash

if [ $# -ne 1 ]; then
  cat <<EOF
Usage:
  ./setversion.sh <new version>
EOF
  exit 1
fi

REPO_HOME=$(dirname $(readlink -e $0))
NEW_VERSION="$1"

sed -i 's/^version := .*$/version := "'"$NEW_VERSION"'"/' core/build.sbt
sed -i 's/^version := .*$/version := "'"$NEW_VERSION"'"/' jsoup/build.sbt
sed -i 's/^version := .*$/version := "'"$NEW_VERSION"'"/' json4s/build.sbt
sed -i 's/^version := .*$/version := "'"$NEW_VERSION"'"/' filedl/build.sbt

sed -i 's/^\(libraryDependencies.*\) % .*$/\1 % "'"$NEW_VERSION"'"/' README.md

