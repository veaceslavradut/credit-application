# 2. DETAILED SERVICE ARCHITECTURE

## 2.1 User Service

**Responsibility:** Authentication, authorization, user profiles, KYC data management.

**Key Endpoints:**
```
POST   /api/users/register              # Register borrower or bank admin
POST   /api/users/login                 # JWT authentication
GET    /api/users/profile               # Get user profile
PUT    /api/users/profile               # Update profile
POST   /api/users/kyc                   # Submit KYC documents
GET    /api/users/kyc/status            # Get KYC verification status
POST   /api/auth/refresh                # Refresh JWT token
POST   /api/auth/logout                 # Logout & revoke token
```

**Database Schema (Excerpt):**
```sql
-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(20),
  role ENUM('BORROWER', 'BANK_ADMIN') NOT NULL,
  organization_id UUID,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Organizations (Banks) table
CREATE TABLE organizations (
  id UUID PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  tax_id VARCHAR(50) UNIQUE,
  country_code VARCHAR(2) DEFAULT 'MD',
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW()
);
```

**Security:**
- Passwords: bcrypt with salt factor 12
- Tokens: JWT (RS256 signature) with 15-minute expiration
- Refresh tokens stored in Redis with revocation capability

---

## 2.2 Application Service

**Responsibility:** Loan application submission, validation, status tracking, history management.

**Key Endpoints:**
```
POST   /api/applications                # Submit new loan application
GET    /api/applications/{id}           # Get application details
GET    /api/applications                # List borrower's applications
PUT    /api/applications/{id}           # Update application (draft state only)
GET    /api/applications/{id}/status    # Get application status
POST   /api/applications/{id}/consent   # Submit explicit consent
GET    /api/applications/{id}/history   # Get application status history
```

**Database Schema:**
```sql
-- Loan Applications
CREATE TABLE loan_applications (
  id UUID PRIMARY KEY,
  borrower_id UUID NOT NULL,
  loan_type VARCHAR(50) NOT NULL,
  loan_amount DECIMAL(15, 2) NOT NULL,
  loan_currency VARCHAR(3) DEFAULT 'MDL',
  loan_term_months INT NOT NULL,
  rate_type VARCHAR(20),
  annual_income DECIMAL(15, 2),
  employment_status VARCHAR(50),
  application_status VARCHAR(20) DEFAULT 'DRAFT',
  submitted_at TIMESTAMP,
  expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (borrower_id) REFERENCES users(id)
);

-- Consent & Data Sharing Audit
CREATE TABLE consents (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  borrower_id UUID NOT NULL,
  consent_type VARCHAR(100) NOT NULL,
  is_granted BOOLEAN NOT NULL,
  granted_at TIMESTAMP,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (application_id) REFERENCES loan_applications(id),
  FOREIGN KEY (borrower_id) REFERENCES users(id)
);
```

---

## 2.3 Offer Service

**Responsibility:** Preliminary offer calculation, offer management, comparison logic.

**Key Endpoints:**
```
POST   /api/offers                      # Create preliminary offer (internal)
GET    /api/offers/application/{id}     # Get all offers for application
GET    /api/offers/{id}                 # Get single offer details
POST   /api/offers/{id}/select          # Borrower selects offer
POST   /api/bank/offers                 # Bank submits binding offer
GET    /api/bank/applications           # Bank views application queue
PUT    /api/bank/offers/{id}            # Bank updates offer
```

**Database Schema:**
```sql
-- Rate Cards (configurable by bank)
CREATE TABLE rate_cards (
  id UUID PRIMARY KEY,
  organization_id UUID NOT NULL,
  loan_type VARCHAR(50) NOT NULL,
  min_amount DECIMAL(15, 2),
  max_amount DECIMAL(15, 2),
  min_term_months INT,
  max_term_months INT,
  base_apr_fixed DECIMAL(5, 3),
  base_apr_variable DECIMAL(5, 3),
  origination_fee_percent DECIMAL(5, 3),
  insurance_premium_percent DECIMAL(5, 3),
  processing_time_days INT,
  effective_from DATE,
  effective_to DATE,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Preliminary Offers (auto-calculated)
CREATE TABLE preliminary_offers (
  id UUID PRIMARY KEY,
  application_id UUID NOT NULL,
  organization_id UUID NOT NULL,
  loan_amount DECIMAL(15, 2) NOT NULL,
  loan_term_months INT NOT NULL,
  apr DECIMAL(5, 3) NOT NULL,
  monthly_payment DECIMAL(15, 2) NOT NULL,
  total_cost DECIMAL(15, 2) NOT NULL,
  origination_fee DECIMAL(15, 2),
  insurance_cost DECIMAL(15, 2),
  processing_time_days INT,
  rate_type VARCHAR(20),
  required_documents JSONB,
  validity_days INT DEFAULT 14,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT NOW(),
  FOREIGN KEY (application_id) REFERENCES loan_applications(id),
  FOREIGN KEY (organization_id) REFERENCES organizations(id),
  INDEX (application_id, created_at)
);
```

**Offer Calculation Algorithm (Preliminary):**
```
Given: Application (amount, term, loan_type, rate_type)
1. Load active RateCard for organization + loan_type
2. Validate amount within [min, max] range
3. Validate term within [min_term, max_term]
4. apr = base_apr (fixed/variable)
5. origination_fee = loan_amount × origination_fee_percent
6. insurance = loan_amount × insurance_premium_percent
7. monthly_payment = PMT formula (rate = apr/12, nper = term_months, pv = loan_amount)
8. total_cost = (monthly_payment × term_months) + origination_fee + insurance - loan_amount
9. Set expires_at = now + 14 days
10. Create PreliminaryOffer with status GENERATED
11. Auto-transition application to OFFERS_RECEIVED
12. Trigger notification to borrower
```

**Performance Target:** <500ms calculation (achieved via pre-loaded rate cards in Redis)

---
