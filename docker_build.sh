set -eu
docker run -it --rm --name maven-compiler -v maven-repo:/root/.m2 -v $(pwd):/openai-api-server -w /openai-api-server maven:3.9.1-sapmachine-17 mvn clean package -U -Dmaven.test.skip=true
docker build --build-arg PROJECT_VERSION=$1 -t coodex/openai-api-server:$1 .
# docker save coodex/openai-api-server:$1 > image-coodex/openai-api-server-$1.tar