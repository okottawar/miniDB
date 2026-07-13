# 🗄️ MiniDB

A lightweight in-memory relational database system built in Java that simulates core database internals such as schema management, table operations, and CRUD execution, along with a simple Swing-based interface for visualization.

---

## 🚀 Overview

MiniDB is a simplified relational database engine built for learning how databases work internally. It focuses on core concepts such as schema-driven storage, table management, and structured data operations without using SQL parsing or disk-based persistence.

The project also includes a minimal graphical interface built using Java Swing to visualize and interact with tables.

---

## ✨ Features

### Core Database Engine

* In-memory table storage
* Multiple table support within a single database
* Schema-based design for strict structure enforcement
* CRUD operations:

  * Create tables
  * Insert rows
  * Select data
  * Update records
  * Delete records

### Schema System

* Column definitions with names and types
* Basic data types:

  * INT
  * STRING
  * BOOLEAN
  * DOUBLE
* Constraints support:

  * NOT NULL
  * PRIMARY KEY (basic enforcement)

### Architecture

* Clean modular design:

  * Models (DTOs / Java Beans)
  * Core database engine
  * Service layer abstraction
  * UI layer (Swing)
  * Utility functions

### UI Layer

* Swing-based graphical interface
* Table selection panel
* Data visualization using JTable
* Basic interactive CRUD controls

---

## 🧠 Architecture

```text id="arch1"
Swing UI
   ↓
Service Layer (DbService)
   ↓
Core Database Engine
   ↓
Table & Row Storage (In-Memory)
```

---

## 📁 Project Structure

```text id="structure1"
MiniDB/

├── Main.java

├── core/
│   ├── Database.java
│   ├── Table.java
│
├── model/
│   ├── Column.java
│   ├── Row.java
│   ├── TableSchema.java
│   └── DataValue.java
│
├── enums/
│   ├── DataType.java
│   └── Constraint.java
│
├── service/
│   └── DbService.java
│
├── ui/
│   ├── MainFrame.java
│   ├── TablePanel.java
│   ├── DataPanel.java
│   └── CommandPanel.java
│
├── utils/
│   └── Validators.java
```

---

## ⚙️ How It Works

1. Tables are defined using schema objects (columns + constraints)
2. Data is stored in-memory as structured rows
3. All operations are routed through the Database core
4. Service layer acts as a bridge between UI and core logic
5. UI renders tables and allows basic interactions

---

## 🧪 Example Usage

```java id="example1"
Database db = new Database();

db.createTable("users")
  .addColumn("id", DataType.INT)
  .addColumn("name", DataType.STRING);

db.insert("users", 1, "Alice");

db.select("users");
```

---

## 🏁 Summary

MiniDB is a minimal relational database system designed for educational purposes. It demonstrates how databases structure data internally, enforce schemas, and execute basic operations, combined with a simple Swing-based UI for visualization.
