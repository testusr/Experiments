
installing influxdb and grafana:

docker run -d -p 8083:8083 -p 8086:8086 --expose 8090 --expose 8099 --name influxdb influxdb
run -d -p 3000:3000 --link influxdb:influxdb --name grafana grafana/grafana

logon to grafana
admin/admin

start insluxdb cient in container
docker exec -it influxdb influx

sources
http://www.fasterj.com/articles/gcnotifs.shtml
https://www.baeldung.com/java-influxdb