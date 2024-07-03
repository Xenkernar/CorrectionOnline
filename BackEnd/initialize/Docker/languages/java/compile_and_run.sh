#!/bin/bash
cd "${SRC_DIR}"
find . -name "*.java" -print | xargs javac
main_classes=$(grep -l 'main(String\[\] args)' *.java | sed -e 's/.java$//')
while true; do
  echo "9C1BF212D8B112395B4686349953273C"
  read -t ${TIMEOUT} yn
  if [ -z "$yn" ]; then
    exit
  fi
  case $yn in
    [Yy]* )
        output=$(java ${main_classes})
        echo "$output"
        ;;
    [Nn]* )
        exit
        ;;
    * )
        ;;
  esac
done