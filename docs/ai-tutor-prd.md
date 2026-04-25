# AI Tutor PRD and Technical Architecture

## 1. Product Summary

AI Tutor is a mobile/tablet-first study assistant for children. The app reads a photo of homework or classwork, estimates the work size, creates a realistic completion timeline, monitors progress, detects focus drift, and answers student questions in a hint-first tutoring style.

The product should help the child stay on track without creating pressure. It should behave like a study companion, not a surveillance tool.

## 2. Product Goals

- Convert a homework/classwork photo into a structured study plan.
- Estimate completion time using task type, word count, question count, subject, class, age, and learned student speed.
- Show a checkpoint-based timeline for the current study session.
- Support both manual progress marking and image-based progress detection.
- Detect long focus drift using on-device camera signals and show gentle nudges.
- Let the child ask for help through a help/hand gesture icon.
- Answer questions using hint-first tutoring instead of direct-answer-first behavior.
- Learn the child's actual pace over time.
- Let the student review their own study history: assignments, scanned images, extracted study questions, tutor questions asked, finish time, and actual duration.
- Provide analytics through Metabase, not custom parent/team dashboards.

## 3. Non-Goals For MVP

- No custom parent app.
- No custom parent analytics dashboard.
- No custom internal/team analytics dashboard.
- No continuous video recording.
- No emotion detection or behavioral judgment.
- No teacher/classroom management module.
- No web/PWA app for MVP.
- No external push notification system for MVP.
- No external authentication provider for MVP.
- No message broker or queue for MVP.

Analytics will be handled through Metabase dashboards, subscriptions, and alerts.

## 4. Target Users

### Primary User

The student using a phone or tablet during study.

### Secondary Users

- Parent or guardian, receiving shared Metabase reports or summaries.
- Internal team, using Metabase for product and learning analytics.

## 5. Core MVP Features

### 5.1 Homework Image Intake

The student captures or uploads an image of homework/classwork.

The system extracts:

- OCR text.
- Approximate word count.
- Question count.
- Subject.
- Task type.
- Expected writing amount.
- Expected reading amount.
- Difficulty/effort estimate.
- Visible instructions.

Task type examples:

- Copying.
- Reading.
- Short-answer writing.
- Long-answer writing.
- Math problem solving.
- Worksheet.
- Diagram/table work.

### 5.2 Timeline Planner

The app creates a timeline with checkpoints.

Example:

```text
0-3 min: Read instructions
3-10 min: Finish Q1-Q2
10-18 min: Finish Q3-Q5
18-22 min: Review answers
```

The estimate should use effort units, not only word count.

```text
estimated_time =
reading_time
+ writing_time
+ problem_solving_time
+ diagram_table_time
+ review_time
+ age_class_buffer
```

For English or Hindi writing/copying work, the writing estimate should use a cap of maximum 1 minute per handwritten line.

### 5.3 Manual Progress Tracking

The student can mark a checkpoint as done.

Manual tracking is required even if image-based detection exists because it is reliable and simple.

### 5.4 Image-Based Progress Detection

At planned intervals, the app can ask for a progress photo.

The system compares:

- Original assignment image.
- Assignment plan.
- Current progress image.
- Expected checkpoint.

It returns:

- Completion percentage.
- Completed questions/sections.
- Written content estimate.
- Confidence score.
- On-track / behind / ahead status.

If confidence is low, the app asks for confirmation.

Example:

```text
I am not sure if Q3 is complete. Mark Q3 as done?
```

### 5.5 Focus Monitoring

Focus detection should be optional and privacy-preserving.

The app should process face/head-direction signals on-device where possible and send only aggregate events to the backend.

Events:

```text
looking_away_started
looking_away_resolved
face_missing
returned_to_work
focus_nudge_shown
```

Suggested thresholds:

```text
Looking away for 20 seconds: local soft nudge
Looking away for 60 seconds: stronger local nudge
Away from desk for 2 minutes: pause or mark focus break
```

The app should not say "you are not studying." It should nudge specifically:

```text
Back to Q2.
Read the question once more.
Write the first sentence.
```

### 5.6 Ask Tutor

The child can tap a hand/help icon to ask a question.

Supported inputs:

- Text.
- Voice.
- Photo.
- Cropped question area.
- Current written answer for checking.

Tutor modes:

- Explain question.
- Give hint.
- Step-by-step help.
- Check my answer.
- Read aloud.
- Translate.

Default behavior must be hint-first:

```text
Let's solve it together.
```

Direct final answers should be policy-controlled and not the default.

### 5.7 Adaptive Learning

The app should update each student's speed profile after sessions.

Tracked by:

- Subject.
- Task type.
- Reading speed.
- Writing speed.
- Problem-solving speed.
- Focus blocks.
- Help requests.
- Actual vs estimated time.

### 5.8 Student Study Record

The student can open a "My Study" or "My Work" area to review completed and in-progress work.

The list view should show:

- Assignment title/subject.
- Assignment status.
- Planned duration.
- Actual duration, if finished.
- Started time.
- Finished time, if finished.
- On-track / behind / completed summary.

The detail view should show:

- Original scanned homework/classwork image.
- Progress/scanned images captured during the session.
- Extracted study questions or worksheet sections, when OCR/AI can identify them.
- Timeline checkpoints and completion status.
- Questions the student asked the tutor.
- Tutor responses.
- Time each question was asked.
- Session finish time.
- Actual vs planned completion difference.

This view is for the student first. Parent/team reporting remains outside the app and is handled through Metabase.

## 6. Product Scope After Removing Parent/Team Modules

Keep:

- Student app.
- Student study history and scanned work review.
- Study session engine.
- Assignment analysis.
- Timeline planner.
- Progress detection.
- Focus detection events.
- Ask Tutor.
- Analytics event storage.
- Metabase views.

Remove:

- Parent dashboard.
- Parent app.
- Team analytics UI.
- Custom reporting module.
- Custom parent notification center.

Metabase will read analytics views and provide subscriptions/alerts.

## 7. Recommended Architecture

Use Spring Boot as the product backend and Python workers for AI/CV workloads.

Presentation-ready diagrams are maintained in [architecture-diagram.md](/Users/vipul.pandey/projects/ai-tutor/docs/architecture-diagram.md).

```text
Expo React Native App
 iOS / Android / Tablet / Web
        |
Spring Boot API
        |
PostgreSQL + Object Storage
        |
Python AI/CV Workers
        |
OCR / OpenAI / ML Models
        |
Metabase analytics views + subscriptions
```

## 8. Frontend Tech Stack

- Expo React Native.
- TypeScript.
- Expo Router.
- Expo Camera.
- Expo SecureStore.
- TanStack Query for API state.
- Zustand for local session state.
- React Native Reanimated for progress/timer interactions.
- Native ML Kit bridge for mobile focus detection.

Reasoning:

- One codebase for iOS, Android, and tablet.
- Good camera support.
- Can ship to phones and tablets without a separate native rewrite.
- In-app nudges can be shown through local app state as banners, modals, sounds, or haptics.

## 9. Backend Tech Stack

### Spring Boot API

- Java 21.
- Spring Boot 3.5 initially for conservative compatibility.
- Spring Web MVC.
- Spring Security with local JWT/session authentication.
- Spring Data JPA or jOOQ.
- Spring Actuator for health, metrics, readiness, and liveness.
- OpenAPI contract generation.

Spring Boot owns all product state and business rules.

### Python Workers

- Python 3.11+.
- FastAPI for internal health/control endpoints only.
- OpenCV.
- Pillow.
- NumPy.
- OCR SDKs.
- OpenAI SDK.
- Optional Celery only if Python owns sub-task orchestration.

Python workers should be mostly stateless.

### Data and Infrastructure

- PostgreSQL as system of record.
- Object storage: S3, GCS, or Supabase Storage.
- No RabbitMQ or managed queue for MVP. Spring Boot calls Python workers through internal APIs.
- Redis only if needed for caching, locks, or rate limits.
- Metabase for analytics dashboards and subscriptions.
- OpenTelemetry for traces.
- Prometheus/Grafana or managed observability.

## 10. Service Boundaries

### Spring Boot Owns

- Authentication.
- Student profile.
- Assignment lifecycle.
- Study sessions.
- Timeline planning.
- Tutor policy.
- Progress result persistence.
- Focus event persistence.
- Analytics event writes.
- Direct Python worker API calls.

### Python Workers Own

- OCR.
- Homework image analysis.
- Progress image comparison.
- Computer vision experiments.
- Image preprocessing.
- AI feature extraction.

Python workers should return structured results to Spring Boot through internal APIs.

Avoid letting Python workers directly mutate core business tables.

## 11. Core Backend Modules

### 11.1 Student Profile Module

Stores:

- Age.
- Class/grade.
- Language.
- Reading speed.
- Writing speed.
- Subject-specific speed.
- Focus settings.
- Tutor policy settings.

### 11.2 Assignment Module

Handles:

- Assignment creation.
- Image upload lifecycle.
- OCR job creation.
- Assignment features.
- Assignment status.

### 11.3 Planner Module

Handles:

- Time estimation.
- Checkpoint creation.
- Break planning.
- Plan versioning.
- Replanning if progress diverges.

### 11.4 Study Session Module

Handles:

- Start/pause/resume/end.
- Current checkpoint.
- Manual progress.
- Session events.
- Actual duration.

### 11.5 Progress Analysis Module

Handles:

- Progress capture.
- Progress analysis job.
- Completion estimate.
- Confidence handling.
- Behind/ahead calculation.

### 11.6 Focus Module

Handles:

- Focus event ingestion.
- Focus streak calculation.
- Behind-risk signals.
- Local nudge history.

### 11.7 Tutor Module

Handles:

- Tutor conversation.
- Hint-first prompting.
- Tutor policy.
- Image/text/voice question handling.
- Answer checking.

### 11.8 Analytics Module

Handles:

- Append-only analytics events.
- Materialized views.
- Metabase-facing views.

### 11.9 Student Study Record Module

Handles read-only aggregation for the logged-in student:

- Assignment list.
- Assignment detail.
- Original assignment images.
- Progress/scanned images.
- Extracted questions.
- Session timeline.
- Tutor question history.
- Asked-at timestamps.
- Started-at, finished-at, and actual duration.

This module should not duplicate core data. It should compose data from assignment, study session, progress capture, and tutor tables.

## 12. Suggested Data Model

Core tables:

```text
students
student_speed_profiles
assignments
assignment_assets
assignment_features
assignment_plans
assignment_plan_steps
study_sessions
session_events
progress_captures
progress_analysis_results
focus_events
tutor_threads
tutor_messages
analytics_events
```

Important fields for student history:

```text
assignments.created_at
assignments.completed_at
assignment_assets.asset_type
assignment_assets.storage_path
assignment_features.extracted_questions
study_sessions.started_at
study_sessions.finished_at
study_sessions.actual_duration_seconds
progress_captures.created_at
progress_captures.asset_path
tutor_messages.created_at
tutor_messages.student_role
tutor_messages.content
```

Metabase views:

```text
mv_session_summary
mv_assignment_estimation_accuracy
mv_focus_summary
mv_tutor_usage
mv_student_speed_profile
mv_behind_schedule_events
mv_student_study_history
```

## 13. Main User Flows

### 13.1 Assignment Scan Flow

```text
App -> Spring: create assignment
App -> Storage: upload image using signed URL
Spring -> Python internal API: analyze assignment
Python -> OCR/AI: extract features
Python -> Spring: return structured analysis result
Spring -> DB: persist features + generate timeline
App -> Spring: fetch timeline
```

### 13.2 Study Session Flow

```text
App -> Spring: start session
Spring -> App: current plan/checkpoint
App: timer starts
Student: marks progress manually or uploads progress image
Spring: updates session state
App: shows on-track/behind/ahead
Spring: stores events for analytics
```

### 13.3 Progress Image Flow

```text
App -> Storage: upload progress image
App -> Spring: create progress capture
Spring -> Python internal API: analyze progress capture
Python: compare progress image against assignment/plan
Python -> Spring: return structured progress result
Spring: stores result and updates session status
App: shows result or asks for confirmation if low confidence
```

### 13.4 Focus Flow

```text
Device: runs local focus detection
App: shows local nudge after threshold
App -> Spring: sends aggregate focus event
Spring: stores event and updates session risk score
Metabase: reports focus summaries later
```

### 13.5 Ask Tutor Flow

```text
Student taps help icon
App sends text/voice/photo/crop
Spring applies tutor policy
Spring calls OpenAI or Python preprocessing if needed
Spring returns hint-first response
Session records help event
```

### 13.6 Student Study Record Flow

```text
Student opens My Study
App -> Spring: fetch my assignments/sessions summary
Spring -> DB: query assignments, latest session, completion times, plan summary
Spring -> App: return study history list
Student opens one assignment
App -> Spring: fetch assignment detail
Spring -> DB: query assignment images, extracted questions, plan steps, progress captures, tutor thread/messages
Spring -> App: return complete study record
App: shows scanned images, study questions, tutor questions, asked-at time, finished-at time, actual duration
```

## 14. Alerting and Nudges

Child-level nudges happen inside the app first.

Examples:

- "Back to Q2."
- "Try writing the first line."
- "Read the question once more."
- "Looks like this is taking longer. Want a hint?"

MVP nudges are in-app only. They can be implemented as banners, modals, haptics, sounds, or focus prompts while the app is open. External push notifications can be added later only if alerts are needed while the app is closed or backgrounded.

## 15. Privacy and Safety Requirements

- Focus monitoring must be opt-in.
- No continuous video storage.
- Store focus events, not raw video.
- Encrypt homework and progress images at rest.
- Use signed URLs for upload/download.
- Add retention policies for images.
- Keep AI/OCR provider keys only on backend.
- Add audit logging for image access.
- Treat homework OCR as untrusted input.
- Prevent prompt injection from worksheet text.
- Avoid judgmental language.

## 16. Analytics Strategy

Metabase will directly consume database views or materialized views.

Useful questions:

- Which assignments are underestimated?
- Which subjects take longer than planned?
- Which students are often behind schedule?
- How often does the child ask for help?
- Are estimates improving over time?
- Focus minutes vs completion time.
- Tutor help usage by subject/task type.

Use Metabase dashboard subscriptions and alerts instead of custom dashboards.

## 17. MVP Milestones

### Phase 1: Foundation

- Repo structure.
- Spring Boot API.
- PostgreSQL schema.
- Object storage integration.
- Local Spring Security authentication.
- Basic student profile.
- Assignment upload.

### Phase 2: Assignment Analysis

- OCR worker.
- Assignment feature extraction.
- Task type detection.
- Basic planner.
- Timeline API.

### Phase 3: Study Session

- Session start/pause/resume/end.
- Timeline UI.
- Manual checkpoint completion.
- Actual vs planned tracking.
- Student study history list.
- Assignment detail with started/finished time and actual duration.

### Phase 4: Progress Image Detection

- Progress image upload.
- Progress analysis worker.
- Completion estimate.
- Confidence handling.
- Behind/ahead status.

### Phase 5: Focus Monitoring

- On-device focus event generation.
- Focus event API.
- In-app nudges.
- Focus analytics.

### Phase 6: Ask Tutor

- Help icon.
- Text question support.
- Image/crop question support.
- Hint-first tutor response.
- Check-my-answer mode.
- Persist asked-at time and show student question history inside assignment detail.

### Phase 7: Analytics

- Analytics event table.
- Materialized views.
- Metabase dashboards.
- Metabase subscriptions/alerts.

### Phase 8: Personalization

- Speed profile updates.
- Subject-specific estimation.
- Better replanning.
- Estimate accuracy improvements.

## 18. Key Risks

- OCR may fail on messy images or handwriting.
- Word count alone may produce bad time estimates.
- Image-based progress detection can be uncertain.
- Focus detection can feel invasive if not carefully designed.
- Children may be behind because they are confused, not distracted.
- AI must not become a direct-answer shortcut.
- Cost can rise with image and AI calls.

## 19. Risk Mitigations

- Use effort units instead of word count only.
- Keep manual progress as a reliable fallback.
- Store confidence scores for AI/CV outputs.
- Ask for confirmation on low-confidence results.
- Keep focus processing on-device.
- Use hint-first tutor behavior.
- Log estimation accuracy and improve over time.
- Add provider-cost tracking early.

## 20. Open Product Decisions

- Exact default focus thresholds by age/class.
- Whether direct answers are ever allowed.
- Which OCR provider to start with.
- Whether mobile focus detection uses ML Kit directly or a React Native bridge.
- How long homework/progress images should be retained.
- When to add a queue or broker after MVP scale requires it.

## 21. Initial Stack Decision

Preferred first implementation:

```text
Frontend: Expo React Native + TypeScript
Backend: Spring Boot + Java 21
Workers: Python 3.11+
Database: PostgreSQL
Storage: S3/GCS/Supabase Storage
Analytics: Metabase
AI: OpenAI API
OCR: Google Cloud Vision, ML Kit, or provider abstraction
Focus: ML Kit on mobile/tablet
```
