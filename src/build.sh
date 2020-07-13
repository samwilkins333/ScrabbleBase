#!/bin/bash
cd ./out || exit
echo "Removing stale version..."
rm ScrabbleBase.jar
echo "Building from sources..."
jar cfv ScrabbleBase.jar .
echo "Done."