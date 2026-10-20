#!/bin/bash
set -e

source "buildScript/init/env.sh"
ENV_NB4A=1
source "buildScript/lib/core/get_source_env.sh"
pushd ..

####

if [ ! -d "sing-box" ]; then
  git clone --no-checkout https://github.com/SagerNet/sing-box.git
fi
pushd sing-box
git remote set-url origin https://github.com/SagerNet/sing-box.git
git fetch --tags origin
git checkout --force "$COMMIT_SING_BOX"
git clean -fdx
SING_BOX_PATCH="$SRC_ROOT/buildScript/lib/core/sing-box-neko-turn.patch"
if git apply --reverse --check "$SING_BOX_PATCH" >/dev/null 2>&1; then
  echo "sing-box NekoBox TURN patch is already applied"
elif git apply --check "$SING_BOX_PATCH" >/dev/null 2>&1; then
  git apply "$SING_BOX_PATCH"
else
  echo "cannot apply sing-box NekoBox TURN patch" >&2
  exit 1
fi
popd

####

if [ ! -d "libneko" ]; then
  git clone --no-checkout https://github.com/MatsuriDayo/libneko.git
fi
pushd libneko
git checkout "$COMMIT_LIBNEKO"
popd

####

popd
