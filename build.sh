#!/bin/bash

echo "Building from..."
pwd
echo

echo "Removing stale artifacts..."
rm -rf out
mkdir out out/ScrabbleBase
echo "Done."
echo

echo "Recompiling from sources..."
cp -r src/ScrabbleBase/* out/ScrabbleBase
find out/ScrabbleBase -name "*.java" | xargs javac
find out/ScrabbleBase -name "*.java" | xargs rm
echo "Done."
echo

echo "Building .jar..."
cd out || exit
jar cfv ScrabbleBase.jar .
echo "Done."
echo

echo "Build complete."