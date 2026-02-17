#!/usr/bin/env bash
set -e

mkdir -p dist out

echo "Compiling..."
javac -d out @sources.txt

echo "Creating JAR..."
cat > manifest.txt <<EOF
Main-Class: Main
EOF

jar cfm dist/HotelBooker.jar manifest.txt -C out .
rm -f manifest.txt

echo "Done! Run with: java -jar dist/HotelBooker.jar"
