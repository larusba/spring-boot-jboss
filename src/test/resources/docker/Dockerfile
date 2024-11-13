FROM docker.io/bitnami/wildfly:23

RUN /opt/bitnami/wildfly/bin/add-user.sh admin password --silent

CMD ["/opt/bitnami/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]