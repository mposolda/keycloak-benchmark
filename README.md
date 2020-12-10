# keycloak-benchmark

cd /home/mposolda/tmp/keycloak-9.0.10-SNAPSHOT/bin
./jboss-cli.sh --command="module add --name=org.keycloak.keycloak-benchmark --resources=/home/mposolda/IdeaProjects/keycloak-benchmark/dataset/target/keycloak-benchmark-dataset-0.1-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-common,org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,org.keycloak.keycloak-model-infinispan,javax.ws.rs.api,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging,org.infinispan,org.infinispan.commons,org.infinispan.client.hotrod,org.infinispan.persistence.remote"


IN STANDALONE.XML

<provider>module:org.keycloak.keycloak-benchmark</provider>


REQUEST:

http://localhost:8080/auth/realms/master/dataset/create-realms?count=100