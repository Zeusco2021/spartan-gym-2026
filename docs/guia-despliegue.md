# Guía de Despliegue — Spartan Golden Gym

## Prerrequisitos

| Herramienta | Versión Mínima | Uso |
|---|---|---|
| AWS CLI | 2.x | Gestión de recursos AWS |
| Terraform | 1.5.0 | Infraestructura como código |
| kubectl | 1.27+ | Gestión de Kubernetes |
| Docker | 24.x | Construcción de imágenes |
| Maven | 3.8.x | Build del backend |
| JDK | 8 | Compilación Java |
| Node.js | 18.x | Build del frontend y móvil |
| npm | 9.x | Gestión de dependencias JS |

## 1. Infraestructura AWS (Terraform)

### 1.1 Configurar credenciales AWS

```bash
aws configure
# Ingresar: AWS Access Key ID, Secret Access Key, Region (us-east-1)
```

### 1.2 Inicializar Terraform

```bash
cd infrastructure/terraform

# Crear bucket S3 para estado (solo la primera vez)
aws s3 mb s3://spartan-golden-gym-terraform-state --region us-east-1
aws dynamodb create-table \
  --table-name terraform-locks \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

terraform init
```

### 1.3 Desplegar por entorno

```bash
# Desarrollo
terraform workspace new desarrollo || terraform workspace select desarrollo
terraform plan -var-file=environments/desarrollo.tfvars
terraform apply -var-file=environments/desarrollo.tfvars

# Staging
terraform workspace new staging || terraform workspace select staging
terraform plan -var-file=environments/staging.tfvars
terraform apply -var-file=environments/staging.tfvars

# Producción
terraform workspace new produccion || terraform workspace select produccion
terraform plan -var-file=environments/produccion.tfvars
terraform apply -var-file=environments/produccion.tfvars
```

### 1.4 Recursos desplegados

- VPC con subnets públicas y privadas en 2 AZs
- EKS cluster con autoescalado (CPU 70%, memoria 80%)
- Aurora PostgreSQL (2 AZs, failover automático, backup 30 días)
- DynamoDB (tablas globales en 2 regiones, PITR habilitado)
- Timestream (base de datos `spartan_metrics`)
- Neptune (grafo de ejercicios y relaciones sociales)
- ElastiCache Redis (sesiones, rankings, geofences)
- Amazon MSK (Kafka con 8 tópicos configurados)
- S3 + CloudFront (videos, imágenes)
- SageMaker (endpoints de inferencia ML)
- Redshift (analíticas)

### 1.5 Post-despliegue: Crear tópicos Kafka

```bash
chmod +x infrastructure/kafka/create-topics.sh
./infrastructure/kafka/create-topics.sh
```

### 1.6 Post-despliegue: Inicializar Neptune

```bash
# Ejecutar el script Gremlin contra el endpoint de Neptune
gremlin-console -e infrastructure/neptune/init-graph-schema.groovy
```

## 2. Backend — Microservicios

### 2.1 Build local

```bash
# Desde la raíz del proyecto
mvn clean package -DskipTests
```

### 2.2 Build de imágenes Docker

```bash
# Para cada microservicio
SERVICES="api-gateway servicio-usuarios servicio-gimnasio servicio-entrenamiento servicio-seguimiento servicio-nutricion servicio-ia-coach servicio-social servicio-pagos servicio-analiticas servicio-notificaciones servicio-reservas servicio-mensajeria servicio-calendario"

for SERVICE in $SERVICES; do
  docker build --build-arg MODULE=$SERVICE -t spartan-gym/$SERVICE:latest .
done
```

### 2.3 Push a Amazon ECR

```bash
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION=us-east-1

aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

for SERVICE in $SERVICES; do
  aws ecr create-repository --repository-name spartan-gym/$SERVICE --region $AWS_REGION 2>/dev/null || true
  docker tag spartan-gym/$SERVICE:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/spartan-gym/$SERVICE:latest
  docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/spartan-gym/$SERVICE:latest
done
```

### 2.4 Desplegar en EKS

```bash
# Configurar kubectl
aws eks update-kubeconfig --name spartan-gym-desarrollo --region us-east-1

# Aplicar namespace
kubectl apply -f k8s/base/namespace.yml

# Desplegar cada servicio
for SERVICE in $SERVICES; do
  export SERVICE_NAME=$SERVICE
  export ENVIRONMENT=desarrollo
  export IMAGE=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/spartan-gym/$SERVICE:latest
  envsubst < k8s/base/deployment.yml | kubectl apply -f -
  envsubst < k8s/base/service.yml | kubectl apply -f -
  envsubst < k8s/base/hpa.yml | kubectl apply -f -
done
```

### 2.5 Verificar despliegue

```bash
kubectl get pods -n spartan-gym-desarrollo
kubectl get services -n spartan-gym-desarrollo
kubectl get hpa -n spartan-gym-desarrollo
```

## 3. Frontend Web

### 3.1 Desarrollo local

```bash
cd spartan-gym-frontend
npm install
npm run dev
# Disponible en http://localhost:5173
# Proxy automático: /api -> http://localhost:8080
```

### 3.2 Build de producción

```bash
npm run build
# Output en spartan-gym-frontend/dist/
```

### 3.3 Desplegar en S3 + CloudFront

```bash
# Subir a S3
aws s3 sync dist/ s3://spartan-gym-frontend-${ENVIRONMENT}/ --delete

# Invalidar cache de CloudFront
aws cloudfront create-invalidation \
  --distribution-id $CLOUDFRONT_DISTRIBUTION_ID \
  --paths "/*"
```

## 4. App Móvil

### 4.1 Desarrollo local

```bash
cd spartan-gym-mobile
npm install

# iOS (requiere macOS + Xcode)
npx react-native run-ios

# Android (requiere Android Studio + emulador)
npx react-native run-android
```

### 4.2 Build de producción — Android

```bash
cd android
./gradlew assembleRelease
# APK en: android/app/build/outputs/apk/release/app-release.apk

# Para AAB (Google Play):
./gradlew bundleRelease
# AAB en: android/app/build/outputs/bundle/release/app-release.aab
```

### 4.3 Build de producción — iOS

```bash
cd ios
pod install
# Abrir SpartanGym.xcworkspace en Xcode
# Product > Archive > Distribute App (App Store Connect)
```

### 4.4 Distribución

- Android: Google Play Console (subir AAB)
- iOS: App Store Connect (subir desde Xcode Archive)
- Testing: Firebase App Distribution para builds internos

## 5. CI/CD Automatizado (GitHub Actions)

El proyecto incluye 3 workflows:

### ci.yml — Build & Test
- Trigger: push/PR a `main` o `develop`
- Jobs: backend-build, frontend-web-build, frontend-mobile-build, docker-build
- Construye imágenes Docker y las sube a ECR en push a main/develop

### deploy.yml — Deploy to EKS
- Trigger: después de CI exitoso o manual (workflow_dispatch)
- Mapeo de branches: `develop` → desarrollo, `main` → staging
- Promoción a producción: gate manual después de staging + auditoría de accesibilidad
- Despliegue rolling update con rollout status check (timeout 300s)

### accessibility-audit.yml — Auditorías de Accesibilidad
- axe-core para web (WCAG 2.1 AA)
- Android Accessibility Scanner (staging)
- iOS Accessibility Inspector (staging)

## 6. Configuración por Entorno

### Recursos Kubernetes

| Recurso | Desarrollo | Staging | Producción |
|---|---|---|---|
| Réplicas mínimas | 2 | 2 | 3 |
| Réplicas máximas (HPA) | 10 | 10 | 20 |
| CPU request | 250m | 250m | 500m |
| Memory request | 512Mi | 512Mi | 1Gi |
| CPU limit | 1 | 1 | 2 |
| Memory limit | 1Gi | 1Gi | 2Gi |

### Variables de Entorno (Spring Profiles)

Cada microservicio usa `SPRING_PROFILES_ACTIVE` para cargar la configuración del entorno correspondiente (`desarrollo`, `staging`, `produccion`).

## 7. Migraciones de Base de Datos

Las migraciones SQL se ejecutan automáticamente al iniciar cada microservicio mediante Flyway. Los scripts se encuentran en `common-lib/src/main/resources/db/migration/`.

```bash
# Ejecutar migraciones manualmente (si es necesario)
mvn flyway:migrate -pl common-lib -Dflyway.url=jdbc:postgresql://HOST:5432/spartangym
```
