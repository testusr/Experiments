docker run -d -p 8083:8083 -p 8086:8086 --expose 8090 --expose 8099 --name influxdb influxdb:latest
docker run -d -p 3000:3000 --link influxdb:influxdb --name grafana grafana/grafana
