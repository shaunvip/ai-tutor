import * as ImagePicker from "expo-image-picker";
import * as Speech from "expo-speech";
import { Ionicons } from "@expo/vector-icons";
import { CameraView, useCameraPermissions } from "expo-camera";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { ReactNode } from "react";
import {
  ActivityIndicator,
  Alert,
  Modal,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View
} from "react-native";

import {
  Assignment,
  FocusCheck,
  ProgressCapture,
  StudySession,
  analyzeAssignment,
  completeStep,
  createAssignment,
  createTutorThread,
  login,
  register,
  saveToken,
  sendTutorMessage,
  startSession,
  uploadHomeworkImage,
  uploadFocusCheck,
  uploadProgressCapture
} from "./src/api";

type AuthMode = "login" | "register";
type CaptureRate = 3 | 4;

export default function StudyHome() {
  const [cameraPermission, requestCameraPermission] = useCameraPermissions();
  const autoCameraRef = useRef<CameraView | null>(null);
  const autoCaptureBusyRef = useRef(false);
  const focusCheckBusyRef = useRef(false);
  const lastFocusAlertAtRef = useRef(0);
  const [authMode, setAuthMode] = useState<AuthMode>("register");
  const [tokenReady, setTokenReady] = useState(false);
  const [username, setUsername] = useState("student1");
  const [password, setPassword] = useState("password1");
  const [displayName, setDisplayName] = useState("Student");
  const [age, setAge] = useState("8");
  const [gradeLevel, setGradeLevel] = useState("3");
  const [subject, setSubject] = useState("Math");
  const [assignment, setAssignment] = useState<Assignment | null>(null);
  const [session, setSession] = useState<StudySession | null>(null);
  const [latestProgress, setLatestProgress] = useState<ProgressCapture | null>(null);
  const [latestFocusCheck, setLatestFocusCheck] = useState<FocusCheck | null>(null);
  const [question, setQuestion] = useState("");
  const [tutorReply, setTutorReply] = useState("");
  const [tutorBusy, setTutorBusy] = useState(false);
  const [focusSeconds, setFocusSeconds] = useState(0);
  const [focusWatchOn, setFocusWatchOn] = useState(false);
  const [focusAlertText, setFocusAlertText] = useState("");
  const [focusCheckError, setFocusCheckError] = useState("");
  const [nudgeVisible, setNudgeVisible] = useState(false);
  const [autoCaptureOn, setAutoCaptureOn] = useState(false);
  const [autoCaptureReady, setAutoCaptureReady] = useState(false);
  const [autoCapturesPerMinute, setAutoCapturesPerMinute] = useState<CaptureRate>(3);
  const [autoCaptureCount, setAutoCaptureCount] = useState(0);
  const [autoCaptureError, setAutoCaptureError] = useState("");
  const [busy, setBusy] = useState(false);

  const currentStep = useMemo(() => {
    if (!assignment || !session) return null;
    return assignment.steps.find((step) => step.stepOrder === session.currentStepOrder) ?? assignment.steps.at(-1);
  }, [assignment, session]);

  const focusNudgeText = focusAlertText || `Back to ${currentStep?.title ?? "your work"}.`;
  const autoCaptureIntervalSeconds = Math.round(60 / autoCapturesPerMinute);
  const studyCameraOn = autoCaptureOn || focusWatchOn;

  useEffect(() => {
    if (!focusWatchOn || !session) return;
    const interval = setInterval(() => {
      setFocusSeconds((value) => value + 1);
    }, 1000);
    return () => clearInterval(interval);
  }, [focusWatchOn, session]);

  const runAutoProgressCapture = useCallback(async () => {
    const sessionId = session?.id;
    if (!sessionId || !autoCameraRef.current || autoCaptureBusyRef.current) return;

    autoCaptureBusyRef.current = true;
    setAutoCaptureError("");
    try {
      const picture = await autoCameraRef.current.takePictureAsync({
        quality: 0.45
      });
      if (!picture?.uri) return;
      const progress = await uploadProgressCapture(sessionId, picture.uri);
      setLatestProgress(progress);
      setAutoCaptureCount((value) => value + 1);
    } catch (error) {
      setAutoCaptureError(error instanceof Error ? error.message : "Auto capture failed");
    } finally {
      autoCaptureBusyRef.current = false;
    }
  }, [session?.id]);

  useEffect(() => {
    if (!autoCaptureOn || !session || !autoCaptureReady || !cameraPermission?.granted) return;
    const interval = setInterval(() => {
      void runAutoProgressCapture();
    }, autoCaptureIntervalSeconds * 1000);
    return () => clearInterval(interval);
  }, [
    autoCaptureIntervalSeconds,
    autoCaptureOn,
    autoCaptureReady,
    cameraPermission?.granted,
    runAutoProgressCapture,
    session
  ]);

  const runFocusCheck = useCallback(async () => {
    const sessionId = session?.id;
    if (
      !sessionId ||
      !autoCameraRef.current ||
      focusCheckBusyRef.current ||
      autoCaptureBusyRef.current ||
      nudgeVisible
    ) {
      return;
    }

    focusCheckBusyRef.current = true;
    setFocusCheckError("");
    try {
      const picture = await autoCameraRef.current.takePictureAsync({
        quality: 0.35
      });
      if (!picture?.uri) return;
      const focusCheck = await uploadFocusCheck(sessionId, picture.uri);
      setLatestFocusCheck(focusCheck);
      if (focusCheck.alert) {
        const now = Date.now();
        if (now - lastFocusAlertAtRef.current > 10_000) {
          const message = focusCheck.alertMessage || "Please look back at your book and continue.";
          lastFocusAlertAtRef.current = now;
          setFocusAlertText(message);
          setNudgeVisible(true);
          speakNudge(message);
        }
      }
    } catch (error) {
      setFocusCheckError(error instanceof Error ? error.message : "Focus check failed");
    } finally {
      focusCheckBusyRef.current = false;
    }
  }, [nudgeVisible, session?.id]);

  useEffect(() => {
    if (!focusWatchOn || !session || !autoCaptureReady || !cameraPermission?.granted) return;
    void runFocusCheck();
    const interval = setInterval(() => {
      void runFocusCheck();
    }, 5000);
    return () => clearInterval(interval);
  }, [autoCaptureReady, cameraPermission?.granted, focusWatchOn, runFocusCheck, session]);

  useEffect(() => {
    if (!session) {
      setAutoCaptureOn(false);
      setAutoCaptureReady(false);
      setFocusWatchOn(false);
    }
  }, [session]);

  async function run<T>(action: () => Promise<T>, onSuccess?: (value: T) => void) {
    try {
      setBusy(true);
      const value = await action();
      onSuccess?.(value);
    } catch (error) {
      Alert.alert("Action failed", error instanceof Error ? error.message : "Unknown error");
    } finally {
      setBusy(false);
    }
  }

  async function handleAuth() {
    await run(
      async () => {
        const response =
          authMode === "login"
            ? await login(username, password)
            : await register({
                username,
                password,
                displayName,
                age: Number(age),
                gradeLevel: Number(gradeLevel),
                language: "en"
              });
        await saveToken(response.token);
        return response.token;
      },
      () => setTokenReady(true)
    );
  }

  async function pickImage() {
    const permission = await ImagePicker.requestCameraPermissionsAsync();
    if (!permission.granted) {
      Alert.alert("Camera needed", "Camera access is required for homework capture.");
      return;
    }

    const capture = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8
    });

    if (capture.canceled || capture.assets.length === 0) return;
    const uri = capture.assets[0].uri;

    await run(async () => {
      const created = await createAssignment(subject);
      await uploadHomeworkImage(created.id, uri);
      return analyzeAssignment(created.id);
    }, setAssignment);
  }

  async function handleStartSession() {
    if (!assignment) return;
    await run(() => startSession(assignment.id), setSession);
  }

  async function handleCompleteStep(stepOrder: number) {
    if (!session) return;
    await run(() => completeStep(session.id, stepOrder), setSession);
  }

  async function handleProgressCapture() {
    if (!session) return;

    const permission = await ImagePicker.requestCameraPermissionsAsync();
    if (!permission.granted) {
      Alert.alert("Camera needed", "Camera access is required for progress capture.");
      return;
    }

    const capture = await ImagePicker.launchCameraAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      quality: 0.8
    });

    if (capture.canceled || capture.assets.length === 0) return;
    await run(() => uploadProgressCapture(session.id, capture.assets[0].uri), setLatestProgress);
  }

  async function handleToggleAutoCapture() {
    if (autoCaptureOn) {
      setAutoCaptureOn(false);
      if (!focusWatchOn) setAutoCaptureReady(false);
      return;
    }

    const permission = cameraPermission?.granted ? cameraPermission : await requestCameraPermission();
    if (!permission.granted) {
      Alert.alert("Camera needed", "Camera access is required for automatic progress capture.");
      return;
    }

    setAutoCaptureCount(0);
    setAutoCaptureError("");
    setAutoCaptureReady(false);
    setAutoCaptureOn(true);
  }

  async function handleToggleFocusWatch() {
    if (focusWatchOn) {
      setFocusWatchOn(false);
      if (!autoCaptureOn) setAutoCaptureReady(false);
      return;
    }

    const permission = cameraPermission?.granted ? cameraPermission : await requestCameraPermission();
    if (!permission.granted) {
      Alert.alert("Camera needed", "Camera access is required to watch focus.");
      return;
    }

    setFocusSeconds(0);
    setFocusAlertText("");
    setFocusCheckError("");
    setLatestFocusCheck(null);
    setAutoCaptureReady(false);
    setFocusWatchOn(true);
  }

  async function handleTutorAsk() {
    if (!question.trim()) return;
    setTutorBusy(true);
    setTutorReply("");
    try {
      const thread = await createTutorThread(session?.id);
      const message = await sendTutorMessage(thread.id, "hint", question);
      const reply = message.content || "Try reading the question again and tell me which word is confusing.";
      setTutorReply(reply);
      Alert.alert("Tutor Hint", reply);
    } catch (error) {
      Alert.alert("Hint failed", error instanceof Error ? error.message : "Unknown error");
    } finally {
      setTutorBusy(false);
    }
  }

  function speakNudge(text: string) {
    Speech.stop();
    Speech.speak(text, {
      language: "en",
      pitch: 1,
      rate: 0.92
    });
  }

  if (!tokenReady) {
    return (
      <SafeAreaView style={styles.screen}>
        <View style={styles.authPanel}>
          <Text style={styles.title}>AI Tutor</Text>
          <View style={styles.segment}>
            <SegmentButton label="Register" active={authMode === "register"} onPress={() => setAuthMode("register")} />
            <SegmentButton label="Login" active={authMode === "login"} onPress={() => setAuthMode("login")} />
          </View>
          <TextInput style={styles.input} value={username} onChangeText={setUsername} autoCapitalize="none" />
          <TextInput style={styles.input} value={password} onChangeText={setPassword} secureTextEntry />
          {authMode === "register" ? (
            <>
              <TextInput style={styles.input} value={displayName} onChangeText={setDisplayName} />
              <View style={styles.row}>
                <TextInput style={[styles.input, styles.flex]} value={age} onChangeText={setAge} keyboardType="number-pad" />
                <TextInput
                  style={[styles.input, styles.flex]}
                  value={gradeLevel}
                  onChangeText={setGradeLevel}
                  keyboardType="number-pad"
                />
              </View>
            </>
          ) : null}
          <ActionButton icon={<Ionicons name="checkmark" size={18} color="#fff" />} label={authMode === "login" ? "Login" : "Start"} onPress={handleAuth} />
        </View>
        <BusyOverlay visible={busy} />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.screen}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <View>
            <Text style={styles.eyebrow}>Study Session</Text>
            <Text style={styles.title}>AI Tutor</Text>
          </View>
          <Pressable
            accessibilityLabel="Ask tutor"
            style={styles.iconButton}
            onPress={() => {
              setQuestion((value) => value || `I need help with ${currentStep?.title ?? "the current question"}.`);
              setTutorReply("");
            }}
          >
            <Ionicons name="hand-left-outline" size={22} color="#243447" />
          </Pressable>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Homework</Text>
          <View style={styles.row}>
            <TextInput style={[styles.input, styles.flex]} value={subject} onChangeText={setSubject} />
            <ActionButton icon={<Ionicons name="camera-outline" size={18} color="#fff" />} label="Scan" onPress={pickImage} compact />
          </View>
        </View>

        {assignment ? (
          <View style={styles.section}>
            <View style={styles.metricRow}>
              <Metric label="Time" value={`${assignment.estimatedTotalMinutes ?? "-"}m`} />
              <Metric label="Questions" value={`${assignment.questionCount ?? "-"}`} />
              <Metric label="Words" value={`${assignment.estimatedWordCount ?? "-"}`} />
            </View>
            <Timeline assignment={assignment} currentStepOrder={session?.currentStepOrder ?? 1} onComplete={handleCompleteStep} />
            {!session ? (
              <ActionButton icon={<Ionicons name="play" size={18} color="#fff" />} label="Start Session" onPress={handleStartSession} />
            ) : null}
          </View>
        ) : null}

        {session ? (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>{currentStep?.title ?? "Current step"}</Text>
            <View style={styles.row}>
              <ActionButton icon={<Ionicons name="camera-outline" size={18} color="#fff" />} label="Progress" onPress={handleProgressCapture} compact />
              <ActionButton
                icon={<Ionicons name="timer-outline" size={18} color="#fff" />}
                label={focusWatchOn ? `${focusSeconds}s` : "Focus"}
                onPress={handleToggleFocusWatch}
                compact
                secondary
              />
            </View>
            <View style={styles.autoCapturePanel}>
              <View style={styles.autoCaptureHeader}>
                <View>
                  <Text style={styles.autoCaptureTitle}>Auto Capture</Text>
                  <Text style={styles.muted}>
                    {autoCaptureOn ? `${autoCaptureCount} saved · every ${autoCaptureIntervalSeconds}s` : "Progress off"}
                  </Text>
                  {focusWatchOn ? <Text style={styles.muted}>Focus check every 5s</Text> : null}
                </View>
                <ActionButton
                  icon={<Ionicons name={autoCaptureOn ? "pause" : "aperture-outline"} size={18} color="#fff" />}
                  label={autoCaptureOn ? "Stop" : "Auto"}
                  onPress={handleToggleAutoCapture}
                  compact
                  secondary={autoCaptureOn}
                />
              </View>
              <View style={styles.captureRateRow}>
                <SegmentButton
                  label="3/min"
                  active={autoCapturesPerMinute === 3}
                  onPress={() => setAutoCapturesPerMinute(3)}
                />
                <SegmentButton
                  label="4/min"
                  active={autoCapturesPerMinute === 4}
                  onPress={() => setAutoCapturesPerMinute(4)}
                />
              </View>
              {studyCameraOn && cameraPermission?.granted ? (
                <CameraView
                  ref={autoCameraRef}
                  style={styles.cameraPreview}
                  facing="front"
                  mode="picture"
                  mirror
                  active={studyCameraOn}
                  onCameraReady={() => setAutoCaptureReady(true)}
                  onMountError={(event) => setAutoCaptureError(event.message)}
                />
              ) : null}
              {autoCaptureError ? <Text style={styles.errorText}>{autoCaptureError}</Text> : null}
              {focusCheckError ? <Text style={styles.errorText}>{focusCheckError}</Text> : null}
              {latestFocusCheck ? (
                <Text style={styles.muted}>
                  Focus: {latestFocusCheck.lookingAway ? "looking away" : "ok"} ·{" "}
                  {Math.round(latestFocusCheck.confidence * 100)}%
                </Text>
              ) : null}
            </View>
            {latestProgress ? (
              <View style={styles.progressBand}>
                <Text style={styles.progressText}>{latestProgress.completionPercent ?? 0}% complete</Text>
                <Text style={styles.muted}>{latestProgress.summary}</Text>
              </View>
            ) : null}
          </View>
        ) : null}

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Ask Tutor</Text>
          <TextInput
            style={[styles.input, styles.questionInput]}
            value={question}
            onChangeText={setQuestion}
            multiline
          />
          <ActionButton icon={<Ionicons name="help-circle-outline" size={18} color="#fff" />} label="Hint" onPress={handleTutorAsk} />
          {tutorBusy ? (
            <View style={styles.reply}>
              <ActivityIndicator color="#255f85" />
            </View>
          ) : null}
          {tutorReply ? (
            <View style={styles.reply}>
              <Text style={styles.replyLabel}>Tutor Hint</Text>
              <Text style={styles.replyText}>{tutorReply}</Text>
            </View>
          ) : null}
        </View>
      </ScrollView>

      <Modal visible={nudgeVisible} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalPanel}>
            <Ionicons name="book-outline" size={28} color="#243447" />
            <Text style={styles.modalTitle}>{focusNudgeText}</Text>
            <Pressable style={styles.replayButton} onPress={() => speakNudge(focusNudgeText)}>
              <Ionicons name="volume-high-outline" size={18} color="#255f85" />
              <Text style={styles.replayText}>Repeat</Text>
            </Pressable>
            <ActionButton
              icon={<Ionicons name="checkmark" size={18} color="#fff" />}
              label="Continue"
              onPress={() => {
                Speech.stop();
                setNudgeVisible(false);
                setFocusAlertText("");
                setFocusSeconds(0);
              }}
            />
          </View>
        </View>
      </Modal>
      <BusyOverlay visible={busy} />
    </SafeAreaView>
  );
}

function SegmentButton({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable style={[styles.segmentButton, active && styles.segmentButtonActive]} onPress={onPress}>
      <Text style={[styles.segmentText, active && styles.segmentTextActive]}>{label}</Text>
    </Pressable>
  );
}

function ActionButton({
  icon,
  label,
  onPress,
  compact,
  secondary
}: {
  icon: ReactNode;
  label: string;
  onPress: () => void;
  compact?: boolean;
  secondary?: boolean;
}) {
  return (
    <Pressable style={[styles.actionButton, compact && styles.actionCompact, secondary && styles.actionSecondary]} onPress={onPress}>
      {icon}
      <Text style={styles.actionText}>{label}</Text>
    </Pressable>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.metric}>
      <Text style={styles.metricLabel}>{label}</Text>
      <Text style={styles.metricValue}>{value}</Text>
    </View>
  );
}

function Timeline({
  assignment,
  currentStepOrder,
  onComplete
}: {
  assignment: Assignment;
  currentStepOrder: number;
  onComplete: (stepOrder: number) => void;
}) {
  return (
    <View style={styles.timeline}>
      {assignment.steps.map((step) => {
        const isDone = step.stepOrder < currentStepOrder;
        const isCurrent = step.stepOrder === currentStepOrder;
        return (
          <View key={step.stepOrder} style={[styles.step, isCurrent && styles.stepCurrent]}>
            <View style={[styles.stepDot, isDone && styles.stepDotDone]} />
            <View style={styles.flex}>
              <Text style={styles.stepTitle}>{step.title}</Text>
              <Text style={styles.muted}>
                {step.plannedStartMinute}-{step.plannedEndMinute} min
              </Text>
            </View>
            {!isDone ? (
              <Pressable style={styles.smallCheck} onPress={() => onComplete(step.stepOrder)}>
                <Ionicons name="checkmark" size={16} color="#1f7a4d" />
              </Pressable>
            ) : null}
          </View>
        );
      })}
    </View>
  );
}

function BusyOverlay({ visible }: { visible: boolean }) {
  if (!visible) return null;
  return (
    <View style={styles.busy}>
      <ActivityIndicator color="#fff" />
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: "#f5f7f8"
  },
  content: {
    padding: 18,
    gap: 14
  },
  authPanel: {
    flex: 1,
    justifyContent: "center",
    padding: 22,
    gap: 12
  },
  header: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between",
    marginBottom: 4
  },
  title: {
    color: "#1f2933",
    fontSize: 30,
    fontWeight: "700",
    letterSpacing: 0
  },
  eyebrow: {
    color: "#657786",
    fontSize: 13,
    fontWeight: "600",
    letterSpacing: 0,
    textTransform: "uppercase"
  },
  section: {
    backgroundColor: "#fff",
    borderColor: "#d9e1e7",
    borderRadius: 8,
    borderWidth: 1,
    gap: 12,
    padding: 14
  },
  sectionTitle: {
    color: "#243447",
    fontSize: 18,
    fontWeight: "700",
    letterSpacing: 0
  },
  input: {
    backgroundColor: "#fff",
    borderColor: "#cbd5de",
    borderRadius: 8,
    borderWidth: 1,
    color: "#1f2933",
    fontSize: 16,
    minHeight: 46,
    paddingHorizontal: 12
  },
  questionInput: {
    minHeight: 86,
    paddingTop: 10,
    textAlignVertical: "top"
  },
  row: {
    alignItems: "center",
    flexDirection: "row",
    gap: 10
  },
  flex: {
    flex: 1
  },
  segment: {
    backgroundColor: "#dbe5ea",
    borderRadius: 8,
    flexDirection: "row",
    padding: 3
  },
  segmentButton: {
    alignItems: "center",
    borderRadius: 6,
    flex: 1,
    paddingVertical: 10
  },
  segmentButtonActive: {
    backgroundColor: "#fff"
  },
  segmentText: {
    color: "#52616b",
    fontWeight: "700"
  },
  segmentTextActive: {
    color: "#243447"
  },
  actionButton: {
    alignItems: "center",
    backgroundColor: "#255f85",
    borderRadius: 8,
    flexDirection: "row",
    gap: 8,
    justifyContent: "center",
    minHeight: 46,
    paddingHorizontal: 14
  },
  actionCompact: {
    minWidth: 116
  },
  actionSecondary: {
    backgroundColor: "#576f53"
  },
  actionText: {
    color: "#fff",
    fontSize: 15,
    fontWeight: "700"
  },
  iconButton: {
    alignItems: "center",
    backgroundColor: "#fff",
    borderColor: "#d9e1e7",
    borderRadius: 8,
    borderWidth: 1,
    height: 44,
    justifyContent: "center",
    width: 44
  },
  metricRow: {
    flexDirection: "row",
    gap: 10
  },
  metric: {
    backgroundColor: "#eef3f5",
    borderRadius: 8,
    flex: 1,
    padding: 10
  },
  metricLabel: {
    color: "#657786",
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 0
  },
  metricValue: {
    color: "#1f2933",
    fontSize: 21,
    fontWeight: "800",
    letterSpacing: 0
  },
  timeline: {
    gap: 8
  },
  step: {
    alignItems: "center",
    borderColor: "#e1e8ed",
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: 10,
    minHeight: 58,
    padding: 10
  },
  stepCurrent: {
    borderColor: "#255f85"
  },
  stepDot: {
    backgroundColor: "#aebbc5",
    borderRadius: 6,
    height: 12,
    width: 12
  },
  stepDotDone: {
    backgroundColor: "#1f7a4d"
  },
  stepTitle: {
    color: "#243447",
    fontSize: 15,
    fontWeight: "700"
  },
  muted: {
    color: "#657786",
    fontSize: 13,
    letterSpacing: 0
  },
  smallCheck: {
    alignItems: "center",
    backgroundColor: "#e8f4ed",
    borderRadius: 8,
    height: 34,
    justifyContent: "center",
    width: 34
  },
  autoCapturePanel: {
    borderColor: "#e1e8ed",
    borderRadius: 8,
    borderWidth: 1,
    gap: 10,
    padding: 10
  },
  autoCaptureHeader: {
    alignItems: "center",
    flexDirection: "row",
    gap: 10,
    justifyContent: "space-between"
  },
  autoCaptureTitle: {
    color: "#243447",
    fontSize: 15,
    fontWeight: "800"
  },
  captureRateRow: {
    backgroundColor: "#dbe5ea",
    borderRadius: 8,
    flexDirection: "row",
    padding: 3
  },
  cameraPreview: {
    backgroundColor: "#1f2933",
    borderRadius: 8,
    height: 180,
    overflow: "hidden",
    width: "100%"
  },
  errorText: {
    color: "#b42318",
    fontSize: 13,
    fontWeight: "600"
  },
  progressBand: {
    backgroundColor: "#eef3f5",
    borderRadius: 8,
    padding: 10
  },
  progressText: {
    color: "#243447",
    fontSize: 16,
    fontWeight: "800"
  },
  reply: {
    alignItems: "flex-start",
    backgroundColor: "#eef3f5",
    borderRadius: 8,
    gap: 6,
    padding: 12
  },
  replyLabel: {
    color: "#255f85",
    fontSize: 12,
    fontWeight: "800",
    letterSpacing: 0,
    textTransform: "uppercase"
  },
  replyText: {
    color: "#243447",
    fontSize: 15,
    lineHeight: 21
  },
  modalBackdrop: {
    alignItems: "center",
    backgroundColor: "rgba(31, 41, 51, 0.42)",
    flex: 1,
    justifyContent: "center",
    padding: 28
  },
  modalPanel: {
    alignItems: "center",
    backgroundColor: "#fff",
    borderRadius: 8,
    gap: 14,
    padding: 18,
    width: "100%"
  },
  modalTitle: {
    color: "#243447",
    fontSize: 19,
    fontWeight: "800",
    textAlign: "center"
  },
  replayButton: {
    alignItems: "center",
    borderColor: "#b8c7d2",
    borderRadius: 8,
    borderWidth: 1,
    flexDirection: "row",
    gap: 8,
    minHeight: 40,
    paddingHorizontal: 12
  },
  replayText: {
    color: "#255f85",
    fontSize: 14,
    fontWeight: "700"
  },
  busy: {
    alignItems: "center",
    backgroundColor: "rgba(31, 41, 51, 0.35)",
    bottom: 0,
    justifyContent: "center",
    left: 0,
    position: "absolute",
    right: 0,
    top: 0
  }
});
