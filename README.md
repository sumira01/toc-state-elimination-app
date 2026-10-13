# State Elimination Tool
### DFA → GNFA → Regular Expression Conversion

**State Elimination Tool** is an interactive educational Android application and solver designed to help students, professors, and researchers understand and solve **Finite Automata (DFA) to Regular Expression conversion** using the formal **State Elimination Method**.

---

## 🌟 Key Features

1. **Flexible DFA Definition**:
   - Supports both **complete** and **partial/incomplete** DFA transition diagrams.
   - Undefined transitions are naturally represented as ∅ (no path) or can optionally be routed to an implicit trap state (`q_trap`).
   - Interactive transition editor with `+ Add Transition` and per-path deletion.

2. **Automata Visualization (Interactive Canvas)**:
   - Visual graph rendering for both DFA and GNFA states.
   - Directed curved arrows with regular expression labels, self-loops, and double rings for accepting states.
   - Interactive pan, zoom, and reset view controls.
   - Toggle between **Graph View** and **Transition Matrix Table View**.

3. **Generalized NFA (GNFA) Construction**:
   - Adds unique start state q_start (with ε → q0) and unique accept state q_accept (with ε from all final states).
   - Combines parallel transitions with regular expression unions (a | b).

4. **State Elimination Engine**:
   - Mathematical formula:
     R_ij = R_ij | R_ik · (R_kk)* · R_kj
   - **Auto Guided Mode**: Step-by-step carousel showing incoming/outgoing paths, loop expressions, and substituted values.
   - **Manual Mode**: Choose which state to eliminate interactively.
   - Safe algebraic simplification rules:
     ∅ · R = ∅, ε · R = R, ∅ | R = R, R | R = R, ∅* = ε

5. **Empirical Verification Engine**:
   - Automatically generates test strings (ε, length 1, 2, 3...) and tests them concurrently against the DFA simulator and the resulting Regular Expression to prove language equivalence.

6. **Presets & Random Generator**:
   - 10 built-in presets across Beginner, Intermediate, and Advanced categories (e.g. strings ending in 1, even number of 1s, binary modulo 3 counter).
   - Random DFA generator with configurable states (2–6), alphabet size, and final states.

7. **Educational Guides**:
   - Quick concept theory cards (DFA, NFA, GNFA, Kleene Star, ε, ∅, Union, Concatenation).
   - Viva and exam preparation tips.

---

## 🚀 How to Run the Application

### Option 1: Direct Live Web Link (Easiest — No Installation Needed)
You can test and use the application directly in any modern desktop or mobile web browser:
- **Web App URL:** https://ais-pre-swfnmifijxff5wvutimbmu-279193640861.us-east1.run.app
*(In Chrome or Edge on desktop, you can also click the "Install App" button in the address bar to install it as a standalone desktop window).*

---

### Option 2: Install Pre-compiled APK on Android or Desktop Emulator
A ready-to-install Android APK is included in this repository:
- **File location:** `StateEliminationTool.apk` (or `apk/StateEliminationTool.apk`)

**How to run:**
- **On Android:** Copy the APK to your phone or tablet and tap **Install**.
- **On Desktop (Windows / Mac):** Open an Android emulator (such as **LDPlayer**, **BlueStacks**, or **Android Studio Emulator**) and drag and drop `StateEliminationTool.apk` into the window.

---

### Option 3: Open and Build in Android Studio (From Source Code)
1. Download or extract the project folder.
2. Launch **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer recommended).
3. Select **Open an Existing Project** and choose this project folder.
4. Wait for Gradle sync to complete (repositories and dependencies will resolve automatically).
5. Select a virtual device (Emulator) or a physical Android phone connected via USB.
6. Click the green **Run (▶)** button.

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
- **Automata & Regex Engine:** Custom AST with recursive algebraic simplification
- **Min SDK:** Android 7.0 (API level 24)
- **Target SDK:** Android 16 (API level 36)
- **Build System:** Gradle (Kotlin DSL)
