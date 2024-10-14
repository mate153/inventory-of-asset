package com.devforce.inventory.config

  /******************************/      /********************************/
 /**   C.O.N.F.I.G  A.R.E.A   **/      /**   Last Build: 2024.05.08   **/
/******************************/      /********************************/

/*   Declare DevForce   */
object DevForceConfig {

    /* BASICS */
        object Basics {
            const val VERSION = "1.0.0"                           // Current version
            const val PROJECT_ID = "simple-select-153153"         // Unique identifier
            const val ACCEPTED_IMPORT_EXTENSION = "csv"           // Only CSV extension is accepted
        }

    /* AUTHENTICATION */
        object Auth {
            const val NEED_AUTH = false                               // Need authentication form
            val USERS = arrayOf(             // Users who can log in
                mapOf("name" to "nhs", "password" to "2020"),
                mapOf("name" to "devforce", "password" to "techforce")
            )
        }

    /* UPDATE */
        object Update {
            const val ACTIVE = false                                 // Updater active or deactivate
            const val PROTOCOL = "http"                             // Updater server protocol
            const val IP_ADDRESS = "80.211.202.5"                   // Updater server IP address
            const val PORT = 8080                                   // Updater server port
        }

    /* EXPORT SETTINGS */
        object Export {
            const val OUTPUT_REGULAR_FILE_NAME = "EXPORT_inventory"     // Exported Regular CSV file name
            const val OUTPUT_BUILD_FILE_NAME = "EXPORT_inventory"       // Exported Build CSV file name
            const val OUTPUT_FILE_EXTENSION = "csv"                     // DO NOT MODIFY !
            const val DATE_IN_FILE_NAME = true                          // Actual date will show in exported file name
            const val NEED_CONFIRMATION = true                          // Before exporting, need a user confirmation
            const val EXPORT_PROCESS_DELAY = 1500L                      // Delay after success export process
        }

    /* SAVE STATE SETTINGS */
        object Save {
            object Regular {
                const val JSON_FILE_NAME = "inventory_save"         // JSON file name, that contains save state
                const val JSON_FILE_EXTENSION = "json"              // DO NOT MODIFY
            }
            object Build {
                const val JSON_FILE_NAME = "build_inventory_save"   // JSON file name, that contains save state
                const val JSON_FILE_EXTENSION = "json"              // DO NOT MODIFY
            }
        }

    /* EXPORT TEMPLATE */
        object Template {                                       // This template depend on row indexes
            val EXPORT_TEMPLATE = intArrayOf(
                0, 1, 2, 9, 5, 7, 10, 8, 11, 0
            )
        }
}