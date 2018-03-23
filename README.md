[![Build Status](https://travis-ci.org/openmrs/openmrs-module-registrationapp.svg?branch=master)](https://travis-ci.org/openmrs/openmrs-module-registrationapp)

Registration App Module
========================

Registration App for the Reference Application

Buildung and running
=====================

    mvn clean install

Before running generation make sure that you have a database gnereted by sdk with concepts inserted.

Obligatory options:</br>
--db-name <name_of_database> </br>
--db-password <database_password>

Optionally:
--clinics <number_of_clinics> (default:140)</br>
--patients <number_of_patients> (default:13000)</br>
--visits <number_of_visits_per_patient> (default:47)</br>
--encounters <number_of_encounters_per_visit> (default:1)</br>
--obs <number_of_cobservations_per_encounter> (default:55)</br>
--db-login <database_username> (default:root)</br>
--db-server <database_servername> (default:localhost)</br>
--db-port <database_port> (default:3306)</br>
--start-date <start_date_for_entities_generation> (default: 1970.01.01 00:00)</br>

Example:

    java -jar performance-data-0.0.1-SNAPSHOT.jar --db-name generation_db --db-password password123 --clinics 19

