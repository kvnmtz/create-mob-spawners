new_version=$1

./gradlew reobfJar
mv build/reobfJar/output.jar build/reobfJar/create-mob-spawners-1.20.1-"$new_version".jar