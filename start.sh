sudo mvn install -DskipTests
sudo docker build -t highload.jar .
sudo docker network prune -f
sudo docker-compose up --force-recreate --remove-orphans
