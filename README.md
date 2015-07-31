# JStepWat

A java version of StepWat. Uses JsoilWat.
## Building
This project requires java and git. The make.sh file will attempt to clone the JsoiWat and build it because it is a dependency. It will then compile the jstepwat code and make a executable jar file.
```
git clone https://github.com/ryanmurf/JStepWat.git
cd JStepWat
./make.sh
#To display help
./stepwat.jar --help
```

## Testing
```
git clone https://github.com/Burke-Lauenroth-Lab/StepWat.git
cd StepWat
git checkout SoilWat31
cp ../JStepWat/stepwat.jar testing/Stepwat\ Inputs/
cd testing/Stepwat\ Inputs/
./stepwat.jar
```
