@echo off
echo ============================================
echo  Ride-Sharing Assignment Optimisation
echo ============================================

:: Create output directory
if not exist out mkdir out

:: Compile all Java source files
echo Compiling...
javac -encoding UTF-8 -d out src\ridesharing\Driver.java src\ridesharing\Passenger.java src\ridesharing\Assignment.java src\ridesharing\ComplexityResult.java src\ridesharing\GreedyAlgorithm.java src\ridesharing\HungarianAlgorithm.java src\ridesharing\BruteForceAlgorithm.java src\ridesharing\MapPanel.java src\ridesharing\ComparisonPanel.java src\ridesharing\BenchmarkPanel.java src\ridesharing\ComplexityPanel.java src\ridesharing\Dashboard.java

if %errorlevel% neq 0 (
    echo COMPILATION FAILED. Check errors above.
    pause
    exit /b 1
)

echo Compilation successful!
echo Launching dashboard...
java -cp out ridesharing.Dashboard
pause
