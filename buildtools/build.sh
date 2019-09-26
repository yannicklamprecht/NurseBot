#!/bin/bash

DEPLOY=$1
VERSIONS=./versions

EXIT_SUCCESS=0
EXIT_COMPILE_FAILED=1
EXIT_PACKING_FAILED=2
EXIT_INSTRUMENTATION_FAILED=3
EXIT_READY_FAILED=4

mkdir -p ../bin/
mkdir -p ../build/
mkdir -p ../build/NurseBot_lib/

pushd ../bin/

# cleanup
rm -r *

popd

pushd ../src/

export manifest_cp=.
for file in $(ls ../build/NurseBot_lib); do
	echo "found lib: $file"
	export CLASSPATH=$CLASSPATH:../build/NurseBot_lib/$file
	export manifest_cp="$manifest_cp NurseBot_lib/$file"
done

echo "Building... "
javac -encoding utf8 -cp "$CLASSPATH" -d ../bin/ $(find ./ -iname "*.java")
if test ! $? = 0; then
	echo "... failed"
	exit $EXIT_COMPILE_FAILED
fi
echo "... done"

popd

pushd ../buildtools/

echo "Building instrumentation..."
./instrumentation.sh
if test ! $? = 0; then
	echo "... failed"
	exit $EXIT_INSTRUMENTATION_FAILED
fi
echo "... done"

echo "Copying instrumentation..."
cp ../bin/activejdbc_models.properties ../build/
if test ! $? = 0; then
	echo "... failed"
	exit $EXIT_INSTRUMENTATION_FAILED
fi
echo "... done"

popd

pushd ../bin/

MAX_LINE=72
manifest_cp="lass-Path: $manifest_cp"
manifest_cp="$(echo $manifest_cp | fold -bw $((MAX_LINE - 1)) | awk '{ if (NR == 1) print "C" $0; else print " " $0}')"


cat > ../build/MANIFST.MF <<EOF
Manifest-Version: 1.0
$manifest_cp
Main-Class: asylum.nursebot.NurseNoakes
EOF

echo "Packing jar..."
jar cmf ../build/MANIFST.MF ../build/NurseBot.jar $(find ./ -iname "*.class")
if test ! $? = 0; then
	echo "... failed"
	exit $EXIT_PACKING_FAILED
fi
echo "... done"

popd

pushd ../build

echo "Determine version..."
version=$(java -jar NurseBot.jar -v)
echo "This is version $version."

if test ! "$DEPLOY" = ""; then
	echo "Set to deploy ($DEPLOY/$VERSIONS/NurseBot$version.jar)
..."
	cp activejdbc_models.properties $DEPLOY/$VERSIONS/activejdbc_models$version.properties
	cp NurseBot.jar $DEPLOY/$VERSIONS/NurseBot$version.jar
	if test ! $? = 0; then
		echo "... failed"
		exit $EXIT_READY_FAILED
	fi
	echo "... done"
fi

echo "Overall done."
popd

exit $EXIT_SUCCESS

