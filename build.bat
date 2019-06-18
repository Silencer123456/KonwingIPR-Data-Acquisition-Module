CD /D %~dp0

call mvn clean compile assembly:single

IF EXIST target (
	copy mydb.cfg target
	copy mongo-config.cfg target
	md target\data
	xcopy data target\data /E
) ELSE (
	ECHO target folder not found
)