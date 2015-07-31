#!/bin/bash

rm -rf bin
rm -rf lib
mkdir bin
mkdir lib

cd lib
git clone https://github.com/Burke-Lauenroth-Lab/JsoilWat.git
cd JsoilWat
./make.sh
cp soilwat.jar ../
cp -r bin/soilwat/ ../../bin/
cp -r bin/events/ ../../bin/
cd ..
cd ..

javac -d bin -sourcepath src src/stepwat/input/ST/BmassFlags.java
javac -d bin -sourcepath src src/stepwat/input/ST/Environment.java
javac -d bin -sourcepath src src/stepwat/input/ST/Files.java
javac -d bin -sourcepath src src/stepwat/input/ST/Model.java
javac -d bin -sourcepath src src/stepwat/input/ST/MortFlags.java
javac -d bin -sourcepath src src/stepwat/input/ST/Plot.java
javac -d bin -sourcepath src src/stepwat/input/ST/Rgroup.java
javac -d bin -sourcepath src src/stepwat/input/ST/Species.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/input/ST/ST_Input.java

javac -d bin -sourcepath src src/stepwat/input/SXW/BVT.java
javac -d bin -sourcepath src src/stepwat/input/SXW/DeBug.java
javac -d bin -sourcepath src src/stepwat/input/SXW/Files.java
javac -d bin -sourcepath src src/stepwat/input/SXW/Phenology.java
javac -d bin -sourcepath src src/stepwat/input/SXW/Production.java
javac -d bin -sourcepath src src/stepwat/input/SXW/Roots.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/input/SXW/SXW_Input.java
javac -d bin -sourcepath src src/stepwat/input/SXW/Times.java

javac -d bin -sourcepath src src/stepwat/input/grid/Disturbances.java
javac -d bin -sourcepath src src/stepwat/input/grid/Files.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/input/grid/Grid_Input.java
javac -d bin -sourcepath src src/stepwat/input/grid/SeedDispersal.java
javac -d bin -sourcepath src src/stepwat/input/grid/Setup.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/input/grid/Soils.java
javac -d bin -sourcepath src src/stepwat/input/grid/Species.java

javac -d bin -sourcepath src src/stepwat/input/Input.java

javac -d bin -sourcepath src src/stepwat/internal/BmassFlags.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Environs.java
javac -d bin -sourcepath src src/stepwat/internal/Globals.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Indiv.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Mortality.java
javac -d bin -sourcepath src src/stepwat/internal/MortFlags.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Output.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Plot.java
javac -d bin -sourcepath src src/stepwat/internal/Plot.java
javac -d bin -sourcepath src src/stepwat/internal/Rand.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/ResourceGroup.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/RGroups.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Species.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/Stats.java
javac -d bin -sourcepath src src/stepwat/internal/Succulent.java
javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/internal/SXW.java

javac -d bin -sourcepath src -cp lib/soilwat.jar src/stepwat/Control.java
javac -d bin -sourcepath src src/stepwat/LogFileIn.java

javac -d bin -sourcepath src -cp bin/ src/Main.java

echo "Main-Class: Main" > Manifest.txt
jar cfm stepwat.jar Manifest.txt -C bin/ .
chmod +x stepwat.jar
