-- A compact starter catalogue for local demos. Each record names the official
-- source used for review. Detailed eligibility remains intentionally
-- conservative: JanSetu only evaluates facts represented in CitizenProfile,
-- and citizens are directed to the official source for final confirmation.

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000001',
    'PM-KISAN Samman Nidhi',
    'Central income-support scheme for eligible land-holding farmer families.',
    'India',
    'Department of Agriculture and Farmers Welfare',
    'https://pmkisan.gov.in/',
    'PM-KISAN — Government of India',
    '2026-09-11T00:00:00Z',
    '["Income support paid through direct benefit transfer", "Online beneficiary status and eKYC services"]'::jsonb,
    '["Aadhaar details", "Bank account details", "Land record details as required by the state"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresFarmer": true}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('PM-KISAN Samman Nidhi'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000002',
    'Post-Matric Scholarship for Persons with Disability',
    'Maharashtra scholarship supporting eligible students with disabilities in recognised post-matric study.',
    'Maharashtra',
    'Social Justice and Special Assistance Department',
    'https://www.mahadbt.maharashtra.gov.in/SchemeData/SchemeData?str=E9DDFA703C38E51ABCC44582035F06CB9B6B2704869FE16E9E8F561A36261D4D',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Maintenance allowance", "Eligible compulsory tuition and examination fees"]'::jsonb,
    '["Disability certificate", "Maharashtra residence proof", "Current admission and course documents", "Bank account details"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresStudent": true, "requiresDisability": true}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Post-Matric Scholarship for Persons with Disability'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000003',
    'PMKSY — Per Drop More Crop',
    'Micro-irrigation assistance offered to eligible Maharashtra farmers through the MahaDBT farmer portal.',
    'Maharashtra',
    'Department of Agriculture',
    'https://www.mahadbt.maharashtra.gov.in/',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Support for approved micro-irrigation components, subject to current portal rules"]'::jsonb,
    '["Aadhaar details", "Bank account details", "Land and crop details", "Component quotation or invoice when requested"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresFarmer": true}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('PMKSY — Per Drop More Crop'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000004',
    'Sub-Mission on Agricultural Mechanization',
    'Assistance for approved farm-mechanisation components through Maharashtra''s MahaDBT farmer service.',
    'Maharashtra',
    'Department of Agriculture',
    'https://www.mahadbt.maharashtra.gov.in/',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Assistance toward approved agricultural machinery or equipment components"]'::jsonb,
    '["Aadhaar details", "Bank account details", "Land details", "Quotation or invoice for the selected component"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresFarmer": true}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Sub-Mission on Agricultural Mechanization'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000005',
    'Dr. Babasaheb Ambedkar Krushi Swavalamban Yojana',
    'Agricultural support programme listed for eligible Scheduled Caste farmers in Maharashtra.',
    'Maharashtra',
    'Department of Agriculture',
    'https://www.mahadbt.maharashtra.gov.in/',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Support for approved agricultural development components under the current programme rules"]'::jsonb,
    '["Caste certificate", "Aadhaar details", "Bank account details", "Land details"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresFarmer": true, "requiredCategories": ["SC"]}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Dr. Babasaheb Ambedkar Krushi Swavalamban Yojana'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000006',
    'Birsa Munda Krishi Kranti Yojana',
    'Agricultural support programme listed for eligible Scheduled Tribe farmers in Maharashtra.',
    'Maharashtra',
    'Department of Agriculture',
    'https://www.mahadbt.maharashtra.gov.in/',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Support for approved agricultural development components under the current programme rules"]'::jsonb,
    '["Tribe certificate", "Aadhaar details", "Bank account details", "Land details"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresFarmer": true, "requiredCategories": ["ST"]}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Birsa Munda Krishi Kranti Yojana'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000007',
    'Post-Matric Scholarship to OBC Students',
    'Post-matric educational support listed for eligible OBC students in Maharashtra.',
    'Maharashtra',
    'VJNT, OBC and SBC Welfare Department',
    'https://www.mahadbt.maharashtra.gov.in/',
    'MahaDBT — Government of Maharashtra',
    '2026-09-11T00:00:00Z',
    '["Post-matric scholarship support subject to current academic-year rules"]'::jsonb,
    '["Caste certificate", "Income certificate", "Maharashtra residence proof", "Current admission documents", "Bank account details"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresStudent": true, "requiredCategories": ["OBC"]}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Post-Matric Scholarship to OBC Students'));

INSERT INTO schemes (
    id, name, description, state, department, official_url, source_name,
    last_verified_at, benefits, required_documents, status, published_at,
    version, created_at, updated_at, eligibility_criteria
)
SELECT
    '10000000-0000-0000-0000-000000000008',
    'Post-Matric Scholarship for Students with Disabilities',
    'Central scholarship programme listed for eligible students with benchmark disabilities.',
    'India',
    'Department of Empowerment of Persons with Disabilities',
    'https://depwd.gov.in/scholarship/',
    'Department of Empowerment of Persons with Disabilities — Government of India',
    '2026-09-11T00:00:00Z',
    '["Educational scholarship support subject to the current official guidelines"]'::jsonb,
    '["Disability certificate", "Current admission documents", "Identity and bank account details"]'::jsonb,
    'PUBLISHED', NOW(), 0, NOW(), NOW(),
    '{"requiresStudent": true, "requiresDisability": true}'::jsonb
WHERE NOT EXISTS (SELECT 1 FROM schemes WHERE lower(name) = lower('Post-Matric Scholarship for Students with Disabilities'));
