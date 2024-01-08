docker build -t store:v1.0 .
docker rm -f store
docker run -it -d --network=host --name=store store:v1.0
docker logs -f store

