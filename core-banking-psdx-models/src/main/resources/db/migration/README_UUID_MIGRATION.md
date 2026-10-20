# UUID Migration Documentation

## Overview

This document describes the comprehensive database migration from Long/BIGSERIAL primary keys to UUID primary keys for the PSD2/PSD3 Core Banking system. The migration ensures data integrity, maintains all relationships, and provides optimal performance for UUID-based operations.

## Migration Scripts

### V3__convert_long_ids_to_uuid.sql
**Purpose**: Main migration script that converts all Long ID fields to UUID
**Status**: Production Ready

**What it does**:
1. Enables UUID extension (`uuid-ossp`)
2. Adds new UUID columns alongside existing BIGINT columns
3. Generates UUIDs for existing data using `uuid_generate_v4()`
4. Creates mapping table to preserve relationships during migration
5. Updates all foreign key references using the mapping
6. Drops old columns and constraints
7. Renames UUID columns to original names
8. Recreates primary keys, foreign keys, and indexes
9. Adds documentation comments

**Tables affected**:
- `consents` (id: BIGINT → UUID, party_id: BIGINT → UUID)
- `access_logs` (id: BIGINT → UUID, consent_id: BIGINT → UUID, party_id: BIGINT → UUID)
- `third_party_providers` (id: BIGINT → UUID)
- `payments` (id: BIGINT → UUID, consent_id: BIGINT → UUID)
- `funds_confirmations` (id: BIGINT → UUID, consent_id: BIGINT → UUID)

### V4__verify_uuid_migration.sql
**Purpose**: Comprehensive verification script to validate migration success
**Status**: Production Ready

**Verification checks**:
1. Primary key UUID type validation
2. Foreign key UUID type validation
3. Party ID UUID type validation
4. Foreign key constraint existence
5. Data preservation validation
6. Referential integrity checks
7. Index existence validation
8. UUID format validation

### V5__optimize_uuid_performance.sql
**Purpose**: Basic performance optimization for UUID-based operations (Transactional)
**Status**: Production Ready

**Optimizations**:
1. B-tree indexes for UUID columns
2. Composite indexes for common query patterns
3. Optimized UUID generation function
4. Performance monitoring view
5. Table and column documentation
6. Statistics updates for query planning

### V6__create_uuid_indexes.sql
**Purpose**: High-performance indexes for UUID columns
**Status**: Production Ready

**Optimizations**:
1. Hash indexes for UUID equality lookups
2. Partial indexes for filtered queries
3. Additional performance indexes

**Note**: Separated from V5 to keep index creation in a dedicated migration script.

## Rollback Strategy

### R3__rollback_uuid_migration_fixed.sql
**Purpose**: Complete rollback from UUID to BIGINT
**Status**: Fixed version available (R3__rollback_uuid_migration_fixed.sql)

**Rollback process**:
1. Creates backup tables for all UUID data
2. Adds new BIGINT columns alongside UUID columns
3. Creates UUID to BIGINT mapping
4. Updates foreign key references
5. Drops UUID columns and recreates BIGINT structure
6. Restores all constraints and indexes

**Important Notes**:
- Rollback may cause data loss if UUIDs cannot be mapped to sequential integers
- Use only in development or with proper backup strategy
- Test thoroughly before using in production

## Entity Model Compatibility

### Current Entity Models (Compatible)

All entity models have been updated to use UUID:

```java
// Consent Entity
@Id
private UUID id;

@Column("party_id")
private UUID partyId;

// AccessLog Entity
@Id
private UUID id;

@Column("consent_id")
private UUID consentId;

@Column("party_id")
private UUID partyId;

// ThirdPartyProvider Entity
@Id
private UUID id;

// Payment Entity
@Id
private UUID id;

@Column("consent_id")
private UUID consentId;

// FundsConfirmation Entity
@Id
private UUID id;

@Column("consent_id")
private UUID consentId;
```

### Repository Interfaces (Compatible)

All repositories extend `ReactiveCrudRepository<Entity, UUID>`:

```java
public interface ConsentRepository extends ReactiveCrudRepository<Consent, UUID>
public interface AccessLogRepository extends ReactiveCrudRepository<AccessLog, UUID>
public interface ThirdPartyProviderRepository extends ReactiveCrudRepository<ThirdPartyProvider, UUID>
public interface PaymentRepository extends ReactiveCrudRepository<Payment, UUID>
public interface FundsConfirmationRepository extends ReactiveCrudRepository<FundsConfirmation, UUID>
```

## Migration Execution

### Automatic Execution (Recommended)
The migration is configured to run automatically when the application starts via Spring Boot Flyway integration:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

### Manual Execution (Development/Testing)
For development or testing purposes, you can run individual scripts:

```sql
-- 1. Run main migration
\i V3__convert_long_ids_to_uuid.sql

-- 2. Verify migration
\i V4__verify_uuid_migration.sql

-- 3. Apply performance optimizations
\i V5__optimize_uuid_performance.sql
```

## Testing and Validation

### Test Script
Run the comprehensive test script to validate the migration:

```bash
# Location: core-banking-psdx-models/src/test/resources/db/test_uuid_migration.sql
psql -d your_database -f test_uuid_migration.sql
```

### Test Coverage
The test script validates:
- UUID data insertion and relationships
- Foreign key integrity
- UUID lookup performance
- UUID format validation
- Index usage
- Constraint validation
- Data type validation

## Performance Considerations

### UUID vs BIGINT Performance
- **Storage**: UUID (16 bytes) vs BIGINT (8 bytes) - 2x storage overhead
- **Index Size**: Larger indexes due to UUID size
- **Generation**: UUID generation is slightly slower than BIGINT sequences
- **Clustering**: UUIDs provide better distribution but less clustering

### Optimizations Applied
1. **Hash Indexes**: Faster equality lookups for UUID columns
2. **Composite Indexes**: Optimized for common query patterns
3. **Partial Indexes**: Reduced index size for filtered queries
4. **Materialized Views**: Pre-computed aggregations for frequent queries

### Monitoring
Use the `uuid_performance_metrics` view to monitor index efficiency:

```sql
SELECT * FROM uuid_performance_metrics;
```

## Production Deployment Checklist

### Pre-Migration
- [ ] Backup database
- [ ] Test migration on staging environment
- [ ] Verify application compatibility
- [ ] Plan maintenance window
- [ ] Prepare rollback strategy

### During Migration
- [ ] Monitor migration progress
- [ ] Check for errors in logs
- [ ] Validate data integrity
- [ ] Test application functionality

### Post-Migration
- [ ] Run verification script
- [ ] Monitor performance metrics
- [ ] Update documentation
- [ ] Clean up backup data (after validation)

## Troubleshooting

### Common Issues

1. **Migration Fails with Constraint Violations**
   - Check for orphaned records before migration
   - Ensure referential integrity

2. **Performance Degradation**
   - Run `ANALYZE` on all tables after migration
   - Check index usage with `uuid_performance_metrics`

3. **Application Errors**
   - Verify entity models use UUID types
   - Check repository interfaces extend `ReactiveCrudRepository<Entity, UUID>`

### Recovery Procedures

1. **Partial Migration Failure**
   - Check migration logs for specific errors
   - Fix data issues and re-run migration

2. **Complete Migration Failure**
   - Restore from backup
   - Investigate and fix issues
   - Re-run migration

3. **Performance Issues**
   - Run performance optimization script (V5)
   - Add additional indexes if needed

## Support and Maintenance

### Monitoring Queries
```sql
-- Check migration status
SELECT * FROM migration_verification_log ORDER BY checked_at DESC;

-- Monitor performance
SELECT * FROM uuid_performance_metrics WHERE index_efficiency_percent < 90;

-- Check UUID format compliance
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND column_name LIKE '%id%' 
AND data_type != 'uuid';
```

### Regular Maintenance
- Monitor index efficiency monthly
- Update table statistics after bulk operations
- Review performance metrics quarterly

## Contact Information

For questions or issues related to the UUID migration:
- **Team**: Firefly Software Foundation
- **Email**: dev@fireflysoftware.io
- **Documentation**: This README and inline SQL comments
