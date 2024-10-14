# Honeywell Asset Inventory

This reference project demonstrates how to perform asset inventory using Honeywell devices equipped with barcode scanners and export the results in a CSV format that can be re-imported into an SAP system.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Required Software](#required-software)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [License](#license)

## Project Overview

This project uses the Honeywell SDK, which enables barcode scanning with Honeywell devices, such as mobile terminals. The software performs an asset inventory, using CSV files exported from the SAP system as the source data, and allows modifications during the inventory process to be saved and exported back to a CSV format compatible with the SAP system.

## Features

- **Barcode-based Inventory**: Identifies and records assets using Honeywell devices equipped with barcode scanners.
- **SAP Integration**: Uses CSV files exported from the SAP system as the source, and the inventory results are saved in SAP-compatible CSV format.
- **Offline Operation**: The system is capable of operating offline during the on-site inventory process, and the modified CSV file can be re-imported into the SAP system.
- **Honeywell SDK Integration**: Configures the barcode scanner and other device-specific functions using the Honeywell SDK.

## Required Software

To run this project, you will need the following:

- Honeywell SDK (available from the official Honeywell website)
- Java or Kotlin (for Android development)
- Android Studio or another compatible IDE
- SAP system for exporting and importing CSV files

## Installation

1. **Install Honeywell SDK**:
   Download and install the Honeywell SDK for your development environment.
   
   - Visit the [Honeywell Developer](https://developer.honeywell.com/) page and register to download the necessary SDK.
   
2. **Clone the Project**:
   Clone the project from the GitHub repository