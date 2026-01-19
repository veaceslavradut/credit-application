-- Seed help content for Story 2.10
INSERT INTO help_articles (id, topic, version, title, description, content, language, status)
VALUES
  (gen_random_uuid(), 'loan-types', 1, 'Loan Types Overview', 'Overview of available loan types', 'Detailed explanation of personal, home, auto loans.', 'en', 'PUBLISHED'),
  (gen_random_uuid(), 'application-requirements', 1, 'Application Requirements', 'Documents and information needed to apply', 'Checklist of required documents and tips.', 'en', 'PUBLISHED'),
  (gen_random_uuid(), 'document-checklist', 1, 'Document Checklist', 'Checklist of documents by loan type', 'List of documents required for each loan type.', 'en', 'PUBLISHED'),
  (gen_random_uuid(), 'rate-preference', 1, 'Rate Preference', 'Fixed vs Variable rate explanations', 'Explanation of fixed, variable, and either preference.', 'en', 'PUBLISHED'),
  (gen_random_uuid(), 'submission-tips', 1, 'Submission Tips', 'Best practices for successful applications', 'Guidance to improve application success.', 'en', 'PUBLISHED');

-- Minimal sections for loan-types
INSERT INTO help_sections (id, article_id, section_number, heading, content, display_order)
SELECT gen_random_uuid(), id, 1, 'Personal Loans', 'Personal loans are unsecured and flexible.', 1 FROM help_articles WHERE topic='loan-types' AND language='en';
INSERT INTO help_sections (id, article_id, section_number, heading, content, display_order)
SELECT gen_random_uuid(), id, 2, 'Home Loans', 'Home loans are secured against property.', 2 FROM help_articles WHERE topic='loan-types' AND language='en';
INSERT INTO help_sections (id, article_id, section_number, heading, content, display_order)
SELECT gen_random_uuid(), id, 3, 'Auto Loans', 'Auto loans finance vehicle purchases.', 3 FROM help_articles WHERE topic='loan-types' AND language='en';

-- FAQs examples
INSERT INTO help_faqs (id, article_id, question, answer, "order")
SELECT gen_random_uuid(), id, 'What is a personal loan?', 'It is an unsecured loan for various personal expenses.', 1 FROM help_articles WHERE topic='loan-types' AND language='en';
INSERT INTO help_faqs (id, article_id, question, answer, "order")
SELECT gen_random_uuid(), id, 'Can I prepay my loan?', 'Yes, but check your loan terms for prepayment penalties.', 2 FROM help_articles WHERE topic='loan-types' AND language='en';
