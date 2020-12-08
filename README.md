# keycloak-benchmark

cd /home/mposolda/tmp/keycloak-12.0.0-SNAPSHOT/bin
./jboss-cli.sh --command="module add --name=org.keycloak.keycloak-benchmark --resources=/home/mposolda/IdeaProjects/keycloak-benchmark/dataset/target/keycloak-benchmark-dataset-0.1-SNAPSHOT.jar --dependencies=org.keycloak.keycloak-common,org.keycloak.keycloak-core,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,org.keycloak.keycloak-services,javax.ws.rs.api,org.jboss.resteasy.resteasy-jaxrs,org.jboss.logging"


IN STANDALONE.XML

<provider>module:org.keycloak.keycloak-benchmark</provider>


REQUEST:

http://localhost:8080/auth/realms/master/dataset?count=100&realm-prefix=realmmmek