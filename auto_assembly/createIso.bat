mkdir Distr
copy target\artifacts\Installer\* Distr\
copy D:\AL\Work\abs3\jdk\* Distr\
svn export svn://alcomputer/RunaWFE-4.x/trunk Distr\src
rd /S /Q Distr\src\testing
jar -cMf Distr/src.zip Distr\src
rd /S /Q Distr\src
"C:\Program Files (x86)\PowerISO\piso.exe" create -o Distr/runawfe-installer.iso -add Distr /

mkdir D:\AL\Work\repositories\Runa\4.x\ISO
copy Distr\runawfe-installer.exe D:\AL\Work\repositories\Runa\4.x\ISO\
copy Distr\runawfe-installer.iso D:\AL\Work\repositories\Runa\4.x\ISO\
