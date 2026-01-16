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

## Borrower Application API

### POST /api/borrower/applications
**Description:** Create a new loan application in DRAFT status.

**Authentication:** Required - BORROWER role

**Request Method:** POST

**Request Headers:**
- Content-Type: application/json
- Authorization: Bearer {jwt_token}

**Request Body:**
```json
{
  "loanType": "PERSONAL",
  "loanAmount": 25000,
  "loanTermMonths": 36,
  "currency": "EUR",
  "ratePreference": "VARIABLE"
}
```

**Request Parameters:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|-----------|-------------|
| loanType | String | Yes | Enum: PERSONAL, HOME, AUTO, DEBT_CONSOLIDATION, STUDENT, BUSINESS, OTHER | Type of loan requested |
| loanAmount | BigDecimal | Yes | Min: 100, Max: 1,000,000 | Loan amount requested |
| loanTermMonths | Integer | Yes | Min: 6, Max: 360 | Loan term in months |
| currency | String | Yes | Enum: EUR, USD, MDL | Currency for the loan |
| ratePreference | String | No | Enum: VARIABLE, FIXED, EITHER (default: VARIABLE) | Interest rate preference |

**Success Response (HTTP 201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "loanType": "PERSONAL",
  "loanAmount": 25000,
  "loanTermMonths": 36,
  "currency": "EUR",
  "ratePreference": "VARIABLE",
  "status": "DRAFT",
  "createdAt": "2026-01-14T10:30:00Z",
  "submittedAt": null,
  "updatedAt": "2026-01-14T10:30:00Z"
}
```

**Error Responses:**

**400 Bad Request** - Validation failure
```json
{
  "error": "Invalid Application",
  "message": "Loan amount must be at least 100",
  "timestamp": "2026-01-14T10:30:00Z"
}
```

Common validation errors:
- "Loan amount must be at least 100"
- "Loan amount cannot exceed 1,000,000"
- "Loan term must be at least 6 months"
- "Loan term cannot exceed 360 months"
- "Loan amount is required"
- "Loan term is required"

**401 Unauthorized** - Not authenticated
```json
{
  "error": "Unauthorized",
  "message": "Authentication required",
  "timestamp": "2026-01-14T10:30:00Z"
}
```

**403 Forbidden** - Wrong role (not BORROWER)
```json
{
  "error": "Forbidden",
  "message": "BORROWER role required",
  "timestamp": "2026-01-14T10:30:00Z"
}
```

**429 Too Many Requests** - Rate limit exceeded
```json
{
  "error": "Rate Limit Exceeded",
  "message": "You can create max 1 application per minute",
  "retryAfter": 60,
  "timestamp": "2026-01-14T10:30:00Z"
}
```

**500 Internal Server Error** - Unexpected server error

**Rate Limiting:** Maximum 1 application per borrower per minute

**Notes:**
- Application is created in DRAFT status
- Application is linked to authenticated borrower
- Audit log entry created with APPLICATION_CREATED event
- If ratePreference is not provided, defaults to VARIABLE

**cURL Example:**
```bash
curl -X POST https://api.creditapp.com/api/borrower/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -d '{
    "loanType": "PERSONAL",
    "loanAmount": 25000,
    "loanTermMonths": 36,
    "currency": "EUR",
    "ratePreference": "VARIABLE"
  }'
```

---

## Document Upload & Management API

### POST /api/borrower/applications/{applicationId}/documents
**Description:** Upload a document to support a loan application.

**Authentication:** Required (BORROWER role)

**Request Method:** POST

**Request Headers:**
- Content-Type: multipart/form-data
- Authorization: Bearer {jwt_token}

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| applicationId | UUID | Yes | Unique identifier of the application |

**Request Body (multipart/form-data):**

| Field | Type | Required | Validation | Description |
|-------|------|----------|-----------|-------------|
| file | File (binary) | Yes | Max 10 MB, specific MIME types | The document file to upload |
| documentType | String (enum) | Yes | One of: INCOME_STATEMENT, EMPLOYMENT_VERIFICATION, IDENTIFICATION, BANK_STATEMENT, OTHER | Type classification for the document |

**Supported MIME Types:**
- application/pdf
- image/jpeg
- image/png
- application/msword
- application/vnd.openxmlformats-officedocument.wordprocessingml.document

**Response (201 Created):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "applicationId": "550e8400-e29b-41d4-a716-446655440000",
  "documentType": "INCOME_STATEMENT",
  "originalFilename": "income_2025.pdf",
  "fileSize": 2048576,
  "uploadDate": "2026-01-16T10:30:00Z",
  "uploadedByUserId": "660e8400-e29b-41d4-a716-446655440001"
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Unique identifier for the uploaded document |
| applicationId | UUID | Associated application ID |
| documentType | String | Type of document (enum value) |
| originalFilename | String | Original name of the uploaded file |
| fileSize | Long | File size in bytes |
| uploadDate | ISO-8601 DateTime | Timestamp when document was uploaded |
| uploadedByUserId | UUID | User ID of the borrower who uploaded the document |

**Error Responses:**

**400 Bad Request** - Invalid document or format
```json
{
  "error": "Invalid Document",
  "message": "File type '.exe' is not supported. Supported types: PDF, JPG, PNG, DOC, DOCX",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**403 Forbidden** - Not the application owner
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to upload documents to this application",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**404 Not Found** - Application not found
```json
{
  "error": "Not Found",
  "message": "Application not found",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**409 Conflict** - Application status prevents uploads
```json
{
  "error": "Application Locked",
  "message": "Cannot upload documents to applications in UNDER_REVIEW or later status",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**413 Payload Too Large** - File or total size exceeded
```json
{
  "error": "File Size Exceeded",
  "message": "Total document size for this application cannot exceed 50 MB (current: 45 MB, attempted: 8 MB)",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**500 Internal Server Error** - Storage failure
```json
{
  "error": "Document Storage Error",
  "message": "Failed to store document. Please try again later.",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**Constraints:**
- Maximum file size: 10 MB per document
- Maximum total per application: 50 MB
- Application status must be DRAFT or SUBMITTED (UNDER_REVIEW and later statuses block uploads with 409)
- Borrower can only upload to their own applications
- Only specific MIME types allowed (see list above)

**cURL Example:**
```bash
curl -X POST https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/documents \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -F "file=@income_statement.pdf" \
  -F "documentType=INCOME_STATEMENT"
```

---

### GET /api/borrower/applications/{applicationId}/documents
**Description:** List all documents for a loan application (excluding soft-deleted documents).

**Authentication:** Required (BORROWER role)

**Request Method:** GET

**Request Headers:**
- Authorization: Bearer {jwt_token}

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| applicationId | UUID | Yes | Unique identifier of the application |

**Response (200 OK):**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "applicationId": "550e8400-e29b-41d4-a716-446655440000",
    "documentType": "INCOME_STATEMENT",
    "originalFilename": "income_2025.pdf",
    "fileSize": 2048576,
    "uploadDate": "2026-01-16T10:30:00Z",
    "uploadedByUserId": "660e8400-e29b-41d4-a716-446655440001"
  },
  {
    "id": "880e8400-e29b-41d4-a716-446655440003",
    "applicationId": "550e8400-e29b-41d4-a716-446655440000",
    "documentType": "BANK_STATEMENT",
    "originalFilename": "bank_statement_jan.pdf",
    "fileSize": 1524288,
    "uploadDate": "2026-01-16T11:15:00Z",
    "uploadedByUserId": "660e8400-e29b-41d4-a716-446655440001"
  }
]
```

**Error Responses:**

**403 Forbidden** - Not the application owner
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to view documents for this application",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**404 Not Found** - Application not found
```json
{
  "error": "Not Found",
  "message": "Application not found",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**Notes:**
- Returns only active documents (soft-deleted documents are excluded)
- Results are ordered by uploadDate descending (newest first)
- Borrower can only view documents for their own applications

**cURL Example:**
```bash
curl -X GET https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/documents \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

### DELETE /api/borrower/applications/{applicationId}/documents/{documentId}
**Description:** Soft-delete a document from a loan application. Document is marked for deletion but not physically removed from storage.

**Authentication:** Required (BORROWER role)

**Request Method:** DELETE

**Request Headers:**
- Authorization: Bearer {jwt_token}

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| applicationId | UUID | Yes | Unique identifier of the application |
| documentId | UUID | Yes | Unique identifier of the document to delete |

**Response (204 No Content):**
No response body. HTTP status 204 indicates successful soft-deletion.

**Error Responses:**

**403 Forbidden** - Not the application owner
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to delete documents from this application",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**404 Not Found** - Document or application not found
```json
{
  "error": "Not Found",
  "message": "Document not found",
  "timestamp": "2026-01-16T10:30:00Z"
}
```

**Notes:**
- Documents are soft-deleted using a deleted_at timestamp (not physically removed)
- Soft-deleted documents are excluded from GET /documents list
- Only active documents can be deleted
- Borrower can only delete documents from their own applications
- Audit log entry created with DOCUMENT_DELETED event

**cURL Example:**
```bash
curl -X DELETE https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/documents/770e8400-e29b-41d4-a716-446655440002 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

## Authorization Matrix

| Endpoint | Method | Public | Borrower | Bank Admin | Compliance |
|----------|--------|--------|----------|------------|------------|
| /api/auth/register-bank | POST | ✓ |  |  |  |
| /api/auth/activate | GET | ✓ |  |  |  |
| /api/borrower/applications | POST |  | ✓ |  |  |
| /api/borrower/applications/{id}/documents | POST |  | ✓ |  |  |
| /api/borrower/applications/{id}/documents | GET |  | ✓ |  |  |
| /api/borrower/applications/{id}/documents/{docId} | DELETE |  | ✓ |  |  |
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
| 2026-01-16 | 1.2 | Added document upload, list, and delete endpoints with file size limits and soft-delete pattern | Story 2.6 |
| 2026-01-16 | 1.1 | Added borrower application creation endpoint with validation and rate limiting | Story 2.2 |
| 2026-01-15 | 1.0 | Initial API documentation - Bank registration and activation endpoints | Story 1.4 |
