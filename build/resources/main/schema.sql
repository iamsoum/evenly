PRAGMA foreign_keys = ON;

-- ------------------
-- Users
-- ------------------
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    default_group_id INTEGER REFERENCES groups(id) ON DELETE SET NULL
);

-- ------------------
-- Groups
-- ------------------
CREATE TABLE IF NOT EXISTS groups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    expense REAL DEFAULT 0.0,
    expense_currency TEXT DEFAULT 'CAD',
    description TEXT
);

-- ------------------
-- Memberships
-- ------------------
CREATE TABLE IF NOT EXISTS memberships (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    UNIQUE(user_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_memberships_group ON memberships(group_id);
CREATE INDEX IF NOT EXISTS idx_memberships_user ON memberships(user_id);

-- ------------------
-- Split strategies
-- ------------------
CREATE TABLE IF NOT EXISTS split_strategies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    params JSON
);

CREATE INDEX IF NOT EXISTS idx_split_strategy_type ON split_strategies(type);

-- ------------------
-- Transactions
-- ------------------
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT,
    amount REAL NOT NULL,
    currency TEXT NOT NULL DEFAULT 'CAD',
    payer_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    split_strategy_id INTEGER REFERENCES split_strategies(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_group ON transactions(group_id);
CREATE INDEX IF NOT EXISTS idx_transactions_payer ON transactions(payer_id);

-- ------------------
-- Allocations
-- ------------------
CREATE TABLE IF NOT EXISTS allocations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id INTEGER NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    debtor_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    amount REAL NOT NULL,
    currency TEXT NOT NULL DEFAULT 'CAD',
    remaining_due REAL NOT NULL,
    remaining_currency TEXT NOT NULL DEFAULT 'CAD'
);

CREATE INDEX IF NOT EXISTS idx_allocations_transaction ON allocations(transaction_id);
CREATE INDEX IF NOT EXISTS idx_allocations_debtor ON allocations(debtor_id);

-- ------------------
-- Payments
-- ------------------
CREATE TABLE IF NOT EXISTS payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    allocation_id INTEGER NOT NULL REFERENCES allocations(id) ON DELETE CASCADE,
    from_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    to_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    amount REAL NOT NULL,
    currency TEXT NOT NULL DEFAULT 'CAD'
);

CREATE INDEX IF NOT EXISTS idx_payments_allocation ON payments(allocation_id);
CREATE INDEX IF NOT EXISTS idx_payments_from ON payments(from_user_id);
CREATE INDEX IF NOT EXISTS idx_payments_to ON payments(to_user_id);

-- ------------------
-- Balances
-- ------------------
CREATE TABLE IF NOT EXISTS balances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    group_id INTEGER NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    amount_owed REAL DEFAULT 0.0,
    amount_owed_currency TEXT DEFAULT 'CAD',
    amount_paid REAL DEFAULT 0.0,
    amount_paid_currency TEXT DEFAULT 'CAD',
    UNIQUE(user_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_balances_user ON balances(user_id);
CREATE INDEX IF NOT EXISTS idx_balances_group ON balances(group_id);

-- ------------------
-- Settlements
-- ------------------
CREATE TABLE IF NOT EXISTS settlements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    settled_by_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE RESTRICT
);

-- ------------------
-- Settlement Payments
-- ------------------
CREATE TABLE IF NOT EXISTS settlement_payments (
    settlement_id INTEGER NOT NULL REFERENCES settlements(id) ON DELETE CASCADE,
    payment_id INTEGER NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    PRIMARY KEY (settlement_id, payment_id)
);

CREATE INDEX IF NOT EXISTS idx_settlement_payments_settlement ON settlement_payments(settlement_id);
CREATE INDEX IF NOT EXISTS idx_settlement_payments_payment ON settlement_payments(payment_id);
