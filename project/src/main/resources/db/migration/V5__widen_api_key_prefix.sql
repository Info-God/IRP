-- key_prefix = "irp_live_" (9 chars) + up to 12 chars of the random key = 21 chars,
-- which overflowed the original VARCHAR(20) on every single API key creation.
ALTER TABLE api_keys ALTER COLUMN key_prefix TYPE VARCHAR(30);
