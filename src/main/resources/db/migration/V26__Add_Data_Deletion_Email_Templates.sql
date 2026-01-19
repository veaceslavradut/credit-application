-- V26__Add_Data_Deletion_Email_Templates.sql
-- Insert email templates for data deletion requests

INSERT INTO email_templates (template_name, subject, html_body, text_body, variables, active, created_at, updated_at)
VALUES (
    'deletion-confirmation',
    'Confirm Your Data Deletion Request',
    '<html><head><style>body{font-family:Arial,sans-serif;color:#333}h1{color:#d32f2f}.container{max-width:600px;margin:0 auto;padding:20px}.warning{background-color:#fff3cd;border-left:4px solid #ffc107;padding:15px;margin:20px 0}.btn{background-color:#d32f2f;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;margin:15px 0;margin-right:10px}.btn-secondary{background-color:#6c757d}.footer{color:#7f8c8d;font-size:12px;margin-top:30px;border-top:1px solid #ecf0f1;padding-top:20px}</style></head><body><div class=\"container\"><h1>Confirm Your Data Deletion Request</h1><p>Hello {borrower_name},</p><p>You have requested to permanently delete your account and all associated personal data from our platform.</p><div class=\"warning\"><strong> WARNING:</strong><br>This action is <strong>irreversible</strong>. Once confirmed, your personal information will be permanently deleted, including:<ul><li>Your profile and personal details</li><li>Contact information and preferences</li><li>Communication history</li></ul><p>However, your application history and transaction records may be retained for regulatory compliance purposes.</p></div><p>To proceed with the deletion, click the button below within <strong>{expiry_days} days</strong>:</p><p><a href=\"{confirmation_link}\" class=\"btn\">Confirm Deletion</a></p><p>If you did not request this deletion or changed your mind, click below to cancel:</p><p><a href=\"{cancel_link}\" class=\"btn btn-secondary\">Cancel Request</a></p><p>This confirmation link expires on {expires_at}.</p><div class=\"footer\"><p>If you have any questions, please contact our support team.</p><p>&copy; Credit Application Platform. All rights reserved.</p></div></div></body></html>',
    'Hello {borrower_name},\n\nYou have requested to permanently delete your account and all associated personal data.\n\n  WARNING: This action is IRREVERSIBLE. Once confirmed, your personal information will be permanently deleted.\n\nTo confirm deletion, visit: {confirmation_link}\n\nTo cancel this request, visit: {cancel_link}\n\nThis link expires on {expires_at}.\n\nIf you have questions, please contact our support team.\n\nCopyright Credit Application Platform. All rights reserved.',
    '[\"borrower_name\",\"confirmation_link\",\"cancel_link\",\"expires_at\",\"expiry_days\"]',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'deletion-completed',
    'Your Account Has Been Deleted',
    '<html><head><style>body{font-family:Arial,sans-serif;color:#333}h1{color:#2c3e50}.container{max-width:600px;margin:0 auto;padding:20px}.info-box{background-color:#e3f2fd;border-left:4px solid #2196f3;padding:15px;margin:20px 0}.btn{background-color:#3498db;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;margin-top:20px}.footer{color:#7f8c8d;font-size:12px;margin-top:30px;border-top:1px solid #ecf0f1;padding-top:20px}</style></head><body><div class=\"container\"><h1>Your Account Has Been Deleted</h1><p>Hello {borrower_name},</p><p>Your deletion request has been processed successfully. Your personal data has been permanently removed from our system.</p><div class=\"info-box\"><strong>What has been deleted:</strong><ul><li>Your personal information and profile details</li><li>Contact information and communication preferences</li><li>Any linked accounts or preferences</li></ul></div><p>Your application and transaction history will be retained for regulatory and audit purposes, but all personally identifiable information has been anonymized.</p><p>If you wish to use our services again in the future, you are welcome to re-register with the same email address.</p><p><a href=\"{reregistration_link}\" class=\"btn\">Re-register</a></p><div class=\"footer\"><p>If you have any questions about your deletion, please contact our support team.</p><p>&copy; Credit Application Platform. All rights reserved.</p></div></div></body></html>',
    'Hello {borrower_name},\n\nYour deletion request has been processed successfully. Your personal data has been permanently removed from our system.\n\nWhat has been deleted:\n- Your personal information and profile details\n- Contact information and communication preferences\n- Any linked accounts or preferences\n\nYour application history will be retained for regulatory purposes, but all personally identifiable information has been anonymized.\n\nIf you wish to re-register in the future, visit: {reregistration_link}\n\nIf you have questions, please contact our support team.\n\nCopyright Credit Application Platform. All rights reserved.',
    '[\"borrower_name\",\"reregistration_link\"]',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'deletion-cancelled',
    'Your Data Deletion Request Has Been Cancelled',
    '<html><head><style>body{font-family:Arial,sans-serif;color:#333}h1{color:#2c3e50}.container{max-width:600px;margin:0 auto;padding:20px}.success-box{background-color:#f1f8e9;border-left:4px solid #4caf50;padding:15px;margin:20px 0}.footer{color:#7f8c8d;font-size:12px;margin-top:30px;border-top:1px solid #ecf0f1;padding-top:20px}</style></head><body><div class=\"container\"><h1>Data Deletion Cancelled</h1><p>Hello {borrower_name},</p><p>Your data deletion request has been successfully cancelled.</p><div class=\"success-box\"><strong> Confirmation:</strong><br>Your account remains active and all your personal data is safe. You can continue using our platform normally.</div><p>If you have any questions or concerns, please feel free to contact our support team.</p><div class=\"footer\"><p>&copy; Credit Application Platform. All rights reserved.</p></div></div></body></html>',
    'Hello {borrower_name},\n\nYour data deletion request has been successfully cancelled.\n\n Your account remains active and all your personal data is safe.\n\nYou can continue using our platform normally.\n\nIf you have any questions, please contact our support team.\n\nCopyright Credit Application Platform. All rights reserved.',
    '[\"borrower_name\"]',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
