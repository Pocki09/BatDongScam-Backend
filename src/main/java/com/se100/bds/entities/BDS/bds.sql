CREATE TABLE "User" (
  user_id       varchar(100) NOT NULL,
  email         varchar(100) NOT NULL UNIQUE,
  zalo_contact  varchar(100),
  password      varchar(255) NOT NULL,
  first_name    varchar(50) NOT NULL,
  last_name     varchar(50) NOT NULL,
  phone_number  varchar(20) NOT NULL,
  avatar_url    varchar(100),
  role          varchar(255) NOT NULL,
  city_id       varchar(100) NOT NULL,
  district_id   varchar(100) NOT NULL,
  status        varchar(255) DEFAULT 'PendingApproval',
  last_login_at timestamp,
  created_at    timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at    timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id));
CREATE TABLE PropertyOwner (
  owner_id              varchar(100) NOT NULL,
  identification_number varchar(20) NOT NULL UNIQUE,
  for_rent              int4 DEFAULT 0,
  for_sell              int4 DEFAULT 0,
  renting               int4 DEFAULT 0,
  sold                  int4 DEFAULT 0,
  approved_at           timestamp,
  created_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (owner_id));
CREATE TABLE IdentificationDocument (
  document_id         varchar(100) NOT NULL,
  document_type_id    varchar(100) NOT NULL,
  property_id         varchar(100),
  document_number     varchar(20) NOT NULL,
  document_name       varchar(255) NOT NULL,
  file_path           varchar(500) NOT NULL,
  issue_date          date,
  expiry_date         date,
  issuing_authority   varchar(100),
  verification_status varchar(255) DEFAULT 'Pending',
  verified_at         timestamp,
  rejection_reason    text,
  created_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (document_id));
CREATE TABLE SalesAgent (
  agent_id              varchar(100) NOT NULL,
  employee_code         varchar(20) NOT NULL UNIQUE,
  max_properties        int4 DEFAULT 20,
  hired_date            date NOT NULL,
  current_month_revenue numeric(15, 2) DEFAULT 0.00,
  total_revenue         numeric(15, 2) DEFAULT 0.00,
  current_month_deals   int4 DEFAULT 0,
  total_deals           int4 DEFAULT 0,
  active_properties     int4 DEFAULT 0,
  performance_tier      varchar(255) DEFAULT 'Bronze',
  current_month_ranking int4,
  career_ranking        int4,
  created_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (agent_id));
CREATE TABLE Customer (
  customer_id             varchar(100) NOT NULL,
  current_month_spending  numeric(15, 2) DEFAULT 0.00,
  total_spending          numeric(15, 2) DEFAULT 0.00,
  current_month_purchases int4 DEFAULT 0,
  total_purchases         int4 DEFAULT 0,
  current_month_rentals   int4 DEFAULT 0,
  total_rentals           int4 DEFAULT 0,
  current_month_searches  int4 DEFAULT 0,
  customer_tier           varchar(255) DEFAULT 'Bronze',
  lead_score              int4 DEFAULT 0,
  last_activity_date      timestamp,
  created_at              timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at              timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id));
CREATE TABLE PropertyType (
  property_type_id varchar(100) NOT NULL,
  type_name        varchar(50) NOT NULL UNIQUE,
  avatar_url       varchar(255),
  description      text,
  is_active        int4 DEFAULT TRUE,
  created_at       timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at       timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (property_type_id));
CREATE TABLE ward (
  ward_id        varchar(100) NOT NULL,
  ward_name      varchar(100) NOT NULL,
  img_url        varchar(255),
  description    varchar(100) NOT NULL,
  district_id    varchar(100) NOT NULL,
  total_area     numeric(15, 2) NOT NULL,
  avg_land_price numeric(15, 2),
  population     int4 NOT NULL,
  is_active      int4 DEFAULT TRUE,
  created_at     timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at     timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (ward_id));
CREATE TABLE Property (
  property_id            varchar(100) NOT NULL,
  owner_id               varchar(100) NOT NULL,
  assigned_agent_id      varchar(100),
  property_type_id       varchar(100) NOT NULL,
  ward_id                varchar(100) NOT NULL,
  title                  varchar(200) NOT NULL,
  description            text NOT NULL,
  transaction_type       varchar(255) NOT NULL,
  full_address           varchar(255),
  area                   numeric(10, 2) NOT NULL,
  rooms                  int4,
  bathrooms              int4,
  floors                 int4,
  bedrooms               int4,
  house_orientation      varchar(100),
  balcony_orientation    varchar(100),
  year_built             int4,
  price_amount           numeric(15, 2) NOT NULL,
  price_per_square_meter numeric(15, 2),
  commission_rate        numeric(5, 4) NOT NULL,
  amenities              text,
  status                 varchar(255) DEFAULT 'PendingApproval',
  view_count             int4 DEFAULT 0,
  approved_at            timestamp,
  created_at             timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at             timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (property_id));
CREATE TABLE Media (
  media_id      varchar(100) NOT NULL,
  property_id   varchar(100) NOT NULL,
  violation_id  varchar(100) NOT NULL,
  media_type    varchar(255) NOT NULL,
  file_name     varchar(255) NOT NULL,
  file_path     varchar(500) NOT NULL,
  mime_type     varchar(100) NOT NULL,
  document_type varchar(255),
  created_at    timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at    timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (media_id));
CREATE TABLE Appointment (
  appointment_id          varchar(100) NOT NULL,
  property_id             varchar(100) NOT NULL,
  customer_id             varchar(100) NOT NULL,
  agent_id                varchar(100) NOT NULL,
  requested_date          timestamp NOT NULL,
  confirmed_date          timestamp,
  status                  varchar(255) DEFAULT 'Requested',
  customer_requirements   text,
  agent_notes             text,
  viewing_outcome         text,
  customer_interest_level varchar(255),
  created_at              timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at              timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (appointment_id));
CREATE TABLE Contract (
  contract_id               varchar(100) NOT NULL,
  property_id               varchar(100) NOT NULL,
  customer_id               varchar(100) NOT NULL,
  agent_id                  varchar(100) NOT NULL,
  contract_type             varchar(255) NOT NULL,
  contract_number           varchar(50) NOT NULL UNIQUE,
  commission_amount         numeric(15, 2) NOT NULL,
  service_fee_amount        numeric(15, 2) NOT NULL,
  start_date                date NOT NULL,
  end_date                  date NOT NULL,
  special_terms             text NOT NULL,
  status                    varchar(255) DEFAULT 'Draft' NOT NULL,
  cancellation_reason       text NOT NULL,
  cancellation_penalty      numeric(15, 2) NOT NULL,
  contract_payment_type     varchar(255) NOT NULL,
  total_contract_amount     numeric(15, 2) NOT NULL,
  deposit_amount            numeric(15, 2) NOT NULL,
  remaining_amount          numeric(15, 2) NOT NULL,
  advance_payment_amount    numeric(15, 2) NOT NULL,
  installment_amount        int4 NOT NULL,
  progress_milestone        numeric(15, 2) NOT NULL,
  final_payment_amount      numeric(15, 2) NOT NULL,
  late_payment_penalty_rate numeric(5, 4) NOT NULL,
  special_conditions        text NOT NULL,
  signed_at                 timestamp NOT NULL,
  completed_at              timestamp NOT NULL,
  created_at                timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  updated_at                timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (contract_id));
CREATE TABLE Payment (
  payment_id            varchar(100) NOT NULL,
  contract_id           varchar(100),
  payment_type          varchar(255) NOT NULL,
  amount                numeric(15, 2) NOT NULL,
  due_date              date NOT NULL,
  paid_date             date,
  installment_number    int4,
  payment_method        varchar(255),
  transaction_reference varchar(100),
  status                varchar(255) DEFAULT 'Pending',
  overdue_days          int4 DEFAULT 0,
  penalty_amount        numeric(15, 2) DEFAULT 0,
  notes                 text,
  created_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at            timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (payment_id));
CREATE TABLE ViolationReport (
  violation_id        varchar(100) NOT NULL,
  reporter_user_id    varchar(100),
  violation_type      varchar(255) NOT NULL,
  description         text NOT NULL,
  evidence            text,
  status              varchar(255) DEFAULT 'Pending',
  severity            varchar(255) NOT NULL,
  penalty_applied     varchar(255),
  resolution          text,
  resolved_at         timestamp,
  created_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  related_entity_type varchar(255),
  related_entity_id   varchar(100),
  PRIMARY KEY (violation_id));
CREATE TABLE Review (
  review_id      varchar(100) NOT NULL,
  appointment_id varchar(100),
  contract_id    varchar(100),
  rating         int2 NOT NULL CHECK(rating >= 1 AND rating <= 5),
  comment        text,
  created_at     timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at     timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (review_id));
CREATE TABLE CustomerFavoriteProperty (
  customer_id varchar(100) NOT NULL,
  property_id varchar(100) NOT NULL,
  created_at  timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at  timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id,
  property_id));
CREATE TABLE CustomerPreferredPropertyType (
  customer_id      varchar(100) NOT NULL,
  property_type_id varchar(100) NOT NULL,
  created_at       timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at       timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id,
  property_type_id));
CREATE TABLE CustomerPreferredWard (
  customer_id varchar(100) NOT NULL,
  ward_id     varchar(100) NOT NULL,
  created_at  timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at  timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (customer_id,
  ward_id));
CREATE TABLE Notification (
  notification_id     varchar(100) NOT NULL,
  recipient_id        varchar(100) NOT NULL,
  type                varchar(255),
  title               varchar(200) NOT NULL,
  message             text NOT NULL,
  related_entity_type varchar(255),
  related_entity_id   varchar(100),
  delivery_status     varchar(255),
  is_read             int4 DEFAULT FALSE,
  img_url             varchar(255) NOT NULL,
  read_at             timestamp,
  created_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at          timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notification_id));
CREATE TABLE Report (
  report_id    varchar(100) NOT NULL,
  report_type  varchar(255) NOT NULL,
  report_month int4,
  report_year  int4,
  title        varchar(200) NOT NULL,
  description  text,
  start_date   date NOT NULL,
  end_date     date NOT NULL,
  file_path    varchar(500),
  file_format  varchar(255) NOT NULL,
  status       varchar(255) DEFAULT 'Generating',
  completed_at timestamp,
  created_at   timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at   timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE FinancialReport (
  report_id                             varchar(100) NOT NULL,
  total_revenue_current_month           numeric(15, 2),
  total_revenue                         numeric(15, 2),
  total_service_fees_current_month      numeric(15, 2),
  total_service_fees                    numeric(15, 2),
  contract_count_current_month          int4,
  contract_count                        int4,
  total_commission_earned_current_month numeric(15, 2),
  total_commission_earned               numeric(15, 2),
  top_performing_city_id                varchar(100) NOT NULL,
  top_performing_district_id            varchar(100) NOT NULL,
  top_performing_ward_id                varchar(100),
  top_performing_property_type_id       varchar(100),
  net_profit                            numeric(15, 2),
  avg_property_price                    numeric(15, 2),
  total_rates                           int4,
  avg_rating                            numeric(5, 2),
  total_rates_current_month             int4,
  avg_rating_current_month              numeric(5, 2),
  created_at                            timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at                            timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE AgentPerformanceReport (
  report_id                               varchar(100) NOT NULL,
  "total_ agents"                         int4,
  total_active_agents                     int4,
  top_performer_agent_id_current_month    varchar(100) NOT NULL,
  top_performer_revenue_current_month     numeric(15, 2) DEFAULT TRUE,
  top_performer_agent_id                  varchar(100) NOT NULL,
  top_performer_revenue                   numeric(15, 2) DEFAULT CURRENT_TIMESTAMP,
  bottom_performer_agent_id_current_month varchar(100) NOT NULL,
  bottom_performer_revenue_current_month  numeric(15, 2),
  avg_revenue_per_agent                   numeric(15, 2),
  avg_customer_satisfaction               int4,
  total_rates                             int4,
  avg_rating                              numeric(5, 2),
  total_rates_current_month               int4,
  avg_rating_current_month                numeric(5, 2),
  created_at                              timestamp,
  updated_at                              timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE PropertyStatisticsReport (
  report_id                             varchar(100) NOT NULL,
  total_active_properties               int4,
  total_sold_properties_current_month   int4,
  total_sold_properties                 numeric(5, 2) DEFAULT TRUE,
  total_rented_properties_current_month numeric(15, 2),
  total_rented_properties               int4,
  most_popular_property_type_id         varchar(255),
  most_popular_location_id              varchar(255),
  created_at                            timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at                            timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE CustomerAnalyticsReport (
  report_id                            varchar(100) NOT NULL,
  total_active_customers               int4 DEFAULT 'All',
  new_customers_acquired_current_month int4 DEFAULT 'All',
  customer_churn_rate                  numeric(5, 2),
  avg_customer_transaction_value       numeric(15, 2),
  high_value_customers_count           int4 DEFAULT TRUE,
  customer_satisfaction_score          numeric(5, 2) DEFAULT TRUE,
  total_rates                          int4,
  avg_rating                           numeric(5, 2),
  total_rates_current_month            int4,
  avg_rating_current_month             numeric(5, 2),
  created_at                           timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at                           timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE ViolationReportDetails (
  report_id                               varchar(100) NOT NULL,
  total_violations_reported               int4 DEFAULT 'All',
  total_violations_reported_current_month int4 DEFAULT 'All',
  serious_violations_count_curent_month   int4 DEFAULT 'All',
  serious_violations_count                int4 DEFAULT 'All',
  minor_violations_count_curent_month     int4 DEFAULT TRUE,
  minor_violations_count                  int4 DEFAULT TRUE,
  avg_resolution_time_hours               int4 DEFAULT TRUE,
  violation_trend                         numeric(5, 2),
  most_common_violation_type              varchar(255),
  accounts_suspended                      int4,
  compliance_rate                         numeric(5, 2),
  created_at                              timestamp DEFAULT CURRENT_TIMESTAMP,
  updated_at                              timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (report_id));
CREATE TABLE IndividualSalesAgentRanking (
  ranking_id                      varchar(255) NOT NULL,
  agent_id                        varchar(100) NOT NULL,
  ranking_month                   int4,
  ranking_year                    int4,
  month_revenue                   numeric(15, 2),
  month_deals                     int4,
  month_properties_assigned       int4,
  month_appointments_completed    int4,
  month_customer_satisfaction_avg numeric(3, 2),
  performance_tier                varchar(255),
  ranking_position                int4,
  created_at                      timestamp,
  updated_at                      timestamp,
  PRIMARY KEY (ranking_id));
CREATE TABLE IndividualCustomerLead (
  lead_id                  varchar(255) NOT NULL,
  customer_id              varchar(100) NOT NULL,
  lead_month               int4,
  lead_year                int4,
  month_viewings_requested int4,
  month_viewings_attended  int4,
  month_spending           numeric(15, 2),
  month_purchases          numeric(15, 2),
  month_rentals            numeric(15, 2),
  month_contracts_signed   int4,
  lead_score               int4,
  lead_classification      varchar(255),
  customer_tier            varchar(255),
  created_at               timestamp,
  updated_at               timestamp,
  PRIMARY KEY (lead_id));
CREATE TABLE City (
  city_id        varchar(100) NOT NULL,
  city_name      varchar(255),
  description    varchar(255),
  img_url        varchar(255),
  total_area     numeric(15, 2),
  avg_land_price numeric(15, 2),
  population     int4,
  is_active      bool,
  created_at     timestamp,
  updated_ad     timestamp,
  PRIMARY KEY (city_id));
CREATE TABLE District (
  district_id    varchar(100) NOT NULL,
  city_id        varchar(100) NOT NULL,
  district_name  varchar(255),
  img_url        varchar(255),
  description    varchar(255),
  total_area     numeric(15, 2),
  avg_land_price numeric(15, 2),
  population     int4,
  is_active      bool,
  created_at     timestamp,
  updated_at     timestamp,
  PRIMARY KEY (district_id));
CREATE TABLE CustomerPreferredDistrict (
  customer_id varchar(100) NOT NULL,
  district_id varchar(100) NOT NULL,
  created_at  timestamp,
  updated_at  timestamp,
  PRIMARY KEY (customer_id,
  district_id));
CREATE TABLE CustomerPreferredCity (
  customer_id varchar(100) NOT NULL,
  city_id     varchar(100) NOT NULL,
  created_at  timestamp,
  updated_at  timestamp,
  PRIMARY KEY (customer_id,
  city_id));
CREATE TABLE DocumentType (
  document_type_id varchar(100) NOT NULL,
  description      varchar(255),
  name             varchar(100),
  is_compulsory    bool,
  created_at       timestamp,
  updated_at       timestamp,
  PRIMARY KEY (document_type_id));
CREATE TABLE SearchLog (
  log_id                 varchar(100) NOT NULL,
  total_searches         int4,
  current_month_searches int4,
  month                  int4,
  year                   int4,
  city_id                varchar(100) NOT NULL,
  ward_id                varchar(100) NOT NULL,
  district_id            varchar(100) NOT NULL,
  property_id            varchar(100),
  created_at             timestamp,
  updated_at             timestamp,
  PRIMARY KEY (log_id));
CREATE UNIQUE INDEX unique_customer_property
  ON CustomerFavoriteProperty (customer_id, property_id);
ALTER TABLE SalesAgent ADD CONSTRAINT FKSalesAgent593553 FOREIGN KEY (agent_id) REFERENCES "User" (user_id) ON DELETE Cascade;
ALTER TABLE Customer ADD CONSTRAINT FKCustomer730249 FOREIGN KEY (customer_id) REFERENCES "User" (user_id) ON DELETE Cascade;
ALTER TABLE Property ADD CONSTRAINT FKProperty269139 FOREIGN KEY (owner_id) REFERENCES PropertyOwner (owner_id) ON DELETE Cascade;
ALTER TABLE Property ADD CONSTRAINT FKProperty341603 FOREIGN KEY (assigned_agent_id) REFERENCES SalesAgent (agent_id) ON DELETE Set null;
ALTER TABLE Property ADD CONSTRAINT FKProperty26489 FOREIGN KEY (property_type_id) REFERENCES PropertyType (property_type_id);
ALTER TABLE Property ADD CONSTRAINT FKProperty423000 FOREIGN KEY (ward_id) REFERENCES ward (ward_id);
ALTER TABLE Media ADD CONSTRAINT FKMedia917383 FOREIGN KEY (property_id) REFERENCES Property (property_id) ON DELETE Cascade;
ALTER TABLE Appointment ADD CONSTRAINT FKAppointmen571385 FOREIGN KEY (property_id) REFERENCES Property (property_id) ON DELETE Cascade;
ALTER TABLE Appointment ADD CONSTRAINT FKAppointmen538755 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE Cascade;
ALTER TABLE Appointment ADD CONSTRAINT FKAppointmen857420 FOREIGN KEY (agent_id) REFERENCES SalesAgent (agent_id) ON DELETE Cascade;
ALTER TABLE Contract ADD CONSTRAINT FKContract393909 FOREIGN KEY (property_id) REFERENCES Property (property_id) ON DELETE Cascade;
ALTER TABLE Contract ADD CONSTRAINT FKContract361279 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE Cascade;
ALTER TABLE Contract ADD CONSTRAINT FKContract936693 FOREIGN KEY (agent_id) REFERENCES SalesAgent (agent_id) ON DELETE Cascade;
ALTER TABLE ViolationReport ADD CONSTRAINT FKViolationR763799 FOREIGN KEY (reporter_user_id) REFERENCES "User" (user_id) ON DELETE Set null;
ALTER TABLE CustomerFavoriteProperty ADD CONSTRAINT FKCustomerFa876279 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE Cascade;
ALTER TABLE CustomerFavoriteProperty ADD CONSTRAINT FKCustomerFa843649 FOREIGN KEY (property_id) REFERENCES Property (property_id) ON DELETE Cascade;
ALTER TABLE CustomerPreferredPropertyType ADD CONSTRAINT FKCustomerPr459946 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE Cascade;
ALTER TABLE CustomerPreferredPropertyType ADD CONSTRAINT FKCustomerPr681972 FOREIGN KEY (property_type_id) REFERENCES PropertyType (property_type_id) ON DELETE Cascade;
ALTER TABLE CustomerPreferredWard ADD CONSTRAINT FKCustomerPr378043 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id) ON DELETE Cascade;
ALTER TABLE CustomerPreferredWard ADD CONSTRAINT FKCustomerPr633915 FOREIGN KEY (ward_id) REFERENCES ward (ward_id) ON DELETE Cascade;
ALTER TABLE Notification ADD CONSTRAINT FKNotificati527261 FOREIGN KEY (recipient_id) REFERENCES "User" (user_id) ON DELETE Cascade;
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR360481 FOREIGN KEY (report_id) REFERENCES Report (report_id) ON DELETE Cascade;
ALTER TABLE AgentPerformanceReport ADD CONSTRAINT FKAgentPerfo700169 FOREIGN KEY (report_id) REFERENCES Report (report_id) ON DELETE Cascade;
ALTER TABLE PropertyStatisticsReport ADD CONSTRAINT FKPropertySt216140 FOREIGN KEY (report_id) REFERENCES Report (report_id) ON DELETE Cascade;
ALTER TABLE CustomerAnalyticsReport ADD CONSTRAINT FKCustomerAn764401 FOREIGN KEY (report_id) REFERENCES Report (report_id) ON DELETE Cascade;
ALTER TABLE ViolationReportDetails ADD CONSTRAINT FKViolationR924267 FOREIGN KEY (report_id) REFERENCES Report (report_id) ON DELETE Cascade;
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR590323 FOREIGN KEY (top_performing_ward_id) REFERENCES ward (ward_id);
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR514057 FOREIGN KEY (top_performing_property_type_id) REFERENCES PropertyType (property_type_id);
ALTER TABLE AgentPerformanceReport ADD CONSTRAINT FKAgentPerfo894789 FOREIGN KEY (top_performer_agent_id_current_month) REFERENCES SalesAgent (agent_id);
ALTER TABLE AgentPerformanceReport ADD CONSTRAINT FKAgentPerfo893268 FOREIGN KEY (top_performer_agent_id) REFERENCES SalesAgent (agent_id);
ALTER TABLE AgentPerformanceReport ADD CONSTRAINT FKAgentPerfo521910 FOREIGN KEY (bottom_performer_agent_id_current_month) REFERENCES SalesAgent (agent_id);
ALTER TABLE IndividualSalesAgentRanking ADD CONSTRAINT FKIndividual383398 FOREIGN KEY (agent_id) REFERENCES SalesAgent (agent_id);
ALTER TABLE IndividualCustomerLead ADD CONSTRAINT FKIndividual615639 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id);
ALTER TABLE District ADD CONSTRAINT FKDistrict587805 FOREIGN KEY (city_id) REFERENCES City (city_id);
ALTER TABLE ward ADD CONSTRAINT FKward8497 FOREIGN KEY (district_id) REFERENCES District (district_id);
ALTER TABLE CustomerPreferredDistrict ADD CONSTRAINT FKCustomerPr78527 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id);
ALTER TABLE CustomerPreferredDistrict ADD CONSTRAINT FKCustomerPr237173 FOREIGN KEY (district_id) REFERENCES District (district_id);
ALTER TABLE CustomerPreferredCity ADD CONSTRAINT FKCustomerPr789993 FOREIGN KEY (customer_id) REFERENCES Customer (customer_id);
ALTER TABLE CustomerPreferredCity ADD CONSTRAINT FKCustomerPr106676 FOREIGN KEY (city_id) REFERENCES City (city_id);
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR743328 FOREIGN KEY (top_performing_district_id) REFERENCES District (district_id);
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR651133 FOREIGN KEY (top_performing_city_id) REFERENCES City (city_id);
ALTER TABLE IdentificationDocument ADD CONSTRAINT FKIdentifica449652 FOREIGN KEY (document_type_id) REFERENCES DocumentType (document_type_id);
ALTER TABLE Media ADD CONSTRAINT FKMedia809716 FOREIGN KEY (violation_id) REFERENCES ViolationReport (violation_id);
ALTER TABLE SalesAgent ADD CONSTRAINT FKSalesAgent593554 FOREIGN KEY (agent_id) REFERENCES "User" (user_id);
ALTER TABLE Customer ADD CONSTRAINT FKCustomer730250 FOREIGN KEY (customer_id) REFERENCES "User" (user_id);
ALTER TABLE AgentPerformanceReport ADD CONSTRAINT FKAgentPerfo700170 FOREIGN KEY (report_id) REFERENCES Report (report_id);
ALTER TABLE FinancialReport ADD CONSTRAINT FKFinancialR360482 FOREIGN KEY (report_id) REFERENCES Report (report_id);
ALTER TABLE ViolationReportDetails ADD CONSTRAINT FKViolationR924268 FOREIGN KEY (report_id) REFERENCES Report (report_id);
ALTER TABLE CustomerAnalyticsReport ADD CONSTRAINT FKCustomerAn764402 FOREIGN KEY (report_id) REFERENCES Report (report_id);
ALTER TABLE PropertyStatisticsReport ADD CONSTRAINT FKPropertySt216141 FOREIGN KEY (report_id) REFERENCES Report (report_id);
ALTER TABLE SearchLog ADD CONSTRAINT FKSearchLog171497 FOREIGN KEY (city_id) REFERENCES City (city_id);
ALTER TABLE SearchLog ADD CONSTRAINT FKSearchLog110687 FOREIGN KEY (ward_id) REFERENCES ward (ward_id);
ALTER TABLE SearchLog ADD CONSTRAINT FKSearchLog887444 FOREIGN KEY (property_id) REFERENCES Property (property_id);
ALTER TABLE SearchLog ADD CONSTRAINT FKSearchLog460885 FOREIGN KEY (district_id) REFERENCES District (district_id);
ALTER TABLE IdentificationDocument ADD CONSTRAINT FKIdentifica210444 FOREIGN KEY (property_id) REFERENCES Property (property_id);
ALTER TABLE "User" ADD CONSTRAINT FKUser4483 FOREIGN KEY (district_id) REFERENCES District (district_id);
ALTER TABLE "User" ADD CONSTRAINT FKUser627899 FOREIGN KEY (city_id) REFERENCES City (city_id);
ALTER TABLE PropertyOwner ADD CONSTRAINT FKPropertyOw564258 FOREIGN KEY (owner_id) REFERENCES "User" (user_id);
ALTER TABLE Payment ADD CONSTRAINT FKPayment763855 FOREIGN KEY (contract_id) REFERENCES Contract (contract_id);
ALTER TABLE Review ADD CONSTRAINT FKReview395465 FOREIGN KEY (appointment_id) REFERENCES Appointment (appointment_id);
ALTER TABLE Review ADD CONSTRAINT FKReview278976 FOREIGN KEY (contract_id) REFERENCES Contract (contract_id);
