# run this being in WebHavenFrontend directory


## change to basedir
cd $(dirname $0)
yarn run build
rm -rf ../WebHavenResources/dist
cp -r dist ../WebHavenResources/dist