# AI Tutor Architecture Diagrams

Use this document for architecture walkthroughs and slide preparation. The diagrams are written in Mermaid so they can be rendered by GitHub, Mermaid Live Editor, Notion, Obsidian, and many presentation tools.

## 1. System Architecture

```mermaid
flowchart TB
    Student["Student on phone or tablet"]
    Mobile["Expo React Native App\nCamera, timer, timeline, nudges, tutor chat, study history"]
    API["Spring Boot API\nAuth, assignments, sessions, tutor policy, storage metadata"]
    DB[("PostgreSQL\nSystem of record")]
    LocalImages[("Local Image Storage\n/Users/vipul.pandey/images/{studentId}/{category}")]
    Worker["Python FastAPI Worker\nAssignment, progress, focus, tutor analysis"]
    Ollama["Ollama Local LLM\ngemma3:latest"]
    OpenAI["OpenAI API\nOptional hosted AI"]
    Metabase["Existing Metabase\nDashboards, pulses, parent/team reporting"]

    Student --> Mobile
    Mobile -->|"HTTPS/JSON + multipart image upload"| API
    API --> DB
    API --> LocalImages
    API -->|"Internal JSON, worker token, HTTP/1.1"| Worker
    Worker -->|"Local model request"| Ollama
    Worker -. "Optional future provider" .-> OpenAI
    Metabase -->|"Read-only analytics queries"| DB
```

## 2. Runtime Flow

```mermaid
sequenceDiagram
    actor Student
    participant App as Expo Mobile App
    participant API as Spring Boot API
    participant Store as Local Image Storage
    participant DB as PostgreSQL
    participant Worker as Python Worker
    participant LLM as Ollama gemma3

    Student->>App: Capture homework image
    App->>API: Create assignment
    API->>DB: Save assignment
    App->>API: Upload homework image
    API->>Store: Save under student assignment folder
    API->>Worker: Analyze assignment image
    Worker->>LLM: Extract questions, effort, timeline hints
    LLM-->>Worker: Structured response
    Worker-->>API: Assignment analysis result
    API->>DB: Save features and plan steps
    API-->>App: Timeline and assignment summary

    Student->>App: Start study session
    App->>API: Start session
    API->>DB: Save started_at and current checkpoint
    API-->>App: Active session

    loop 3 or 4 times per minute
        App->>API: Upload progress/focus image
        API->>Store: Save under student progress/focus folder
        API->>Worker: Analyze progress or focus
        Worker-->>API: Completion/focus result
        API->>DB: Save capture, result, and event
        API-->>App: On-track/behind/focus alert result
    end

    Student->>App: Ask tutor question
    App->>API: Send tutor message
    API->>DB: Save question with asked-at time
    API->>Worker: Generate hint-first response
    Worker->>LLM: Tutor prompt
    LLM-->>Worker: Hint response
    Worker-->>API: Tutor answer
    API->>DB: Save tutor response
    API-->>App: Show hint

    Student->>App: Finish session
    App->>API: Complete final checkpoint or end session
    API->>DB: Save finished_at and actual duration
    API-->>App: Session summary
```

## 3. Product Modules

```mermaid
flowchart LR
    subgraph Mobile["Mobile and Tablet App"]
        Camera["Camera Capture"]
        Timeline["Timeline UI"]
        Nudges["Popup, Voice, Haptic Nudges"]
        TutorUI["Tutor Help Icon"]
        HistoryUI["My Study History"]
    end

    subgraph Spring["Spring Boot API"]
        Auth["Local Auth"]
        Assignment["Assignment Module"]
        Planner["Planner Module"]
        Session["Study Session Module"]
        Progress["Progress Capture Module"]
        Focus["Focus Module"]
        Tutor["Tutor Module"]
        StudyRecord["Student Study Record Module"]
        Analytics["Analytics Event Writes"]
    end

    subgraph Worker["Python Worker"]
        AssignmentAI["Assignment Analysis"]
        ProgressAI["Progress Analysis"]
        FocusAI["Focus Analysis"]
        TutorAI["Tutor Hint Generation"]
    end

    Mobile --> Spring
    Assignment --> AssignmentAI
    Progress --> ProgressAI
    Focus --> FocusAI
    Tutor --> TutorAI
    StudyRecord --> Assignment
    StudyRecord --> Session
    StudyRecord --> Progress
    StudyRecord --> Tutor
    Analytics --> Metabase["Metabase Reads DB Views"]
```

## 4. Data Ownership

```mermaid
erDiagram
    STUDENTS ||--o{ ASSIGNMENTS : owns
    STUDENTS ||--o{ STUDY_SESSIONS : starts
    ASSIGNMENTS ||--o{ ASSIGNMENT_ASSETS : has
    ASSIGNMENTS ||--o{ ASSIGNMENT_PLAN_STEPS : plans
    ASSIGNMENTS ||--o{ STUDY_SESSIONS : studied_in
    STUDY_SESSIONS ||--o{ PROGRESS_CAPTURES : records
    STUDY_SESSIONS ||--o{ FOCUS_EVENTS : records
    STUDY_SESSIONS ||--o{ TUTOR_THREADS : has
    TUTOR_THREADS ||--o{ TUTOR_MESSAGES : contains

    STUDENTS {
        uuid id
        string username
        int age
        int grade_level
    }
    ASSIGNMENTS {
        uuid id
        uuid student_id
        string subject
        string status
        timestamp created_at
        timestamp completed_at
    }
    STUDY_SESSIONS {
        uuid id
        uuid assignment_id
        timestamp started_at
        timestamp finished_at
        int actual_duration_seconds
    }
    PROGRESS_CAPTURES {
        uuid id
        uuid session_id
        string asset_path
        timestamp created_at
    }
    TUTOR_MESSAGES {
        uuid id
        uuid thread_id
        string student_role
        string tutor_role
        string content
        timestamp created_at
    }
```

## 5. Presentation Summary

```text
Student app captures homework and study progress.
Spring Boot owns product state and persistence.
Python worker handles AI/CV analysis behind internal APIs.
Ollama gives local AI without API keys; OpenAI can be added later.
PostgreSQL remains the source of truth.
Images are saved by student id and category.
Metabase reads analytics directly from the database.
```
