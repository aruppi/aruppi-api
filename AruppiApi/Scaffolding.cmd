@echo off
REM dotnet new tool-manifest
REM dotnet tool install dotnet-ef
REM dotnet add package Microsoft.EntityFrameworkCore.Design
REM Microsoft.EntityFrameworkCore.Sqlite

REM Preare the needed tools
dotnet tool restore
IF '%errorlevel%' NEQ '0' (
    echo "could not restore the needed tools"
    pause
    goto end
) ELSE (
    rmdir /s /q "Database\ModelsTemp"
)

REM Try creating an updated version of the models that exist in the database
ren "Database\Models" "ModelsTemp"
@rem dotnet ef dbcontext scaffold name=DatabaseContext Microsoft.EntityFrameworkCore.Sqlite --data-annotations --no-onconfiguring -c DatabaseContext --context-dir Database --output-dir Database/Models --force
dotnet ef dbcontext scaffold^
 "name=DatabaseContext"^
 Microsoft.EntityFrameworkCore.Sqlite^
 --data-annotations^
 --context DatabaseContext^
 --context-dir Database^
 --force^
 --output-dir Database/Models^
 --no-onconfiguring^
 --verbose
IF '%errorlevel%' NEQ '0' (
    ren "Database\ModelsTemp" "Models"
    pause
) ELSE (
    rmdir /s /q "Database\ModelsTemp"
)

:end
@REM --no-build^
@REM Arguments:
@REM   <CONNECTION>  The connection string to the database.
@REM   <PROVIDER>    The provider to use. (E.g. Microsoft.EntityFrameworkCore.SqlServer)

@REM Options:
@REM   -d|--data-annotations                  Use attributes to configure the model (where possible). If omitted, only the fluent API is used.
@REM   -c|--context <NAME>                    The name of the DbContext. Defaults to the database name.
@REM   --context-dir <PATH>                   The directory to put the DbContext file in. Paths are relative to the project directory.
@REM   -f|--force                             Overwrite existing files.
@REM   -o|--output-dir <PATH>                 The directory to put files in. Paths are relative to the project directory.
@REM   --schema <SCHEMA_NAME>...              The schemas of tables and views to generate entity types for. All tables and views in the schemas will be included in the model, even if they are not explicitly included with the --table parameter.
@REM   -t|--table <TABLE_NAME>...             The tables and views to generate entity types for. Tables or views in a specific schema can be included using the 'schema.table' or 'schema.view' format.
@REM   --use-database-names                   Use table, view, sequence, and column names directly from the database.
@REM   --json                                 Show JSON output. Use with --prefix-output to parse programatically.
@REM   -n|--namespace <NAMESPACE>             The namespace to use. Matches the directory by default.
@REM   --context-namespace <NAMESPACE>        The namespace of the DbContext class. Matches the directory by default.
@REM   --no-onconfiguring                     Don't generate DbContext.OnConfiguring.
@REM   --no-pluralize                         Don't use the pluralizer.
@REM   -p|--project <PROJECT>                 The project to use. Defaults to the current working directory.
@REM   -s|--startup-project <PROJECT>         The startup project to use. Defaults to the current working directory.
@REM   --framework <FRAMEWORK>                The target framework. Defaults to the first one in the project.
@REM   --configuration <CONFIGURATION>        The configuration to use.
@REM   --runtime <RUNTIME_IDENTIFIER>         The runtime to use.
@REM   --msbuildprojectextensionspath <PATH>  The MSBuild project extensions path. Defaults to "obj".
@REM   --no-build                             Don't build the project. Intended to be used when the build is up-to-date.
@REM   -h|--help                              Show help information
@REM   -v|--verbose                           Show verbose output.
@REM   --no-color                             Don't colorize output.
@REM   --prefix-output                        Prefix output with level.