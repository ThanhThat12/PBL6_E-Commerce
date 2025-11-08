-- Migration Script: Update shops table to use pickup_address_id instead of address
-- Date: November 7, 2025
-- Description: Remove address column and add pickup_address_id foreign key to addresses table

USE `ecommerce1`;

-- Step 1: Add the new column
ALTER TABLE `shops`
  ADD COLUMN `pickup_address_id` bigint(20) DEFAULT NULL;

-- Step 2: Add the foreign key constraint
ALTER TABLE `shops`
  ADD CONSTRAINT `fk_shop_pickup_address`
    FOREIGN KEY (`pickup_address_id`)
    REFERENCES `addresses` (`id`) ON DELETE SET NULL;

-- Step 3: Drop the old address column
ALTER TABLE `shops`
  DROP COLUMN `address`;

-- Verification
SELECT 'SHOPS TABLE MIGRATION COMPLETED SUCCESSFULLY!' as status;
DESCRIBE `shops`;