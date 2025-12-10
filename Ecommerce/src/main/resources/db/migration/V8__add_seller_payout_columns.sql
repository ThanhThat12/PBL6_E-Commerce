-- Migration: Add seller payout tracking columns to orders table
-- Date: 2025-12-08
-- Purpose: Track when money is transferred from admin to seller

ALTER TABLE orders 
ADD COLUMN seller_paid_out BOOLEAN DEFAULT FALSE,
ADD COLUMN seller_paid_out_at DATETIME NULL;

-- Add index for faster query in scheduler
CREATE INDEX idx_orders_seller_payout 
ON orders(payment_status, seller_paid_out, paid_at);
