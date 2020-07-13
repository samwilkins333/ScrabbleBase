#!/bin/bash

echo "Building from..."
pwd
echo

echo "Removing stale version..."
rm -rf out/ScrabbleBase/*
rm out/ScrabbleBase.jar
echo "Done."
echo

echo "Recompiling from sources..."
cp -r ScrabbleBase/* out/ScrabbleBase
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