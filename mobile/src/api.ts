import * as SecureStore from "expo-secure-store";

export type AuthResponse = {
  token: string;
  student: {
    id: string;
    username: string;
    displayName: string;
    age: number;
    gradeLevel: number;
    language: string;
  };
};

export type Assignment = {
  id: string;
  status: string;
  subject?: string;
  taskType?: string;
  estimatedWordCount?: number;
  questionCount?: number;
  estimatedTotalMinutes?: number;
  confidence?: number;
  summary?: string;
  steps: PlanStep[];
};

export type PlanStep = {
  stepOrder: number;
  title: string;
  plannedStartMinute: number;
  plannedEndMinute: number;
};

export type StudySession = {
  id: string;
  assignmentId: string;
  status: string;
  currentStepOrder: number;
  startedAt: string;
  progressCaptures: ProgressCapture[];
};

export type ProgressCapture = {
  id: string;
  completionPercent?: number;
  confidence?: number;
  behindMinutes?: number;
  summary?: string;
};

export type FocusCheck = {
  lookingAway: boolean;
  alert: boolean;
  confidence: number;
  reason: string;
  alertMessage: string;
};

export type TutorThread = {
  id: string;
  sessionId?: string;
};

export type TutorMessage = {
  id: string;
  studentRole: string;
  tutorRole: string;
  content: string;
};

const TOKEN_KEY = "ai_tutor_token";
const API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export async function saveToken(token: string) {
  await SecureStore.setItemAsync(TOKEN_KEY, token);
}

export async function getToken() {
  return SecureStore.getItemAsync(TOKEN_KEY);
}

export async function login(username: string, password: string) {
  return post<AuthResponse>("/api/auth/login", { username, password }, false);
}

export async function register(payload: {
  username: string;
  password: string;
  displayName: string;
  age: number;
  gradeLevel: number;
  language: string;
}) {
  return post<AuthResponse>("/api/auth/register", payload, false);
}

export async function createAssignment(subject: string) {
  return post<Assignment>("/api/assignments", { subject });
}

export async function getSubjects() {
  return get<string[]>("/api/assignments/subjects");
}

export async function uploadHomeworkImage(assignmentId: string, uri: string) {
  return upload<Assignment>(`/api/assignments/${assignmentId}/homework-image`, uri);
}

export async function analyzeAssignment(assignmentId: string) {
  return post<Assignment>(`/api/assignments/${assignmentId}/analyze`, {});
}

export async function startSession(assignmentId: string) {
  return post<StudySession>("/api/study-sessions", { assignmentId });
}

export async function completeStep(sessionId: string, stepOrder: number) {
  return post<StudySession>(`/api/study-sessions/${sessionId}/steps/${stepOrder}/complete`, {});
}

export async function uploadProgressCapture(sessionId: string, uri: string) {
  return upload<ProgressCapture>(`/api/study-sessions/${sessionId}/progress-captures`, uri);
}

export async function uploadFocusCheck(sessionId: string, uri: string) {
  return upload<FocusCheck>(`/api/study-sessions/${sessionId}/focus-checks`, uri);
}

export async function sendFocusEvent(sessionId: string, eventType: string, durationSeconds: number, note?: string) {
  return post(`/api/study-sessions/${sessionId}/focus-events`, { eventType, durationSeconds, note });
}

export async function createTutorThread(sessionId?: string) {
  return post<TutorThread>("/api/tutor/threads", { sessionId });
}

export async function sendTutorMessage(threadId: string, mode: string, content: string) {
  return post<TutorMessage>(`/api/tutor/threads/${threadId}/messages`, { mode, content });
}

async function post<T>(path: string, body: unknown, needsAuth = true): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (needsAuth) {
    const token = await getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers,
    body: JSON.stringify(body)
  });

  return parseResponse<T>(response);
}

async function get<T>(path: string, needsAuth = true): Promise<T> {
  const headers: Record<string, string> = {};
  if (needsAuth) {
    const token = await getToken();
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "GET",
    headers
  });

  return parseResponse<T>(response);
}

async function upload<T>(path: string, uri: string): Promise<T> {
  const token = await getToken();
  const data = new FormData();
  data.append("file", {
    uri,
    name: "capture.jpg",
    type: "image/jpeg"
  } as unknown as Blob);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    body: data
  });

  return parseResponse<T>(response);
}

async function parseResponse<T>(response: Response): Promise<T> {
  const text = await response.text();
  const json = text ? JSON.parse(text) : {};
  if (!response.ok) {
    throw new Error(json.message ?? "Request failed");
  }
  return json as T;
}
