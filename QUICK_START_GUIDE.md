# Quick Start Guide: Integrate JWT Validation

## 🎯 What You Need to Do

### Step 1: Add Dependencies to `pom.xml`

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### Step 2: Add Configuration to `application.properties`

```properties
# Auth Service Configuration
auth.service.url=http://localhost:9090
auth.service.public-key-endpoint=/api/auth/public-key
```

### Step 3: Copy Files from Template

Copy these 5 files from `JWT_CLIENT_CODE_TEMPLATE.java`:
1. `PublicKeyResponse.java` (DTO)
2. `PublicKeyLoaderService.java` (Loads key on startup)
3. `JwtValidationService.java` (Validates tokens)
4. `JwtAuthenticationFilter.java` (Filters requests)
5. `SecurityConfig.java` (Security configuration)

### Step 4: Test It!

1. **Start your auth service** (should be running on port 9090)
2. **Start your other service** (should automatically load public key)
3. **Get a token**:
   ```bash
   curl -X POST http://localhost:9090/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"12345"}'
   ```
4. **Use the token**:
   ```bash
   curl -X GET http://localhost:YOUR_PORT/api/protected/data \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```

## ✅ What Happens:

1. **On Startup**: Service calls `http://localhost:9090/api/auth/public-key` and loads the public key into memory
2. **On Each Request**: Filter intercepts requests, validates JWT token using the stored public key
3. **If Valid**: Request proceeds, user is authenticated
4. **If Invalid**: Returns 401 Unauthorized

## 📝 Customization:

- **Public Endpoints**: Edit `isPublicEndpoint()` method in `JwtAuthenticationFilter`
- **Auth Service URL**: Change `auth.service.url` in properties
- **Error Messages**: Modify responses in `JwtAuthenticationFilter`

## 🔍 Troubleshooting:

**Problem**: Service fails to start - "Failed to load public key"
- **Solution**: Make sure auth service is running and accessible

**Problem**: Getting 401 Unauthorized
- **Solution**: Check token is valid and Authorization header format is correct: `Bearer <token>`

**Problem**: Public key not loading
- **Solution**: Check auth service URL and network connectivity

