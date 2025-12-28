# 4. Data Requirements
This section describes various aspects of the data that the system will consume as inputs, process, and create as outputs.

# 4.1 Logical Data Model

[IMAGES (already added)]

## 4.1.1 Relational (SQL) entities
The system’s primary transactional data is stored in a relational database (JPA entities):

- **User** (base profile/account data)
  - One **User** belongs to one **Ward** (Ward → District → City).
  - One **User** may have exactly one specialized role record:
    - **Customer** (1:1 with User, shared key)
    - **SaleAgent** (1:1 with User, shared key)
    - **PropertyOwner** (1:1 with User, shared key)
  - One **User** receives many **Notification** records (1:N).
  - One **User** may create many **ViolationReport** records as reporter (1:N).

- **PropertyOwner** owns many **Property** (1:N).
- **SaleAgent** may be assigned many **Property** (1:N) via `Property.assignedAgent`.
- **Property** belongs to one **PropertyType** (N:1) and one **Ward** (N:1).
  - One **Property** has many **Media** (1:N).
  - One **Property** has many **Appointment** (1:N).
  - One **Property** has many **Contract** (1:N).
  - One **Property** has many **IdentificationDocument** (1:N).

- **Customer** makes many **Appointment** (1:N) and signs many **Contract** (1:N).
- **Appointment** links **Property** + **Customer** (+ optional **SaleAgent**).
- **Contract** links **Property** + **Customer** + **SaleAgent**.
  - One **Contract** has many **Payment** records (1:N).

- **DocumentType** defines categories for **IdentificationDocument** (1:N).
- **ViolationReport** is filed by a **User** and can have many **Media** attachments (1:N).

## 4.1.2 Document (MongoDB) collections
The system stores analytics/audit and reporting data in MongoDB collections:

- **Audit / tracking collections**
  - `search_logs` (**SearchLog**): stores search context references (user/location/property IDs).
  - Customer preference collections (each stores `customer_id` + `ref_id`):
    - `customer_favorite_properties`
    - (and other preference collections in `models/schemas/customer/*`, if enabled)

- **Report collections** (generated aggregates)
  - `agent_performance_reports` (**AgentPerformanceReport**)
  - `financial_reports` (**FinancialReport**)
  - `customer_analytics_reports` (**CustomerAnalysisReport**)
  - `property_statistic_reports` (**PropertyStatisticReport**)
  - `property_owner_contribution_reports` (**PropertyOwnerReport**)
  - `violation_report_details` (**ViolationReportDetails**)

# 4.2 Data Dictionary

> Notes:
> - Unless stated otherwise, all relational entities inherit fields from `AbstractBaseEntity`: `id (UUID)`, `created_at (datetime)`, `updated_at (datetime)`.
> - MongoDB schemas inherit from `AbstractBaseMongoSchema`: `id (string UUID)`, `createdAt (datetime)`, `updatedAt (datetime)`.
> - Assets (documents/images/media) are stored in **Cloudinary**; the database stores only **URLs/paths**.
> - Sensitive fields are visible only to **the owning user** and **admin**.

## 4.2.1 Relational (SQL) entities

### User (`users`)
- `user_id` (UUID, PK): Unique user identifier.
- `role` (enum, NOT NULL): User role (e.g., Customer / SaleAgent / PropertyOwner / Admin as defined by system constants).
- `email` (string, NOT NULL, UNIQUE): Login/email address.
- `phone_number` (string, NOT NULL, UNIQUE): Primary phone contact.
- `zalo_contact` (string, nullable): Zalo contact.
- `ward_id` (UUID, FK → wards.ward_id, NOT NULL): User’s ward.
- `password` (string, NOT NULL): Stored as Base64-hashed/encoded value per project policy.
- `first_name` (string, NOT NULL)
- `last_name` (string, NOT NULL)
- `avatar_url` (string, nullable): Cloudinary URL.
- `status` (enum, NOT NULL): Profile status.
- `identification_number` (string, nullable): Government ID number (sensitive).
- `day_of_birth` (date, nullable)
- `gender` (string, nullable)
- `nation` (string, nullable)
- `bank_account_number` (string, length 30, nullable): (sensitive)
- `bank_account_name` (string, length 150, nullable): (sensitive)
- `bank_bin` (string, length 20, nullable): (sensitive)
- `issue_date` (date, nullable)
- `issuing_authority` (string, nullable)
- `front_id_picture_path` (string, nullable): Cloudinary URL/path (sensitive).
- `back_id_picture_path` (string, nullable): Cloudinary URL/path (sensitive).
- `last_login_at` (datetime, nullable)
- `fcm_token` (string, nullable)

### Customer (`customers`)
- `customer_id` (UUID, PK & FK → users.user_id): Shares identifier with User.
- Relationships:
  - 1 Customer → N Appointments
  - 1 Customer → N Contracts

### SaleAgent (`sale_agents`)
- `sale_agent_id` (UUID, PK & FK → users.user_id): Shares identifier with User.
- `employee_code` (string, NOT NULL, UNIQUE)
- `max_properties` (int, NOT NULL)
- `hired_date` (datetime, NOT NULL)

### PropertyOwner (`property_owners`)
- `owner_id` (UUID, PK & FK → users.user_id): Shares identifier with User.
- `approved_at` (datetime, nullable)

### PropertyType (`property_types`)
- `property_type_id` (UUID, PK)
- `type_name` (string, length 50, NOT NULL, UNIQUE)
- `avatar_url` (string, nullable): Cloudinary URL.
- `description` (text, nullable)
- `is_active` (boolean, nullable)

### City (`cities`)
- `city_id` (UUID, PK)
- `city_name` (string, nullable)
- `description` (string, nullable)
- `img_url` (string, nullable): Cloudinary URL.
- `total_area` (decimal(15,2), nullable)
- `avg_land_price` (decimal(15,2), nullable)
- `population` (int, nullable)
- `is_active` (boolean, nullable)

### District (`districts`)
- `district_id` (UUID, PK)
- `city_id` (UUID, FK → cities.city_id, NOT NULL)
- `district_name` (string, nullable)
- `img_url` (string, nullable): Cloudinary URL.
- `description` (string, nullable)
- `total_area` (decimal(15,2), nullable)
- `avg_land_price` (decimal(15,2), nullable)
- `population` (int, nullable)
- `is_active` (boolean, nullable)

### Ward (`wards`)
- `ward_id` (UUID, PK)
- `district_id` (UUID, FK → districts.district_id, NOT NULL)
- `ward_name` (string, NOT NULL)
- `img_url` (string, nullable): Cloudinary URL.
- `description` (string, NOT NULL)
- `total_area` (decimal(15,2), NOT NULL)
- `avg_land_price` (decimal(15,2), nullable)
- `population` (int, NOT NULL)
- `is_active` (boolean, nullable)

### Property (`properties`)
- `property_id` (UUID, PK)
- `owner_id` (UUID, FK → property_owners.owner_id, NOT NULL)
- `assigned_agent_id` (UUID, FK → sale_agents.sale_agent_id, nullable)
- `service_fee_amount` (decimal(15,2), NOT NULL)
- `service_fee_collected_amount` (decimal(15,2), NOT NULL)
- `property_type_id` (UUID, FK → property_types.property_type_id, NOT NULL)
- `ward_id` (UUID, FK → wards.ward_id, NOT NULL)
- `title` (string, length 200, NOT NULL)
- `description` (text, NOT NULL)
- `transaction_type` (enum, NOT NULL)
- `full_address` (string, nullable)
- `area` (decimal(10,2), NOT NULL)
- `rooms` (int, nullable)
- `bathrooms` (int, nullable)
- `floors` (int, nullable)
- `bedrooms` (int, nullable)
- `house_orientation` (enum, nullable)
- `balcony_orientation` (enum, nullable)
- `year_built` (int, nullable)
- `price_amount` (decimal(15,2), NOT NULL)
- `price_per_square_meter` (decimal(15,2), nullable)
- `commission_rate` (decimal(5,4), NOT NULL)
- `amenities` (text, nullable)
- `status` (enum, nullable)
- `view_count` (int, nullable)
- `approved_at` (datetime, nullable)

### Media (`media`)
- `media_id` (UUID, PK)
- `property_id` (UUID, FK → properties.property_id, nullable)
- `violation_id` (UUID, FK → violation_reports.violation_id, nullable)
- `media_type` (enum, NOT NULL)
- `file_name` (string, NOT NULL)
- `file_path` (string, length 500, NOT NULL): Cloudinary URL/path.
- `mime_type` (string, length 100, NOT NULL)
- `document_type` (string, nullable)

### Appointment (`appointment`)
- `appointment_id` (UUID, PK)
- `property_id` (UUID, FK → properties.property_id, NOT NULL)
- `customer_id` (UUID, FK → customers.customer_id, NOT NULL)
- `agent_id` (UUID, FK → sale_agents.sale_agent_id, nullable)
- `requested_date` (datetime, NOT NULL)
- `confirmed_date` (datetime, nullable)
- `status` (enum, nullable)
- `customer_requirements` (text, nullable)
- `agent_notes` (text, nullable)
- `viewing_outcome` (text, nullable)
- `customer_interest_level` (string, nullable)
- `cancelled_at` (datetime, nullable)
- `cancelled_by` (enum, nullable)
- `cancelled_reason` (string, nullable)
- `rating` (short, nullable)
- `comment` (text, nullable)

### Contract (`contract`)
- `contract_id` (UUID, PK)
- `property_id` (UUID, FK → properties.property_id, NOT NULL)
- `customer_id` (UUID, FK → customers.customer_id, NOT NULL)
- `agent_id` (UUID, FK → sale_agents.sale_agent_id, NOT NULL)
- `contract_type` (enum, NOT NULL)
- `contract_number` (string, length 50, NOT NULL, UNIQUE)
- `start_date` (date, NOT NULL)
- `end_date` (date, NOT NULL)
- `special_terms` (text, NOT NULL)
- `status` (enum, NOT NULL)
- `cancellation_reason` (text, nullable)
- `cancellation_penalty` (decimal(15,2), nullable)
- `cancelled_by` (enum, nullable)
- `contract_payment_type` (enum, NOT NULL)
- `total_contract_amount` (decimal(15,2), NOT NULL)
- `commission_amount` (decimal(15,2), NOT NULL)
- `deposit_amount` (decimal(15,2), NOT NULL)
- `remaining_amount` (decimal(15,2), NOT NULL)
- `advance_payment_amount` (decimal(15,2), NOT NULL)
- `installment_amount` (int, NOT NULL)
- `progress_milestone` (decimal(15,2), NOT NULL)
- `final_payment_amount` (decimal(15,2), NOT NULL)
- `late_payment_penalty_rate` (decimal(5,4), NOT NULL)
- `special_conditions` (text, NOT NULL)
- `signed_at` (datetime, NOT NULL)
- `completed_at` (datetime, NOT NULL)
- `rating` (short, nullable)
- `comment` (text, nullable)

### Payment (`payments`)
- `payment_id` (UUID, PK)
- `contract_id` (UUID, FK → contract.contract_id, nullable)
- `property_id` (UUID, FK → properties.property_id, nullable)
- `sale_agent_id` (UUID, FK → sale_agents.sale_agent_id, nullable)
- `payment_type` (enum, NOT NULL)
- `amount` (decimal(15,2), NOT NULL)
- `due_date` (date, NOT NULL)
- `paid_date` (date, nullable)
- `installment_number` (int, nullable)
- `payment_method` (string, nullable)
- `transaction_reference` (string, length 100, nullable)
- `status` (enum, nullable)
- `penalty_amount` (decimal(15,2), nullable)
- `notes` (text, nullable)
- `payway_payment_id` (string, length 36, UNIQUE, nullable)

### DocumentType (`document_types`)
- `document_type_id` (UUID, PK)
- `name` (string, length 100, nullable)
- `description` (string, nullable)
- `is_compulsory` (boolean, nullable)

### IdentificationDocument (`identification_documents`)
- `document_id` (UUID, PK)
- `document_type_id` (UUID, FK → document_types.document_type_id, NOT NULL)
- `property_id` (UUID, FK → properties.property_id, nullable)
- `document_number` (string, length 20, NOT NULL)
- `document_name` (string, NOT NULL)
- `file_path` (string, length 500, NOT NULL): Cloudinary URL/path.
- `issue_date` (date, nullable)
- `expiry_date` (date, nullable)
- `issuing_authority` (string, length 100, nullable)
- `verification_status` (enum, nullable)
- `verified_at` (datetime, nullable)
- `rejection_reason` (text, nullable)

### Notification (`notifications`)
- `notification_id` (UUID, PK)
- `recipient_id` (UUID, FK → users.user_id, NOT NULL)
- `type` (enum, nullable)
- `title` (string, length 200, NOT NULL)
- `message` (text, NOT NULL)
- `related_entity_type` (enum, nullable)
- `related_entity_id` (string, length 100, nullable)
- `delivery_status` (enum, nullable)
- `is_read` (boolean, nullable)
- `img_url` (string, NOT NULL): Cloudinary URL.
- `read_at` (datetime, nullable)

### ViolationReport (`violation_reports`)
- `violation_id` (UUID, PK)
- `reporter_user` (UUID, FK → users.user_id, NOT NULL)
- `related_entity_type` (enum, NOT NULL)
- `related_entity_id` (UUID, NOT NULL)
- `violation_type` (enum, NOT NULL)
- `description` (text, NOT NULL)
- `status` (enum, nullable)
- `penalty_applied` (enum, nullable)
- `resolution_notes` (text, nullable)
- `resolved_at` (datetime, nullable)

## 4.2.2 MongoDB collections

### SearchLog (`search_logs`)
- `id` (string, PK)
- `createdAt` (datetime)
- `updatedAt` (datetime)
- `user_id` (UUID, nullable/optional depending on tracking policy)
- `city_id` (UUID, nullable)
- `district_id` (UUID, nullable)
- `ward_id` (UUID, nullable)
- `property_id` (UUID, nullable)
- `property_type_id` (UUID, nullable)

### CustomerFavoriteProperty (`customer_favorite_properties`)
- `id` (string, PK)
- `createdAt` (datetime)
- `updatedAt` (datetime)
- `customer_id` (UUID, NOT NULL)
- `ref_id` (UUID, NOT NULL): References `Property.property_id`.

### Report documents (common)
All report documents extend `AbstractBaseMongoReport`:
- `base_report_data` (object): common report metadata (time range, generation time, etc.)

# 4.3 Reports
The application generates aggregate reports (stored in MongoDB) for administration/analytics:

1. **AgentPerformanceReport**
   - Purpose: Overall performance summary for sale agents.
   - Typical content: totalAgents, newThisMonth, average satisfaction, ratings totals/average.

2. **PropertyOwnerReport**
   - Purpose: Contribution/engagement analytics for property owners.
   - Typical content: Aggregated metrics per owner and/or across time.

3. **PropertyStatisticReport**
   - Purpose: Property inventory and engagement statistics.
   - Typical content: counts of active/sold/rented properties; aggregated search/favorite counts by city/district/ward/property type/property.

4. **CustomerAnalysisReport**
   - Purpose: Customer metrics and satisfaction analytics.
   - Typical content: total customers, new customers in current month, average transaction value, high value customer count, satisfaction scores, ratings.

5. **ViolationReportDetails**
   - Purpose: Violation monitoring and enforcement analytics.
   - Typical content: total reports, average resolution time, actions taken (accounts suspended, properties removed), counts by violation type.

6. **FinancialReport**
   - Purpose: Financial overview for the business.
   - Typical content: total revenue, net profit, salary totals, contract counts, tax, ratings, and ranked revenue breakdowns by location and property type.

# 4.4 Data Acquisition, Integrity, Retention, and Disposal

## 4.4.1 Data acquisition
- Users, roles (Customer/SaleAgent/PropertyOwner), listings (Property), contracts, payments, and appointments are created and updated via application business workflows.
- Assets (documents/images/media) are uploaded to **Cloudinary**; the system stores only the returned **URL/path** (`file_path`, `avatar_url`, `img_url`, etc.).
- Audit/analytics data (e.g., search logs, favorites) is written to MongoDB collections.

## 4.4.2 Data integrity
- The relational database enforces referential integrity through foreign keys implied by entity relationships (e.g., Property → Ward/Owner/Type; Contract → Property/Customer/Agent).
- Unique constraints are enforced for:
  - `users.email`, `users.phone_number`
  - `sale_agents.employee_code`
  - `contract.contract_number`
  - `payments.payway_payment_id` (where present)
- Enumerated fields (role/status/types) must only take values defined by system constants.

## 4.4.3 Security and privacy
- Sensitive fields (e.g., password, bank details, identification data, ID image URLs) must be accessible only by:
  - the owning user; and
  - admin.
- Password is stored as Base64-hashed/encoded value per project policy.

## 4.4.4 Retention and disposal
- **Audit log data** (search logs, favorites, and similar tracking data) is retained until an **admin deletes/clears** it.
- Regular users **do not** have permission to delete audit log records.
- Other transactional data (users/properties/contracts/payments/appointments) follows standard lifecycle management within the application; deletions (if any) are controlled by administrative/business rules.
