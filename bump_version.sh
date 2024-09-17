#!/bin/bash -e

NEW_VERSION=$1"-SNAPSHOT"

if [[ -z $1 ]]; then
    echo "Please provide a version number."
    exit 1
fi

# Update pom.xml files in demo, messenger, sponsoring, and library directories
for dir in demo messenger payment sponsoring redirect; do
    pom_file="${dir}/pom.xml"
    sed -i '' "s|<version>[0-9.]*-SNAPSHOT</version>|<version>${NEW_VERSION}</version>|g" "${pom_file}"
    sed -i '' "s|<leaf.version>[0-9.]*-SNAPSHOT</leaf.version>|<leaf.version>${NEW_VERSION}</leaf.version>|g" "${pom_file}"
    echo "Updated ${pom_file}"
done

pom_file="library/pom.xml"
sed -i '' "s|<version>[0-9.]*-SNAPSHOT</version>|<version>${NEW_VERSION}</version>|g" "${pom_file}"

echo "Updated ${pom_file}"
