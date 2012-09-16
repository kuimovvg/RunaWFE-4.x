#!/bin/bash

EOF_SYM=EOF

addInfo(){
    pre=""
    if [ -s $2 ]; then
	pre="1"
    fi
    if [ -s  $2.custom ]; then
	pre="1"
    fi
    if [ "$pre" = "1" ]; then
	echo $1 >>runawfe.spec
        echo >>runawfe.spec
    fi
    if [ -s $2 ]; then
        cat $2 >>runawfe.spec
        echo >>runawfe.spec
    fi
    if [ -s $2.custom ]; then
        cat $2.custom >>runawfe.spec
        echo >>runawfe.spec
    fi
}

cat macros >runawfe.spec
echo >>runawfe.spec
if [ -f additional_macros ]; then
    cat additional_macros >>runawfe.spec
    echo >>runawfe.spec
fi

cat << EOF >>runawfe.spec
Name: runawfe
Version: ReplaceablePackageVersion
Release: ReplaceableReleaseVersion
EOF

echo Summary: `cat descriptions/runawfe.summary` >>runawfe.spec
echo License: `cat descriptions/runawfe.license` >>runawfe.spec
echo Group: `cat descriptions/runawfe.group` >>runawfe.spec

cat << EOF >>runawfe.spec
Url: http://sourceforge.net/projects/runawfe
Packager: Aleksey Konstantinov <kana@altlinux.org>
EOF
#echo BuildArch: `cat descriptions/runawfe.arch` >>runawfe.spec
cat << EOF >>runawfe.spec
Source0: %{name}-%{version}.tar.gz
EOF

echo BuildPrereq: `cat descriptions/runawfe.buildprereq` >>runawfe.spec

echo >>runawfe.spec

echo %description >>runawfe.spec
cat descriptions/runawfe.description >>runawfe.spec

echo >>runawfe.spec
echo "############################   { Packages description }   #############################">>runawfe.spec
for package in `ls descriptions/runawfe-*.description | cut -f1 -d. | cut -f2- -d-`; do
    echo "############################   { runawfe-$package }   #############################">>runawfe.spec
    echo "%package $package" >>runawfe.spec
    echo Group: `cat descriptions/runawfe.group` >>runawfe.spec
    if [ -f descriptions/runawfe-$package.arch ]; then
        echo "BuildArch: noarch" >>runawfe.spec
    fi
    echo Summary: `cat descriptions/runawfe-$package.summary` >>runawfe.spec
    if [ -s descriptions/runawfe-$package.requires ]; then
        echo Requires: `cat descriptions/runawfe-$package.requires` >>runawfe.spec
    fi
    if [ -s descriptions/runawfe-$package.conflicts ]; then
        echo Conflicts: `cat descriptions/runawfe-$package.conflicts` >>runawfe.spec
    fi
    echo "%description $package" >>runawfe.spec
    cat descriptions/runawfe-$package.description >>runawfe.spec
    echo >>runawfe.spec
done
echo >>runawfe.spec
echo "############################   { End of packages description }   #############################">>runawfe.spec
echo >>runawfe.spec

cat << EOF >>runawfe.spec
%clean
%prep
%setup -q
%build
%install

EOF

cat descriptions/runawfe.installseq >>runawfe.spec
echo >>runawfe.spec

cat << EOF >>runawfe.spec

prepareJboss(){

	rm -Rf %JBOSS_ROOT_RPM/client
	mkdir -p %JBOSS_ROOT_RPM/client
	cp -Rf %JBOSS_ROOT_ORIG/client/* %JBOSS_ROOT_RPM/client
        # For installation we need: default configuration and deploy folders.
	mkdir -p %JBOSS_ROOT_RPM/server/default/conf
	mkdir -p %JBOSS_ROOT_RPM/server/default/deploy
        cp -Rf %JBOSS_ROOT_ORIG/server/default/conf/* %JBOSS_ROOT_RPM/server/default/conf
        cp -Rf %JBOSS_ROOT_ORIG/server/default/deploy/* %JBOSS_ROOT_RPM/server/default/deploy
}

# Expected param: 1 - ant target to build jboss config; 2 - jboss configuration name
installWFEConf(){
        # install runawfe
        ant \$1

	# Port 8080 is busy on ALTLinux (by control center) so start jboss server on  port 28080
	#sed "s/8080/%runawfe_web_port/" %JBOSS_ROOT_RPM/server/default/deploy/jbossweb-tomcat55.sar/server.xml > tmp_file && mv -f tmp_file %JBOSS_ROOT_RPM/server/default/deploy/jbossweb-tomcat55.sar/server.xml
	sed "s/8080/%runawfe_web_port/" %JBOSS_ROOT_RPM/server/default/deploy/http-invoker.sar/META-INF/jboss-service.xml > tmp_file && mv -f tmp_file %JBOSS_ROOT_RPM/server/default/deploy/http-invoker.sar/META-INF/jboss-service.xml

        # remove unchanged jboss files
        rm -Rf \`diff %JBOSS_ROOT_RPM/server/default/conf %JBOSS_ROOT_ORIG/server/default/conf -s -r | grep -v "diff -s -r" | grep " %JBOSS_ROOT_ORIG/server/default/conf" | grep " %JBOSS_ROOT_RPM/server/default/conf" | cut -d' ' --fields=2\`
        rm -Rf \`diff %JBOSS_ROOT_RPM/server/default/deploy %JBOSS_ROOT_ORIG/server/default/deploy -s -r | grep -v "diff -s -r" | grep " %JBOSS_ROOT_ORIG/server/default/deploy" | grep " %JBOSS_ROOT_RPM/server/default/deploy" | cut -d' ' --fields=2\`

        mkdir -p %JBOSS_ROOT_RPM/server/\$2
        mv -f %JBOSS_ROOT_RPM/server/default/conf %JBOSS_ROOT_RPM/server/\$2
        mv -f %JBOSS_ROOT_RPM/server/default/deploy %JBOSS_ROOT_RPM/server/\$2
        mkdir %JBOSS_ROOT_RPM/server/\$2/data
        cp -Rf data %JBOSS_ROOT_RPM/server/\$2/
        rm -Rf %JBOSS_ROOT_RPM/bin/*.dll
}

rm -Rf %JBOSS_ROOT_RPM
# Configure build script to use jboss from RPM folder
echo "" >> build.properties
echo "jboss.home.dir=%JBOSS_ROOT_RPM" >> build.properties
echo "eclipse.home.dir=\\\${basedir}/../eclipse" >> build.properties
echo "" >> build.properties

prepareJboss
ant install.wfe
cd rtn
ant copy.libs
cd ..
installWFEConf install.remote.bots %jboss_cfg_botstation

prepareJboss
installWFEConf install.wfe %jboss_cfg_server

prepareJboss
installWFEConf install.simulation %jboss_cfg_simulation

rm -Rf %JBOSS_ROOT_RPM/client

EOF

for package in `ls descriptions/runawfe-*.description | cut -f1 -d. | cut -f2- -d-`; do
    echo >>runawfe.spec
    echo "############################   { install sequence for runawfe-$package }   #############################">>runawfe.spec
    echo "pushd ." >>runawfe.spec
    addInfo "" descriptions/runawfe-$package.installseq
    echo "popd" >>runawfe.spec
done
echo >>runawfe.spec
echo "############################   { End of packages installation sequences }   #############################">>runawfe.spec
echo >>runawfe.spec

for package in `ls descriptions/runawfe-*.description | cut -f1 -d. | cut -f2- -d-`; do
    echo >>runawfe.spec
    echo "############################   { pre/post scripts for runawfe-$package }   #############################">>runawfe.spec
    addInfo "%pre $package" descriptions/runawfe-$package.preinst
    addInfo "%post $package" descriptions/runawfe-$package.postinst
    addInfo "%preun $package" descriptions/runawfe-$package.preuninst
    addInfo "%postun $package" descriptions/runawfe-$package.postuninst
done
echo >>runawfe.spec

for package in `ls descriptions/runawfe-*.description | cut -f1 -d. | cut -f2- -d-`; do
    echo >>runawfe.spec
    echo >>runawfe.spec
    addInfo "%files $package" descriptions/runawfe-$package.files
    
done
echo >>runawfe.spec

cat <<EOF >>runawfe.spec
%changelog
EOF
