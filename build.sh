#!/bin/bash

# Definisci la directory dei sorgenti e il nome del file JAR
SRC_DIR="src"
BUILD_DIR="build"
MANIFEST="manifest.txt"
JAR_NAME="haco.jar"

# Crea la directory di build se non esiste
mkdir -p $BUILD_DIR

# Naviga nella directory dei sorgenti
cd $SRC_DIR

# Trova e compila tutti i file Java
find . -name "*.java" > sources.txt
javac @sources.txt

# Torna alla radice del progetto
cd ..

# Crea il file JAR includendo il manifest
jar cfm $JAR_NAME $SRC_DIR/$MANIFEST -C $SRC_DIR .