export version=1.0
export release=1

.PHONY: kafka-ims-server-rpm kafka-ims-rest-server-rpm kafka-ims-client-rpm 

kafka-ims-server-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-server-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-server-${version}-${release}.noarch.rpm: kafka-ims—server-rpmbuild
	docker run --rm kafka-ims-server-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-server-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—server-rpmbuild: \
  kafka-ims-server-rpmbuild.spec \
  Dockerfile-server-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-server-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-server-rpmbuild --progress=plain .

kafka-ims-rest-server-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm: kafka-ims—rest-server-rpmbuild
	docker run --rm kafka-ims-rest-server-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—rest-server-rpmbuild: \
  kafka-ims-rest-server-rpmbuild.spec \
  Dockerfile-rest-server-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-rest-server-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-rest-server-rpmbuild --progress=plain . 

kafka-ims-client-rpm: ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-client-${version}-${release}.noarch.rpm
${HOME}/rpmbuild/RPMS/noarch/kafka-ims-client-${version}-${release}.noarch.rpm: kafka-ims—client-rpmbuild
	docker run --rm kafka-ims-client-rpmbuild tar cf - rpmbuild/RPMS/noarch/kafka-ims-client-${version}-${release}.noarch.rpm | tar xvf - -C /Users/nex37045

kafka-ims—client-rpmbuild: \
  kafka-ims-client-rpmbuild.spec \
  Dockerfile-client-rpmbuild
	DOCKER_BUILDKIT=1 docker build -t kafka-ims-client-rpmbuild --build-arg version=${version} --build-arg release=${release} -f Dockerfile-client-rpmbuild --progress=plain .

server-rpm: kafka-ims-server-rpm kafka-ims-rest-server-rpm 

client-rpm: kafka-ims-client-rpm 

rpm: server-rpm client-rpm

deploy: rpm
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-server-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-rest-server-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/
	jfrog rt u --server-id=corp ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-client-${version}-${release}.noarch.rpm rpm-dim-kafka-ims-release-local/7/dev/

clean:
	docker rmi -f kafka-ims-server-rpmbuild kafka-ims-rest-server-rpmbuild kafka-ims-client-rpmbuild || exit 0
	rm -f ${HOME}/rpmbuild/RPMS/noarch/kafka-ims-*.rpm 
