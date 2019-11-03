#! /bin/bash

./gradlew javadoc
cp -r build/docs/javadoc docs/
git add docs/javadoc
