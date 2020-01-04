#!/usr/bin/rpm
%define __jar_repack %{nil}
%define install_path /usr/share/java/kafka-rest

Name: kafka-ims-rest-client
Summary: IMS Plugin for Kafka REST Proxy 
Version: 1.0
Release: 1
Vendor: Upala Corporation
Packager: Manoj Murumkar <nex37045@adobe.com>
Group: Adobe/IDS/DIM
License: Copyright (c) 2019 Adobe. All Rights Reserved.
URL: https://git.corp.adobe.com/ids-big-data-platform/kafka-ims/README.md
Prefix: /usr/share/java/kafka-rest
BuildRoot: %{_tmppath}/%{name}-%{version}
Requires: java
BuildArch: noarch

%description
This plugin integrates Adobe IMS with Confluent Kafka REST Proxy. This has been built on Confluent 5.3.1 distribution. 
As long as the RestResourceExtension interface definition doesn't change, this plugin can be used with newer versions of REST Proxy.

%changelog
* Wed Jan 1 2020 Manoj Murumkar <nex37045@adobe.com> 1.0-0
- Initial RPM release

%prep
if [ "$RPM_BUILD_ROOT" != "/" ]; then
    rm -rf "$RPM_BUILD_ROOT"
fi

%build
cd -
export JAVA_HOME=/usr/lib/jvm/java
mvn -e package -DskipTests=true

%install
cd -
install -d "$RPM_BUILD_ROOT"%{install_path}
install kafka-ims-rest/target/kafka-ims-rest-%{version}-*jar-with-dependencies.jar "${RPM_BUILD_ROOT}"%{install_path}/%{name}-%{version}-%{release}.jar

%files
%defattr(0644, root,root)
%{install_path}/%{name}-%{version}-%{release}.jar

%clean
if [ "$RPM_BUILD_ROOT" != "/" ]; then
    rm -rf "$RPM_BUILD_ROOT"
fi
