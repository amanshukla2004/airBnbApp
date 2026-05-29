#!/bin/sh
echo "=== DEBUG: Checking environment variables ==="
echo "SPRING_DATASOURCE_URL is set: $([ -n "$SPRING_DATASOURCE_URL" ] && echo YES || echo NO)"
echo "SPRING_DATASOURCE_USERNAME is set: $([ -n "$SPRING_DATASOURCE_USERNAME" ] && echo YES || echo NO)"
echo "SPRING_DATASOURCE_PASSWORD is set: $([ -n "$SPRING_DATASOURCE_PASSWORD" ] && echo YES || echo NO)"
echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
echo "FRONTEND_URL: $FRONTEND_URL"
echo "=== Starting Spring Boot Application ==="
exec java -jar app.jar --spring.profiles.active=prod
