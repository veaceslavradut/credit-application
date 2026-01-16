-- V9__Create_Email_Templates_Table.sql
-- Create email templates and email delivery logs tables for notification service

-- Create email_templates table
CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    html_body TEXT NOT NULL,
    text_body TEXT NOT NULL,
    variables TEXT, -- JSON array of variable names as text
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on template_name for fast lookups
CREATE INDEX idx_email_templates_name ON email_templates(template_name);
CREATE INDEX idx_email_templates_active ON email_templates(active);

-- Create email_delivery_logs table
CREATE TABLE email_delivery_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_email VARCHAR(255) NOT NULL,
    template_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SENT', 'DELIVERED', 'BOUNCED', 'FAILED')),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for email_delivery_logs
CREATE INDEX idx_email_delivery_logs_recipient_sent ON email_delivery_logs(recipient_email, sent_at);
CREATE INDEX idx_email_delivery_logs_status_created ON email_delivery_logs(status, created_at);
CREATE INDEX idx_email_delivery_logs_template ON email_delivery_logs(template_name);

-- Insert default email templates
INSERT INTO email_templates (template_name, subject, html_body, text_body, variables, active) VALUES
('REGISTRATION_CONFIRMATION', 
 'Welcome to Credit Application Platform!',
 '<html><body><h1>Welcome, {firstName}!</h1><p>Your account has been successfully created.</p><p>Email: {email}</p><p>You can now log in at: <a href="{loginUrl}">{loginUrl}</a></p><p>If you have any questions, contact us at {supportEmail}</p></body></html>',
 'Welcome, {firstName}! Your account has been successfully created. Email: {email}. You can now log in at: {loginUrl}. If you have any questions, contact us at {supportEmail}',
 '["firstName", "lastName", "email", "loginUrl", "supportEmail"]',
 TRUE),

('APPLICATION_SUBMITTED',
 'Your Loan Application Has Been Submitted',
 '<html><body><h1>Application Submitted Successfully</h1><p>Dear {firstName},</p><p>Your loan application for {loanAmount} {currency} has been submitted and is now being reviewed by our partner banks.</p><p>Application ID: {applicationId}</p><p>Loan Type: {loanType}</p><p>Amount: {loanAmount} {currency}</p><p>Term: {loanTermMonths} months</p><p>You will receive updates as banks review your application.</p></body></html>',
 'Dear {firstName}, Your loan application for {loanAmount} {currency} has been submitted and is now being reviewed by our partner banks. Application ID: {applicationId}. You will receive updates as banks review your application.',
 '["firstName", "loanAmount", "currency", "applicationId", "loanType", "loanTermMonths"]',
 TRUE),

('APPLICATION_VIEWED_BY_BANK',
 'A Bank Has Viewed Your Application',
 '<html><body><h1>Good News!</h1><p>Dear {firstName},</p><p>A partner bank has viewed your loan application (ID: {applicationId}).</p><p>They are currently evaluating your request and may send you a preliminary offer soon.</p></body></html>',
 'Dear {firstName}, A partner bank has viewed your loan application (ID: {applicationId}). They are currently evaluating your request and may send you a preliminary offer soon.',
 '["firstName", "applicationId"]',
 TRUE),

('PRELIMINARY_OFFER_RECEIVED',
 'You Have Received a New Loan Offer!',
 '<html><body><h1>New Offer Available!</h1><p>Dear {firstName},</p><p>Congratulations! You have received a preliminary offer from {bankName}.</p><p>Offer Details:</p><ul><li>APR: {apr}%</li><li>Monthly Payment: {monthlyPayment} {currency}</li><li>Total Cost: {totalCost} {currency}</li></ul><p><a href="{offerUrl}">View Offer Details</a></p></body></html>',
 'Dear {firstName}, Congratulations! You have received a preliminary offer from {bankName}. APR: {apr}%. Monthly Payment: {monthlyPayment} {currency}. View details at: {offerUrl}',
 '["firstName", "bankName", "apr", "monthlyPayment", "totalCost", "currency", "offerUrl"]',
 TRUE),

('OFFER_ACCEPTED',
 'Your Loan Offer Has Been Accepted',
 '<html><body><h1>Offer Accepted</h1><p>Dear {firstName},</p><p>You have successfully accepted the loan offer from {bankName}.</p><p>The bank will contact you shortly with next steps.</p><p>Offer ID: {offerId}</p></body></html>',
 'Dear {firstName}, You have successfully accepted the loan offer from {bankName}. The bank will contact you shortly with next steps. Offer ID: {offerId}',
 '["firstName", "bankName", "offerId"]',
 TRUE),

('OFFER_EXPIRED',
 'Your Loan Offer Has Expired',
 '<html><body><h1>Offer Expired</h1><p>Dear {firstName},</p><p>Unfortunately, the loan offer from {bankName} (Offer ID: {offerId}) has expired.</p><p>You can submit a new application to receive updated offers.</p></body></html>',
 'Dear {firstName}, Unfortunately, the loan offer from {bankName} (Offer ID: {offerId}) has expired. You can submit a new application to receive updated offers.',
 '["firstName", "bankName", "offerId"]',
 TRUE),

('PASSWORD_RESET',
 'Password Reset Request',
 '<html><body><h1>Password Reset</h1><p>Dear {firstName},</p><p>You have requested to reset your password.</p><p>Click here to reset: <a href="{resetUrl}">{resetUrl}</a></p><p>This link expires in {expiryMinutes} minutes.</p><p>If you did not request this, please ignore this email.</p></body></html>',
 'Dear {firstName}, You have requested to reset your password. Click here to reset: {resetUrl}. This link expires in {expiryMinutes} minutes. If you did not request this, please ignore this email.',
 '["firstName", "resetUrl", "expiryMinutes"]',
 TRUE);
