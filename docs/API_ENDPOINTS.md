# API Endpoints Documentation

## Overview
This document describes all available API endpoints for the Credit Application Platform.

### Authentication
Most endpoints require JWT Bearer token authentication. Include the token in the Authorization header:
`
Authorization: Bearer {jwt_token}
`

Public endpoints do not require authentication.

---

## Authentication API

### POST /api/auth/register-bank
**Description:** Register a new bank organization with an admin account.

**Authentication:** None required (public endpoint)

**Request Method:** POST

**Request Headers:**
- Content-Type: application/json

**Request Body:**
`json
{
  \"bankName\": \"Example Bank\",
  \"registrationNumber\": \"BNK123456\",
  \"contactEmail\": \"admin@examplebank.com\",
  \"adminFirstName\": \"John\",
  \"adminLastName\": \"Doe\",
  \"adminPassword\": \"SecurePassword123!\",
  \"adminPasswordConfirm\": \"SecurePassword123!\",
  \"adminPhone\": \"+373-012-345-67\"
}
`

**Request Parameters:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|-----------|-------------|
| bankName | String | Yes | @NotBlank | Legal name of the bank |
| registrationNumber | String | Yes | @NotBlank, 5-20 alphanumeric, unique | Official registration/license number |
| contactEmail | String | Yes | @Email | Bank contact email address |
| adminFirstName | String | Yes | @NotBlank | Bank admin first name |
| adminLastName | String | Yes | @NotBlank | Bank admin last name |
| adminPassword | String | Yes | @Size(min=12) | Admin password (minimum 12 characters) |
| adminPasswordConfirm | String | Yes | Must match adminPassword | Password confirmation for validation |
| adminPhone | String | Yes | @Pattern(regexp=...) | Bank admin phone number |

**Response (201 Created):**
`json
{
  \"bankId\": \"550e8400-e29b-41d4-a716-446655440000\",
  \"bankName\": \"Example Bank\",
  \"adminUserId\": \"660e8400-e29b-41d4-a716-446655440001\",
  \"status\": \"PENDING_ACTIVATION\",
  \"message\": \"Bank registered successfully. Activation email sent to admin@examplebank.com\"
}
`

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| bankId | UUID | Unique identifier for the bank organization |
| bankName | String | Name of the registered bank |
| adminUserId | UUID | User ID of the created admin account |
| status | String | Bank status: PENDING_ACTIVATION (must activate via email) |
| message | String | Success message with next steps |

**Error Responses:**

**400 Bad Request** - Input validation failed
`json
{
  \"error\": \"Invalid Input\",
  \"message\": \"Invalid email format or password does not meet requirements\",
  \"timestamp\": \"2026-01-15T10:30:00Z\"
}
`

**409 Conflict** - Registration number already registered
`json
{
  \"error\": \"Conflict\",
  \"message\": \"A bank with registration number 'BNK123456' is already registered\",
  \"timestamp\": \"2026-01-15T10:30:00Z\"
}
`

**500 Internal Server Error** - Unexpected error
`json
{
  \"error\": \"Internal Server Error\",
  \"message\": \"Failed to register bank\",
  \"timestamp\": \"2026-01-15T10:30:00Z\"
}
`

**Workflow:**
1. Submit valid bank registration details
2. Admin account created with BANK_ADMIN role
3. Activation email sent to contactEmail with activation link
4. Bank status set to PENDING_ACTIVATION
5. Admin must click email link to activate bank (see GET /api/auth/activate)

**Example cURL:**
`ash
curl -X POST http://localhost:8080/api/auth/register-bank \
  -H \"Content-Type: application/json\" \
  -d '{
    \"bankName\": \"Example Bank\",
    \"registrationNumber\": \"BNK123456\",
    \"contactEmail\": \"admin@examplebank.com\",
    \"adminFirstName\": \"John\",
    \"adminLastName\": \"Doe\",
    \"adminPassword\": \"SecurePassword123!\",
    \"adminPasswordConfirm\": \"SecurePassword123!\",
    \"adminPhone\": \"+373-012-345-67\"
  }'
`

---

### GET /api/auth/activate
**Description:** Activate a bank account using the activation token from the email.

**Authentication:** None required (public endpoint)

**Request Method:** GET

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| token | String | Yes | Activation token from email link |

**Response (200 OK):**
`json
{
  \"bankId\": \"550e8400-e29b-41d4-a716-446655440000\",
  \"bankName\": \"Example Bank\",
  \"status\": \"ACTIVE\",
  \"message\": \"Bank activated successfully. You can now log in.\"
}
`

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| bankId | UUID | Unique identifier for the bank organization |
| bankName | String | Name of the activated bank |
| status | String | Bank status: ACTIVE (now ready to log in and access features) |
| message | String | Success message confirming activation |

**Error Responses:**

**400 Bad Request** - Token expired or already activated
`json
{
  \"error\": \"Bad Request\",
  \"message\": \"Activation token has expired or bank is already activated\",
  \"timestamp\": \"2026-01-15T10:30:00Z\"
}
`

**404 Not Found** - Invalid or non-existent token
`json
{
  \"error\": \"Not Found\",
  \"message\": \"Activation token not found. Please check the link from your email.\",
  \"timestamp\": \"2026-01-15T10:30:00Z\"
}
`

**Workflow:**
1. Click activation link from email: /api/auth/activate?token=XXXXX
2. Token validated (must not be expired, format must be correct)
3. Bank status changed from PENDING_ACTIVATION to ACTIVE
4. Activation token nullified (one-time use)
5. Admin can now log in using the bank admin credentials

**Token Details:**
- Format: 32-character alphanumeric string
- Expiration: 7 days from registration
- Valid for one activation only
- Cannot be reused after successful activation

**Example cURL:**
`ash
curl -X GET 'http://localhost:8080/api/auth/activate?token=AbCdEfGhIjKlMnOpQrStUvWxYz123456'
`

---

## Health Check API

### GET /api/health
**Description:** Check the health status of the application and its dependencies.

**Authentication:** None required (public endpoint)

**Response (200 OK):**
`json
{
  \"status\": \"UP\",
  \"database\": \"connected\",
  \"redis\": \"connected\",
  \"version\": \"1.0.0\"
}
`

---

## API Response Format

### Success Response
All successful responses follow this format:
- **HTTP Status:** 200 OK, 201 Created, 204 No Content
- **Body:** JSON object or array (varies by endpoint)
- **Headers:** Content-Type: application/json

### Error Response
All error responses follow this format:
`json
{
  \"error\": \"Error Type\",
  \"message\": \"Human-readable error message\",
  \"timestamp\": \"2026-01-15T10:30:00Z\",
  \"path\": \"/api/auth/register-bank\" (optional)
}
`

**Common HTTP Status Codes:**
- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request successful, no response body
- **400 Bad Request** - Invalid input or validation failed
- **401 Unauthorized** - Missing or invalid authentication
- **403 Forbidden** - Authenticated but not authorized for this resource
- **404 Not Found** - Resource not found
- **409 Conflict** - Conflict (e.g., duplicate registration)
- **429 Too Many Requests** - Rate limit exceeded
- **500 Internal Server Error** - Unexpected server error

---

## Authorization Matrix

| Endpoint | Method | Public | Borrower | Bank Admin | Compliance |
|----------|--------|--------|----------|------------|------------|
| /api/auth/register-bank | POST |  |  |  |  |
| /api/auth/activate | GET |  |  |  |  |
| /api/health | GET |  |  |  |  |

---

## Versioning
This API uses path-based versioning. Future versions will be available at /api/v2/auth/... etc.

## Rate Limiting
- Public endpoints: 100 requests per minute
- Authenticated endpoints: 1000 requests per minute
- Special limits noted in endpoint descriptions

## Changelog

| Date | Version | Changes | Story |
|------|---------|---------|-------|
| 2026-01-15 | 1.0 | Initial API documentation - Bank registration and activation endpoints | Story 1.4 |
