FROM docker.io/bitnami/wildfly:23.0.0

# ADD target/demo-0.0.1-SNAPSHOT.war /opt/bitnami/wildfly/standalone/deployments/

RUN /opt/bitnami/wildfly/bin/add-user.sh admin password --silent

CMD ["/opt/bitnami/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]