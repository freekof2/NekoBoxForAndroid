#!/bin/bash

set -e

DIR=app/src/main/assets/sing-box
rm -rf $DIR
mkdir -p $DIR
cd $DIR

####
echo "Downloading geoip.db..."
curl -fLSsO https://github.com/SagerNet/sing-geoip/releases/latest/download/geoip.db
curl -sI https://github.com/SagerNet/sing-geoip/releases/latest/download/geoip.db | grep -i 'location:' | sed -E 's/.*tag\/([^/]+)\/.*/\1/' | tr -d '\r\n' > geoip.version.txt
if [ ! -s geoip.version.txt ]; then
  echo -n "latest" > geoip.version.txt
fi
xz -9 geoip.db

####
echo "Downloading geosite.db..."
curl -fLSsO https://github.com/SagerNet/sing-geosite/releases/latest/download/geosite.db
curl -sI https://github.com/SagerNet/sing-geosite/releases/latest/download/geosite.db | grep -i 'location:' | sed -E 's/.*tag\/([^/]+)\/.*/\1/' | tr -d '\r\n' > geosite.version.txt
if [ ! -s geosite.version.txt ]; then
  echo -n "latest" > geosite.version.txt
fi
xz -9 geosite.db
