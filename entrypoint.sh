#!/bin/sh
echo "=== DEBUG: Checking environment variables ==="
echo "SPRING_DATASOURCE_URL is set: $([ -n "$SPRING_DATASOURCE_URL" ] && echo YES || echo NO)"
echo "SPRING_DATASOURCE_USERNAME is set: $([ -n "$SPRING_DATASOURCE_USERNAME" ] && echo YES || echo NO)"
echo "SPRING_DATASOURCE_PASSWORD is set: $([ -n "$SPRING_DATASOURCE_PASSWORD" ] && echo YES || echo NO)"
echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
echo "JWT_SECRETKEY is set: $([ -n "$JWT_SECRETKEY" ] && echo YES || echo NO)"
echo "FRONTEND_URL: $FRONTEND_URL"
echo "STRIPE_SECRET_KEY is set: $([ -n "$STRIPE_SECRET_KEY" ] && echo YES || echo NO)"
echo "STRIPE_WEBHOOK_SECRET is set: $([ -n "$STRIPE_WEBHOOK_SECRET" ] && echo YES || echo NO)"
echo "=== Starting Spring Boot Application ==="

exec java -jar app.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url="$SPRING_DATASOURCE_URL" \
  --spring.datasource.username="$SPRING_DATASOURCE_USERNAME" \
  --spring.datasource.password="$SPRING_DATASOURCE_PASSWORD" \
  --spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect \
  --spring.datasource.hikari.connection-timeout=60000 \
  --spring.datasource.hikari.maximum-pool-size=5 \
  --jwt.secretKey="$JWT_SECRETKEY" \
  --frontend.url="$FRONTEND_URL" \
  --stripe.secret.key="$STRIPE_SECRET_KEY" \
  --stripe.webhook.secret="$STRIPE_WEBHOOK_SECRET"
