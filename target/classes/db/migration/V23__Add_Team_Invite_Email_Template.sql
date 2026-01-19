-- V23__Add_Team_Invite_Email_Template.sql
-- Insert team member invitation email template

INSERT INTO email_templates (template_name, subject, html_body, text_body, variables, active, created_at, updated_at)
VALUES (
    'team-invite',
    'Join {bank_name} on Credit Application Platform',
    '<html><head><style>body{font-family:Arial,sans-serif;color:#333}h1{color:#2c3e50}.container{max-width:600px;margin:0 auto;padding:20px}.btn{background-color:#3498db;color:white;padding:12px 24px;text-decoration:none;border-radius:5px;display:inline-block;margin-top:20px}.footer{color:#7f8c8d;font-size:12px;margin-top:30px;border-top:1px solid #ecf0f1;padding-top:20px}</style></head><body><div class="container"><h1>Join {bank_name}</h1><p>Hello {member_name},</p><p>{inviter_name} invites you to join the team at {bank_name} on the Credit Application Platform.</p><p>As a team member, you''ll be able to:</p><ul><li>Manage bank profile and settings</li><li>Review and submit loan offers</li><li>Collaborate with your team on applications</li><li>Access comprehensive analytics and reports</li></ul><p><a href="{invite_link}" class="btn">Accept Invitation</a></p><p>This invitation expires in 7 days.</p><div class="footer"><p>If you have questions about this invitation, please contact {inviter_name} or reply to this email.</p><p>&copy; Credit Application Platform. All rights reserved.</p></div></div></body></html>',
    'Hello {member_name},\n\n{inviter_name} invites you to join the team at {bank_name} on the Credit Application Platform.\n\nAs a team member, you''ll be able to:\n- Manage bank profile and settings\n- Review and submit loan offers\n- Collaborate with your team on applications\n- Access comprehensive analytics and reports\n\nAccept your invitation at: {invite_link}\n\nThis invitation expires in 7 days.\n\nIf you have questions about this invitation, please contact {inviter_name} or reply to this email.\n\nCopyright Credit Application Platform. All rights reserved.',
    '["bank_name","member_name","inviter_name","invite_link"]',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
