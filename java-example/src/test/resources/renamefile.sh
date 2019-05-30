#!/bin/bash

# Rename file,  first argument is filePath, second argument is filename, and third argument is filename to rename it to.

FILE_PATH=$1
FILE_NAME=$2
FILE_NAME_TO=$3

sudo mv $FILE_PATH/$FILE_NAME  $FILE_PATH/$FILE_NAME_TO
