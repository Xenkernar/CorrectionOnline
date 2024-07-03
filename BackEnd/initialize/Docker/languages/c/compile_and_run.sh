#!/bin/bash
gcc -o output $(find "${SRC_DIR}" -name '*.c') -I "${SRC_DIR}" -lm
while true; do
  echo "9C1BF212D8B112395B4686349953273C"
  read -t ${TIMEOUT} yn
  if [ -z "$yn" ]; then
    exit
  fi
  case $yn in
    [Yy]* )
        output=$(./output)
        echo "$output"
        ;;
    [Nn]* )
        exit
        ;;
    * )
        ;;
  esac
done

