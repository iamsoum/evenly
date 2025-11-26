-- ==================================================

-- Evenly App - SQLite schema

-- ==================================================

  

PRAGMA foreign_keys = ON;

  

-- ------------------

-- Users

-- ------------------

CREATE TABLE users (

id INTEGER PRIMARY KEY AUTOINCREMENT,

name TEXT NOT NULL,

email TEXT NOT NULL UNIQUE,

-- optional user-level default group

default_group_id INTEGER REFERENCES groups(id) ON DELETE SET NULL

);

  

-- ------------------

-- Groups

-- ------------------

CREATE TABLE groups (

id INTEGER PRIMARY KEY AUTOINCREMENT,

name TEXT NOT NULL,

expense REAL DEFAULT 0.0,  -- aggregate or placeholder

expense_currency TEXT DEFAULT ‘CAD’,

description TEXT

);

  

-- ------------------

-- Memberships (1 User <-> 1 Group, user may have many memberships)

-- ------------------

CREATE TABLE memberships (

id INTEGER PRIMARY KEY AUTOINCREMENT,

user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,

UNIQUE(user_id, group_id)

);

  

-- Index for quick lookup of group members

CREATE INDEX idx_memberships_group ON memberships(group_id);

CREATE INDEX idx_memberships_user ON memberships(user_id);

  

-- ------------------

-- Split strategies (Strategy Pattern) - params stored as JSON

-- ------------------

CREATE TABLE split_strategies (

id INTEGER PRIMARY KEY AUTOINCREMENT,

type TEXT NOT NULL,  -- EQUAL, PERCENTAGE

params JSON, -- JSON blob: percentages, ratios, itemAssignments, etc.

);

CREATE INDEX idx_split_strategy_type ON split_strategies(type);

  

-- ------------------

-- Transactions / Expenses

-- ------------------

CREATE TABLE transactions (

id INTEGER PRIMARY KEY AUTOINCREMENT,

description TEXT,

amount REAL NOT NULL,

currency TEXT NOT NULL DEFAULT ‘CAD’,

payer_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT, -- payer (creator)

group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,

split_strategy_id INTEGER REFERENCES split_strategies(id) ON DELETE SET NULL

);

CREATE INDEX idx_transactions_group ON transactions(group_id);

CREATE INDEX idx_transactions_payer ON transactions(payer_id);

  

-- ------------------

-- Allocations (who owes what for a transaction)

-- ------------------

CREATE TABLE allocations (

id INTEGER PRIMARY KEY AUTOINCREMENT,

transaction_id INTEGER NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,

debtor_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,

amount REAL NOT NULL, -- allocation amount

currency TEXT NOT NULL DEFAULT ‘CAD’,

remaining_due REAL NOT NULL,  -- remaining amount owed (for partial payments)

remaining_currency TEXT NOT NULL DEFAULT ‘CAD’

);

CREATE INDEX idx_allocations_transaction ON allocations(transaction_id);

CREATE INDEX idx_allocations_debtor ON allocations(debtor_id);

  

-- ------------------

-- Payments

-- ------------------

CREATE TABLE payments (

id INTEGER PRIMARY KEY AUTOINCREMENT,

allocation_id INTEGER NOT NULL REFERENCES allocations(id) ON DELETE CASCADE,

from_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT, -- payer

to_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT, -- recipient

amount REAL NOT NULL,

currency TEXT NOT NULL DEFAULT ‘CAD’

);

CREATE INDEX idx_payments_allocation ON payments(allocation_id);

CREATE INDEX idx_payments_from ON payments(from_user_id);

CREATE INDEX idx_payments_to ON payments(to_user_id);

  

-- ------------------

-- Balances (per user-per-group) - one balance row per (user, group)

-- ------------------

CREATE TABLE balances (

id INTEGER PRIMARY KEY AUTOINCREMENT,

user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,

group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,

amount_owed REAL DEFAULT 0.0,

amount_owed_currency TEXT DEFAULT ‘CAD’,

amount_paid REAL DEFAULT 0.0,

amount_paid_currency TEXT DEFAULT ‘CAD’,

UNIQUE(user_id, group_id)

);

CREATE INDEX idx_balances_user ON balances(user_id);

CREATE INDEX idx_balances_group ON balances(group_id);

  

-- ------------------

-- Settlements

-- ------------------

CREATE TABLE settlements (

id INTEGER PRIMARY KEY AUTOINCREMENT,

settled_by_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT

);

  

-- Bridge table linking settlements <-> payments (1 settlement -> many payments)

CREATE TABLE settlement_payments (

settlement_id INTEGER NOT NULL REFERENCES settlements(id) ON DELETE CASCADE,

payment_id INTEGER NOT NULL REFERENCES payments(id) ON DELETE CASCADE,

PRIMARY KEY (settlement_id, payment_id)

);

CREATE INDEX idx_settlement_payments_settlement ON settlement_payments(settlement_id);

CREATE INDEX idx_settlement_payments_payment ON settlement_payments(payment_id);

  

-- ==================================================

-- Notes / Implementation Guidance (short)

-- ==================================================

-- 1) Strategy implementations: use split_strategies.params JSON to store e.g.

--  { "percentages": { "user_2": 50, "user_3": 50 } } or { "ratios": {"user_2":1,"user_3":2} } or { "itemAssignments": {"user_2": 10.5, "user_3": 5.0} }.

-- 2) State machines: persisted as the `status` column on relevant tables (transactions.status, allocations.status, payments.status, memberships.status, invite_tokens.status).

-- 3) Monetary fields: store amount as REAL + currency as TEXT. For stronger correctness, store cents as INTEGER (minor units) if needed.

-- 4) JSON usage: SQLite JSON1 extension works for querying params/payload. You can also normalize percentages/ratios into child tables if you prefer strict relational modeling.

-- 5) Referential actions: ON DELETE CASCADE/SET NULL chosen to keep integrity; adjust to your business rules.
