# Warehouse Barcode Scanner (Mobile App)

This repository contains the mobile application for the Warehouse Product Management System developed by the **Koło Naukowe Programistów i Miłośników Informatyki, Politechnika Krakowska**.

The app allows users to:
* Scan product and location barcodes.
* Add, update, move, and delete products.

The architecture is based on **Clean Architecture**, separating domain, data, and presentation layers and **MVVM** architecture

## Documentation
Detailed documentation and project requirements are available on the **Koło Naukowe Discord server**. Team members should refer to the documentation for data models, use cases, and API specifications.

## Getting Started

### Cloning the Repository
```bash
git https://github.com/grupadotnet/code-warehouse-mobile.git
cd code-warehouse-mobile
```

### Branching Strategy
For each parent task or issue, create a dedicated branch:
```bash
git checkout -b <issue-number>-<short-task-name>
```
Commit your changes regularly with descriptive messages. (Please use english language for commits)

After completing the task, push your branch to the repository:
```bash
git push origin <branch-name>
```
Open a Pull Request for review before merging into `main` or `master`.

## Project Structure
* **domain** – core business logic, models, and repository interfaces
* **data** – repository implementations and mock data (later data from api)
* **di** – Hilt dependency injection setup
* **presentation** – UI and ViewModels (future implementation)

## Contribution Guidelines
1. Always check the relevant issue before starting work.
2. Create a branch based on the issue number.
3. After finishing a task, push the branch and submit a Pull Request for review.
4. Ensure your code follows project conventions and passes any tests.

## Notes
* The current repository uses fake/mock data to allow UI development without backend integration.
* Backend integration (Retrofit API) will be added in later tasks.
* Keep branches small and focused on a single parent task for easier review.
