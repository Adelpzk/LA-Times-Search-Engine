#!/bin/bash
# Compile the Java code
javac -d out -cp src src/IndexEngine/*.java src/utilities/*.java



# Run the application
java -Xmx8g -cp "out:out/resources" IndexEngine.IndexEngine Data/latimes.gz Data/latimes-index
