docker build -t order:v1.0 .
docker rm -f order
docker run -it -d --network=host --name=order order:v1.0
docker logs -f order

