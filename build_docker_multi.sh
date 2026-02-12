TAG=$1
docker buildx build --platform linux/amd64,linux/arm64 --tag $TAG --push .