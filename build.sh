new_version=$1

sed -i "s/^mod_version=.*/mod_version=$new_version/" gradle.properties

./gradlew reobfJar
mv build/reobfJar/output.jar build/reobfJar/create-mob-spawners-1.20.1-"$new_version".jar