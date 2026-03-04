# Finovate - AI Coding Agent Instructions

## Project Overview

**Finovate** is a desktop crowdfunding investment platform built with **JavaFX 17** and **MySQL**. It enables users to create projects seeking funding, invest in others' projects, and track their portfolio. The app uses **Maven** for build management and provides admin features for moderation.

### Key Tech Stack
- **UI Framework**: JavaFX 17 (FXML-based) with CSS styling
- **Database**: MySQL 8.0.33 via JDBC
- **APIs**: Exchange Rate API (TND↔EUR), Unsplash (project images), OpenStreetMap (via JavaFX WebView)
- **Build**: Maven with `javafx-maven-plugin`
- **Testing**: JUnit 5

---

## Architecture Patterns

### Session-Based Authentication
- **`Session.currentUser`** (static) holds the logged-in user globally
- **`UserService`** manages login/register; dev account: `dev@finovate.tn / dev123` (role: ADMIN)
- Password hashing: SHA-256 via `PasswordUtils.sha256()`
- User roles: `ADMIN` (full access) or `USER` (standard investor/creator)

### Service Layer (Business Logic)
- **`ProjectService`**: CRUD for crowdfunding projects, auto-status updates (OPEN→CLOSED when deadline passes)
- **`InvestissementService`**: Handle investment records, update project funding amounts
- **Singleton Pattern**: Services retrieve database connection via `MyDataBase.getInstance().getConnection()`
- All services throw `SQLException`; callers must handle database errors

### Model-View-Controller (MVC)
- **Models** ([Project.java](../src/main/java/org/esprit/finovate/models/Project.java)): Plain POJOs with getters/setters
- **Controllers**: Implement `Initializable`, manage UI logic, instantiate services locally
  - Controllers wire UI components with `@FXML` annotations
  - Controllers call `SceneUtils.changeScene()` for navigation
- **FXML Views** (`src/main/resources/fxml/`): Define UI layouts; each paired with a controller

### Database Connection Management
- Centralized singleton: [MyDataBase.java](../src/main/java/org/esprit/finovate/utils/MyDataBase.java)
- Connection pooling: **NOT implemented** (direct `DriverManager`)
- Config: Hardcoded (`localhost:3306/finovate`, root user, no password)
- **Gotcha**: Connection is initialized once on app startup; reconnection requires app restart

---

## Critical Workflows

### Running the App
```bash
# Desktop (JavaFX):
mvn javafx:run

# Or use IDE run configuration (FinovateApp)
```
- **PowerShell script**: `run-app.ps1` checks if Maven is available
- App starts with stub user (ID=1, role=ADMIN) for testing

### Building & Testing
```bash
# Compile:
mvn clean compile

# Tests (JUnit 5):
mvn test

# Full build:
mvn package
```

### Database Setup
- Auto-migrations: See `src/main/resources/db/migration_add_location_category.sql`
- Schema assumed to exist at startup (no automatic schema creation)
- **Dev workflow**: Manually run migrations before `mvn javafx:run`

---

## Project-Specific Patterns

### API Integration
- **`ExchangeRateService`**: Fetches TND→EUR rates with 6-hour caching (HTTP client)
- **`ApiConfig`**: Loads API keys from `src/main/resources/api_config.properties`
- **`UnsplashService`** & **`MapPicker`**: For image selection and location mapping
- Error handling: APIs return `null` if request fails; graceful fallback in UI

### Form Validation
- **`ValidationUtils`**: Static methods for email, project title, investment amount validation
- **`LiveValidationHelper`**: Real-time field validation as user types
- Invalid inputs: Show error messages; block form submission

### PDF Export
- **`PdfExportUtil`**: Generates project reports using PDFBox 3.0.1
- Used in investment confirmations and project summaries

### Image Management
- **`ImageUtils`**: Handles image upload/display in projects
- **`UnsplashService`**: Search and select free project images
- Images stored as file paths in database (e.g., `uploads/projects/{id}.jpg`)

### UI Navigation
- **`SceneUtils.changeScene()`**: Atomic scene replacement with stage reference
- Controllers must call `setStage(Stage)` before `initialize()`
- All FXML files in `src/main/resources/fxml/`; CSS in `src/main/resources/css/style.css`

---

## Code Conventions

### Naming
- **Package structure**: `org.esprit.finovate.{api,config,controllers,models,services,utils,view}`
- **Class suffixes**:
  - `*Service` = business logic
  - `*Controller` = JavaFX view controllers
  - `*Utils` = utility/helper static methods
  - `*Config` = configuration/constants

### Error Handling
- Services throw `SQLException`; controllers catch and display toast/alerts
- No silent failures; log errors with `System.out.println()` or `e.printStackTrace()`

### Database Operations
- Use `PreparedStatement` (parameterized queries) to prevent SQL injection
- Handle `ResultSet` with try-with-resources
- Map `ResultSet` → entity via private `mapResultSetTo*()` methods
- Null handling: Use `rs.wasNull()` after `.getLong()/.getDouble()`

### Testing
- **Test classes**: `src/test/java/org/esprit/finovate/` (e.g., `ProjectServiceTest.java`)
- Test services in isolation; mock `MyDataBase` connection if needed
- JUnit 5 annotations: `@Test`, `@BeforeEach`, `@DisplayName`

---

## Important Gotchas & Known Limitations

1. **No Connection Pooling**: Single `Connection` instance; reconnection requires restart
2. **Hardcoded DB Credentials**: Update [MyDataBase.java](../src/main/java/org/esprit/finovate/utils/MyDataBase.java) for production
3. **Session is Static**: Only one user per app instance; multi-threading can cause state issues
4. **FXML/Controller Coupling**: Controllers must be registered in FXML `fx:controller` attribute
5. **No Dependency Injection**: Services instantiate `MyDataBase.getInstance()` directly
6. **Date Handling**: Mix of `java.util.Date` (entities) and `java.sql.Date` (DB); conversions needed
7. **CSS/Image Loading**: Requires correct classpath; `getClass().getResource()` for FXML/CSS paths

---

## Common Tasks for Agents

- **Add a new feature**: Create model → Service → Controller → FXML → CSS
- **Fix a bug**: Reproduce in test, trace through service → database → controller
- **Refactor**: Follow existing patterns; maintain `Session.currentUser` access in controllers
- **Database changes**: Update schema migration, entity model, service queries, and tests
- **API integration**: Add to `api/` package; handle errors gracefully (null return)

