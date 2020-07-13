#!/bin/bash

echo "Building from..."
pwd
echo

echo "Removing stale version..."
rm out/ScrabbleBase/*.class
rm out/ScrabbleBase.jar
echo "Done."
echo

echo "Recompiling from sources..."
javac ScrabbleBase/*.java
mv ScrabbleBase/*.class out/ScrabbleBase
echo "Done."
echo

echo "Building .jar..."
cd out || exit
jar cfv ScrabbleBase.jar .
echo "Done."
echo

echo "Build complete."