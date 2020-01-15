export version=1.0
export release=1

.PHONY: kafka-ims-java-rpm kafka-ims-rest-server-rpm kafka-ims-uber-client-rpm 

kafka-ims-java-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-java-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-java-${version}-${release}.noarch.rpm: kafka-ims—java-rpmbuild
	docker run --rm kafka-ims-java-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-java-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—java-rpmbuild: \
  kafka-ims-java-rpmbuild.spec \
  Dockerfile-java-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-java-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-java-rpmbuild --progress=plain .

kafka-ims-rest-server-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm: kafka-ims—rest-server-rpmbuild
	docker run --rm kafka-ims-rest-server-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—rest-server-rpmbuild: \
  kafka-ims-rest-server-rpmbuild.spec \
  Dockerfile-rest-server-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-rest-server-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-rest-server-rpmbuild --progress=plain . 

kafka-ims-uber-client-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-uber-client-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-uber-client-${version}-${release}.noarch.rpm: kafka-ims—uber-client-rpmbuild
	docker run --rm kafka-ims-uber-client-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-uber-client-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—uber-client-rpmbuild: \
  kafka-ims-uber-client-rpmbuild.spec \
  Dockerfile-uber-client-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-uber-client-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-uber-client-rpmbuild --progress=plain .

server-rpm: kafka-ims-java-rpm kafka-ims-rest-server-rpm 

client-rpm: kafka-ims-uber-client-rpm 

rpm: server-rpm client-rpm

deploy: rpm
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-java-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-uber-client-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/

clean:
	docker rmi -f kafka-ims-server-rpmbuild kafka-ims-rest-server-rpmbuild kafka-ims-client-rpmbuild || exit 0
	rm -f ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-*.rpm 
