#!/bin/bash

# constants
SRC_DIR="src"
MANIFEST="manifest.txt"
JAR_NAME="haco.jar"

# goes to source directory
cd $SRC_DIR

# finds and compiles all java files
find . -name "*.java" > sources.txt
javac @sources.txt

# back to project root
cd ..

# creates jar including manifest
jar cfm $JAR_NAME $SRC_DIR/$MANIFEST -C $SRC_DIR .

# gives execution permission to haco.sh and haco-debug.sh
chmod +x haco.sh
chmod +x haco-debug.sh