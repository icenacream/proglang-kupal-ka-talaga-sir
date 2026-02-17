#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

mkdir -p bin

echo "Compiling..."
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d bin @sources.txt

echo "Running..."
java -cp bin Main
