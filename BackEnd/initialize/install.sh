#!/bin/bash
currentDir=$(cd $(dirname $0); pwd)




echo -e "
/usr/bin/mc alias set minio http://minio:9000 xenkernar xenkernar; \n
/usr/bin/mc mb minio/labreports; \n
/usr/bin/mc anonymous set public minio/labreports; \n
exit 0;
" | docker run -i --entrypoint=/bin/bash --name mc --network xenker-network  minio/mc


