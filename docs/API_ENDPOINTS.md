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

### GET /api/borrower/applications/{applicationId}/status
**Description:** Get current application status and full history timeline of status transitions.

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
{
  "applicationId": "550e8400-e29b-41d4-a716-446655440000",
  "currentStatus": "UNDER_REVIEW",
  "submittedAt": "2026-01-16T10:30:00Z",
  "createdAt": "2026-01-14T09:00:00Z",
  "progressionPercentage": 50,
  "statusHistory": [
    {
      "oldStatus": "SUBMITTED",
      "newStatus": "UNDER_REVIEW",
      "changedAt": "2026-01-16T11:00:00Z",
      "changedByUserId": null,
      "changedByName": "System",
      "reason": "Bank started review process"
    },
    {
      "oldStatus": "DRAFT",
      "newStatus": "SUBMITTED",
      "changedAt": "2026-01-16T10:30:00Z",
      "changedByUserId": "660e8400-e29b-41d4-a716-446655440001",
      "changedByName": "User 660e8400",
      "reason": "Borrower submitted application"
    }
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| applicationId | UUID | Application identifier |
| currentStatus | String | Current application status (DRAFT, SUBMITTED, UNDER_REVIEW, OFFERS_AVAILABLE, ACCEPTED, COMPLETED, etc.) |
| submittedAt | ISO-8601 DateTime | Timestamp when application was submitted (null if still DRAFT) |
| createdAt | ISO-8601 DateTime | Timestamp when application was created |
| progressionPercentage | Integer | Workflow progression (0-100): DRAFT=17%, SUBMITTED=33%, UNDER_REVIEW=50%, OFFERS=67%, ACCEPTED=83%, COMPLETED=100% |
| statusHistory | Array | List of status transitions in reverse chronological order (newest first) |

**Status History Entry Fields:**

| Field | Type | Description |
|-------|------|-------------|
| oldStatus | String | Previous application status |
| newStatus | String | New application status |
| changedAt | ISO-8601 DateTime | When the transition occurred |
| changedByUserId | UUID | User who made the change (null if system-initiated) |
| changedByName | String | Display name of user or "System" |
| reason | String | Reason or description of the status change |

**Error Responses:**

**403 Forbidden** - Not the application owner
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to view this application",
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
- Status history is returned in reverse chronological order (most recent first)
- Only borrower who owns the application can view status
- Progression percentage helps visualize workflow completion
- Timeline shows all status transitions for audit and visibility
- Real-time data: reflects current database state

**Application Status Workflow:**
- DRAFT → SUBMITTED (when borrower clicks submit)
- SUBMITTED → UNDER_REVIEW (when bank starts review)
- UNDER_REVIEW → OFFERS_AVAILABLE (when banks submit offers) or REJECTED
- OFFERS_AVAILABLE → ACCEPTED (when borrower selects offer) or EXPIRED
- ACCEPTED → COMPLETED (when loan is funded)
- Any status → WITHDRAWN (if borrower cancels)

**cURL Example:**
```bash
curl -X GET https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

### POST /api/borrower/applications/{applicationId}/withdraw

**Description:** Withdraw a borrower application from the review process. Once withdrawn, the application status changes to WITHDRAWN and banks stop evaluating it.

**Authentication:** Required (BORROWER role)

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| applicationId | UUID | The ID of the application to withdraw |

**Request Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "withdrawalReason": "Found better offer elsewhere"
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| withdrawalReason | String | No | Optional reason for withdrawal (max 500 characters) |

**Success Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "WITHDRAWN",
  "withdrawnAt": "2026-01-16T14:25:30Z",
  "withdrawalReason": "Found better offer elsewhere",
  "message": "Your application has been withdrawn successfully."
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| id | UUID | Application ID |
| status | String | Will always be "WITHDRAWN" |
| withdrawnAt | String | ISO-8601 timestamp when application was withdrawn |
| withdrawalReason | String | The reason provided (if any) |
| message | String | Confirmation message |

**Error Responses:**

**403 Forbidden** - Not the application owner
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to withdraw this application",
  "timestamp": "2026-01-16T14:25:30Z"
}
```

**404 Not Found** - Application not found
```json
{
  "error": "Not Found",
  "message": "Application not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-16T14:25:30Z"
}
```

**409 Conflict** - Application cannot be withdrawn (terminal state)
```json
{
  "error": "Cannot Withdraw",
  "message": "Cannot withdraw application in status: ACCEPTED",
  "currentStatus": "ACCEPTED",
  "timestamp": "2026-01-16T14:25:30Z"
}
```

**Notes:**
- Can only withdraw applications in: DRAFT, SUBMITTED, UNDER_REVIEW, or OFFERS_AVAILABLE states
- Cannot withdraw applications in terminal states: ACCEPTED, REJECTED, EXPIRED, COMPLETED, WITHDRAWN
- Withdrawal records audit trail with status transition history
- Borrower receives confirmation email after withdrawal (async)
- Withdrawal reason is optional but recommended for analytics
- Once withdrawn, application status cannot be changed back

**Withdrawable States:**
- **DRAFT**: Cancel application before submission
- **SUBMITTED**: Withdraw after submission but before underwriting
- **UNDER_REVIEW**: Withdraw during bank review process
- **OFFERS_AVAILABLE**: Withdraw after receiving offers but before acceptance

**Terminal States (Cannot Withdraw):**
- **ACCEPTED**: Loan already accepted
- **REJECTED**: Application already rejected by banks
- **EXPIRED**: Offers already expired
- **COMPLETED**: Loan process completed
- **WITHDRAWN**: Already withdrawn

**cURL Example:**
```bash
curl -X POST https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/withdraw \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{"withdrawalReason":"Found better offer elsewhere"}'
```

---

## Help Content API

### GET /api/help/topics
**Description:** List published help topics for a given language.

**Authentication:** None required (public endpoint)

**Request Method:** GET

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| language | String | No | en | Language code (ISO-2), falls back to 'en' if not provided |

**Response (200 OK):** Returns an array of HelpArticleListDTO

**Response Fields:**
- topic: String (unique identifier of the topic)
- title: String (localized title)
- description: String (short summary)
- language: String (language code)

**Example Response:**
```json
[
  { "topic": "loan-types", "title": "Loan Types Overview", "description": "Compare common loan types and suitability", "language": "en" },
  { "topic": "application-requirements", "title": "Application Requirements", "description": "What you need to submit an application", "language": "en" },
  { "topic": "document-checklist", "title": "Document Checklist", "description": "Required documents by loan type", "language": "en" }
]
```

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/help/topics?language=en"
```

---

### GET /api/help/{topic}
**Description:** Retrieve a published help article by topic with ordered sections and FAQs. If the requested language is not available, returns the English version.

**Authentication:** None required (public endpoint)

**Request Method:** GET

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| topic | String | Yes | Topic identifier (e.g., loan-types) |

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| language | String | No | en | Language code (ISO-2). Falls back to English if not available |

**Response (200 OK):** Returns a HelpArticleDTO

**Response Fields:**
- id: UUID
- topic: String
- title: String
- description: String
- content: String (rich text)
- version: Integer
- language: String
- sections: Array<HelpSectionDTO> { heading, content, order }
- faqs: Array<HelpFAQDTO> { question, answer, order }
- lastUpdated: ISO-8601 DateTime

**Example Response:**
```json
{
  "id": "c1f1b5b2-0000-0000-0000-000000000001",
  "topic": "loan-types",
  "title": "Loan Types Overview",
  "description": "Compare common loan types and suitability",
  "content": "Learn about personal, home, auto, and business loans.",
  "version": 1,
  "language": "en",
  "sections": [
    { "heading": "Personal Loans", "content": "Unsecured loans for personal needs.", "order": 1 },
    { "heading": "Home Loans", "content": "Mortgages for purchasing property.", "order": 2 }
  ],
  "faqs": [
    { "question": "Which loan is best for me?", "answer": "It depends on your needs and eligibility.", "order": 1 }
  ],
  "lastUpdated": "2026-01-17T10:00:00Z"
}
```

**Error Responses:**

**404 Not Found** - Article not found for given topic
```json
{
  "error": "Not Found",
  "message": "Help article not found for topic 'unknown-topic'",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

**Example cURL:**
```bash
curl -X GET "http://localhost:8080/api/help/loan-types?language=en"
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
| /api/borrower/applications/{id}/status | GET |  | ✓ |  |  |
| /api/borrower/applications/{id}/offers | GET |  | ✓ |  |  |
| /api/borrower/applications/{id}/withdraw | POST |  | ✓ |  |  |
| /api/help/topics | GET | ✓ |  |  |  |
| /api/help/{topic} | GET | ✓ |  |  |  |
| /api/health | GET |  |  |  |  |

---

## Offer Data Model & Bank Rate Cards

### Overview
This section documents the data model for offers and bank rate cards used in the marketplace. These entities represent:
- **BankRateCard**: Bank's loan calculator configuration (loan type, APR, fees, etc.)
- **Offer**: Preliminary offer created by the bank for a borrower's application
- **OfferCalculationLog**: Audit trail of calculations performed to generate offers

### Offer Entity

**Description:** Represents a preliminary offer from a bank for a borrower's application.

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | UUID | Yes | Unique identifier (manually assigned UUID) |
| applicationId | UUID | Yes | Foreign key to Application |
| bankId | UUID | Yes | Foreign key to Organization (Bank) |
| offerStatus | String (enum) | Yes | Status: CALCULATED, SUBMITTED, ACCEPTED, REJECTED, EXPIRED, WITHDRAWN |
| apr | BigDecimal | Yes | Annual Percentage Rate (0.5-50%) |
| monthlyPayment | BigDecimal | Yes | Calculated monthly payment amount |
| totalCost | BigDecimal | Yes | Total cost of the loan (principal + interest) |
| originationFee | BigDecimal | Yes | One-time origination fee amount |
| insuranceCost | BigDecimal | No | Optional insurance cost |
| processingTimeDays | Integer | Yes | Days until approval (1-30) |
| validityPeriodDays | Integer | Yes | Days offer remains valid (7-90) |
| requiredDocuments | String | No | Comma-separated list of required documents |
| createdAt | LocalDateTime | Yes | Timestamp when offer was created (auto-set) |
| expiresAt | LocalDateTime | Yes | Timestamp when offer expires (created_at + validity_period_days) |
| offerSubmittedAt | LocalDateTime | No | Timestamp when borrower submitted intent to accept |
| updatedAt | LocalDateTime | Yes | Timestamp of last update (auto-set) |

**Example JSON Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "applicationId": "660e8400-e29b-41d4-a716-446655440001",
  "bankId": "770e8400-e29b-41d4-a716-446655440002",
  "offerStatus": "CALCULATED",
  "apr": 8.5,
  "monthlyPayment": 850.00,
  "totalCost": 25500.00,
  "originationFee": 2550.00,
  "insuranceCost": 150.00,
  "processingTimeDays": 7,
  "validityPeriodDays": 30,
  "requiredDocuments": "proof_of_income,identity_verification",
  "createdAt": "2026-01-17T10:30:00Z",
  "expiresAt": "2026-02-16T10:30:00Z",
  "offerSubmittedAt": null,
  "updatedAt": "2026-01-17T10:30:00Z"
}
```

**Relationships:**
- **Many-to-One to Application**: Each offer relates to one application (via applicationId)
- **Many-to-One to Organization**: Each offer is created by one bank (via bankId)
- **One-to-Many from OfferCalculationLog**: Multiple calculation logs may reference an offer

**Key Constraints:**
- `applicationId` and `bankId` are foreign keys (required, ON DELETE CASCADE for applications, ON DELETE RESTRICT for banks)
- `apr` value: 0.5 to 50.0%
- `validityPeriodDays` value: 7 to 90 days
- `monthlyPayment`, `totalCost`, `originationFee` must be positive decimals

---

### BankRateCard Entity

**Description:** Represents a bank's loan calculator configuration for a specific loan type and currency. Rate cards are versioned via `validFrom` and `validTo` timestamps.

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | UUID | Yes | Unique identifier |
| bankId | UUID | Yes | Foreign key to Organization (Bank) |
| loanType | String (enum) | Yes | PERSONAL, HOME, AUTO, DEBT_CONSOLIDATION, STUDENT, BUSINESS, OTHER |
| currency | String (enum) | Yes | EUR, USD, MDL |
| minLoanAmount | BigDecimal | Yes | Minimum loan amount (100+) |
| maxLoanAmount | BigDecimal | Yes | Maximum loan amount (100-1,000,000) |
| baseApr | BigDecimal | Yes | Base Annual Percentage Rate (0.5-50%) |
| aprAdjustmentRange | BigDecimal | Yes | APR adjustment range (0-5%) for risk adjustment |
| originationFeePercent | BigDecimal | Yes | Origination fee as percentage (0-10%) |
| insurancePercent | BigDecimal | No | Insurance cost as percentage (0-5%) |
| processingTimeDays | Integer | Yes | Processing time in days (1-30) |
| validFrom | LocalDateTime | Yes | When this rate card becomes active |
| validTo | LocalDateTime | No | When this rate card becomes inactive (null = currently active) |
| createdAt | LocalDateTime | Yes | Timestamp of creation (auto-set) |
| updatedAt | LocalDateTime | Yes | Timestamp of last update (auto-set) |

**Example JSON Response:**
```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "bankId": "770e8400-e29b-41d4-a716-446655440002",
  "loanType": "PERSONAL",
  "currency": "EUR",
  "minLoanAmount": 5000.00,
  "maxLoanAmount": 100000.00,
  "baseApr": 8.5,
  "aprAdjustmentRange": 3.0,
  "originationFeePercent": 2.5,
  "insurancePercent": 0.5,
  "processingTimeDays": 7,
  "validFrom": "2026-01-17T00:00:00Z",
  "validTo": null,
  "createdAt": "2026-01-17T10:30:00Z",
  "updatedAt": "2026-01-17T10:30:00Z",
  "active": true
}
```

**Versioning Strategy:**
- When a bank updates rate cards, the old card is marked inactive by setting `validTo` to current timestamp
- New card is created with `validFrom` = current timestamp and `validTo` = null
- All historical versions are preserved for audit trail
- Active cards always have `validTo` IS NULL

**Relationships:**
- **Many-to-One to Organization**: Each rate card belongs to one bank
- **One-to-Many from Offer**: Rate cards guide offer calculations

**Key Constraints:**
- Unique constraint on (bankId, loanType, currency, validTo) per time period
- `minLoanAmount` must be less than `maxLoanAmount`
- Both amount fields must be >= 100
- APR, fees, and insurance percentages within specified ranges

---

### OfferCalculationLog Entity

**Description:** Immutable audit trail of all calculations performed to generate offers. Stores input parameters and calculated values as JSONB for flexible audit tracking.

**Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long | Yes | Auto-incremented sequential ID |
| applicationId | UUID | Yes | Foreign key to Application |
| bankId | UUID | No | Foreign key to Organization (Bank) - nullable for audit |
| calculationMethod | String | Yes | Name of calculation method used |
| inputParameters | String (JSONB) | Yes | JSON object of input parameters used |
| calculatedValues | String (JSONB) | Yes | JSON object of calculation results |
| calculationType | String (enum) | Yes | MOCK_CALCULATION, REAL_API, OVERRIDE |
| timestamp | LocalDateTime | Yes | When calculation occurred |
| createdAt | LocalDateTime | Yes | Timestamp of log creation (auto-set) |

**Example JSON Response:**
```json
{
  "id": 1001,
  "applicationId": "660e8400-e29b-41d4-a716-446655440001",
  "bankId": "770e8400-e29b-41d4-a716-446655440002",
  "calculationMethod": "standard_affordability_check",
  "inputParameters": {
    "loanType": "PERSONAL",
    "loanAmount": 25000,
    "loanTermMonths": 36,
    "currency": "EUR",
    "borrowerIncome": 3500,
    "existingDebts": 500,
    "riskScore": 720
  },
  "calculatedValues": {
    "apr": 8.5,
    "monthlyPayment": 850.00,
    "totalCost": 25500.00,
    "originationFee": 2550.00,
    "insuranceCost": 150.00,
    "processingTimeDays": 7,
    "affordabilityRatio": 0.22,
    "approvalProbability": 0.92
  },
  "calculationType": "REAL_API",
  "timestamp": "2026-01-17T10:30:00Z",
  "createdAt": "2026-01-17T10:30:00Z"
}
```

**Input Parameters (JSONB):**
Common fields logged for all calculations:
- `loanType`: Type of loan being considered
- `loanAmount`: Requested loan amount
- `loanTermMonths`: Requested loan term
- `currency`: Loan currency
- `borrowerIncome`: Borrower's annual income
- `existingDebts`: Existing monthly debt obligations
- `riskScore`: Credit/risk score from evaluation
- `employmentStatus`: Borrower's employment status
- `downPayment`: Down payment amount (if applicable)

**Calculated Values (JSONB):**
Results of the calculation:
- `apr`: Calculated Annual Percentage Rate
- `monthlyPayment`: Calculated monthly payment
- `totalCost`: Total loan cost (principal + interest)
- `originationFee`: Calculated origination fee
- `insuranceCost`: Calculated insurance cost
- `processingTimeDays`: Processing time estimate
- `affordabilityRatio`: Debt-to-income ratio
- `approvalProbability`: Probability of approval (0.0-1.0)

**Relationships:**
- **Many-to-One to Application**: Multiple calculations per application
- **Many-to-One to Organization**: Bank that performed calculation

**Key Constraints:**
- `inputParameters` and `calculatedValues` stored as JSONB for flexibility
- `timestamp` must be set (required, not null)
- Append-only: no updates or deletes allowed (immutable audit trail)
- Created with FK ON DELETE CASCADE for applications, ON DELETE SET NULL for banks

**Audit Trail Use:**
- Every offer calculation is logged for compliance
- Full input parameters and results preserved
- Supports regulatory audits and dispute resolution
- Enables tracing of why specific offers were generated

---

## Borrower Offer Retrieval API

### GET /api/borrower/applications/{applicationId}/offers
**Description:** Retrieve and compare all preliminary offers for a specific application. Offers are sorted by APR in ascending order (lowest APR first).

**Authentication:** Required - BORROWER role

**Request Method:** GET

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| applicationId | UUID | Yes | Unique identifier of the application |

**Response (200 OK):** Returns OfferComparisonResponse with offers sorted by APR

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| offers | Array<OfferComparisonDTO> | Array of offers sorted by APR ascending |
| totalOffersCount | Integer | Total number of offers (auto-calculated from offers array length) |
| disclaimer | String | Static disclaimer about preliminary nature of offers |
| retrievedAt | ISO-8601 DateTime | Timestamp when offers were retrieved |
| nextRefreshAvailableAt | ISO-8601 DateTime | Timestamp when offer data can be refreshed (now + 5 minutes for caching) |
| applicationId | UUID | The requested application ID |

**OfferComparisonDTO Fields (8-Field Comparison Schema):**

| Field | Type | Description |
|-------|------|-------------|
| offerId | UUID | Unique identifier of the offer |
| bankId | UUID | Bank/Organization ID |
| bankName | String | Display name of the bank |
| logoUrl | String | URL to bank logo for UI display |
| apr | BigDecimal | Annual Percentage Rate (4 decimal places) |
| monthlyPayment | BigDecimal | Regular monthly payment amount (2 decimal places) |
| totalCost | BigDecimal | Sum of all payments minus principal (2 decimal places) |
| originationFee | BigDecimal | Upfront fee charged by bank (2 decimal places) |
| insuranceCost | BigDecimal | Optional loan insurance cost (2 decimal places) |
| processingTimeDays | Integer | Days to approval |
| validityPeriodDays | Integer | Days the offer remains valid |
| requiredDocuments | Array<String> | List of required documents for this offer |
| expiresAt | ISO-8601 DateTime | When the offer expires |
| offerStatus | String | Current status of the offer (CALCULATED, SUBMITTED, ACCEPTED, REJECTED, EXPIRED, WITHDRAWN) |

**Example Response (200 OK):**
```json
{
  "applicationId": "550e8400-e29b-41d4-a716-446655440000",
  "offers": [
    {
      "offerId": "660e8400-e29b-41d4-a716-446655440001",
      "bankId": "770e8400-e29b-41d4-a716-446655440002",
      "bankName": "First National Bank",
      "logoUrl": "https://firstbank.com/logo.png",
      "apr": 3.50,
      "monthlyPayment": 1500.00,
      "totalCost": 45000.00,
      "originationFee": 500.00,
      "insuranceCost": 200.00,
      "processingTimeDays": 5,
      "validityPeriodDays": 30,
      "requiredDocuments": ["ID", "Recent Paycheck", "Tax Return"],
      "expiresAt": "2026-02-17T12:00:00Z",
      "offerStatus": "CALCULATED"
    },
    {
      "offerId": "880e8400-e29b-41d4-a716-446655440003",
      "bankId": "770e8400-e29b-41d4-a716-446655440002",
      "bankName": "First National Bank",
      "logoUrl": "https://firstbank.com/logo.png",
      "apr": 4.25,
      "monthlyPayment": 1520.00,
      "totalCost": 46000.00,
      "originationFee": 600.00,
      "insuranceCost": 250.00,
      "processingTimeDays": 7,
      "validityPeriodDays": 30,
      "requiredDocuments": ["ID", "Recent Bank Statement"],
      "expiresAt": "2026-02-17T12:00:00Z",
      "offerStatus": "CALCULATED"
    }
  ],
  "totalOffersCount": 2,
  "disclaimer": "These are preliminary offers based on estimated calculations. Final terms may vary after formal review by the bank.",
  "retrievedAt": "2026-01-17T14:30:00Z",
  "nextRefreshAvailableAt": "2026-01-17T14:35:00Z"
}
```

**Error Responses:**

**400 Bad Request** - Application not in SUBMITTED status or later
```json
{
  "error": "Bad Request",
  "message": "Application must be in SUBMITTED status or later to view offers",
  "timestamp": "2026-01-17T14:30:00Z"
}
```

**403 Forbidden** - Borrower does not own this application
```json
{
  "error": "Forbidden",
  "message": "Borrower does not own this application",
  "timestamp": "2026-01-17T14:30:00Z"
}
```

**404 Not Found** - Application not found
```json
{
  "error": "Not Found",
  "message": "Application not found with ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2026-01-17T14:30:00Z"
}
```

**Performance & Caching:**
- Response time: < 100ms for typical load
- Offers cached for 5 minutes (nextRefreshAvailableAt indicates when refresh is available)
- Use Spring Cache abstraction with configurable backend (Redis, Ehcache, etc.)
- Caching key: applicationId + borrowerId
- Cache invalidation: automatic on expiration or manual when new offers added

**Features:**
- Automatic APR sorting (ascending = lowest rate first)
- Bank logo fallback to placeholder if not configured
- Required documents parsed from comma-separated strings to arrays
- N+1 query prevention: single offer query + batch bank details fetch
- Read-only access (borrower cannot modify offers through this endpoint)
- All dates returned in ISO-8601 UTC format

**Authorization:**
- Only BORROWER role can access this endpoint
- Borrower can only view offers for their own applications
- Returns 403 Forbidden if accessing another borrower's application

**cURL Example:**
```bash
curl -X GET https://api.creditapp.com/api/borrower/applications/550e8400-e29b-41d4-a716-446655440000/offers \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json"
```

---

## Bank Rate Card Configuration API

### POST /api/bank/rate-cards
**Description:** Create a new rate card configuration for a specific loan type and currency. If an active rate card exists for the same loan type and currency, it is marked as inactive (versioning).

**Authentication:** Required - BANK_ADMIN role

**Request Method:** POST

**Request Headers:**
- Authorization: Bearer {jwt_token}
- Content-Type: application/json

**Request Body:**
```json
{
  "loanType": "PERSONAL",
  "currency": "EUR",
  "minLoanAmount": 5000,
  "maxLoanAmount": 100000,
  "baseApr": 8.5,
  "aprAdjustmentRange": 3.0,
  "originationFeePercent": 2.5,
  "insurancePercent": 0.5
}
```

**Request Parameters:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|-----------|-------------|
| loanType | String | Yes | Enum (PERSONAL, HOME, AUTO, etc.) | Type of loan |
| currency | String | Yes | Enum (EUR, USD, MDL) | Currency of the loan |
| minLoanAmount | BigDecimal | Yes | 100-1,000,000 | Minimum loan amount |
| maxLoanAmount | BigDecimal | Yes | >= minLoanAmount, <= 1,000,000 | Maximum loan amount |
| baseApr | BigDecimal | Yes | 0.5-50.0 | Base Annual Percentage Rate |
| aprAdjustmentRange | BigDecimal | Yes | 0-5.0 | APR adjustment range (%) |
| originationFeePercent | BigDecimal | Yes | 0-10.0 | Origination fee as percentage |
| insurancePercent | BigDecimal | Yes | 0-5.0 | Insurance cost as percentage |

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "loanType": "PERSONAL",
  "currency": "EUR",
  "minLoanAmount": 5000,
  "maxLoanAmount": 100000,
  "baseApr": 8.5,
  "aprAdjustmentRange": 3.0,
  "originationFeePercent": 2.5,
  "insurancePercent": 0.5,
  "processingTimeDays": null,
  "validFrom": "2026-01-17T10:30:00Z",
  "validTo": null,
  "active": true
}
```

**Error Responses:**

**400 Bad Request** - Validation failed
```json
{
  "error": "Invalid Rate Card",
  "message": "Minimum loan amount must be less than maximum loan amount",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

**403 Forbidden** - Not BANK_ADMIN role
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

---

### GET /api/bank/rate-cards
**Description:** Retrieve all active rate cards for the authenticated bank. Returns only rate cards with validTo = NULL.

**Authentication:** Required - BANK_ADMIN role

**Request Method:** GET

**Request Headers:**
- Authorization: Bearer {jwt_token}

**Query Parameters:** None

**Response (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "loanType": "PERSONAL",
    "currency": "EUR",
    "minLoanAmount": 5000,
    "maxLoanAmount": 100000,
    "baseApr": 8.5,
    "aprAdjustmentRange": 3.0,
    "originationFeePercent": 2.5,
    "insurancePercent": 0.5,
    "processingTimeDays": null,
    "validFrom": "2026-01-17T10:30:00Z",
    "validTo": null,
    "active": true
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "loanType": "HOME",
    "currency": "EUR",
    "minLoanAmount": 50000,
    "maxLoanAmount": 500000,
    "baseApr": 5.2,
    "aprAdjustmentRange": 2.5,
    "originationFeePercent": 1.5,
    "insurancePercent": 0.3,
    "processingTimeDays": null,
    "validFrom": "2026-01-15T08:00:00Z",
    "validTo": null,
    "active": true
  }
]
```

**Error Responses:**

**403 Forbidden** - Not BANK_ADMIN role
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

---

### PUT /api/bank/rate-cards/{rateCardId}
**Description:** Update a rate card by creating a new version. The old rate card is marked as inactive (validTo = now), and a new card is created with the updated parameters.

**Authentication:** Required - BANK_ADMIN role

**Request Method:** PUT

**Request Headers:**
- Authorization: Bearer {jwt_token}
- Content-Type: application/json

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| rateCardId | UUID | Yes | ID of the rate card to update |

**Request Body:**
```json
{
  "loanType": "PERSONAL",
  "currency": "EUR",
  "minLoanAmount": 5000,
  "maxLoanAmount": 100000,
  "baseApr": 8.75,
  "aprAdjustmentRange": 3.0,
  "originationFeePercent": 2.5,
  "insurancePercent": 0.5
}
```

**Response (200 OK):**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440002",
  "loanType": "PERSONAL",
  "currency": "EUR",
  "minLoanAmount": 5000,
  "maxLoanAmount": 100000,
  "baseApr": 8.75,
  "aprAdjustmentRange": 3.0,
  "originationFeePercent": 2.5,
  "insurancePercent": 0.5,
  "processingTimeDays": null,
  "validFrom": "2026-01-17T11:00:00Z",
  "validTo": null,
  "active": true
}
```

**Note:** The response includes a new ID for the updated rate card (new version). The original rate card is marked inactive with validTo timestamp but remains in the database for audit history.

**Error Responses:**

**400 Bad Request** - Validation failed
```json
{
  "error": "Invalid Rate Card",
  "message": "Base APR must be between 0.5 and 50.0",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

**403 Forbidden** - Not BANK_ADMIN role or rate card belongs to different bank
```json
{
  "error": "Forbidden",
  "message": "Rate card does not belong to your bank",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

**404 Not Found** - Rate card not found
```json
{
  "error": "Not Found",
  "message": "Rate card with ID 550e8400-e29b-41d4-a716-446655440000 not found",
  "timestamp": "2026-01-17T10:30:00Z"
}
```

---

## Offer Calculation API

### Overview: Offer Calculation Flow

The Offer Calculation Engine automatically generates preliminary loan offers when a borrower submits an application. This is an **internal process** - no external endpoints are exposed directly for calculation, but this section documents how the calculation works and what data is produced.

**Flow:**
1. Borrower submits application (POST /api/borrower/applications/{applicationId}/submit)
2. ApplicationService triggers OfferCalculationService asynchronously
3. For each bank with active rate cards matching the loan type/currency:
   - Retrieve bank's rate card configuration
   - Calculate offer using simulated bank formulas
   - Store Offer entity with status CALCULATED
   - Log calculation details in OfferCalculationLog
4. Borrower receives notification: "Offers Available"
5. Borrower retrieves offers (GET /api/borrower/applications/{applicationId}/offers)

**Note:** This is a **mock/simulated** approach for MVP. No external bank APIs are called. Calculations use locally-configured rate cards (Story 3.2) to simulate what each bank's calculator would return.

---

### Internal Calculation Process

**Triggered By:** Application submission (ApplicationService)

**Input Parameters:**
- Application: loan amount, term (months), loan type, currency
- BankRateCard: base APR, APR adjustment range, origination fee %, insurance %

**Calculation Steps:**

1. **APR Determination**
   - Base APR from rate card
   - If loan term > 120 months: Add half of APR adjustment range
   - Formula: `Final APR = Base APR + (APR Adjustment Range ÷ 2)` if term > 120
   - Example: 8.5% base + (2.0% ÷ 2) = 9.5% final APR for 15-year loan

2. **Monthly Payment Calculation (Amortization)**
   - Formula: `M = P × [r(1 + r)^n] / [(1 + r)^n - 1]`
   - Where:
     - M = Monthly Payment
     - P = Principal (loan amount)
     - r = Monthly interest rate (APR ÷ 100 ÷ 12)
     - n = Number of payments (loan term in months)
   - Example: $25,000 @ 8.5% for 36 months = $788.67/month

3. **Fee Calculations**
   - Origination Fee = Loan Amount × (Origination Fee % ÷ 100)
   - Insurance Cost = Loan Amount × (Insurance % ÷ 100), or $0 if null
   - Example: $25,000 loan → $625 origination (2.5%) + $125 insurance (0.5%)

4. **Total Cost**
   - Total Interest = (Monthly Payment × Term) - Principal
   - Total Cost = (Monthly Payment × Term)
   - Example: $788.67 × 36 = $28,392.12 total cost

**Output:**
- Offer entity: APR, monthly payment, origination fee, insurance cost, total cost, required documents, validity period (24 hours)
- OfferCalculationLog: All input/output values, calculation type (MOCK_CALCULATION), timestamp

---

### Calculation Examples

#### Example 1: Standard Personal Loan
**Input:**
- Loan Amount: $25,000
- Loan Term: 36 months
- Loan Type: PERSONAL
- Currency: EUR
- Bank Rate Card: Base APR 8.5%, Origination 2.5%, Insurance 0.5%

**Calculation:**
- Monthly Rate: 8.5% ÷ 100 ÷ 12 = 0.00708333
- Monthly Payment: $25,000 × [0.00708333(1.00708333)^36] / [(1.00708333)^36 - 1] = **$788.67**
- Total Interest: ($788.67 × 36) - $25,000 = $3,392.12
- Total Cost: $788.67 × 36 = **$28,392.12**
- Origination Fee: $25,000 × 0.025 = **$625.00**
- Insurance Cost: $25,000 × 0.005 = **$125.00**

**Offer Result:**
```json
{
  "id": "aa0e8400-e29b-41d4-a716-446655440010",
  "applicationId": "990e8400-e29b-41d4-a716-446655440009",
  "bankId": "550e8400-e29b-41d4-a716-446655440000",
  "bankName": "Example Bank",
  "loanAmount": 25000.00,
  "loanTermMonths": 36,
  "apr": 8.5000,
  "monthlyPayment": 788.67,
  "totalCost": 28392.12,
  "originationFee": 625.00,
  "insuranceCost": 125.00,
  "requiredDocuments": ["ID", "PROOF_OF_INCOME", "BANK_STATEMENT"],
  "status": "CALCULATED",
  "offerValidUntil": "2026-01-18T14:30:00Z",
  "createdAt": "2026-01-17T14:30:00Z"
}
```

#### Example 2: Long-Term Loan with APR Adjustment
**Input:**
- Loan Amount: $50,000
- Loan Term: 180 months (15 years)
- Bank Rate Card: Base APR 8.5%, APR Adjustment Range 2.0%

**Calculation:**
- APR Adjustment Applied: Term > 120 months → Add 2.0% ÷ 2 = +1.0%
- Final APR: 8.5% + 1.0% = **9.5%**
- Monthly Rate: 9.5% ÷ 100 ÷ 12 = 0.00791667
- Monthly Payment: $50,000 × [0.00791667(1.00791667)^180] / [(1.00791667)^180 - 1] = **$522.90**
- Total Cost: $522.90 × 180 = **$94,122.00**

#### Example 3: No Insurance
**Input:**
- Loan Amount: $25,000
- Insurance Percent: null (bank doesn't require insurance)

**Calculation:**
- Insurance Cost: **$0.00** (null insurance percent treated as zero)

---

### Offer Status: CALCULATED

**What it means:**
- Offer is **preliminary** and **simulated** (not submitted by bank)
- Based on bank's configured rate card formulas
- No external API calls were made
- Represents what the bank's calculator would likely return

**Next Steps:**
- Borrower reviews offers (Story 3.4)
- Borrower accepts offer (Story 3.5)
- In Phase 2: Real bank API integration replaces simulated calculations

---

### Asynchronous Execution

**Why Async?**
- Calculations run in background (don't block application submission)
- Multiple banks calculated in parallel
- Borrower receives "Application Submitted" confirmation immediately

**Implementation:**
- `@Async` annotation on `OfferCalculationService.calculateOffersForApplication()`
- ThreadPoolTaskExecutor with 10 threads
- Each bank calculation is independent

**Error Handling:**
- If one bank calculation fails, others proceed
- Failed calculations logged with error details
- Borrower still receives successful offers

---

### Calculation Determinism

**Requirement:** Identical inputs MUST produce identical outputs.

**Verified Behavior:**
- Same loan amount, term, and rate card → Same APR, payment, fees
- BigDecimal precision ensures no floating-point errors
- All calculations use HALF_UP rounding mode
- Monetary values: 2 decimal places
- APR: 4 decimal places

---

### Validity Period

**Default:** 24 hours from calculation timestamp

**Configuration:** `app.offer.validity.period.hours=24`

**Behavior:**
- After 24 hours: Offer status changes to EXPIRED
- Borrower must submit new application if offers expired
- Configurable per deployment environment

---

### Audit Trail

Every calculation is logged in `offer_calculation_logs` table:
- Input: loan amount, term, rate card ID
- Output: APR, monthly payment, fees, total cost
- Calculation type: MOCK_CALCULATION
- Timestamp: when calculation occurred
- Bank ID: which bank's rate card was used

**Purpose:**
- Regulatory compliance
- Debugging calculation issues
- Comparing simulated vs. real calculations (Phase 2)

---

### Related Entities

**Offer** (Story 3.1):
- Stores calculated offer details
- Linked to Application and Bank (Organization)
- Status: CALCULATED, ACCEPTED, REJECTED, EXPIRED

**BankRateCard** (Story 3.2):
- Bank-configured parameters for calculations
- Versioned (immutable history)
- Active rate cards used for calculations

**OfferCalculationLog** (Story 3.1):
- Immutable log of all calculation inputs/outputs
- Used for audit and analysis

---

### Known Test Cases

For detailed verified calculation examples, see [docs/CALCULATION_TEST_CASES.md](../CALCULATION_TEST_CASES.md):
- Test Case 1: $25,000 @ 8.5% for 36 months = $788.67/month
- Test Case 2: $25,000 @ 7.8% (no insurance) = $781.93/month
- Test Case 3: $25,000 @ 9.2% (higher rate) = $795.46/month
- Test Case 4: $50,000 @ 9.5% for 180 months = $522.90/month (APR adjustment)
- Test Case 5: $5,000 @ 8.5% for 24 months = $226.66/month

---

### Phase 2: Real Bank API Integration

**Current (MVP):** Mock/simulated calculations using local rate cards

**Future (Phase 2):** Replace with real bank API calls
- External API endpoints per bank
- Real-time offer generation
- Asynchronous polling for results
- Fallback to simulated if API unavailable

**Benefits of Simulated Approach:**
- Faster MVP development
- Predictable calculations for testing
- No dependency on external systems
- Benchmark for validating real API responses

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
| 2026-01-17 | 1.8 | Added Offer Calculation Engine documentation: internal calculation flow, formulas, examples, async execution, and known test cases | Story 3.3 |
| 2026-01-17 | 1.7 | Added Bank Rate Card Configuration API: POST, GET, PUT /api/bank/rate-cards endpoints for managing rate cards with versioning | Story 3.2 |
| 2026-01-17 | 1.6 | Added Offer, BankRateCard, and OfferCalculationLog data model documentation | Story 3.1 |
| 2026-01-17 | 1.5 | Added Help Content endpoints: topics listing and article retrieval | Story 2.10 |
| 2026-01-16 | 1.4 | Added application withdrawal endpoint allowing borrowers to cancel applications | Story 2.8 |
| 2026-01-16 | 1.3 | Added application status tracking endpoint with status history timeline | Story 2.7 |
| 2026-01-16 | 1.2 | Added document upload, list, and delete endpoints with file size limits and soft-delete pattern | Story 2.6 |
| 2026-01-16 | 1.1 | Added borrower application creation endpoint with validation and rate limiting | Story 2.2 |
| 2026-01-15 | 1.0 | Initial API documentation - Bank registration and activation endpoints | Story 1.4 |
