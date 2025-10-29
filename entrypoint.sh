#!/bin/sh

# Check if MONGO_CONNECTION_STRING is defined
if [ -z "$MONGO_CONNECTION_STRING" ]; then
  >&2  echo -e "ERROR: The environment variable MONGO_CONNECTION_STRING is not defined.\n\tYou must provide a valid Connection String"
  exit 1
fi

# Verificar si MONGO_DATABASE_NAME está definido
if [ -z "$MONGO_DATABASE_NAME" ]; then
  >&2  echo -e "WARNING: The environment variable for the database, MONGO_DATABASE_NAME, is not defined.\n\tUsing ‘aruppi’ as default value."
  MONGO_DATABASE_NAME="aruppi"
fi

# Strip vars of enclosing quotes if any
[[ "${MONGO_CONNECTION_STRING}" == \"*\" || "${MONGO_CONNECTION_STRING}" == \'*\' ]] && MONGO_CONNECTION_STRING="${MONGO_CONNECTION_STRING:1:-1}"
[[ "${MONGO_DATABASE_NAME}" == \"*\" || "${MONGO_DATABASE_NAME}" == \'*\' ]] && MONGO_DATABASE_NAME="${MONGO_DATABASE_NAME:1:-1}"

# Generate the application.yaml file
cat <<EOF > /app/application.yaml
ktor:
  application:
    modules:
      - com.jeluchu.ApplicationKt.module
  deployment:
    port: 8080
    host: "0.0.0.0"

db:
  mongo:
    connectionStrings: "${MONGO_CONNECTION_STRING}"
    database:
      name: "${MONGO_DATABASE_NAME:-mongodb}"
EOF

cd /app/

# Run the application with the specified configuration
exec java -Dconfig.file=/app/application.yaml -jar /app/app.jar
