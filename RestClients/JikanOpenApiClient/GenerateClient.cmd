@echo off
REM dotnet new tool-manifest
REM dotnet tool install Microsoft.OpenApi.Kiota

REM Preare the needed tools
dotnet tool restore
IF '%errorlevel%' NEQ '0' (
    echo "could not restore the needed tools"
    pause
    goto end
)

@REM dotnet add package Microsoft.Kiota.Abstractions
@REM dotnet add package Microsoft.Kiota.Http.HttpClientLibrary
@REM dotnet add package Microsoft.Kiota.Serialization.Form
@REM dotnet add package Microsoft.Kiota.Serialization.Json
@REM dotnet add package Microsoft.Kiota.Serialization.Text
@REM dotnet add package Microsoft.Kiota.Serialization.Multipart

dotnet kiota generate ^
--openapi "./api-docs.json" ^
--output "./Clients" ^
--language csharp ^
--class-name "JikanApi" ^
--namespace-name "JikanRest" ^
--exclude-backward-compatible ^
--additional-data true ^
--clean-output


@REM --backing-store ^