# Step 4 Report: Testing, APK Build, Bugs and Limitations

## 1. Testing Strategy
**UNIT TEST:**
| Aspect | Description | Rationale |
| :-------------- | :-------------- | :---------------- |
| **Target Components** | ViewModels (e.g., ItemViewModel), business logic, data utility classes. | To verify that the core application logic is correct, efficient, and free of defects. |
| **Frameworks/Libraries** | JUnit 4/5, Robolectric, MockK. | Allows execution on the developer's local machine (JVM) for fast feedback loops. |
| **Implementation Example** | ItemViewModelTest.kt verifies that ItemViewModel.addItem() correctly calls ItemRepository.insert(). |  |
| **Key Annotation/Rule** | InstantTaskExecutorRule, runTest (for Coroutines). | Ensures asynchronous components are tested reliably. |
| **Strategy Goal** | Correctness: Guarantee that functions perform their intended calculation or state change accurately. |  |

<br><br>
**UI TEST:**
| Aspect | Description | Rationale |
| :-------------- | :-------------- | :---------------- |
| **Target Components** | VActivities (e.g., LoginActivity), Fragments, User Flows. | To confirm the end-to-end user experience and test interactions across multiple screens. |
| **Frameworks/Libraries** | Espresso, AndroidX Test, ActivityScenarioRule. | Espresso provides reliable synchronization with the UI thread. |
| **Implementation Example** | LoginActivityTest.kt confirms redirection and input validation flow. |  |
| **Key Annotation/Rule** | onView(...).perform(click()), check(matches(isDisplayed())). | Simulates real user taps and asserts resulting views/states. |
| **Strategy Goal** | Usability & Flow: Ensure seamless navigation and correct handling of user input. |  |


## 2. Build Process for APK

The application is built using the Gradle build system with Kotlin DSL.

- **Build Command:** The release APK is generated using the standard Gradle task:

  ```bash
  ./gradlew assembleRelease
  ```

- **Configuration:**
  - **Target SDK:** 36
  - **Build Types:** A `release` build type is configured.
- **Output:** The resulting APK is located at `app/release/app-release.apk`.

## 3. Known Bugs or Limitations

- **Test Coverage:** While key components like `ItemViewModel` and `LoginActivity` have associated tests, coverage does not extend to all ViewModels, Fragments, and Repositories.
- **The add items needs work** Some logic on how the notification should work is missing, and profile should be updated
- **Can make profile without real mail** - as we don't have 2 factor authentication for register.
